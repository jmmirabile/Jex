# Jex

A plugin-based CLI framework for Java that allows developers to create modular, extensible command-line applications.

## Overview

I had multiple Java "test" apps spread out across project directories. I need a way to organize them and combine them 
into a single framework. I came up with this idea based on another project developed using Python. 

I'm calling it Jex. It is a Java-based CLI framework that enables users to execute plugins through a simple 
command-line interface. Plugins are self-contained JAR files that can be dropped into the plugins directory 
and registered for use.

As I worked through the design, I decided to use Claude Code to help me get it done. The amount of time saved using
AI tools like Claude is invaluable. Coding with AI is like having a robot or a development partner that can type 1000 
characters per second. The more specific you can be when describing each component, the better 
the resulting code will be. 

However, I will point out, I came up with the architecture, plugin system, had written some of the code already and 
decided on which libraries to use. 

You still need to understand what you're building, use AI as a powerful assistant, but not a replacement for thinking.

It was an iterative process, not a single prompt to perfect code experience. It was conversational, resulting in 
refinement, testing and iteration. Code had to be reviewed, I knew what I wanted, reviewed the code, suggested changes, 
and drove the effort. 

## Architecture Origins
This architecture was informed by lessons learned from a Python-based multi-platform REST client framework I previously 
built. With that project, I discovered:

- **Performance degradation** as API platforms were added, the client became slower. This led to the item below.
- The importance of **lazy loading**  or **on-demand loading** - Only load what you're executing.
- **Simplicity beats complexity** - Frameworks can become overhead, introduce unnecessary complexity, design challenges 
  due to the framework requirements. I reviewed several Python "cli" tools, but they seemed to add unnecessary complexity.
  For Jex, I could have used Picocli, but it doesn't lazy load and requires changes to the bootstrap or main 
  application. Jex is simple, small and extensible and adding new plugins has no impact on Jex speed and 
  does not require a recompile.
- **YAML-based plugin registries** are cleaner than code-based registration. 

Jex applies these hard-won lessons from day one: lazy plugin loading, minimal framework
complexity, and a focus on developer experience. Each plugin is isolated and only loaded when
invoked, ensuring Jex scales to hundreds of plugins without performance impact.


**Key Features:**
- Self-installing fat JAR with OS-specific wrapper scripts
- YAML-based configuration for arguments and plugins
- Dynamic plugin loading from JAR files
- Built-in help and plugin management commands
- Zero configuration required - just download and run setup

## Quick Start

### Installation

1. **Download** the `Jex.jar` file (fat JAR with all dependencies) from the Releases link.

2. **Run setup** to install Jex:
   ```bash
   java -jar Jex.jar --setup
   ```
   * After setup has been run, the following has been created:
     * After setup has been run, a soft link is also created named "jex" so you don't have to type "Jex".
     * A default plugin is installed named "hello". It takes one parameter "-n|--name". This is created as an example.
     * A plugins.yaml file is created in the Jex config directory. This is where the plugins are registered.
     
3. **Add to PATH** (if needed):
   - **Linux/macOS**: The installer will tell you if `~/.local/bin` needs to be added to PATH
   - **Windows**: Add `%LOCALAPPDATA%\Programs\Jex` to your PATH environment variable

4. **Verify installation**:
   ```bash
   Jex --help
   ```

### Installation Locations

Jex installs to OS-specific locations:

**Linux:**
- JAR: `~/.local/lib/Jex/Jex.jar`
- Script: `~/.local/bin/Jex`
- Config: `~/.config/Jex/`

**macOS:**
- JAR: `~/Library/Application Support/Jex/Jex.jar`
- Script: `~/.local/bin/Jex`
- Config: `~/Library/Application Support/Jex/`

