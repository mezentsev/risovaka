package pro.mezentsev.risovaka.chat.models

import pro.mezentsev.risovaka.common.models.Action
import pro.mezentsev.risovaka.common.models.Channel
import pro.mezentsev.risovaka.common.models.ChannelType

const val RENAME_ACTION = "rename"

data class RenameAction(
    val name: String = ""
) : Action(RENAME_ACTION, Channel(ChannelType.USER_SETTINGS))