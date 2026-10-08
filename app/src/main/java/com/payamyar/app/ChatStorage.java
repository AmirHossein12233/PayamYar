package com.payamyar.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatStorage {

    private static final String PREF_NAME =
            "PayamYarChats";

    private static final String CHAT_LIST_KEY =
            "chat_list";

    public static void saveMessage(
            Context context,
            String user1,
            String user2,
            String message
    ) {

        if (user1 == null
                || user2 == null
                || user1.isEmpty()
                || user2.isEmpty()
                || message == null
                || message.isEmpty()) {

            return;
        }

        String chatKey =
                makeKey(
                        user1,
                        user2
                );

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        String old =
                prefs.getString(
                        chatKey,
                        "[]"
                );

        try {

            JSONArray messages =
                    new JSONArray(old);

            JSONObject item =
                    new JSONObject();

            item.put(
                    "message",
                    message
            );

            messages.put(item);

            prefs.edit()
                    .putString(
                            chatKey,
                            messages.toString()
                    )
                    .apply();

            addChatToList(
                    context,
                    user1,
                    user2
            );

        } catch (Exception ignored) {
        }
    }

    public static String loadMessages(
            Context context,
            String user1,
            String user2
    ) {

        String chatKey =
                makeKey(
                        user1,
                        user2
                );

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        String data =
                prefs.getString(
                        chatKey,
                        "[]"
                );

        try {

            JSONArray messages =
                    new JSONArray(data);

            StringBuilder result =
                    new StringBuilder();

            for (int i = 0;
                 i < messages.length();
                 i++) {

                JSONObject item =
                        messages.getJSONObject(i);

                String message =
                        item.optString(
                                "message",
                                ""
                        );

                if (message.isEmpty()) {
                    continue;
                }

                if (result.length() > 0) {
                    result.append("\n\n");
                }

                result.append(message);
            }

            return result.toString();

        } catch (Exception e) {

            return "";
        }
    }

    public static String getLastMessage(
            Context context,
            String user1,
            String user2
    ) {

        String chatKey =
                makeKey(
                        user1,
                        user2
                );

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        String data =
                prefs.getString(
                        chatKey,
                        "[]"
                );

        try {

            JSONArray messages =
                    new JSONArray(data);

            if (messages.length() == 0) {
                return "";
            }

            JSONObject last =
                    messages.getJSONObject(
                            messages.length() - 1
                    );

            return last.optString(
                    "message",
                    ""
            );

        } catch (Exception e) {

            return "";
        }
    }

    public static List<String> getRecentChats(
            Context context,
            String currentUsername
    ) {

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        Set<String> savedChats =
                prefs.getStringSet(
                        CHAT_LIST_KEY,
                        new HashSet<>()
                );

        List<String> result =
                new ArrayList<>();

        for (String chat : savedChats) {

            String[] users =
                    chat.split(
                            "\\|",
                            -1
                    );

            if (users.length != 2) {
                continue;
            }

            String otherUser;

            if (users[0].equals(
                    currentUsername
            )) {

                otherUser =
                        users[1];

            } else if (users[1].equals(
                    currentUsername
            )) {

                otherUser =
                        users[0];

            } else {

                continue;
            }

            if (!otherUser.isEmpty()) {

                result.add(
                        otherUser
                );
            }
        }

        Collections.sort(
                result
        );

        return result;
    }

    private static void addChatToList(
            Context context,
            String user1,
            String user2
    ) {

        String chat =
                makeKey(
                        user1,
                        user2
                );

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        Set<String> oldChats =
                prefs.getStringSet(
                        CHAT_LIST_KEY,
                        new HashSet<>()
                );

        Set<String> chats =
                new HashSet<>(
                        oldChats
                );

        chats.add(chat);

        prefs.edit()
                .putStringSet(
                        CHAT_LIST_KEY,
                        chats
                )
                .apply();
    }

    private static String makeKey(
            String user1,
            String user2
    ) {

        if (user1.compareTo(user2) < 0) {

            return user1
                    + "|"
                    + user2;

        } else {

            return user2
                    + "|"
                    + user1;
        }
    }
}