**Windows:**
- JAR: `%LOCALAPPDATA%\Programs\Jex\Jex.jar`
- Script: `%LOCALAPPDATA%\Programs\Jex\Jex.bat`
- Config: `%APPDATA%\Jex\`

## Architecture

### Execution Flow

1. User runs: `Jex [options]` or `Jex <plugin-name> [plugin-args...]`
2. Jex loads its `arguments.yaml` and parses built-in options
3. If a built-in command is detected (e.g., `--setup`, `--help`), execute it and exit
4. Otherwise, treat the first argument as a plugin name
5. Look up the plugin in `plugin.yaml` registry
6. Load the plugin JAR dynamically 
7. Extract the plugin's `arguments.yaml` from the JAR 
8. Parse remaining arguments using Apache Commons CLI 
9. Instantiate and execute the plugin 

### Configuration Directory Structure

Jex stores its configuration in OS-specific locations:

- **Windows**: `%APPDATA%/Jex`
- **Linux**: `~/.config/Jex`
- **macOS**: `~/Library/Application Support/Jex`

```
<platform config directory>/Jex/
├── plugin.yaml           # Registry of installed plugins
├── arguments.yaml        # Jex's own CLI arguments
└── plugins/
    ├── my-plugin.jar     # Self-contained plugin JAR
    ├── another-plugin.jar
    └── third-plugin.jar
```

## Plugin System

### Plugin JAR Structure

Each plugin is a self-contained JAR file containing:

```
my-plugin.jar
├── com/example/MyPlugin.class    # Implements Plugin interface
├── arguments.yaml                 # CLI argument definitions
└── [other plugin classes/resources]
```

### Plugin Interface

All plugins must implement the `Plugin` interface:

```java
package solutions.cloudbusiness.cli;

public interface Plugin {
    String getName();
    void execute(String[] args);
    String[] getCommandLineOptions();
}
```

### Plugin Registry (plugin.yaml)

The `plugin.yaml` file maps plugin names to their JAR files and main classes:

```yaml
my-plugin:
  jar: my-plugin.jar
  class: com.example.MyPlugin
  version: 1.0.0
  description: "Does something useful"

another-plugin:
  jar: another-plugin.jar
  class: com.example.AnotherPlugin
  version: 2.1.0
  description: "Another useful tool"
```

### Plugin Arguments (arguments.yaml)

Each plugin defines its command-line arguments in an `arguments.yaml` file bundled in the JAR:

```yaml
options:
  - name: input
    short: i
    long: input-file
    description: "Input file path"
    required: true
    hasArg: true
  - name: output
    short: o
    long: output-file
    description: "Output file path"
    required: false
    hasArg: true
  - name: verbose
    short: v
    long: verbose
    description: "Enable verbose output"
    required: false
    hasArg: false
```

## Jex Built-in Commands

### Help

Display usage information and available commands:

```bash
Jex --help
Jex -h
Jex        # No arguments also shows help
```

### Setup

Initialize and install Jex:

```bash
Jex --setup
java -jar Jex.jar --setup  # First-time setup
```

This command:
- Creates configuration directory (OS-specific location)
- Creates `plugins/` subdirectory
- Generates default `plugin.yaml` registry with helpful comments
- Generates default `arguments.yaml` for Jex
- **Installs** `Jex.jar` to the lib directory
- **Creates** and installs OS-specific wrapper script (`Jex` or `Jex.bat`)
- **Makes** the script executable (Unix/Linux/macOS)
- **Checks** if bin directory is in PATH and provides instructions if needed

### List Plugins

List all installed plugins:

```bash
Jex --list
Jex -l
```

Shows all registered plugins from `plugin.yaml` or a helpful message if no plugins are installed.

### Generate Plugin Template ⏳ Planned

Generate a plugin template/stub for development:

```bash
Jex --generate-plugin <plugin-name>
```

This will create a plugin project template with:
- Plugin class skeleton implementing the `Plugin` interface
- Sample `arguments.yaml`
- Maven build configuration
- README with development instructions

*Status: Command placeholder exists, implementation pending*

## Usage

### Using Jex

```bash
# Show help
Jex --help

# List installed plugins
Jex --list

