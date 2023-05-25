package es.ukanda.playroll.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import es.ukanda.playroll.controllers.helpers.ComunicationHelpers
import es.ukanda.playroll.databinding.FragmentPlayPartyBinding
import es.ukanda.playroll.databinding.FragmentPlayPartyMasterBinding
import es.ukanda.playroll.entyties.PartieEntities.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket


class PlayPartyFragment : Fragment() {
    private var _binding: FragmentPlayPartyBinding? = null
    private val binding get() = _binding!!

    private val listenPort = 5691
    private val writePort = 5690
    private var isMaster = false
    private lateinit var ipServer: String



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
        loadBundle()
        initListenerSocket()
        initTabs()

    }

    private fun loadBundle() {
        //recoge el bundle recibido y carga toda la informacion en los distintos fragments
    }

    private fun initListenerSocket() {
        var port = listenPort
        if (isMaster) {
            port = writePort
        }
        CoroutineScope(Dispatchers.IO).launch {
            val serverSocket = ServerSocket(port)
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
}