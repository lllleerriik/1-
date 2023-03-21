package com.webiki.bucketlist.activities

import android.icu.util.TimeUnit
import android.os.Bundle
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
import java.lang.Integer.parseInt
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var goalsLayout: LinearLayout

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
        // TODO: extract to method
        val goalsList = intent.getStringArrayListExtra("goals")
        fillGoalsLayoutFromList<String>(goalsLayout, goalsList!!)
        //
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun <T> fillGoalsLayoutFromList(layout: LinearLayout, items: ArrayList<String>) {
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

            viewCheckBox.setOnClickListener {
                Toast.makeText(this, "You clicked text $item", Toast.LENGTH_SHORT).show()
            }

            layout.addView(view)
        }
    }
}