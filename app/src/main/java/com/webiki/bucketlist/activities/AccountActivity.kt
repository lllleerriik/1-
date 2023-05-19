package com.webiki.bucketlist.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.orm.SugarRecord
import com.webiki.bucketlist.Goal
import com.webiki.bucketlist.R
import java.util.Collections

class AccountActivity : AppCompatActivity() {

    private lateinit var accountUserAvatar: ImageView
    private lateinit var accountUserName: TextView
    private lateinit var accountUserEmail: TextView
    private lateinit var accountButtonResetProgress: TextView
    private lateinit var accountButtonBackup: TextView
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
        accountButtonBackup = findViewById(R.id.accountButtonBackup)
        accountButtonResetProgress = findViewById(R.id.accountButtonResetProgress)
        accountButtonLogOut = findViewById(R.id.accountButtonLogOut)

        auth = Firebase.auth
        currentUser = auth.currentUser!!

        currentUser.let {
            Glide
                .with(this)
                .load(it.photoUrl.toString().replace("s96-c", "s400-c"))
                .into(accountUserAvatar)
            accountUserName.text = it.displayName
            accountUserEmail.text = it.email
        }

        val allGoalsInStringList = SugarRecord
            .listAll(Goal::class.java)
            .map { it.parseToString() }
            .toMutableList()

//        if (intent.extras?.getBoolean("isNeedToSaveAllGoals", false) == true) {
            saveAllGoalsToFirebase(allGoalsInStringList)
//        }

        accountButtonBackup.setOnClickListener {
            saveAllGoalsToFirebase(allGoalsInStringList)
            Snackbar
                .make(
                    findViewById(R.id.accountButtonBackup),
                    "Резервное копирование выполнено",
                    Snackbar.LENGTH_LONG
                )
                .show()
        }

        accountButtonLogOut.setOnClickListener {
            auth.signOut()
            finish()
        }

    }

    private fun saveAllGoalsToFirebase(
        localGoals: MutableList<String>
    ) {
        val userGoalsDatabase = Firebase
            .database
            .reference
            .child(
                "${getString(R.string.userFolderInDatabase)}" +
                        "/${currentUser.uid}" +
                        "/${getString(R.string.userGoalsInDatabase)}"
            )

        userGoalsDatabase.get().addOnCompleteListener {
            val cloudGoals = ((it.result.value
                ?: hashMapOf<String, String>()) as HashMap<*, *>)
                .map { pair -> pair.value }
                .toSet()

                val intersectionGoals = localGoals.intersect(cloudGoals)
                val uniqueLocalGoals = localGoals.toMutableList()
                uniqueLocalGoals.removeAll(intersectionGoals)

                val uniqueCloudGoals = cloudGoals.map { g -> g.toString() }.toMutableList()
                uniqueCloudGoals.removeAll(intersectionGoals)

                for (goal in uniqueCloudGoals.map { goalString -> Goal.parseFromString(goalString) }) goal.save()
                for (goal in uniqueLocalGoals) saveGoalToFirebase(goal)
        }
    }

    private fun saveGoalToFirebase(goal: String) {
        Firebase
            .database
            .reference
            .child(
                "${getString(R.string.userFolderInDatabase)}" +
                        "/${currentUser.uid}" +
                        "/${getString(R.string.userGoalsInDatabase)}"
            )
            .push()
            .setValue(goal)
    }
}