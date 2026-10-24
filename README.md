# Arena Coach — Fabric 1.21.11

Arena Coach combines a client-side PvP practice HUD with a permission-gated server flight command. The HUD works in single-player and on multiplayer servers. Survival flight works only in worlds/servers where the mod is loaded server-side and the player has permission to run commands.

## PvP practice HUD

- Toggleable HUD with manual target lock and local target glow
- Target name, health, absorption, and distance
- Approximate melee practice-range indicator (3 blocks; server settings can differ)
- Combo counter based on the target's observed hurt animation
- Critical-hit timing hint
- Your current health with a low-health warning
- Attack-cooldown meter removed from the HUD
- No auto-attacks, automated aiming, forced critical hits, or attack-cooldown bypass

## Controls

| Key | Action |
|---|---|
| `M` | Enable/disable the practice HUD |
| `N` | Lock the entity under your crosshair |
| `\\` | Clear the locked target |

Change these in **Options → Controls → Key Binds → Arena Coach**.

## Flight (server-authorized)

Use `/arenacoach flight` to toggle flight if you have permission to run operator-level commands.

- **Single-player:** enable cheats/commands, or open the world to LAN with commands enabled, then run the command.
- **Your own multiplayer server:** install the mod on both the client and the server, restart the server, and give your account permission to use operator-level commands.
- **Someone else's server:** the command will not work unless the server owner installs/supports the mod and grants permission.
- The mod preserves whether flight was already allowed when you toggle it off. Flight permission is not persisted across server restarts.

## Requirements

- Minecraft Java Edition **1.21.11**
- Fabric Loader **0.18.4 or newer compatible with 1.21.11**
- Fabric API **0.141.1+1.21.11**
- Java 21
- Gradle 9.x to build from source

## Build on Windows

1. Install Java 21 and Gradle 9.x.
2. Run `gradle --no-daemon clean build` in the project folder (or use `build.bat`).
3. The JAR is written to `build\\libs\\arena-coach-1.0.0.jar`.
4. Install the JAR in `%APPDATA%\\.minecraft\\mods` for your client.
5. For survival flight on a multiplayer server, install the same JAR in the server's `mods` folder too. Install Fabric API on both sides.

GitHub Actions compiles the project and uploads the JAR as a workflow artifact. Check the Actions tab for the latest build result.

## Limitations

- The HUD does not change vanilla attack cooldown or server combat rules.
- Critical timing and melee range are estimates, not guarantees.
- Follow multiplayer server rules; do not use target highlighting where it is prohibited.
