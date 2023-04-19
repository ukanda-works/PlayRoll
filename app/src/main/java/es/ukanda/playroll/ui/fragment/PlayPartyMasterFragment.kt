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
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


class PlayPartyMasterFragment : Fragment() {

    private var _binding: FragmentPlayPartyMasterBinding? = null
    private val binding get() = _binding!!

    lateinit var party: Party

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
            val socket = DatagramSocket(5689)
            val buffer = ByteArray(1024)
            while (true) {
                val packet = DatagramPacket(buffer, buffer.size)

                socket.receive(packet)

                Toast.makeText(requireContext(), "Recibido", Toast.LENGTH_SHORT).show()
                val address = packet.address
                val port = packet.port

                val response = "HOLA".toByteArray()
                val responsePacket = DatagramPacket(response, response.size, address, port)
                Thread.sleep(1000)
                socket.send(responsePacket)
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