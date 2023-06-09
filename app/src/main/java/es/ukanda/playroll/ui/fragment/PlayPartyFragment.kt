package es.ukanda.playroll.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import es.ukanda.playroll.R
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

    lateinit var clientSocket: ClientSocket

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
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        dataLoaded.observe(viewLifecycleOwner, Observer { loaded ->
            if (loaded == true) {
                Toast.makeText(requireContext(), getString(R.string.uploaded_data), Toast.LENGTH_SHORT).show()
                initListenerSocket()
                initTabs()
            }
        })
        loadBundle()
        initSockets()

    }

    private fun initSockets() {
        CoroutineScope(Dispatchers.IO).launch {
            if (isMaster) {
                //clientSocket = ClientSocketServer(playersIp, listenPort, this@PlayPartyFragment)
            }else{
                clientSocket = ClientSocket(ipServer, writePort, this@PlayPartyFragment)
            }
        }
    }

    private fun loadBundle() {
        val bundle = arguments
        if (bundle != null) {
            val master = bundle.getBoolean("isMaster")
            if (master) {
                isMaster = true
                addMasterTab()
                playersIp = bundle.getSerializable("players") as Map<String, String>
            }else{
                ipServer = bundle.getString("ipServer").toString()
            }
            val partyId = bundle.getInt("party")
            CoroutineScope(Dispatchers.IO).launch {
                initDb(partyId)
            }
        }else{
            Toast.makeText(requireContext(), getString(R.string.error_loading_data), Toast.LENGTH_SHORT).show()
        }
    }



    private suspend fun initDb(partyId: Int) {
        CoroutineScope(Dispatchers.Main).launch{
            Toast.makeText(context, "cargando partida", Toast.LENGTH_SHORT).show()
        }
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
                serverSocket = ServerSocket(port)
                while (true){
                    val socket = withContext(Dispatchers.IO) {

                        serverSocket.accept()
                    }
                    val serverThread = ServerThread(socket, this@PlayPartyFragment)
                    serverThread.start()
                }
            }catch(e: Exception) {
                println("---------------------------------------")
                e.printStackTrace()
                println("---------------------------------------")
            }

        }
    }

    private fun initTabs() {
        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(PlayPartyPlayersFragment(), getString(R.string.players))
        adapter.addFragment(PlayPartyInventarioFragment(), getString(R.string.inventory))
        adapter.addFragment(PlayPartyCombatFragment(), getString(R.string.combat))
        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)

    }

    private fun addMasterTab() {
        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(PlayPartyMasterOptionsFragment(), getString(R.string.master_options))
        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    fun exitParty() {
        sendByMensaje()
        findNavController().navigate(R.id.action_nav_playParty_to_nav_home)
    }

    private fun sendByMensaje() {
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

        private val socket = Socket(ip, port)

        private fun sendMensaje(mensaje: String){
            if (socket.isClosed) {
                socket.connect(InetSocketAddress(ip, port))
            }
            val output = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
            output.write(mensaje)
            output.newLine()
            output.flush()
        }

        fun sendByMensaje(){
            val mensaje = listOf("peticion" to "exit")
            sendMensaje(Gson().toJson(mensaje))
        }
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

    class ClientSocketServer(val ipUsers: Map<String, String>, val port: Int, Fragment: PlayPartyFragment){



        fun sendByMensaje(){
            val mensaje = listOf("peticion" to "exit")
            //sendMensaje(Gson().toJson(mensaje))
        }
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