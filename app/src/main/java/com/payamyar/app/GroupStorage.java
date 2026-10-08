package com.payamyar.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GroupStorage {

    private static final String PREF_NAME =
            "PayamYarGroups";

    private static final String GROUPS_KEY =
            "groups";

    public static void createGroup(
            Context context,
            String groupName,
            String creator
    ) {

        if (groupName == null
                || groupName.trim().isEmpty()
                || creator == null
                || creator.trim().isEmpty()) {
            return;
        }

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        JSONArray groups =
                getGroups(context);

        try {

            // جلوگیری از ساخت گروه تکراری
            for (int i = 0;
                    i < groups.length();
                    i++) {

                JSONObject oldGroup =
                        groups.getJSONObject(i);

                if (oldGroup.optString(
                        "name",
                        ""
                ).equalsIgnoreCase(
                        groupName.trim()
                )) {

                    return;
                }
            }

            JSONObject group =
                    new JSONObject();

            group.put(
                    "name",
                    groupName.trim()
            );

            group.put(
                    "creator",
                    creator
            );

            JSONArray members =
                    new JSONArray();

            members.put(
                    creator
            );

            group.put(
                    "members",
                    members
            );

            group.put(
                    "messages",
                    new JSONArray()
            );

            groups.put(
                    group
            );

            saveGroups(
                    context,
                    groups
            );

        } catch (Exception ignored) {
        }
    }

    public static JSONArray getGroups(
            Context context
    ) {

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        String data =
                prefs.getString(
                        GROUPS_KEY,
                        "[]"
                );

        try {

            return new JSONArray(data);

        } catch (Exception e) {

            return new JSONArray();
        }
    }

    public static boolean groupExists(
            Context context,
            String groupName
    ) {

        JSONArray groups =
                getGroups(context);

        try {

            for (int i = 0;
                    i < groups.length();
                    i++) {

                JSONObject group =
                        groups.getJSONObject(i);

                if (group.optString(
                        "name",
                        ""
                ).equals(groupName)) {

                    return true;
                }
            }

        } catch (Exception ignored) {
        }

        return false;
    }

    public static void addMember(
            Context context,
            String groupName,
            String username
    ) {

        if (groupName == null
                || username == null
                || groupName.trim().isEmpty()
                || username.trim().isEmpty()) {

            return;
        }

        JSONArray groups =
                getGroups(context);

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

                JSONArray members =
                        group.optJSONArray(
                                "members"
                        );

                if (members == null) {

                    members =
                            new JSONArray();

                    group.put(
                            "members",
                            members
                    );
                }

                for (int j = 0;
                        j < members.length();
                        j++) {

                    if (members.optString(j)
                            .equals(username)) {

                        saveGroups(
                                context,
                                groups
                        );

                        return;
                    }
                }

                members.put(
                        username
                );

                saveGroups(
                        context,
                        groups
                );

                return;
            }

        } catch (Exception ignored) {
        }
    }

    public static boolean isMember(
            Context context,
            String groupName,
            String username
    ) {

        JSONArray groups =
                getGroups(context);

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

                JSONArray members =
                        group.optJSONArray(
                                "members"
                        );

                if (members == null) {
                    return false;
                }

                for (int j = 0;
                        j < members.length();
                        j++) {

                    if (members.optString(j)
                            .equals(username)) {

                        return true;
                    }
                }
            }

        } catch (Exception ignored) {
        }

        return false;
    }

    public static List<String> getMembers(
            Context context,
            String groupName
    ) {

        List<String> result =
                new ArrayList<>();

        JSONArray groups =
                getGroups(context);

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

                JSONArray members =
                        group.optJSONArray(
                                "members"
                        );

                if (members == null) {
                    return result;
                }

                for (int j = 0;
                        j < members.length();
                        j++) {

                    String username =
                            members.optString(
                                    j,
                                    ""
                            );

                    if (!username.isEmpty()) {

                        result.add(
                                username
                        );
                    }
                }

                return result;
            }

        } catch (Exception ignored) {
        }

        return result;
    }

    public static String getCreator(
            Context context,
            String groupName
    ) {

        JSONArray groups =
                getGroups(context);

        try {

            for (int i = 0;
                    i < groups.length();
                    i++) {

                JSONObject group =
                        groups.getJSONObject(i);

                if (group.optString(
                        "name",
                        ""
                ).equals(groupName)) {

                    return group.optString(
                            "creator",
                            ""
                    );
                }
            }

        } catch (Exception ignored) {
        }

        return "";
    }

    public static void saveMessage(
            Context context,
            String groupName,
            String username,
            String message
    ) {

        if (message == null
                || message.trim().isEmpty()) {
            return;
        }

        JSONArray groups =
                getGroups(context);

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

                if (messages == null) {

                    messages =
                            new JSONArray();

                    group.put(
                            "messages",
                            messages
                    );
                }

                JSONObject item =
                        new JSONObject();

                item.put(
                        "username",
                        username
                );

                item.put(
                        "message",
                        message
                );

                item.put(
                        "time",
                        System.currentTimeMillis()
                );

                messages.put(
                        item
                );

                saveGroups(
                        context,
                        groups
                );

                return;
            }

        } catch (Exception ignored) {
        }
    }

    public static List<String> getGroupNames(
            Context context
    ) {

        List<String> result =
                new ArrayList<>();

        JSONArray groups =
                getGroups(context);

        try {

            for (int i = 0;
                    i < groups.length();
                    i++) {

                JSONObject group =
                        groups.getJSONObject(i);

                String name =
                        group.optString(
                                "name",
                                ""
                        );

                if (!name.isEmpty()) {

                    result.add(
                            name
                    );
                }
            }

        } catch (Exception ignored) {
        }

        return result;
    }

    private static void saveGroups(
            Context context,
            JSONArray groups
    ) {

        context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        )
                .edit()
                .putString(
                        GROUPS_KEY,
                        groups.toString()
                )
                .apply();
    }
}
