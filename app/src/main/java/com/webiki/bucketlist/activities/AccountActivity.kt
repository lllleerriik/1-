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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.orm.SugarRecord
import com.webiki.bucketlist.Goal
import com.webiki.bucketlist.R
import java.util.Objects
import kotlin.reflect.typeOf

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
//        Firebase.database.setPersistenceEnabled(true) TODO de-comment for caching

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
            saveAllGoalsToFirebase(SugarRecord.listAll(Goal::class.java).map { it.parseToString() }
                .toMutableList())
        }

        accountButtonLogOut.setOnClickListener {
            auth.signOut()
            finish()
        }

    }

    private fun saveAllGoalsToFirebase(offlineGoals: MutableList<String>) {
        val userGoalsDatabase = Firebase
            .database
            .reference
            .child(
                "${getString(R.string.userFolderInDatabase)}" +
                        "/${currentUser.uid}" +
                        "/${getString(R.string.userGoalsInDatabase)}"
            )


        userGoalsDatabase.get().addOnCompleteListener {
            val goalsInFirebase = ((it.result.value
                ?: hashMapOf<String, String>()) as HashMap<*, *>)
            .map { pair -> pair.value }
            .toSet()

            offlineGoals.removeAll(goalsInFirebase)
            offlineGoals.forEach { goal -> userGoalsDatabase.push().setValue(goal) }
        }
    }
}