package com.payamyar.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ProfileActivity extends Activity {

    private TextView txtProfileName;
    private TextView txtProfileUsername;

    private Button btnProfileBack;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_profile
        );

        txtProfileName =
                findViewById(
                        R.id.txtProfileName
                );

        txtProfileUsername =
                findViewById(
                        R.id.txtProfileUsername
                );

        btnProfileBack =
                findViewById(
                        R.id.btnProfileBack
                );

        btnLogout =
                findViewById(
                        R.id.btnLogout
                );

        String name =
                getSharedPreferences(
                        "PayamYar",
                        MODE_PRIVATE
                ).getString(
                        "name",
                        "کاربر"
                );

        String username =
                getSharedPreferences(
                        "PayamYar",
                        MODE_PRIVATE
                ).getString(
                        "username",
                        ""
                );

        txtProfileName.setText(
                name
        );

        txtProfileUsername.setText(
                "@" + username
        );

        btnProfileBack.setOnClickListener(
                v -> finish()
        );

        btnLogout.setOnClickListener(v -> {

            getSharedPreferences(
                    "PayamYar",
                    MODE_PRIVATE
            )
                    .edit()
                    .putBoolean(
                            "registered",
                            false
                    )
                    .apply();

            Intent intent =
                    new Intent(
                            this,
                            MainActivity.class
                    );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK
                    );

            startActivity(intent);

            finish();
        });
    }
}
