package es.ukanda.playroll.ui.fragment

import android.app.AlertDialog
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import es.ukanda.playroll.R
import es.ukanda.playroll.controllers.helpers.ComunicationHelpers
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.databinding.FragmentPlayPartyMasterBinding
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.entyties.PartieEntities.Party
import es.ukanda.playroll.entyties.PartieEntities.Player
import es.ukanda.playroll.entyties.PartieEntities.PlayerCharacters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.lang.Thread.sleep
import java.net.*


class PlayPartyMasterFragment : Fragment(){

    private var _binding: FragmentPlayPartyMasterBinding? = null
    private val binding get() = _binding!!

    lateinit var party: Party
    var listaJugadores = mutableMapOf<Player, String>()
    var listaJugadoresCharacter = mutableMapOf<String, CharacterEntity>()
    lateinit var characterList : List<CharacterEntity>
    var listaJudoresSeleccion = mutableMapOf<String, String>()

    lateinit var serverSocket: ServerSocket
    lateinit var socketUdp: DatagramSocket

    companion object{
        lateinit var instance: PlayPartyMasterFragment
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
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        val partyId = arguments?.getInt("id") ?: 0
        setParty(partyId)
        setBtn()
    }
    /**
    Establece el comportamiento de los botones.
    Este método establece el comportamiento de los botones en la interfaz de usuario.
     */
    private fun setBtn() {
        binding.btStartGame.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.start_game))
            builder.setMessage(getString(R.string.are_you_sure_you_want_to_start_the_game))
            builder.setPositiveButton(getString(R.string.yes)){dialog, which ->
                startGame()
            }
            builder.setNegativeButton(R.string.no){dialog, which ->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
        binding.btnStopSharing.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.stop_sharing))
            builder.setMessage(getString(R.string.are_you_sure_you_want_to_stop_sharing))
            builder.setPositiveButton(getString(R.string.yes)){dialog, which ->
                closeServerTcp()
                closeServerUdp()
                findNavController().navigate(R.id.action_playPartyMasterFragment_to_home)
            }
            builder.setNegativeButton(R.string.no){dialog, which ->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    private fun startGame() {
        CoroutineScope(Dispatchers.IO).launch {
            closeServerTcp()
            closeServerUdp()
            val db = PartyDb.getDatabase(requireContext())
            val savedParty = db.partyDao().getParty(party.partyID)
            val savedPlayers = mutableListOf<Player>()
            val jsonPlayerList = mutableListOf<String>()
            listaJugadores.forEach {
                var playerId : Long
                if (db.playerDao().checkPlayerByIdentifier(it.key.identifier)) {
                    val player = db.playerDao().getPlayerByIdentifier(it.key.identifier)
                    playerId = player.playerID.toLong()
                }else{
                    playerId = db.playerDao().insertPlayer(it.key)
                }
                val saved = db.playerDao().getPlayerById(playerId.toInt())
                savedPlayers.add(saved)
                jsonPlayerList.add(saved.toJson())
            }
            val savedPlayerCharacterList = mutableListOf<String>()
            savedPlayers.forEach {
                val playerHash = it.identifier
                val character = listaJugadoresCharacter[playerHash]
                val playerCharacter = PlayerCharacters(partyID = savedParty.partyID, playerID = it.playerID, characterID = character!!.characterID)
                db.playerCharacterDao().insertPartyPlayerCharacter(playerCharacter)
                savedPlayerCharacterList.add(playerCharacter.toJson())
            }
            val gson = Gson()
            val jsonCharaterList = mutableListOf<String>()
            characterList.forEach {
                jsonCharaterList.add(it.toJson())
            }
            val mensaje = listOf("peticion" to "started",
                                "response" to "ok",
                                "party" to savedParty.toJson(),
                                "characters" to jsonCharaterList,
                                "players" to jsonPlayerList,
                                "player_character" to savedPlayerCharacterList)
            val mensajeJson = gson.toJson(mensaje)
            listaJugadores.values.forEach{ ip ->
                val socket = Socket(ip, 5691)
                val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                writer.write(mensajeJson)
                writer.flush()
                writer.close()
                socket.close()
            }
            val bundle = Bundle()
            bundle.putInt("party", savedParty.partyID)
            val playerList = mutableMapOf<String,String>()
            listaJugadores.forEach {
                playerList.put(it.key.identifier, it.value)
            }
            bundle.putSerializable("players", playerList as Serializable)
            bundle.putBoolean("isMaster", true)
            try {
                serverSocket.close()
            }catch (e: Exception){
                e.printStackTrace()
            }
            CoroutineScope(Dispatchers.Main).launch {

                findNavController().navigate(R.id.action_nav_playPartyMaster_to_nav_playParty, bundle)
            }
        }
    }

    private fun setParty(partyId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            if (partyId == 0){
                withContext(Dispatchers.Main){
                    Toast.makeText(requireContext(), getString(R.string.error_party_not_found), Toast.LENGTH_LONG).show()
                }
            }else{
                initDb(partyId)
                starServerUdp()
                starServerTcp()
            }
        }
    }
    /**
    Actualiza la lista de jugadores.
    Este método actualiza la lista de jugadores en la interfaz de usuario.
    */
    fun updatePlayerList(){
       listaJugadores.forEach {
           binding.tvPlayerList.append("${it.key.name} - ${listaJudoresSeleccion.get(it.key.name)}\n")
       }
    }
    /**
    Inicializa la base de datos.
    Este método inicializa la base de datos utilizando el ID de fiesta proporcionado.
    @param partyId El ID de la fiesta.
     */
     suspend fun initDb(partyId: Int) {
         val db = PartyDb.getDatabase(requireContext())
         party =db.partyDao().getParty(partyId)
         val playerCharacters = db.playerCharacterDao().getPlayersAndCharactersByPartyId(partyId)
         val characters = mutableListOf<CharacterEntity>()
         playerCharacters.forEach {
                characters.add(db.characterDao().getCharacterById(it.characterID))
            }
         characterList = characters
    }
    /**
    Inicia el servidor UDP.
    Este método inicia un servidor UDP en segundo plano utilizando un hilo de fondo y CoroutineScope.
    */
    private fun starServerUdp(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                socketUdp = DatagramSocket(5689)
                val buffer = ByteArray(1024)
                while (true) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socketUdp.receive(packet)
                    sleep(1000)
                    val address = packet.address
                    val data = party.toJson().toByteArray()
                    val packet2 = DatagramPacket(data, data.size, address, 5688)
                    socketUdp.send(packet2)
                }
            }catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }
    /**
    Cierra el servidor UDP.
    */
    private fun closeServerUdp(){
        try {
            socketUdp.close()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
    /**
    Inicia el servidor TCP.
    Este método inicia un servidor TCP en segundo plano utilizando un hilo de fondo y CoroutineScope.
    */
    private fun starServerTcp() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                serverSocket = ServerSocket(5690)
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(requireContext(), getString(R.string.waiting_for_user_requests), Toast.LENGTH_SHORT).show()
                }
                while (true) {
                    val socket = withContext(Dispatchers.IO) {
                        serverSocket.accept()
                    }
                    val serverThread = ServerThread(socket, this@PlayPartyMasterFragment)
                    serverThread.start()
                }
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    /**
    Cierra el servidor TCP.
    */
    private fun closeServerTcp(){
        try {
            serverSocket.close()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
    /**
    Obtener la dirección IP.
    */
    private suspend fun getIp(): String{
        val wifiManager = context!!.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = wifiManager.connectionInfo.ipAddress
        val addressBytes = byteArrayOf(
            (ipAddress and 0xff).toByte(),
            (ipAddress shr 8 and 0xff).toByte(),
            (ipAddress shr 16 and 0xff).toByte(),
            (ipAddress shr 24 and 0xff).toByte()
        )
        return withContext(Dispatchers.IO) {
            InetAddress.getByAddress(addressBytes)
        }.hostAddress ?: ""
    }

    /**
    Mostrar petición.
    Este método muestra una petición en forma de diálogo utilizando AlertDialog.
    @param message título para personalizar el contenido del diálogo.
    */
    fun showPetition(message: String, title:String) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton(getString(R.string.accept)) { _, _ ->
                lastResponse.value= true
            }
            builder.setNegativeButton(getString(R.string.decline)) { _, _ ->
                lastResponse.value = false
            }
            val dialog = builder.create()
            dialog.show()
    }


}
private class ServerThread(val clientSocket: Socket, val fragment: PlayPartyMasterFragment) : Thread() {
    override fun run() {
        try {
            val input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val output = BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream()))

            val message = input.readLine()

            processMessage(message, output)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    /**
    Procesar mensaje.
    Este método procesa un mensaje recibido y realiza acciones en función del contenido del mensaje.
    @param message mensaje en formato de cadena
    @param output objeto BufferedWriter para enviar respuestas.
    */
    private fun processMessage(message: String, output: BufferedWriter) {
        val decodedMensaje = ComunicationHelpers.getMapFromJson(message)
        val peticion = decodedMensaje["peticion"]
        when (peticion) {
            "join" -> {
                val nombre = decodedMensaje["nombre"]
                fragment.activity?.runOnUiThread {
                    fragment.showPetition(fragment.getString(R.string.user_want_to_join,nombre), fragment.getString(R.string.union_request))
                }
                    while (PlayPartyMasterFragment.lastResponse.value == null) {
                        sleep(1000)
                    }
                if (PlayPartyMasterFragment.lastResponse.value!!) {
                    val jsonCharacterEntity = mutableListOf<String>()
                    fragment.characterList.forEach {
                        jsonCharacterEntity.add(it.toJson())
                    }
                    val sendResponse = listOf("reponse" to "ok",
                                        "party" to fragment.party.toJson(),
                                        "characters" to jsonCharacterEntity)
                    output.write(Gson().toJson(sendResponse))
                    output.newLine()
                    output.flush()
                } else {
                    val sendResponse = listOf("reponse" to "no")
                    output.write(Gson().toJson(sendResponse))
                    output.newLine()
                    output.flush()
                }
            }
            "start" -> {
                val alias = decodedMensaje["alias"]
                val hash = decodedMensaje["hash"]
                val characterOwn = decodedMensaje["characterOwn"].toBoolean()
                val selectedCharacter = CharacterEntity.fromJson(decodedMensaje["selectedCharacter"]!!)
                CoroutineScope(Dispatchers.IO).launch {
                    addPlayerCharacter(hash!!, selectedCharacter!!, characterOwn)
                }
                addPlayer(alias!!, clientSocket.inetAddress.hostAddress, hash!!, selectedCharacter!!.name)
                val sendResponse = listOf("reponse" to "ok")
                output.write(Gson().toJson(sendResponse))
                output.newLine()
                output.flush()
            }
            "adios" -> {
                output.write("adios")
            }
            else -> {
                output.write("no entiendo")
            }
        }
    }
    /**
    Agregar personaje del jugador.
    Este método suspendido agrega un personaje seleccionado por un jugador a la base de datos y lo asigna al jugador correspondiente.
    @param hash del jugador
    @param personaje seleccionado
    @param indicador de si el personaje es propio del jugador.
    */
    private suspend fun addPlayerCharacter(hash: String, selectedCharacter: CharacterEntity, characterOwn: Boolean) {
        if (characterOwn){
            val characterId = PartyDb.getDatabase(fragment.requireContext()).characterDao().insertCharacter(CharacterEntity.removeIdFromCharacter(selectedCharacter))
            val savedCharacter = PartyDb.getDatabase(fragment.requireContext()).characterDao().getCharacterById(characterId.toInt())
            fragment.listaJugadoresCharacter.put(hash, savedCharacter)
        }else{
            fragment.listaJugadoresCharacter.put(hash, selectedCharacter)
        }
    }
    /**
    Agregar jugador.
    Este método agrega un jugador a la lista de jugadores en el fragmento y actualiza la lista de jugadores en la interfaz de usuario.
    @param el alias del jugador,
    @param dirección IP
    @param hash del jugador.
     */
    private fun addPlayer(alias: String, ip:String, hash:String, selectedCharacter: String) {
        val player = Player(name = alias, identifier = hash)
        fragment.listaJugadores.put(player,ip)
        fragment.listaJudoresSeleccion.put(player.name, selectedCharacter)
        fragment.activity?.runOnUiThread {
            fragment.updatePlayerList()
        }
    }

    override fun interrupt() {
        super.interrupt()
        clientSocket.close()
    }
}