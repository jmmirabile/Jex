# Jex - Plugin-Based CLI Framework for Java

## Project Overview
Jex is a plugin-based CLI framework for Java that enables developers to create modular, extensible command-line applications. Plugins are self-contained JAR files that can be dynamically loaded and executed.

**Key Philosophy**: Lazy loading of plugins, minimal framework overhead, excellent developer experience. Only loads plugins when invoked, enabling scaling to hundreds of plugins without performance degradation.

## Quick Start Commands

### Build Commands
- **Build main JAR**: `mvn clean package` (creates `target/jex.jar`)
- **Build without tests**: `mvn clean package -DskipTests`
- **Clean build artifacts**: `mvn clean`

### Test Commands
- **Run all tests**: `mvn test`
- **Run specific test**: `mvn test -Dtest=TestClassName`

### Installation & Setup
- **Install Jex**: `java -jar target/jex.jar --setup`
- **List installed plugins**: `java -jar target/jex.jar --list`
- **Generate new plugin**: `java -jar target/jex.jar --generate-plugin <plugin-name>`

### Example Plugin Build
- **Build plugin**: `cd hello-plugin && mvn clean package`
- **Install plugin**: Copy JAR to `~/.config/Jex/plugins/` (Linux) or appropriate OS directory

## Project Structure

```
Jex/
├── src/main/java/solutions/cloudbusiness/cli/
│   ├── App.java                 # Main entry point and command routing
│   ├── Plugin.java              # Plugin interface (3 methods)
│   ├── PluginLoader.java        # Dynamic JAR loading via URLClassLoader
│   ├── ArgumentParser.java      # YAML to CLI options conversion
│   ├── PathConfig.java          # OS-aware path management
│   ├── Setup.java               # Installation and initialization
│   └── PluginGenerator.java     # Plugin template generation
│
├── src/main/resources/
│   ├── jex.sh                   # Unix/Linux/macOS wrapper script
│   └── jex.bat                  # Windows wrapper script
│
├── hello-plugin/                # Example plugin demonstrating architecture
├── pom.xml                      # Maven build configuration
├── TODO.md                      # Persistent TODO list (maintained across sessions)
└── CLAUDE.md                    # This file
```

## Code Style Guidelines

### Java Standards
- **Java Version**: Java 21
- **Formatting**: Follow standard Java conventions
- **Naming**:
  - Classes: PascalCase
  - Methods/variables: camelCase
  - Constants: UPPER_SNAKE_CASE
- **Documentation**: Add Javadoc for public APIs and complex logic

