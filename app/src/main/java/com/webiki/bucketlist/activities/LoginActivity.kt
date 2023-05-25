package com.webiki.bucketlist.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiConfig
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.exceptions.VKAuthException
import com.vk.api.sdk.utils.VKUtils
import com.webiki.bucketlist.R
import kotlin.random.Random


class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var signInRequest: BeginSignInRequest
    private val GOOGLE_REQUEST_ID = 2201

    //    private val VK_REQUEST_ID = 2202
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
//                            throw NotImplementedError("Need to realise excluding goals with same names")
                        }
                    }
            }

            else -> {
                Log.d("DEB", "No ID token!")
            }
        }
    }
}