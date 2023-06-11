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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.streams.toList

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
    /**
    Animación de fondo animado.
    Este método configura una animación de fondo animado utilizando propiedades de animación de desplazamiento
    para dos vistas de nube en la pantalla. Realiza las siguientes acciones:
    Obtiene las dimensiones de la pantalla en píxeles utilizando los DisplayMetrics.
    Calcula el ancho de la pantalla como un valor de coma flotante.
    Crea un animador de desplazamiento en el eje Y para la segunda nube, desplazándola hacia arriba y hacia abajo.
    La duración de la animación es de 40 segundos.
    El número de repeticiones es infinito.
    El modo de repetición es de reversa, lo que hace que la nube se mueva hacia arriba y hacia abajo de manera continua.
    Crea un animador de desplazamiento en el eje X para la segunda nube, desplazándola desde la izquierda de la pantalla hasta más allá del ancho de la pantalla.
    La duración de la animación es de 40 segundos.
    El número de repeticiones es infinito.
    El modo de repetición es de reversa, lo que hace que la nube se mueva hacia la derecha y hacia la izquierda de manera continua.
    Inicia ambos animadores.
    Crea un animador de desplazamiento en el eje X para la primera nube, desplazándola desde la derecha de la pantalla hasta más allá del ancho de la pantalla hacia la izquierda.
    La duración de la animación es de 25 segundos.
    El número de repeticiones es infinito.
    El modo de repetición es de reversa, lo que hace que la nube se mueva hacia la izquierda y hacia la derecha de manera continua.
    Inicia el animador para la primera nube.
    Nota: Este método asume la disponibilidad de las vistas de nube enlazadas en el archivo de diseño correspondiente.
     */
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
    /**
    Configuración de botones.
    Este método configura los botones de la vista principal del hogar. Realiza las siguientes acciones:
    Configura un listener de clic para el botón "Crear Partida" que navega hacia la pantalla de creación de partida.
    Configura un listener de clic para el botón "Agregar Personaje" que navega hacia la pantalla de creación de personajes.
    Configura un listener de clic para el botón "Unirse a una Partida" que navega hacia la pantalla de unión a partida.
    Nota: Este método asume la disponibilidad de los botones correspondientes en el archivo de diseño vinculado.
     */
    private fun buttons() {
        binding.btCrearPartida.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_PartyCreator)
        }
        binding.btAddCharacter.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_CharacterCreator)
        }
        binding.btJoinParty.setOnClickListener{
            findNavController().navigate(R.id.action_nav_home_to_nav_JoinParty)
        }
    }
    /**
    Cargar Bundle.
    Este método carga un objeto Bundle con los datos necesarios para inicializar una pantalla.
    Realiza las siguientes acciones:
    Asigna un ID de partido (partyId) con el valor 7.
    Asigna un indicador de maestro (isMaster) con el valor false.
    Asigna una dirección IP del servidor (ipServer) con el valor "127.0.0.1".
    Devuelve el objeto Bundle con los datos cargados.
    Nota: Este método asume que se utilizará en el contexto adecuado y los valores están establecidos estáticamente.
    Se puede ajustar según sea necesario para cargar los datos dinámicamente.
     */
    private fun loadBundle(): Bundle {
        val partyId = 7
        val bundle = Bundle()
        bundle.putInt("party", partyId)
        bundle.putBoolean("isMaster", false)
        bundle.putString("ipServer", "127.0.0.1")
        return bundle
    }
    /**
    Configurar Spinners.
    Este método configura los spinners en la pantalla. Realiza las siguientes acciones:
    Obtiene los datos necesarios de la base de datos.
    Crea adaptadores de ArrayAdapter con los datos obtenidos.
    Asigna los adaptadores a los spinners correspondientes.
    Configura el evento de selección para el spinner de partidos, que navega a la pantalla de administración del partido seleccionado.
    Nota: Este método asume que se utilizará en el contexto adecuado y que se tiene acceso a la base de datos.
    Se puede ajustar según sea necesario para obtener los datos de manera dinámica.
     */
    private fun spiners() {
        lifecycleScope.launch(Dispatchers.IO) {
            //se inicializan los spinners con los datos de la base de datos
            val database = PartyDb.getDatabase(context!!)
            var partyList = database.partyDao().getAllParties().stream().filter {it.own}.toList() as MutableList
            var characterList =database.characterDao().getAllCharacters().stream().filter {it.own}.toList() as MutableList

                val party = Party(0, getString(R.string.no_parties),"")
                partyList.add(0, party)
            if(characterList.isEmpty()){
                val character = CharacterEntity()
                characterList += character
            }
            withContext(Dispatchers.Main) {
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
        }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isFirebaseUserLoggedIn()) {
            Toast.makeText(context, getString(R.string.do_you_need_sing_in), Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_nav_home_to_nav_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    /**
    Verificar si el usuario está conectado a Firebase.
    Este método verifica si el usuario está conectado a Firebase. Retorna un valor booleano que indica si el usuario está o no conectado.
    @return Booleano que indica si el usuario está conectado a Firebase.
     */
    fun isFirebaseUserLoggedIn(): Boolean {
        val user = FirebaseAuth.getInstance().currentUser
        return user != null
    }
}