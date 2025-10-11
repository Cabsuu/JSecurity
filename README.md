# JSecurity

JSecurity is a security plugin for Minecraft servers.

## Features

- Ban, TempBan, IPBan, Mute, TempMute, Kick, Warn
- Player profiles and punishment history
- Alt account detection
- Private messaging
- Chat control (delay and keyword replacement)
- Vanish

## Commands

- `/jsecurity` (alias: `/js`)
  - `/js help` - Shows the help message.
  - `/js reload` - Reloads the configuration.
  - `/js record [page] [-sort]` - Shows player records.
  - `/js profile <player>` - Shows a player's profile.
  - `/js log [page]` - Shows the punishment log.
  - `/js history <player> [page]` - Shows a player's punishment history.
  - `/js note <player> <note|-clear>` - Adds a note to a player's profile, or clears all notes.
- `/message <player> <message>` (alias: `/msg`) - Sends a private message to a player.
- `/reply <message>` (alias: `/r`) - Replies to the last private message.
- `/socialspy` - Toggles social spy.
- `/vanish` (alias: `/v`) - Toggles vanish mode.

## Permissions

- `jsecurity.*` - Gives access to all jSecurity commands.
- `jsecurity.admin` - Allows access to jSecurity admin commands.
- `jsecurity.help` - Allows using the `/js help` command.
- `jsecurity.record` - Allows using the `/js record` command.
- `jsecurity.profile` - Allows using the `/js profile` command.
- `jsecurity.log` - Allows using the `/js log` command.
- `jsecurity.history` - Allows using the `/js history` command.
- `jsecurity.note` - Allows using the `/js note` command.
- `jsecurity.ban` - Allows banning players.
- `jsecurity.tempban` - Allows temporarily banning players.
- `jsecurity.ipban` - Allows IP banning players.
- `jsecurity.unban` - Allows unbanning players.
- `jsecurity.mute` - Allows muting players.
- `jsecurity.tempmute` - Allows temporarily muting players.
- `jsecurity.unmute` - Allows unmuting players.
- `jsecurity.kick` - Allows kicking players.
- `jsecurity.warn` - Allows warning players.
- `jsecurity.alt.alert` - Notifies staff on possible alt account join.
- `jsecurity.socialspy` - Allows toggling social spy.
- `jsecurity.socialspy.view` - Allows viewing private messages with social spy.
- `jsecurity.chat.delay.bypass` - Allows bypassing the chat delay.
- `jsecurity.vanish` - Allows toggling vanish mode.
- `jsecurity.vanish.see` - Allows seeing vanished players.
- `jsecurity.vanish.interact` - Allows interacting with players while vanished.
- `jsecurity.vanish.silentjoin` - Allows joining the server silently while vanished.

## Configuration

The `config.yml` file contains the following options:

- `chat-delay.enabled`: (true/false) - Enables or disables the chat delay.
- `chat-delay.period`: (number) - The delay in seconds between messages.
- `keyword-replacement.enabled`: (true/false) - Enables or disables keyword replacement.
- `keyword-replacement.words`: (map) - A map of replacement words to a list of keywords to be replaced.