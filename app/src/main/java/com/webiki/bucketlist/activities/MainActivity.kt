package com.webiki.bucketlist.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.webiki.bucketlist.ProjectSharedPreferencesHelper
import com.webiki.bucketlist.R
import com.webiki.bucketlist.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var accountPreviewLayout: LinearLayout
    private lateinit var accountPreviewAvatar: ImageView
    private lateinit var accountPreviewName: TextView
    private lateinit var accountPreviewEmail: TextView

    private lateinit var storageHelper: ProjectSharedPreferencesHelper
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private var isModalWindowWasShown: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hasInternetConnection =
            (this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
                .activeNetworkInfo?.isConnected ?: false

        if (!hasInternetConnection && !isModalWindowWasShown) {
            createModalWindow(
                this,
                getString(R.string.hasnotNetworkConnection),
                "Хорошо",
                "Отмена",
                {},
                {}
            )
            isModalWindowWasShown = true
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        supportActionBar?.setBackgroundDrawable(
            AppCompatResources.getDrawable(
                this,
                R.drawable.custom_actionbar
            )
        )

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        binding.closeAppButton.setOnClickListener {
            createModalWindow(
                this,
                getString(R.string.doYouWantToCloseApp),
                getString(R.string.submitCloseApp),
                getString(R.string.cancelButtonText),
                { finish() }
            )
        }
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_motivation, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        accountPreviewLayout = navView.getHeaderView(0).findViewById(R.id.accountPreviewLayout)
        accountPreviewAvatar = navView.getHeaderView(0).findViewById(R.id.accountPreviewAvatar)
        accountPreviewName = navView.getHeaderView(0).findViewById(R.id.accountPreviewName)
        accountPreviewEmail = navView.getHeaderView(0).findViewById(R.id.accountPreviewEmail)

        auth = Firebase.auth

        storageHelper = ProjectSharedPreferencesHelper(this)
    }

    override fun onResume() {
        super.onResume()

        currentUser = auth.currentUser

        if (currentUser != null) {
            Glide
                .with(this)
                .load(currentUser!!.photoUrl)
                .into(accountPreviewAvatar)
        } else accountPreviewAvatar.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_account_circle))

        accountPreviewName.text = if (currentUser != null) currentUser!!.displayName else getString(R.string.name_surname)
        accountPreviewEmail.text = if (currentUser != null) currentUser!!.email else getString(R.string.nav_email)
    }

    override fun onStart() {
        super.onStart()

//        SugarRecord.deleteAll(Goal::class.java)
//        storageHelper.addBooleanToStorage(getString(R.string.isUserPassedInitialQuestionnaire), false) //PLUG FOR TESTING QUESTIONNAIRE

        if (!storageHelper.getBooleanFromStorage(getString(R.string.isUserPassedInitialQuestionnaire), false))
            startActivity(Intent(this, WelcomeForm::class.java))

        accountPreviewLayout.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    if (Firebase.auth.currentUser == null) LoginActivity::class.java
                    else AccountActivity::class.java
                )
            )
        }

    }

    override fun onBackPressed() {
        createModalWindow(
            this,
            getString(R.string.doYouWantToCloseApp),
            getString(R.string.submitCloseApp),
            getString(R.string.cancelButtonText),
            { finish() }
        )
    }

    /**
     * Создаёт модальное окно по макету popup_window_view
     *
     * @param ctx Контекст создания окна
     * @param messageText Текст главного текста окна
     * @param confirmButtonText Текст на кнопке положительного ответа
     * @param cancelButtonText Текст на кнопке отрицательного ответа
     * @param confirmButtonHandler Обработчик положительного ответа
     * @param cancelButtonHandler Обработчик отрицательного ответа
     *
     */
    companion object {
        internal fun createModalWindow(
            ctx: Context,
            messageText: String,
            confirmButtonText: String,
            cancelButtonText: String,
            confirmButtonHandler: (AppCompatButton) -> Unit,
            cancelButtonHandler: ((AppCompatButton) -> Unit)? = null
        ) {
            val dialog = Dialog(ctx)

            dialog.setContentView(R.layout.popup_window_view)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val dialogText = dialog.window?.findViewById<TextView>(R.id.simplePopupText)!!
            val dialogSubmitButton =
                dialog.window?.findViewById<AppCompatButton>(R.id.simplePopupButtonSubmit)!!
            val dialogCancelButton =
                dialog.window?.findViewById<AppCompatButton>(R.id.simplePopupButtonCancel)!!

            dialogText.text = messageText
            dialogSubmitButton.text = confirmButtonText
            dialogCancelButton.text = cancelButtonText

            dialogSubmitButton.setOnClickListener { btn ->
                confirmButtonHandler(btn as AppCompatButton)
                dialog.dismiss()
            }
            dialogCancelButton.setOnClickListener { btn ->
                if (cancelButtonHandler != null) cancelButtonHandler(btn as AppCompatButton)
                dialog.dismiss()
            }

            dialog.create()
            dialog.show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}