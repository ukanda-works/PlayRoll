package es.ukanda.playroll.ui.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.Socket


class ConexionViewModel(): ViewModel() {

    private val _estadoConexion = MutableLiveData<enunEstadoConexion>()
    val estadoConexion: LiveData<enunEstadoConexion> get()= _estadoConexion

    private val _mensajeList = MutableLiveData<String>()
    val mensajeList: LiveData<String> get() = _mensajeList

    lateinit var socket: Socket

    init {

    }

    fun conectar(ip: InetAddress){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                socket = Socket(ip, 5690)
                _estadoConexion.postValue(enunEstadoConexion.conectando)
                val outputStream = socket.getOutputStream()
                val user = FirebaseAuth.getInstance().currentUser
                val userName = user?.displayName ?: "Anonimo"
                outputStream.write(userName.toByteArray())

                val inputStream = socket.getInputStream()
                val buffer = ByteArray(1024)
                val bytes = inputStream.read(buffer)
                val mensaje = String(buffer, 0, bytes).toBoolean()
                if (mensaje){
                    _estadoConexion.postValue(enunEstadoConexion.aceptado)
                    //se espera a la lista de personajes
                    val bytes = inputStream.read(buffer)
                    val mensaje = String(buffer, 0, bytes)
                    _mensajeList.postValue(mensaje)
                }else{
                    _estadoConexion.postValue(enunEstadoConexion.rechazado)
                }
            }catch (e: Exception){
                _estadoConexion.postValue(enunEstadoConexion.error)
                e.printStackTrace()
            }
        }
    }

    enum class enunEstadoConexion{
        conectando,
        rechazado,
        aceptado,
        error
    }

}

