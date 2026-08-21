package com.example.cs360project;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class Profile extends AppCompatActivity {

    private EditText editProfileName;
    private EditText editProfilePhone;
    private EditText editProfileGoalWeight;

    private Button updateProfileButton;
    private Switch nightModeSwitch;

    private DatabaseHelper dbHelper;

    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect layout file to Java class
        setContentView(R.layout.activity_profile);

        // App Bar setup
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setIcon(R.drawable.lotus_vector);
        }

        // Get user's ID
        userId = getIntent().getLongExtra("USER_ID", -1);

        // Make sure a valid user ID was received
        if (userId == -1) {

            Toast.makeText(
                    Profile.this,
                    "Unable to load user information.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        // Connect views
        editProfileName =
                findViewById(R.id.editProfileName);

        editProfilePhone =
                findViewById(R.id.editProfilePhone);

        editProfileGoalWeight =
                findViewById(R.id.editProfileGoalWeight);

        updateProfileButton =
                findViewById(R.id.updateProfileButton);

        nightModeSwitch =
                findViewById(R.id.nightModeSwitch);

        // Create database helper
        dbHelper = new DatabaseHelper(this);

        // Load current profile information
        loadProfile();

        // Update Profile button
        updateProfileButton.setOnClickListener(v -> {

            String name = editProfileName
                    .getText()
                    .toString()
                    .trim();

            String phone = editProfilePhone
                    .getText()
                    .toString()
                    .trim();

            String goalWeightText = editProfileGoalWeight
                    .getText()
                    .toString()
                    .trim();

            // Check that fields aren't empty
            if (name.isEmpty()
                    || phone.isEmpty()
                    || goalWeightText.isEmpty()) {

                Toast.makeText(
                        Profile.this,
                        "Please complete all fields.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            float goalWeight;

            try {

                goalWeight =
                        Float.parseFloat(goalWeightText);

            } catch (NumberFormatException e) {

                Toast.makeText(
                        Profile.this,
                        "Please enter a valid goal weight.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // Update database
            int rowsUpdated =
                    dbHelper.updateUserProfile(
                            userId,
                            name,
                            phone,
                            goalWeight
                    );

            if (rowsUpdated > 0) {

                Toast.makeText(
                        Profile.this,
                        "Profile updated successfully!",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                        Profile.this,
                        "Unable to update profile.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Night Mode
        nightModeSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        AppCompatDelegate.setDefaultNightMode(
                                AppCompatDelegate.MODE_NIGHT_YES
                        );

                    } else {

                        AppCompatDelegate.setDefaultNightMode(
                                AppCompatDelegate.MODE_NIGHT_NO
                        );
                    }
                }
        );
    }

    // Menu Navigation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(
                R.menu.appbar_menu,
                menu
        );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        if (itemId == R.id.menu_home) {

            Intent intent =
                    new Intent(this, MainActivity.class);

            intent.putExtra(
                    "USER_ID",
                    userId
            );

            startActivity(intent);

            return true;

        } else if (itemId == R.id.menu_profile) {

            // Already on Profile
            return true;

        } else if (itemId == R.id.menu_logout) {

            Intent intent =
                    new Intent(this, LogIn.class);

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Load current profile information
    private void loadProfile() {

        String name =
                dbHelper.getUserName(userId);

        String phone =
                dbHelper.getUserPhone(userId);

        float goalWeight =
                dbHelper.getGoalWeight(userId);

        editProfileName.setText(name);

        editProfilePhone.setText(phone);

        editProfileGoalWeight.setText(
                String.valueOf(goalWeight)
        );
    }
}