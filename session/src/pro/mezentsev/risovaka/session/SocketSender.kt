package pro.mezentsev.risovaka.session

import io.ktor.http.cio.websocket.WebSocketSession
import pro.mezentsev.risovaka.session.models.Session

interface SocketSender {
    suspend fun sendTo(to: Session, text: String)
    suspend fun sendTo(to: WebSocketSession, text: String)
    suspend fun broadcast(text: String)
}