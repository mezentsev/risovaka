package pro.mezentsev.risovaka.session

import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.close
import io.ktor.sessions.CurrentSession
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import io.ktor.util.generateNonce
import io.ktor.websocket.WebSocketServerSession
import kotlinx.coroutines.channels.ClosedSendChannelException
import pro.mezentsev.risovaka.common.Logger
import pro.mezentsev.risovaka.session.models.Session
import pro.mezentsev.risovaka.session.models.User
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicInteger

class SessionController: SessionHolder, SocketSender {

    private val sessionCounter = AtomicInteger()
    private val users = ConcurrentHashMap<Session, User>()
    private val sessions = ConcurrentHashMap<Session, CopyOnWriteArrayList<WebSocketSession>>()
    private val lifecycleListeners = CopyOnWriteArraySet<SessionLifecycleListener>()

    fun interceptSession(sessions: CurrentSession) {
        if (sessions.get<Session>() == null) {
            sessions.set(generateSession())
        }
        if (sessions.get<User>() == null) {
            sessions.set(generateUser())
        }
    }

    private fun generateSession() = Session(generateNonce())
    private fun generateUser() = User("user${sessionCounter.getAndIncrement()}")

    suspend fun startSession(socket: WebSocketServerSession): Session? {
        val session = socket.call.sessions.get<Session>()
        val userSettings = socket.call.sessions.get<User>()
        // We check that we actually have a session. We should always have one,
        // since we have defined an interceptor before to set one.
        if (session == null || userSettings == null) {
            socket.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
            return null
        }

        val sockets = sessions.computeIfAbsent(session) { CopyOnWriteArrayList<WebSocketSession>() }
        val user = users.computeIfAbsent(session) { User(userSettings.name) }

        if (sockets.size == 5) {
            socket.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Can't handle more sessions"))
            return null
        }
        sockets.add(socket)

        lifecycleListeners.forEach { listener ->
            listener.onStart(session, user.copy(), sockets.size, socket)
        }

        return session
    }

    fun closeSession(session: Session, socket: WebSocketServerSession) {
        val user = users[session] ?: return
        val sockets = sessions[session] ?: return

        lifecycleListeners.forEach { listener ->
            listener.onFinish(session, user.copy(), sockets.size, socket)
        }

        if (sockets.size == 1) {
            sessions.remove(session)
            users.remove(session)
        } else {
            sockets.remove(socket)
        }
    }

    override fun all(): List<User> {
        return users.values.toList()
    }

    override fun get(session: Session): User? {
        return users[session]?.copy()
    }

    override fun set(session: Session, user: User) {
        users[session] = user.copy()
    }

    override fun setSessionListener(listener: SessionLifecycleListener?) {
        listener
            ?.let { lifecycleListeners.add(listener) }
            ?: lifecycleListeners.remove(listener)
    }

    override suspend fun sendTo(to: Session, text: String) {
        this.sessions
            .filterKeys { it == to }
            .forEach { (_, sockets) -> sockets.send(text) }
    }

    override suspend fun sendTo(to: WebSocketSession, text: String) {
        to.send(Frame.Text(text))
    }

    override suspend fun broadcast(text: String) {
        this.sessions.forEach { (_, sockets) -> sockets.send(text) }
    }

    private suspend fun List<WebSocketSession>.send(text: String) {
        forEach { socket ->
            socket.send(text)
        }
    }

    private suspend fun WebSocketSession.send(text: String) {
        try {
            Logger.d("Send text: $text")
            send(Frame.Text(text))
        } catch (t: Throwable) {
            try {
                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, ""))
            } catch (ignore: ClosedSendChannelException) {
                // at some point it will get closed
            }
        }
    }
}