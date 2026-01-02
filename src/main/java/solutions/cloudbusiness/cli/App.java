package solutions.cloudbusiness.cli;

import org.apache.commons.cli.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Commander - Plugin-based CLI Framework
 */
public class App
{

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

    public static void main(String[] args) {
        String configDir = getConfigDirectory();
        String argumentsYamlPath = configDir + File.separator + "arguments.yaml";

        // Load Commander's options from arguments.yaml
        Options commanderOptions = ArgumentParser.loadOptionsFromYaml(argumentsYamlPath);

        // Parse command line arguments
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            // Try to parse as Commander arguments (only if args start with -)
            if (args.length > 0 && args[0].startsWith("-")) {
                cmd = parser.parse(commanderOptions, args, false);
            }
        } catch (ParseException e) {
            // If parsing fails, it might be a plugin command
            cmd = null;
        }

        // Handle Commander built-in commands
        if (cmd != null) {
            if (cmd.hasOption("setup")) {
                Setup.run();
                return;
            }

            if (cmd.hasOption("h") || cmd.hasOption("help")) {
                ArgumentParser.printCommanderHelp(commanderOptions);
                return;
            }

            if (cmd.hasOption("l") || cmd.hasOption("list")) {
                listPlugins();
                return;
            }

            if (cmd.hasOption("generate-plugin")) {
                String pluginName = cmd.getOptionValue("generate-plugin");
                generatePlugin(pluginName);
                return;
            }
        }

        // If no Commander options matched, try to execute as a plugin
        if (args.length > 0) {
            String pluginName = args[0];

            // Load plugins
            String pluginYamlPath = configDir + File.separator + "plugin.yaml";
            PluginLoader loader = new PluginLoader();
            Map<String, List<Map<String, String>>> plugins = loader.loadPlugins(pluginYamlPath);

            if (plugins != null && plugins.containsKey(pluginName)) {
                // TODO: Implement plugin loading and execution
                System.out.println("Plugin found: " + pluginName);
                System.out.println("Plugin execution not yet implemented.");
            } else {
                System.err.println("Error: Unknown command or plugin: " + pluginName);
                System.out.println("\nUse 'commander --help' for usage information.");
                displayAvailablePlugins(plugins);
            }
        } else {
            // No arguments provided
            ArgumentParser.printCommanderHelp(commanderOptions);
        }
    }

    private static void listPlugins() {
        String configDir = getConfigDirectory();
        String pluginYamlPath = configDir + File.separator + "plugin.yaml";

        PluginLoader loader = new PluginLoader();
        Map<String, List<Map<String, String>>> plugins = loader.loadPlugins(pluginYamlPath);

        displayAvailablePlugins(plugins);
    }

    private static void displayAvailablePlugins(Map<String, List<Map<String, String>>> plugins) {
        if (plugins == null || plugins.isEmpty()) {
            System.out.println("\nNo plugins installed.");
            System.out.println("Install plugins by:");
            System.out.println("1. Copying plugin JAR files to: " + getConfigDirectory() + File.separator + "plugins");
            System.out.println("2. Registering them in: " + getConfigDirectory() + File.separator + "plugin.yaml");
            return;
        }

        System.out.println("\nInstalled Plugins:");
        for (String name : plugins.keySet()) {
            System.out.println("  " + name);
        }
        System.out.println("\nUse 'commander <plugin-name> --help' to see plugin-specific options.");
    }

    private static void generatePlugin(String pluginName) {
        if (pluginName == null || pluginName.trim().isEmpty()) {
            System.err.println("Error: Plugin name is required.");
            System.out.println("Usage: commander --generate-plugin <plugin-name>");
            return;
        }

        System.out.println("Generating plugin template: " + pluginName);
        System.out.println("TODO: Plugin template generation not yet implemented.");
    }
}
