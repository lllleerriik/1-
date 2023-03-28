package com.webiki.bucketlist.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.webiki.bucketlist.R
import com.webiki.bucketlist.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var goalsLayout: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sPrefEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

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

    }

    override fun onStart() {
        super.onStart()
        goalsLayout = findViewById(R.id.goalsLayout)
        sharedPreferences = getSharedPreferences(R.string.sharedPreferencesName.toString(), MODE_PRIVATE)
        sPrefEditor = sharedPreferences.edit()

//        sPrefEditor.putBoolean(getString(R.string.isUserPassedInitialQuestionnaire), false) //PLUG FOR TESTING QUESTIONNAIRE
//        sPrefEditor.commit()

        if (!sharedPreferences.getBoolean(getString(R.string.isUserPassedInitialQuestionnaire), false))
            startActivity(Intent(this, WelcomeForm::class.java))

        // TODO: extract to method
        val goalsList = arrayListOf("goal 1")
        fillGoalsLayoutFromList(goalsLayout, goalsList)
        //
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun <T> fillGoalsLayoutFromList(layout: LinearLayout, items: ArrayList<T>) {
        val inflater = layoutInflater
        layout.removeAllViews()
        for (item in items) {
            val view = inflater.inflate(R.layout.simple_goal_view, layout, false)
            val viewCheckBox = view.findViewById<CheckBox>(R.id.goalCheckBox)
            val layoutParameters = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            layoutParameters.setMargins(0, 0, 0, 12)
            view.layoutParams = layoutParameters
            viewCheckBox.text = item.toString()

            layout.addView(view)
        }
    }
}