### Best Practices
- Keep plugins self-contained and focused
- Use YAML configuration for CLI options (don't hardcode argument parsing)
- Maintain OS independence (use PathConfig for paths)
- Prefer composition over inheritance

## Architecture Patterns

### Plugin Interface
All plugins must implement:
```java
public interface Plugin {
    String getName();
    void execute(String[] args);
    String[] getCommandLineOptions();
}
```

### Dynamic Loading
- Plugins loaded via URLClassLoader at runtime
- JAR files stored in OS-specific plugin directories
- Registry maintained in `plugin.yaml`

### Configuration Files
- **plugin.yaml**: Registry of installed plugins (JAR, class, version, description)
- **arguments.yaml**: CLI option definitions for Jex and individual plugins

## Common Development Tasks

### Adding a New Core Feature
1. Identify which class handles the feature (App.java for commands, PathConfig for paths, etc.)
2. Read the relevant source file first
3. Implement the feature following existing patterns
4. Test manually with `mvn clean package && java -jar target/jex.jar <command>`
5. Update TODO.md with any remaining work

### Creating a New Plugin
1. Run: `java -jar target/jex.jar --generate-plugin <plugin-name>`
2. Navigate to generated directory: `cd <plugin-name>-plugin`
3. Implement Plugin interface in generated class
4. Define CLI options in `src/main/resources/arguments.yaml`
5. Build: `mvn clean package`
6. Install: Copy JAR to plugins directory and update plugin.yaml

### Debugging
- **Check installation**: `java -jar target/jex.jar --list`
- **Verify paths**: Check PathConfig.java for OS-specific directories
- **Plugin loading issues**: Check plugin.yaml format and JAR paths
- **Argument parsing**: Verify arguments.yaml structure

## Git Workflow

### Branch Naming
- Features: `feature/feature-name`
- Bugfixes: `bugfix/bug-description`
- Releases: `release/vX.Y.Z`

### Commit Messages
- Use imperative mood: "Add feature" (not "Added feature")
- Format: `<type>: <description>` (e.g., "feat: Add plugin validation")
- Keep first line under 72 characters

### Pull Requests
- Include description of changes
- Reference related issues
- Ensure build passes: `mvn clean package`

## TODO List Management

**CRITICAL**: Always maintain TODO.md for cross-session persistence.

### Session Start Protocol
1. **ALWAYS read TODO.md at the start of each session** to understand pending tasks
2. Review uncompleted items before starting new work
3. Ask user if priorities have changed

### During Work
- Use TodoWrite tool for real-time in-session tracking
- **ALSO update TODO.md** when adding, completing, or removing tasks
- Mark completed items with `[x]` checkbox syntax
- Keep both TodoWrite and TODO.md in sync

### Session End Protocol
- Ensure TODO.md reflects current state before ending work
- Move completed items to a "Completed" section (don't delete them immediately)
- Update task descriptions if scope changed during implementation

### TODO.md Format
```markdown
# Jex TODO List

## Current Tasks
- [ ] Task description (brief, actionable)
- [ ] Another pending task

## In Progress
- [ ] Task currently being worked on

## Completed
- [x] Previously completed task
- [x] Another completed task
```

## Dependencies

| Dependency | Version | Purpose |
|-----------|---------|---------|
| Apache Commons CLI | 1.11.0 | Command-line argument parsing |
| SnakeYAML | 2.5 | YAML file parsing and generation |
| Maven Shade Plugin | 3.5.1 | Fat JAR creation |

## Testing Standards

- Test core functionality after changes
- Manually verify CLI commands work: `--setup`, `--list`, `--generate-plugin`
- Test cross-platform compatibility when modifying PathConfig
- Verify plugin loading with hello-plugin example

## Architectural Refactoring Proposal (v2.0.0)

### Current Architecture Issues

The current v1.0.0 architecture has some design concerns:

1. **Mixed Responsibilities in Setup.java**
   - Setup.java handles installation (creating directories, installing JAR, wrapper scripts)
   - But it also defines runtime commands in `createDefaultArgumentsYaml()`
   - To add a new Jex command, you have to modify Setup (which is for installation)
   - Users who already installed won't get new commands without re-running setup

2. **`--java-package` Option Missing**
   - `--generate-plugin` intended to accept `--java-package` parameter
   - But the option isn't defined in arguments.yaml
   - Causes the command to break when both options are used together

3. **Naming Clarity**
   - `--setup` should be `--install` (more accurate for what it does)

### Proposed v2.0.0 Architecture

#### 1. Should `--setup` be `--install`?

**Yes!** More accurate naming:
- `--install` = Install Jex (create dirs, install JAR, create wrapper scripts)
- `--setup` sounds like configuration, but this is really installation

**Recommendation: Rename to `--install`**

#### 2. Should the install program be a plugin?

**No** - Chicken-and-egg problem:
- Plugins are loaded from the plugins directory
- But you can't load plugins until Jex is installed
- Installation must happen **before** plugins can work
- It's the bootstrap command

**Recommendation: Keep installation as built-in**

#### 3. Should `--generate-plugin` be a plugin?

**Yes! This is the key architectural improvement:**

**Pros:**
- Plugin generation is not core to Jex's operation
- It's a developer tool, not needed for running Jex
- Demonstrates Jex's plugin architecture by example
- Can be updated independently of Jex core
- Others could create alternative plugin generators
- Keeps Jex core minimal and focused

**How It Would Work:**
- Ship `generator-plugin.jar` with Jex
- During `--install`, automatically install the generator plugin
- Or document it as an optional plugin to install manually
- Users run: `jex generate my-plugin --package com.example`

### Proposed Minimal Core Architecture

**Built-in commands (only):**
```
jex --install        # Bootstrap Jex installation
jex --help / -h      # Display help
jex --list / -l      # List installed plugins
jex <plugin-name>    # Execute plugin
```

**Everything else is a plugin:**
```
jex generate <name> --package <pkg>   # generator plugin
jex lint <files>                       # hypothetical linter plugin
jex test <path>                        # hypothetical test runner plugin
```

### Benefits of This Design:

1. **Jex core** = Minimal framework (App, PluginLoader, PathConfig, Setup)
2. **Generator** = First-class plugin demonstrating the system
3. **Clear separation** = Core vs extensions
4. **Dogfooding** = Jex uses its own plugin system

### Refactoring Plan:

1. ✅ Rename `--setup` → `--install`
2. ✅ Keep install as built-in (must be)
3. ✅ **Move generator to a plugin** (bundled by default during install)
4. ✅ Hardcode built-in commands in App.java (remove arguments.yaml generation from Setup)

## TODO: v2.0.0 Architectural Refactoring

### Phase 1: Fix Current Issues
- [ ] Fix `--java-package` option for `--generate-plugin`
  - Add `--java-package` option definition to arguments.yaml in Setup.java
  - Test: `jex --generate-plugin my-plugin --java-package com.example`

### Phase 2: Refactor to Minimal Core
- [ ] Rename `--setup` to `--install` throughout codebase
  - Update App.java
  - Update Setup.java
  - Update CLAUDE.md documentation
  - Update README.md
  - Update help text

- [ ] Move built-in command definitions from YAML to code
  - Define Options directly in App.java (--install, --help, --list)
  - Remove `createDefaultArgumentsYaml()` from Setup.java
  - Remove arguments.yaml file generation entirely
  - Update ArgumentParser.java to only handle plugin-specific YAML parsing

### Phase 3: Extract Generator to Plugin
- [ ] Create generator-plugin as separate Maven project
  - Move PluginGenerator.java logic to new plugin project
  - Implement Plugin interface
  - Package as generator-plugin.jar

- [ ] Update installation to bundle generator plugin
  - Copy generator-plugin.jar to plugins directory during install
  - Auto-register in plugin.yaml during install
  - Document as bundled plugin in README

- [ ] Remove PluginGenerator.java from core
  - Delete PluginGenerator.java from core codebase
  - Remove --generate-plugin handling from App.java
  - Update tests if any

### Phase 4: Testing & Documentation
- [ ] Test all functionality
  - `jex --install` works correctly
  - `jex --help` displays correct information
  - `jex --list` shows bundled generator plugin
  - `jex generate my-plugin --package com.example` works
  - Plugin loading and execution works as before

- [ ] Update documentation
  - Update README.md with new architecture
  - Update CLAUDE.md (this file)
  - Add migration guide for v1.0.0 → v2.0.0

- [ ] Version and release
  - Update pom.xml to version 2.0.0
  - Create git tag v2.0.0
  - Update CHANGELOG if it exists

## Notes

- Current version: 1.0.1 (as of 2026-01-03)
- Project renamed from "Commander" to "Jex" on 2026-01-03
- Main branch: `main`
- Deployment: Fat JAR distribution (`jex.jar`)
- OS Support: Linux, macOS, Windows
