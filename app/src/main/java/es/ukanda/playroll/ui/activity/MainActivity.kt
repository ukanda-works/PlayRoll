package es.ukanda.playroll.ui.activity

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.widget.Toast
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import es.ukanda.playroll.R
import es.ukanda.playroll.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    companion object{
        val permisionList = arrayOf(
            android.Manifest.permission.ACCESS_WIFI_STATE,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.CAMERA
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setDefaultLanguage()
        super.onCreate(savedInstanceState)
        // inflar el layout de la actividad
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage(getString(R.string.loading))
        progressDialog.setCancelable(false)

        progressDialog.show()
        Handler().postDelayed({
            checkPermissions()

            progressDialog.dismiss()
        }, 1000)
        slidebar()
        loadBaseData()
    }

    private fun setDefaultLanguage() {
        val locale = Locale("es")
        Locale.setDefault(locale)

        val resources = resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        createConfigurationContext(configuration)
    }


    private fun loadBaseData() {
        /*val loadData = LoadData(this)
        loadData.load()*/
    }

    private fun checkPermissions() {
        val requestCode = 1
        val permissionsToRequest = mutableListOf<String>()

        for (permission in permisionList) {
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            val permissionsArray = permissionsToRequest.toTypedArray()
            requestPermissions(permissionsArray, requestCode)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun slidebar(){
        setSupportActionBar(binding.appBarMain.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val menu = navView.menu
        menu.findItem(R.id.miSignOut).setOnMenuItemClickListener {
            showSignOutConfirmationDialog(navController)
            true
        }

        menu.findItem(R.id.nav_acountInfo).setOnMenuItemClickListener {
            navController.navigate(R.id.action_to_acountInfo)
            true
        }
        menu.findItem(R.id.nav_home).setOnMenuItemClickListener {
            navController.navigate(R.id.action_to_home)
            true
        }
        menu.findItem(R.id.nav_partyList).setOnMenuItemClickListener {
            navController.navigate(R.id.action_to_partyList)
            true
        }
        menu.findItem(R.id.nav_characterList).setOnMenuItemClickListener {
            navController.navigate(R.id.action_to_characterList)
            true
        }

        binding.appBarMain.toolbar.setOnMenuItemClickListener{menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    // Navegar al fragment de configuración
                    navController.navigate(R.id.action_to_settings)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.action_about -> {
                    // Navegar al fragment de acerca de
                    navController.navigate(R.id.action_to_about)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.action_help -> {
                    // Navegar al fragment de ayuda
                    navController.navigate(R.id.action_to_help)
                    drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.miSignOut, R.id.miSignOut
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
    private fun showSignOutConfirmationDialog(navController: NavController) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.sign_out))
            .setMessage(getString(R.string.sign_out_confirmation))
            .setPositiveButton(R.string.yes) { dialog: DialogInterface, _: Int ->
                signOutAndNavigateToLogin(navController)
                dialog.dismiss()
            }
            .setNegativeButton(getText(R.string.no)) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .show()
    }

    private fun signOutAndNavigateToLogin(navController: NavController) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            firebaseAuth.signOut()
            val prefs = getSharedPreferences(getString(R.string.prefs_file), 0).edit()
            prefs.clear()
            prefs.apply()
            Toast.makeText(this, getString(R.string.sesion_closed), Toast.LENGTH_SHORT).show()
        }

        navController.navigate(R.id.action_nav_home_to_nav_login)
    }


}