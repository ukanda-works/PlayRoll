package es.ukanda.playroll.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import es.ukanda.playroll.R
import es.ukanda.playroll.databinding.FragmentAcountBinding


class AcountFragment : Fragment() {

    private var _binding: FragmentAcountBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentAcountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }
    /**
    Realiza la configuración inicial de la actividad.
     */
    private fun setup() {
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        currentUser?.let {
            val userName = currentUser.displayName
            val email = currentUser.email
            val loginType = getString(R.string.undetermined)

            binding.tvUserName.text = userName
            binding.tvEmail.text = email

            val photoUrl = currentUser.photoUrl
            if (photoUrl != null) {
                Picasso.get().load(photoUrl).into(binding.userImage)
            }
        }
    }
}