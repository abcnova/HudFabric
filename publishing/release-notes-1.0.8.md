# HudFabric 1.0.8

HudFabric 1.0.8 adds privacy-safe settings codes for sharing via Discord, a movable pause-menu HUD Editor button, and vanilla resource-pack hotbar textures for independent armor slots. It also includes the editable note board, totem and active-arrow counter, optional armor-break warning (off by default), reset controls, and grouped compatibility JARs from Minecraft 1.21 through 26.3.

### Follow-up fixes in the refreshed 1.0.8 release

- Reworked mouse-drag event handling across the 26.x targets after reports that HUD modules and the pause-menu HUD Editor button could remain immovable on Minecraft 26.3.
- Redesigned the personal note board for all supported versions as a blocky wooden board with a cork-like surface, pinned paper, ruled writing lines, and a matching editor.
- The note board remains local to each client; it is not synchronized to other players or a server.

Settings codes omit server-specific profiles and addresses; importing a code preserves the recipient's local server profiles.

All 17 version-specific projects passed Gradle `build` on 2026-10-23. The 26.3 automated client game test was retried after the drag-input changes, but the Java process again exited with Windows status `0xC0000005` before reporting results. Live confirmation of the updated drag handling is still pending; the source and JAR compile successfully, but this is not proof that dragging works in-game. Please report issues with the exact Minecraft version and log.

Nine JARs and `SHA256SUMS.txt` are attached.
