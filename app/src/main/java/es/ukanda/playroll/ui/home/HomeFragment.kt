package es.ukanda.playroll.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import es.ukanda.playroll.R
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.databinding.FragmentHomeBinding
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.entyties.PartieEntities.Party
import es.ukanda.playroll.pruebas.Test_uno
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spiners()
        buttons()
    }

    private fun buttons() {
        binding.btCrearPartida.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_PartyCreator)
        }

        binding.btAddCharacter.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_CharacterCreator)
        }
        binding.btJoinParty.setOnClickListener{
            findNavController().navigate(R.id.action_nav_home_to_nav_JoinParty)
        }
    }

    private fun spiners() {
        lifecycleScope.launch(Dispatchers.IO) {
            //se inicializan los spinners con los datos de la base de datos
            val database = PartyDb.getDatabase(context!!)
            var partyList = database.partyDao().getAllParties()
            var characterList =
                database.characterDao().getAllCharacters()

                val party = Party(0, "No hay partidas","")
                partyList.add(0, party)
            if(characterList.isEmpty()){
                val character = CharacterEntity(0, "No hay personajes", "",0)
                characterList += character
            }
            //se crean los adaptadores
            val adapterParty =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, partyList.map { it.partyName })
            val adapterCharacter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, characterList.map { it.name})

            adapterParty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapterCharacter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            //se asignan los adaptadores a los spinners
            binding.spParties.adapter = adapterParty
            binding.spCharacters.adapter = adapterCharacter
            binding.spParties.setSelection(0)
            binding.spCharacters.setSelection(0)

            //seteamos el listener de los spinners
            //navegamos a la pantalla de la partida o personaje seleccionado enviando id


            binding.spParties.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, id: Long) {
                val party = partyList[position]
                    val bundle = Bundle()
                    bundle.putInt("id", party.partyID)
                    Toast.makeText(context, "Seleccionado: ${position}, party id: ${party.partyID}", Toast.LENGTH_SHORT).show()
                    if(party.partyID != 0){
                        findNavController().navigate(R.id.action_nav_home_to_nav_PartyManager, bundle)
                    }
                }
                override fun onNothingSelected(adapterView: AdapterView<*>) {
                    // Nothing to do here
                }
            }

            /*binding.spCharacters.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val character = characterList[position]
                    val bundle = Bundle()
                    bundle.putInt("id", character.characterID)
                    if(character.characterID != 0){
                        findNavController().navigate(R.id.action_nav_home_to_nav_CharacterInfo, bundle)
                    }else{
                        Toast.makeText(context, "No hay personajes", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }*/


        }


    }

    override fun onResume() {
        super.onResume()
        session()

    }

    fun session(){
        val pref = activity?.getSharedPreferences(getString(R.string.prefs_file), 0)
        val email = pref?.getString("email", null)
        val provider = pref?.getString("provider", null)

        if (email != null && provider != null) {
            Toast.makeText(context, "Sesion identificada, bienvenido ${email}", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(context, "Necesitas iniciar sesion", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_nav_home_to_nav_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}