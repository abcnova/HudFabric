## HudFabric 1.0.8 Beta

- Nine Fabric JARs cover Minecraft 1.21–1.21.11 and 26.1–26.3; choose the artifact whose filename matches your game version.
- Added the editable note board, totem/arrow counter, optional armor-break warning (off by default), and reset controls.
- All 17 per-version source projects pass a clean Gradle build. The packaged JARs declare their supported Minecraft ranges and the correct Java baseline.

### Testing note
The 26.3 client game test reached client startup after a key-category fix, then the Java process exited with Windows status `0xC0000005` before test results were produced. Module dragging on 26.3 is therefore not yet runtime-verified. This is a beta release; please report any issues with the exact Minecraft version and log.

SHA-256 checksums are included in `SHA256SUMS.txt`.
