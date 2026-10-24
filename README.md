# Arena Coach — Fabric 1.21.11

A client-side PvP practice HUD for Minecraft Java Edition 1.21.11. It can be used in single-player and while joining multiplayer servers; the server does not need this client-side HUD installed.

## Features

- Toggleable PvP practice HUD
- Manual target lock on the living entity under the crosshair
- Local glow outline for the locked target
- Target name, health, absorption, and distance
- Approximate melee practice-range indicator (3 blocks; server settings can differ)
- Combo counter based on observed target hurt animation
- Critical-hit timing hint
- Your current health display with a low-health warning
- No attack-cooldown meter in the HUD
- No auto-attacks, automated aiming, forced critical hits, or attack-cooldown bypass

## Controls

| Key | Action |
|---|---|
| `M` | Enable/disable Arena Coach |
| `N` | Lock the entity currently under your crosshair (when enabled) |
| `\\` | Clear the locked target |

You can change these in **Options → Controls → Key Binds → Arena Coach**.

## Requirements

- Minecraft Java Edition **1.21.11**
- Fabric Loader **0.18.4 or newer compatible with 1.21.11**
- Fabric API **0.141.1+1.21.11**
- Java 21
- Gradle 9.x to build from source

## Build the mod JAR on Windows

1. Install Java 21 and Gradle 9.x, and make sure `java` and `gradle` work in Command Prompt.
2. Extract this project ZIP to a normal folder.
3. Double-click `build.bat` (or run `gradle --no-daemon clean build` in this folder).
4. The output will be `build\\libs\\arena-coach-1.0.0.jar`.
5. Copy that JAR into `%APPDATA%\\.minecraft\\mods`.
6. Launch the **Fabric 1.21.11** profile. Install Fabric API in the same `mods` folder as well.

The GitHub Actions build workflow compiles the project and uploads the JAR as a workflow artifact. Check the Actions tab for the latest build result.

## Important limitations

- This is a client-side practice HUD. It does not change server combat rules, remove Minecraft's actual attack cooldown, or force critical hits.
- The 3-block range is only a practice estimate; server reach settings and latency can differ.
- Flight in survival requires server-side support/permission. A client-only HUD cannot safely grant survival flight on arbitrary multiplayer servers. Creative/spectator flight follows Minecraft's normal permissions.
- Multiplayer servers may have rules about target highlighting. Follow the server's rules.
