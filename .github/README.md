<h1 align="center">TNT Run</h1>

<div align="center">

[![Build](https://github.com/Despical/TNTRun/actions/workflows/build.yml/badge.svg)](https://github.com/Despical/TNTRun/actions/workflows/build.yml)
[![](https://jitpack.io/v/Despical/TNTRun.svg)](https://jitpack.io/#Despical/TNTRun)
[![](https://img.shields.io/badge/JavaDocs-latest-lime.svg)](https://javadoc.jitpack.io/com/github/Despical/TNTRun/latest/javadoc/index.html)
[![Support](https://img.shields.io/badge/Patreon-Support-lime.svg?logo=Patreon)](https://www.patreon.com/despical)

TNT Run is an old Minecraft minigame that supports almost every version. Jump on the blocks but don't fall into void, show your parkour talent!

</div>

## Documentation
- [Wiki](https://github.com/Despical/TNTRun/wiki)
- [JavaDocs](https://javadoc.jitpack.io/com/github/Despical/TNTRun/latest/javadoc/index.html)
## Donations
- [Patreon](https://www.patreon.com/despical)
- [Buy Me A Coffe](https://www.buymeacoffee.com/despical)

## TNT Run API
<details>
<summary>Maven dependency</summary>

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
    <version>2.4.9</version>
    <scope>provided</scope>
</dependency>
```

</details>

<details>
<summary>Gradle dependency</summary>

```
repositories {
    maven { url 'https://jitpack.io' }
}
```
```
dependencies {
    compileOnly group: "com.github.Despical", name: "TNTRun", version: "2.4.9";
}
```
</details>

## Contributing

I accept Pull Requests via GitHub. There are some guidelines which will make applying PRs easier for me:
+ Ensure you didn't use spaces! Please use tabs for indentation.
+ Respect the code style.
+ Do not increase the version numbers in any examples files and the README.md to the new version that this Pull Request would represent.
+ Create minimal diffs - disable on save actions like reformat source code or organize imports. If you feel the source code should be reformatted create a separate PR for this change.

You can learn more about contributing via GitHub in [contribution guidelines](../CONTRIBUTING.md).

## Translations
We are supporting multiple languages such as English, Turkish and German for now.<br>
If you want to help us with translating take a look at our [language repository](https://github.com/Despical/LocaleStorage).

## License
This code is under [GPL-3.0 License](http://www.gnu.org/licenses/gpl-3.0.html)

See the [LICENSE](https://github.com/Despical/TNTRun/blob/master/LICENSE) file for required notices and attributions.

## Building from source
To build this project from source code, run the following from Git Bash:
```
git clone https://www.github.com/Despical/TNTRun.git && cd TNTRun
mvn clean package -Dmaven.javadoc.skip=true
```

> [!NOTE]  
> Don't forget to install Maven before building.
