package es.ukanda.playroll.ui.fragment

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.databinding.FragmentCharacterCreatorBinding
import es.ukanda.playroll.databinding.FragmentPlayPartyMasterBinding
import es.ukanda.playroll.entyties.PartieEntities.Party
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket


class PlayPartyMasterFragment : Fragment() {

    private var _binding: FragmentPlayPartyMasterBinding? = null
    private val binding get() = _binding!!

    lateinit var party: Party
    private var listaJugadores = mutableListOf<String>()

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
                party = Party(0,"")
            }else{
                party = PartyDb.getDatabase(requireContext()).partyDao().getParty(partyId)
                starServer()
            }

        }
    }

    private fun starServer(){
        CoroutineScope(Dispatchers.IO).launch {

            try {
                val socket = DatagramSocket(5689)
                val buffer = ByteArray(1024)
                while (true) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Esperando", Toast.LENGTH_SHORT).show()
                    }
                    socket.receive(packet)
                    //muestro el mensaje recibido
                    val message = String(packet.data, 0, packet.length)
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Mensaje recibido: $message", Toast.LENGTH_SHORT).show()
                    }
                    sleep(1000)
                    val address = packet.address
                    val data = party.toJson().toByteArray()
                    val packet2 = DatagramPacket(data, data.size, address, 5689)
                    socket.send(packet2)
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Respondiendo", Toast.LENGTH_SHORT).show()
                    }
                }
            }catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error:${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

        }
        starServerTcp()
    }

    private fun starServerTcp() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val serverSocket = ServerSocket(5690)
                while (true) {
                    val socket = serverSocket.accept()

                }
            }catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
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

}
private class ServerThread(val socket: Socket) : Thread() {
    override fun run() {
        try {
            val input = socket.getInputStream()
            val output = socket.getOutputStream()

            val buffer = ByteArray(1024)
            var bytes = input.read(buffer)
            val message = String(buffer, 0, bytes)

            processMessage(message, output)

            socket.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processMessage(message: String, output: OutputStream) {
        when (message) {
            "hola" -> {
                output.write("hola".toByteArray())
            }
            "adios" -> {
                output.write("adios".toByteArray())
            }
            else -> {
                output.write("no entiendo".toByteArray())
            }
        }
    }
}