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
import es.ukanda.playroll.databinding.FragmentCharacterCreatorBinding
import es.ukanda.playroll.databinding.FragmentPartyManagerBinding
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CharacterCreatorFragment : Fragment() {
    private var _binding: FragmentCharacterCreatorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharacterCreatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttons()
    }

    private fun buttons() {
        binding.btCrear.setOnClickListener {
            addCharacter()
            findNavController().navigate(R.id.action_nav_CharacterCreator_to_nav_home)
        }

        binding.btnCamera.setOnClickListener {
            findNavController().navigate(R.id.action_nav_CharacterCreator_to_nav_camera)
        }
    }

    private fun addCharacter() {
        val name = binding.etName.text.toString()
        val description = binding.etDescripcion.text.toString()
        if(name.isEmpty() || description.isEmpty()){
            Toast.makeText(context, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        val character = CharacterEntity(name = name, description = description)
        try{
            //se añade desde una corrutina
            CoroutineScope(Dispatchers.IO).launch {
                PartyDb.getDatabase(requireContext()).characterDao().insertCharacter(character)
            }
            println("Personaje creado")
        }catch (e: Exception){
            println("Error al crear el personaje")
            println(e)
            Toast.makeText(context, "Error al crear el personaje", Toast.LENGTH_SHORT).show()
        }
    }


}