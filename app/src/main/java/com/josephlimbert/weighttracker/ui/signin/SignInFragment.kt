package com.josephlimbert.weighttracker.ui.signin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.josephlimbert.weighttracker.R
import com.josephlimbert.weighttracker.data.repository.AuthResult
import com.josephlimbert.weighttracker.data.repository.FirestoreResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignInFragment : Fragment() {
    private val viewModel: SignInViewModel by viewModels()
    private var emailInput: EditText? = null
    private var passwordInput: EditText? = null

    var loginButton: Button? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_sign_in, container, false)

        // Initialize variables
        emailInput = rootView.findViewById<EditText>(R.id.email_input)
        passwordInput = rootView.findViewById<EditText>(R.id.password_input)
        loginButton = rootView.findViewById<Button>(R.id.login_submit_button)

        loginButton!!.setOnClickListener { _: View? -> this.createAccount() }

        return rootView
    }

    // This function will log the user in if they already have an account or create a new account
    fun createAccount() {
        // do nothing if the username and password are not valid
        if (!validateFields()) return
        loginButton!!.isEnabled = false

        val email = emailInput!!.text.toString()
        val password = passwordInput!!.text.toString()
        //If the user doesn't exist we create a new one
        lifecycleScope.launch {
            when (val result = viewModel.signUpWithEmail(email, password)) {
                is AuthResult.Success -> {
                    when (val result = viewModel.linkUserProfile(result.data)) {
                        is FirestoreResult.Success -> {
                            findNavController().navigate(R.id.navigation_home)
                        }
                        is FirestoreResult.Error -> {
                            Toast.makeText(
                                context,
                                "Invalid Credentials",
                                Toast.LENGTH_LONG
                            ).show()
                            loginButton!!.isEnabled = true
                        }
                    }
                }

                is AuthResult.Error -> {
                    if (result.ex is FirebaseAuthUserCollisionException) {
                        signInAccount()
                    } else if (result.ex is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(
                            context,
                            "Invalid Credentials",
                            Toast.LENGTH_LONG
                        ).show()
                        loginButton!!.isEnabled = true
                    }
                }
            }
        }
    }

    fun signInAccount() {
        val email = emailInput!!.text.toString()
        val password = passwordInput!!.text.toString()
        lifecycleScope.launch {
            when (val result = viewModel.signInWithEmail(email, password)) {
                is AuthResult.Success -> {
                    findNavController().navigate(R.id.navigation_home)
                }
                is AuthResult.Error -> {
                    if (result.ex is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(
                            context,
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