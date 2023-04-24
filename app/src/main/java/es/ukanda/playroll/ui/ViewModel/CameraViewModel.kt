package es.ukanda.playroll.ui.ViewModel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(picture.value!!, 90)
            val result = recognizer.process(image).await()
            _textoPrueva.value = result.text
            _foundText.value = mapOf("nombre" to "prueva", "descipcion" to "prueva")
            _procesed.value = true
            println(result.text)
        }catch (e: Exception){
            e.printStackTrace()
            _textoPrueva.value = "Error: ${e.message}"
        }
        }
    }

    fun getValue(key : String): String?{
        return foundText.value?.get(key)
    }

    fun getByteArray(): ByteArray?{
         val stream = ByteArrayOutputStream()
        picture.value?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}