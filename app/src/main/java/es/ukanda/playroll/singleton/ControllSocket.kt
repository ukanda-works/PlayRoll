package es.ukanda.playroll.singleton

import android.graphics.Paint.Join
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import es.ukanda.playroll.ui.fragment.JoinPartyFragment
import kotlinx.coroutines.*
import java.net.InetAddress

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
                    SocketSingleton.connect(ip)
                    JoinPartyFragment.setConexionEstate(ConnectionState.CONNECTING)
                    joinPartyRequest()
                   /*val mensaje = SocketSingleton.getInstance().recive().toBoolean()
                    if (mensaje) {
                        JoinPartyFragment.setConexionEstate(ConnectionState.ACCEPTED)
                        //se espera a la lista de personajes
                    } else {
                        JoinPartyFragment.setConexionEstate(ConnectionState.REJECTED)
                        throw Exception("Connection rejected")
                    }*/
                } catch (e: Exception) {
                    JoinPartyFragment.setConexionEstate(ConnectionState.ERROR)
                    JoinPartyFragment.setErrorMensaje(e.message ?: "Error")
                    JoinPartyFragment.setErrorMensaje(e.stackTraceToString())
                    throw e
                } finally {
                    //SocketSingleton.getInstance().close()
                }
            }
            job?.invokeOnCompletion {
                if (it is CancellationException) {
                    SocketSingleton.getInstance().close()
                }
            }
            delay(timeout.toLong())
            job?.cancel()
        }

        private suspend fun joinPartyRequest(){
                try {
                    val user = FirebaseAuth.getInstance().currentUser
                    val userName = user?.displayName ?: "Anonimo"
                    val sendmensaje = listOf("peticion" to "join","nombre" to userName )
                    SocketSingleton.getInstance().send(Gson().toJson(sendmensaje))
                }catch (e: Exception){
                    throw e
                }
        }
    }
}