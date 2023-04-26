package es.ukanda.playroll.ui.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import es.ukanda.playroll.R
import es.ukanda.playroll.databinding.FragmentResultCameraBinding
import es.ukanda.playroll.ui.ViewModel.CameraViewModel

class ResultCameraFragment : Fragment() {
    private var _binding: FragmentResultCameraBinding? = null
    private val binding get() = _binding!!
    lateinit var personaje: Map<String,String>
    lateinit var texto: String
    lateinit var imagen: Bitmap


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResultCameraBinding.inflate(inflater, container, false)

        personaje = arguments?.getSerializable("personaje") as Map<String,String>
        imagen = BitmapFactory.decodeByteArray(arguments?.getByteArray("image"), 0, arguments?.getByteArray("image")!!.size)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val imagenRotada = rotateBitmap(imagen,90f)
        super.onViewCreated(view, savedInstanceState)
        val with = imagenRotada.width
        val height = (imagenRotada.height as Int) / 5
        binding.tvNombreResult.text = personaje.getValue("Nombre")
        binding.tvDescripcionResult.text = personaje.getValue("Descripcion")
        binding.imageView.setImageBitmap(imagen.let { it })
        //binding.imageView.setImageBitmap(Bitmap.createBitmap(imagenRotada, 0, 0, with!!, height))
    }
    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }


}
