package com.webiki.bucketlist.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.AppCompatButton
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.webiki.bucketlist.R

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var signInRequest: BeginSignInRequest
    private val REQUEST_ID = 2201
    private lateinit var googleSignInClient: GoogleSignInClient
    private var currentUser: FirebaseUser? = null

    private lateinit var googleLogInButton: AppCompatButton
    private lateinit var vkLogInButton: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.firebaseClientID))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            .build()
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.firebaseClientID))
            .requestEmail()
            .build()

        googleLogInButton = findViewById(R.id.googleSignInButton)
        vkLogInButton = findViewById(R.id.vkSignInButton)

        googleLogInButton.setOnClickListener {
            googleSignInClient = GoogleSignIn.getClient(this, signInOptions)
            startActivityForResult(
                googleSignInClient.signInIntent,
                REQUEST_ID
            )
        }
        vkLogInButton.setOnClickListener {
            MainActivity.createModalWindow(
                this,
                getString(R.string.inDevProgress),
                "Хорошо",
                "Отмена",
                { },
                { })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_ID -> {
                try {
                    val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                        .getResult(ApiException::class.java)!!
                    when {
                        account.idToken != null -> {
                            authWithGoogle(account.idToken)
                        }

                        else -> {
                        }
                    }
                } catch (e: ApiException) {
                }
            }
        }
    }

    private fun authWithGoogle(idToken: String?) {
        when {
            idToken != null -> {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            currentUser = auth.currentUser
                            finish()
                            startActivity(Intent(this, AccountActivity::class.java).putExtra("isNeedToSaveAllGoals", true)) //TODO put string to res
                        }
                    }
            }

            else -> {
                Log.d("DEB", "No ID token!")
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }


}