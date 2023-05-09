package es.ukanda.playroll.ui.fragment

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import es.ukanda.playroll.databinding.FragmentJoinPartyBinding
import es.ukanda.playroll.entyties.PartieEntities.Party
import es.ukanda.playroll.singleton.ControllSocket
import es.ukanda.playroll.ui.adapter.PartyAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface


class JoinPartyFragment : Fragment() {
    private var _binding: FragmentJoinPartyBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PartyAdapter
    private val partyList = mutableMapOf<InetAddress, Party>()
    //val viewModel : ConexionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJoinPartyBinding.inflate(inflater, container, false)
        instance = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recicleViewInit()
        rgInit()
        getIp()

        conexionEstate.observe(viewLifecycleOwner) { newValue ->
            onConexionEstateChanged(newValue)
        }

        errorMensaje.observe(viewLifecycleOwner) { newValue ->
            onErrorMensajeChanged(newValue)
        }
    }

    private fun rgInit() {
        /*binding.rbLocal.setOnClickListener {
            binding.rbOnline.isChecked = false
            buscarPartida()
            Toast.makeText(context, "Buscando partidas", Toast.LENGTH_SHORT).show()
        }*/
        binding.btBuscarLocal.setOnClickListener {
            try {
                buscarPartida()
                Toast.makeText(context, "Buscando partidas", Toast.LENGTH_SHORT).show()
            }catch (e: Exception){
                Toast.makeText(context, "Error al buscar partidas", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun recicleViewInit() {
        adapter = PartyAdapter(partyList)
        binding.rvPartidasAbiertas.layoutManager = LinearLayoutManager(context)
        binding.rvPartidasAbiertas.adapter = adapter
    }

    private fun buscarPartidae(){
            CoroutineScope(Dispatchers.IO).launch {
                val broadCastAddress = InetAddress.getByName("255.255.255.255")
                val data = "hola, jaime".toByteArray()
                val packet = DatagramPacket(data, data.size, broadCastAddress, 5689)
                try{
                    val socket = DatagramSocket()
                    socket.send(packet)
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "mensaje enviado", Toast.LENGTH_LONG).show()
                    }
                   /* while (true){
                        sleep(300)
                        val socket = DatagramSocket(5688)
                        val buffer = ByteArray(1024)
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket.receive(packet)
                        procesarMensaje(packet)
                        socket.close()
                    }*/
                }catch (e: Exception){
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "Error al enviar el mensaje ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    e.printStackTrace()
                }

            }
    }

    private fun buscarPartida() {
        CoroutineScope(Dispatchers.IO).launch {
        try {
            val socket = DatagramSocket()
            socket.broadcast = true
            val broadcastAddress = getBroadcastAddress()
            val data = "hola, jaime".toByteArray()
            val packet = DatagramPacket(data, data.size, broadcastAddress, 5689)
            withContext(Dispatchers.IO) {
                socket.send(packet)
            }
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            while (true){
                sleep(300)
                val socket = DatagramSocket(5688)
                val buffer = ByteArray(1024)
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)
                procesarMensaje(packet)
                socket.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        }
    }

    private fun getBroadcastAddress(): InetAddress? {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            if (networkInterface.isLoopback || !networkInterface.isUp) {
                continue
            }
            for (interfaceAddress in networkInterface.interfaceAddresses) {
                val broadcast = interfaceAddress.broadcast
                if (broadcast != null) {
                    return broadcast
                }
            }
        }
        return null
    }

    private fun procesarMensaje(packet: DatagramPacket){
        try{
        val json = String(packet.data, 0, packet.length)
        val gson = Gson()
        val party = gson.fromJson(json, Party::class.java)
        if (party != null) {
            val ip = packet.address
            partyList.put(ip, party)
            activity?.runOnUiThread {
                adapter.notifyDataSetChanged()
                Toast.makeText(context, "mensaje procesado", Toast.LENGTH_SHORT).show()
            }
        } else {
            activity?.runOnUiThread {
                Toast.makeText(context, "Json no valido", Toast.LENGTH_SHORT).show()
            }
        }
        }catch (e: Exception){
            activity?.runOnUiThread {
                Toast.makeText(context, "Error al procesar el mensaje", Toast.LENGTH_SHORT).show()
            }
            e.printStackTrace()
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

    companion object {
        lateinit var instance: JoinPartyFragment
        var _conexionEstate = MutableLiveData(ControllSocket.Companion.ConnectionState.NONE)
        val conexionEstate: LiveData<ControllSocket.Companion.ConnectionState>
            get() = _conexionEstate

        val _errorMensaje = MutableLiveData<String>()
        val errorMensaje: LiveData<String>
            get() = _errorMensaje
        fun onConexionEstateChanged(newValue: ControllSocket.Companion.ConnectionState) {
            val message = "Nuevo valor de conexión: $newValue"
            instance.activity?.runOnUiThread {
                Toast.makeText(instance.context, message, Toast.LENGTH_SHORT).show()
            }
        }

        private fun onErrorMensajeChanged(newValue: String?) {
            val message = "Nuevo valor de error: $newValue"
            instance.activity?.runOnUiThread {
                Toast.makeText(instance.context, message, Toast.LENGTH_LONG).show()
            }
        }


        fun setConexionEstate(newValue: ControllSocket.Companion.ConnectionState) {
            _conexionEstate.postValue(newValue)
        }

        fun setErrorMensaje(newValue: String) {
            _errorMensaje.postValue(newValue)
        }
    }



}