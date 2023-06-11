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
        conexionEstate.observe(viewLifecycleOwner) { newValue ->
            onConexionEstateChanged(newValue)
        }

        errorMensaje.observe(viewLifecycleOwner) { newValue ->
            onErrorMensajeChanged(newValue)
        }

    }
    /**
    Inicializa la vista del RecyclerView para mostrar una lista de partidas.
    Crea y configura un adaptador de tipo PartyAdapter y lo asigna al RecyclerView.
     */
    private fun recicleViewInit() {
        adapter = PartyAdapter(partyList)
        binding.rvPartidasAbiertas.layoutManager = LinearLayoutManager(context)
        binding.rvPartidasAbiertas.adapter = adapter
    }
    /**
    Obtiene los datos de la base de datos.
    Realiza una operación asíncrona para obtener la lista de entidades de personajes desde la base de datos.
     */
    fun getDatabaseData(){
        CoroutineScope(Dispatchers.IO).launch {
            characterEntityList = PartyDb.getDatabase(context!!).characterDao().getAllCharacters()
        }
    }
    /**
    Inicializa el componente de radio group (rg).
    Configura el click listener para el botón de buscar localmente (btBuscarLocal).
    Al hacer clic en el botón, se ejecuta el método buscarPartida() para buscar partidas.
    Muestra un mensaje de "Buscando juegos" mediante un Toast de duración corta.
    En caso de producirse alguna excepción, se muestra un mensaje de error mediante un Toast.
     */
    private fun rgInit() {
        binding.btBuscarLocal.setOnClickListener {
            try {
                buscarPartida()
                Toast.makeText(context, getString(R.string.looking_for_games), Toast.LENGTH_SHORT).show()
            }catch (e: Exception){
                Toast.makeText(context, getString(R.string.error_when_searching_for_games), Toast.LENGTH_SHORT).show()
            }
        }

    }
    /**
    Realiza una búsqueda de partida mediante el uso de sockets UDP.
    Envía un mensaje de difusión (broadcast) a través del socket UDP para descubrir partidas disponibles.
    Luego, espera y procesa los mensajes recibidos a través del socket UDP.
    Este método se ejecuta en un contexto de hilos de fondo.
     */
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
    /**
    Obtiene la dirección de difusión (broadcast) para la red actual.
    @return La dirección de difusión (broadcast) de la red actual, o null si no se encuentra disponible.
     */
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
    /**
    Procesa un mensaje recibido a través de un paquete DatagramPacket.
    @param packet El paquete DatagramPacket que contiene el mensaje a procesar.
     */
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
                Toast.makeText(context, getString(R.string.error_processing_message), Toast.LENGTH_SHORT).show()
            }
            e.printStackTrace()
        }
    }
    /**
    Obtiene la dirección IP del dispositivo en el que se está ejecutando la aplicación.
    El método se ejecuta en un contexto de hilos de fondo y utiliza operaciones asíncronas.
    La dirección IP obtenida se guarda en la variable 'ip'.
     */
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
        }

    }
    /**
    Muestra un diálogo con un campo de texto y opciones de selección para unirte a una partida.
    @param party La partida a la que se desea unir.
    @param characterList La lista de personajes disponibles para seleccionar.
     */
    fun mostrarDialogoConTextField(party: Party, characterList : List<CharacterEntity>) {
        val builder = AlertDialog.Builder(this.context!!)
        val listCharacter = mutableListOf<CharacterEntity>()
        builder.setTitle(getString(R.string.make_connection))
        val view = layoutInflater.inflate(R.layout.dialog_join_alias, null)
        builder.setView(view)
        val configuration = party.partyConfig
        if (configuration?.get("Pass").equals("")) {
            view.findViewById<TextView>(R.id.tvPassJoin).visibility = View.GONE
            view.findViewById<EditText>(R.id.etPassJoin).visibility = View.GONE
        }else {
            val passWd = view.findViewById<EditText>(R.id.etPassJoin)
            val passTv = view.findViewById<TextView>(R.id.tvPassJoin)
            passWd.visibility = View.VISIBLE
            passTv.visibility = View.VISIBLE
        }
        if(configuration?.get("OnlyOwn").equals("false")){
            listCharacter.addAll(characterList)
            listCharacter.addAll(characterEntityList)
        }else{
            listCharacter.addAll(characterList)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvCharactersJoin)
        initRecycler(listCharacter,recyclerView)

        builder.setPositiveButton(getString(R.string.accept), DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })

        builder.setNegativeButton(getString(R.string.cancel), DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })

        val alertDialog = builder.create()

        alertDialog.setOnShowListener {
            val acceptButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            acceptButton.setOnClickListener {
                val alias = view.findViewById<EditText>(R.id.etNumeroCaras).text.toString()
                var pass = ""
                if (!party.partyConfig?.get("Pass").equals("")) {
                    pass = view.findViewById<EditText>(R.id.etPassJoin).text.toString()
                }
                val character = getSelectedCharacter()
                if (character == null) {
                    Toast.makeText(context, getString(R.string.select_a_character), Toast.LENGTH_SHORT).show()
                } else if (alias == "") {
                    Toast.makeText(context, getString(R.string.enter_a_nickname), Toast.LENGTH_SHORT).show()
                } else {
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
        /**
        Muestra un diálogo con un campo de texto y opciones de selección para unirte a una partida.
        @param party La partida a la que se desea unir.
        @param characterList La lista de personajes disponibles para seleccionar.
         */
        fun onConexionEstateChanged(newValue: ControllSocket.Companion.ConnectionState) {
            if (newValue == ControllSocket.Companion.ConnectionState.ACCEPTED) {
                instance.activity?.runOnUiThread {
                   instance.mostrarDialogoConTextField(party.value!!, characterList.value!!)
                }
            }else if(newValue == ControllSocket.Companion.ConnectionState.REJECTED){
                instance.activity?.runOnUiThread {
                    Toast.makeText(instance.context,  instance.getString(R.string.connection_refused), Toast.LENGTH_SHORT).show()
                }
            }else if(newValue == ControllSocket.Companion.ConnectionState.ERROR){
                instance.activity?.runOnUiThread {
                    Toast.makeText(instance.context,  instance.getString(R.string.connection_loss), Toast.LENGTH_SHORT).show()
                }
            }else if(newValue == ControllSocket.Companion.ConnectionState.STARTING){
                instance.activity?.runOnUiThread {
                    Toast.makeText(instance.context, instance.getString(R.string.waiting_for_the_game_to_start), Toast.LENGTH_SHORT).show()
                }
            }else if(newValue == ControllSocket.Companion.ConnectionState.STARTED){
                instance.activity?.runOnUiThread {
                    Toast.makeText(instance.context, instance.getString(R.string.the_game_has_started), Toast.LENGTH_SHORT).show()
                    val bundle = Bundle()
                    bundle.putInt("party", party.value!!.partyID)
                    bundle.putString("ipServer", targetIp.value!!)
                    bundle.putBoolean("isMaster", false)
                    instance.findNavController().navigate(R.id.action_nav_JoinParty_to_nav_playParty,bundle)
                }
            }
        }
        /**
        Método que se invoca cuando cambia el mensaje de error.
        @param newValue El nuevo valor del mensaje de error.
         */
        private fun onErrorMensajeChanged(newValue: String?) {
            val message = "${instance.getString(R.string.error)}: $newValue"
            instance.activity?.runOnUiThread {
                Toast.makeText(instance.context, message, Toast.LENGTH_LONG).show()
            }
        }
        /**
        Establece la dirección IP de destino.
        @param newValue La nueva dirección IP de destino.
         */
        fun setTargetIp(newValue: String) {
            _targetIp.postValue(newValue)
        }
        /**
        Establece el estado de la conexión.
        @param newValue El nuevo estado de la conexión.
         */
        fun setConexionEstate(newValue: ControllSocket.Companion.ConnectionState) {
            _conexionEstate.postValue(newValue)
        }
        /**
        Establece el mensaje de error.
        @param newValue El nuevo mensaje de error.
         */
        fun setErrorMensaje(newValue: String) {
            _errorMensaje.postValue(newValue)
        }
        /**
        Establece la lista de personajes.
        @param newValue La nueva lista de personajes.
         */
        fun setCharacterList(newValue: List<CharacterEntity>) {
            _characterList.postValue(newValue)
        }
        /**
        Establece la instancia de la Party.
        @param newValue La nueva instancia de Party.
         */
        fun setParty(newValue: Party) {
            _party.postValue(newValue)
        }
    }
    /**
    Inicializa el RecyclerView con la lista de personajes.
    @param characterList La lista de personajes a mostrar.
    @param recyclerView El RecyclerView en el que se mostrarán los personajes.
     */
    fun initRecycler(characterList: List<CharacterEntity>, recyclerView: RecyclerView ){
            characterAdapter = CharacterAdapter(characterList, 1) { character, isSelected ->
            }
            recyclerView.layoutManager  = LinearLayoutManager(context)
            recyclerView.adapter = characterAdapter
        }
    /**
    Obtiene el personaje seleccionado.
    @return El personaje seleccionado, o null si no se ha seleccionado ningún personaje o se han seleccionado más de uno.
     */
    fun getSelectedCharacter(): CharacterEntity? {
        val charactesSelected = characterAdapter.getSelectedCharacters()
        if(charactesSelected.size > 1){
            Toast.makeText(context, instance.getString(R.string.select_only_one_character), Toast.LENGTH_SHORT).show()
            return null
        }else if(charactesSelected.size == 0){
            Toast.makeText(context, instance.getString(R.string.select_a_character), Toast.LENGTH_SHORT).show()
            return null
        }else{
            return charactesSelected[0]
        }
    }

}