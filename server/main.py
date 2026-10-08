import json
import os

from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware


app = FastAPI(
    title="PayamYar Server"
)


app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"]
)


# =========================================================
# مسیر ذخیره اطلاعات
# =========================================================

DATA_FILE = "payamyar_data.json"


# =========================================================
# اطلاعات آنلاین
# =========================================================

users = {}


# =========================================================
# گروه‌ها
# =========================================================

groups = {}


# =========================================================
# دعوت‌های منتظر
# =========================================================

pending_invites = {}


# =========================================================
# بارگذاری اطلاعات
# =========================================================

def load_data():

    global groups
    global pending_invites

    if not os.path.exists(DATA_FILE):

        groups = {}
        pending_invites = {}

        return

    try:

        with open(
            DATA_FILE,
            "r",
            encoding="utf-8"
        ) as file:

            data = json.load(file)

        saved_groups = data.get(
            "groups",
            {}
        )

        groups = {}

        for group_name, group_data in saved_groups.items():

            groups[group_name] = {
                "creator": group_data.get(
                    "creator",
                    ""
                ),
                "members": set(
                    group_data.get(
                        "members",
                        []
                    )
                )
            }

        pending_invites = data.get(
            "pending_invites",
            {}
        )

    except Exception as e:

        print(
            f"Load data error: {e}"
        )

        groups = {}
        pending_invites = {}


# =========================================================
# ذخیره اطلاعات
# =========================================================

def save_data():

    try:

        data = {
            "groups": {},
            "pending_invites": pending_invites
        }

        for group_name, group_data in groups.items():

            data["groups"][group_name] = {
                "creator": group_data.get(
                    "creator",
                    ""
                ),
                "members": list(
                    group_data.get(
                        "members",
                        set()
                    )
                )
            }

        with open(
            DATA_FILE,
            "w",
            encoding="utf-8"
        ) as file:

            json.dump(
                data,
                file,
                ensure_ascii=False,
                indent=2
            )

    except Exception as e:

        print(
            f"Save data error: {e}"
        )


# =========================================================
# شروع سرور
# =========================================================

load_data()


# =========================================================
# صفحه اصلی
# =========================================================

@app.get("/")
def home():

    return {
        "app": "PayamYar",
        "status": "online"
    }


# =========================================================
# Health
# =========================================================

@app.get("/health")
def health():

    return {
        "status": "ok",
        "online_users": len(users),
        "groups": len(groups),
        "pending_invites": sum(
            len(items)
            for items in pending_invites.values()
        )
    }


# =========================================================
# کاربران آنلاین
# =========================================================

@app.get("/users")
def get_users():

    return {
        "users": list(
            users.keys()
        )
    }


# =========================================================
# گروه‌ها
# =========================================================

@app.get("/groups")
def get_groups():

    result = []

    for group_name, group_data in groups.items():

        result.append({
            "name": group_name,
            "creator": group_data.get(
                "creator",
                ""
            ),
            "members": list(
                group_data.get(
                    "members",
                    set()
                )
            )
        })

    return {
        "groups": result
    }


# =========================================================
# دعوت آفلاین
# =========================================================

def add_pending_invite(
    username,
    group_name,
    added_by
):

    if username not in pending_invites:

        pending_invites[username] = []

    for invite in pending_invites[username]:

        if (
            invite.get("group")
            == group_name
            and
            invite.get("added_by")
            == added_by
        ):

            return

    pending_invites[username].append({
        "group": group_name,
        "added_by": added_by
    })

    save_data()


# =========================================================
# ارسال دعوت‌های قبلی
# =========================================================

async def send_pending_invites(
    username,
    websocket
):

    invites = pending_invites.get(
        username,
        []
    )

    if not invites:
        return

    remaining = []

    for invite in invites:

        group_name = invite.get(
            "group",
            ""
        )

        added_by = invite.get(
            "added_by",
            ""
        )

        if not group_name:
            continue

        try:

            await websocket.send_json({
                "type": "group_added",
                "group": group_name,
                "added_by": added_by
            })

        except Exception:

            remaining.append(
                invite
            )

    if remaining:

        pending_invites[username] = remaining

    else:

        pending_invites.pop(
            username,
            None
        )

    save_data()


# =========================================================
# WebSocket
# =========================================================

@app.websocket("/ws/{username}")
async def websocket_endpoint(
    websocket: WebSocket,
    username: str
):

    await websocket.accept()

    users[username] = websocket

    print(
        f"{username} connected"
    )

    await send_pending_invites(
        username,
        websocket
    )

    try:

        while True:

            data = await websocket.receive_json()

            message_type = data.get(
                "type",
                "message"
            )

            # =================================================
            # پیام خصوصی
            # =================================================

            if message_type == "message":

                target = data.get(
                    "to"
                )

                message = data.get(
                    "message"
                )

                if not target or not message:
                    continue

                if target in users:

                    await users[target].send_json({
                        "type": "message",
                        "from": username,
                        "message": message
                    })

            # =================================================
            # ورود به گروه
            # =================================================

            elif message_type == "join_group":

                group_name = data.get(
                    "group"
                )

                if not group_name:
                    continue

                if group_name not in groups:

                    groups[group_name] = {
                        "creator": username,
                        "members": set()
                    }

                groups[group_name]["members"].add(
                    username
                )

                save_data()

                await websocket.send_json({
                    "type": "group_joined",
                    "group": group_name
                })

            # =================================================
            # افزودن عضو
            # =================================================

            elif message_type == "add_group_member":

                group_name = data.get(
                    "group"
                )

                new_username = data.get(
                    "username"
                )

                if not group_name:
                    continue

                if not new_username:
                    continue

                if group_name not in groups:

                    groups[group_name] = {
                        "creator": username,
                        "members": set()
                    }

                group = groups[group_name]

                group["members"].add(
                    new_username
                )

                save_data()

                # کاربر آنلاین
                if new_username in users:

                    try:

                        await users[
                            new_username
                        ].send_json({
                            "type": "group_added",
                            "group": group_name,
                            "added_by": username
                        })

                    except Exception:

                        add_pending_invite(
                            new_username,
                            group_name,
                            username
                        )

                else:

                    # کاربر آفلاین
                    add_pending_invite(
                        new_username,
                        group_name,
                        username
                    )

                # اطلاع به اعضای آنلاین
                for member in group["members"]:

                    if member == username:
                        continue

                    if member == new_username:
                        continue

                    if member not in users:
                        continue

                    try:

                        await users[
                            member
                        ].send_json({
                            "type": "group_member_added",
                            "group": group_name,
                            "username": new_username
                        })

                    except Exception:
                        pass

            # =================================================
            # پیام گروه
            # =================================================

            elif message_type == "group_message":

                group_name = data.get(
                    "group"
                )

                message = data.get(
                    "message"
                )

                if not group_name:
                    continue

                if not message:
                    continue

                if group_name not in groups:

                    groups[group_name] = {
                        "creator": username,
                        "members": set()
                    }

                group = groups[group_name]

                group["members"].add(
                    username
                )

                save_data()

                members = group[
                    "members"
                ].copy()

                for member in members:

                    if member not in users:
                        continue

                    try:

                        await users[
                            member
                        ].send_json({
                            "type": "group_message",
                            "group": group_name,
                            "from": username,
                            "message": message
                        })

                    except Exception:
                        pass

    except WebSocketDisconnect:

        if username in users:

            del users[username]

        print(
            f"{username} disconnected"
        )

    except Exception as e:

        print(
            f"{username} error: {e}"
        )

        if username in users:

            del users[username]
