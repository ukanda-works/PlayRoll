package es.ukanda.playroll.ui.ViewModel

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class CameraViewModel: ViewModel() {
    private val _procesed = MutableLiveData<Boolean>()
    val procesed: LiveData<Boolean> get() = _procesed

    private val _picture = MutableLiveData<Bitmap>()
    val picture: LiveData<Bitmap> get() = _picture

    private  val _textoPrueva = MutableLiveData<String>()
    val textoPrueva: LiveData<String> get() = _textoPrueva

    private val _foundText = MutableLiveData<MutableMap<String,String>>()
    val foundText: LiveData<Map<String,String>> get() = _foundText as LiveData<Map<String,String>>

    val foundAll = mutableListOf<Pair<String,String>>()

    private val _info = MutableLiveData<String>()
    val info: LiveData<String> get() = _info


    var cabeceraIzq = MutableLiveData<Bitmap>()

    init{
        _procesed.value = false
    }

    fun setPicture(bitmap: Bitmap){
        _picture.value = bitmap
        processPicture()
    }

    private fun processPicture(){
        viewModelScope.launch {
            try {
                procesarCabecera()

            }catch (e: Exception){
                e.printStackTrace()
                _textoPrueva.value = "Error: ${e.message}"
            }
        }
    }

    private fun procesarCabecera(){
        viewModelScope.launch {
            try {
                procesarCabeceraIzq()
                procesarCabeceraDer()
                procesarEstadisticas()
                _picture.value = cabeceraIzq.value
                checkValues()
                _procesed.value = true
            }catch (e : Exception){
                e.printStackTrace()
            }
        }
    }

    fun getFoundText(): List<Pair<String,String>>{
        checkValues()
        return foundAll
    }

    private fun checkValues() {
        val map = foundAll.toMap()
        if (map?.get("name") == null || map?.get("name").equals("")) {
            _foundText.value?.set("name", "none")
            foundAll.add(Pair("name", "none"))
        }

        if(map?.get("clase") == null){
           _foundText.value?.set("clase", "guerrero")
            foundAll.add(Pair("clase", "guerrero"))
        }
        if(map?.get("level") == null|| map?.get("level").equals("")|| map?.get("level")?.toIntOrNull() == null){
            _foundText.value?.set("level", "1")
            foundAll.add(Pair("level", "1"))
        }
        if(map?.get("raza") == null){
            _foundText.value?.set("raza", "enano")
            foundAll.add(Pair("raza", "enano"))
        }
        if(map?.get("alineamiento") == null){
            _foundText.value?.set("alineamiento", "neutral")
            foundAll.add(Pair("alineamiento", "neutral"))
        }
        if(map?.get("background") == null){
            _foundText.value?.set("background", "criminal")
            foundAll.add(Pair("background", "criminal"))
        }
        if(map?.get("fuerza") == null || map?.get("fuerza").equals("")|| map?.get("fuerza")?.toIntOrNull() == null){
            _foundText.value?.set("fuerza", "10")
            foundAll.add(Pair("fuerza", "10"))
        }
        if(map?.get("destreza") == null || map?.get("destreza").equals("")|| map?.get("destreza")?.toIntOrNull() == null){
            _foundText.value?.set("destreza", "10")
            foundAll.add(Pair("destreza", "10"))
        }
        if(map?.get("constitucion") == null || map?.get("constitucion").equals("")|| map?.get("constitucion")?.toIntOrNull() == null){
            _foundText.value?.set("constitucion", "10")
            foundAll.add(Pair("constitucion", "10"))
        }
        if(map?.get("inteligencia") == null || map?.get("inteligencia").equals("")|| map?.get("inteligencia")?.toIntOrNull() == null){
            _foundText.value?.set("inteligencia", "10")
            foundAll.add(Pair("inteligencia", "10"))
        }
        if(map?.get("sabiduria") == null || map?.get("sabiduria").equals("")|| map?.get("sabiduria")?.toIntOrNull() == null){
            _foundText.value?.set("sabiduria", "10")
            foundAll.add(Pair("sabiduria", "10"))
        }
        if(map?.get("carisma") == null || map?.get("carisma").equals("")|| map?.get("carisma")?.toIntOrNull() == null){
            _foundText.value?.set("carisma", "10")
            foundAll.add(Pair("carisma", "10"))
        }
    }

    /**
     * Esta se utilizara para reconocer la clase, el nivel, background, raza y alineamiento
     */
    private suspend fun  procesarCabeceraDer() {
        try {
            var clase = ""
            var level = ""
            var background = ""
            var race = ""
            var alineamiento = ""
            var textoTotal = ""

            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(getCabeceraDer(), 90)
            val result = recognizer.process(image).await()

            val linesToProcess = result.textBlocks
                .flatMap { it.lines }
                .filter { it.boundingBox?.top ?: 0 > 0 }

            for (i in 0 until linesToProcess.size) {
                val line = linesToProcess[i]
                val text = line.elements.joinToString(" ") { it.text } // Unir los elementos de la línea en un solo texto

                val altura = line.boundingBox?.top ?: 0 // Obtener la altura de la línea

                when(i){
                    0 -> {
                        clase = text.split(" ")[0]
                        level = text.split(" ")[1]?: ""
                        textoTotal += "n: $i altura = $altura: $text\n"
                    }
                    2 -> {
                        race = text
                        textoTotal += "n: $i altura = $altura: $text\n"
                    }
                    6 -> {
                        alineamiento = text
                        textoTotal += "n: $i altura = $altura: $text\n"
                    }
                    4 -> {
                        background = text
                        textoTotal += "n: $i altura = $altura: $text\n"
                    }
                }
            }

            clase = findMostSimilarElement(listOf("Barbaro","Bardo","Clerigo","Druida","Guerrero","Mago","Monje","Paladin","Picaro","Hechicero","Brujo"),clase)!!
            _foundText.value?.set("clase", clase)
            foundAll.add(Pair("clase", clase))
            background = findMostSimilarElement(listOf("Acolito","Criminal","Artista","Erudito","Heroe del Pueblo","Marinero","Soldado","Urbano"),background)!!
            _foundText.value?.set("background", background)
            foundAll.add(Pair("background", background))
            race = findMostSimilarElement(listOf(
                "enano",
                "elfo",
                "humano",
                "halfling",
                "gnomo",
                "mediano",
                "semiorco",
                "tiefling",
                "drow",
                "draconiano",
                "genasi",
                "aasimar",
                "firbolg",
                "goliath",
                "gith",
                "kobold",
                "hombre lagarto",
                "medusa",
                "minotauro",
                "orco",
                "troll"
            ),race)!!
            _foundText.value?.set("race", race)
            foundAll.add(Pair("race", race))
            alineamiento = findMostSimilarElement(listOf("Legal bueno","Neutral bueno","Caotico bueno","Legal neutral","Neutral","Caotico neutral","Legal malo","Neutral malo","Caotico malo"),alineamiento)!!
            _foundText.value?.set("alignment", listOf("Legal bueno","Neutral bueno","Caotico bueno","Legal neutral","Neutral","Caotico neutral","Legal malo","Neutral malo","Caotico malo").indexOf(alineamiento).toString())
            foundAll.add(Pair("alignment",  listOf("Legal bueno","Neutral bueno","Caotico bueno","Legal neutral","Neutral","Caotico neutral","Legal malo","Neutral malo","Caotico malo").indexOf(alineamiento).toString()))
            _foundText.value?.set("level", level)
            foundAll.add(Pair("level", level))
            //y se asigna el valor correspondiente

        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    /**
     * Esta se utilizara para reconocer fuerza, destreza, constitucion, inteligencia, sabiduria y carisma
     */
    private suspend fun procesarEstadisticas(){
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(getStadisticas(), 90)
        val result = recognizer.process(image).await()
        var textotal = ""
        val textBlocks = result.textBlocks
        val regex = Regex("\\d+")
        val numbersList = mutableListOf<String>()

        textBlocks.forEach { textBlock ->
                textBlock.lines.forEach { line ->
                var numbersInLine = listOf<String>()
                if ((line.text.contains("O") || line.text.contains("o"))&& line.text.length == 1){
                    numbersInLine += "0"
                }else{
                    numbersInLine += line.text
                }
                numbersInLine.forEach { numberOrO ->
                    val yPosition = textBlock.cornerPoints?.get(0)?.y ?: 0
                    numbersList.add(numberOrO)
                }
            }
        }

        val stadisticasOrdenadas = mutableListOf<Pair<String, Pair<String, String>>>()
        var i = 0

        while (i < numbersList.size) {
            var stat = ""
            var modificador = ""
            var skill = ""
            val valor = numbersList[i]
            if (valor.length > 4) {
                skill = valor
                if (numbersList.size > i+1) {
                    val next = numbersList[i+1]
                    if (next.length < 4) {
                        if (next.contains("+") || next == "0") {
                            modificador = next
                            if (numbersList.size > i+2 && numbersList[i+2].length < 4) {
                                stat = numbersList[i+2]
                            }
                        } else {
                            modificador = next
                        }
                    }
                }
                stadisticasOrdenadas.add(skill to (modificador to stat))
            }
            i++
        }

        stadisticasOrdenadas.forEach{ (clave, valor) ->
            textotal += "[$clave ${valor.first} ${valor.second}]"
        }
        if (stadisticasOrdenadas.isNotEmpty()) {
            val fuerza = stadisticasOrdenadas.getOrNull(0)?.second?.second ?: "0"
            _foundText.value?.set("fuerza", fuerza)
            foundAll.add(Pair("fuerza", fuerza))
            val destreza = stadisticasOrdenadas.getOrNull(1)?.second?.second ?: "0"
            _foundText.value?.set("destreza", destreza)
            foundAll.add(Pair("destreza", destreza))
            val constitucion = stadisticasOrdenadas.getOrNull(2)?.second?.second ?: "0"
            _foundText.value?.set("constitucion", constitucion)
            foundAll.add(Pair("constitucion", constitucion))
            val inteligencia = stadisticasOrdenadas.getOrNull(3)?.second?.second ?: "0"
            _foundText.value?.set("inteligencia", inteligencia)
            foundAll.add(Pair("inteligencia", inteligencia))
            val sabiduria = stadisticasOrdenadas.getOrNull(4)?.second?.second ?: "0"
            _foundText.value?.set("sabiduria", sabiduria)
            foundAll.add(Pair("sabiduria", sabiduria))
            val carisma = stadisticasOrdenadas.getOrNull(5)?.second?.second ?: "0"
            _foundText.value?.set("carisma", carisma)
            foundAll.add(Pair("carisma", carisma))
        }


    }

    /**
     * Esta se utilizara para reconocer el nombre del personaje
     */
    private suspend fun procesarCabeceraIzq(){
        try {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(getCabeceraIzq(), 90)
            val result = recognizer.process(image).await()

            val orderedList = result.textBlocks
                .sortedBy { it.cornerPoints?.get(0)?.y ?: 0 }
                .take(2)

            val nombreBlock = orderedList.getOrNull(1)
            val nombre = nombreBlock?.text ?: ""

            _foundText.value?.set("name", nombre)
            foundAll.add(Pair("name", nombre))
            _info.value = "name: $nombre"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getValue(key : String): String?{
        return foundText.value?.get(key)
    }

    fun getByteArray(): ByteArray?{
         val stream = ByteArrayOutputStream()
        rotateBitmap(picture.value!!,90f).compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun getCabeceraIzq(): Bitmap{
        val image = rotateBitmap(picture.value!!, 90f)
        val with = image.width/3
        val height = (image.height) / 5
        val result= Bitmap.createBitmap(image, 0, 0, with!!, height)
        cabeceraIzq.value = result
        return result
    }

    fun getCabeceraDer(): Bitmap{
        val image = rotateBitmap(picture.value!!, 90f)
        val with = image.width*3/5
        val height = (image.height) / 6
        val result = Bitmap.createBitmap(image, image.width - with, 0, with, height)
        cabeceraIzq.value = result
        return result
    }

    fun getStadisticas(): Bitmap{
        val image = rotateBitmap(picture.value!!, 90f)
        val with = image.width*1/4
        val initHeight = (image.height) / 7
        val height = initHeight * 3
        val result = Bitmap.createBitmap(image, 0, initHeight, with, (height*0.95).toInt())
        cabeceraIzq.value = result
        return result
    }


    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun findMostSimilarElement(list: List<String>, text: String): String? {
        var mostSimilarElement: String? = null
        var minDistance = Int.MAX_VALUE
        for (element in list) {
            val distance = LevenshteinDistance(element, text)
            if (distance < minDistance) {
                minDistance = distance
                mostSimilarElement = element
            }
        }
        return mostSimilarElement
    }

    fun LevenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) {
            dp[i][0] = i
        }

        for (j in 0..n) {
            dp[0][j] = j
        }

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }

        return dp[m][n]
    }
}