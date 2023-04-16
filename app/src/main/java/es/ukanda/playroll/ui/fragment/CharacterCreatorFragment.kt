package es.ukanda.playroll.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import es.ukanda.playroll.databinding.FragmentCharacterCreatorBinding
import es.ukanda.playroll.databinding.FragmentPartyManagerBinding

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


}