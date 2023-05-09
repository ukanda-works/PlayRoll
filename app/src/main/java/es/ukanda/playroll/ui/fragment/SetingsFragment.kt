package es.ukanda.playroll.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import es.ukanda.playroll.R
import es.ukanda.playroll.databinding.FragmentPartiesListBinding
import es.ukanda.playroll.databinding.FragmentSetingsBinding

class SetingsFragment : Fragment() {
    private var _binding: FragmentSetingsBinding ? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentSetingsBinding.inflate(inflater, container, false)
        return binding.root
    }
}