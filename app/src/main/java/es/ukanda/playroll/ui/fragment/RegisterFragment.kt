package es.ukanda.playroll.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

import es.ukanda.playroll.R
import es.ukanda.playroll.databinding.FragmentLoginBinding
import es.ukanda.playroll.databinding.FragmentRegisterBinding
import es.ukanda.playroll.ui.home.HomeFragment


class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        register()
    }
    /**
    Registrar usuario.
    Este método configura el botón de registro para manejar la acción de registro de usuario utilizando Firebase Authentication.
     Este método debe ser llamado después de que se haya establecido el enlace de datos y el objeto binding esté disponible.
     */
    private fun register() {
        binding.btRegister.setOnClickListener {
            if (binding.etEmailRegister.text.isNotEmpty() && binding.etPasswordRegister.text.isNotEmpty()) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    binding.etEmailRegister.text.toString(),
                    binding.etPasswordRegister.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this.context, getString(R.string.user_successfully_registered), Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_nav_register_to_nav_home)
                    } else {
                        Toast.makeText(this.context, getString(R.string.failed_to_register), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}