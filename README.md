# Arena Coach — Fabric 1.21.11

A client-side PvP practice HUD for Minecraft Java Edition 1.21.11. It works in singleplayer and when joining multiplayer servers; **the server does not need this client-side mod installed**.

## Features

- Toggleable practice HUD
- Manual target lock on the entity under the crosshair
- Local glow outline for the locked entity
- Target name and distance
- Simple combo counter based on the target's hurt animation
- Attack cooldown readiness indicator
- Basic critical-hit timing hint
- No auto-attacks, no automated aiming, and no attack cooldown bypass

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
4. The output will be `build\libs\arena-coach-1.0.0.jar`.
5. Copy that JAR into `%APPDATA%\.minecraft\mods`.
6. Launch the **Fabric 1.21.11** profile. Install Fabric API in the same `mods` folder as well.

The first build needs an internet connection so Gradle can download Minecraft/Fabric build dependencies. The project has not been compiled in this offline generation environment, so treat the first local build as a verification step.

## Notes

- This is a client-side HUD/training aid. It does not change server combat rules or force critical hits.
- Multiplayer servers may have rules against client-side target highlighting. Follow the server's rules.
- Target highlighting is a local visual hint; it does not move your camera or attack the target.
