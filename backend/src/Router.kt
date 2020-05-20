package pro.mezentsev.risovaka

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.routing.Routing
import io.ktor.websocket.webSocket
import kotlinx.coroutines.channels.consumeEach
import pro.mezentsev.risovaka.chat.ChatRouter
import pro.mezentsev.risovaka.session.SessionController
import pro.mezentsev.risovaka.session.models.Session

class Router {
    private val sessionController = SessionController()
    private val chatRouter = ChatRouter(sessionController, sessionController)

    fun websocket(routing: Routing) {
        routing.webSocket("/ws") {
            val session = sessionController.startSession(this) ?: return@webSocket
            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        receivedMessage(session, frame.readText())
                    }
                }
            } finally {
                sessionController.closeSession(session, this)
            }
        }
    }

    private suspend fun receivedMessage(
        session: Session,
        command: String
    ) {
        when {
            command.startsWith("/chat") ->
                chatRouter.handleMessage(session, command.removePrefix("/chat"))
        }
    }
}