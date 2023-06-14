package es.ukanda.playroll.ui.fragment

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import es.ukanda.playroll.R
import es.ukanda.playroll.databinding.FragmentPlayPartyPlayersBinding
import es.ukanda.playroll.ui.adapter.PlayersPlayPartyAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.Socket

class PlayPartyPlayersFragment : Fragment() {

    private var _binding: FragmentPlayPartyPlayersBinding? = null
    private val binding get() = _binding!!

    lateinit var playerAdapter: PlayersPlayPartyAdapter
    val instance = PlayPartyFragment.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentPlayPartyPlayersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        buttons()
    }
    /**
    Configurar botones.
    */
    private fun buttons() {
        binding.btExitParty.setOnClickListener {
            exitParty()
        }
        binding.btPedirTirada.setOnClickListener {
            pedirTirada(PlayPartyFragment.ipServerCompanion, instance.writePort)
        }
        if (PlayPartyFragment.isMasterCompanion) {
            binding.btPedirTirada.visibility = View.GONE
        }
    }

    fun pedirTirada(ip: String, puerto: Int) {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(getString(R.string.ask_roll_dice))
        val view = layoutInflater.inflate(R.layout.dialog_send_roll_dice, null)
        builder.setView(view)

        builder.setPositiveButton(getString(R.string.accept), DialogInterface.OnClickListener { dialog, which ->
            val number = view.findViewById<EditText>(R.id.etNumeroCaras).text.toString().toInt()
            sendAskRollDice(number, ip, puerto)
            dialog.dismiss()
        })

        builder.setNegativeButton(getString(R.string.cancel), DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        val dialog = builder.create()
        dialog.show()
    }

    private fun sendAskRollDice(number: Int, ip: String, puerto: Int) {
        val mensaje = Gson().toJson(listOf("peticion" to "roll_dice_player", "dice_num" to number))
        CoroutineScope(Dispatchers.IO).launch {
            val socket = Socket(ip, puerto)
            val output = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
            output.write(mensaje)
            output.newLine()
            output.flush()
            socket.close()
        }
    }

    private fun exitParty() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.exit_party))
        builder.setMessage(getString(R.string.confirm_exit_party))
        builder.setPositiveButton(getString(R.string.yes)) { dialog, which ->
            sendByMensaje()
            findNavController().navigate(R.id.action_nav_playParty_to_nav_home)
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, which ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
    private fun sendByMensaje() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket = Socket(PlayPartyFragment.ipServerCompanion, instance.writePort)
                val output = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                val message = Gson().toJson(listOf("peticion" to "bye"))
                output.write(message)
                output.newLine()
                output.flush()
                socket.close()
                instance.endParty()
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }/**
    Inicializar RecyclerView.
     */
    private fun initRecyclerView() {
        playerAdapter = PlayersPlayPartyAdapter(PlayPartyFragment.playersCompanion,
                        PlayPartyFragment.charactersCompanion,
                        PlayPartyFragment.playerCharactersCompanion,
                        this@PlayPartyPlayersFragment)
        binding.rvPlayersPlayParty.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlayersPlayParty.adapter = playerAdapter
    }

    companion object{
        private var instance: PlayPartyPlayersFragment? = null
        fun getInstance(): PlayPartyPlayersFragment {
            if (instance == null) {
                instance = PlayPartyPlayersFragment()
            }
            return instance as PlayPartyPlayersFragment
        }
    }
}