# Run setup (installs Jex)
Jex --setup
```

### Installing a Plugin ⏳ Manual Process (Automated loading pending)

1. **Copy** the plugin JAR to the plugins directory:
   - Linux: `~/.config/Jex/plugins/`
   - macOS: `~/Library/Application Support/Jex/plugins/`
   - Windows: `%APPDATA%\Jex\plugins\`

2. **Register** the plugin in `plugin.yaml`:
   ```yaml
   my-plugin:
     jar: my-plugin.jar
     class: com.example.MyPlugin
     version: 1.0.0
     description: "My custom plugin"
   ```

3. **Run** the plugin:
   ```bash
   Jex my-plugin [args...]
   ```
   *Note: Automatic plugin loading is in progress*

## Developing Plugins

### Plugin Development Steps

1. **Generate a plugin template** *(planned)*:
   ```bash
   Jex --generate-plugin my-plugin
   ```

2. **Implement the `Plugin` interface** in your main class:
   ```java
   package com.example;

   import solutions.cloudbusiness.cli.Plugin;

   public class MyPlugin implements Plugin {
       @Override
       public String getName() {
           return "my-plugin";
       }

       @Override
       public void execute(String[] args) {
           // Your plugin logic here
       }

       @Override
       public String[] getCommandLineOptions() {
           // Return CLI options
           return new String[0];
       }
   }
   ```

3. **Define CLI arguments** in `src/main/resources/arguments.yaml`:
   ```yaml
   options:
     - name: input
       short: i
       long: input-file
       description: "Input file path"
       required: true
       hasArg: true
   ```

4. **Build the plugin JAR** with `arguments.yaml` included as a resource

5. **Install the plugin**:
   - Copy JAR to `~/.config/Jex/plugins/`
   - Register in `plugin.yaml`

### Plugin Template Structure

```
my-plugin/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/MyPlugin.java
│       └── resources/
│           └── arguments.yaml
├── pom.xml
└── README.md
```

## Building Jex from Source

### Prerequisites

- Java 21 or later
- Maven 3.6+

### Build Steps

1. **Clone the repository** (or download source)

2. **Build the fat JAR**:
   ```bash
   mvn clean package
   ```

3. **The built JAR** will be at:
   ```
   target/Jex.jar
   ```

4. **Install it**:
   ```bash
   java -jar target/Jex.jar --setup
   ```

### Development Commands

```bash
# Compile only
mvn compile

# Run tests
mvn test

# Build without tests
mvn package -DskipTests

# Clean build directory
mvn clean
```

## Project Structure

```
Jex/
├── src/
│   ├── main/
│   │   ├── java/solutions/cloudbusiness/cli/
│   │   │   ├── App.java              # Main entry point
│   │   │   ├── Setup.java            # Setup command implementation
│   │   │   ├── Plugin.java           # Plugin interface
│   │   │   ├── PluginLoader.java     # Loads plugins from YAML
│   │   │   └── ArgumentParser.java   # YAML-based argument parser
│   │   └── resources/
│   │       ├── Jex.sh          # Unix wrapper script template
│   │       └── Jex.bat         # Windows wrapper script template
│   └── test/
│       └── java/solutions/cloudbusiness/cli/
│           └── AppTest.java
├── pom.xml                            # Maven build configuration
└── README.md
```

## Implementation Status

### ✅ Completed Features
- Self-installing fat JAR with Maven Shade Plugin
- OS-specific installation (Linux, macOS, Windows)
- Wrapper script generation and installation
- Help system (`--help`, `-h`)
- Setup command (`--setup`)
- List plugins command (`--list`, `-l`)
- YAML-based argument parsing
- Configuration directory management

### ⏳ In Progress / Planned
- Dynamic plugin JAR loading
- Plugin argument parsing from JAR resources
- Plugin instantiation and execution
- Plugin template generator (`--generate-plugin`)
- Sample plugin for testing
- Comprehensive error handling

## Dependencies

- **Apache Commons CLI 1.11.0**: Command-line argument parsing
- **SnakeYAML 2.5**: YAML configuration file parsing
- **JUnit 3.8.1**: Testing framework
- **Maven Shade Plugin 3.5.1**: Fat JAR creation

## License

Apache 2.0 License - see LICENSE file for details.

## Contributing

Found a bug or want a feature? Please open an issue or submit a pull request!
