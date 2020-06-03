package pro.mezentsev.risovaka.chat.internal

import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pro.mezentsev.risovaka.chat.models.Message
import pro.mezentsev.risovaka.session.SocketSender
import pro.mezentsev.risovaka.session.models.Session

class ChatSender(
    private val sender: SocketSender
) {
    private val scope = CoroutineScope(Dispatchers.Unconfined)

    fun sendTo(to: Session, message: Message) = scope.launch {
        sender.sendTo(to, message.asJsonString())
    }

    fun sendTo(to: WebSocketSession, message: Message) = scope.launch {
        sender.sendTo(to, message.asJsonString())
    }

    fun broadcast(message: Message) = scope.launch {
        sender.broadcast(message.asJsonString())
    }
}