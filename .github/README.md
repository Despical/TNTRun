# TNT Run
[![](https://jitpack.io/v/Despical/TNTRun.svg)](https://jitpack.io/#Despical/TNTRun)
[![](https://img.shields.io/badge/JavaDocs-latest-lime.svg)](https://javadoc.jitpack.io/com/github/Despical/TNTRun/latest/javadoc/index.html)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/Despical/TNTRun/TNTRun%20Build)

TNT Run is an old Minecraft mini game that supports almost every version. 
Jump on the blocks but don't fall into void, show your parkour talent!

## Documentation
More information can be found on the [wiki page](https://github.com/Despical/TNTRun/wiki). The [Javadoc](https://javadoc.jitpack.io/com/github/Despical/TNTRun/latest/javadoc/index.html) can be browsed.

## Using TNT Run API
The project isn't in the Central Repository yet, so specifying a repository is needed.<br>
To add this project as a dependency to your project, add the following to your pom.xml:

### Maven dependency

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
    <version>1.0.0</version>
    <scope>compile</scope>
</dependency>
```

### Gradle dependency
```
repositories {
    maven { url 'https://jitpack.io' }
}
```
```
dependencies {
    compileOnly group: "com.github.Despical", name: "TNTRun", version: "2.0.1;
}
```

## License
This code is under [GPL-3.0 License](http://www.gnu.org/licenses/gpl-3.0.html)

See the [LICENSE](https://github.com/Despical/TNTRun/blob/master/LICENSE) file for required notices and attributions.

## Donations
You like the TNTRun? Then [donate](https://www.patreon.com/despical) back me to support the development.

## Contributing

I accept Pull Requests via GitHub. There are some guidelines which will make applying PRs easier for me:
+ Ensure you didn't use spaces! Please use tabs for indentation.
+ Respect the code style.
+ Do not increase the version numbers in any examples files and the README.md to the new version that this Pull Request would represent.
+ Create minimal diffs - disable on save actions like reformat source code or organize imports. If you feel the source code should be reformatted create a separate PR for this change.

You can learn more about contributing via GitHub in [contribution guidelines](CONTRIBUTING.md).

## Building from source
If you want to build this project from source code, run the following from Git Bash:
```
git clone https://www.github.com/Despical/TNTRun.git && cd TNTRun
mvn clean package -Dmaven.javadoc.skip=true (to not generate docs)
```
Also don't forget to install Maven before building.