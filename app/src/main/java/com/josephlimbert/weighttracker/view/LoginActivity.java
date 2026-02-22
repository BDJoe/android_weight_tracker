package com.josephlimbert.weighttracker.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.josephlimbert.weighttracker.R;
import com.josephlimbert.weighttracker.model.User;
import com.josephlimbert.weighttracker.viewmodel.UserViewModel;

public class LoginActivity extends AppCompatActivity {

    private UserViewModel userViewModel;
    private EditText usernameInput;
    private EditText passwordInput;

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
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        Button loginButton = findViewById(R.id.login_submit_button);
        TextView guestButton = findViewById(R.id.login_guest_button);

        loginButton.setOnClickListener(this::submitCredentials);
        guestButton.setOnClickListener(this::loginGuest);
    }

    // This function will log the user in if they already have an account or create a new account
    public void submitCredentials(View v) {
        // do nothing if the username and password are not valid
        if (!validateFields()) return;

        long userId;
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();
        // Try to retrieve the user from the database
        //If the user doesn't exist we create a new one
        User user = userViewModel.loginUser(username, password);
        if (user == null) {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password);
            userId = userViewModel.registerUser(newUser);
        }
        else userId = user.getId();
        // log the user in and close the activity
        userViewModel.setLoggedInUserId(userId, getApplicationContext());
        finish();
    }

    // This function will log in a user anonymously
    public void loginGuest(View v) {
        // Create an anonymous user
        long userId;
        String username = getString(R.string.anonymous_login);
        String password = getString(R.string.anonymous_login);
        User user = userViewModel.loginUser(username, password);
        // Check if an anonymous user already exits. If it does we log in as that user, otherwise
        // we create a new anonymous user. This is required since we need a user ID for the weight db.
        if (user == null) {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password);
            userId = userViewModel.registerUser(newUser);
        }
        else userId = user.getId();

        userViewModel.setLoggedInUserId(userId, getApplicationContext());
        finish();
    }

    // This function checks that the username and password are not empty before submitting them.
    private boolean validateFields() {
        if (usernameInput.length() == 0) {
            usernameInput.setError("Username cannot be empty");
            return false;
        }
        if (passwordInput.length() == 0) {
            passwordInput.setError("Password cannot be empty");
            return false;
        }
        return true;
    }
}