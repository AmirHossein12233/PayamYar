from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(title="PayamYar Server")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

users = {}

groups = {}


@app.get("/")
def home():
    return {
        "app": "PayamYar",
        "status": "online"
    }


@app.get("/health")
def health():
    return {
        "status": "ok",
        "online_users": len(users),
        "groups": len(groups)
    }


@app.get("/users")
def get_users():
    return {
        "users": list(users.keys())
    }


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

    try:

        while True:

            data = await websocket.receive_json()

            message_type = data.get(
                "type",
                "message"
            )

            # ==============================
            # پیام خصوصی
            # ==============================

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

            # ==============================
            # ورود به گروه
            # ==============================

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

                await websocket.send_json({
                    "type": "group_joined",
                    "group": group_name
                })

            # ==============================
            # اضافه کردن عضو
            # ==============================

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

                # اطلاع به عضو جدید
                if new_username in users:

                    await users[
                        new_username
                    ].send_json({
                        "type": "group_added",
                        "group": group_name,
                        "added_by": username
                    })

                # اطلاع به اعضای آنلاین
                for member in group["members"]:

                    if member == username:
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

            # ==============================
            # پیام گروه
            # ==============================

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

                members = group["members"].copy()

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
