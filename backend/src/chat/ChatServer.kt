package pro.mezentsev.risovaka.chat

import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pro.mezentsev.risovaka.session.MessageSender
import pro.mezentsev.risovaka.session.SessionHolder
import pro.mezentsev.risovaka.session.SessionLifecycleListener
import pro.mezentsev.risovaka.session.models.Session
import pro.mezentsev.risovaka.session.models.User
import java.util.*

internal class ChatServer(
    private val sessions: SessionHolder,
    private val sender: MessageSender
) {
    private val lastMessages = LinkedList<String>()
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        sessions.setSessionListener(object : SessionLifecycleListener {
            override fun onStart(session: Session, user: User, ws: WebSocketSession) {
                scope.launch {
                    joinUser(session, user)
                }
            }

            override fun onFinish(session: Session, user: User, ws: WebSocketSession) {
                scope.launch {
                    leaveUser(session, user)
                }
            }
        })
    }

    /**
     * Handles that a user with a [session] joined.
     */
    suspend fun joinUser(session: Session, user: User) {
        // Sends the user the latest messages from this server to let the member have a bit context.
        val messages = synchronized(lastMessages) { lastMessages.toList() }
        for (message in messages) {
            sender.sendTo(session, message)
        }

        broadcastMessage("[b][server] [${user.name}] joined")
    }

    /**
     * Handles that a user with [session] left the server.
     */
    suspend fun leaveUser(session: Session, user: User) {
        broadcastMessage("[b][server] [${user.name}] left")
    }

    /**
     * Handles a user with [session] renaming [to] a specific name.
     */
    suspend fun rename(session: Session, to: String) {
        //val oldName = memberNames.put(member, to) ?: member
        val user = sessions[session] ?: return
        sessions[session] = User(to)

        sender.broadcast("[b][server] Member renamed from [${user.name}] to [$to]")
    }

    suspend fun who(session: Session) {
        val listOfUsers = sessions.all().joinToString(", ") { it.name }
        sender.sendTo(session, "[b][server] Online: [$listOfUsers]")
    }

    /**
     * Handles the 'help' command by sending the member a list of available commands.
     */
    suspend fun help(session: Session) {
        sender.sendTo(session, "[b][server] Possible commands are: /user, /help and /who")
    }

    /**
     * Handles a [message] sent from a user with [session] by notifying the rest of the session.
     */
    suspend fun message(session: Session, message: String) {
        // Pre-format the message to be send, to prevent doing it for all the session or connected sockets.
        val user = sessions[session] ?: return
        val formatted = "[${user.name}] $message"

        broadcastMessage(formatted)
    }

    private suspend fun broadcastMessage(message: String) {
        sender.broadcast(message)

        // Appends the message to the list of [lastMessages] and caps that collection to 100 items to prevent
        // growing too much.
        synchronized(lastMessages) {
            lastMessages.add(message)
            println(lastMessages)
            if (lastMessages.size > 100) {
                lastMessages.removeFirst()
            }
        }
    }
}