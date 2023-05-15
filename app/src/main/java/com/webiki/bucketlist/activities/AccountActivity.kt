package com.webiki.bucketlist.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.orm.SugarRecord
import com.webiki.bucketlist.Goal
import com.webiki.bucketlist.R

class AccountActivity : AppCompatActivity() {

    private lateinit var accountUserAvatar: ImageView
    private lateinit var accountUserName: TextView
    private lateinit var accountUserEmail: TextView
    private lateinit var accountButtonResetProgress: TextView
    private lateinit var accountButtonLogOut: AppCompatButton

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
//        Firebase.database.setPersistenceEnabled(true)

        accountUserAvatar = findViewById(R.id.accountUserAvatar)
        accountUserName = findViewById(R.id.accountUserName)
        accountUserEmail = findViewById(R.id.accountUserEmail)
        accountButtonResetProgress = findViewById(R.id.accountButtonResetProgress)
        accountButtonLogOut = findViewById(R.id.accountButtonLogOut)

        auth = Firebase.auth
        currentUser = auth.currentUser!!

        currentUser.let {
            Glide.with(this).load(currentUser.photoUrl.toString().replace("s96-c", "s400-c"))
                .into(accountUserAvatar)
            accountUserName.text = it.displayName
            accountUserEmail.text = it.email
        }

        if (intent.extras?.getBoolean("isNeedToSaveAllGoals", false) == true) {
            saveAllGoalsToFirebase(SugarRecord.listAll(Goal::class.java).map { it.parseToString() })
        }

    }

    private fun saveAllGoalsToFirebase(offlineGoals: List<String>) {
        val goalsInFirebase = mutableListOf<String>()
        val userGoalsDatabase = Firebase
            .database
            .reference
            .child(getString(R.string.userFolderInDatabase))
            .child(Firebase.auth.currentUser!!.uid)
            .child(getString(R.string.userGoalsInDatabase))

        userGoalsDatabase.get().addOnSuccessListener {
            goalsInFirebase.add(it.value.toString())
        }
            .addOnFailureListener {
                Log.d("DEB", it.toString())
            }

        goalsInFirebase.addAll(offlineGoals)

        Log.d("DEB", offlineGoals.toString())
        Log.d("DEB", goalsInFirebase.toString()) //TODO very strange behavior getting goals from realtime...

        val uniqueGoals = goalsInFirebase.distinctBy { goalsInFirebase.contains(it) }

        uniqueGoals.forEach { userGoalsDatabase.setValue(it) }
        Log.d("DEB", uniqueGoals.toString())
    }
}