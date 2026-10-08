package com.payamyar.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class GroupsActivity extends Activity {

    private LinearLayout layoutGroups;

    private Button btnBack;
    private Button btnCreateGroup;

    private String currentUsername = "";

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

        setContentView(
                R.layout.activity_groups
        );

        currentUsername =
                getSharedPreferences(
                        "PayamYar",
                        MODE_PRIVATE
                ).getString(
                        "username",
                        ""
                );

        layoutGroups =
                findViewById(
                        R.id.layoutGroups
                );

        btnBack =
                findViewById(
                        R.id.btnGroupsBack
                );

        btnCreateGroup =
                findViewById(
                        R.id.btnCreateGroup
                );

        btnBack.setOnClickListener(
                v -> finish()
        );

        btnCreateGroup.setOnClickListener(
                v -> showCreateGroupDialog()
        );

        loadGroups();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (layoutGroups != null) {

            loadGroups();
        }
    }

    // =========================================================
    // نمایش گروه‌ها
    // =========================================================

    private void loadGroups() {

        layoutGroups.removeAllViews();

        List<String> groups =
                GroupStorage.getGroupNames(
                        this
                );

        if (groups.isEmpty()) {

            TextView empty =
                    new TextView(this);

            empty.setText(
                    "هنوز گروهی ساخته یا دریافت نشده است"
            );

            empty.setTextSize(
                    17
            );

            empty.setGravity(
                    Gravity.CENTER
            );

            empty.setPadding(
                    20,
                    40,
                    20,
                    40
            );

            layoutGroups.addView(
                    empty
            );

            return;
        }

        for (String groupName : groups) {

            if (groupName == null
                    || groupName.trim().isEmpty()) {

                continue;
            }

            addGroupItem(
                    groupName
            );
        }
    }

    // =========================================================
    // آیتم گروه
    // =========================================================

    private void addGroupItem(
            String groupName
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
                16,
                12,
                16,
                12
        );

        TextView title =
                new TextView(this);

        title.setText(
                "👥  " + groupName
        );

        title.setTextSize(
                18
        );

        title.setTextColor(
                android.graphics.Color.BLACK
        );

        TextView members =
                new TextView(this);

        List<String> memberList =
                GroupStorage.getMembers(
                        this,
                        groupName
                );

        members.setText(
                memberList.size()
                        + " عضو"
        );

        members.setTextSize(
                13
        );

        members.setTextColor(
                android.graphics.Color.DKGRAY
        );

        item.addView(
                title
        );

        item.addView(
                members
        );

        item.setBackgroundResource(
                android.R.drawable
                        .list_selector_background
        );

        item.setOnClickListener(
                v -> openGroup(
                        groupName
                )
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                8,
                5,
                8,
                5
        );

        layoutGroups.addView(
                item,
                params
        );
    }

    // =========================================================
    // ساخت گروه
    // =========================================================

    private void showCreateGroupDialog() {

        EditText input =
                new EditText(this);

        input.setHint(
                "نام گروه"
        );

        input.setSingleLine(
                true
        );

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
                                "ساخت گروه جدید"
                        )
                        .setView(
                                input
                        )
                        .setNegativeButton(
                                "انصراف",
                                null
                        )
                        .setPositiveButton(
                                "ساخت",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                d -> {

                    Button createButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    createButton.setOnClickListener(
                            v -> {

                                String groupName =
                                        input.getText()
                                                .toString()
                                                .trim();

                                if (groupName.isEmpty()) {

                                    input.setError(
                                            "نام گروه را وارد کنید"
                                    );

                                    return;
                                }

                                if (GroupStorage.groupExists(
                                        this,
                                        groupName
                                )) {

                                    input.setError(
                                            "این گروه قبلاً وجود دارد"
                                    );

                                    return;
                                }

                                GroupStorage.createGroup(
                                        this,
                                        groupName,
                                        currentUsername
                                );

                                dialog.dismiss();

                                loadGroups();
                            }
                    );
                }
        );

        dialog.show();
    }

    // =========================================================
    // باز کردن گروه
    // =========================================================

    private void openGroup(
            String groupName
    ) {

        Intent intent =
                new Intent(
                        this,
                        GroupChatActivity.class
                );

        intent.putExtra(
                "groupName",
                groupName
        );

        intent.putExtra(
                "currentUsername",
                currentUsername
        );

        startActivity(
                intent
        );
    }
}
