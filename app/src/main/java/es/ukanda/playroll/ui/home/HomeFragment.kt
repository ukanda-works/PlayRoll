package es.ukanda.playroll.ui.home

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.internal.safeparcel.SafeParcelReader.createBundle
import com.google.firebase.auth.FirebaseAuth
import es.ukanda.playroll.R
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.databinding.FragmentHomeBinding
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.entyties.PartieEntities.Party
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
        animatedBackground()
        spiners()
        buttons()
    }

    private fun animatedBackground() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val animator1y = ObjectAnimator.ofFloat(binding.homeAnimatedCloud2, "translationY", 0f, 100f)
        val animator1 = ObjectAnimator.ofFloat(binding.homeAnimatedCloud2, "translationX", 0f, screenWidth*2)
        animator1y.duration = 40000
        animator1.duration = 40000
        animator1y.repeatCount = ObjectAnimator.INFINITE
        animator1.repeatCount = ObjectAnimator.INFINITE
        animator1y.repeatMode = ObjectAnimator.REVERSE
        animator1.repeatMode = ObjectAnimator.REVERSE
        animator1y.start()
        animator1.start()
        val animator2 = ObjectAnimator.ofFloat(binding.homeAnimatedCloud1, "translationX", 0f, -(screenWidth*2))
        animator2.duration = 25000
        animator2.repeatCount = ObjectAnimator.INFINITE
        animator2.repeatMode = ObjectAnimator.REVERSE
        animator2.start()
    }

    private fun buttons() {
        binding.btCrearPartida.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_PartyCreator)
        }
        binding.btPrueba.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_playParty)
        }
        binding.btAddCharacter.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_CharacterCreator)
        }
        binding.btJoinParty.setOnClickListener{
            var bundle = Bundle()
            CoroutineScope(Dispatchers.IO).launch {
               bundle = loadBundle()
            }
            findNavController().navigate(R.id.action_nav_home_to_nav_JoinParty, bundle)
        }
    }

    private fun loadBundle(): Bundle {
        val partyId = 1
        val bundle = Bundle()
        //id partida
        bundle.putInt("partyId", partyId)
        //ip del servidor 127.0.0.1
        bundle.putString("ip", "127.0.0.1")

        return bundle
    }

    private fun spiners() {
        lifecycleScope.launch(Dispatchers.IO) {
            //se inicializan los spinners con los datos de la base de datos
            val database = PartyDb.getDatabase(context!!)
            var partyList = database.partyDao().getAllParties()
            var characterList =
                database.characterDao().getAllCharacters()

                val party = Party(0, "No hay partidas","")
                partyList.add(0, party)
            if(characterList.isEmpty()){
                val character = CharacterEntity()
                characterList += character
            }
            //se crean los adaptadores
            val adapterParty =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, partyList.map { it.partyName })
            val adapterCharacter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, characterList.map { it.name})

            adapterParty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapterCharacter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            //se asignan los adaptadores a los spinners
            binding.spParties.adapter = adapterParty
            binding.spCharacters.adapter = adapterCharacter
            binding.spParties.setSelection(0)
            binding.spCharacters.setSelection(0)

            //seteamos el listener de los spinners
            //navegamos a la pantalla de la partida o personaje seleccionado enviando id


            binding.spParties.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, id: Long) {
                val party = partyList[position]
                    val bundle = Bundle()
                    bundle.putInt("id", party.partyID)
                    if(party.partyID != 0){
                        findNavController().navigate(R.id.action_nav_home_to_nav_PartyManager, bundle)
                    }
                }
                override fun onNothingSelected(adapterView: AdapterView<*>) {
                    // Nothing to do here
                }
            }

            /*binding.spCharacters.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val character = characterList[position]
                    val bundle = Bundle()
                    bundle.putInt("id", character.characterID)
                    if(character.characterID != 0){
                        findNavController().navigate(R.id.action_nav_home_to_nav_CharacterInfo, bundle)
                    }else{
                        Toast.makeText(context, "No hay personajes", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }*/


        }
    }

    override fun onResume() {
        super.onResume()
        if (!isFirebaseUserLoggedIn()) {
            Toast.makeText(context, "Necesitas iniciar sesion", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_nav_home_to_nav_login)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun isFirebaseUserLoggedIn(): Boolean {
        val user = FirebaseAuth.getInstance().currentUser
        return user != null
    }


}