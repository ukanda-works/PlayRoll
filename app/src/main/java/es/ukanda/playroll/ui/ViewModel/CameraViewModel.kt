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

    private val _foundText = MutableLiveData<Map<String,String>>()
    val foundText: LiveData<Map<String,String>> get() = _foundText

    private val _info = MutableLiveData<String>()
    val info: LiveData<String> get() = _info

    private var valores = mutableListOf<Pair<String,String>>(    )

    init{
        _procesed.value = false
        valores = mutableListOf(
            "Clase" to "nada",
            "Nivel" to "",
            "Transfondo" to "",
            "Nombre" to "",
            "Raza" to "",
            "Alineamiento" to "",
            "Experiencia" to "",
            "Descripcion" to "nada",
        )
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
                _foundText.value = valores.toMap()

                _procesed.value = true
            }catch (e : Exception){
                e.printStackTrace()
            }
        }
    }

    /**
     * Esta se utilizara para reconocer la clase, el nivel, transfondp, raza y alineamiento
     */
    private suspend fun  procesarCabeceraDer() {
        try {
            var clase = ""
            var level = ""
            var transfondo = ""
            var race = ""
            var alineamiento = ""

            var textoTotal = ""

            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(getCabeceraDer(), 90)
            val result = recognizer.process(image).await()
            for (bloks in result.textBlocks){
                for (line in bloks.lines){
                    for (element in line.elements){
                        textoTotal += element.text + " "
                    }
                }
            }
            valores.add("Descripcion" to textoTotal)

            //una vez reconocido se compara con los valores de la base de datos
            //TODO: hacer la comparacion con la base de datos
            clase = findMostSimilarElement(listOf("Barbaro","Bardo","Clerigo","Druida","Guerrero","Mago","Monje","Paladin","Picaro","Hechicero","Brujo"),clase)!!
            valores.add("clase" to clase)
            transfondo = findMostSimilarElement(listOf("Acolito","Criminal","Artista","Erudito","Heroe del Pueblo","Marinero","Soldado","Urbano"),transfondo)!!
            valores.add("transfondo" to transfondo)
            //race = findMostSimilarElement(listOf())
            valores.add("race" to race)
            alineamiento = findMostSimilarElement(listOf("Legal bueno","Neutral bueno","Caotico bueno","Legal neutral","Neutral","Caotico neutral","Legal malo","Neutral malo","Caotico malo"),alineamiento)!!
            valores.add("alineamiento" to alineamiento)
            valores.add("level" to level)
            //y se asigna el valor correspondiente

        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    /**
     * Esta se utilizara para reconocer fuerza, destreza, constitucion, inteligencia, sabiduria y carisma
     */
    private suspend fun procesarEstadisticas(){

    }

    /**
     * Esta se utilizara para reconocer el nombre del personaje
     */
    private suspend fun procesarCabeceraIzq(){
            try {
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val image = InputImage.fromBitmap(getCabeceraIzq(), 90)
                val result = recognizer.process(image).await()
                var orderedList = result.textBlocks.sortedBy { it.cornerPoints?.get(0)?.x ?: 0 }
                orderedList = orderedList.take(2)
                orderedList.sortedByDescending { it.cornerPoints?.get(0)?.y ?: 0 }
                val nameBlock = orderedList.firstOrNull()
                val nombre = nameBlock?.text ?: ""
                valores.add("Nombre" to nombre)
                _info.value = "Nombre: ${nombre}"
            }catch (e : Exception){
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
        return Bitmap.createBitmap(image, 0, 0, with!!, height)
    }

    fun getCabeceraDer(): Bitmap{
        val image = rotateBitmap(picture.value!!, 90f)
        val with = image.width*2/3
        val height = (image.height) / 5
        return Bitmap.createBitmap(image, image.width - with, 0, with, height)
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