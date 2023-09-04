# channel-api

This module is part of the plugin-project and won't run without
the [root module](https://git.timesnake.de/timesnake/plugin-root-project) [1].

This library provides a tcp socket communication between arbitrary many hosts.
A proxy manages initialization. Each member can listen to defined messages by registering at the proxy,
which stores and forwards them to regarding members.

## ChannelMessages

Message: `<channelType>;<messageType>;<identifier>;<value>`

The channel type organises messages into groups, like server, player or group related messages.
It also defines the identifier data type.
The message type defines the specific type of message. This also defines the value data type of the message.
The identifier determines to whom the message refers to.

## Exception Handling

Each message is being tried to send 3 times.
If there is no connection existing, the client tries to set up one (three times).
Furthermore, by default, the channel manager sends a heartbeat ping message to all connected servers and waits for a
pong resposne.
If no response is being received, the connection is being closed and deleted.

## Code Style

The code style guide can be found in the plugin root project [1].

## License

- The source is licensed under the GNU GPLv2 license that can be found in the [LICENSE](LICENSE)
  file.

[1] https://git.timesnake.de/timesnake/plugin-root-project