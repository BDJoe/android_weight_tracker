package com.josephlimbert.weighttracker

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.josephlimbert.weighttracker.ui.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val userViewModel: UserViewModel by viewModels()
    private var emailInput: EditText? = null
    private var passwordInput: EditText? = null
    var loginButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.login)
        ) { v: View?, insets: WindowInsetsCompat? ->
            val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
            v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Initialize variables
        emailInput = findViewById<EditText>(R.id.email_input)
        passwordInput = findViewById<EditText>(R.id.password_input)
        loginButton = findViewById<Button>(R.id.login_submit_button)

        loginButton!!.setOnClickListener { _: View? -> this.submitCredentials() }
    }

    // This function will log the user in if they already have an account or create a new account
    fun submitCredentials() {
        // do nothing if the username and password are not valid
        if (!validateFields()) return
        loginButton!!.isEnabled = false

        val email = emailInput!!.text.toString()
        val password = passwordInput!!.text.toString()
        //If the user doesn't exist we create a new one
        lifecycleScope.launch {
            try {
                userViewModel.signUpWithEmail(email, password)
                finish()
            } catch (e: Exception) {
                if (e is FirebaseAuthUserCollisionException) {
                    try {
                        userViewModel.signInWithEmail(email, password)
                        finish()
                    } catch (e: Exception) {
                        if (e is FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(
                                applicationContext,
                                "Invalid Credentials",
                                Toast.LENGTH_LONG
                            ).show()
                            loginButton!!.isEnabled = true
                        }
                    }
                }else {
                    if (e is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(
                            applicationContext,
                            "Invalid Credentials",
                            Toast.LENGTH_LONG
                        ).show()
                        loginButton!!.isEnabled = true
                    }
                }
            }
        }
    }

    // This function checks that the username and password are not empty before submitting them.
    private fun validateFields(): Boolean {
        if (emailInput!!.length() == 0) {
            emailInput!!.error = "Username cannot be empty"
            return false
        }
        if (passwordInput!!.length() == 0) {
            passwordInput!!.error = "Password cannot be empty"
            return false
        }
        return true
    }
}