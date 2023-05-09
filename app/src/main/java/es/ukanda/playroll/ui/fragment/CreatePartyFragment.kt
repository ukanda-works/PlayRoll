package es.ukanda.playroll.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import es.ukanda.playroll.R
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.databinding.FragmentCreatePartyBinding
import es.ukanda.playroll.databinding.FragmentLoginBinding
import es.ukanda.playroll.entyties.PartieEntities.Party
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CreatePartyFragment : Fragment() {
    private var _binding: FragmentCreatePartyBinding? = null
    private val binding get() = _binding!!

    companion object{

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePartyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttons()
    }

    private fun buttons() {
        binding.btCrear.setOnClickListener {
            createParty()
            findNavController().navigate(R.id.action_nav_PartyCreator_to_nav_home)
        }
    }

    private fun createParty() {
        val party = Party(partyName = binding.etName.text.toString(), partyDescription = binding.etDescripcion.text.toString())
        try{
            //se añade desde una corrutina
            CoroutineScope(Dispatchers.IO).launch {
                PartyDb.getDatabase(requireContext()).partyDao().insertParty(party)
            }
            println("Partida creada")
            Toast.makeText(context, "Partida creada", Toast.LENGTH_SHORT).show()
        }catch (e: Exception){
            println("Error al crear la partida")
            println(e)
        }
    }
}