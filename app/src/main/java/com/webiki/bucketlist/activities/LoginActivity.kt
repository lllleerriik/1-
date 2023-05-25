package com.webiki.bucketlist.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.exceptions.VKAuthException
import com.webiki.bucketlist.R

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var signInRequest: BeginSignInRequest
    private val GOOGLE_REQUEST_ID = 2201

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
                    .setServerClientId(getString(R.string.firebaseClientID))
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
                GOOGLE_REQUEST_ID
            )
        }

        vkLogInButton.setOnClickListener {
            MainActivity.createModalWindow(
                this,
                getString(R.string.vpnAttention),
                "Хорошо",
                "Отмена",
                {
                    VK.login(this, arrayListOf(VKScope.EMAIL, VKScope.WALL))
                },
                { })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            GOOGLE_REQUEST_ID -> {
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
                } catch (_: ApiException) {
                }
            }

            else -> {
                val callback = object : VKAuthCallback {
                    override fun onLogin(token: VKAccessToken) {

                        val email = token.email!!
                        val password = token.userId.toString()
                        val instance = FirebaseAuth.getInstance()

                        Log.d("DEB", "${email} ${password}")

                        instance.fetchSignInMethodsForEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val isNewUser = task.result?.signInMethods?.isEmpty() ?: false

                                    if (isNewUser) {
                                        instance
                                            .createUserWithEmailAndPassword(email, password)
                                            .addOnCompleteListener {
                                                if (it.isSuccessful) {
                                                    currentUser = auth.currentUser
                                                    finish()
                                                    startActivity(Intent(applicationContext, AccountActivity::class.java))
                                                    Toast.makeText(
                                                        applicationContext,
                                                        getString(R.string.successLogin),
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                    } else instance.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            currentUser = auth.currentUser
                                            finish()
                                            startActivity(Intent(applicationContext, AccountActivity::class.java))
                                        }
                                    }

                                }
                            }

                        Toast.makeText(
                            applicationContext,
                            getString(R.string.successLogin),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    override fun onLoginFailed(authException: VKAuthException) {
                        Log.d("DEB", "FAIL$authException")
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.failedLogin),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                if (data == null || !VK.onActivityResult(
                        requestCode,
                        resultCode,
                        data,
                        callback
                    )
                ) return
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
                            startActivity(Intent(this, AccountActivity::class.java))
                        }
                    }
            }

            else -> {
                Log.d("DEB", "No ID token!")
            }
        }
    }
}