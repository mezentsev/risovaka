package pro.mezentsev.risovaka

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readBytes
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    runBlocking {
        val client = HttpClient(CIO).config {
            install(WebSockets)
        }

        client.ws(
            method = HttpMethod.Get,
            host = "0.0.0.0",
            port = 8080,
            path = "/ws"
        ) {

            send(Frame.Text("/chat/who"))
            while (!incoming.isClosedForReceive) {
                when (val frame = incoming.receive()) {
                    is Frame.Text -> println(frame.readText())
                    is Frame.Binary -> println(frame.readBytes())
                }
            }
        }
    }
}
