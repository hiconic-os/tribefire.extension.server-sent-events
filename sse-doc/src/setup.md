# Setup and Configuration

##  Setup

In order to use Server-Sent Events in your setup, you have to include the artifact:

`tribefire.extension.server-sent-events:sse-aggregator`

The initializer of this module will take of everything else and `PushRequest`s will automatically be offered to registered clients.


### Configuration Properties


For more information about the properties, see [Runtime Properties](asset://tribefire.cortex.documentation:concepts-doc/features/runtime_properties.md).

| Property           | Description      | Default      |
| :------------- | :----     | :----      |
| SSE_RETRY_S         | If set, this will be the number of seconds sent to the client for the retry mechanism of SSE. If not set, no value will be sent. The client will use the default of 3 seconds.  | `null` |
| SSE_MAX_CONNECTION_TTL_MS         | The maximum time a connection to the client should be kept open. After this time, the connection will be cut, regardless of the actual usage. This is a security measure to drop dead connections eventually. Live clients will re-connect automatically and will not loose any messages due to the Last-Seen-Id. When this is not set, the default of 1 hour applies.  | `null` |
| SSE_BLOCK_TIMEOUT_MS         | The time the endpoint should wait for PushRequests. When this time is exceeded without a PushRequest being received, a `ping` message will be sent to the client and the endpoint resumes waiting for PushRequests. If this is not set, a default of 30 seconds applies.       | `null` |
| SSE_STORAGE_SIZE         | The number of `PushRequest` events should be stored in memory. This helps clients to resume a connection (using the `Last-Event-ID` header) without loosing messages in the meantime.      | 256 |

