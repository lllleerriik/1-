package com.webiki.bucketlist.activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.orm.SugarRecord
import com.webiki.bucketlist.Goal
import com.webiki.bucketlist.R

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

        accountUserAvatar = findViewById(R.id.accountUserAvatar)
        accountUserName = findViewById(R.id.accountUserName)
        accountUserEmail = findViewById(R.id.accountUserEmail)
        accountButtonBackup = findViewById(R.id.accountButtonBackup)
        accountButtonResetProgress = findViewById(R.id.accountButtonResetProgress)
        accountButtonLogOut = findViewById(R.id.accountButtonLogOut)

        auth = Firebase.auth
        currentUser = auth.currentUser!!

        loadUserData(accountUserAvatar, accountUserName, accountUserEmail)

        val allGoalsInStringList = SugarRecord
            .listAll(Goal::class.java)
            .toMutableList()

        Firebase
            .database
            .reference
            .child(
                "${getString(R.string.userFolderInDatabase)}" +
                        "/${currentUser.uid}" +
                        "/${getString(R.string.userGoalsInDatabase)}"
            )
            .get()
            .addOnCompleteListener {
                if (it.result.value == null)
                    saveAllGoalsToFirebase(allGoalsInStringList)
            }

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

        accountButtonResetProgress.setOnClickListener {
            MainActivity.createModalWindow(
                this,
                getString(R.string.douYouWantToResetProgress),
                getString(R.string.submitDelete),
                getString(R.string.cancelButtonText),
                { _ ->
                    SugarRecord.deleteAll(Goal::class.java)

                    Firebase
                        .database
                        .reference
                        .child(getString(R.string.userFolderInDatabase))
                        .child(auth.currentUser!!.uid)
                        .child(getString(R.string.userGoalsInDatabase))
                        .setValue(null)
                        .addOnCompleteListener {
                            if (it.isSuccessful) Toast.makeText(
                                this,
                                "Все цели удалены",
                                Toast.LENGTH_LONG
                            ).show()

                            finish()
                        }
                },
                {}
            )
        }

        accountButtonLogOut.setOnClickListener {
            auth.signOut()
            finish()
        }

    }

    private fun saveAllGoalsToFirebase(
        localGoals: MutableList<Goal>
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
                .map { pair -> Goal.parseFromString(pair.value.toString()) }
                .toMutableList()

            val allGoals = mutableListOf(cloudGoals, localGoals)
                .flatten()
                .distinctBy { g -> g.getLabel() }
                .toMutableList()

            val intersectionGoals = localGoals.intersect(cloudGoals)
            val uniqueGoals =
                mutableListOf<Goal>()

            uniqueGoals.addAll(allGoals)
            uniqueGoals.removeAll(intersectionGoals)

            for (goal in uniqueGoals) {
                if (!SugarRecord.listAll(Goal::class.java).contains(goal))
                    goal.save()

                if (!cloudGoals.contains(goal))
                    saveGoalToFirebase(goal)
            }
        }
    }

    private fun saveGoalToFirebase(goal: Goal) {
        Firebase
            .database
            .reference
            .child(
                "${getString(R.string.userFolderInDatabase)}" +
                        "/${currentUser.uid}" +
                        "/${getString(R.string.userGoalsInDatabase)}"
            )
            .push()
            .setValue(goal.parseToString())
    }

    private fun loadUserData(avatar: ImageView, name: TextView, email: TextView) {
        Thread {
            Firebase.storage.reference.child("ic_account_circle.xml").downloadUrl.addOnCompleteListener { task ->
                if (currentUser.displayName?.isEmpty() == true || currentUser.photoUrl == null) {
                    currentUser.updateProfile(userProfileChangeRequest {
                        displayName =
                            if (currentUser.displayName?.isEmpty()!!) getString(R.string.guest) else currentUser.displayName
                    })
                }

                currentUser.let {
                    name.text = it.displayName
                    email.text = it.email
                }

                if (currentUser.displayName == getString(R.string.guest))
                    avatar.setImageDrawable(getDrawable(R.drawable.ic_account_circle))
                else
                    Glide
                        .with(applicationContext)
                        .load(currentUser.photoUrl.toString().replace("s96-c", "s400-c"))
                        .into(avatar)
            }
        }.start()
    }

    private fun removeGoalFromFirebase(goal: Goal) {
        val query = Firebase
            .database
            .reference
            .child(
                "${getString(R.string.userFolderInDatabase)}" +
                        "/${(Firebase.auth.currentUser ?: return).uid}" +
                        "/${getString(R.string.userGoalsInDatabase)}"
            )
            .orderByValue()
            .equalTo(goal.parseToString())


        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children) item.ref.removeValue().addOnSuccessListener {
                    query.removeEventListener(this)
                }
                return
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        query.addValueEventListener(listener)
    }
}