package pro.mezentsev.risovaka.communication.models

import com.google.gson.annotations.SerializedName

data class ChannelDto(
    val channel: Channel = Channel()
)

data class Channel(
    val type: ChannelType = ChannelType.NONE,
    val room: String = ""
)

enum class ChannelType {
    @SerializedName("chat")
    CHAT,
    NONE
}