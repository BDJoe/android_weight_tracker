package com.josephlimbert.weighttracker.view;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.josephlimbert.weighttracker.R;
import com.josephlimbert.weighttracker.model.User;
import com.josephlimbert.weighttracker.viewmodel.UserViewModel;
import com.josephlimbert.weighttracker.viewmodel.WeightViewModel;

public class LoginActivity extends AppCompatActivity {

    private UserViewModel userViewModel;
    private EditText emailInput;
    private EditText passwordInput;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize variables
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_submit_button);

        loginButton.setOnClickListener(this::submitCredentials);
    }

    // This function will log the user in if they already have an account or create a new account
    public void submitCredentials(View v) {
        // do nothing if the username and password are not valid
        if (!validateFields()) return;
        loginButton.setEnabled(false);

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        // Try to retrieve the user from the database
        //If the user doesn't exist we create a new one
        userViewModel.signUpEmail(email, password, task -> {
            if (task.isSuccessful()) {
                finish();
            } else {
                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    userViewModel.signInEmail(email, password, signInTask -> {
                        if (signInTask.isSuccessful()) {
                            finish();
                        } else {
                            if (signInTask.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(), "Invalid Credentials", Toast.LENGTH_LONG).show();
                                loginButton.setEnabled(true);
                            }
                        }
                    });
                }
            }
        });
    }

    // This function checks that the username and password are not empty before submitting them.
    private boolean validateFields() {
        if (emailInput.length() == 0) {
            emailInput.setError("Username cannot be empty");
            return false;
        }
        if (passwordInput.length() == 0) {
            passwordInput.setError("Password cannot be empty");
            return false;
        }
        return true;
    }
}