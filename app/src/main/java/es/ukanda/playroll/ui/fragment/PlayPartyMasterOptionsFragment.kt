package es.ukanda.playroll.ui.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import es.ukanda.playroll.R
import es.ukanda.playroll.controllers.helpers.ComunicationHelpers
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.databinding.FragmentPlayPartyMasterOptionsBinding
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket


class PlayPartyMasterOptionsFragment : Fragment() {

    private var _binding: FragmentPlayPartyMasterOptionsBinding? = null
    private val binding get() = _binding!!

    val instance = PlayPartyFragment.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayPartyMasterOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttons()
    }

    private fun buttons() {
        binding.btTiradaSalvacion.setOnClickListener {
            mostrarDialogoPedirDados()
        }
        binding.btCloseParty.setOnClickListener {
        }
        binding.btCloseParty.setOnClickListener {
            exitParty()
        }

    }

    private fun exitParty() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.exit_party))
        builder.setMessage(getString(R.string.confirm_exit_party))
        builder.setPositiveButton(getString(R.string.yes)) { dialog, which ->
            sendByMensajeForAll()
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, which ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun sendByMensajeForAll() {
        val mensaje = Gson().toJson(listOf("peticion" to "end_party"))
        CoroutineScope(Dispatchers.IO).launch{
            val db = PartyDb.getDatabase(context!!)
            for (player in PlayPartyFragment.playersIpCompanion){
                val ip = player.value
                val socket = Socket(ip, instance.listenPort)
                socket.soTimeout = 5000
                val output = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                output.write(mensaje)
                output.flush()
                socket.close()
            }
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context!!, getString(R.string.party_ended), Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_nav_playParty_to_nav_home)
            }
        }
    }

    fun mostrarDialogoPedirDados(){
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(getString(R.string.ask_roll_dice))
        val view = layoutInflater.inflate(R.layout.dialog_send_roll_dice, null)
        builder.setView(view)

        builder.setPositiveButton(getString(R.string.accept), DialogInterface.OnClickListener { dialog, which ->
            val number = view.findViewById<EditText>(R.id.etNumeroCaras).text.toString().toInt()
            sendAskRollDice(number)
            dialog.dismiss()
        })

        builder.setNegativeButton(getString(R.string.cancel), DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        val dialog = builder.create()
        dialog.show()
    }

    fun sendAskRollDice(num: Int) {
        val mensaje = Gson().toJson(listOf("peticion" to "roll_dice", "dice_num" to num))
        CoroutineScope(Dispatchers.IO).launch{
            for (player in PlayPartyFragment.playersIpCompanion){
                val ip = player.value
                val socket = Socket(ip, instance.listenPort)
                socket.soTimeout = 5000
                val output = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                output.write(mensaje)
                output.newLine()
                output.flush()
                socket.close()
            }
        }
    }
}