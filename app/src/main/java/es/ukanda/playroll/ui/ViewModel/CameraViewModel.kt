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

    private suspend fun  procesarCabeceraDer() {
        try {
            var clase = ""
            var level = ""
            var transfondo = ""
            var playerName = ""
            var race = ""
            var alineamiento = ""
            var exp = ""
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

        }catch (e : Exception){
            e.printStackTrace()
        }
    }

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
}