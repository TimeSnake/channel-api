# Channel

## ChannelMessages

Message: `<channelType>;<messageType>;<identifier>;<value>`

The identifier determines to whom the message refers to.

### ChannelTypes

### Server `server`

Identifier: `<port>`

**MessageTypes** <br>

- `status` - `<offline/starting/online/service/ingame/pregame/postgame>`
- `online_players` - `<number>`
- `max_players` - `<number>`
- `command` - `<cmd>`
- `map` - `<mapName>`
- `password` - `<password>`
- `old_pvp` - `<True/False>`
- `state` - `<custom>`
- `restart` - `<delay>`
- `discord` - `<True/False>`
- `user_stats` - `<gameName>`
- `load_world` - `<worldName>`
- `custom`- `<message>`

### User `user`

Identifier: `<uuid>`

**MessageTypes** <br>

- `status` - `<offline/online/ingame/outgame/pregame/spectator>`
- `service` - `<True/False>`
- `switch_port` - `<port>`
- `switch_server` - `<name>`
- `permission` - _void_
- `punish` - _void_
- `alias` - _void_
- `task` - `<task>`
- `command` - `<cmd>`
- `permGroup` - `<groupName>`
- `team` - `<teamName>`
- `statistics` - `<statisticType`
- `sound` - `<PLING/PLONG>`
- `custom` - `<message>`

### Listener `listener`

Identifier: `<host>`

**MessageTypes** <br>

- `server_port` - `<serverport>`
- `server_message_type` - `<messageType>`
- `register_server` - `<port>`
- `unregister_server` - `<port>`
- `register_host` - _void_
- `unregister_host` - _void_

### Support `support`

Identifier: `<serverPort>`

**MessageTypes** <br>

- `ticket_lock` - `<ticketId>`
- `submit` - `<ticketId>`
- `reject` - `<ticketId>`
- `accept` - `<ticketId>`
- `creation` - `<ticketId>`

### Group `permGroup`

Identifier: `<groupName>`

**MessageTypes** <br>

- `alias` - _void_
- `permission` - _void_

### Ping `ping`

Identifier: `<serverPort>`

**MessageTypes** <br>

- `ping` - _void_
- `pong` - _void_

### Discord `discord`

Identifier: `<categoryName>`

**MessageTypes** <br>

- `destroy_teams` - `[<teamName>]`
- `move_teams` - `<teamName>[.<uuid>][#<teamName>[.<uuid>]]`

