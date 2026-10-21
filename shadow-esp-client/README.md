# GoonClient — Fabric 1.21.5

A client-side Fabric mod for Minecraft Java Edition 1.21.5, intended for use in your own worlds or on servers where these tools are permitted.

## Features

- **Block ESP:** outlines selected ores and ancient debris within a 12-block cube around the player.
- **Player ESP:** outlines other currently loaded player entities.
- **Vanish diagnostics:** warns when a loaded player entity is absent from the tab list. This is only a clue, not proof of vanish; a fully hidden server-side player cannot be reliably detected from client-only data.
- **GoonClient GUI:** press **Right Shift** to open the menu and toggle modules.

## Default keybinds

- `Right Shift`: Open GoonClient GUI
- `B`: Toggle Block ESP
- `P`: Toggle Player ESP
- `V`: Toggle vanish diagnostics

Keybinds can be changed in Minecraft's Controls menu.

## Build

Requirements: JDK 21 and Gradle. The project uses Fabric Loom and Yarn mappings for Minecraft 1.21.5.

From this directory, run:

```bash
gradle build
```

The mod JAR should be created under `build/libs/` (normally `goonclient-1.0.0.jar`). Install Fabric Loader for Minecraft 1.21.5, then copy the JAR into your `.minecraft/mods` directory. Fabric API for 1.21.5 is also required.

## Notes

This is source code, not a precompiled or in-game-verified JAR. Build and test it locally; if the build reports a mapping/API error, share the complete error output to adapt the code to your exact environment.
