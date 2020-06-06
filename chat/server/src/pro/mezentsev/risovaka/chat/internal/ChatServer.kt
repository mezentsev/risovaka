package pro.mezentsev.risovaka.chat.internal

import io.ktor.http.cio.websocket.WebSocketSession
import pro.mezentsev.risovaka.chat.models.Message
import pro.mezentsev.risovaka.chat.models.MessageType
import pro.mezentsev.risovaka.session.SessionHolder
import pro.mezentsev.risovaka.session.SessionLifecycleListener
import pro.mezentsev.risovaka.session.models.Session
import pro.mezentsev.risovaka.session.models.User
import java.util.*

internal class ChatServer(
    private val sessions: SessionHolder,
    private val sender: ChatSender
) {
    private val lastMessages = LinkedList<Message>()

    init {
        sessions.setSessionListener(object : SessionLifecycleListener {
            override fun onStart(
                session: Session,
                user: User,
                sessionSocketsSize: Int,
                openedSocket: WebSocketSession
            ) {
                // Sends the user the latest messages from this server to let the member have a bit context.
                val messages = synchronized(lastMessages) { lastMessages.toList() }
                for (message in messages) {
                    sender.sendTo(openedSocket, message)
                }

                if (sessionSocketsSize == 1) {
                    Message("joined", from = user.name, type = MessageType.SYSTEM).broadcast()
                }
            }

            override fun onFinish(
                session: Session,
                user: User,
                sessionSocketsSize: Int,
                closedSocket: WebSocketSession
            ) {
                if (sessionSocketsSize == 1) {
                    Message("left", from = user.name, type = MessageType.SYSTEM).broadcast()
                }
            }
        })
    }

    /**
     * Handles a user with [session] renaming [to] a specific name.
     */
    fun rename(session: Session, to: String) = when {
        to.isEmpty() -> Message("/user [newName]", type = MessageType.SYSTEM).sendTo(session)
        to.length > 50 -> Message("New name is too long: 50 characters limit", type = MessageType.SYSTEM).sendTo(session)
        else -> {
            val user = sessions[session]

            user?.let {
                sessions[session] = User(to)
                sender.sendUserSettings(session, to)
                Message("Member renamed from [${it.name}] to [$to]", type = MessageType.SYSTEM).broadcast()
            }
        }
    }

    /**
     * Handles the 'who' command by sending the list of all users.
     */
    fun who(session: Session) {
        val listOfUsers = sessions.all().joinToString(", ") { it.name }
        Message("Online: [$listOfUsers]", type = MessageType.SYSTEM).sendTo(session)
    }

    /**
     * Handles the 'help' command by sending the member a list of available commands.
     */
    fun help(session: Session) {
        Message("Possible commands are: /user, /help, /who", type = MessageType.SYSTEM).sendTo(session)
    }

    /**
     * Handles unsupported command.
     */
    fun customCommand(session: Session, command: String) {
        Message("Unknown command ${command.takeWhile { !it.isWhitespace() }}", type = MessageType.SYSTEM).sendTo(session)
    }

    /**
     * Handles a [message] sent from a user with [session] by notifying the rest of the session.
     */
    fun message(session: Session, message: String) {
        val user = sessions[session] ?: return
        Message(message, from = user.name).broadcast()
    }

    private fun Message.sendTo(session: Session) {
        sender.sendTo(session, this)
    }

    private fun Message.sendTo(socket: WebSocketSession) {
        sender.sendTo(socket, this)
    }

    private fun Message.broadcast() {
        sender.broadcast(this)

        // Appends the message to the list of [lastMessages] and caps that collection to 100 items to prevent
        // growing too much.
        synchronized(lastMessages) {
            lastMessages.add(this)
            if (lastMessages.size > 100) {
                lastMessages.removeFirst()
            }
        }
    }
}