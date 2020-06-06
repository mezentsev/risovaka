package pro.mezentsev.risovaka

import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.gson.gson
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.cio.websocket.pingPeriod
import io.ktor.http.cio.websocket.timeout
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.websocket.WebSockets
import kotlinx.css.*
import kotlinx.html.*
import org.slf4j.event.Level
import pro.mezentsev.risovaka.session.models.Session
import pro.mezentsev.risovaka.session.models.User
import java.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private val router by lazy { Router() }

fun Application.module(testing: Boolean = false) {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(DefaultHeaders)
    install(StatusPages)

    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    install(Sessions) {
        cookie<Session>("SESSION") {
            cookie.httpOnly = false
        }
        cookie<User>("USER_SETTINGS") {
            cookie.httpOnly = false
        }
    }

    // This adds an interceptor that will create a specific session in each request if no session is available already.
    intercept(ApplicationCallPipeline.Features) {
        router.interceptSession(call)
    }

    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    routing {
        static("/static") {
            resources("static")
        }

        router.websocket(this)

        static {
            // This marks index.html from the 'web' folder in resources as the default file to serve.
            defaultResource("index.html", "static")
            // This serves files from the 'web' folder in the application resources.
            resources("static")
        }
        /*get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }*/

        get("/html-dsl") {
            call.respondHtml {
                body {
                    h1 { +"HTML" }
                    ul {
                        for (n in 1..10) {
                            li { +"$n" }
                        }
                    }
                }
            }
        }

        get("/styles.css") {
            call.respondCss {
                body {
                    backgroundColor = Color.red
                }
                p {
                    fontSize = 2.em
                }
                rule("p.myclass") {
                    color = Color.blue
                }
            }
        }

        get("/html-freemarker") {
            call.respond(FreeMarkerContent("index.ftl", mapOf("data" to IndexData(
                listOf(1, 2, 3)
            )
            ), ""))
        }

        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }

//        get("/session/increment") {
//            val session = call.sessions.get<Session>() ?: Session(generateNonce())
//            call.sessions.set(session.copy(id = session.id + 1))
//            call.respondText("Counter is ${session.id}. Refresh to increment.")
//        }
    }
}

data class JsonSampleClass(val hello: String)

data class IndexData(val items: List<Int>)

fun FlowOrMetaDataContent.styleCss(builder: CSSBuilder.() -> Unit) {
    style(type = ContentType.Text.CSS.toString()) {
        +CSSBuilder().apply(builder).toString()
    }
}

fun CommonAttributeGroupFacade.style(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
