package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "WeatherAppPrefs";
    private static final String PREF_LOCATION = "pref_location";
    private static final String PREF_UNIT = "pref_unit";

    private EditText locationEditText;
    private Switch unitSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        locationEditText = findViewById(R.id.locationEditText);
        unitSwitch = findViewById(R.id.unitSwitch);
        Button saveButton = findViewById(R.id.saveButton);

        // Load saved preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        locationEditText.setText(settings.getString(PREF_LOCATION, "Istanbul"));
        unitSwitch.setChecked(settings.getString(PREF_UNIT, "metric").equals("imperial"));

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePreferences(locationEditText.getText().toString(), unitSwitch.isChecked() ? "imperial" : "metric");
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void savePreferences(String location, String unit) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_LOCATION, location);
        editor.putString(PREF_UNIT, unit);
        editor.apply();
    }
}
