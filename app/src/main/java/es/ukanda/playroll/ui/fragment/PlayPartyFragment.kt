package es.ukanda.playroll.ui.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.findFragment
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
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.*
import kotlin.random.Random


class PlayPartyFragment : Fragment() {
    private var _binding: FragmentPlayPartyBinding? = null
    private val binding get() = _binding!!

    val listenPort = 5691
    val writePort = 5690

    private var isMaster = false
    lateinit var playersIp : Map<String, String>
    private lateinit var ipServer: String
    lateinit var serverSocket: ServerSocket

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
    }

    private fun loadBundle() {
        val bundle = arguments
        if (bundle != null) {
            val master = bundle.getBoolean("isMaster")
            if (master) {
                isMaster = true
                playersIp = bundle.getSerializable("players") as Map<String, String>
                CoroutineScope(Dispatchers.IO).launch{
                    playersIp.forEach(){
                        var alias = PartyDb.getDatabase(requireContext()).playerDao().getPlayerByIdentifier(it.key)!!.name
                        playersIpCompanion.put(alias, it.value)
                    }
                }

            }else{
                ipServer = bundle.getString("ipServer").toString()
                ipServerCompanion = ipServer
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
            Toast.makeText(context, getString(R.string.party_loaded), Toast.LENGTH_SHORT).show()
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
                e.printStackTrace()
            }

        }
    }

    private fun initTabs() {
        if (isMaster) {
            val adapter = ViewPagerAdapter(childFragmentManager)
            adapter.addFragment(PlayPartyPlayersFragment(), getString(R.string.players))
            adapter.addFragment(PlayPartyMasterOptionsFragment(), getString(R.string.master_options))
            adapter.addFragment(PlayPartyCombatFragment(), getString(R.string.combat))
            binding.viewPager.adapter = adapter
            binding.tabLayout.setupWithViewPager(binding.viewPager)
        }else{
            val adapter = ViewPagerAdapter(childFragmentManager)
            adapter.addFragment(PlayPartyPlayersFragment(), getString(R.string.players))
            adapter.addFragment(PlayPartyInventarioFragment(), getString(R.string.inventory))
            adapter.addFragment(PlayPartyCombatFragment(), getString(R.string.combat))
            binding.viewPager.adapter = adapter
            binding.tabLayout.setupWithViewPager(binding.viewPager)
        }


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
            val decodedMensaje = ComunicationHelpers.getMapFromJson(message)
            val peticion = decodedMensaje["peticion"]
            if(fragment.isMaster){
                when(peticion){
                    "roll_dice_result" ->{
                        val numResult = decodedMensaje["num"]!!.toInt()
                        val clientIp = clientSocket.inetAddress.hostAddress
                        var alias = ""
                        playersIpCompanion.forEach(){
                            if(it.value == clientIp){
                                alias = it.key
                            }
                        }
                        CoroutineScope(Dispatchers.Main).launch{
                            Toast.makeText(fragment.requireContext(), "Resultado de ${alias}: $numResult", Toast.LENGTH_SHORT).show()
                        }
                    }
                    "bye" -> {
                        val clientIp = clientSocket.inetAddress.hostAddress
                        var alias = ""
                        playersIpCompanion.forEach(){
                            if(it.value == clientIp){
                                alias = it.key
                            }
                        }
                        CoroutineScope(Dispatchers.Main).launch{
                            Toast.makeText(fragment.requireContext(), "Se ha desconectado ${alias}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    "roll_dice_player" ->{
                        val rollNumber = decodedMensaje["dice_num"]?.toInt()
                        val clientIp = clientSocket.inetAddress.hostAddress
                        var alias = ""
                        playersIpCompanion.forEach(){
                            if(it.value == clientIp){
                                alias = it.key
                            }
                        }
                        CoroutineScope(Dispatchers.Main).launch{
                            val builder = AlertDialog.Builder(fragment.requireContext())
                            builder.setTitle(fragment.getString(R.string.roll_dice))
                            builder.setMessage("${alias} ${fragment.getString(R.string.want_roll_dice)} ${rollNumber}")
                            builder.setPositiveButton(fragment.getString(R.string.yes)) { dialog, which ->
                                sendOkRoll(rollNumber!!, clientIp)
                            }
                            builder.setNegativeButton(fragment.getString(R.string.no)) { dialog, which ->
                                dialog.dismiss()
                            }
                            val dialog: AlertDialog = builder.create()
                            dialog.show()
                            interrupt()
                        }
                    }
                }
            }else{
                when(peticion){
                    "end_party" -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(fragment.requireContext(), fragment.getString(R.string.the_game_has_end), Toast.LENGTH_SHORT).show()
                        }
                        fragment.endParty()
                    }
                    "roll_dice" ->{
                        val rollNumber = decodedMensaje["dice_num"]?.toInt()
                        CoroutineScope(Dispatchers.Main).launch {
                            fragment.mostarDialogoLanzarDados(rollNumber!!)
                            interrupt()
                        }
                    }
                    "roll_dice_player_result" ->{
                        if(decodedMensaje["response"].equals("ok")){
                            val rollNumber = decodedMensaje["dice_num"]?.toInt()
                            CoroutineScope(Dispatchers.Main).launch {
                                fragment.mostarDialogoLanzarDados(rollNumber!!)
                                interrupt()
                            }
                        }else{
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(fragment.requireContext(), fragment.getString(R.string.denied), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

        private fun sendOkRoll(rollNumber: Int, ip: String) {
            val mensaje =Gson().toJson(listOf("peticion" to "roll_dice_player_result","response" to "ok", "dice_num" to rollNumber))
            CoroutineScope(Dispatchers.IO).launch {
                val socket = Socket(ip, fragment.listenPort)
                val output = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                output.write(mensaje)
                output.newLine()
                output.flush()
                socket.close()
            }

        }

        override fun interrupt() {
            super.interrupt()
            clientSocket.close()
        }
    }

    fun endParty() {
        try {
            serverSocket.close()
        }catch (e: Exception){
            e.printStackTrace()
        }
        findNavController().navigate(R.id.action_nav_playParty_to_nav_home)
    }

    companion object{
        private var instance: PlayPartyFragment? = null

        var playersIpCompanion = mutableMapOf<String, String>()
        val playersCompanion = mutableListOf<Player>()
        val charactersCompanion = mutableListOf<CharacterEntity>()
        val playerCharactersCompanion = mutableListOf<PlayerCharacters>()

        lateinit var ipServerCompanion: String

        fun getInstance(): PlayPartyFragment {
            if (instance == null) {
                instance = PlayPartyFragment()
            }
            return instance as PlayPartyFragment
        }
    }

    suspend fun mostarDialogoLanzarDados(num: Int){
        val builder = AlertDialog.Builder(this.context!!)
        builder.setTitle(getString(R.string.roll_dice))
        val view = layoutInflater.inflate(R.layout.dialog_roll_dice, null)
        builder.setView(view)
        view.findViewById<ImageView>(R.id.ivDice).setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val socket = Socket(ipServer, writePort)
                val output = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                val result = view.findViewById<TextView>(R.id.tvDiceNumber)
                var numResult = 0
                repeat(6) {
                    numResult = Random.nextInt(1, num)
                    delay(500)
                    withContext(Dispatchers.Main) {
                        result.text = numResult.toString()
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "${getString(R.string.result)}: $numResult", Toast.LENGTH_SHORT).show()
                }
                val mensaje = Gson().toJson(listOf("peticion" to "roll_dice_result", "num" to numResult))
                output.write(mensaje)
                output.newLine()
                output.flush()
                socket.close()
            }
        }
        val dialog = builder.create()
        withContext(Dispatchers.Main) {
            dialog.show()
        }
    }
}