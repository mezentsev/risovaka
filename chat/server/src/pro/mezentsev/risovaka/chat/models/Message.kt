package pro.mezentsev.risovaka.chat.models

import com.google.gson.Gson
import java.util.*

data class MessageDto(
    val message: Message
)

data class Message(
    val text: String = "",
    val id: String = UUID.randomUUID().toString(),
    val timestamp: String = System.currentTimeMillis().toString(),
    val type: MessageType = MessageType.BROADCAST,
    val from: String? = null
) {
    fun asJsonString() = Gson().toJson(this)
}

enum class MessageType{
    BROADCAST,
    SYSTEM
}