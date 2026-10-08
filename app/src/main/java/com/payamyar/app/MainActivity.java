package com.payamyar.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    EditText edtName;
    EditText edtUsername;
    EditText edtPassword;
    Button btnRegister;
    TextView txtLogin;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("PayamYar", MODE_PRIVATE);

        // اگر قبلاً ثبت‌نام شده باشد
        if (prefs.getBoolean("registered", false)) {
            showLoginPage();
        } else {
            showRegisterPage();
        }
    }

    private void showRegisterPage() {
        setContentView(R.layout.activity_register);

        edtName = findViewById(R.id.edtName);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLogin);

        btnRegister.setOnClickListener(v -> {

            String name = edtName.getText().toString().trim();
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "همه قسمت‌ها را پر کنید", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit()
                    .putBoolean("registered", true)
                    .putString("name", name)
                    .putString("username", username)
                    .putString("password", password)
                    .apply();

            Toast.makeText(this, "ثبت‌نام با موفقیت انجام شد", Toast.LENGTH_SHORT).show();

            showLoginPage();
        });

        txtLogin.setOnClickListener(v -> showLoginPage());
    }

    private void showLoginPage() {
        setContentView(R.layout.activity_login);

        EditText username = findViewById(R.id.edtLoginUsername);
        EditText password = findViewById(R.id.edtLoginPassword);
        Button login = findViewById(R.id.btnLogin);
        TextView register = findViewById(R.id.txtRegister);

        login.setOnClickListener(v -> {

            String savedUsername = prefs.getString("username", "");
            String savedPassword = prefs.getString("password", "");

            String enteredUsername = username.getText().toString().trim();
            String enteredPassword = password.getText().toString().trim();

            if (enteredUsername.equals(savedUsername)
                    && enteredPassword.equals(savedPassword)) {

                Toast.makeText(this, "ورود موفق بود", Toast.LENGTH_SHORT).show();

                // فعلاً بعد از ورود همین صفحه اصلی را نشان می‌دهیم
                setContentView(R.layout.activity_main);

            } else {
                Toast.makeText(this, "نام کاربری یا رمز عبور اشتباه است",
                        Toast.LENGTH_SHORT).show();
            }
        });

        register.setOnClickListener(v -> showRegisterPage());
    }
}