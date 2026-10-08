package com.payamyar.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatActivity extends Activity {

    private static final String SERVER_URL =
            "wss://payamyar-server.onrender.com/ws/";

    private String currentUsername = "";
    private String targetUsername = "";

    private OkHttpClient client;
    private WebSocket webSocket;

    private TextView txtChatUser;
    private TextView txtChatStatus;

    private EditText edtChatMessage;

    private ScrollView scrollChat;

    private LinearLayout layoutMessages;

    private Button btnBack;
    private Button btnChatSend;
    private Button btnCall;
    private Button btnVideo;
    private Button btnAttach;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_chat
        );

        currentUsername =
                getIntent().getStringExtra(
                        "currentUsername"
                );

        targetUsername =
                getIntent().getStringExtra(
                        "targetUsername"
                );

        if (currentUsername == null) {
            currentUsername = "";
        }

        if (targetUsername == null) {
            targetUsername = "";
        }

        initViews();

        setupPage();

        loadHistory();

        connectToServer();
    }

    private void initViews() {

        txtChatUser =
                findViewById(
                        R.id.txtChatUser
                );

        txtChatStatus =
                findViewById(
                        R.id.txtChatStatus
                );

        edtChatMessage =
                findViewById(
                        R.id.edtChatMessage
                );

        scrollChat =
                findViewById(
                        R.id.scrollChat
                );

        layoutMessages =
                findViewById(
                        R.id.layoutMessages
                );

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        btnChatSend =
                findViewById(
                        R.id.btnChatSend
                );

        btnCall =
                findViewById(
                        R.id.btnCall
                );

        btnVideo =
                findViewById(
                        R.id.btnVideo
                );

        btnAttach =
                findViewById(
                        R.id.btnAttach
                );
    }

    private void setupPage() {

        txtChatUser.setText(
                "@" + targetUsername
        );

        txtChatStatus.setText(
                "در حال اتصال..."
        );

        btnBack.setOnClickListener(
                v -> finish()
        );

        btnChatSend.setOnClickListener(
                v -> sendMessage()
        );

        btnCall.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "تماس صوتی در مرحله بعد",
                    Toast.LENGTH_SHORT
            ).show();

        });

        btnVideo.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "تماس تصویری در مرحله بعد",
                    Toast.LENGTH_SHORT
            ).show();

        });

        btnAttach.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "ارسال عکس و فایل در مرحله بعد",
                    Toast.LENGTH_SHORT
            ).show();

        });

        edtChatMessage.setOnEditorActionListener(
                (v, actionId, event) -> {

                    sendMessage();

                    return true;
                }
        );
    }

    private void loadHistory() {

        String history =
                ChatStorage.loadMessages(
                        this,
                        currentUsername,
                        targetUsername
                );

        layoutMessages.removeAllViews();

        if (history == null
                || history.isEmpty()) {

            TextView empty =
                    new TextView(this);

            empty.setText(
                    "شروع گفت‌وگو با @"
                            + targetUsername
            );

            empty.setTextSize(16);

            empty.setGravity(
                    Gravity.CENTER
            );

            empty.setPadding(
                    20,
                    30,
                    20,
                    30
            );

            layoutMessages.addView(
                    empty
            );

        } else {

            String[] messages =
                    history.split(
                            "\\n\\n"
                    );

            for (String message : messages) {

                if (!message.trim().isEmpty()) {

                    if (message.startsWith(
                            "من:"
                    )) {

                        addMyBubble(
                                message.substring(
                                        3
                                ).trim()
                        );

                    } else {

                        addOtherBubble(
                                message
                        );
                    }
                }
            }
        }

        scrollToBottom();
    }

    private void connectToServer() {

        if (currentUsername.isEmpty()) {
            return;
        }

        client =
                new OkHttpClient.Builder()
                        .readTimeout(
                                0,
                                TimeUnit.MILLISECONDS
                        )
                        .build();

        Request request =
                new Request.Builder()
                        .url(
                                SERVER_URL
                                        + currentUsername
                        )
                        .build();

        webSocket =
                client.newWebSocket(
                        request,
                        new WebSocketListener() {

                            @Override
                            public void onOpen(
                                    WebSocket webSocket,
                                    Response response
                            ) {

                                runOnUiThread(() -> {

                                    txtChatStatus.setText(
                                            "آنلاین"
                                    );
                                });
                            }

                            @Override
                            public void onMessage(
                                    WebSocket webSocket,
                                    String text
                            ) {

                                runOnUiThread(() -> {

                                    receiveMessage(
                                            text
                                    );
                                });
                            }

                            @Override
                            public void onFailure(
                                    WebSocket webSocket,
                                    Throwable t,
                                    Response response
                            ) {

                                runOnUiThread(() -> {

                                    txtChatStatus.setText(
                                            "اتصال قطع شد"
                                    );
                                });
                            }

                            @Override
                            public void onClosed(
                                    WebSocket webSocket,
                                    int code,
                                    String reason
                            ) {

                                runOnUiThread(() -> {

                                    txtChatStatus.setText(
                                            "آفلاین"
                                    );
                                });
                            }
                        }
                );
    }

    private void receiveMessage(
            String text
    ) {

        try {

            JSONObject json =
                    new JSONObject(text);

            String from =
                    json.optString(
                            "from",
                            ""
                    );

            String message =
                    json.optString(
                            "message",
                            ""
                    );

            if (message.isEmpty()) {
                return;
            }

            String formatted =
                    "@" + from
                            + ": "
                            + message;

            ChatStorage.saveMessage(
                    this,
                    currentUsername,
                    from,
                    formatted
            );

            addOtherBubble(
                    formatted
            );

        } catch (Exception e) {

            addOtherBubble(text);
        }
    }

    private void sendMessage() {

        String message =
                edtChatMessage
                        .getText()
                        .toString()
                        .trim();

        if (message.isEmpty()) {
            return;
        }

        if (webSocket == null) {

            Toast.makeText(
                    this,
                    "به سرور متصل نیستید",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try {

            JSONObject json =
                    new JSONObject();

            json.put(
                    "to",
                    targetUsername
            );

            json.put(
                    "message",
                    message
            );

            boolean sent =
                    webSocket.send(
                            json.toString()
                    );

            if (!sent) {

                Toast.makeText(
                        this,
                        "ارسال پیام ناموفق بود",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            String formatted =
                    "من: " + message;

            ChatStorage.saveMessage(
                    this,
                    currentUsername,
                    targetUsername,
                    formatted
            );

            addMyBubble(
                    message
            );

            edtChatMessage.setText("");

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "خطا در ارسال پیام",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void addMyBubble(
            String message
    ) {

        addBubble(
                message,
                true
        );
    }

    private void addOtherBubble(
            String message
    ) {

        addBubble(
                message,
                false
        );
    }

    private void addBubble(
            String message,
            boolean mine
    ) {

        if (message == null
                || message.trim().isEmpty()) {
            return;
        }

        /*
         * اگر پیام دریافتی با نام کاربر شروع شده،
         * فقط متن پیام را داخل حباب قرار می‌دهیم.
         */

        String displayText =
                message;

        if (!mine
                && displayText.startsWith("@")) {

            int colon =
                    displayText.indexOf(":");

            if (colon >= 0
                    && colon + 1
                    < displayText.length()) {

                displayText =
                        displayText
                                .substring(
                                        colon + 1
                                )
                                .trim();
            }
        }

        LinearLayout row =
                new LinearLayout(this);

        row.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                mine
                        ? Gravity.END
                        : Gravity.START
        );

        TextView bubble =
                new TextView(this);

        bubble.setText(
                displayText
        );

        bubble.setTextSize(16);

        bubble.setTextColor(
                Color.BLACK
        );

        bubble.setGravity(
                Gravity.CENTER_VERTICAL
        );

        bubble.setPadding(
                18,
                12,
                18,
                12
        );

        if (mine) {

            bubble.setBackgroundResource(
                    R.drawable.bubble_me
            );

        } else {

            bubble.setBackgroundResource(
                    R.drawable.bubble_other
            );
        }

        LinearLayout.LayoutParams bubbleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        bubbleParams.setMargins(
                6,
                5,
                6,
                5
        );

        bubble.setMaxWidth(
                (int) (
                        getResources()
                                .getDisplayMetrics()
                                .widthPixels
                                * 0.78
                )
        );

        row.addView(
                bubble,
                bubbleParams
        );

        layoutMessages.addView(
                row
        );

        scrollToBottom();
    }

    private void scrollToBottom() {

        scrollChat.post(
                () -> scrollChat.fullScroll(
                        ScrollView.FOCUS_DOWN
                )
        );
    }

    @Override
    protected void onDestroy() {

        if (webSocket != null) {

            webSocket.close(
                    1000,
                    "Chat closed"
            );

            webSocket = null;
        }

        if (client != null) {

            client.dispatcher()
                    .executorService()
                    .shutdown();

            client = null;
        }

        super.onDestroy();
    }
}
