package es.ukanda.playroll.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import es.ukanda.playroll.R
import es.ukanda.playroll.databinding.FragmentLoginBinding
import kotlin.math.acos


class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    companion object {
        enum class ProviderType {
            BASIC,
            GOOGLE
        }

        var provider: ProviderType = ProviderType.BASIC
        var email: String = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        buttons()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    /**
    Configura los botones y las acciones asociadas en la pantalla de inicio de sesión.
    */
    private fun buttons() {
        this.activity?.title = getString(R.string.login)
        binding.btSignIn.setOnClickListener {
            if(binding.etEmailLog.text.isNotEmpty() && binding.etPasswordLog.text.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    binding.etEmailLog.text.toString(),
                    binding.etPasswordLog.text.toString()
                ).addOnCompleteListener {
                    if(it.isSuccessful) {
                        provider = ProviderType.BASIC
                        email = binding.etEmailLog.text.toString()

                        val prefs = activity?.getSharedPreferences(getString(R.string.prefs_file), 0)?.edit()
                        prefs?.putString("email", email)
                        prefs?.putString("provider", provider.name)
                        prefs?.apply()
                        Toast.makeText(context, getString(R.string.session_started_successfully), Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_nav_login_to_nav_home)
                    } else {
                        Toast.makeText(context, getString(R.string.failed_to_login), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, getString(R.string.fill_in_all_the_fields), Toast.LENGTH_SHORT).show()
            }
        }
        binding.btRegistrarseLog.setOnClickListener {
            findNavController().navigate(R.id.action_nav_login_to_nav_register)
        }

        var launcher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                        println(it.result.toString())
                        if (it.isSuccessful){
                            val prefs = activity?.getSharedPreferences(getString(R.string.prefs_file), 0)?.edit()
                            prefs?.putString("email", account.email)
                            prefs?.putString("provider", ProviderType.GOOGLE.name)
                            prefs?.apply()
                            Toast.makeText(context, getString(R.string.session_started_successfully), Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_nav_login_to_nav_home)
                        }else{
                            Toast.makeText(context, getString(R.string.failed_to_login), Toast.LENGTH_SHORT).show()
                        }
                    }

                }
                }catch (e: Exception){
                    Toast.makeText(context, "${getString(R.string.error)}: ${e.message}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }

        binding.btGoogleLog.setOnClickListener {
            val confGoogle = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleClient = GoogleSignIn.getClient(this.context!!, confGoogle)
            googleClient.signOut()
            launcher.launch(googleClient.signInIntent)
        }
    }
}

