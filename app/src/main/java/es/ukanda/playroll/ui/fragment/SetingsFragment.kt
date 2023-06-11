package es.ukanda.playroll.ui.fragment

import android.app.AlertDialog
import android.content.Context
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showDevelopmentAlert(requireContext())
    }
    /**
    Mostrar alerta de desarrollo.
    Este método muestra un cuadro de diálogo de alerta para informar al usuario que la función está en desarrollo.
    Realiza las siguientes acciones:
    Crea un constructor de AlertDialog.Builder con el contexto proporcionado.
    Establece el título del cuadro de diálogo como el texto correspondiente obtenido a través del contexto.
    Establece el mensaje del cuadro de diálogo como el texto correspondiente obtenido a través del contexto.
    Configura el botón positivo del cuadro de diálogo para cerrar el cuadro de diálogo al hacer clic.
    Crea y muestra el cuadro de diálogo.
    @param context El contexto en el que se mostrará la alerta de desarrollo.
    Nota: Este método asume la disponibilidad de los recursos de cadena correspondientes en el contexto para el título y el mensaje del cuadro de diálogo.
     */
    fun showDevelopmentAlert(context: Context) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(getString(R.string.developing))
        alertDialogBuilder.setMessage(getString(R.string.developing_text_alert))
        alertDialogBuilder.setPositiveButton(getString(R.string.accept)) { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}