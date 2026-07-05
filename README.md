# TNT Run

[![CI](https://github.com/Despical/TNTRun/actions/workflows/build.yml/badge.svg)](https://github.com/Despical/TNTRun/actions/workflows/build.yml)
![Java 25](https://img.shields.io/badge/Java-25-007396.svg)
![Gradle](https://img.shields.io/badge/Gradle-9.6.1-079ec0?logo=gradle&logoColor=white)
![Minecraft](https://img.shields.io/badge/Minecraft-26.2-62b47a)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Docs](https://img.shields.io/badge/Docs-despical.dev-2ea44f.svg)](https://docs.despical.dev/tnt-run/)

TNT Run is a fast-paced jump 'n run TNT game where players survive as long as possible by parkouring across a floor of disappearing blocks.

Players (up to 12 or 24, mostly, depending on the map) are placed on sand/gravel floors. Blocks you run or stand on will disappear or "fall" after a brief delay. Once you fall, you will end up on the next floor below. Each map multiple floors to run on, the lowest floor being above the void which eliminates you. The goal is to be the last player alive which gets progressively more difficult as the space available diminishes.

---

## Features

- Configurable arenas with setup, editing, signs, and runtime state handling.
- Customizable messages, scoreboards, boss bars, sounds, items, and menus.
- Player statistics, records, spectator tools, and leaderboard support.
- Flat-file storage by default, with optional MySQL persistence.
- Public API for game lifecycle, player flow, and statistic changes.
- Optional hooks for common server plugins such as PlaceholderAPI and world-management plugins.

---

## Requirements

- Java 25
- A Paper-compatible Minecraft server

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
- WorldGuard
- Multiverse-Core
- SlimeWorldManager
- SlimeWorldPlugin
- MultiWorld
- My_Worlds
- NoteBlockAPI

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
