# HudFabric 1.0.8

HudFabric 1.0.8 adds privacy-safe settings codes for sharing via Discord, a movable pause-menu HUD Editor button, and vanilla resource-pack hotbar textures for independent armor slots. It also includes the editable note board, totem and active-arrow counter, optional armor-break warning (off by default), reset controls, and grouped compatibility JARs from Minecraft 1.21 through 26.3.

### Follow-up fixes in the refreshed 1.0.8 release

- Fixed HUD module movement on Minecraft 26.3: the editor now checks SDL3's left mouse-button constant rather than the old GLFW value `0`.
- Updated the 26.3 client regression test to use that same native button constant. It moves the stats module from `(20, 60)` to `(65, 90)` and verifies the saved position.
- Updated the pause-menu HUD Editor button's repositioning check to use the version-correct left-button constant as well.
- Redesigned the personal note board for all supported versions as a blocky wooden board with a cork-like surface, pinned paper, ruled writing lines, and a matching editor.
- The note board remains local to each client; it is not synchronized to other players or a server.

Settings codes omit server-specific profiles and addresses; importing a code preserves the recipient's local server profiles.

All nine grouped compatibility projects passed Gradle `build` on 2026-10-25. The automated Minecraft 26.3 client drag test passed and confirmed the updated saved coordinates. The release contains nine JARs for Minecraft 1.21 through 26.3.

Nine JARs and `SHA256SUMS.txt` are attached. Match the artifact filename to your Minecraft version. Fabric Loader and Fabric API are required; Java 25 is required for Minecraft 26.x. Mod Menu is optional.
