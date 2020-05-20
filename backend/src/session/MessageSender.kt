package pro.mezentsev.risovaka.session

import pro.mezentsev.risovaka.session.models.Session

interface MessageSender {
    suspend fun sendTo(to: Session, text: String)
    suspend fun broadcast(text: String)
}