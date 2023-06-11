package es.ukanda.playroll.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import es.ukanda.playroll.R
import es.ukanda.playroll.databinding.FragmentPlayPartyPlayersBinding
import es.ukanda.playroll.ui.adapter.PlayersPlayPartyAdapter

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
    Este método configura el botón de salida de la fiesta para manejar la acción de salida de la fiesta.
    Realiza las siguientes acciones:
    Asigna un evento de clic al botón de salida de la fiesta (btExitParty) en el enlace de datos (binding).
    Dentro del evento de clic, llama al método exitParty() de la instancia correspondiente para manejar la acción de salida de la fiesta.
    Nota: Este método asume la existencia de un objeto binding con una referencia al botón de salida de la fiesta (btExitParty) y una instancia adecuada que proporciona el método exitParty().
    Este método debe ser llamado después de que se haya establecido el enlace de datos y el objeto binding esté disponible.
     */
    private fun buttons() {
        binding.btExitParty.setOnClickListener {
            instance.exitParty()
        }
    }/**
    Inicializar RecyclerView.
    Este método inicializa el RecyclerView en el fragmento y configura su adaptador y administrador de diseño.
    Realiza las siguientes acciones:
    Crea una instancia del adaptador PlayersPlayPartyAdapter, pasando los compañeros de jugadores, personajes y jugadores-personajes correspondientes del fragmento PlayPartyFragment.
    Configura el administrador de diseño del RecyclerView para utilizar un LinearLayoutManager en el contexto actual.
    Asigna el adaptador al RecyclerView.
    Nota: Este método asume la existencia de un objeto binding con una referencia al RecyclerView (rvPlayersPlayParty) y los compañeros adecuados en el fragmento PlayPartyFragment.
    Este método debe ser llamado después de que se haya establecido el enlace de datos y el objeto binding esté disponible.
     */
    private fun initRecyclerView() {
        playerAdapter = PlayersPlayPartyAdapter(PlayPartyFragment.playersCompanion,
                        PlayPartyFragment.charactersCompanion,
                        PlayPartyFragment.playerCharactersCompanion)
        binding.rvPlayersPlayParty.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlayersPlayParty.adapter = playerAdapter
    }
}