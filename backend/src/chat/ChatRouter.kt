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
        println(command)
        when {
            // The command `who` responds the user about all the member names connected to the user.
            command.startsWith("/who") -> chat.who(session)
            // The command `user` allows the user to set its name.
            command.startsWith("/user") -> {
                val newName = command.removePrefix("/user").trim()
                // We verify that it is a valid name (in terms of length) to prevent abusing
                when {
                    newName.isEmpty() -> sender.sendTo(session, "[server::help] /user [newName]")
                    newName.length > 50 -> sender.sendTo(
                        session,
                        "[server::help] new name is too long: 50 characters limit"
                    )
                    else -> chat.rename(session, newName)
                }
            }
            // The command 'help' allows session to get a list of available commands.
            command.startsWith("/help") -> chat.help(session)
            // If no commands matched at this point, we notify about it.
            command.startsWith("/") -> sender.sendTo(
                session,
                "[server::help] Unknown command ${command.takeWhile { !it.isWhitespace() }}"
            )
            // Handle a normal message.
            else -> chat.message(session, command)
        }
    }
}