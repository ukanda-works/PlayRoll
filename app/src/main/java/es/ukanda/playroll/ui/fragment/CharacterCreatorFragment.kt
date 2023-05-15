package es.ukanda.playroll.ui.fragment

import android.R
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import es.ukanda.playroll.controllers.sysmtem.BackgroundController
import es.ukanda.playroll.controllers.sysmtem.ClasesController
import es.ukanda.playroll.controllers.sysmtem.RaceController
import es.ukanda.playroll.customExceptions.CustomException
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.databinding.FragmentCharacterCreatorBinding
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import es.ukanda.playroll.entyties.SystemClases.Race
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CharacterCreatorFragment : Fragment() {
    private var _binding: FragmentCharacterCreatorBinding? = null
    private val binding get() = _binding!!

    private var character = CharacterEntity()

    //controllers
    private lateinit var raceController: RaceController
    private lateinit var clasesController: ClasesController
    private lateinit var backgroundController: BackgroundController

    //listas
    private lateinit var raceList :List<Race>
    private lateinit var clasesList: List<String>
    private lateinit var backgroundList : List<String>
    private var alignmentList = listOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharacterCreatorBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        raceController = RaceController(requireContext())
        clasesController = ClasesController(requireContext())
        backgroundController = BackgroundController(requireContext())
        buttons()
        spinners()
        if(arguments?.getString("from").equals("camera")){
            Toast.makeText(requireContext(), "camara", Toast.LENGTH_SHORT).show()
            fromCamara()
        }else if(arguments?.getString("from").equals("edit")){
            Toast.makeText(requireContext(), "edit", Toast.LENGTH_SHORT).show()
            //editCharacter()
        }else{
            Toast.makeText(requireContext(), "new", Toast.LENGTH_SHORT).show()
        }
    }

    private fun spinners() {
        val context = this.requireContext()
        CoroutineScope(Dispatchers.IO).launch {
            raceList = raceController.getAllRaces()
            clasesList = clasesController.getAllClases()
            backgroundList = backgroundController.getAllBackgrounds()
            CharacterEntity.typeAlignment.forEach{  alignmentList = alignmentList.plus(it.value)}

            val adapterRace = ArrayAdapter(context, R.layout.simple_spinner_item, raceList.map{it.raceName})
            val adapterClases = ArrayAdapter(context, R.layout.simple_spinner_item, clasesList)
            val adapterBackground = ArrayAdapter(context, R.layout.simple_spinner_item, backgroundList)
            val adapterAlignment = ArrayAdapter(context, R.layout.simple_spinner_item, alignmentList)

            adapterRace.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapterClases.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapterBackground.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapterAlignment.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            binding.formCreateCharacter.spRazas.adapter = adapterRace
            binding.formCreateCharacter.spTransfondo.adapter = adapterBackground
            binding.formCreateCharacter.spClases.adapter = adapterClases
            binding.formCreateCharacter.spAlineamiento.adapter = adapterAlignment
        }

    }

    private fun buttons() {

        binding.btCreateCharacter.setOnClickListener {
            try{
                validateform1()
                addCharacter()
                findNavController().navigate(es.ukanda.playroll.R.id.action_nav_CharacterCreator_to_nav_home)//cambiar por info

            }catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        binding.btnCamera.setOnClickListener {
            findNavController().navigate(es.ukanda.playroll.R.id.action_nav_CharacterCreator_to_nav_camera)
        }

    }

    private fun validateform1() {
        try{
            val name = binding.formCreateCharacter.etName.text.toString()
            val description = binding.formCreateCharacter.etDescripcion.text.toString()
            val raza = binding.formCreateCharacter.spRazas.selectedItem.toString()
            val clase = binding.formCreateCharacter.spClases.selectedItem.toString()
            val alineamiento = binding.formCreateCharacter.spAlineamiento.selectedItem.toString()
            val nivel = binding.formCreateCharacter.etNivel.text.toString()
            val fuerza = binding.formCreateCharacter.etFuerza2.text.toString()
            val destreza = binding.formCreateCharacter.etDestreza2.text.toString()
            val constitucion = binding.formCreateCharacter.etConstitucion2.text.toString()
            val inteligencia = binding.formCreateCharacter.etInteligencia2.text.toString()
            val sabiduria = binding.formCreateCharacter.etSabiduria2.text.toString()
            val carisma = binding.formCreateCharacter.etCarisma2.text.toString()
            //val salvacion = binding.formCreateCharacter.spTiradasSalvacion2.selectedItem.toString()
            //val habilidad = binding.formCreateCharacter.spSkills2.selectedItem.toString()

            if (nivel.toInt() < 1 || nivel.toInt() > 20) {
                throw CustomException("El nivel debe estar entre 1 y 20")
            }

            character.name = name
            character.description = description
            character.race = raza
            character.clase = clase
            character.alignment = CharacterEntity.getAlignment(alineamiento)
            character.level = nivel.toInt()

            if (fuerza.isEmpty() || destreza.isEmpty() || constitucion.isEmpty() || inteligencia.isEmpty() || sabiduria.isEmpty() || carisma.isEmpty()) {
                throw CustomException("Rellena todos los campos")
            }

            if (fuerza.toInt() < 1 || fuerza.toInt() > 20) {
                throw CustomException("La fuerza debe estar entre 1 y 20")
            }
            if (destreza.toInt() < 1 || destreza.toInt() > 20) {
                throw CustomException("La destreza debe estar entre 1 y 20")
            }
            if (constitucion.toInt() < 1 || constitucion.toInt() > 20) {
                throw CustomException("La constitucion debe estar entre 1 y 20")
            }
            if (inteligencia.toInt() < 1 || inteligencia.toInt() > 20) {
                throw CustomException("La inteligencia debe estar entre 1 y 20")
            }
            if (sabiduria.toInt() < 1 || sabiduria.toInt() > 20) {
                throw CustomException("La sabiduria debe estar entre 1 y 20")
            }
            if (carisma.toInt() < 1 || carisma.toInt() > 20) {
                throw CustomException("El carisma debe estar entre 1 y 20")
            }
            val stats = HashMap<String, Int>(
                mapOf(
                    "fuerza" to fuerza.toInt(),
                    "destreza" to destreza.toInt(),
                    "constitucion" to constitucion.toInt(),
                    "inteligencia" to inteligencia.toInt(),
                    "sabiduria" to sabiduria.toInt(),
                    "carisma" to carisma.toInt()
                )
            )
            character.statistics = stats
            /*val skills = listOf<String>(
                habilidad
            )
            character.skills = skills
            val savingThrows = listOf<String>(
                salvacion
            )
            character.salvaciones = savingThrows*/

        }catch (e: CustomException){
            throw e
        }
        catch (e: Exception){
            e.printStackTrace()
            throw CustomException("Error al validar datos")
        }
    }

    private fun addCharacter() {
        try{
            //se añade desde una corrutina
            CoroutineScope(Dispatchers.IO).launch {
                PartyDb.getDatabase(requireContext()).characterDao().insertCharacter(character)
                withContext(Dispatchers.Main){
                    Toast.makeText(context, "Personaje creado", Toast.LENGTH_SHORT).show()
                }
            }
        }catch (e: Exception){
            println("Error al crear el personaje")
            println(e)
            Toast.makeText(context, "Error al crear el personaje", Toast.LENGTH_SHORT).show()
        }
    }


    private fun editCharacter() {
        //TODO("Not yet implemented")
    }

    private fun fromCamara() {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("Advertencia")
        alertDialogBuilder.setMessage("Puede que la informacion detectada no sea correcta, por favor revisela")
        alertDialogBuilder.setPositiveButton("Aceptar") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

        val personaje = arguments?.getSerializable("personaje") as List<Pair<String,String>>
        val bundle = mutableMapOf<String, String>()
        for (pair in personaje) {
            bundle[pair.first] = pair.second
        }
/*
        val statistics = HashMap<String, Int>(
            mapOf(
                "fuerza" to bundle.getOrDefault("fuerza", 0).toString().toInt(),
                "destreza" to bundle.getOrDefault("destreza", 0).toString().toInt(),
                "constitucion" to bundle.getOrDefault("constitucion", 0).toString().toInt(),
                "inteligencia" to bundle.getOrDefault("inteligencia", 0).toString().toInt(),
                "sabiduria" to bundle.getOrDefault("sabiduria", 0).toString().toInt(),
                "carisma" to bundle.getOrDefault("carisma", 0).toString().toInt()
            )
        )*/

       /* character.name = bundle.getOrDefault("name", "")
        character.statistics = statistics
        var level = bundle.getOrDefault("level", 0).toString()
        if (level.equals("")){
            level = "0"
        }

        character.level = level.toInt()
        character.clase = bundle.getOrDefault("clase", "")

        character.alignment = bundle.getOrDefault("alignment", "5").toInt()
        character.race = bundle.getOrDefault("race", "")
        character.background = bundle.getOrDefault("background", "")*/

        println("---------------------")
        bundle.forEach() { (key, value) ->
            println("$key = $value")
            when(key){
                "name" -> character.name = value
                "level" -> character.level = value.toInt()
                "clase" -> character.clase = value
                "race" -> character.race = value
                "background" -> character.background = value
                "alignment" -> character.alignment = value.toInt()

                "fuerza" -> character.statistics["fuerza"] = value.toInt()
                "destreza" -> character.statistics["destreza"] = value.toInt()
                "constitucion" -> character.statistics["constitucion"] = value.toInt()
                "inteligencia" -> character.statistics["inteligencia"] = value.toInt()
                "sabiduria" -> character.statistics["sabiduria"] = value.toInt()
                "carisma" -> character.statistics["carisma"] = value.toInt()
            }

        }
        println("---------------------")
        println(character)

        rellenarFormulario()
    }

    /**
     * Este metodo tomara la variable character y rellenara el formulario con sus datos
     */
    private fun rellenarFormulario() {
        binding.formCreateCharacter.etName.setText(character.name)

        binding.formCreateCharacter.etDescripcion.setText(character.description)
        binding.formCreateCharacter.etNivel.setText(character.level.toString())

        //spiners
        raceList.forEach{
            val race = it.raceName.toLowerCase()
            if(race.equals(character.race)){
                binding.formCreateCharacter.spRazas.setSelection(raceList.indexOf(it))
            }
        }

        clasesList.forEach{
            val clase = it.toLowerCase()
            if(clase.equals(character.clase)){
                binding.formCreateCharacter.spClases.setSelection(clasesList.indexOf(it))
            }
        }

        backgroundList.forEach{
            val background = it.toLowerCase()
            if(background.equals(character.background)){
                binding.formCreateCharacter.spTransfondo.setSelection(backgroundList.indexOf(it))
            }
        }

        alignmentList.forEach{
            if(it.equals(CharacterEntity.getAlignment(character.alignment))){
                binding.formCreateCharacter.spAlineamiento.setSelection(alignmentList.indexOf(it))
            }
        }

        //stats
        binding.formCreateCharacter.etFuerza2.setText(character.statistics["fuerza"].toString())
        binding.formCreateCharacter.etDestreza2.setText(character.statistics["destreza"].toString())
        binding.formCreateCharacter.etConstitucion2.setText(character.statistics["constitucion"].toString())
        binding.formCreateCharacter.etInteligencia2.setText(character.statistics["inteligencia"].toString())
        binding.formCreateCharacter.etSabiduria2.setText(character.statistics["sabiduria"].toString())
        binding.formCreateCharacter.etCarisma2.setText(character.statistics["carisma"].toString())


    }

}