package es.ukanda.playroll.singleton

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import es.ukanda.playroll.controllers.helpers.ComunicationHelpers
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.entyties.PartieEntities.*
import es.ukanda.playroll.ui.fragment.HelpFragment
import es.ukanda.playroll.ui.fragment.JoinPartyFragment
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class ControllSocket {

    companion object {
        enum class ConnectionState {
            CONNECTING,
            REJECTED,
            ACCEPTED,
            ERROR,
            NONE,
            STARTING,
            STARTED,
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
            println("Mensaje decodificado $decodedMensaje")

           when(type){
               "join" -> {
                  if (decodedMensaje["reponse"].equals("ok")){
                      println("aceptada")
                        if (decodedMensaje["party"] != null){
                            val partyRecived = decodedMensaje["party"].toString()
                            println("party recibida $partyRecived")
                            JoinPartyFragment.setParty(Party.fromJson(partyRecived))
                        }
                      val listaPersonajesJson = decodedMensaje["characters"]!!
                      println("lista recibida $listaPersonajesJson")
                      val listaPersonajes = ComunicationHelpers.convertStringToCharacterList(listaPersonajesJson)
                      println("lista recibida convertida $listaPersonajes")
                        if (!listaPersonajes.isEmpty()){
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
                   if (decodedMensaje["reponse"].equals("ok")){
                       println("aceptada")
                          JoinPartyFragment.setConexionEstate(ConnectionState.STARTING)
                            waitStart()
                   }else{
                       println("rechazada")
                       JoinPartyFragment.setConexionEstate(ConnectionState.ERROR)
                   }
               }
               "started" -> {
                   if (decodedMensaje["response"].equals("ok")){
                       println("aceptada")
                       var partyRecived = Party.fromJson(decodedMensaje["party"].toString())
                       partyRecived = Party.removeIdFromParty(partyRecived)

                       var recivedCharacters = ComunicationHelpers.convertStringToCharacterList(decodedMensaje["characters"].toString())
                       var recivedPlayers = ComunicationHelpers.convertStringToPlayerList(decodedMensaje["players"].toString())
                       var recivedPlayerCharacters = ComunicationHelpers.convertStringToPlayerCharacterList(decodedMensaje["player_character"].toString())
                       //var recivedInventarios = ComunicationHelpers.convertStringToInventarioList(decodedMensaje["inventarios"].toString())

                       CoroutineScope(Dispatchers.IO).launch {
                           val db = PartyDb.getDatabase(JoinPartyFragment.instance.requireContext())
                           val partyId = db.partyDao().insertParty(partyRecived)
                           JoinPartyFragment.setParty(db.partyDao().getParty(partyId.toInt()))
                           val savedCharacters = mutableListOf<CharacterEntity>()
                           val savedPlayers = mutableListOf<Player>()
                           val savedInventarios = mutableListOf<Inventario>()
                           recivedCharacters.forEach {
                                 val oldId  = it.characterID
                                 val id =db.characterDao().insertCharacter(CharacterEntity.removeIdFromCharacter(it))
                                 savedCharacters.add(db.characterDao().getCharacterById(id.toInt()))
                                    recivedPlayerCharacters.forEach { playerCharacter ->
                                        playerCharacter.partyID = partyId.toInt()
                                        if (playerCharacter.characterID == oldId){
                                            playerCharacter.characterID = id.toInt()
                                        }
                                    }
                                    /*recivedInventarios.forEach { inventario ->
                                        inventario.partyID = partyId.toInt()
                                        if (inventario.characterID == oldId){
                                            inventario.characterID = id.toInt()
                                        }
                                    }*/
                            }
                           recivedPlayers.forEach {
                               val oldId = it.playerID
                               val id = db.playerDao().insertPlayer(Player.removeIdFromPlayer(it))
                               savedPlayers.add(db.playerDao().getPlayerById(id.toInt()))
                                 recivedPlayerCharacters.forEach { playerCharacter ->
                                      if (playerCharacter.playerID == oldId){
                                        playerCharacter.playerID = id.toInt()
                                      }
                                 }
                           }
                           /*recivedInventarios.forEach {
                               val id = db.inventarioDao().insertInventario(Inventario.removeIdFromInventario(it))
                               savedInventarios.add(db.inventarioDao().getInventarioById(id.toInt()))
                           }*/

                            recivedPlayerCharacters.forEach {
                                 db.playerCharacterDao().insertPartyPlayerCharacter(it)
                            }



                       }
                       JoinPartyFragment.setConexionEstate(ConnectionState.STARTED)
                   }else{
                       println("rechazada")
                       JoinPartyFragment.setConexionEstate(ConnectionState.ERROR)
                   }
               }
           }

        }

        fun startParty(ip:String, timeout: Int = 5000, alias: String,pass:String, personaje: CharacterEntity){
            job = CoroutineScope(Dispatchers.IO).launch {
                try {
                    var clientSocket = Socket(ip, 5690)
                    var writer = BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream()))
                    var reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                    //se envia el alias y el personaje con la peticion start
                    val mensaje = listOf("peticion" to "start",
                                    "alias" to alias,
                                    "selectedCharacter" to personaje.toJson(),
                                    "pass" to pass,
                                    "characterOwn" to false, //indica si el personaje es propio o no, true si es propio
                                    "hash" to ComunicationHelpers.getHashFromUser())
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

        fun waitStart(){
            CoroutineScope(Dispatchers.IO).launch {
                println("esperando a que el servidor inicie la partida")
                try {
                    var serverSocket = ServerSocket(5691)
                    println("escuchando...")
                    val socket = serverSocket.accept()
                    var reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                    val mensajeRecibido = reader.readLine()
                    procesarRespuesta("started", mensajeRecibido)
                    serverSocket.close()
                } catch (e: Exception) {
                    JoinPartyFragment.setConexionEstate(ConnectionState.ERROR)
                    JoinPartyFragment.setErrorMensaje(e.message ?: "Error")
                    JoinPartyFragment.setErrorMensaje(e.stackTraceToString())
                    e.printStackTrace()
                }
            }
        }

    }

}