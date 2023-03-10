package com.webiki.bucketlist.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.webiki.bucketlist.R

/**
 * Class for creating smth like SPA
 */
class IntroductoryForm : AppCompatActivity() {
    private lateinit var welcomeFormTitle: TextView
    private lateinit var welcomeFormImage: ImageView
    private lateinit var welcomeFormDescription: TextView
    private lateinit var welcomeFormButton: Button
    private val titles: List<String> = listOf("Title 1", "Title 2")
    private val descs: List<String> = listOf("Desc 1", "Desc 2")
    private var currentPage: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introductory_form)

        welcomeFormTitle = findViewById(R.id.welcomeFormTitle)
        welcomeFormImage = findViewById(R.id.welcomeFormImage)
        welcomeFormDescription = findViewById(R.id.welcomeFormDescription)
        welcomeFormButton = findViewById(R.id.welcomeFormButton)

        welcomeFormButton.setOnClickListener{view ->
            skipToNextPage()
        }
    }

    private fun skipToNextPage() {
        welcomeFormTitle.setText(titles.get(currentPage))
        welcomeFormDescription.setText(descs.get(currentPage++))
    }
}