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
import pro.mezentsev.risovaka.chat.models.MessageDto
import pro.mezentsev.risovaka.communication.models.Channel
import pro.mezentsev.risovaka.communication.models.ChannelDto
import pro.mezentsev.risovaka.communication.models.ChannelType
import pro.mezentsev.risovaka.session.SessionController
import pro.mezentsev.risovaka.session.models.Session

class Router {
    private val sessionController = SessionController()
    private val chatRouter = ChatRouter(sessionController, sessionController)
    private val gson = Gson()

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

        Logger.d("Channel: '$channel.'")
        when(channel.type) {
            ChannelType.CHAT -> handleChat(session, channel, command)
            else -> Logger.w("No routers for $command")
        }
    }

    private fun handleChat(session: Session, channel: Channel, json: String) {
        val chatMessage = try { gson.fromJson(json, MessageDto::class.java).message } catch (e: JsonIOException) {
            Logger.e("Can't parse message", e)
            return
        }
        Logger.d("Message: $chatMessage")
        chatRouter.handleMessage(session, chatMessage)
    }

    fun interceptSession(call: ApplicationCall) {
        sessionController.interceptSession(call.sessions)
    }
}