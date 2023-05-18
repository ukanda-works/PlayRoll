package es.ukanda.playroll.singleton

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
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
            NONE
        }

        private var job: Job? = null


        suspend fun conectar(ip: InetAddress, timeout: Int = 5000) {
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
                        println("Mensaje recibido $mensaje")
                        if (mensaje == "ok"){
                            println("aceptada")
                            JoinPartyFragment.setConexionEstate(ConnectionState.ACCEPTED)
                            var listaPersonajes = reader.readLine()
                            if (!listaPersonajes.isEmpty()){
                                println("lista recibida $listaPersonajes")
                                //se hace lo necesario para mostrar la lista de personajes
                            }
                        }else{
                            println("rechazada")
                            JoinPartyFragment.setConexionEstate(ConnectionState.REJECTED)

                        }
                        //se espera a la lista de personajes
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
       

    }
}