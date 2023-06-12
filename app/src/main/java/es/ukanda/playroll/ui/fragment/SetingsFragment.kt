package es.ukanda.playroll.ui.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat.recreate
import androidx.navigation.fragment.findNavController
import es.ukanda.playroll.R
import es.ukanda.playroll.databinding.FragmentPartiesListBinding
import es.ukanda.playroll.databinding.FragmentSetingsBinding
import java.util.*

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
        spInit()
    }

    /**
     * Inicializa el spiner de selección de idioma.
     */
    private fun spInit() {
        val languages = resources.getStringArray(R.array.languages)
        val adapter = context?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, languages) }
        binding.spinner.adapter = adapter

        binding.spinner.setSelection(0)
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLanguage = parent.getItemAtPosition(position).toString()
                setLocale(selectedLanguage)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    /**
     * Establece el idioma de la aplicación.
     * @param language El idioma que se establecerá.
     */
    private fun setLocale(language: String) {
        val configuration = Configuration(resources.configuration)

        val currentLocale = configuration.locale
        println("currentLocale: $currentLocale")
        val locale = when (language) {
            "Español" -> Locale("es")
            "English" -> Locale("en")
            else -> Locale.getDefault() // Idioma predeterminado si no coincide ninguno
        }
        println("locale: $locale")
        if (currentLocale.toString().contains(locale.toString())) return

        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        findNavController().navigate(R.id.nav_home)
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