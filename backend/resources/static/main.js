// Global variable to hold the websocket.
var socket = null;

/**
 * This function is in charge of connecting the client.
 */
function connect() {
    // First we create the socket.
    // The socket will be connected automatically asap. Not now but after returning to the event loop,
    // so we can register handlers safely before the connection is performed.
    console.log("Begin connect");
    socket = new WebSocket("ws://" + window.location.host + "/ws");

    // We set a handler that will be executed if the socket has any kind of unexpected error.
    // Since this is a just sample, we only report it at the console instead of making more complex things.
    socket.onerror = function() {
        console.log("socket error");
    };

    // We set a handler upon connection.
    // What this does is to put a text in the messages container notifying about this event.
    socket.onopen = function() {
        showText("Connected", true);
    };

    // If the connection was closed gracefully (either normally or with a reason from the server),
    // we have this handler to notify to the user via the messages container.
    // Also we will retry a connection after 5 seconds.
    socket.onclose = function(evt) {
        // Try to gather an explanation about why this was closed.
        var explanation = "";
        if (evt.reason && evt.reason.length > 0) {
            explanation = "reason: " + evt.reason;
        } else {
            explanation = "without a reason specified";
        }

        // Notify the user using the messages container.
        write("Disconnected with close code " + evt.code + " and " + explanation);
        // Try to reconnect after 5 seconds.
        setTimeout(connect, 5000);
    };

    // If we receive a message from the server, we want to handle it.
    socket.onmessage = function(event) {
        received(event.data.toString());
    };
}

/**
 * Handle messages received from the sever.
 *
 * @param message The textual message
 */
function received(jsonString) {
    // Out only logic upon message receiving is to output in the messages container to notify the user.
    write(jsonString);
}

/**
 * Writes a message in the HTML 'messages' container that the user can see.
 *
 * @param message The message to write in the container
 */
function write(jsonString) {
    var message = JSON.parse(jsonString);
    switch (message.channel.type) {
        case "chat":
            handleChat(message);
            break;
        case "user_settings":
            handleUserSettings(message);
            break;
        default:
            console.log("Can't handle " + message);
    }
}

function handleChat(message) {
    var from = message.from ? message.from : message.type;
    showText("[" + from + "] " + message.text, message.type == "SYSTEM")
}

function handleUserSettings(message) {
    switch (message.action) {
        case "rename":
            document.cookie = "USER_SETTINGS=" + encodeURIComponent("name=#s" + message.name);
            break;
        default:
            console.log("Can't handle action " + message.action);
    }
}

function showText(text, isBold) {
    var line = document.createElement("p");
    line.className = isBold ? "message-bold" : "message";
    line.textContent = text

    var messagesDiv = document.getElementById("messages");
    messagesDiv.appendChild(line);
    messagesDiv.scrollTop = line.offsetTop;
}

/**
 * Function in charge of sending the 'commandInput' text to the server via the socket.
 */
function onSend() {
    var input = document.getElementById("commandInput");
    // Validates that the input exists
    if (input) {
        var text = input.value;
        // Validates that there is a text and that the socket exists
        if (text && socket) {
            var data = JSON.stringify({
                "channel": {
                    "type": "chat",
                    "room": "test_room_id"
                },
                "message": {
                    "timestamp": Date.now(),
                    "text": text
                }
            });
            // Sends the text
            socket.send(data);
            // Clears the input so the user can type a new command or text to say
            input.value = "";
        }
    }
}

/**
 * The initial code to be executed once the page has been loaded and is ready.
 */
function start() {
    // First, we should connect to the server.
    connect();

    // If we click the sendButton, let's send the message.
    document.getElementById("sendButton").onclick = onSend;
    // If we pressed the 'enter' key being inside the 'commandInput', send the message to improve accessibility and making it nicer.
    document.getElementById("commandInput").onkeydown = function(e) {
        if (e.keyCode == 13) {
            onSend();
        }
    };
}

/**
 * The entry point of the client.
 */
function initLoop() {
    // Is the sendButton available already? If so, start. If not, let's wait a bit and rerun this.
    if (document.getElementById("sendButton")) {
        start();
    } else {
        setTimeout(initLoop, 300);
    }
}

// This is the entry point of the client.
initLoop();