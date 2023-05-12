package es.ukanda.playroll.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

import es.ukanda.playroll.R
import es.ukanda.playroll.databinding.FragmentLoginBinding
import es.ukanda.playroll.databinding.FragmentSignOffBinding


class SignOffFragment : Fragment() {

    private var _binding: FragmentSignOffBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignOffBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    private fun setup() {
        val pref = activity?.getSharedPreferences(getString(R.string.prefs_file), 0)
        println("pref:")
        println(pref?.getString("email", "no hay email"))
        val email = pref?.getString("email", null)

        val provider = pref?.getString("provider", null)
        binding.btSignOut.setOnClickListener {
            if (email != null && provider != null) {
               FirebaseAuth.getInstance().signOut()
                val pref = activity?.getSharedPreferences(getString(R.string.prefs_file), 0)?.edit()
                pref?.clear()
                pref?.apply()
                Toast.makeText(context, "Sesion cerrada", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, "Necesitas iniciar sesion", Toast.LENGTH_SHORT).show()

            }
        }
    }

}