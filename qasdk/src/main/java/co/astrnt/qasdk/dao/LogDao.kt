package co.astrnt.qasdk.dao

import java.text.SimpleDateFormat
import java.util.*

class LogDao(event: String, message: String) {
    var event: String
    var log_time: String
    var message: String
    override fun equals(other: Any?): Boolean {
        return if (other == null) {
            false
        } else if (other !is LogDao) {
            false
        } else {
            (other.event == event
                    && other.message == message)
        }
    }

    init {
        val yourMilliSeconds = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val resultDate = Date(yourMilliSeconds)
        val logTime = sdf.format(resultDate)
        this.event = event
        log_time = logTime
        this.message = message
    }
}