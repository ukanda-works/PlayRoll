package es.ukanda.playroll.controllers.helpers

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import es.ukanda.playroll.customExceptions.CustomException
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.entyties.PartieEntities.Inventario
import es.ukanda.playroll.entyties.PartieEntities.Player
import es.ukanda.playroll.entyties.PartieEntities.PlayerCharacters
import org.json.JSONArray
import org.json.JSONObject
import java.net.DatagramSocket
import java.util.*

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

        fun convertStringToCharacterList(mensaje: String): List<CharacterEntity> {
            val jsonArray = JSONArray(mensaje)
            val characterList = mutableListOf<CharacterEntity>()

            for (i in 0 until jsonArray.length()) {
                val character = CharacterEntity.fromJson(jsonArray.get(0).toString())
                characterList.add(character)
            }

            return characterList
        }

        fun convertStringToPlayerList(mensaje: String): List<Player> {
            val jsonArray = JSONArray(mensaje)
            val playerList = mutableListOf<Player>()

            for (i in 0 until jsonArray.length()) {
                val player = Player.fromJson(jsonArray.get(0).toString())
                playerList.add(player)
            }

            return playerList
        }

        fun convertStringToInventarioList(mensaje: String): List<Inventario> {
            val jsonArray = JSONArray(mensaje)
            val inventarioList = mutableListOf<Inventario>()

            for (i in 0 until jsonArray.length()) {
                val inventario = Inventario.fromJson(jsonArray.get(0).toString())
                inventarioList.add(inventario)
            }

            return inventarioList
        }

        fun convertStringToPlayerCharacterList(mensaje: String): List<PlayerCharacters> {
            val jsonArray = JSONArray(mensaje)
            val playerCharacterList = mutableListOf<PlayerCharacters>()

            for (i in 0 until jsonArray.length()) {
                val playerCharacter = PlayerCharacters.fromJson(jsonArray.get(0).toString())
                playerCharacterList.add(playerCharacter)
            }

            return playerCharacterList
        }

        fun getHashFromUser():String{
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val email = user?.email ?: throw CustomException("No se ha podido obtener el email del usuario")
                return email.hashCode().toString()
            }catch (e:Exception){
                e.printStackTrace()
            }catch (e: CustomException){
                e.printStackTrace()
            }
            return ""
        }

        fun generarIdentificador(prefijo: String,objeto: String): String {
            val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            val longitud = 8

            val random = Random(System.currentTimeMillis())
            val sb = StringBuilder(prefijo)

            repeat(longitud) {
                val indice = random.nextInt(caracteres.length)
                val caracter = caracteres[indice]
                sb.append(caracter)
            }

            return sb.toString()
        }


    }
}