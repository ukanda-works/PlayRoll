package es.ukanda.playroll.ui.fragment

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import es.ukanda.playroll.databinding.FragmentJoinPartyBinding
import es.ukanda.playroll.ui.adapter.PartyAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


class JoinPartyFragment : Fragment() {
    private var _binding: FragmentJoinPartyBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PartyAdapter
    private val partyList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJoinPartyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recicleViewInit()
        rgInit()
        getIp()
    }

    private fun rgInit() {
        binding.rbLocal.setOnClickListener {
            binding.rbOnline.isChecked = false
            buscarPartida()
            Toast.makeText(context, "Buscando partidas", Toast.LENGTH_SHORT).show()
        }
        binding.rbOnline.setOnClickListener {
            binding.rbLocal.isChecked = false
            //pufff ya vere como hago para esto dios mio una api lo mas seguro
        }
    }

    private fun recicleViewInit() {
        adapter = PartyAdapter(partyList)
        binding.rvPartidasAbiertas.layoutManager = LinearLayoutManager(context)
        binding.rvPartidasAbiertas.adapter = adapter
    }

    private fun buscarPartida(){
        if (binding.rbLocal.isChecked){
            CoroutineScope(Dispatchers.IO).launch {
                try{
                val broadCastAddress = InetAddress.getByName("255.255.255.255")
                val packet = DatagramPacket("".toByteArray(), 0, broadCastAddress, 5689)
                val socket = DatagramSocket()
                socket.send(packet)
                socket.close()
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
            CoroutineScope(Dispatchers.IO).launch {
                sleep(500)
                while (true){
                    val socket = DatagramSocket(5689)
                    val buffer = ByteArray(1024)
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    val message = String(packet.data, 0, packet.length)
                    partyList.add(message)
                    activity?.runOnUiThread {
                        adapter.notifyDataSetChanged()
                    }
                    socket.close()
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
            binding.tvIp.text = "Tu ip es: $ip"
        }

    }




}