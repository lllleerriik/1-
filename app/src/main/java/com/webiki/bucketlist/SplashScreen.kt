package com.webiki.bucketlist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.webiki.bucketlist.activities.MainActivity
import java.sql.Time
import java.util.Calendar
import kotlin.random.Random

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val motivationText: TextView  =findViewById<TextView>(R.id.motivationText)
        val texts = listOf("Motivation", "phrases", "into")
        val rnd = Random(System.currentTimeMillis())
        motivationText.setText( texts.get(rnd.nextInt( 3)) ) // not see because activity
                                                                  // started before loading rnd
//        Thread.sleep(2000) //TODO: delete if main will long-loaded
        val rndList = arrayListOf(arrayListOf("1", "2"), arrayListOf("3", "4"))
        startActivity(Intent(this, MainActivity::class.java).putExtra("goals", rndList[Random.nextInt(2)]))
        finish()

    }
}