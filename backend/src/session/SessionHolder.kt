package pro.mezentsev.risovaka.session

import io.ktor.http.cio.websocket.WebSocketSession
import pro.mezentsev.risovaka.session.models.Session
import pro.mezentsev.risovaka.session.models.User

interface SessionHolder {
    fun all(): List<User>
    operator fun get(session: Session): User?
    operator fun set(session: Session, user: User)
    fun setSessionListener(listener: SessionLifecycleListener?)
}

interface SessionLifecycleListener {
    fun onStart(session: Session, user: User, ws: WebSocketSession)
    fun onFinish(session: Session, user: User, ws: WebSocketSession)
}