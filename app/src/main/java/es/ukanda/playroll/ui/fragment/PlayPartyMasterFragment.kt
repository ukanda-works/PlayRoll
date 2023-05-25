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
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import es.ukanda.playroll.R
import es.ukanda.playroll.controllers.comunication.PetitionResponseListerner
import es.ukanda.playroll.controllers.helpers.ComunicationHelpers
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.databinding.FragmentPlayPartyMasterBinding
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.entyties.PartieEntities.Party
import es.ukanda.playroll.entyties.PartieEntities.Player
import kotlinx.coroutines.*
import java.io.*
import java.lang.Thread.sleep
import java.net.*


class PlayPartyMasterFragment : Fragment(){

    private var _binding: FragmentPlayPartyMasterBinding? = null
    private val binding get() = _binding!!

    lateinit var party: Party
    var listaJugadores = mutableMapOf<Player, String>()
    lateinit var characterList : List<CharacterEntity>

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
        setBtn()

    }

    private fun setBtn() {
        binding.btStartGame.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Comenzar partida")
            builder.setMessage("¿Estas seguro de que quieres comenzar la partida?")
            builder.setPositiveButton("Si"){dialog, which ->
                startGame()
            }
            builder.setNegativeButton("No"){dialog, which ->
            }
        }
    }

    private fun startGame() {

        CoroutineScope(Dispatchers.IO).launch {
            //antes de enviarse las cosas se almacenan
            val db = PartyDb.getDatabase(requireContext())

            val partyId = db.partyDao().insertParty(party)
            val savedParty = db.partyDao().getParty(partyId.toInt())

            val savedPlayers = mutableListOf<Player>()
            val jsonPlayerList = mutableListOf<String>()
            listaJugadores.forEach {
                val playerId = db.playerDao().insertPlayer(it.key)
                val saved = db.playerDao().getPlayerById(playerId.toInt())
                savedPlayers.add(saved)
                jsonPlayerList.add(saved.toJson())
            }

            //TODO implementar inventarios


            //luego se leen y se envian
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
                                "player_character" to "")//hay que pasar la relacion entre jugadores y personajes
            val mensajeJson = gson.toJson(mensaje)
            listaJugadores.values.forEach{ ip ->
                var socket = Socket(ip, 5691)
                var writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                writer.write(mensajeJson)
                writer.flush()
                writer.close()
                socket.close()
            }
            val bundle = Bundle()
            bundle.putInt("id", savedParty.partyID)
            //el bundle de master incluira un map con un hash de jugadores y su ip
            bundle.putBoolean("isMaster", true)
            //esto tal vez haya que hacerlo en el hilo principal
            findNavController().navigate(R.id.action_nav_playPartyMaster_to_nav_playParty)
        }

    }

    private fun setParty(partyId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            if (partyId == 0){
                //lanzar error de que no se ha encontrado la party
            }else{
                initDb(partyId)
                starServerUdp()
                starServerTcp()
            }

        }
    }

    fun updatePlayerList(){
       listaJugadores.forEach {
           binding.tvPlayerList.append("${it.key.name} - ${it.value}\n")
       }
    }

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
            ip = InetAddress.getByAddress(addressBytes).hostAddress ?: ""
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
                    val jsonCharacterEntity = mutableListOf<String>()
                    fragment.characterList.forEach {
                        jsonCharacterEntity.add(it.toJson())
                    }
                    val sendResponse = listOf("reponse" to "ok",
                                        "party" to fragment.party.toJson(),
                                        "characters" to jsonCharacterEntity)
                    println("sendResponse: $sendResponse -----------------------------------------------------")
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
                val characterOwn = decodedMensaje["characterOwn"]
                val selectedCharacter = decodedMensaje["selectedCharacter"]
                //almacenar en la base de datos
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

    private fun addPlayer(alias: String, ip:String, hash:String) {
        val player = Player(name = alias, partyID = fragment.party.partyID, identifier = hash)
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