package es.ukanda.playroll.singleton

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import es.ukanda.playroll.controllers.helpers.ComunicationHelpers
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.entyties.PartieEntities.Party
import es.ukanda.playroll.ui.fragment.JoinPartyFragment
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetAddress
import java.net.Socket

class ControllSocket {

    companion object {
        enum class ConnectionState {
            CONNECTING,
            REJECTED,
            ACCEPTED,
            ERROR,
            NONE,
            STARTING
        }

        private var job: Job? = null


        suspend fun conectar(ip: InetAddress, timeout: Int = 5000) {
            JoinPartyFragment.setTargetIp(ip.hostAddress)
            job = CoroutineScope(Dispatchers.IO).launch {
                try {
                    var clientSocket = Socket(ip.hostAddress, 5690)
                    var writer = BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream()))
                    var reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                    JoinPartyFragment.setConexionEstate(ConnectionState.CONNECTING)
                    val user = FirebaseAuth.getInstance().currentUser
                    val userName = user?.displayName ?: "Anonimo"
                    val sendmensaje = listOf("peticion" to "join","nombre" to userName )
                    writer.write(Gson().toJson(sendmensaje))
                    writer.newLine()
                    writer.flush()
                    println("esperando respuesta..."    )
                    val mensaje = reader.readLine()
                    if (!mensaje.isEmpty()) {
                        procesarRespuesta("join", mensaje)
                    } else {
                        JoinPartyFragment.setConexionEstate(ConnectionState.REJECTED)
                        throw Exception("Connection rejected")
                    }
                } catch (e: Exception) {
                    JoinPartyFragment.setConexionEstate(ConnectionState.ERROR)
                    JoinPartyFragment.setErrorMensaje(e.message ?: "Error")
                    JoinPartyFragment.setErrorMensaje(e.stackTraceToString())
                    e.printStackTrace()
                }
            }
            delay(timeout.toLong())
            job?.cancel()
        }

        suspend fun procesarRespuesta(type: String, message: String) {
            println("Mensaje recibido $message")

            val decodedMensaje = ComunicationHelpers.getMapFromJson(message)

           when(type){
               "join" -> {
                  if (decodedMensaje["peticion"] == "ok"){
                      println("aceptada")
                      val partyRecived = decodedMensaje["party"]
                        if (partyRecived != null){
                            println("party recibida $partyRecived")
                            JoinPartyFragment.setParty(Party.fromJson(partyRecived))
                        }
                      val listaPersonajesJson = decodedMensaje["characters"]?.toList()
                      val listaPersonajes = mutableListOf<CharacterEntity>()
                        listaPersonajesJson?.forEach {
                            listaPersonajes.add(CharacterEntity.fromJson(it.toString()))
                        }
                        if (listaPersonajes != null){
                            println("lista recibida $listaPersonajes")
                            JoinPartyFragment.setCharacterList(listaPersonajes)
                        }
                      delay(500)
                      JoinPartyFragment.setConexionEstate(ConnectionState.ACCEPTED)
                  }else{
                      println("rechazada")
                      JoinPartyFragment.setConexionEstate(ConnectionState.REJECTED)
                  }
               }
               "start" -> {
                   if (decodedMensaje["peticion"] == "ok"){
                       println("aceptada")
                          JoinPartyFragment.setConexionEstate(ConnectionState.STARTING)
                   }else{
                       println("rechazada")
                       JoinPartyFragment.setConexionEstate(ConnectionState.ERROR)
                   }
               }
           }

        }

        fun startParty(ip: InetAddress, timeout: Int = 5000, alias: String, personaje: CharacterEntity){
            job = CoroutineScope(Dispatchers.IO).launch {
                try {
                    var clientSocket = Socket(ip.hostAddress, 5690)
                    var writer = BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream()))
                    var reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                    //se envia el alias y el personaje con la peticion start
                    val mensaje = listOf("peticion" to "start","alias" to alias, "personaje" to personaje.toJson())
                    writer.write(Gson().toJson(mensaje))
                    writer.newLine()
                    writer.flush()
                    println("esperando respuesta..."    )
                    val mensajeRecibido = reader.readLine()
                    if (!mensajeRecibido.isEmpty()) {
                        procesarRespuesta("start", mensajeRecibido)
                    } else {
                        JoinPartyFragment.setConexionEstate(ConnectionState.REJECTED)
                        throw Exception("Connection rejected")
                    }

                } catch (e: Exception) {
                    JoinPartyFragment.setConexionEstate(ConnectionState.ERROR)
                    JoinPartyFragment.setErrorMensaje(e.message ?: "Error")
                    JoinPartyFragment.setErrorMensaje(e.stackTraceToString())
                    e.printStackTrace()
                }
            }
            job?.cancel()
        }

    }
}