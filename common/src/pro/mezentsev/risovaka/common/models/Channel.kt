package pro.mezentsev.risovaka.common.models

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
    @SerializedName("user_settings")
    USER_SETTINGS,
    NONE
}