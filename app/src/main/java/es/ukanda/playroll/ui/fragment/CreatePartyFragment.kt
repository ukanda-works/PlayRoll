package es.ukanda.playroll.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.CorrectionInfo
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.color.utilities.PointProviderLab
import com.google.firebase.auth.FirebaseAuth
import es.ukanda.playroll.R
import es.ukanda.playroll.controllers.game.GameController
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.databinding.FragmentCreatePartyBinding
import es.ukanda.playroll.databinding.FragmentLoginBinding
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.entyties.PartieEntities.Party
import es.ukanda.playroll.entyties.PartieEntities.Player
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
    private lateinit var gameController: GameController
    private lateinit var currentPlayer: Player

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
        gameController = GameController(context!!)
        initLoad()
        initRecycler()
        buttons()
    }
    /**
    Inicializa la carga de datos.
    Este método utiliza corrutinas para obtener el jugador actual en segundo plano y configurar los datos de la fiesta.
    Primero, se obtiene el jugador actual utilizando el controlador del juego en un contexto de Dispatchers.IO.
    Luego, se verifica si se proporcionó un ID de fiesta en los argumentos.
    Si se proporciona un ID de fiesta, se obtiene la fiesta correspondiente de la base de datos en un contexto de Dispatchers.IO
    y se configuran los datos en los campos correspondientes de la interfaz de usuario.
     */
    private fun initLoad() {
        CoroutineScope(Dispatchers.IO).launch{
            currentPlayer = gameController.getCurrentPlayer()
        }
        val partyId = arguments?.getInt("id") ?: 0
        if (partyId != 0){
            CoroutineScope(Dispatchers.IO).launch {
                val party = PartyDb.getDatabase(requireContext()).partyDao().getParty(partyId)
                binding.etName.setText(party.partyName)
                binding.cbOnlyOwn.isChecked = party.partyConfig?.get("OnlyOwn")!!.toBoolean()
                binding.etPassword.setText(party.partyConfig!!["Pass"])
            }
        }
    }
    /**

    Configura los botones de la interfaz de usuario.
    Este método asigna un listener al botón "Crear" que llama al método createParty()
    y navega al fragmento de inicio después de crear la fiesta.
     */
    private fun buttons() {
        binding.btCrear.setOnClickListener {
            createParty()
            findNavController().navigate(R.id.action_nav_PartyCreator_to_nav_home)
        }
    }
    /**
    Crea una nueva fiesta.
    Este método obtiene la instancia de FirebaseAuth y los personajes seleccionados de characterAdapter.
    Luego, se obtiene la contraseña y la configuración de la fiesta desde los campos de la interfaz de usuario.
    A continuación, se obtiene el usuario actualmente autenticado y se guarda su nombre.
    Se crea una nueva instancia de Party con los datos proporcionados.
    Finalmente, se inserta la fiesta en la base de datos utilizando corrutinas y se insertan los personajes asociados
    a la entidad PlayerCharacter si hay personajes seleccionados.
     */
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
        val party = Party(partyName = binding.etName.text.toString(), partyCreator =  userName ,partyDescription = "", partyConfig = partyConfig, own = true)
        try{
            //se añade desde una corrutina
            CoroutineScope(Dispatchers.IO).launch {
                //la partida
                var partida = PartyDb.getDatabase(requireContext()).partyDao().insertParty(party)
                //los personajes a la entidad playerCharacter
                if (!selectedCharacters.isEmpty()){
                    selectedCharacters.forEach {
                    PartyDb.getDatabase(requireContext()).playerCharacterDao().insertPartyPlayerCharacter(
                        PlayerCharacters(
                            partyID = partida.toInt(),
                            characterID = it.characterID,
                            playerID = currentPlayer.playerID))
                    }
                }
            }
            Toast.makeText(context, getString(R.string.party_created), Toast.LENGTH_SHORT).show()
        }catch (e: Exception){
            println(e)
        }
    }
    /**
    Inicializa el RecyclerView y muestra la lista de personajes disponibles.
    Este método obtiene la lista de personajes de la base de datos utilizando corrutinas.
    Luego, crea una instancia de CharacterAdapter y la asocia al RecyclerView.
    También configura el LayoutManager del RecyclerView para mostrar los elementos en una lista vertical.
     */
    fun initRecycler(){
        CoroutineScope(Dispatchers.IO).launch {
            val listCharacter = PartyDb.getDatabase(requireContext()).characterDao().getAllCharacters()
            characterList = listCharacter.toMutableList()
            characterAdapter = CharacterAdapter(characterList, characterList.count()) { character, isSelected ->
            }
            binding.rvCharactersCreator.layoutManager  = LinearLayoutManager(context)
            binding.rvCharactersCreator.adapter = characterAdapter
        }
    }
    /**
    Obtiene los personajes seleccionados en el RecyclerView.
    @return Lista de CharacterEntity que representa los personajes seleccionados.
     */
        private fun getSelectedCharacters(): List<CharacterEntity>{
            val selectedCharacters = characterAdapter.getSelectedCharacters()
            return selectedCharacters
        }
}