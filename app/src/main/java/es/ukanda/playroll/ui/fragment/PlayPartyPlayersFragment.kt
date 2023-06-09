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

    private fun buttons() {
        binding.btExitParty.setOnClickListener {
            instance.exitParty()
        }
    }

    private fun initRecyclerView() {
        playerAdapter = PlayersPlayPartyAdapter(PlayPartyFragment.playersCompanion,
                        PlayPartyFragment.charactersCompanion,
                        PlayPartyFragment.playerCharactersCompanion)
        binding.rvPlayersPlayParty.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlayersPlayParty.adapter = playerAdapter
    }
}