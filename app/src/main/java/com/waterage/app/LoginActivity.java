package com.waterage.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // আগে থেকেই লগইন করা থাকলে সরাসরি বাকি হিসাব পেজে চলে যাবে
        if (mAuth.getCurrentUser() != null) {
            openBakiHisab();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        progressBar = findViewById(R.id.progressBarLogin);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "জিমেইল এবং পাসওয়ার্ড দিন!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "লগইন সফল হয়েছে!", Toast.LENGTH_SHORT).show();
                        openBakiHisab();
                    } else {
                        String err = (task.getException() != null) ? task.getException().getMessage() : "লগইন ব্যর্থ হয়েছে";
                        Toast.makeText(this, "ত্রুটি: " + err, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.length() < 6) {
            Toast.makeText(this, "সঠিক জিমেইল এবং কমপক্ষে ৬ সংখ্যার পাসওয়ার্ড দিন!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "নতুন অ্যাকাউন্ট তৈরি হয়েছে!", Toast.LENGTH_SHORT).show();
                        openBakiHisab();
                    } else {
                        String err = (task.getException() != null) ? task.getException().getMessage() : "অ্যাকাউন্ট তৈরি ব্যর্থ হয়েছে";
                        Toast.makeText(this, "ত্রুটি: " + err, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openBakiHisab() {
        Intent intent = new Intent(LoginActivity.this, BakiHisabActivity.class);
        startActivity(intent);
        finish();
    }
}
