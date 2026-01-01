# Commander

A plugin-based CLI framework for Java that allows developers to create modular, extensible command-line applications.

## Overview

Commander is a Java-based CLI framework that enables users to execute plugins through a simple command-line interface. Plugins are self-contained JAR files that can be dropped into the plugins directory and registered for use.

## Architecture

### Execution Flow

1. User runs: `commander <plugin-name> [plugin-args...]` (via wrapper script)
2. Commander checks if the command is a Commander built-in (e.g., `--setup`)
3. If not a built-in, Commander treats the first argument as a plugin name
4. Looks up the plugin in `plugin.yaml` registry
5. Loads the plugin JAR dynamically
6. Extracts the plugin's `arguments.yaml` from the JAR
7. Parses remaining arguments using Apache Commons CLI
8. Executes the plugin with parsed arguments

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

### Setup

Initialize the Commander configuration directory:

```bash
commander --setup
```

This creates:
- Configuration directory (OS-specific location)
- `plugins/` subdirectory
- Default `plugin.yaml` registry
- Default `arguments.yaml` for Commander

### Template Generator

Generate a plugin template/stub for development:

```bash
commander --generate-plugin <plugin-name>
```

This creates a plugin project template with:
- Plugin class skeleton implementing the `Plugin` interface
- Sample `arguments.yaml`
- Build configuration
- README with development instructions

## Usage

### Installing a Plugin

1. Copy the plugin JAR to the `plugins/` directory
2. Register the plugin in `plugin.yaml`
3. Run the plugin: `commander <plugin-name> [args...]`

### Running a Plugin

```bash
# Example: Run my-plugin with arguments
commander my-plugin --input-file data.txt --output-file result.txt --verbose
```

### Listing Available Plugins

```bash
# When no plugin is specified, Commander lists available plugins
commander
```

## Developing Plugins

### Plugin Development Steps

1. Generate a plugin template:
   ```bash
   commander --generate-plugin my-plugin
   ```

2. Implement the `Plugin` interface in your main class

3. Define CLI arguments in `arguments.yaml`

4. Build the plugin JAR with `arguments.yaml` included as a resource

5. Install the plugin:
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
├── pom.xml (or build.gradle)
└── README.md
```

## Dependencies

- **Apache Commons CLI**: Command-line argument parsing
- **SnakeYAML**: YAML configuration file parsing
- **JUnit**: Testing framework

## License

TBD

## Contributing

TBD