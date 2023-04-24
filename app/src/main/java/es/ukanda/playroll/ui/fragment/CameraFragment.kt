package es.ukanda.playroll.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.Toast
import android.hardware.Camera
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import es.ukanda.playroll.R
import es.ukanda.playroll.databinding.FragmentCameraBinding
import es.ukanda.playroll.ui.ViewModel.CameraViewModel
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
            Toast.makeText(context, "Foto procesada", Toast.LENGTH_SHORT).show()
            val bundle = Bundle()
            bundle.putByteArray("image", cameraViewModel.getByteArray())
            bundle.putSerializable("personaje", cameraViewModel.foundText.value as java.io.Serializable)
            bundle.putString("texto", cameraViewModel.textoPrueva.value?: "No se ha encontrado texto")
            findNavController().navigate(R.id.action_nav_camera_to_nav_camera_result, bundle)
        }
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
            Toast.makeText(context, "Permisos concedidos", Toast.LENGTH_SHORT).show()
        }
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    public override fun onResume() {
        super.onResume()
        cameraViewModel.procesed.observe(viewLifecycleOwner, procesedObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        cameraViewModel.procesed.removeObserver(procesedObserver)
    }

    //Permisions
    private fun checkPermissions() = CAMERA_PERMISSIONS.all{
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == 10) {
            if (checkPermissions()) {
                Toast.makeText(context, "Permisos concedidos", Toast.LENGTH_SHORT).show()
                camera = Camera.open()
            } else {
                Toast.makeText(
                    context,
                    "Se denegaron los permisos necesarios.",
                    Toast.LENGTH_SHORT
                ).show()
                //vuelve al fragment anterior
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
            val maxSize = sizes[sizes.size - 1]
            parameters.setPictureSize(maxSize.width, maxSize.height)
            camera!!.parameters = parameters
            camera!!.setPreviewDisplay(holder)
            camera!!.setDisplayOrientation(90)

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

    override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
        val bipMap = BitmapFactory.decodeByteArray(data, 0, data!!.size)
        cameraViewModel.setPicture(bipMap)
        Toast.makeText(context, "Foto tomada", Toast.LENGTH_SHORT).show()
    }


}