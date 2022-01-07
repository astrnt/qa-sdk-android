package co.astrnt.qasdk.dao

import java.text.SimpleDateFormat
import java.util.*

class LogDao(event: String, message: String) {
    var event: String
    var log_time: String
    var message: String
    override fun equals(o: Any?): Boolean {
        return if (o == null) {
            false
        } else if (o !is LogDao) {
            false
        } else {
            (o.event == event
                    && o.message == message)
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