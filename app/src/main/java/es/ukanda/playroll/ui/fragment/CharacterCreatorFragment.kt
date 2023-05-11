package es.ukanda.playroll.ui.fragment

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CursorAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import es.ukanda.playroll.controllers.BackgroundController
import es.ukanda.playroll.controllers.ClasesController
import es.ukanda.playroll.controllers.RaceController
import es.ukanda.playroll.customExceptions.CustomException
import es.ukanda.playroll.database.db.PartyDb
import es.ukanda.playroll.databinding.FormCreateCharacter1Binding
import es.ukanda.playroll.databinding.FormCreateCharacter2Binding
import es.ukanda.playroll.databinding.FragmentCharacterCreatorBinding
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CharacterCreatorFragment : Fragment() {
    private var _binding: FragmentCharacterCreatorBinding? = null
    private val binding get() = _binding!!

    private var character = CharacterEntity()
    private lateinit var includeBinding: FormCreateCharacter2Binding

    //controllers
    private lateinit var raceController:RaceController
    private lateinit var clasesController: ClasesController
    private lateinit var backgroundController: BackgroundController


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
    }

    private fun spinners() {
        val context = this.requireContext()
        CoroutineScope(Dispatchers.IO).launch {
            val raceList = raceController.getAllRaces()
            val clasesList = clasesController.getAllClases()
            val backgroundList = backgroundController.getAllBackgrounds()
            val alignmentList = CharacterEntity.typeAlignment

            val adapterRace = ArrayAdapter(context, R.layout.simple_spinner_item, raceList.map{it.raceName})
            val adapterClases = ArrayAdapter(context, R.layout.simple_spinner_item, clasesList)
            val adapterBackground = ArrayAdapter(context, R.layout.simple_spinner_item, backgroundList)
            val adapterAlignment = ArrayAdapter(context, R.layout.simple_spinner_item, alignmentList.map { it.value })

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
        binding.btCreateCharacterNext.setOnClickListener {
            try{
            validateform1()
            includeBinding = FormCreateCharacter2Binding.inflate(layoutInflater)
            binding.formCreateCharacter.root.removeAllViews()
            binding.formCreateCharacter.root.addView(includeBinding.root)
            binding.btCreateCharacterNext.visibility = View.GONE
            binding.btCreateCharacter.visibility = View.VISIBLE
            }catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btCreateCharacter.setOnClickListener {
            validateform2(includeBinding)
            addCharacter()
            findNavController().navigate(es.ukanda.playroll.R.id.action_nav_CharacterCreator_to_nav_home)//cambiar por info
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
            val experiencia = binding.formCreateCharacter.etExperiencia.text.toString()

            if (name.isEmpty() || description.isEmpty() || nivel.isEmpty() || experiencia.isEmpty()) {
                throw CustomException("Rellena todos los campos")
                return
            }
            if (binding.formCreateCharacter.spRazas.selectedItemPosition == 0) {
                throw CustomException("Selecciona una raza")
            }
            if (binding.formCreateCharacter.spClases.selectedItemPosition == 0) {
                throw CustomException("Selecciona una clase")
            }
            if (binding.formCreateCharacter.spAlineamiento.selectedItemPosition == 0) {
                throw CustomException("Selecciona un alineamiento")
            }
            if (nivel.toInt() < 1 || nivel.toInt() > 20) {
                throw CustomException("El nivel debe estar entre 1 y 20")
            }
            if (experiencia.toInt() < 0 || experiencia.toInt() > 355000) {
                throw CustomException("La experiencia debe estar entre 0 y 355000")
            }
            character.name = name
            character.description = description
            character.race = raza
            character.clase = clase
            character.alignment = alineamiento.toInt()
            character.level = nivel.toInt()
            character.experience = experiencia.toInt()


        }catch (e: CustomException){
            throw e
        }
        catch (e: Exception){
            throw CustomException("Error al validar datos")
        }
    }
    private fun validateform2(binding: FormCreateCharacter2Binding) {
        val fuerza = binding.etFuerza.text.toString()
        val destreza = binding.etDestreza.text.toString()
        val constitucion = binding.etConstitucion.text.toString()
        val inteligencia = binding.etInteligencia.text.toString()
        val sabiduria = binding.etSabiduria.text.toString()
        val carisma = binding.etCarisma.text.toString()
        val salvacion = binding.spTiradasSalvacion.selectedItem.toString()
        val habilidad = binding.spSkills.selectedItem.toString()
        if (fuerza.isEmpty() || destreza.isEmpty() || constitucion.isEmpty() || inteligencia.isEmpty() || sabiduria.isEmpty() || carisma.isEmpty()) {
            throw CustomException("Rellena todos los campos")
        }
        if (salvacion.isEmpty() || habilidad.isEmpty()) {
            throw CustomException("Selecciona una tirada de salvación y una habilidad")
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
        val skills = listOf<String>(
            habilidad
        )
        character.skills = skills
        val savingThrows = listOf<String>(
            salvacion
        )
        character.salvaciones = savingThrows

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


}