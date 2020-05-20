package pro.mezentsev.risovaka.session

import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.close
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.websocket.WebSocketServerSession
import kotlinx.coroutines.channels.ClosedSendChannelException
import pro.mezentsev.risovaka.session.models.Session
import pro.mezentsev.risovaka.session.models.User
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicInteger

class SessionController: SessionHolder, MessageSender {

    private val sessionCounter = AtomicInteger()
    private val users = ConcurrentHashMap<Session, User>()
    private val sessions = ConcurrentHashMap<Session, WebSocketSession>()
    private val lifecycleListeners = CopyOnWriteArraySet<SessionLifecycleListener>()

    suspend fun startSession(socket: WebSocketServerSession): Session? {
        val session = socket.call.sessions.get<Session>()
        // We check that we actually have a session. We should always have one,
        // since we have defined an interceptor before to set one.
        if (session == null) {
            socket.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
            return null
        }

        if (sessions.containsKey(session)) {
            socket.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Session already exists"))
            return null
        }

        sessions.computeIfAbsent(session) { socket }
        val user = users.computeIfAbsent(session) { User("user${sessionCounter.incrementAndGet()}") }

        val listeners = lifecycleListeners
        listeners.forEach { it.onStart(session, user, socket) }

        return session
    }

    fun closeSession(session: Session, socket: WebSocketServerSession) {
        val user = users[session] ?: return

        val listeners = lifecycleListeners
        listeners.forEach { it.onFinish(session, user.copy() , socket) }

        sessions.remove(session)
        users.remove(session)
    }

    override fun all(): List<User> {
        return users.values.toList()
    }

    override fun get(session: Session): User? {
        return users[session]?.copy()
    }

    override fun set(session: Session, user: User) {
        users[session] = user
    }

    override fun setSessionListener(listener: SessionLifecycleListener?) {
        listener
            ?.let { lifecycleListeners.add(listener) }
            ?: lifecycleListeners.remove(listener)
    }

    override suspend fun sendTo(to: Session, text: String) {
        this.sessions
            .filterKeys { it == to }
            .forEach { (_, socket) -> socket.sendFrame(Frame.Text(text)) }
    }

    override suspend fun broadcast(text: String) {
        this.sessions.forEach { (_, socket) -> socket.sendFrame(Frame.Text(text)) }
    }

    private suspend fun WebSocketSession.sendFrame(frame: Frame) {
        try {
            send(frame.copy())
        } catch (t: Throwable) {
            try {
                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, ""))
            } catch (ignore: ClosedSendChannelException) {
                // at some point it will get closed
            }
        }
    }
}