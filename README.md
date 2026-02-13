# RPS Multiplayer Server

This is a simple multiplayer Rock-Paper-Scissors server over TCP.

## Requirements
- Java 21

## Quick Start (Copy/Paste)

1) Build the project:
```
./gradlew clean build
```

2) Run the server:
```
./gradlew bootRun
```

3) Connect from a new terminal:
```
telnet localhost 5555
```

If `telnet` is not installed on macOS, use:
```
nc localhost 5555
```

## How to Play
1) Enter a nickname (up to 100 characters).
2) Wait for an opponent.
3) Choose:
   - `1` / `Rock`
   - `2` / `Scissors`
   - `3` / `Paper`

## Session Restore
If you disconnect, reconnect within the configured window and use:
```
SESSION <id>
```
The server prints your `SESSION <id>` after login.
