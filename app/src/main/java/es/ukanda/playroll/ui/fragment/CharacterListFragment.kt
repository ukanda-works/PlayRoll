package es.ukanda.playroll.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import es.ukanda.playroll.databinding.FragmentCharacterListBinding


class CharacterListFragment : Fragment() {
    private var _binding: FragmentCharacterListBinding ? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentCharacterListBinding.inflate(inflater, container, false)
        return binding.root
    }
}