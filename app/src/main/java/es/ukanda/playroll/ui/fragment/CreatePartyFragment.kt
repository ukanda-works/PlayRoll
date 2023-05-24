package es.ukanda.playroll.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.color.utilities.PointProviderLab
import com.google.firebase.auth.FirebaseAuth
import es.ukanda.playroll.R
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.databinding.FragmentCreatePartyBinding
import es.ukanda.playroll.databinding.FragmentLoginBinding
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.entyties.PartieEntities.Party
import es.ukanda.playroll.entyties.PartieEntities.PlayerCharacters
import es.ukanda.playroll.ui.adapter.CharacterAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CreatePartyFragment : Fragment() {
    private var _binding: FragmentCreatePartyBinding? = null
    private val binding get() = _binding!!
    private var characterList = mutableListOf<CharacterEntity>()
    private lateinit var characterAdapter: CharacterAdapter

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
        initRecycler()
        buttons()
    }

    private fun buttons() {
        binding.btCrear.setOnClickListener {
            createParty()
            findNavController().navigate(R.id.action_nav_PartyCreator_to_nav_home)
        }
    }

    private fun createParty() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val selectedCharacters = characterAdapter.getSelectedCharacters()
        //se añaden los personajes a la partida
        val passwd = binding.etPassword.text.toString()
        val partyConfig = HashMap<String, String>()
        partyConfig["OnlyOwn"] = binding.cbOnlyOwn.isChecked.toString()
        partyConfig["Pass"] = passwd
        val currentUser = firebaseAuth.currentUser
        val userName =  currentUser?.displayName ?: "Anonimo"
        val party = Party(partyName = binding.etName.text.toString(), partyCreator =  userName ,partyDescription = "", partyConfig = partyConfig)
        try{
            //se añade desde una corrutina
            CoroutineScope(Dispatchers.IO).launch {
                //la partida
                var partida = PartyDb.getDatabase(requireContext()).partyDao().insertParty(party)
                //los personajes a la entidad playerCharacter
                println("Partida creada con id: $partida")
                if (!selectedCharacters.isEmpty()){
                    selectedCharacters.forEach {
                    PartyDb.getDatabase(requireContext()).playerCharacterDao().insertPartyPlayerCharacter(
                        PlayerCharacters(
                            partyID = partida.toInt(),
                            characterID = it.characterID))
                }
                }
            }
            println("Partida creada")
            Toast.makeText(context, "Partida creada", Toast.LENGTH_SHORT).show()
        }catch (e: Exception){
            println("Error al crear la partida")
            println(e)
        }
    }

    fun initRecycler(){
        //corrutina
        CoroutineScope(Dispatchers.IO).launch {
            //se obtienen los personajes del usuario
            val listCharacter = PartyDb.getDatabase(requireContext()).characterDao().getAllCharacters()
            characterList = listCharacter.toMutableList()
            characterAdapter = CharacterAdapter(characterList, characterList.count()) { character, isSelected ->

            }
            binding.rvCharactersCreator.layoutManager  = LinearLayoutManager(context)
            binding.rvCharactersCreator.adapter = characterAdapter
        }
    }

    private fun getSelectedCharacters(): List<CharacterEntity>{
        val selectedCharacters = characterAdapter.getSelectedCharacters()
        return selectedCharacters
    }
}