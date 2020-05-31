package pro.mezentsev.risovaka.chat

import pro.mezentsev.risovaka.session.MessageSender
import pro.mezentsev.risovaka.session.SessionHolder
import pro.mezentsev.risovaka.session.models.Session

class ChatRouter(
    sessions: SessionHolder,
    private val sender: MessageSender
) {
    private val chat = ChatServer(sessions, sender)

    suspend fun handleMessage(session: Session, command: String) {
        when {
            // The command `who` responds the user about all the member names connected to the user.
            command.startsWith("/who") -> chat.who(session)
            // The command `user` allows the user to set its name.
            command.startsWith("/user") -> {
                chat.rename(session, command.removePrefix("/user").trim())
            }
            // The command 'help' allows session to get a list of available commands.
            command.startsWith("/help") -> chat.help(session)
            // If no commands matched at this point, we notify about it.
            command.startsWith("/") -> sender.sendTo(
                session,
                "[b][server] Unknown command ${command.takeWhile { !it.isWhitespace() }}"
            )
            // Handle a normal message.
            else -> chat.message(session, command)
        }
    }
}