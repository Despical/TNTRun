<p align="center">
  <img src="assets/tnt-run-icon.svg" width="128" height="128" alt="TNT Run icon">
</p>

<h1 align="center">TNT Run</h1>

<p align="center"><a href="https://github.com/Despical/TNTRun">TNT Run</a> is a fast-paced jump 'n run game where players survive as long as possible while the floor disappears beneath their feet.</p>

<p align="center">
  <a href="https://github.com/Despical/TNTRun/actions/workflows/build.yml"><img src="https://github.com/Despical/TNTRun/actions/workflows/build.yml/badge.svg" alt="CI"></a>
  <img src="https://img.shields.io/badge/Java-25-007396.svg" alt="Java 25">
  <img src="https://img.shields.io/badge/Gradle-9.8.2-079ec0?logo=gradle&amp;logoColor=white" alt="Gradle 9.8.2">
  <img src="https://img.shields.io/badge/Paper-1.21.11--26.3-62b47a" alt="Paper 1.21.11-26.3">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-GPLv3-blue.svg" alt="License: GPL v3"></a>
</p>

<p align="center"><a href="https://hub.despical.dev/plugins/tnt-run">Hub</a> · <a href="https://discord.gg/uXVU8jmtpU">Discord</a> · <a href="https://www.spigotmc.org/resources/tnt-run.83196/">SpigotMC</a> · <a href="https://builtbybit.com/resources/tnt-run.116706/">BuiltByBit</a> · <a href="https://modrinth.com/plugin/tnt-run-minigame">Modrinth</a></p>

<p align="center">Read the <a href="https://docs.despical.dev/tnt-run/">documentation</a> for setup instructions, configuration guides and the public API.</p>

Players (up to 12 or 24, mostly, depending on the map) are placed on sand/gravel floors. Blocks you run or stand on will disappear or "fall" after a brief delay. Once you fall, you will end up on the next floor below. Each map has multiple floors to run on, the lowest floor being above the void which eliminates you. The goal is to be the last player alive, which gets progressively more difficult as the space available diminishes.

---

## Features

- Configurable arenas with an in-game setup menu, signs, and runtime state handling.
- Double jumps, spectator tools, potion effects, and crash-safe block recovery.
- Arena music with separate lobby, in-game, and end-game songs through NoteBlockAPI.
- Customizable messages, scoreboards, boss bars, sounds, items, and menus.
- Player statistics, records, spectator tools, and leaderboard support.
- Configurable command rewards for podium placements, survival time, PvP kills, records, and milestones.
- Flat-file storage by default, with optional MySQL persistence.
- Public API for game lifecycle, player flow, and statistic changes.
- Optional hooks for common server plugins such as PlaceholderAPI and world-management plugins.

---

## Installation

TNT Run supports [Paper](https://papermc.io/downloads/paper) and compatible forks for Minecraft **1.21.11 through 26.3**. Use **Java 25** to run your server and build the project.

1. Download the plugin JAR from [Hub](https://hub.despical.dev/plugins/tnt-run), [SpigotMC](https://www.spigotmc.org/resources/tnt-run.83196/), [Modrinth](https://modrinth.com/plugin/tnt-run-minigame) or [BuiltByBit](https://builtbybit.com/resources/tnt-run.116706/).
2. Stop the server and place `tnt-run-<version>.jar` in its `plugins/` directory.
3. Install any optional integrations you want to use, such as PlaceholderAPI or NoteBlockAPI.
4. Start the server and check the console for a successful TNT Run startup.

Missing settings and bundled songs are installed under `plugins/TNTRun/`; existing files are preserved.

## Getting started

```text
/tntrun create <arena_id>
/tntrun edit <arena_id>
/tntrun join <arena_id>
```

Create an arena, set its lobby, start and end locations in the editor, then register it. The editor also configures player amounts, signs, potion effects and the arena music. See the [arena setup guide](https://docs.despical.dev/tnt-run/arena-setup) for a step-by-step walkthrough.

---

## Requirements

- Java 25
- A Paper-compatible Minecraft server (1.21.11 - 26.3)

---

## Resources

- [Documentation](https://docs.despical.dev/tnt-run/)
- [Javadocs](https://javadoc.despical.dev/tnt-run/)
- [SpigotMC](https://spigotmc.org/resources/tnt-run.83196/)
- [BuiltByBit](https://builtbybit.com/resources/tnt-run.116706/)
- [Modrinth](https://modrinth.com/plugin/tnt-run-minigame)

---

## Building

Clone the repository:

```bash
git clone https://github.com/Despical/TNTRun.git
cd TNTRun
```

Build the plugin jar:

```bash
./gradlew shadowJar
```

On Windows:

```cmd
gradlew.bat shadowJar
```

The packaged jar is created under `build/libs/`.

Run the full verification used by CI:

```bash
./gradlew build
```

On Windows:

```cmd
gradlew.bat build
```

---

## Configuration

Configuration files are bundled in `src/main/resources` and copied to the plugin data folder on first startup. Most server-facing behavior can be adjusted without rebuilding the plugin.

Common files:

- `config.yml` controls gameplay, storage, chat, and command behavior.
- `arenas.yml` stores arena data.
- `messages.yml`, `scoreboard.yml`, `bossbar.yml`, `sounds.yml`, and `items.yml` control presentation.
- `mysql.yml` configures MySQL when database storage is enabled.
- `rewards.yml` configures reward commands, survival payments, and milestone thresholds. Examples are disabled by default; reload changes with `/tr reload`.
- `musics/` holds the `.nbs` songs used by the arena music system. It requires the optional NoteBlockAPI plugin; pick lobby, in-game and end-game songs (and whether an identical song continues or restarts between phases) in the arena editor.
- `menu/` contains menu layouts used by setup, statistics, and spectator screens.

---

## API

TNT Run exposes Bukkit events under `dev.despical.tntrun.api.event` for game lifecycle changes, player joins and leaves, and statistic updates.

Maven:

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

```xml
<dependency>
    <groupId>com.github.Despical</groupId>
    <artifactId>TNTRun</artifactId>
    <version>master-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

Gradle:

```gradle
repositories {
    maven { url = 'https://jitpack.io' }
}
```

```gradle
dependencies {
    compileOnly group: 'com.github.Despical', name: 'TNTRun', version: 'master-SNAPSHOT'
}
```

---

## Integrations

Optional integrations declared by the plugin include:

- PlaceholderAPI
- NoteBlockAPI
- WorldGuard
- Multiverse-Core
- SlimeWorldManager
- SlimeWorldPlugin
- MultiWorld
- My_Worlds

---

## Security

We prioritize user privacy and application integrity. Please do not open public issues for discovered vulnerabilities.

Read our [SECURITY.md](SECURITY.md) for responsible disclosure reporting.

---

## Contributing

We welcome Pull Requests from the community. To help us maintain clean project history and formatting, please follow these guidelines:

* **No tabs:** Use spaces exclusively for indentation.
* **Style consistency:** Respect the established code architecture and style templates.
* **Version control cleanliness:** Do not increment project version numbers in example configurations within your PR.
* **Minimal diffs:** Disable automated reformat-on-save settings that affect untouched files.

Learn more via our formal [Contribution Guidelines](CONTRIBUTING.md).

Please also follow our [Code of Conduct](CODE_OF_CONDUCT.md) when participating in the project.

---

## License

This project is licensed under the [GPL-3.0 License](http://www.gnu.org/licenses/gpl-3.0.html).

See the [LICENSE](LICENSE) file for comprehensive copyright notices and third-party attributions.
