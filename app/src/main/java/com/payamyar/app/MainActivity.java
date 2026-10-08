package com.payamyar.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends Activity {

    private static final String HTTP_SERVER_URL =
            "https://payamyar-server.onrender.com";

    private String currentUsername = "";

    private TextView txtTitle;
    private TextView txtSelectedUser;
    private TextView txtMessages;

    private LinearLayout layoutUsers;
    private LinearLayout layoutRecentChats;

    private Button btnUsers;
    private Button btnProfile;
    private Button btnGroups;

    private OkHttpClient httpClient;

    private boolean homePageShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        httpClient =
                new OkHttpClient();

        currentUsername =
                getSharedPreferences(
                        "PayamYar",
                        MODE_PRIVATE
                ).getString(
                        "username",
                        ""
                );

        if (currentUsername.isEmpty()) {
            showLoginPage();
        } else {
            showHomePage();
        }
    }

    private void showLoginPage() {

        setContentView(
                R.layout.activity_login
        );

        Button btnLogin =
                findViewById(
                        R.id.btnLogin
                );

        Button btnGoRegister =
                findViewById(
                        R.id.btnGoRegister
                );

        btnLogin.setOnClickListener(
                v -> login()
        );

        btnGoRegister.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    this,
                                    RegisterActivity.class
                            );

                    startActivity(intent);
                }
        );
    }

    private void login() {

        String username =
                ((android.widget.EditText)
                        findViewById(
                                R.id.edtLoginUsername
                        ))
                        .getText()
                        .toString()
                        .trim();

        String password =
                ((android.widget.EditText)
                        findViewById(
                                R.id.edtLoginPassword
                        ))
                        .getText()
                        .toString()
                        .trim();

        if (username.isEmpty()
                || password.isEmpty()) {

            Toast.makeText(
                    this,
                    "نام کاربری و رمز عبور را وارد کنید",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        android.content.SharedPreferences prefs =
                getSharedPreferences(
                        "PayamYar",
                        MODE_PRIVATE
                );

        String savedUsername =
                prefs.getString(
                        "username",
                        ""
                );

        String savedPassword =
                prefs.getString(
                        "password",
                        ""
                );

        if (username.equals(savedUsername)
                && password.equals(savedPassword)) {

            prefs.edit()
                    .putBoolean(
                            "registered",
                            true
                    )
                    .apply();

            currentUsername =
                    username;

            showHomePage();

        } else {

            Toast.makeText(
                    this,
                    "نام کاربری یا رمز عبور اشتباه است",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void showHomePage() {

        setContentView(
                R.layout.activity_main
        );

        homePageShown = true;

        txtTitle =
                findViewById(
                        R.id.txtTitle
                );

        txtSelectedUser =
                findViewById(
                        R.id.txtSelectedUser
                );

        txtMessages =
                findViewById(
                        R.id.txtMessages
                );

        layoutUsers =
                findViewById(
                        R.id.layoutUsers
                );

        layoutRecentChats =
                findViewById(
                        R.id.layoutRecentChats
                );

        btnUsers =
                findViewById(
                        R.id.btnUsers
                );

        btnProfile =
                findViewById(
                        R.id.btnProfile
                );

        btnGroups =
                findViewById(
                        R.id.btnGroups
                );

        txtTitle.setText(
                "پیام‌یار"
        );

        txtSelectedUser.setText(
                "👤 @" + currentUsername
        );

        txtMessages.setText(
                "یک گفتگو را انتخاب کنید"
        );

        btnUsers.setOnClickListener(
                v -> loadUsers()
        );

        btnProfile.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    this,
                                    ProfileActivity.class
                            );

                    startActivity(intent);
                }
        );

        btnGroups.setOnClickListener(
                v -> openGroups()
        );

        loadRecentChats();

        loadUsers();
    }

    private void openGroups() {

        Intent intent =
                new Intent(
                        this,
                        GroupsActivity.class
                );

        startActivity(intent);
    }

    private void loadRecentChats() {

        if (layoutRecentChats == null) {
            return;
        }

        layoutRecentChats.removeAllViews();

        List<String> recentChats =
                ChatStorage.getRecentChats(
                        this,
                        currentUsername
                );

        if (recentChats.isEmpty()) {

            TextView empty =
                    new TextView(this);

            empty.setText(
                    "هنوز گفتگویی ندارید"
            );

            empty.setTextSize(
                    16
            );

            empty.setGravity(
                    Gravity.CENTER
            );

            empty.setPadding(
                    20,
                    30,
                    20,
                    30
            );

            layoutRecentChats.addView(
                    empty
            );

            return;
        }

        for (String username : recentChats) {

            addRecentChatItem(
                    username
            );
        }
    }

    private void addRecentChatItem(
            String username
    ) {

        LinearLayout item =
                new LinearLayout(this);

        item.setOrientation(
                LinearLayout.VERTICAL
        );

        item.setGravity(
                Gravity.CENTER_VERTICAL
        );

        item.setPadding(
                18,
                14,
                18,
                14
        );

        item.setBackgroundResource(
                android.R.drawable
                        .list_selector_background
        );

        TextView title =
                new TextView(this);

        title.setText(
                "👤  @" + username
        );

        title.setTextSize(
                18
        );

        title.setTextColor(
                android.graphics.Color.BLACK
        );

        TextView lastMessage =
                new TextView(this);

        String message =
                ChatStorage.getLastMessage(
                        this,
                        currentUsername,
                        username
                );

        if (message == null
                || message.isEmpty()) {

            message =
                    "گفتگو";
        }

        if (message.startsWith("من:")) {

            message =
                    message.substring(3)
                            .trim();
        }

        if (message.startsWith("@")) {

            int colon =
                    message.indexOf(":");

            if (colon >= 0
                    && colon + 1 < message.length()) {

                message =
                        message.substring(
                                colon + 1
                        ).trim();
            }
        }

        lastMessage.setText(
                message
        );

        lastMessage.setTextSize(
                14
        );

        lastMessage.setTextColor(
                android.graphics.Color.DKGRAY
        );

        lastMessage.setMaxLines(
                1
        );

        item.addView(
                title
        );

        item.addView(
                lastMessage
        );

        item.setOnClickListener(
                v -> openPrivateChat(
                        username
                )
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                1,
                0,
                1
        );

        layoutRecentChats.addView(
                item,
                params
        );
    }

    private void loadUsers() {

        if (currentUsername.isEmpty()) {
            return;
        }

        Request request =
                new Request.Builder()
                        .url(
                                HTTP_SERVER_URL
                                        + "/users"
                        )
                        .get()
                        .build();

        httpClient
                .newCall(request)
                .enqueue(
                        new Callback() {

                            @Override
                            public void onFailure(
                                    Call call,
                                    IOException e
                            ) {

                                runOnUiThread(
                                        () ->
                                                Toast.makeText(
                                                        MainActivity.this,
                                                        "اتصال به سرور برقرار نشد",
                                                        Toast.LENGTH_SHORT
                                                ).show()
                                );
                            }

                            @Override
                            public void onResponse(
                                    Call call,
                                    Response response
                            ) throws IOException {

                                if (!response.isSuccessful()
                                        || response.body() == null) {

                                    return;
                                }

                                String body =
                                        response.body()
                                                .string();

                                runOnUiThread(
                                        () ->
                                                showUsers(
                                                        body
                                                )
                                );
                            }
                        }
                );
    }

    private void showUsers(
            String body
    ) {

        try {

            JSONObject object =
                    new JSONObject(body);

            JSONArray users =
                    object.optJSONArray(
                            "users"
                    );

            if (users == null) {
                users =
                        new JSONArray();
            }

            layoutUsers.removeAllViews();

            boolean foundOtherUser =
                    false;

            for (int i = 0;
                    i < users.length();
                    i++) {

                String username =
                        users.getString(i);

                if (username.equals(
                        currentUsername
                )) {

                    continue;
                }

                foundOtherUser = true;

                addUserButton(
                        username
                );
            }

            if (!foundOtherUser) {

                TextView empty =
                        new TextView(this);

                empty.setText(
                        "کاربر دیگری آنلاین نیست"
                );

                empty.setTextSize(
                        15
                );

                empty.setPadding(
                        16,
                        12,
                        16,
                        12
                );

                layoutUsers.addView(
                        empty
                );
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "خطا در دریافت کاربران",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void addUserButton(
            String username
    ) {

        Button button =
                new Button(this);

        button.setText(
                "👤 @" + username
        );

        button.setTextSize(
                15
        );

        button.setOnClickListener(
                v -> openPrivateChat(
                        username
                )
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                5,
                2,
                5,
                2
        );

        layoutUsers.addView(
                button,
                params
        );
    }

    private void openPrivateChat(
            String username
    ) {

        if (username == null
                || username.isEmpty()) {

            return;
        }

        Intent intent =
                new Intent(
                        this,
                        ChatActivity.class
                );

        intent.putExtra(
                "currentUsername",
                currentUsername
        );

        intent.putExtra(
                "targetUsername",
                username
        );

        startActivity(
                intent
        );
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (homePageShown
                && layoutRecentChats != null) {

            loadRecentChats();

            loadUsers();
        }
    }

    @Override
    protected void onDestroy() {

        if (httpClient != null) {

            httpClient.dispatcher()
                    .executorService()
                    .shutdown();

            httpClient = null;
        }

        super.onDestroy();
    }
}
