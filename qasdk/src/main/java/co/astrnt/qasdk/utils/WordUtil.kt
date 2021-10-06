package co.astrnt.qasdk.utils

object WordUtil {
    @JvmStatic
    fun createAlphabet(i: Int): String {
        return if (i < 0) "" else createAlphabet(i / 26 - 1) + (65 + i % 26).toChar()
    }
}