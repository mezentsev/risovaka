package pro.mezentsev.risovaka.chat

import pro.mezentsev.risovaka.chat.internal.ChatSender
import pro.mezentsev.risovaka.chat.internal.ChatServer
import pro.mezentsev.risovaka.chat.models.Message
import pro.mezentsev.risovaka.session.SessionHolder
import pro.mezentsev.risovaka.session.SocketSender
import pro.mezentsev.risovaka.session.models.Session

class ChatRouter(
    sessions: SessionHolder,
    socketSender: SocketSender
) {
    private val chatSender = ChatSender(socketSender)
    private val chatServer = ChatServer(sessions, chatSender)

    fun handleMessage(session: Session, message: Message) {
        val command = message.text
        when {
            command.startsWith("/who") -> chatServer.who(session)
            command.startsWith("/user") -> chatServer.rename(session, command.removePrefix("/user").trim())
            command.startsWith("/help") -> chatServer.help(session)
            command.startsWith("/") -> chatServer.customCommand(session, command)
            else -> chatServer.message(session, command)
        }
    }
}