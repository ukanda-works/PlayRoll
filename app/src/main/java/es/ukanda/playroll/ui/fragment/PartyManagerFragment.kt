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
import es.ukanda.playroll.databinding.FragmentPartyManagerBinding
import es.ukanda.playroll.entyties.PartieEntities.Party
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PartyManagerFragment : Fragment() {
    private var _binding: FragmentPartyManagerBinding? = null
    private val binding get() = _binding!!

    lateinit var party: Party

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPartyManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val partyId = arguments?.getInt("id") ?: 0
        setParty(partyId)
    }

    private fun setParty(partyId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            if (partyId == 0){
            }else{
                party = PartyDb.getDatabase(requireContext()).partyDao().getParty(partyId)
            }
            buttons()
            texviews()
        }
    }

    private fun texviews() {
        if (party.partyID != 0){
            binding.tvNombrePartidaManager.text = party.partyName
            binding.tvDescripcionManager.text = party.partyDescription
        }
    }

    private fun buttons() {
        binding.btCompartiOnline.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.this_feature_is_under_development), Toast.LENGTH_SHORT).show()
        }
        binding.btCompartirLocal.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("id", party.partyID)
            if(party.partyID != 0){
                findNavController().navigate(R.id.action_nav_PartyManager_to_nav_playPartyMaster, bundle)
            }
        }
        binding.btEditar.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("id", party.partyID)
            if(party.partyID != 0){
                findNavController().navigate(R.id.action_nav_PartyManager_to_nav_PartyCreator, bundle)
            }
        }
    }
}