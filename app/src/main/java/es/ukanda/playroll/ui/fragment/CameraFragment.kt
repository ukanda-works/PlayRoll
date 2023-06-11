package es.ukanda.playroll.ui.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.Toast
import android.hardware.Camera
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.common.InputImage
import es.ukanda.playroll.R
import es.ukanda.playroll.databinding.FragmentCameraBinding
import es.ukanda.playroll.ui.ViewModel.CameraViewModel
import java.io.File
import java.io.IOException



private val CAMERA_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class CameraFragment : Fragment(), SurfaceHolder.Callback, Camera.PictureCallback {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var camera: Camera? = null
    private lateinit var surfaceHolder: SurfaceHolder



    val cameraViewModel = CameraViewModel() //ViewModelProvider(this).get(CameraViewModel::class.java)
    val procesedObserver = Observer<Boolean> { isProcesed ->
        if (isProcesed) {
            Toast.makeText(context, "photo_processed", Toast.LENGTH_SHORT).show()
            val bundle = Bundle()
            bundle.putSerializable("personaje", cameraViewModel.getFoundText() as java.io.Serializable)
            bundle.putSerializable("from", "camera")
            findNavController().navigate(R.id.action_nav_camera_to_nav_CharacterCreator, bundle)
        }
    }
    val infoObserver = Observer<String> { info ->
        Toast.makeText(context, info, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)


        surfaceHolder = binding.surfaceView.holder
        surfaceHolder.addCallback(this)

        binding.btCamara.setOnClickListener {
            camera!!.startPreview()
            camera!!.autoFocus { success, camera ->
                if (success) {
                    camera.takePicture(null, null, this)
                }
            }
        }
        if (!checkPermissions()) {
            requestPermissions(CAMERA_PERMISSIONS, 10)
        } else {
            val alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.setTitle(getString(R.string.warning))
            alertDialogBuilder.setMessage(getString(R.string.camera_warning))
            alertDialogBuilder.setPositiveButton(getString(R.string.accept)) { dialog, _ ->
                dialog.dismiss()
            }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    public override fun onResume() {
        super.onResume()
        cameraViewModel.procesed.observe(viewLifecycleOwner, procesedObserver)
        cameraViewModel.info.observe(viewLifecycleOwner, infoObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        cameraViewModel.procesed.removeObserver(procesedObserver)
    }

    /**
    Verifica si se tienen todos los permisos necesarios.
    @return true si todos los permisos de la cámara están concedidos, false en caso contrario.
     */
    private fun checkPermissions() = CAMERA_PERMISSIONS.all{
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == 10) {
            if (checkPermissions()) {
                Toast.makeText(context, getString(R.string.permissions_granted), Toast.LENGTH_SHORT).show()
                camera = Camera.open()
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.permissions_denied),
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (checkPermissions()) {
            camera = Camera.open()

            try {
                val parameters = camera!!.parameters
                val sizes = parameters.supportedPictureSizes
                val maxSize = sizes[0]
                parameters.setPictureSize(maxSize.width, maxSize.height)
                camera!!.parameters = parameters
                camera!!.setPreviewDisplay(holder)
                camera!!.setDisplayOrientation(90)

                //se adapta el view a la camara
                val height = maxSize.height
                val width = maxSize.width
                val ratio = height.toFloat() / width.toFloat()
                binding.surfaceView.layoutParams.height = (binding.surfaceView.width * ratio).toInt()

            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            requestPermissions(CAMERA_PERMISSIONS, 10)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if(camera == null) return
        camera!!.startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if(camera == null) return
        camera!!.stopPreview()
        camera!!.release()
    }
    /**
    Callback que se invoca cuando se ha capturado una imagen de la cámara.
    @param data Los datos de la imagen capturada en forma de arreglo de bytes.
    @param camera La instancia de la cámara que capturó la imagen.
     */
    override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
        try {
            val bipMap = BitmapFactory.decodeByteArray(data, 0, data!!.size)

            // Calcular el margen a recortar
            val marginPercentage = 0.1f
            val marginWidth = (bipMap.width * marginPercentage).toInt()

            // Calcular las dimensiones del nuevo bitmap recortado
            val croppedWidth = bipMap.width - (2 * marginWidth)
            val croppedHeight = bipMap.height
            // Recortar el bitmap original
            val croppedBitmap = Bitmap.createBitmap(
                bipMap,
                marginWidth,
                0,
                croppedWidth,
                croppedHeight
            )
            cameraViewModel.setPicture(bipMap)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}