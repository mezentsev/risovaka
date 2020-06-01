## Chat

### Send

#### 1. Message to server
Message to server
%%(json)
{
	"id": String,			// required
	"message": String,		// required
	"timestamp": String		// required
}
%%

### Receive 

#### 1. Message from server
Message from server
%%(json)
{
	"id": String,								// required
	"message": String,							// required
	"timestamp": String,						// required
	"type": String [system/broadcast/silent], 	// required: system - message for all from system, broadcast - for all from single author, silent - only for you
	"from": String,								// required: name of message owner
	"reactions": Array[Reaction]

}
%%

Reaction
%%(json)
{
	"name": String,					// required
	"count": Int					// required
	"description: String,			// required
	"icon_url": String				// required
}
%%