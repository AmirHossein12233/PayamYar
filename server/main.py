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
        "online_users": len(users)
    }


@app.get("/users")
def get_users():
    return {
        "users": list(users.keys())
    }


@app.websocket("/ws/{username}")
async def websocket_endpoint(websocket: WebSocket, username: str):

    await websocket.accept()

    users[username] = websocket

    print(f"{username} connected")

    try:
        while True:

            data = await websocket.receive_json()

            target = data.get("to")
            message = data.get("message")

            if not target or not message:
                continue

            if target in users:

                await users[target].send_json({
                    "type": "message",
                    "from": username,
                    "message": message
                })

    except WebSocketDisconnect:

        if username in users:
            del users[username]

        print(f"{username} disconnected")