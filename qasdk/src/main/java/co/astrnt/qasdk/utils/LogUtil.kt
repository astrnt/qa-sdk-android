package co.astrnt.qasdk.utils

import co.astrnt.qasdk.constants.PreferenceKey
import co.astrnt.qasdk.dao.LogDao
import com.orhanobut.hawk.Hawk
import java.text.SimpleDateFormat
import java.util.*

object LogUtil {
    @JvmStatic
    fun addNewLog(interviewCode: String?, itemLog: LogDao) {
        if (interviewCode == null) return
        val logDaoList = getLog(interviewCode)
        if (itemLog.event == "Response API" || itemLog.event == "Hit API") {
            itemLog.event = itemLog.event + " " + lastApiCall
        }
        logDaoList.add(itemLog)
        val set: Set<LogDao> = LinkedHashSet(logDaoList)
        val logWithoutDuplicates: List<LogDao> = ArrayList(set)
        Hawk.put(interviewCode, logWithoutDuplicates)
    }

    @JvmStatic
    fun getLog(interviewCode: String?): MutableList<LogDao> {
        return if (interviewCode == null) {
            ArrayList()
        } else {
            val logDaoList: List<LogDao> = Hawk.get(interviewCode, ArrayList())
            val set: Set<LogDao> = LinkedHashSet(logDaoList)
            val logWithoutDuplicates: MutableList<LogDao> = ArrayList(set)
            Hawk.put<List<LogDao>>(interviewCode, logWithoutDuplicates)
            logWithoutDuplicates
        }
    }

    @JvmStatic
    val timeZone: String
        get() {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault())
            val timeZone = SimpleDateFormat("Z").format(calendar.time)
            return timeZone.substring(0, 3) + ":" + timeZone.substring(3, 5)
        }

    @JvmStatic
    fun clearSentLog(interviewCode: String?, sentLog: List<LogDao>?) {
        val logDaoList = getLog(interviewCode)
        logDaoList.removeAll(sentLog!!)
        Hawk.put<List<LogDao>>(interviewCode, logDaoList)
    }

    val lastApiCall: String?
        get() = Hawk.get<String?>(PreferenceKey.KEY_LAST_API_CALL, null)
    @JvmStatic
    val lastLogIndex: Int
        get() = Hawk.get(PreferenceKey.KEY_LAST_LOG_INDEX, 0)

    @JvmStatic
    fun saveLastLogIndex(index: Int) {
        Hawk.put(PreferenceKey.KEY_LAST_LOG_INDEX, index)
    }
}