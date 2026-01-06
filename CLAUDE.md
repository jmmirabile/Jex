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

## Completed (v1.0.2 - Released 2026-01-05)

- [x] Package reorganization (solutions.cloudbusiness.cli → org.jex.cli)
- [x] Internal plugin discovery and architecture
- [x] Fixed version detection in JexMavenUtil (using JexMavenUtil.getVersion())
- [x] Cleaned up plugin registry generation (removed new-plugin from plugin.yaml)
- [x] Updated README.md for v1.0.2
- [x] Released v1.0.2 to GitHub

## TODO: v1.0.3 - Plugin Management CRUD Operations

### Feature: Developer Plugin Management Commands

Implement CRUD operations for plugin management to improve developer experience.

**Current State:**
- Developers must manually copy JARs to `~/.config/Jex/plugins/`
- Developers must manually edit `plugin.yaml` to register plugins
- No easy way to update or remove plugins

**Goal:**
Provide command-line tools for plugin lifecycle management.

### Commands to Implement

#### 1. Install Plugin (Create)
```bash
jex --install-plugin <name> --jar <jar-file>
```
- **Behavior:**
  - Copy JAR to `~/.config/Jex/plugins/`
  - Load JAR and extract metadata (class, version, description)
  - Add entry to `plugin.yaml` with key `<name>`
  - Verify plugin works with `jex <name> --help`
- **Error handling:**
  - Error if plugin already exists in plugin.yaml
  - Error if JAR file doesn't exist
  - Error if JAR is not a valid Jex plugin (missing Plugin interface)

#### 2. Update Plugin (Update)
```bash
jex --update-plugin <name> --jar <jar-file>
```
- **Behavior:**
  - Replace existing JAR in `~/.config/Jex/plugins/`
  - Update entry in `plugin.yaml` (version, class, description)
  - Verify updated plugin works
- **Error handling:**
  - Error if plugin doesn't exist in plugin.yaml
  - Error if JAR file doesn't exist

#### 3. Uninstall Plugin (Delete)
```bash
jex --uninstall-plugin <name>
```
- **Behavior:**
  - Remove JAR from `~/.config/Jex/plugins/`
  - Remove entry from `plugin.yaml`
  - Display confirmation message
- **Error handling:**
  - Error if plugin doesn't exist
  - Optionally: prompt for confirmation before deletion

#### 4. Enhanced Read (Optional)
```bash
jex --show-plugin <name>
```
- **Behavior:**
  - Display detailed information about a specific plugin
  - Show: name, JAR, class, version, description, location
- Note: `jex --list` already provides basic read functionality

### Implementation Tasks

- [ ] **Add new command-line options to App.java**
  - Add `--install-plugin` option
  - Add `--update-plugin` option
  - Add `--uninstall-plugin` option
  - Optional: Add `--show-plugin` option

- [ ] **Create PluginManager.java utility class**
  - `installPlugin(String name, String jarPath)` method
  - `updatePlugin(String name, String jarPath)` method
  - `uninstallPlugin(String name)` method
  - `loadPluginMetadata(String jarPath)` - extract class, version, description
  - `updatePluginYaml(String name, PluginMetadata metadata)` method
  - `removeFromPluginYaml(String name)` method

- [ ] **Metadata extraction**
  - Load JAR and instantiate plugin to get metadata
  - Read version from JAR manifest or prompt user
  - Read description from manifest or prompt user
  - Validate plugin implements Plugin interface

- [ ] **Update arguments.yaml**
  - Add definitions for new options

- [ ] **Error handling**
  - Clear error messages for all failure scenarios
  - Validation of JAR files
  - Validation of plugin.yaml integrity

- [ ] **Testing**
  - Test install new plugin
  - Test install duplicate (should error)
  - Test update existing plugin
  - Test update non-existent (should error)
  - Test uninstall existing plugin
  - Test uninstall non-existent (should error)

- [ ] **Documentation**
  - Update README.md with new commands
  - Add examples to README
  - Update CLAUDE.md

### Notes
- `jex --install` (no args) still installs/updates Jex itself (safe, preserves plugin.yaml)
- Plugin management commands only affect developer plugins
- Internal plugins (like `new-plugin`) are not affected
- Consider adding `--force` flag for update/uninstall if needed

