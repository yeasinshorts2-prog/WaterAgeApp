package com.waterage.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Android 13 বা তার পরের ভার্সনের জন্য নোটিফিকেশন পারমিশন চাওয়া
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        LinearLayout btnWaterAlarm = findViewById(R.id.btnWaterAlarm);
        LinearLayout btnAgeCalc = findViewById(R.id.btnAgeCalc);

        btnWaterAlarm.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WaterAlarmActivity.class);
            startActivity(intent);
        });

        btnAgeCalc.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AgeCalculatorActivity.class);
            startActivity(intent);
        });
    }
}