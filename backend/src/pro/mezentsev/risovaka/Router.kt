package pro.mezentsev.risovaka

import com.google.gson.Gson
import com.google.gson.JsonIOException
import io.ktor.application.ApplicationCall
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.routing.Routing
import io.ktor.sessions.sessions
import io.ktor.websocket.webSocket
import kotlinx.coroutines.channels.consumeEach
import pro.mezentsev.risovaka.chat.ChatRouter
import pro.mezentsev.risovaka.common.Logger
import pro.mezentsev.risovaka.common.models.ChannelDto
import pro.mezentsev.risovaka.common.models.ChannelType
import pro.mezentsev.risovaka.session.SessionController
import pro.mezentsev.risovaka.session.models.Session

class Router {
    private val sessionController = SessionController()
    private val chatRouter by lazy { ChatRouter(sessionController, sessionController) }
    private val gson by lazy { Gson() }

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

    private fun receivedMessage(
        session: Session,
        command: String
    ) {
        Logger.d("Command: '$command'")
        val channel = try {
            gson.fromJson(command, ChannelDto::class.java).channel
        } catch (e: JsonIOException) {
            Logger.e("Json problems", e)
            return
        }

        when(channel.type) {
            ChannelType.CHAT -> chatRouter.handleMessage(session, command)
            else -> Logger.w("No routers for $command")
        }
    }

    fun interceptSession(call: ApplicationCall) {
        sessionController.interceptSession(call.sessions)
    }
}