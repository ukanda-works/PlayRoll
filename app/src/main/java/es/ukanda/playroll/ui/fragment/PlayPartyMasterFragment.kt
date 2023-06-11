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
    El botón "btStartGame" muestra un diálogo de confirmación para iniciar el juego.
    El botón "btnStopSharing" muestra un diálogo de confirmación para detener el intercambio.
    Si se confirma la acción en cualquiera de los diálogos, se ejecutan las respectivas funciones asociadas.
    En caso contrario, no se realiza ninguna acción.
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
                val playerId = db.playerDao().insertPlayer(it.key)
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
    /**
    Realiza las siguientes acciones:
    Se ejecuta en un hilo de fondo utilizando CoroutineScope.
    Verifica si el ID de la fiesta es igual a 0.
    Si es igual a 0, muestra un mensaje de error en el contexto actual utilizando Toast.
    Si no es igual a 0, realiza lo siguiente:
    - Inicializa la base de datos con el ID de la fiesta.
    - Inicia el servidor UDP.
    - Inicia el servidor TCP.
    Nota: Este método se ejecuta en un hilo de fondo utilizando CoroutineScope y muestra mensajes de error o realiza acciones en el hilo principal según sea necesario.
     */
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
    Recorre los elementos de la lista de jugadores y realiza lo siguiente para cada elemento:
    Añade el nombre del jugador y su valor asociado a la vista de texto "tvPlayerList" en formato "Nombre - Valor".
    Nota: Este método no realiza operaciones en hilos de fondo y se espera que se llame desde el hilo principal.
     */
    fun updatePlayerList(){
       listaJugadores.forEach {
           binding.tvPlayerList.append("${it.key.name} - ${it.value}\n")
       }
    }
    /**
    Inicializa la base de datos.
    Este método inicializa la base de datos utilizando el ID de fiesta proporcionado.
    Realiza las siguientes acciones:
    Se suspende y ejecuta en un hilo de fondo utilizando CoroutineScope y Dispatchers.IO.
    Obtiene una instancia de la base de datos de la fiesta utilizando el contexto actual.
    Recupera la fiesta correspondiente al ID de fiesta de la base de datos y la asigna a la variable "party".
    Obtiene los jugadores y los personajes asociados a la fiesta mediante la función "getPlayersAndCharactersByPartyId" de la base de datos.
    Crea una lista mutable de entidades de personajes y recorre los elementos obtenidos:
    Obtiene el personaje correspondiente a cada elemento mediante la función "getCharacterById" de la base de datos.
    Agrega el personaje a la lista "characterList".
    Nota: Este método se ejecuta en un hilo de fondo y se espera que sea llamado desde una función suspendida.
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
    Realiza las siguientes acciones:
    Crea un objeto DatagramSocket en el puerto 5689 para recibir paquetes UDP.
    Prepara un búfer de bytes para almacenar los datos recibidos.
    En un bucle infinito, realiza lo siguiente:
    Crea un DatagramPacket para recibir el paquete UDP entrante y lo almacena en el búfer.
    Espera durante 1000 milisegundos (1 segundo).
    Obtiene la dirección del remitente del paquete.
    Convierte la fiesta actual a formato JSON y la convierte en un arreglo de bytes.
    Crea un nuevo DatagramPacket con los datos y la dirección del remitente, y lo envía a través del puerto 5688.
    Captura cualquier excepción que ocurra e imprime la traza de errores.
    Nota: Este método se ejecuta en un hilo de fondo y se mantiene en ejecución continuamente hasta que ocurra una excepción.
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
    Este método cierra el servidor UDP.
    Realiza las siguientes acciones:
    Intenta cerrar el socket UDP utilizado por el servidor.
    Captura cualquier excepción que ocurra e imprime la traza de errores.
    Nota: Este método se utiliza para detener y cerrar el servidor UDP y se espera que se llame cuando sea necesario.
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
    Realiza las siguientes acciones:
    Crea un objeto ServerSocket en el puerto 5690 para escuchar las conexiones entrantes.
    Muestra un mensaje en el contexto actual utilizando Toast para indicar que se está esperando solicitudes de usuario.
    En un bucle infinito, realiza lo siguiente:
    Acepta una conexión entrante utilizando el método accept() del ServerSocket.
    Crea un nuevo hilo de servidor (ServerThread) pasando el socket de conexión y una referencia a esta instancia de PlayPartyMasterFragment.
    Inicia el hilo del servidor.
    Captura cualquier excepción que ocurra e imprime la traza de errores.
    Nota: Este método se ejecuta en un hilo de fondo y se mantiene en ejecución continuamente hasta que ocurra una excepción.
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
    Este método cierra el servidor TCP.
    Realiza las siguientes acciones:
    Intenta cerrar el socket del servidor utilizado por el servidor TCP.
    Captura cualquier excepción que ocurra e imprime la traza de errores.
    Nota: Este método se utiliza para detener y cerrar el servidor TCP y se espera que se llame cuando sea necesario.
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
    Este método suspendido obtiene la dirección IP del dispositivo.
    Realiza las siguientes acciones:
    Obtiene una instancia de WifiManager utilizando el servicio de contexto.
    Obtiene la dirección IP actual del dispositivo desde la información de conexión de Wi-Fi.
    Convierte la dirección IP en bytes utilizando operaciones de desplazamiento de bits.
    Utiliza withContext y Dispatchers.IO para realizar una llamada a red y obtener la dirección IP como InetAddress.
    Devuelve la dirección IP en formato de cadena o una cadena vacía si no se pudo obtener.
    Nota: Este método suspendido debe llamarse desde un contexto de CoroutineScope para permitir operaciones en el hilo principal y realizar la llamada a red en un hilo de fondo.
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
    Recibe un mensaje y un título para personalizar el contenido del diálogo.
    Realiza las siguientes acciones:
    Crea un objeto AlertDialog.Builder utilizando el contexto actual.
    Establece el título del diálogo utilizando el título proporcionado.
    Establece el mensaje del diálogo utilizando el mensaje proporcionado.
    Configura el botón positivo del diálogo para aceptar la petición y establece el valor de lastResponse como verdadero.
    Configura el botón negativo del diálogo para rechazar la petición y establece el valor de lastResponse como falso.
    Crea el diálogo utilizando create() y lo muestra.
    Nota: Este método debe ser llamado desde el contexto adecuado para mostrar el diálogo correctamente.
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
    Recibe el mensaje en formato de cadena y un objeto BufferedWriter para enviar respuestas.
    Realiza las siguientes acciones:
    Decodifica el mensaje JSON en un mapa utilizando el método getMapFromJson de la clase ComunicationHelpers.
    Obtiene la solicitud del mensaje ("peticion") y realiza acciones basadas en el tipo de solicitud.
    En el caso de "join":
    Obtiene el nombre del remitente del mensaje ("nombre").
    Muestra una petición al usuario utilizando showPetition del fragmento actual para solicitar su unión.
    Espera hasta que se obtenga una respuesta del usuario.
    Si la respuesta es positiva:
    diff
    - Crea una lista de personajes en formato JSON utilizando los personajes disponibles en el fragmento.
    css
    - Envía una respuesta positiva al remitente con la fiesta actual y la lista de personajes.
    Si la respuesta es negativa:
    css
    - Envía una respuesta negativa al remitente.
    En el caso de "start":
    Obtiene el alias, el hash y la información del personaje del remitente.
    Agrega el personaje seleccionado por el remitente a la base de datos en segundo plano.
    Agrega al jugador a la lista de jugadores en el fragmento.
    Envía una respuesta positiva al remitente.
    En el caso de "adios":
    Envía un mensaje de despedida al remitente.
    En cualquier otro caso:
    Envía un mensaje indicando que no se comprende la solicitud.
    Nota: Este método asume la existencia de un objeto PlayPartyMasterFragment con métodos y variables relacionadas, como showPetition, lastResponse, party y characterList.
    Este método debe ser llamado dentro del contexto adecuado y se espera que se utilice en un entorno de servidor para procesar mensajes recibidos.
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
                addPlayer(alias!!, clientSocket.inetAddress.hostAddress, hash!!)
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
    Recibe el hash del jugador, el personaje seleccionado y un indicador de si el personaje es propio del jugador.
    Realiza las siguientes acciones:
    Si el personaje es propio del jugador:
    Inserta el personaje en la base de datos utilizando el método insertCharacter de la clase CharacterDao en PartyDb.
    Recupera el personaje guardado de la base de datos utilizando el método getCharacterById de la clase CharacterDao en PartyDb.
    Asigna el personaje guardado al jugador correspondiente en el fragmento utilizando el hash del jugador como clave en el mapa listaJugadoresCharacter.
    Si el personaje no es propio del jugador:
    Asigna directamente el personaje seleccionado al jugador correspondiente en el fragmento utilizando el hash del jugador como clave en el mapa listaJugadoresCharacter.
    Nota: Este método suspendido asume la existencia de un objeto fragment con los contextos y métodos relacionados necesarios, así como el mapa listaJugadoresCharacter en el fragmento.
    Este método debe ser llamado dentro de un contexto de CoroutineScope para permitir operaciones en el hilo principal y realizar operaciones en la base de datos en un hilo de fondo.
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
    Recibe el alias del jugador, la dirección IP y el hash del jugador.
    Realiza las siguientes acciones:
    Crea un objeto Player con el nombre del jugador y el hash proporcionados.
    Agrega el jugador a la lista de jugadores en el fragmento utilizando el jugador como clave y la dirección IP como valor en el mapa listaJugadores.
    Actualiza la lista de jugadores en la interfaz de usuario llamando al método updatePlayerList del fragmento en el hilo principal.
     */
    private fun addPlayer(alias: String, ip:String, hash:String) {
        val player = Player(name = alias, identifier = hash)
        fragment.listaJugadores.put(player,ip)
        fragment.activity?.runOnUiThread {
            fragment.updatePlayerList()
        }
    }

    override fun interrupt() {
        super.interrupt()
        clientSocket.close()
    }
}