package es.ukanda.playroll.controllers.helpers

import org.json.JSONArray
import org.json.JSONObject
import java.net.DatagramSocket

class ComunicationHelpers {
    companion object{
        fun getMapFromJson(json: String): Map<String, String> {
            try {
                val jsonArray = JSONArray(json)
                val mapList = mutableMapOf<String, String>()
                for (i in 0 until jsonArray.length()) {
                    val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                    val map = jsonObject.toMap()
                    val key = map.values.first()
                    val value = map.values.last()
                    mapList[key] = value
                }

                return mapList
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return emptyMap()

        }

        fun JSONObject.toMap(): Map<String, String> {
            val map = mutableMapOf<String, String>()
            val keys = this.keys()

            while (keys.hasNext()) {
                val key = keys.next() as String
                val value = this.getString(key)
                map[key] = value
            }

            return map
        }

        fun openUdpSocket(port:Int):DatagramSocket{
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket(port)
                return socket
            } catch (e: java.net.BindException) {
                socket?.close()
                throw Exception("Port $port is already in use")
            }
            return socket!!
        }
    }
}