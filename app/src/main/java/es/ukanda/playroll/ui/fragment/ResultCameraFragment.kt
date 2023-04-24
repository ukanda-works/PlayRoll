package es.ukanda.playroll.ui.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        texto = arguments?.getString("texto") as String
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvNombreResult.text = texto?: "No se ha encontrado texto"
        binding.tvDescripcionResult.text = personaje.getValue("descipcion")
        binding.imageView.setImageBitmap(imagen.let { it })
    }


}
