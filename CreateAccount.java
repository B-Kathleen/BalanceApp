package com.example.cs360project;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class CreateAccount extends AppCompatActivity {
    private EditText editCreateUsername;
    private EditText editCreatePassword;
    private EditText editCreateName;
    private EditText editCreatePhone;
    private EditText editCurrentWeight;
    private EditText editGoalWeight;

    private Button createAccountButton;
    private Button returnToLoginButton;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect layout file to java class
        setContentView(R.layout.activity_new_account);

        editCreateUsername = findViewById(R.id.editCreateUsername);
        editCreatePassword = findViewById(R.id.editCreatePassword);
        editCreateName = findViewById(R.id.editCreateName);
        editCreatePhone = findViewById(R.id.editCreatePhone);
        editCurrentWeight = findViewById(R.id.editCurrentWeight);
        editGoalWeight = findViewById(R.id.editGoalWeight);

        createAccountButton = findViewById(R.id.createAccountButton);
        returnToLoginButton = findViewById(R.id.returnToLoginButton);

        dbHelper = new DatabaseHelper(this);

        // Add user credentials
        createAccountButton.setOnClickListener(v -> {

            String username = editCreateUsername.getText().toString().trim();
            String password = editCreatePassword.getText().toString().trim();
            String name = editCreateName.getText().toString().trim();
            String phone = editCreatePhone.getText().toString().trim();
            String currentWeightText = editCurrentWeight.getText().toString().trim();
            String goalWeightText = editGoalWeight.getText().toString().trim();

            // Check that all fields have been completed
            if (username.isEmpty() ||
                    password.isEmpty() ||
                    name.isEmpty() ||
                    phone.isEmpty() ||
                    currentWeightText.isEmpty() ||
                    goalWeightText.isEmpty()) {

                Toast.makeText(
                        CreateAccount.this,
                        "Please complete all fields.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // Convert weight values from String to float
            float currentWeight = Float.parseFloat(currentWeightText);
            float goalWeight = Float.parseFloat(goalWeightText);

            // Add user to database
            long userId = dbHelper.addUser(
                    username,
                    password,
                    name,
                    phone,
                    goalWeight
            );

            if (userId != -1) {

                String date = new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                ).format(new Date());

                // Add initial weight entry
                dbHelper.addWeight(
                        userId,
                        date,
                        currentWeight
                );

                Toast.makeText(
                        CreateAccount.this,
                        "Account created successfully!",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            }

            else {

                Toast.makeText(
                        CreateAccount.this,
                        "Unable to create account. Username may already exist.",
                        Toast.LENGTH_SHORT
                ).show();
            }

        });

        // Return to Log In screen
        returnToLoginButton.setOnClickListener(v -> {
            finish();
        });
    }



}
