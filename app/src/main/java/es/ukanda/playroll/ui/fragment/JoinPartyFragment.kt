package es.ukanda.playroll.ui.fragment

import android.content.Context
import android.content.DialogInterface
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import es.ukanda.playroll.R
import es.ukanda.playroll.controllers.helpers.ComunicationHelpers.Companion.openUdpSocket
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.databinding.FragmentJoinPartyBinding
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.entyties.PartieEntities.Party
import es.ukanda.playroll.singleton.ControllSocket
import es.ukanda.playroll.ui.adapter.CharacterAdapter
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

    private lateinit var characterEntityList: List<CharacterEntity>

    private lateinit var characterAdapter: CharacterAdapter

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
        getDatabaseData()
        recicleViewInit()
        rgInit()
        getIp()
        /*CoroutineScope(Dispatchers.IO).launch {
            var party = PartyDb.getDatabase(context!!).partyDao().getParty(3)
            CoroutineScope(Dispatchers.Main).launch {
                mostrarDialogoConTextField(party, characterEntityList)
            }
        }*/

        conexionEstate.observe(viewLifecycleOwner) { newValue ->
            onConexionEstateChanged(newValue)
        }

        errorMensaje.observe(viewLifecycleOwner) { newValue ->
            onErrorMensajeChanged(newValue)
        }

    }
    private fun recicleViewInit() {
        adapter = PartyAdapter(partyList)
        binding.rvPartidasAbiertas.layoutManager = LinearLayoutManager(context)
        binding.rvPartidasAbiertas.adapter = adapter
    }
    fun getDatabaseData(){
        CoroutineScope(Dispatchers.IO).launch {
            characterEntityList = PartyDb.getDatabase(context!!).characterDao().getAllCharacters()
        }
    }

    private fun rgInit() {
        binding.btBuscarLocal.setOnClickListener {
            try {
                buscarPartida()
                Toast.makeText(context, "Buscando partidas", Toast.LENGTH_SHORT).show()
            }catch (e: Exception){
                Toast.makeText(context, "Error al buscar partidas", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun buscarPartida() {
        CoroutineScope(Dispatchers.IO).launch {
        try {
            val socket = DatagramSocket()
            socket.broadcast = true
            val broadcastAddress = getBroadcastAddress()
            val data = "hi".toByteArray()
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
                val socket = openUdpSocket(5688)
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

    fun mostrarDialogoConTextField(party: Party, characterList : List<CharacterEntity>) {
        val builder = AlertDialog.Builder(this.context!!)
        val listCharacter = mutableListOf<CharacterEntity>()
        builder.setTitle("Entablar conexion")
        val view = layoutInflater.inflate(R.layout.dialog_join_alias, null)
        builder.setView(view)
        if (party.partyConfig?.get("Pass").equals("")) {
            println("no hay pass")
            view.findViewById<TextView>(R.id.tvPassJoin).visibility = View.GONE
            view.findViewById<EditText>(R.id.etPassJoin).visibility = View.GONE

        }else {
            println("hay pass")
            val passWd = view.findViewById<EditText>(R.id.etPassJoin)
            val passTv = view.findViewById<TextView>(R.id.tvPassJoin)
            passWd.visibility = View.VISIBLE
            passTv.visibility = View.VISIBLE
        }

        if (!party.partyConfig?.get("Pass").equals("true")) {
            listCharacter.addAll(characterList)
            listCharacter.addAll(characterEntityList)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvCharactersJoin)
        initRecycler(listCharacter,recyclerView)

        builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })

        builder.setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })

        val alertDialog = builder.create()

        alertDialog.setOnShowListener {
            val acceptButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            acceptButton.setOnClickListener {
                val alias = view.findViewById<EditText>(R.id.etAliasJoin).text.toString()
                var pass = ""
                if (!party.partyConfig?.get("Pass").equals("")) {
                    pass = view.findViewById<EditText>(R.id.etPassJoin).text.toString()
                }
                val character = getSelectedCharacter()

                if (character == null) {
                    println("personaje no seleccionado")
                    Toast.makeText(context, "Selecciona un personaje", Toast.LENGTH_SHORT).show()
                } else if (alias == "") {
                    Toast.makeText(context, "Introduce un alias", Toast.LENGTH_SHORT).show()
                } else {
                    println("--------------------alias: $alias")
                    println("--------------------pass: $pass")
                    println("--------------------wcharacter: ${character.name}")

                    ControllSocket.startParty(targetIp.value!!, 5000, alias, pass, character)
                    alertDialog.dismiss()
                }
            }
        }

        alertDialog.show()
    }


    companion object {
        var _targetIp = MutableLiveData<String>()
        val targetIp: LiveData<String>
            get() = _targetIp

        lateinit var instance: JoinPartyFragment
        var _conexionEstate = MutableLiveData(ControllSocket.Companion.ConnectionState.NONE)
        val conexionEstate: LiveData<ControllSocket.Companion.ConnectionState>
            get() = _conexionEstate

        val _errorMensaje = MutableLiveData<String>()
        val errorMensaje: LiveData<String>
            get() = _errorMensaje

        val _characterList = MutableLiveData<List<CharacterEntity>>()
        val characterList: LiveData<List<CharacterEntity>>
            get() = _characterList

        val _party = MutableLiveData<Party>()
        val party: LiveData<Party>
            get() = _party
        fun onConexionEstateChanged(newValue: ControllSocket.Companion.ConnectionState) {
            val message = "Nuevo valor de conexión: $newValue"
            instance.activity?.runOnUiThread {
                Toast.makeText(instance.context, message, Toast.LENGTH_SHORT).show()
            }
            if (newValue == ControllSocket.Companion.ConnectionState.ACCEPTED) {
                instance.activity?.runOnUiThread {
                   instance.mostrarDialogoConTextField(party.value!!, characterList.value!!)
                }
            }else if(newValue == ControllSocket.Companion.ConnectionState.REJECTED){
                instance.activity?.runOnUiThread {
                    Toast.makeText(instance.context, "Conexion rechazada", Toast.LENGTH_SHORT).show()
                }
            }else if(newValue == ControllSocket.Companion.ConnectionState.ERROR){
                instance.activity?.runOnUiThread {
                    Toast.makeText(instance.context, "Conexion perdida", Toast.LENGTH_SHORT).show()
                }
            }else if(newValue == ControllSocket.Companion.ConnectionState.STARTING){
                instance.activity?.runOnUiThread {
                    Toast.makeText(instance.context, "Esperando a que comience la partida", Toast.LENGTH_SHORT).show()
                    //se queda escuchando por el 5691
                    //cuando reciba el mensaje de start se va a la pantalla de juego
                }
            }else if(newValue == ControllSocket.Companion.ConnectionState.STARTED){
                instance.activity?.runOnUiThread {
                    Toast.makeText(instance.context, "La partida ha comenzado", Toast.LENGTH_SHORT).show()
                    val bundle = Bundle()
                    bundle.putInt("party", party.value!!.partyID)
                    bundle.putString("player", targetIp.value!!)
                    instance.findNavController().navigate(R.id.action_nav_JoinParty_to_nav_playParty,bundle)
                }
            }
        }

        private fun onErrorMensajeChanged(newValue: String?) {
            val message = "Nuevo valor de error: $newValue"
            instance.activity?.runOnUiThread {
                Toast.makeText(instance.context, message, Toast.LENGTH_LONG).show()
            }
        }

        fun setTargetIp(newValue: String) {
            _targetIp.postValue(newValue)
        }

        fun setConexionEstate(newValue: ControllSocket.Companion.ConnectionState) {
            _conexionEstate.postValue(newValue)
        }

        fun setErrorMensaje(newValue: String) {
            _errorMensaje.postValue(newValue)
        }

        fun setCharacterList(newValue: List<CharacterEntity>) {
            _characterList.postValue(newValue)
        }

        fun setParty(newValue: Party) {
            _party.postValue(newValue)
        }
    }

    fun initRecycler(characterList: List<CharacterEntity>, recyclerView: RecyclerView ){

            characterAdapter = CharacterAdapter(characterList, 1) { character, isSelected ->

            }
            recyclerView.layoutManager  = LinearLayoutManager(context)
            recyclerView.adapter = characterAdapter
        }

    fun getSelectedCharacter(): CharacterEntity? {
        val charactesSelected = characterAdapter.getSelectedCharacters()
        if(charactesSelected.size > 1){
            Toast.makeText(context, "Selecciona solo un personaje", Toast.LENGTH_SHORT).show()
            return null
        }else if(charactesSelected.size == 0){
            Toast.makeText(context, "Selecciona un personaje", Toast.LENGTH_SHORT).show()
            return null
        }else{
            return charactesSelected[0]
        }
    }

}