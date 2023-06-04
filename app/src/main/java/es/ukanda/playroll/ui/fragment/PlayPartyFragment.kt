package es.ukanda.playroll.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import es.ukanda.playroll.controllers.helpers.ComunicationHelpers
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.databinding.FragmentPlayPartyBinding
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.entyties.PartieEntities.Party
import es.ukanda.playroll.entyties.PartieEntities.Player
import es.ukanda.playroll.entyties.PartieEntities.PlayerCharacters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.*


class PlayPartyFragment : Fragment() {
    private var _binding: FragmentPlayPartyBinding? = null
    private val binding get() = _binding!!

    private val listenPort = 5691
    private val writePort = 5690
    private var isMaster = false
    lateinit var playersIp : Map<String, String>
    private lateinit var ipServer: String

    //relacionado a la partida
    //partida
    lateinit var party : Party
    //personajes
    var characters = mutableListOf<CharacterEntity>()
    //jugadores
     var players = mutableListOf<Player>()
    //relacion entre personajes y jugadores
    var playerCharacters = mutableListOf<PlayerCharacters>()
    val dataLoaded = MutableLiveData<Boolean>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayPartyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataLoaded.observe(viewLifecycleOwner, Observer { loaded ->
            if (loaded == true) {
                Toast.makeText(requireContext(), "Datos cargados", Toast.LENGTH_SHORT).show()
                initListenerSocket()
                initTabs()
            }
        })
        loadBundle()

    }

    private fun loadBundle() {
        val bundle = arguments
        println("bundle: $bundle----------------------------------------")
        if (bundle != null) {
            val master = bundle.getBoolean("isMaster")
            if (master) {
                isMaster = true
                playersIp = bundle.getSerializable("playersIp") as Map<String, String>
            }else{
                ipServer = bundle.getString("ipServer").toString()
            }
            val partyId = bundle.getInt("party")
            CoroutineScope(Dispatchers.IO).launch {
                initDb(partyId)
            }
        }else{
            Toast.makeText(requireContext(), "Error al cargar los datos", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun initDb(partyId: Int) {
        val db = PartyDb.getDatabase(requireContext())
        party = db.partyDao().getParty(partyId)
        playerCharacters.addAll(db.playerCharacterDao().getPlayersAndCharactersByPartyId(partyId))
        playerCharactersCompanion.addAll(db.playerCharacterDao().getPlayersAndCharactersByPartyId(partyId))
        //jugadores
        val playersId = playerCharacters.map { it.playerID }
        playersId.forEach {
            val player = db.playerDao().getPlayerById(it!!)
            players.add(player)
            playersCompanion.add(player)
        }
        val charactersId = playerCharacters.map { it.characterID }
        charactersId.forEach {
            val character = db.characterDao().getCharacterById(it!!)
            characters.add(character)
            charactersCompanion.add(character)
        }
        CoroutineScope(Dispatchers.Main).launch {
            dataLoaded.value = true
        }
    }


    private fun initListenerSocket() {
        var port = listenPort
        if (isMaster) {
            port = writePort
        }
        CoroutineScope(Dispatchers.IO).launch {
            lateinit var serverSocket: ServerSocket
            try {
                println("puerto: $port")
                serverSocket = ServerSocket(port)

            }catch (e: BindException){
                println("error al crear el socket ${e.message}")
                // Se busca el socket que está usando la dirección y se cierra
                val inetAddress = InetAddress.getLocalHost()
                val socketAddress = InetSocketAddress(inetAddress, port)
                val conflictingSocket = ServerSocket()
                conflictingSocket.bind(socketAddress)
                conflictingSocket.close()
                serverSocket.close()
                serverSocket = ServerSocket(port)
            }catch(e: Exception) {
                e.printStackTrace()
            }
            while (true){
                val socket = withContext(Dispatchers.IO) {
                    serverSocket.accept()
                }
                val serverThread = ServerThread(socket, this@PlayPartyFragment)
                serverThread.start()
            }
        }
    }

    private fun initTabs() {
        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(PlayPartyPlayersFragment(), "Jugadores")
        adapter.addFragment(PlayPartyInventarioFragment(), "Inventario")
        adapter.addFragment(PlayPartyCombatFragment(), "Combate")
        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)

    }

    private inner class ViewPagerAdapter(manager: FragmentManager) :
        FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragmentList = mutableListOf<Fragment>()
        private val titleList = mutableListOf<String>()

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titleList[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            titleList.add(title)
        }
    }
    private class ServerThread(val clientSocket: Socket, val fragment: PlayPartyFragment) : Thread() {
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
        }

        override fun interrupt() {
            super.interrupt()
            clientSocket.close()
        }
    }

    class ClientSocket(val ip: String, val port: Int, Fragment: PlayPartyFragment){
        companion object {
            enum class RollDiceType {
                Salvacion, Ataque, Daño
            }
        }
        fun askRollDice(type: RollDiceType, num: Int){
            //se envia la peticion al master
            //se espera la respuesta
            //si el master acepta se muestra el dialogo para tirar dado
            //tras tirar el dado se envia el resultado al master

        }
    }

    companion object{
        private var instance: PlayPartyFragment? = null

        val playersCompanion = mutableListOf<Player>()
        val charactersCompanion = mutableListOf<CharacterEntity>()
        val playerCharactersCompanion = mutableListOf<PlayerCharacters>()

        fun getInstance(): PlayPartyFragment {
            if (instance == null) {
                instance = PlayPartyFragment()
            }
            return instance as PlayPartyFragment
        }
    }
}