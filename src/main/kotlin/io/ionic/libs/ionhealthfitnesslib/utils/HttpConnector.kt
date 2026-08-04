package io.ionic.libs.ionhealthfitnesslib.utils

import java.net.HttpURLConnection
import java.net.URL

object HttpConnector{
    fun connectionURL(url: URL): String {
        return (url.openConnection() as HttpURLConnection).run {
            requestMethod = "GET"
            inputStream.bufferedReader().use { reader ->
                reader.readText()
            }
        }
    }
}
