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
    @param context El contexto en el que se mostrará la alerta de desarrollo.
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