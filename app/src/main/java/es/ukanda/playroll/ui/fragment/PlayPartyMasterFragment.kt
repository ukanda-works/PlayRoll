package es.ukanda.playroll.ui.fragment

import android.app.AlertDialog
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import es.ukanda.playroll.controllers.comunication.PetitionResponseListerner
import es.ukanda.playroll.controllers.helpers.ComunicationHelpers
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.databinding.FragmentPlayPartyMasterBinding
import es.ukanda.playroll.entyties.PartieEntities.Party
import kotlinx.coroutines.*
import java.io.*
import java.lang.Thread.sleep
import java.net.*


class PlayPartyMasterFragment : Fragment(){

    private var _binding: FragmentPlayPartyMasterBinding? = null
    private val binding get() = _binding!!

    lateinit var party: Party
    private var listaJugadores = mutableListOf<String>()

    companion object{
        lateinit var instance: PlayPartyMasterFragment
        var conexionEstate = MutableLiveData<Boolean>()
        var errorMensaje = MutableLiveData<String>()
        var lastResponse = MutableLiveData<Boolean>()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayPartyMasterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val partyId = arguments?.getInt("id") ?: 0
        setParty(partyId)
        getIp()

    }

    private fun setParty(partyId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            if (partyId == 0){
                //lanzar error de que no se ha encontrado la party
            }else{
                party = PartyDb.getDatabase(requireContext()).partyDao().getParty(partyId)
                starServerUdp()
                starServerTcp()
            }

        }
    }

    private fun starServerUdp(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket = DatagramSocket(5689)
                val buffer = ByteArray(1024)
                while (true) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(requireContext(), "Esperando", Toast.LENGTH_SHORT).show()
                    }
                    socket.receive(packet)
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(requireContext(), "Recibido", Toast.LENGTH_SHORT).show()
                    }
                    //muestro el mensaje recibido
                    val message = String(packet.data, 0, packet.length)
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(requireContext(), "Mensaje: $message", Toast.LENGTH_SHORT).show()
                    }
                    sleep(1000)
                    val address = packet.address
                    val data = party.toJson().toByteArray()
                    val packet2 = DatagramPacket(data, data.size, address, 5688)
                    socket.send(packet2)
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Respondiendo", Toast.LENGTH_SHORT).show()
                    }
                }
            }catch (e: Exception) {
                e.printStackTrace()
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(requireContext(), "Error:${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

        }

    }

    private fun starServerTcp() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val serverSocket = ServerSocket(5690)
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(requireContext(), "Esperando jugadores en el puerto ${serverSocket.localPort}", Toast.LENGTH_SHORT).show()
                }
                while (true) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(requireContext(), "Escuchando", Toast.LENGTH_SHORT).show()
                    }
                    val socket = withContext(Dispatchers.IO) {
                        serverSocket.accept()
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(requireContext(), "Recibido", Toast.LENGTH_SHORT).show()
                    }
                    val serverThread = ServerThread(socket, this@PlayPartyMasterFragment)
                    serverThread.start()

                }
            }catch (e: Exception) {
                e.printStackTrace()
               CoroutineScope(Dispatchers.Main).launch {
                   Toast.makeText(requireContext(), "Error:${e.message}", Toast.LENGTH_SHORT).show()
               }
            }
        }
    }

    private fun getIp(){
        var ip = ""
        CoroutineScope(Dispatchers.IO).launch {

            val wifiManager = context!!.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = wifiManager.connectionInfo.ipAddress
            val addressBytes = byteArrayOf(
                (ipAddress and 0xff).toByte(),
                (ipAddress shr 8 and 0xff).toByte(),
                (ipAddress shr 16 and 0xff).toByte(),
                (ipAddress shr 24 and 0xff).toByte()
            )
            ip = InetAddress.getByAddress(addressBytes).hostAddress
            binding.tvIpMaster.text = "Tu ip es: $ip"
        }

    }

    fun showPetition(message: String, title:String)
    {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton("Aceptar") { _, _ ->
                lastResponse.value= true
            }
            builder.setNegativeButton("Rechazar") { _, _ ->
                lastResponse.value = false
            }
            val dialog = builder.create()

            dialog.show()
    }


}
private class ServerThread(val clientSocket: Socket, val fragment: PlayPartyMasterFragment) : Thread() {
    override fun run() {
        try {
            println("metodo run")
            val input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val output = BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream()))

            var message = input.readLine()

            processMessage(message, output)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processMessage(message: String, output: BufferedWriter) {
        println("mensaje: $message -----------------------------------------------------")
        val decodedMensaje = ComunicationHelpers.getMapFromJson(message)
        println("decodedMensaje: $decodedMensaje -----------------------------------------------------")
        val peticion = decodedMensaje["peticion"]
        println("peticion: $peticion -----------------------------------------------------")
        when (peticion) {
            "join" -> {
                val nombre = decodedMensaje["nombre"]
                val mensaje = "El usuario $nombre quiere unirse a la partida"
                fragment.activity?.runOnUiThread {
                    fragment.showPetition(mensaje, "Petición de unión")
                }
                    while (PlayPartyMasterFragment.lastResponse.value == null) {
                        println("esperando respuesta ui")
                        sleep(1000)
                    }
                if (PlayPartyMasterFragment.lastResponse.value!!) {
                    println("aceptado")
                    output.write("ok")
                    output.newLine()
                    output.flush()
                    println("enviado ok")
                } else {
                    println("rechazado")
                    output.write("no")
                }


            }
            "adios" -> {
                output.write("adios")
            }
            else -> {
                output.write("no entiendo")
            }
        }
    }
    override fun interrupt() {
        super.interrupt()
        clientSocket.close()
    }
}