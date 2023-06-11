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
    /**
    Configurar funcionalidad de cierre de sesión.
    Este método configura la funcionalidad de cierre de sesión al hacer clic en el botón correspondiente.
    Realiza las siguientes acciones:
    Obtiene el objeto SharedPreferences del archivo de preferencias utilizando el contexto de la actividad.
    Recupera el correo electrónico y el proveedor de autenticación almacenados en las preferencias.
    Configura un listener de clic para el botón de cierre de sesión.
    Si el correo electrónico y el proveedor no son nulos, se ejecuta el siguiente conjunto de acciones:
    Cierra la sesión actual del usuario utilizando FirebaseAuth.getInstance().signOut().
    Obtiene un editor de SharedPreferences y lo limpia para eliminar los datos de sesión almacenados.
    Aplica los cambios en las preferencias.
    Muestra un mensaje de notificación al usuario indicando que se ha cerrado la sesión.
    Si el correo electrónico o el proveedor es nulo, se muestra un mensaje de notificación indicando que es necesario iniciar sesión.
    Nota: Este método asume la disponibilidad de los recursos de cadena correspondientes en el contexto para los mensajes de notificación.
     */
    private fun setup() {
        val pref = activity?.getSharedPreferences(getString(R.string.prefs_file), 0)
        val email = pref?.getString("email", null)

        val provider = pref?.getString("provider", null)
        binding.btSignOut.setOnClickListener {
            if (email != null && provider != null) {
               FirebaseAuth.getInstance().signOut()
                val pref = activity?.getSharedPreferences(getString(R.string.prefs_file), 0)?.edit()
                pref?.clear()
                pref?.apply()
                Toast.makeText(context, getString(R.string.closed_session), Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, getString(R.string.you_need_to_login), Toast.LENGTH_SHORT).show()

            }
        }
    }

}