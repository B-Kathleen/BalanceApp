package com.example.cs360project;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView helloUserText;
    private TextView currentWeightDisplay;
    private TextView goalWeightDisplay;

    private EditText currentWeightInput;
    private Button enterWeightButton;

    private GridLayout weightHistoryGrid;

    private DatabaseHelper dbHelper;

    private long userId;

    private static final int REQUEST_SMS_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect layout file to Java class
        setContentView(R.layout.activity_main);

        // App Bar Set up
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setIcon(R.drawable.lotus_vector);
        }

        // Get logged-in user's ID
        userId = getIntent().getLongExtra("USER_ID", -1);

        // Make sure a valid user ID was received
        if (userId == -1) {
            Toast.makeText(
                    MainActivity.this,
                    "Unable to load user information.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        // Connect views
        helloUserText = findViewById(R.id.helloUserText);
        currentWeightDisplay = findViewById(R.id.currentWeightDisplay);
        goalWeightDisplay = findViewById(R.id.goalWeightDisplay);

        currentWeightInput = findViewById(R.id.currentWeightInput);
        enterWeightButton = findViewById(R.id.enterWeightButton);

        weightHistoryGrid = findViewById(R.id.weightHistoryGrid);

        // Create database helper
        dbHelper = new DatabaseHelper(this);

        // Load user information
        loadUserInformation();

        // Load weight history
        loadWeightHistory();

        // Get SMS permissions
        hasSmsPermission();

        // Enter Weight button
        enterWeightButton.setOnClickListener(v -> {

            String weightText = currentWeightInput
                    .getText()
                    .toString()
                    .trim();

            // Make sure weight was entered
            if (weightText.isEmpty()) {

                Toast.makeText(
                        MainActivity.this,
                        "Please enter your current weight.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            float weight;

            try {
                weight = Float.parseFloat(weightText);
            } catch (NumberFormatException e) {

                Toast.makeText(
                        MainActivity.this,
                        "Please enter a valid weight.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // Get today's date
            String date = new SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
            ).format(new Date());

            // Add weight to database
            long weightId = dbHelper.addWeight(
                    userId,
                    date,
                    weight
            );

            if (weightId != -1) {

                Toast.makeText(
                        MainActivity.this,
                        "Weight added successfully!",
                        Toast.LENGTH_SHORT
                ).show();

                // Send SMS if goal weight acheived
                float goalWeight = dbHelper.getGoalWeight(userId);

                if (weight <= goalWeight) {
                    sendSmsNotification("Congratulations! You reached your goal weight!");
                }

                // Clear input
                currentWeightInput.setText("");

                // Refresh screen
                loadUserInformation();
                loadWeightHistory();

            } else {

                Toast.makeText(
                        MainActivity.this,
                        "Unable to add weight.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }



    // Load user's name, current weight, and goal weight
    private void loadUserInformation() {

        String name = dbHelper.getUserName(userId);
        float currentWeight = dbHelper.getCurrentWeight(userId);
        float goalWeight = dbHelper.getGoalWeight(userId);

        helloUserText.setText("Hello, " + name + "!");

        currentWeightDisplay.setText(
                "Current Weight: " + currentWeight + " lbs"
        );

        goalWeightDisplay.setText(
                "Goal Weight: " + goalWeight + " lbs"
        );
    }

    // Load weight history into GridLayout
    private void loadWeightHistory() {

        // Remove old rows but keep the four headers
        while (weightHistoryGrid.getChildCount() > 5) {
            weightHistoryGrid.removeViewAt(5);
        }

        android.database.Cursor cursor = dbHelper.getWeights(userId);

        if (cursor.moveToFirst()) {

            do {

                long weightId = cursor.getLong(0);
                String date = cursor.getString(1);
                float weight = cursor.getFloat(2);

                addWeightRow(weightId, date, weight);

            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    // User permission request
    private boolean hasSmsPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.SEND_SMS},
                    REQUEST_SMS_PERMISSION
            );

            return false;
        }

        return true;
    }

    // SMS permission callback
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == REQUEST_SMS_PERMISSION) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // SMS permission granted
                Toast.makeText(
                        MainActivity.this,
                        "SMS notifications enabled.",
                        Toast.LENGTH_SHORT
                ).show();

            } else {
                // SMS permission denied
                Toast.makeText(
                        MainActivity.this,
                        "SMS notifications disabled.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    // Menu Navigation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        if (itemId == R.id.menu_home) {

            // Already on Home
            return true;

        } else if (itemId == R.id.menu_profile) {

            Intent intent = new Intent(this, Profile.class);

            intent.putExtra("USER_ID", userId);

            startActivity(intent);

            return true;

        } else if (itemId == R.id.menu_logout) {

            Intent intent = new Intent(this, LogIn.class);

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Add one weight history row
    private void addWeightRow(long weightId, String date, float weight) {

        TextView dateText = new TextView(this);
        dateText.setText(date);
        ContextCompat.getColor(this, R.color.primary_dark);

        TextView weightText = new TextView(this);
        weightText.setText(weight + " lbs");
        ContextCompat.getColor(this, R.color.primary_dark);

        Button editButton = new Button(this);
        editButton.setText("Edit");

        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");

        dateText.setGravity(Gravity.CENTER);
        weightText.setGravity(Gravity.CENTER);

        GridLayout.LayoutParams dateParams =
                new GridLayout.LayoutParams();
        dateParams.columnSpec =
                GridLayout.spec(0, 1, 1f);

        GridLayout.LayoutParams weightParams =
                new GridLayout.LayoutParams();
        weightParams.columnSpec =
                GridLayout.spec(1, 1, 1f);

        GridLayout.LayoutParams editParams =
                new GridLayout.LayoutParams();
        editParams.columnSpec =
                GridLayout.spec(2, 1, 1f);

        GridLayout.LayoutParams deleteParams =
                new GridLayout.LayoutParams();
        deleteParams.columnSpec =
                GridLayout.spec(3, 1, 1f);

        weightHistoryGrid.addView(dateText, dateParams);
        weightHistoryGrid.addView(weightText, weightParams);
        weightHistoryGrid.addView(editButton, editParams);
        weightHistoryGrid.addView(deleteButton, deleteParams);

        // Edit button
        editButton.setOnClickListener(v -> {
            showEditWeightDialog(weightId, date, weight);
        });

        // Delete button
        deleteButton.setOnClickListener(v -> {
            showDeleteConfirmation(weightId);
        });
    }

    // Show edit weight dialog
    private void showEditWeightDialog(
            long weightId,
            String date,
            float currentWeight) {

        EditText weightInput = new EditText(this);
        weightInput.setText(String.valueOf(currentWeight));

        new AlertDialog.Builder(this)
                .setTitle("Edit Weight")
                .setMessage("Enter your updated weight:")
                .setView(weightInput)
                .setPositiveButton("Save", (dialog, which) -> {

                    String weightText =
                            weightInput.getText().toString().trim();

                    if (weightText.isEmpty()) {

                        Toast.makeText(
                                MainActivity.this,
                                "Please enter a weight.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    try {

                        float newWeight =
                                Float.parseFloat(weightText);

                        int rowsUpdated =
                                dbHelper.updateWeight(
                                        weightId,
                                        date,
                                        newWeight
                                );

                        if (rowsUpdated > 0) {

                            Toast.makeText(
                                    MainActivity.this,
                                    "Weight updated.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadUserInformation();
                            loadWeightHistory();

                        }

                    } catch (NumberFormatException e) {

                        Toast.makeText(
                                MainActivity.this,
                                "Please enter a valid weight.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Show delete confirmation
    private void showDeleteConfirmation(long weightId) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Weight")
                .setMessage(
                        "Are you sure you want to delete this weight entry?"
                )
                .setPositiveButton("Delete", (dialog, which) -> {

                    int rowsDeleted =
                            dbHelper.deleteWeight(weightId);

                    if (rowsDeleted > 0) {

                        Toast.makeText(
                                MainActivity.this,
                                "Weight deleted.",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadUserInformation();
                        loadWeightHistory();

                    } else {

                        Toast.makeText(
                                MainActivity.this,
                                "Unable to delete weight.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // SMS Notification
    private void sendSmsNotification(String message) {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED) {

            String phoneNumber = dbHelper.getUserPhone(userId);

            if (phoneNumber != null && !phoneNumber.isEmpty()) {

                SmsManager smsManager = SmsManager.getDefault();

                smsManager.sendTextMessage(
                        phoneNumber,
                        null,
                        message,
                        null,
                        null
                );
            }
        }

    }
}