package co.astrnt.qasdk.utils

import java.net.MalformedURLException
import java.net.URL

object DownloadUtils {
    @JvmStatic
    fun getFileNameFromURL(url: String?): String {
        if (url == null) {
            return ""
        }
        try {
            val resource = URL(url)
            val host = resource.host
            if (host.length > 0 && url.endsWith(host)) {
                // handle ...example.com
                return ""
            }
        } catch (e: MalformedURLException) {
            return ""
        }
        val startIndex = url.lastIndexOf('/') + 1
        val length = url.length

        // find end index for ?
        var lastQMPos = url.lastIndexOf('?')
        if (lastQMPos == -1) {
            lastQMPos = length
        }

        // find end index for #
        var lastHashPos = url.lastIndexOf('#')
        if (lastHashPos == -1) {
            lastHashPos = length
        }

        // calculate the end index
        val endIndex = Math.min(lastQMPos, lastHashPos)
        return url.substring(startIndex, endIndex)
    }
}