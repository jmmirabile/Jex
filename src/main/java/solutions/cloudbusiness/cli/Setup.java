package solutions.cloudbusiness.cli;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Setup {

    private static String getConfigDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String appName = "Commander";

        if (os.contains("win")) {
            return System.getenv("APPDATA") + File.separator + appName;
        } else if (os.contains("mac")) {
            return System.getProperty("user.home") + "/Library/Application Support/" + appName;
        } else {
            return System.getProperty("user.home") + "/.config/" + appName;
        }
    }

    private static String getLibDirectory() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return System.getenv("LOCALAPPDATA") + File.separator + "Programs" + File.separator + "Commander";
        } else if (os.contains("mac")) {
            return System.getProperty("user.home") + "/Library/Application Support/Commander";
        } else {
            return System.getProperty("user.home") + "/.local/lib/commander";
        }
    }

    private static String getBinDirectory() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return getLibDirectory(); // On Windows, keep in same directory
        } else {
            return System.getProperty("user.home") + "/.local/bin";
        }
    }

    private static String getJarPath() {
        try {
            return new File(Setup.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        } catch (URISyntaxException e) {
            System.err.println("Error locating JAR file: " + e.getMessage());
            return null;
        }
    }

    public static void run() {
        String configDir = getConfigDirectory();
        Path configPath = Paths.get(configDir);
        Path pluginsPath = Paths.get(configDir, "plugins");
        Path pluginYamlPath = Paths.get(configDir, "plugin.yaml");
        Path argumentsYamlPath = Paths.get(configDir, "arguments.yaml");

        try {
            // Create configuration directory
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath);
                System.out.println("Created configuration directory: " + configPath);
            } else {
                System.out.println("Configuration directory already exists: " + configPath);
            }

            // Create plugins subdirectory
            if (!Files.exists(pluginsPath)) {
                Files.createDirectories(pluginsPath);
                System.out.println("Created plugins directory: " + pluginsPath);
            } else {
                System.out.println("Plugins directory already exists: " + pluginsPath);
            }

            // Create default plugin.yaml
            if (!Files.exists(pluginYamlPath)) {
                createDefaultPluginYaml(pluginYamlPath.toString());
                System.out.println("Created default plugin.yaml: " + pluginYamlPath);
            } else {
                System.out.println("plugin.yaml already exists: " + pluginYamlPath);
            }

            // Create default arguments.yaml for Commander
            if (!Files.exists(argumentsYamlPath)) {
                createDefaultArgumentsYaml(argumentsYamlPath.toString());
                System.out.println("Created default arguments.yaml: " + argumentsYamlPath);
            } else {
                System.out.println("arguments.yaml already exists: " + argumentsYamlPath);
            }

            // Install Commander JAR
            installCommanderJar();

            // Install wrapper script
            installWrapperScript();

            System.out.println("\n" + "=".repeat(60));
            System.out.println("Commander setup completed successfully!");
            System.out.println("=".repeat(60));
            System.out.println("\nConfiguration directory: " + configPath);
            System.out.println("Installation directory: " + getLibDirectory());
            System.out.println("\nYou can now install plugins by:");
            System.out.println("1. Copying plugin JAR files to: " + pluginsPath);
            System.out.println("2. Registering them in: " + pluginYamlPath);

        } catch (IOException e) {
            System.err.println("Error during setup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void installCommanderJar() throws IOException {
        String jarPath = getJarPath();
        if (jarPath == null) {
            System.err.println("Warning: Could not locate JAR file. Skipping JAR installation.");
            return;
        }

        String libDir = getLibDirectory();
        Path libPath = Paths.get(libDir);
        Path targetJar = Paths.get(libDir, "commander.jar");

        // Create lib directory if it doesn't exist
        if (!Files.exists(libPath)) {
            Files.createDirectories(libPath);
            System.out.println("Created library directory: " + libPath);
        }

        // Copy JAR to lib directory
        Files.copy(Paths.get(jarPath), targetJar, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Installed Commander JAR to: " + targetJar);
    }

    private static void installWrapperScript() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        String binDir = getBinDirectory();
        Path binPath = Paths.get(binDir);

        // Create bin directory if it doesn't exist
        if (!Files.exists(binPath)) {
            Files.createDirectories(binPath);
            System.out.println("Created bin directory: " + binPath);
        }

        if (os.contains("win")) {
            installWindowsScript(binPath);
        } else {
            installUnixScript(binPath);
        }
    }

    private static void installUnixScript(Path binPath) throws IOException {
        Path scriptPath = binPath.resolve("commander");

        // Read the script template from resources
        try (InputStream is = Setup.class.getResourceAsStream("/commander.sh");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is));
             FileWriter writer = new FileWriter(scriptPath.toFile())) {

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line + "\n");
            }
        }

        // Make executable
        scriptPath.toFile().setExecutable(true, false);
        System.out.println("Installed wrapper script to: " + scriptPath);

        // Create jcmd symlink
        Path jcmdPath = binPath.resolve("jcmd");
        try {
            // Remove existing symlink if it exists
            Files.deleteIfExists(jcmdPath);
            // Create symlink
            Files.createSymbolicLink(jcmdPath, Paths.get("commander"));
            System.out.println("Created jcmd symlink to: " + jcmdPath);
        } catch (Exception e) {
            System.out.println("Note: Could not create jcmd symlink: " + e.getMessage());
        }

        // Check if bin directory is in PATH
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null || !pathEnv.contains(binPath.toString())) {
            System.out.println("\n" + "!".repeat(60));
            System.out.println("IMPORTANT: Add the following to your PATH:");
            System.out.println("export PATH=\"$PATH:" + binPath + "\"");
            System.out.println("\nAdd this line to your ~/.bashrc, ~/.zshrc, or ~/.profile");
            System.out.println("!".repeat(60));
        } else {
            System.out.println("✓ Bin directory is already in PATH");
            System.out.println("\nYou can now run Commander using: commander or jcmd");
        }
    }

    private static void installWindowsScript(Path binPath) throws IOException {
        Path scriptPath = binPath.resolve("commander.bat");

        // Read the script template from resources
        try (InputStream is = Setup.class.getResourceAsStream("/commander.bat");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is));
             FileWriter writer = new FileWriter(scriptPath.toFile())) {

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line + "\r\n"); // Windows line endings
            }
        }

        System.out.println("Installed wrapper script to: " + scriptPath);

        // Create jcmd.bat copy
        Path jcmdPath = binPath.resolve("jcmd.bat");
        try {
            Files.copy(scriptPath, jcmdPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Created jcmd.bat copy to: " + jcmdPath);
        } catch (Exception e) {
            System.out.println("Note: Could not create jcmd.bat: " + e.getMessage());
        }

        // Check if bin directory is in PATH
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null || !pathEnv.contains(binPath.toString())) {
            System.out.println("\n" + "!".repeat(60));
            System.out.println("IMPORTANT: Add the following to your PATH:");
            System.out.println(binPath);
            System.out.println("\nInstructions:");
            System.out.println("1. Open System Properties > Environment Variables");
            System.out.println("2. Edit the PATH variable for your user");
            System.out.println("3. Add: " + binPath);
            System.out.println("!".repeat(60));
        } else {
            System.out.println("✓ Bin directory is already in PATH");
            System.out.println("\nYou can now run Commander using: commander or jcmd");
        }
    }

    private static void createDefaultPluginYaml(String path) throws IOException {
        String defaultContent = "# Commander Plugin Registry\n" +
                "# Add your plugins here in the following format:\n" +
                "#\n" +
                "# plugin-name:\n" +
                "#   jar: plugin-file.jar\n" +
                "#   class: com.example.PluginClassName\n" +
                "#   version: 1.0.0\n" +
                "#   description: \"Plugin description\"\n" +
                "\n" +
                "# Example plugin (commented out):\n" +
                "# example-plugin:\n" +
                "#   jar: example-plugin.jar\n" +
                "#   class: com.example.ExamplePlugin\n" +
                "#   version: 1.0.0\n" +
                "#   description: \"An example plugin\"\n";

        try (FileWriter writer = new FileWriter(path)) {
            writer.write(defaultContent);
        }
    }

    private static void createDefaultArgumentsYaml(String path) throws IOException {
        String defaultContent = "# Commander CLI Arguments\n" +
                "# This file defines Commander's built-in command-line arguments\n" +
                "\n" +
                "options:\n" +
                "  - name: setup\n" +
                "    long: setup\n" +
                "    description: \"Initialize Commander configuration directory\"\n" +
                "    required: false\n" +
                "    hasArg: false\n" +
                "\n" +
                "  - name: generate-plugin\n" +
                "    long: generate-plugin\n" +
                "    description: \"Generate a plugin template\"\n" +
                "    required: false\n" +
                "    hasArg: true\n" +
                "    argName: \"plugin-name\"\n" +
                "\n" +
                "  - name: list\n" +
                "    short: l\n" +
                "    long: list\n" +
                "    description: \"List all installed plugins\"\n" +
                "    required: false\n" +
                "    hasArg: false\n" +
                "\n" +
                "  - name: help\n" +
                "    short: h\n" +
                "    long: help\n" +
                "    description: \"Display help information\"\n" +
                "    required: false\n" +
                "    hasArg: false\n";

        try (FileWriter writer = new FileWriter(path)) {
            writer.write(defaultContent);
        }
    }
}