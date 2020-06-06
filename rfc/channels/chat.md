## Chat

### Types
#### Channel
```
{
    "type": String ["chat"],
    "room": String
}
```

#### ChatMessage
```
{
    "text": String,
    "timestamp": String,
    "id": String?,            // for receive only: uid
    "type": ChatMessageType?, // for receive only
    "from": String?           // for receive only: name of message owner
}
```

#### ChatMessageType
```
["system"/"broadcast"] // system - message from system, 
                       // broadcast - this message has sent to all users
                       // Can be null
```

#### Reaction
```
{
    "name": String,
    "count": Int,
    "icon_url": String,
    "description: String
}
```

### Send a message to server

```
{
    "channel": Channel,
    "message": ChatMessage
}
```

### Receive a message from server

```
{
    "channel": Channel,
    "message": ChatMessage,
    "reactions": Array[Reaction]
}
```