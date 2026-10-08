package com.payamyar.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class GroupChatActivity extends Activity {

    private static final String SERVER_URL =
            "wss://payamyar-server.onrender.com/ws/";

    private String currentUsername = "";
    private String groupName = "";

    private OkHttpClient client;
    private WebSocket webSocket;

    private TextView txtGroupName;
    private TextView txtGroupStatus;

    private EditText edtMessage;

    private Button btnBack;
    private Button btnMembers;
    private Button btnSend;

    private ScrollView scrollChat;
    private LinearLayout layoutMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_group_chat
        );

        currentUsername =
                getIntent().getStringExtra(
                        "currentUsername"
                );

        groupName =
                getIntent().getStringExtra(
                        "groupName"
                );

        if (currentUsername == null) {
            currentUsername = "";
        }

        if (groupName == null) {
            groupName = "";
        }

        initViews();
        setupPage();
        loadHistory();
        connectToServer();
    }

    private void initViews() {

        txtGroupName =
                findViewById(
                        R.id.txtGroupName
                );

        txtGroupStatus =
                findViewById(
                        R.id.txtGroupStatus
                );

        edtMessage =
                findViewById(
                        R.id.edtGroupMessage
                );

        btnBack =
                findViewById(
                        R.id.btnGroupBack
                );

        btnMembers =
                findViewById(
                        R.id.btnGroupMembers
                );

        btnSend =
                findViewById(
                        R.id.btnGroupSend
                );

        scrollChat =
                findViewById(
                        R.id.scrollGroupChat
                );

        layoutMessages =
                findViewById(
                        R.id.layoutGroupMessages
                );
    }

    private void setupPage() {

        txtGroupName.setText(
                "👥 " + groupName
        );

        txtGroupStatus.setText(
                "در حال اتصال..."
        );

        btnBack.setOnClickListener(
                v -> finish()
        );

        btnMembers.setOnClickListener(
                v -> showMembers()
        );

        btnSend.setOnClickListener(
                v -> sendMessage()
        );

        edtMessage.setOnEditorActionListener(
                (v, actionId, event) -> {

                    sendMessage();

                    return true;
                }
        );
    }

    private void showMembers() {

        List<String> members =
                GroupStorage.getMembers(
                        this,
                        groupName
                );

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                30,
                10,
                30,
                10
        );

        if (members.isEmpty()) {

            TextView empty =
                    new TextView(this);

            empty.setText(
                    "هنوز عضوی وجود ندارد"
            );

            empty.setTextSize(
                    16
            );

            layout.addView(
                    empty
            );

        } else {

            for (String username : members) {

                TextView member =
                        new TextView(this);

                if (username.equals(
                        currentUsername
                )) {

                    member.setText(
                            "👤 @" + username
                                    + "  (من)"
                    );

                } else {

                    member.setText(
                            "👤 @" + username
                    );
                }

                member.setTextSize(
                        17
                );

                member.setPadding(
                        10,
                        14,
                        10,
                        14
                );

                layout.addView(
                        member
                );
            }
        }

        String creator =
                GroupStorage.getCreator(
                        this,
                        groupName
                );

        TextView creatorText =
                new TextView(this);

        creatorText.setText(
                "\nسازنده: @" + creator
        );

        creatorText.setTextSize(
                14
        );

        creatorText.setTextColor(
                Color.DKGRAY
        );

        layout.addView(
                creatorText
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "👥 اعضای گروه"
                        )
                        .setView(layout)
                        .setNegativeButton(
                                "بستن",
                                null
                        )
                        .setPositiveButton(
                                "＋ افزودن عضو",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                d -> {

                    Button addButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    addButton.setOnClickListener(
                            v -> {

                                dialog.dismiss();

                                showAddMemberDialog();
                            }
                    );
                }
        );

        dialog.show();
    }

    private void showAddMemberDialog() {

        EditText input =
                new EditText(this);

        input.setHint(
                "نام کاربری عضو جدید"
        );

        input.setSingleLine(true);

        int padding =
                (int) (
                        20 *
                        getResources()
                                .getDisplayMetrics()
                                .density
                );

        input.setPadding(
                padding,
                padding,
                padding,
                padding
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "افزودن عضو"
                        )
                        .setView(input)
                        .setNegativeButton(
                                "انصراف",
                                null
                        )
                        .setPositiveButton(
                                "افزودن",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                d -> {

                    Button addButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    addButton.setOnClickListener(
                            v -> {

                                String username =
                                        input.getText()
                                                .toString()
                                                .trim();

                                if (username.isEmpty()) {

                                    input.setError(
                                            "نام کاربری را وارد کنید"
                                    );

                                    return;
                                }

                                if (username.equals(
                                        currentUsername
                                )) {

                                    input.setError(
                                            "شما از قبل عضو گروه هستید"
                                    );

                                    return;
                                }

                                if (GroupStorage.isMember(
                                        this,
                                        groupName,
                                        username
                                )) {

                                    input.setError(
                                            "این کاربر قبلاً عضو گروه است"
                                    );

                                    return;
                                }

                                GroupStorage.addMember(
                                        this,
                                        groupName,
                                        username
                                );

                                sendJoinGroup(
                                        username
                                );

                                dialog.dismiss();

                                Toast.makeText(
                                        this,
                                        "عضو اضافه شد",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                    );
                }
        );

        dialog.show();
    }

    private void sendJoinGroup(
            String username
    ) {

        if (webSocket == null) {
            return;
        }

        try {

            JSONObject json =
                    new JSONObject();

            json.put(
                    "type",
                    "add_group_member"
            );

            json.put(
                    "group",
                    groupName
            );

            json.put(
                    "username",
                    username
            );

            webSocket.send(
                    json.toString()
            );

        } catch (Exception ignored) {
        }
    }

    private void loadHistory() {

        layoutMessages.removeAllViews();

        JSONArray groups =
                GroupStorage.getGroups(
                        this
                );

        try {

            for (int i = 0;
                    i < groups.length();
                    i++) {

                JSONObject group =
                        groups.getJSONObject(i);

                if (!group.optString(
                        "name",
                        ""
                ).equals(groupName)) {

                    continue;
                }

                JSONArray messages =
                        group.optJSONArray(
                                "messages"
                        );

                if (messages == null
                        || messages.length() == 0) {

                    addSystemMessage(
                            "شروع گفت‌وگو در گروه"
                    );

                    return;
                }

                for (int j = 0;
                        j < messages.length();
                        j++) {

                    JSONObject item =
                            messages.getJSONObject(j);

                    String username =
                            item.optString(
                                    "username",
                                    ""
                            );

                    String message =
                            item.optString(
                                    "message",
                                    ""
                            );

                    boolean mine =
                            username.equals(
                                    currentUsername
                            );

                    addBubble(
                            username,
                            message,
                            mine
                    );
                }

                scrollToBottom();

                return;
            }

        } catch (Exception ignored) {
        }

        addSystemMessage(
                "شروع گفت‌وگو در گروه"
        );
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

                                    txtGroupStatus.setText(
                                            "آنلاین"
                                    );

                                    joinGroup();
                                });
                            }

                            @Override
                            public void onMessage(
                                    WebSocket webSocket,
                                    String text
                            ) {

                                runOnUiThread(
                                        () ->
                                                receiveMessage(
                                                        text
                                                )
                                );
                            }

                            @Override
                            public void onFailure(
                                    WebSocket webSocket,
                                    Throwable t,
                                    Response response
                            ) {

                                runOnUiThread(
                                        () ->
                                                txtGroupStatus.setText(
                                                        "اتصال قطع شد"
                                                )
                                );
                            }

                            @Override
                            public void onClosed(
                                    WebSocket webSocket,
                                    int code,
                                    String reason
                            ) {

                                runOnUiThread(
                                        () ->
                                                txtGroupStatus.setText(
                                                        "آفلاین"
                                                )
                                );
                            }
                        }
                );
    }

    private void joinGroup() {

        if (webSocket == null
                || groupName.isEmpty()) {
            return;
        }

        try {

            JSONObject json =
                    new JSONObject();

            json.put(
                    "type",
                    "join_group"
            );

            json.put(
                    "group",
                    groupName
            );

            webSocket.send(
                    json.toString()
            );

        } catch (Exception ignored) {
        }
    }

    private void receiveMessage(
            String text
    ) {

        try {

            JSONObject json =
                    new JSONObject(text);

            String type =
                    json.optString(
                            "type",
                            ""
                    );

            if (type.equals(
                    "group_joined"
            )) {

                return;
            }

            if (!type.equals(
                    "group_message"
            )) {

                return;
            }

            String group =
                    json.optString(
                            "group",
                            ""
                    );

            if (!group.equals(
                    groupName
            )) {
                return;
            }

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

            if (!from.equals(
                    currentUsername
            )) {

                GroupStorage.saveMessage(
                        this,
                        groupName,
                        from,
                        message
                );
            }

            addBubble(
                    from,
                    message,
                    from.equals(
                            currentUsername
                    )
            );

        } catch (Exception ignored) {
        }
    }

    private void sendMessage() {

        String message =
                edtMessage.getText()
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
                    "type",
                    "group_message"
            );

            json.put(
                    "group",
                    groupName
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

            GroupStorage.saveMessage(
                    this,
                    groupName,
                    currentUsername,
                    message
            );

            addBubble(
                    currentUsername,
                    message,
                    true
            );

            edtMessage.setText("");

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "خطا در ارسال پیام",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void addBubble(
            String username,
            String message,
            boolean mine
    ) {

        if (message == null
                || message.trim().isEmpty()) {

            return;
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
                LinearLayout.VERTICAL
        );

        row.setGravity(
                mine
                        ? Gravity.END
                        : Gravity.START
        );

        TextView sender =
                new TextView(this);

        sender.setText(
                mine
                        ? "من"
                        : "@" + username
        );

        sender.setTextSize(
                12
        );

        sender.setTextColor(
                Color.DKGRAY
        );

        sender.setPadding(
                8,
                2,
                8,
                2
        );

        TextView bubble =
                new TextView(this);

        bubble.setText(
                message
        );

        bubble.setTextSize(
                16
        );

        bubble.setTextColor(
                Color.BLACK
        );

        bubble.setPadding(
                18,
                12,
                18,
                12
        );

        bubble.setBackgroundResource(
                mine
                        ? R.drawable.bubble_me
                        : R.drawable.bubble_other
        );

        LinearLayout.LayoutParams bubbleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        bubbleParams.setMargins(
                6,
                2,
                6,
                8
        );

        row.addView(
                sender
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

    private void addSystemMessage(
            String message
    ) {

        TextView text =
                new TextView(this);

        text.setText(
                message
        );

        text.setTextSize(
                15
        );

        text.setGravity(
                Gravity.CENTER
        );

        text.setPadding(
                20,
                30,
                20,
                30
        );

        layoutMessages.addView(
                text
        );
    }

    private void scrollToBottom() {

        scrollChat.post(
                () ->
                        scrollChat.fullScroll(
                                ScrollView.FOCUS_DOWN
                        )
        );
    }

    @Override
    protected void onDestroy() {

        if (webSocket != null) {

            webSocket.close(
                    1000,
                    "Group closed"
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
