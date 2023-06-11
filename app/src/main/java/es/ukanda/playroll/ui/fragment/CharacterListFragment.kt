package es.ukanda.playroll.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.CoreComponentFactory
import androidx.recyclerview.widget.LinearLayoutManager
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.databinding.FragmentCharacterListBinding
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.ui.adapter.CharacterAdapter
import es.ukanda.playroll.ui.adapter.CharacterListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CharacterListFragment : Fragment() {
    private var _binding: FragmentCharacterListBinding ? = null
    private val binding get() = _binding!!

    lateinit var characterAdapter: CharacterListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentCharacterListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRv()
    }
    /**
    Inicializa el RecyclerView para mostrar la lista de personajes.
    Este método utiliza una corrutina para obtener la lista de personajes en segundo plano
    y luego configura el adaptador del RecyclerView con la lista obtenida.
    Finalmente, asigna el adaptador y el administrador de diseño al RecyclerView.
     */
    private fun initRv() {
        CoroutineScope(Dispatchers.IO).launch {
            val characterList = getCharacters()
            CoroutineScope(Dispatchers.Main).launch {
                characterAdapter = CharacterListAdapter(characterList)

                binding.rvCharacterList.layoutManager = LinearLayoutManager(requireContext())
                binding.rvCharacterList.adapter = characterAdapter

            }
        }
    }
    /**
    Obtiene la lista de personajes.
    Este método suspendido busca en la base de datos todos los personajes y
    devuelve una lista de los personajes propios.
    @return La lista de personajes propios.
     */
    suspend fun getCharacters(): List<CharacterEntity>{
        val characterList = mutableListOf<CharacterEntity>()
       //busca en la base de datos todos los personajes
        val db = PartyDb.getDatabase(requireContext())
        val dao = db.characterDao()
        dao.getAllCharacters().forEach {
            if (it.own){
                characterList.add(it)
            }
        }
        return characterList
    }
}