## Possible Future Architectures and Features

### Multi-Build-Tool Support (Gradle, Ant, SBT)

**Context:** Currently `new-plugin` (internal plugin) only generates Maven projects. Users may want Gradle, Ant, or SBT.

**Initial Thought:** Add `--build` flag to `new-plugin`:
```bash
jex new-plugin my-tool --build gradle
jex new-plugin my-tool --build ant
jex new-plugin my-tool --build sbt
```

**Problem: JAR Bloat**
- Supporting multiple build tools requires bundling their libraries in Jex JAR
- Gradle/Ant/SBT dependencies would significantly increase Jex size
- Violates "keep Jex small and light" principle

**Recommended Architecture: Hybrid Approach**

**Internal (bundled in Jex.jar):**
- `new-plugin` - Maven only (most common, minimal dependencies)
- Always available, no installation needed

**External (optional plugins):**
- `new-gradle-plugin` - Separate JAR with Gradle-specific libraries
- `new-ant-plugin` - Separate JAR with Ant-specific libraries
- `new-sbt-plugin` - Separate JAR with SBT-specific libraries
- Users install only what they need

**Usage:**
```bash
# Maven (always available)
jex new-plugin my-tool

# Gradle (if installed)
jex --install-plugin new-gradle-plugin --jar new-gradle-plugin.jar
jex new-gradle-plugin my-tool

# User can create shell alias for convenience
alias jexgradle="jex new-gradle-plugin"
jexgradle my-tool
```

**Benefits:**
- ✅ Jex core stays small and light
- ✅ Users only install what they need
- ✅ No forced dependency bloat
- ✅ Extensible by community (anyone can create build tool generators)
- ✅ Maven default covers 80% of use cases

**Implementation Notes:**
- Each build tool generator is its own plugin project
- All generators produce compatible JARs (Plugin interface is build-tool agnostic)
- Community could contribute additional generators (Buck, Bazel, etc.)

### Bundled Plugin Protection

**Problem:** Users might accidentally delete bundled plugins (like new-plugin.jar) when cleaning up their plugins directory, thinking they're removing their own custom plugins.

**Possible Solutions:**

1. **Different filename convention**
   - Rename `new-plugin.jar` → `jex-new-plugin.jar` or `_jex-new-plugin.jar`
   - Makes bundled plugins visually distinct from user plugins
   - Pros: Simple, clear visual distinction
   - Cons: Naming convention must be documented and maintained

2. **Separate directory for bundled plugins**
   - User plugins: `~/.config/Jex/plugins/`
   - Bundled plugins: `~/.config/Jex/system/` or `~/.local/lib/jex/plugins/`
   - Pros: Clear physical separation
   - Cons: More complex directory structure, need to search multiple locations

3. **Mark as bundled in plugin.yaml**
   ```yaml
   new-plugin:
     jar: new-plugin.jar
     class: org.jex.plugins.newplugin.NewPlugin
     version: 1.0.0
     bundled: true  # Mark as system plugin
     description: "Plugin generator"
   ```
   - Could show warnings or prevent deletion of bundled plugins via commands
   - Pros: Metadata-driven, enables programmatic protection
   - Cons: Only works if user uses Jex commands to manage plugins

4. **Load bundled plugins from jex.jar itself**
   - Don't extract bundled plugins to filesystem at all
   - Load them directly from resources inside jex.jar
   - Pros: Most protected, can't be accidentally deleted
   - Cons: More complex plugin loading, can't easily update bundled plugins

5. **Easy reinstall mechanism**
   - `jex --install` always reinstalls bundled plugins (already does this)
   - Document this in help/README
   - Pros: Self-healing, simple to implement
   - Cons: Relies on user knowing to reinstall, temporary loss of functionality

**Recommended Approach:**
Combination of Option 3 + 5:
- Mark bundled plugins with `bundled: true` flag in plugin.yaml
- `jex --install` reinstalls bundled plugins (already implemented)
- Document the reinstall mechanism clearly
- Simple, clear, and self-healing

## Notes

- Current version: 1.0.2 (as of 2026-01-05)
- Project renamed from "Commander" to "Jex" on 2026-01-03
- Main branch: `main`
- Deployment: Fat JAR distribution (`jex.jar`)
- OS Support: Linux, macOS, Windows
