# Usage

Server-Sent Events are an HTML 5 standard and thus well documented. 

[Mozilla provides a good source](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events) on how to support SSE on the backend and the frontend.


On the client side, an `EventSource` can be created, using the `Server-Sent Events Endpoint`:


```javascript
const evtSource = new EventSource("https://localhost:8443/tribefire-services/component/sse");
```

This will create a new connection to the server and will also keep it open. When the browser detects that the connection has been closed, it will automatically create a new one after a specific timeout (usually 3 seconds).

Please note that you need a valid session, either included in the request as a `tfsessionid` cookie or as an additional parameter:

```javascript
const evtSource = new EventSource("https://localhost:8443/tribefire-services/component/sse?sessionId=....");
```

It is also possible to provide the following parameters:

| Name         | Description       |
| :------------- | :----      |
| `clientId`          | Any meaningful identification of the connecting client which can be used to address push requests to certain clients.  |
| `pushChannelId`   | Unique identifier of the client's connection. |


For more information on the addressing of push notifications, please refer to [Push Requests](asset://tribefire.cortex.documentation:concepts-doc/features/push_requests.md)

Based on die event source, you can subscribe to multiple types of events:

| Type         | Description       |
| :------------- | :----      |
| `ping`          | This is the standard event that will be sent in intervals to show the client that the connection is still open and also to keep the connection from being closed due to idleness by network components between client and server. |
| `PushRequest`   | This event will contain the JSON-encoded payload of a PushRequest |


This is an example of how to subscribe for these events:

```javascript
evtSource.addEventListener("ping", function (event) {
	const time = JSON.parse(event.data).serverTimeUTC;
	alert("ping at " + time + ": " + event.data);
});
evtSource.addEventListener("PushRequest", function (event) {
	alert("PushRequest: " + event.data);
});
```

The client can also subscribe for other/generic messages:

```javascript
evtSource.onmessage = function (event) {
	alert("Message: " + event.data);
}
```

and for errors:

```javascript
evtSource.onerror = function (err) {
	console.error("EventSource failed:", err);
};
```

Here the complete demo example:

```html
<!DOCTYPE html>
<html>

<head>
    <meta charset="utf-8"/>
    <title>Server-sent Events Example</title>
    <script type="text/javascript">

        var evtSource;

        function start() {
            let baseUrl = document.getElementById("baseUrl").value;
            let sessionId = document.getElementById("sessionId").value;
            let clientId = document.getElementById("clientId").value;
            var fullUrl = baseUrl + "?sessionId=" + sessionId;
            if (clientId) {
                fullUrl += "&clientId="+clientId;
            }
            evtSource = new EventSource(fullUrl);

            let status = document.getElementById('status');
            status.textContent = "Listening to "+fullUrl;

            evtSource.onmessage = function (event) {
                const newElement = document.createElement("li");
                const eventList = document.getElementById("list");

                newElement.textContent = "message: " + event.data;
                eventList.appendChild(newElement);
            }
            evtSource.addEventListener("ping", function (event) {
                const newElement = document.createElement("li");
                const eventList = document.getElementById("list");
                const time = JSON.parse(event.data).serverTimeUTC;
                newElement.textContent = "ping at " + time + ": " + event.data;
                eventList.appendChild(newElement);
            });
            evtSource.addEventListener("PushRequest", function (event) {
                const newElement = document.createElement("li");
                const eventList = document.getElementById("list");
                newElement.textContent = "PushRequest: " + event.data;
                eventList.appendChild(newElement);
            });
            evtSource.onerror = function (err) {
                console.error("EventSource failed:", err);
            };

        }
        function stop() {
            if (evtSource) {
                evtSource.close();
            }
            evtSource = null;

            let status = document.getElementById('status');
            status.textContent = "Stopped listening";
        }
        function clearList() {
            const eventList = document.getElementById("list");
            while(eventList.firstChild) eventList.removeChild(eventList.firstChild);
        }
    </script>
</head>

<body>
    Please enter the Session ID and click on "Start".
    <br />
    <br />

    Base URL: <input type="text" placeholder="https://localhost:8443/tribefire-services/component/sse" id="baseUrl" value="https://localhost:8443/tribefire-services/component/sse" size="70"/>
    <br />
    <br />
    Session ID: <input type="text" placeholder="Session Id " id="sessionId" size="50"/>
    <br />
    <br />
    Client ID: <input type="text" placeholder="Client Id " id="clientId" size="50"/>
    <br />
    <br />

    <input type="button" onclick="start();" name="start" value="start" />
    <input type="button" onclick="stop();" name="stop" value="stop" />
    <input type="button" onclick="clearList();" name="clear" value="clear" />

    <p id="status" />

    <ul id="list">
    </ul>
</body>

</html>
```
