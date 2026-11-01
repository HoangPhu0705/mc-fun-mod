# MC Fun Mod

A Fabric mod for Minecraft 26.2, built to run on a self-hosted Fabric server.

## Versions

| Component    | Version        |
|--------------|----------------|
| Minecraft    | 26.2           |
| Fabric Loader| 0.20.1         |
| Fabric API   | 0.161.0+26.2   |
| Fabric Loom  | 1.19-SNAPSHOT  |
| Gradle       | 9.8.2 (wrapper)|
| Java         | 25             |

Versions are set in [gradle.properties](gradle.properties). Check https://fabricmc.net/develop for newer ones.

## Requirements

- JDK 25 on `PATH` (or set `JAVA_HOME`)
- No Gradle install needed; use the wrapper (`gradlew.bat` / `./gradlew`)

## Project layout

```
src/
  main/        Common code (server + client): core rules, content, registries, events, commands, networking
  client/      Client-only code: rendering, screens, key bindings
  datagen/     Data generators: recipes, loot tables, tags, models, lang -> src/main/generated
  gametest/    In-game tests run on a headless dedicated server
  test/        Plain JUnit tests for core + architecture rules
docs/
  ARCHITECTURE.md   Layers, dependency rules, and "where do I put X"
.github/
  workflows/   CI (ci.yml calls _validate, _build, _datagen, _gametest)
```

Mod id: `mcfunmod`. Base package: `com.hoangphu.mcfunmod`.

**Before adding code, read [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).** It explains the layers
(`core` / `adapter` / `content` / `registry` / `event` / ...) and has a table of which files to touch
for each kind of change. Every package also has a `package-info.java` describing what belongs in it.

## Development

```powershell
.\gradlew.bat build                  # Compile, run unit + architecture + game tests, build the jar
.\gradlew.bat test -x runGameTest    # Unit + architecture tests only (fast)
.\gradlew.bat runGameTest            # Game tests on a headless dedicated server
.\gradlew.bat runDatagen             # Regenerate src/main/generated (commit the result)
.\gradlew.bat runClient              # Launch Minecraft with the mod loaded
.\gradlew.bat runServer              # Launch a dev server in run\ (set eula=true in run\eula.txt on first run)
.\gradlew.bat genSources             # Decompile Minecraft sources for browsing in the IDE
python .github\scripts\check_metadata.py   # Same metadata checks CI runs
```

Build output: `build\libs\mc-fun-mod-<version>.jar` (the `-sources.jar` is not needed on the server).

## CI

GitHub Actions runs on every push to `main` and every pull request:

1. **Validate**: Gradle wrapper checksum, `gradlew` executable bit, mod metadata consistency.
2. **Build**: compile all source sets, unit + architecture tests, upload the mod jar as an artifact.
3. **Datagen**: fails if `src/main/generated` is out of date.
4. **Game tests**: boots a dedicated server with the mod and runs all game tests.

The built jar can be downloaded from the run's **Artifacts** section (`mc-fun-mod-jar`).
Dependabot opens weekly PRs for GitHub Actions and test library updates; Minecraft/Fabric
versions are bumped by hand in `gradle.properties` (and `fabric.mod.json`).

### IDE

- **IntelliJ IDEA**: open the folder as a Gradle project. Loom generates the `Minecraft Client` / `Minecraft Server` run configurations.
- **VS Code**: install the Extension Pack for Java, then run `.\gradlew.bat vscode` to generate launch configurations.

## Hosting a Fabric server on this PC

1. **Install the server.** Download the Fabric server launcher for Minecraft 26.2 / Loader 0.20.1 from https://fabricmc.net/use/server and put it in its own folder outside this repo, e.g. `C:\mc-server\`.
2. **First run.**
   ```powershell
   java -Xmx4G -jar fabric-server-launch.jar nogui
   ```
   It stops after generating files. Set `eula=true` in `eula.txt`, then run it again.
3. **Add mods.** Copy into the server's `mods\` folder:
   - `build\libs\mc-fun-mod-<version>.jar`
   - Fabric API for 26.2 (from https://modrinth.com/mod/fabric-api)

   Players need the same mods installed on their Fabric client unless the mod is server-only.
4. **Configure** `server.properties`:
   ```properties
   online-mode=true
   white-list=true
   enforce-whitelist=true
   ```
   Then add players from the server console: `whitelist add <name>`.
5. **Open the port** (default TCP 25565):
   - Windows Firewall: allow inbound TCP 25565 for `java.exe`, or
     ```powershell
     New-NetFirewallRule -DisplayName "Minecraft Server" -Direction Inbound -Protocol TCP -LocalPort 25565 -Action Allow
     ```
   - Router: forward TCP 25565 to this PC's local IP (give the PC a static/reserved IP).
   - Friends connect to your public IP. If port forwarding is not possible (e.g. CGNAT), use a tunnel such as playit.gg, or a VPN like ZeroTier/Tailscale.
6. **Keep it running.** A simple `start.bat` in the server folder:
   ```bat
   @echo off
   java -Xms2G -Xmx4G -jar fabric-server-launch.jar nogui
   pause
   ```
   Back up the `world\` folder regularly.

### Updating the mod on the server

1. `.\gradlew.bat build`
2. Stop the server (`stop` in the console).
3. Replace the old `mc-fun-mod-*.jar` in `mods\` with the new one.
4. Start the server.

Bump `version` in [gradle.properties](gradle.properties) when releasing changes so clients and server can be matched.

## License

See [LICENSE](LICENSE).
