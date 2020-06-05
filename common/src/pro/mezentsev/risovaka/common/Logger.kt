package pro.mezentsev.risovaka.common

import mu.KotlinLogging

object Logger {
    private val logger = KotlinLogging.logger {}

    fun t(message: String, t: Throwable? = null) = logger.trace(t) { message }
    fun d(message: String, t: Throwable? = null) = logger.debug(t) { message }
    fun i(message: String, t: Throwable? = null) = logger.info(t) { message }
    fun w(message: String, t: Throwable? = null) = logger.warn(t) { message }
    fun e(message: String, t: Throwable? = null) = logger.error(t) { message }
}