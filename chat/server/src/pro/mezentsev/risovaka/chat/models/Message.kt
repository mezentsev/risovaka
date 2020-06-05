package pro.mezentsev.risovaka.chat.models

import pro.mezentsev.risovaka.common.models.Channel
import pro.mezentsev.risovaka.common.models.ChannelType
import java.util.*

data class MessageDto(
    val message: Message
)

data class Message(
    val text: String = "",
    val id: String = UUID.randomUUID().toString(),
    val timestamp: String = System.currentTimeMillis().toString(),
    val type: MessageType = MessageType.BROADCAST,
    val from: String? = null,
    val channel: Channel = Channel(ChannelType.CHAT)
)

enum class MessageType{
    BROADCAST,
    SYSTEM
}