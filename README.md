# Commander

A plugin-based CLI framework for Java that allows developers to create modular, extensible command-line applications.

## Overview

Commander is a Java-based CLI framework that enables users to execute plugins through a simple command-line interface. Plugins are self-contained JAR files that can be dropped into the plugins directory and registered for use.

**Key Features:**
- Self-installing fat JAR with OS-specific wrapper scripts
- YAML-based configuration for arguments and plugins
- Dynamic plugin loading from JAR files
- Built-in help and plugin management commands
- Zero configuration required - just download and run setup

## Quick Start

### Installation

1. **Download** the `commander.jar` file (fat JAR with all dependencies)

2. **Run setup** to install Commander:
   ```bash
   java -jar commander.jar --setup
   ```

3. **Add to PATH** (if needed):
   - **Linux/macOS**: The installer will tell you if `~/.local/bin` needs to be added to PATH
   - **Windows**: Add `%LOCALAPPDATA%\Programs\Commander` to your PATH environment variable

4. **Verify installation**:
   ```bash
   commander --help
   ```

### Installation Locations

Commander installs to OS-specific locations:

**Linux:**
- JAR: `~/.local/lib/commander/commander.jar`
- Script: `~/.local/bin/commander`
- Config: `~/.config/Commander/`

**macOS:**
- JAR: `~/Library/Application Support/Commander/commander.jar`
- Script: `~/.local/bin/commander`
- Config: `~/Library/Application Support/Commander/`

**Windows:**
- JAR: `%LOCALAPPDATA%\Programs\Commander\commander.jar`
- Script: `%LOCALAPPDATA%\Programs\Commander\commander.bat`
- Config: `%APPDATA%\Commander\`

## Architecture

### Execution Flow

1. User runs: `commander [options]` or `commander <plugin-name> [plugin-args...]`
2. Commander loads its `arguments.yaml` and parses built-in options
3. If a built-in command is detected (e.g., `--setup`, `--help`), execute it and exit
4. Otherwise, treat the first argument as a plugin name
5. Look up the plugin in `plugin.yaml` registry
6. Load the plugin JAR dynamically *(in progress)*
7. Extract the plugin's `arguments.yaml` from the JAR *(in progress)*
8. Parse remaining arguments using Apache Commons CLI *(in progress)*
9. Instantiate and execute the plugin *(in progress)*

### Configuration Directory Structure

Commander stores its configuration in OS-specific locations:

- **Windows**: `%APPDATA%/Commander`
- **Linux**: `~/.config/Commander`
- **macOS**: `~/Library/Application Support/Commander`

```
~/.config/Commander/
├── plugin.yaml           # Registry of installed plugins
├── arguments.yaml        # Commander's own CLI arguments
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

## Commander Built-in Commands

### Help

Display usage information and available commands:

```bash
commander --help
commander -h
commander        # No arguments also shows help
```

### Setup ✅ Implemented

Initialize and install Commander:

```bash
commander --setup
java -jar commander.jar --setup  # First-time setup
```

This command:
- Creates configuration directory (OS-specific location)
- Creates `plugins/` subdirectory
- Generates default `plugin.yaml` registry with helpful comments
- Generates default `arguments.yaml` for Commander
- **Installs** `commander.jar` to the lib directory
- **Creates** and installs OS-specific wrapper script (`commander` or `commander.bat`)
- **Makes** the script executable (Unix/Linux/macOS)
- **Checks** if bin directory is in PATH and provides instructions if needed

### List Plugins ✅ Implemented

List all installed plugins:

```bash
commander --list
commander -l
```

Shows all registered plugins from `plugin.yaml` or a helpful message if no plugins are installed.

### Generate Plugin Template ⏳ Planned

Generate a plugin template/stub for development:

```bash
commander --generate-plugin <plugin-name>
```

This will create a plugin project template with:
- Plugin class skeleton implementing the `Plugin` interface
- Sample `arguments.yaml`
- Maven build configuration
- README with development instructions

*Status: Command placeholder exists, implementation pending*

## Usage

### Using Commander

```bash
# Show help
commander --help

# List installed plugins
commander --list

# Run setup (installs Commander)
commander --setup
```

### Installing a Plugin ⏳ Manual Process (Automated loading pending)

1. **Copy** the plugin JAR to the plugins directory:
   - Linux: `~/.config/Commander/plugins/`
   - macOS: `~/Library/Application Support/Commander/plugins/`
   - Windows: `%APPDATA%\Commander\plugins\`

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
   commander my-plugin [args...]
   ```
   *Note: Automatic plugin loading is in progress*

## Developing Plugins

### Plugin Development Steps ⏳ In Progress

1. **Generate a plugin template** *(planned)*:
   ```bash
   commander --generate-plugin my-plugin
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
   - Copy JAR to `~/.config/Commander/plugins/`
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

## Building Commander from Source

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
   target/commander.jar
   ```

4. **Install it**:
   ```bash
   java -jar target/commander.jar --setup
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
Commander/
├── src/
│   ├── main/
│   │   ├── java/solutions/cloudbusiness/cli/
│   │   │   ├── App.java              # Main entry point
│   │   │   ├── Setup.java            # Setup command implementation
│   │   │   ├── Plugin.java           # Plugin interface
│   │   │   ├── PluginLoader.java     # Loads plugins from YAML
│   │   │   └── ArgumentParser.java   # YAML-based argument parser
│   │   └── resources/
│   │       ├── commander.sh          # Unix wrapper script template
│   │       └── commander.bat         # Windows wrapper script template
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

TBD

## Contributing

TBD