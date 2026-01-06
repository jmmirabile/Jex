# ${PLUGIN_NAME_CAPITALIZED} Plugin

A Jex plugin.

## Description

TODO: Describe what this plugin does.

## Installation

### Prerequisites

- Java 21 or later
- Maven 3.6+
- Jex installed

### Build

```bash
mvn clean package
```

### Install

```bash
# Copy JAR to Jex plugins directory
cp target/${ARTIFACT_ID}.jar ~/.config/Jex/plugins/

# Register in Jex
# Edit ~/.config/Jex/plugin.yaml and add:
```

```yaml
${PLUGIN_NAME}:
  jar: ${ARTIFACT_ID}.jar
  class: ${PACKAGE_NAME}.${CLASS_NAME}Plugin
  version: 1.0.0
  description: "TODO: Add description"
```

## Usage

```bash
# Show help
jex ${PLUGIN_NAME} --help

# Run the plugin
jex ${PLUGIN_NAME} [options]
```

## Development

### Project Structure

```
${ARTIFACT_ID}/
├── src/
│   ├── main/
│   │   ├── java/${PACKAGE_PATH}/
│   │   │   └── ${CLASS_NAME}Plugin.java
│   │   └── resources/
│   │       └── arguments.yaml
│   └── test/
├── pom.xml
└── README.md
```

### Customization

1. Edit `${CLASS_NAME}Plugin.java` to implement your logic
2. Update `arguments.yaml` to define CLI arguments
3. Update this README with usage information

## License

TODO: Add license information
