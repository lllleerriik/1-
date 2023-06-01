package com.webiki.bucketlist.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import com.webiki.bucketlist.R

class GreetingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_greeting)

        findViewById<ConstraintLayout>(R.id.greetingLayout).setOnClickListener {
            MainActivity.createModalWindow(this,
                getString(R.string.doYouWantSeeTutorial),
                getString(R.string.iWant),
                getString(R.string.cancelButtonText),
                {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.tutorialUri))))
                    finish()
                },
                {
                    finish()
                }
            )
        }
    }
}