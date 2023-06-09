package es.ukanda.playroll.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import es.ukanda.playroll.R
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.databinding.FragmentHelpBinding
import es.ukanda.playroll.databinding.FragmentPartiesListBinding
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.entyties.PartieEntities.Party
import es.ukanda.playroll.ui.adapter.CharacterListAdapter
import es.ukanda.playroll.ui.adapter.PartiesListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PartiesListFragment : Fragment() {
    private var _binding: FragmentPartiesListBinding? = null
    private val binding get() = _binding!!

    private lateinit var partyAdapter: PartiesListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentPartiesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRv()
    }

    private fun initRv() {
        CoroutineScope(Dispatchers.IO).launch {
            val partiesList = getParties()
            CoroutineScope(Dispatchers.Main).launch {
                partyAdapter = PartiesListAdapter(partiesList)
                binding.rvPatiesList.layoutManager = LinearLayoutManager(requireContext())
                binding.rvPatiesList.adapter = partyAdapter
            }
        }
    }

    suspend fun getParties(): List<Party>{
        val partiesList = mutableListOf<Party>()
        val db = PartyDb.getDatabase(requireContext())
        val dao = db.partyDao()
        dao.getAllParties().forEach {
            if (it.own){
                partiesList.add(it)
            }
        }
        return partiesList
    }
}