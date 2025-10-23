# JSecurity

JSecurity is a comprehensive security and administration plugin for Minecraft servers, designed to give you fine-grained control over your community and protect your server from malicious actors.

## Features

-   **Authentication:** Secure your server by requiring players to register and log in with a password. This is essential for offline-mode (cracked) servers.
    -   Customizable password complexity (length, uppercase, numbers, symbols).
    -   Session-based reconnection to allow players to rejoin without logging in again.
    -   Configurable effects for unauthenticated players (blindness, spectator mode, teleport to spawn).
-   **Punishment System:** A full suite of commands to manage player behavior.
    -   Ban, TempBan, IPBan, Mute, TempMute, Kick, and Warn commands.
    -   Silent punishments (`-s` flag) to hide broadcasts from regular players.
    -   Detailed punishment history for each player (`/js history`).
-   **Player Administration:**
    -   In-depth player profiles with UUID, IP history, and join dates (`/js profile`).
    -   Ability to add and clear persistent notes on player profiles (`/js note`).
-   **Chat Management:**
    -   **Staff Chat:** A private channel for staff members to communicate (`/staffchat` or `/sc`).
    -   **Keyword Replacement:** Automatically replace configurable keywords or phrases in chat.
    -   **Chat Filter:** Block configurable keywords or phrases in chat.
    -   **Clear Chat:** Clear the chat for all players (`/clearchat`).
    -   **Chat Delay:** Prevent spam by setting a cooldown between player messages.
    -   **Private Messaging:** Send private messages (`/msg`) and reply (`/r`) to conversations.
    -   **Social Spy:** Monitor private messages sent on the server (`/socialspy`).
-   **Vanish System:**
    -   Become invisible to other players (`/vanish` or `/v`).
    -   Silent join and quit options to avoid revealing a staff member's presence.
    -   Prevent interaction and mob targeting while vanished.
-   **Security & Alerts:**
    -   **Ban Evasion Detection:** Automatically ban players who join using the same IP as a previously banned account.
    -   **Alt Account Detection:** Receive alerts when a new player joins with an IP that has been used by another account.

## Commands

-   `/jsecurity` (alias: `/js`)
    -   `/js help` - Shows the help message.
    -   `/js reload` - Reloads the configuration.
    -   `/js record [page] [-sort]` - Shows player records.
    -   `/js profile <player>` - Shows a player's profile.
    -   `/js log [page]` - Shows the punishment log.
    -   `/js history <player> [page]` - Shows a player's punishment history.
    -   `/js note <player> <note|-clear>` - Adds a note to a player's profile, or clears all notes.
    -   `/js unregister <player>` - Unregisters a player from the authentication system.
-   `/staffchat <message>` (alias: `/sc`) - Sends a message to the staff chat.
    -   `/sc toggle` - Toggles continuous staff chat mode.
-   `/message <player> <message>` (alias: `/msg`) - Sends a private message to a player.
-   `/reply <message>` (alias: `/r`) - Replies to the last private message.
-   `/socialspy` - Toggles social spy.
-   `/vanish` (alias: `/v`) - Toggles vanish mode.
-   `/register <password> <confirmPassword>` - Registers a new player.
-   `/login <password>` - Logs in a registered player.
-   `/unregister <password>` - Unregisters yourself.
-   `/changepass <oldPassword> <newPassword>` - Changes your password.
-   `/clearchat` - Clears the chat.

## Permissions

-   `jsecurity.*` - Gives access to all jSecurity commands.
-   `jsecurity.admin` - Allows access to jSecurity admin commands like `/js reload`.
-   `jsecurity.help` - Allows using the `/js help` command.
-   `jsecurity.record` - Allows using the `/js record` command.
-   `jsecurity.profile` - Allows using the `/js profile` command.
-   `jsecurity.log` - Allows using the `/js log` command.
-   `jsecurity.history` - Allows using the `/js history` command.
-   `jsecurity.note` - Allows using the `/js note` command.
-   `jsecurity.ban` - Allows banning players.
-   `jsecurity.tempban` - Allows temporarily banning players.
-   `jsecurity.ipban` - Allows IP banning players.
-   `jsecurity.unban` - Allows unbanning players.
-   `jsecurity.mute` - Allows muting players.
-   `jsecurity.tempmute` - Allows temporarily muting players.
-   `jsecurity.unmute` - Allows unmuting players.
-   `jsecurity.kick` - Allows kicking players.
-   `jsecurity.warn` - Allows warning players.
-   `jsecurity.alt.alert` - Notifies staff on possible alt account join.
-   `jsecurity.socialspy` - Allows toggling social spy.
-   `jsecurity.socialspy.view` - Allows viewing private messages with social spy.
-   `jsecurity.socialspy.exempt` - Exempts a player from being spied on.
-   `jsecurity.chat.delay.bypass` - Allows bypassing the chat delay.
-   `jsecurity.replaceword.bypass` - Allows bypassing the keyword replacement.
-   `jsecurity.chatfilter.bypass` - Allows bypassing the chat filter.
-   `jsecurity.clearchat` - Allows clearing the chat.
-   `jsecurity.staffchat` - Allows using the staff chat.
-   `jsecurity.vanish` - Allows toggling vanish mode.
-   `jsecurity.vanish.see` - Allows seeing vanished players.
-   `jsecurity.vanish.interact` - Allows interacting with players while vanished.
-   `jsecurity.vanish.attack` - Allows attacking players while vanished.
-   `jsecurity.vanish.silentjoin` - Allows joining the server silently while vanished.
-   `jsecurity.vanish.silentquit` - Allows leaving the server silently while vanished.

## Configuration

The `config.yml` file is where you can customize the plugin's behavior. The `messages.yml` file allows you to change any user-facing message.

### `config.yml`

```yaml
# Default durations for temporary punishments.
# Format: 1d2h3m4s (d=days, h=hours, m=minutes, s=seconds)
default-durations:
  tempban: "12h"
  tempmute: "12h"

# If true, players joining with the same IP as a banned player will also be banned.
prevent-ban-evasion: false

# Announce when a new player joins the server.
announce-new-player: true
# Announce only when the total player count reaches one of these milestones.
# Leave empty to announce every new player.
# Example: [100, 200, 500, 1000]
announce-milestones: []

# If true, staff will be alerted when a player joins with the same IP as another player.
alt-account-alert: false

# Chat Delay
chat-delay:
  enabled: false
  period: 3 # Delay in seconds

# Keyword Replacement
keyword-replacement:
  enabled: false
  words:
    "example": ["word1", "word2"]

# A list of commands that are blocked for muted players.
muted-command-restriction:
  - "msg"
  - "tell"

# Authentication System (for offline-mode servers)
authentication:
  enabled: false
  min-password-length: 8
  max-password-length: 16
  password-must-contain:
    uppercase-letter: false
    number: false
    symbol: false
  join-at-spawn: true
  return-at-location: false
  join-on-blind: false
  join-on-spectator: false
  session-reconnection: false
  session-limit-timer: "6h"

# Database Configuration
database:
  type: "sqlite" # or "mysql"
  host: "localhost"
  port: 3306
  name: "jsecurity"
  user: "username"
  password: "password"
```

All messages are fully customizable in `messages.yml`.