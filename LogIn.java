package com.example.cs360project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LogIn extends AppCompatActivity {

    private EditText editUsername;
    private EditText editPassword;

    private Button buttonLogin;
    private Button buttonCreateAccount;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect layout file to Java class
        setContentView(R.layout.activity_login);

        // Connect EditTexts
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);

        // Connect Buttons
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        // Create database helper
        dbHelper = new DatabaseHelper(this);

        // Log In button
        buttonLogin.setOnClickListener(v -> {

            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            // Check that both fields have been completed
            if (username.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                        LogIn.this,
                        "Please enter your username and password.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // Check credentials against database
            long userId = dbHelper.checkLogin(username, password);

            if (userId != -1) {

                Toast.makeText(
                        LogIn.this,
                        "Login successful!",
                        Toast.LENGTH_SHORT
                ).show();

                // Collect userID
                Intent intent = new Intent(LogIn.this, MainActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
                finish();
            }

            else {

                Toast.makeText(
                        LogIn.this,
                        "Invalid username or password.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Create Account button
        buttonCreateAccount.setOnClickListener(v -> {

            Intent intent = new Intent(LogIn.this, CreateAccount.class);
            startActivity(intent);
        });
    }
}