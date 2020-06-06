package pro.mezentsev.risovaka.chat

import com.google.gson.Gson
import com.google.gson.JsonIOException
import pro.mezentsev.risovaka.chat.internal.ChatSender
import pro.mezentsev.risovaka.chat.internal.ChatServer
import pro.mezentsev.risovaka.chat.models.MessageDto
import pro.mezentsev.risovaka.common.Logger
import pro.mezentsev.risovaka.session.SessionHolder
import pro.mezentsev.risovaka.session.SocketSender
import pro.mezentsev.risovaka.session.models.Session

class ChatRouter(
    sessions: SessionHolder,
    socketSender: SocketSender
) {
    private val chatSender = ChatSender(socketSender)
    private val chatServer = ChatServer(sessions, chatSender)
    private val gson = Gson()

    fun handleMessage(session: Session, json: String) {
        val chatMessage = try { gson.fromJson(json, MessageDto::class.java).message } catch (e: JsonIOException) {
            Logger.e("Can't parse message", e)
            return
        }
        Logger.d("Message: $chatMessage")

        val command = chatMessage.text
        when {
            command.startsWith("/who") -> chatServer.who(session)
            command.startsWith("/user") -> chatServer.rename(session, command.removePrefix("/user").trim())
            command.startsWith("/help") -> chatServer.help(session)
            command.startsWith("/") -> chatServer.customCommand(session, command)
            else -> chatServer.message(session, command)
        }
    }
}