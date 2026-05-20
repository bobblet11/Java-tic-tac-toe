# Java Tic-Tac-Toe

<p align="center">
  <img src="https://raw.githubusercontent.com/bobblet11/Java-tic-tac-toe/main/images/readme/playthrough.gif" alt="Gameplay" width="500">
</p>

## Overview

This Java project implements a networked Tic-Tac-Toe game using a Swing-based GUI and a simple TCP socket server. Two players connect to the server and play in real time, with the server relaying game messages between clients.

## Features

- Java Swing graphical user interface
- Two-player multiplayer over TCP sockets
- Server-based message relay for game synchronization
- Restart and disconnect handling
- `.env` support for configurable server settings

## Repository Structure

- `Client.java` - starts the client, loads configuration, and launches the game GUI
- `Server.java` - accepts incoming connections and manages client threads
- `ServerWorker.java` - relays client messages and coordinates gameplay state
- `GameGUI.java` - creates the Tic-Tac-Toe interface and handles user interaction
- `EnvLoader.java` - loads environment variables from a `.env` file
- `images/` - contains GIF assets for empty, cross, and naught tiles

## Requirements

- Java 17 or newer
- Terminal / command prompt or Java-capable IDE

## Setup

create a `.env` file in the project root to override defaults:

```env
SERVER_IP=127.0.0.1
SERVER_PORT=6000
```

If `.env` is missing, the client defaults to `127.0.0.1:6000`, and the server defaults to port `6000`.

## Running the Project

### Start the server

```powershell
javac Server.java ServerWorker.java EnvLoader.java
java Server
```

### Start the first client

```powershell
javac Client.java GameGUI.java EnvLoader.java
java Client
```

### Start the second client

```powershell
java Client
```

## How to Play

1. Enter a player name in the client GUI.
2. Wait for the second player to connect.
3. Take turns clicking on empty board tiles.
4. The game ends when one player wins or when the board is full.
5. Choose whether to restart after the round completes.

## Notes

- Only two clients are supported per game session.
- Additional connection attempts are rejected while a game is active.
- Closing the client window sends a disconnect notice to the remaining player.

## License

This project is provided for educational use.
