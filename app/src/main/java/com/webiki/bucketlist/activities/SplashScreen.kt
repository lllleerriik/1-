package com.webiki.bucketlist.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.webiki.bucketlist.R
import org.json.JSONArray
import java.util.Scanner
import kotlin.random.Random

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    private val LOAD_DURATION = 4000L

    override fun onCreate(savedInstanceState: Bundle?) {

        val scan = Scanner(assets.open(getString(R.string.motivationPhrasesFileName)))
        val jsonBuilder = StringBuilder()

        while (scan.hasNext()) jsonBuilder.append(scan.nextLine())

        val jsonArray = JSONArray(jsonBuilder.toString())
        val motivationPhrases = mutableListOf<String>()

        for (i in 0 until jsonArray.length())
            motivationPhrases.add(jsonArray.get(i).toString())

        val rnd = Random(System.currentTimeMillis())

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        findViewById<TextView>(R.id.motivationText).text = motivationPhrases[rnd.nextInt(motivationPhrases.size)]
    }

    override fun onResume() {
        super.onResume()
        Thread {
            Thread.sleep(LOAD_DURATION)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }.start()
    }
}