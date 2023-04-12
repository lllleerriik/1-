package com.webiki.bucketlist.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.orm.SugarRecord
import com.webiki.bucketlist.Goal
import com.webiki.bucketlist.ProjectSharedPreferencesHelper
import com.webiki.bucketlist.R
import com.webiki.bucketlist.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var storageHelper: ProjectSharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        supportActionBar?.setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.custom_actionbar))

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_motivation, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        storageHelper = ProjectSharedPreferencesHelper(this)
    }

    override fun onStart() {
        super.onStart()

//        SugarRecord.deleteAll(Goal::class.java)
//        storageHelper.addBooleanToStorage(getString(R.string.isUserPassedInitialQuestionnaire), false) //PLUG FOR TESTING QUESTIONNAIRE

        if (!storageHelper.getBooleanFromStorage(getString(R.string.isUserPassedInitialQuestionnaire), false))
            startActivity(Intent(this, WelcomeForm::class.java))
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}