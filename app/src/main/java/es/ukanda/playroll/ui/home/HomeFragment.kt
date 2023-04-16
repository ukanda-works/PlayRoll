package es.ukanda.playroll.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import es.ukanda.playroll.R
import es.ukanda.playroll.databinding.FragmentHomeBinding
import es.ukanda.playroll.pruebas.Test_uno
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*binding.testButton.setOnClickListener{
            val test_uno = Test_uno(context!!)
            CoroutineScope(Dispatchers.IO).launch {
                test_uno.createRace()
                test_uno.raceDao.getAllRaces().forEach {
                    println(it.raceName)
                }

            }

        }*/



    }

    override fun onResume() {
        super.onResume()
        session()

    }

    fun session(){
        val pref = activity?.getSharedPreferences(getString(R.string.prefs_file), 0)
        val email = pref?.getString("email", null)
        val provider = pref?.getString("provider", null)

        if (email != null && provider != null) {
            Toast.makeText(context, "Sesion identificada, bienvenido ${email}", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(context, "Necesitas iniciar sesion", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_nav_home_to_nav_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}