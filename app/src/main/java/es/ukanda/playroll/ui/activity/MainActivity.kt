package es.ukanda.playroll.ui.activity

import android.app.ProgressDialog
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
import es.ukanda.playroll.R
import es.ukanda.playroll.databinding.ActivityMainBinding

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
        super.onCreate(savedInstanceState)
        // inflar el layout de la actividad
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Cargando...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        Handler().postDelayed({
            checkPermissions()
            //se chequea si el usuario esta logeado
            //se chequean los permisos

            progressDialog.dismiss()
        }, 1000)
        slidebar()
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
            Toast.makeText(this, "cerrar sesion", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.action_nav_home_to_nav_sign_off)
            true
        }
        menu.findItem(R.id.miSignIn).setOnMenuItemClickListener {
            Toast.makeText(this, "iniciar sesion", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.action_nav_home_to_nav_login)
            true
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.miSignOut, R.id.miSignOut
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

}