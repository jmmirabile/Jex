package solutions.cloudbusiness.cli;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PluginGenerator {

    public static void generate(String pluginName, String javaPackage) {
        if (pluginName == null || pluginName.trim().isEmpty()) {
            System.err.println("Error: Plugin name cannot be empty");
            return;
        }

        // Sanitize plugin name (lowercase, hyphens)
        String sanitizedName = pluginName.toLowerCase().replaceAll("[^a-z0-9-]", "-");
        String className = toCamelCase(sanitizedName);

        // Determine package name
        String packageName;
        if (javaPackage != null && !javaPackage.trim().isEmpty()) {
            packageName = javaPackage;
        } else {
            packageName = "com.example." + sanitizedName.replace("-", "");
        }

        // Display header
        System.out.println();
        System.out.println("Commander Plugin Generator");
        System.out.println("==========================");
        System.out.println();
        System.out.println("This will create a Java Maven project with:");
        System.out.println("  • Plugin class implementing the Plugin interface");
        System.out.println("  • arguments.yaml for CLI argument definitions");
        System.out.println("  • pom.xml configured for Commander plugins");
        System.out.println("  • README.md with usage instructions");
        System.out.println("  • Complete project structure ready to open in your IDE");
        System.out.println();

        // Prompt for directory
        String targetDir = promptForDirectory();
        if (targetDir == null) {
            System.out.println("Plugin generation cancelled.");
            return;
        }

        Path projectPath = Paths.get(targetDir, sanitizedName + "-plugin");

        // Check if directory already exists
        if (Files.exists(projectPath)) {
            System.err.println("Error: Directory already exists: " + projectPath);
            System.err.println("Please choose a different location or plugin name.");
            return;
        }

        try {
            System.out.println("\nCreating " + sanitizedName + "-plugin at: " + projectPath);
            System.out.println();

            // Create project structure
            createProjectStructure(projectPath, packageName);

            // Generate files
            generatePomXml(projectPath, sanitizedName);
            generatePluginClass(projectPath, packageName, className, sanitizedName);
            generateArgumentsYaml(projectPath, sanitizedName);
            generateReadme(projectPath, sanitizedName, className, packageName);
            generateGitignore(projectPath);

            System.out.println();
            System.out.println("Project created successfully!");
            System.out.println();
            System.out.println("Next steps:");
            System.out.println("1. cd " + projectPath);
            System.out.println("2. Open in your IDE (IntelliJ, Eclipse, VS Code)");
            if (javaPackage == null) {
                System.out.println("3. (Optional) Customize package name in pom.xml and " + className + "Plugin.java");
                System.out.println("   Current package: " + packageName);
            }
            System.out.println((javaPackage == null ? "4" : "3") + ". Implement your plugin logic in " + className + "Plugin.java");
            System.out.println((javaPackage == null ? "5" : "4") + ". Build: mvn clean package");
            System.out.println((javaPackage == null ? "6" : "5") + ". Install plugin (see README.md)");
            System.out.println();
            System.out.println("See README.md for detailed instructions.");

        } catch (IOException e) {
            System.err.println("Error generating plugin project: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String promptForDirectory() {
        String currentDir = System.getProperty("user.dir");

        System.out.println("Where would you like to create the project?");
        System.out.print("(Press Enter for current directory: " + currentDir + ")\n> ");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String input = reader.readLine();

            if (input == null || input.trim().isEmpty()) {
                return currentDir;
            }

            // Expand ~ to home directory
            if (input.startsWith("~")) {
                input = System.getProperty("user.home") + input.substring(1);
            }

            Path path = Paths.get(input);
            if (!Files.exists(path)) {
                System.out.print("Directory does not exist. Create it? (y/N): ");
                String confirm = reader.readLine();
                if (confirm != null && confirm.trim().equalsIgnoreCase("y")) {
                    Files.createDirectories(path);
                    return path.toString();
                } else {
                    return null;
                }
            }

            if (!Files.isDirectory(path)) {
                System.err.println("Error: Path is not a directory: " + path);
                return null;
            }

            return path.toString();

        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
            return null;
        }
    }

    private static void createProjectStructure(Path projectPath, String packageName) throws IOException {
        String packagePath = packageName.replace(".", "/");

        // Create directories
        Files.createDirectories(projectPath.resolve("src/main/java/" + packagePath));
        Files.createDirectories(projectPath.resolve("src/main/resources"));
        Files.createDirectories(projectPath.resolve("src/test/java/" + packagePath));

        System.out.println("✓ Created project structure");
    }

    private static void generatePomXml(Path projectPath, String pluginName) throws IOException {
        String artifactId = pluginName + "-plugin";
        String content = String.format("""
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>%s</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>%s Plugin</name>
    <description>A Commander plugin</description>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Commander Plugin Interface -->
        <dependency>
            <groupId>solutions.cloudbusiness.cli</groupId>
            <artifactId>Commander</artifactId>
            <version>1.0-SNAPSHOT</version>
            <scope>provided</scope>
        </dependency>

        <!-- Apache Commons CLI -->
        <dependency>
            <groupId>commons-cli</groupId>
            <artifactId>commons-cli</artifactId>
            <version>1.11.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.2.0</version>
            </plugin>
        </plugins>
    </build>
</project>
""", artifactId, capitalize(pluginName));

        writeFile(projectPath.resolve("pom.xml"), content);
        System.out.println("✓ Generated pom.xml");
    }

    private static void generatePluginClass(Path projectPath, String packageName, String className, String pluginName) throws IOException {
        String packagePath = packageName.replace(".", "/");
        String content = String.format("""
package %s;

import solutions.cloudbusiness.cli.Plugin;
import org.apache.commons.cli.*;

public class %sPlugin implements Plugin {

    @Override
    public String getName() {
        return "%s";
    }

    @Override
    public void execute(String[] args) {
        // Create options
        Options options = new Options();
        options.addOption("h", "help", false, "Display help information");
        // TODO: Add your custom options here

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                formatter.printHelp("commander %s", options);
                return;
            }

            // TODO: Implement your plugin logic here
            System.out.println("%s plugin executed successfully!");

        } catch (ParseException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            formatter.printHelp("commander %s", options);
            System.exit(1);
        }
    }

    @Override
    public String[] getCommandLineOptions() {
        // TODO: Return your command line options
        return new String[]{"--help", "-h"};
    }
}
""", packageName, className, pluginName, pluginName, capitalize(pluginName), pluginName);

        Path javaFile = projectPath.resolve("src/main/java/" + packagePath + "/" + className + "Plugin.java");
        writeFile(javaFile, content);
        System.out.println("✓ Generated " + className + "Plugin.java");
    }

    private static void generateArgumentsYaml(Path projectPath, String pluginName) throws IOException {
        String content = String.format("""
# %s Plugin CLI Arguments

options:
  - name: help
    short: h
    long: help
    description: "Display help information"
    required: false
    hasArg: false

  # TODO: Add your custom arguments here
  # Example:
  # - name: input
  #   short: i
  #   long: input-file
  #   description: "Input file path"
  #   required: true
  #   hasArg: true
  #   argName: "file"
""", capitalize(pluginName));

        writeFile(projectPath.resolve("src/main/resources/arguments.yaml"), content);
        System.out.println("✓ Generated arguments.yaml");
    }

    private static void generateReadme(Path projectPath, String pluginName, String className, String packageName) throws IOException {
        String content = String.format("""
# %s Plugin

A Commander plugin.

## Description

TODO: Describe what this plugin does.

## Installation

### Prerequisites

- Java 21 or later
- Maven 3.6+
- Commander installed

### Build

```bash
mvn clean package
```

### Install

```bash
# Copy JAR to Commander plugins directory
cp target/%s-plugin-1.0.0.jar ~/.config/Commander/plugins/

# Register in Commander
# Edit ~/.config/Commander/plugin.yaml and add:
```

```yaml
%s:
  jar: %s-plugin-1.0.0.jar
  class: %s.%sPlugin
  version: 1.0.0
  description: "TODO: Add description"
```

## Usage

```bash
# Show help
commander %s --help

# Run the plugin
commander %s [options]
```

## Development

### Project Structure

```
%s-plugin/
├── src/
│   ├── main/
│   │   ├── java/%s/
│   │   │   └── %sPlugin.java
│   │   └── resources/
│   │       └── arguments.yaml
│   └── test/
├── pom.xml
└── README.md
```

### Customization

1. Edit `%sPlugin.java` to implement your logic
2. Update `arguments.yaml` to define CLI arguments
3. Update this README with usage information

## License

TODO: Add license information
""", capitalize(pluginName), pluginName, pluginName, pluginName, packageName, className,
    pluginName, pluginName, pluginName, packageName.replace(".", "/"), className, className);

        writeFile(projectPath.resolve("README.md"), content);
        System.out.println("✓ Generated README.md");
    }

    private static void generateGitignore(Path projectPath) throws IOException {
        String content = """
# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
dependency-reduced-pom.xml

# IntelliJ IDEA
.idea/
*.iml
*.iws
*.ipr

# Eclipse
.classpath
.project
.settings/

# VS Code
.vscode/

# macOS
.DS_Store

# Build artifacts
*.jar
*.war
*.class
""";

        writeFile(projectPath.resolve(".gitignore"), content);
        System.out.println("✓ Generated .gitignore");
    }

    private static void writeFile(Path filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write(content);
        }
    }

    private static String toCamelCase(String input) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : input.toCharArray()) {
            if (c == '-' || c == '_') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    private static String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}