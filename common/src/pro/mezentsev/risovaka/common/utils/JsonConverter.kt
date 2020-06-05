package pro.mezentsev.risovaka.common.utils

import com.google.gson.Gson

fun Any.asJson() = Gson().toJson(this)