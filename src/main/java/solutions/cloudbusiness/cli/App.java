package solutions.cloudbusiness.cli;

import org.apache.commons.cli.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Jex - Plugin-based CLI Framework
 */
public class App
{

    public static void main(String[] args) {
        // Check for --setup, -h, --help FIRST (before loading arguments.yaml which may not exist yet)
        if (args.length > 0) {
            String firstArg = args[0];

            if (firstArg.equals("--setup")) {
                Setup.run();
                return;
            }

            if (firstArg.equals("-h") || firstArg.equals("--help")) {
                ArgumentParser.printJexHelp(null);
                return;
            }
        }

        String argumentsYamlPath = PathConfig.getArgumentsYamlPath();

        // Load Jex's options from arguments.yaml
        Options jexOptions = ArgumentParser.loadOptionsFromYaml(argumentsYamlPath);

        // Parse command line arguments
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            // Try to parse as Jex arguments (only if args start with -)
            if (args.length > 0 && args[0].startsWith("-")) {
                cmd = parser.parse(jexOptions, args, false);
            }
        } catch (ParseException e) {
            // If parsing fails, it might be a plugin command
            cmd = null;
        }

        // Handle Jex built-in commands
        if (cmd != null) {
            if (cmd.hasOption("setup")) {
                Setup.run();
                return;
            }

            if (cmd.hasOption("h") || cmd.hasOption("help")) {
                ArgumentParser.printJexHelp(jexOptions);
                return;
            }

            if (cmd.hasOption("l") || cmd.hasOption("list")) {
                listPlugins();
                return;
            }

            if (cmd.hasOption("generate-plugin")) {
                String pluginName = cmd.getOptionValue("generate-plugin");
                String javaPackage = cmd.getOptionValue("java-package");
                generatePlugin(pluginName, javaPackage);
                return;
            }
        }

        // If no Commander options matched, try to execute as a plugin
        if (args.length > 0) {
            String pluginName = args[0];

            // Load plugin registry
            PluginLoader loader = new PluginLoader();
            Map<String, Map<String, Object>> plugins = loader.loadPluginRegistry(PathConfig.getPluginYamlPath());

            if (plugins != null && plugins.containsKey(pluginName)) {
                // Load and execute the plugin
                Map<String, Object> pluginConfig = plugins.get(pluginName);
                Plugin plugin = loader.loadPlugin(pluginName, pluginConfig);

                if (plugin != null) {
                    // Pass remaining arguments to the plugin (skip the plugin name)
                    String[] pluginArgs = new String[args.length - 1];
                    System.arraycopy(args, 1, pluginArgs, 0, args.length - 1);

                    // Execute the plugin
                    plugin.execute(pluginArgs);
                } else {
                    System.err.println("Error: Failed to load plugin: " + pluginName);
                    System.exit(1);
                }
            } else {
                System.err.println("Error: Unknown command or plugin: " + pluginName);
                System.out.println("\nUse 'jex --help' for usage information.");
                displayAvailablePlugins(plugins);
                System.exit(1);
            }
        } else {
            // No arguments provided
            ArgumentParser.printJexHelp(jexOptions);
        }
    }

    private static void listPlugins() {
        PluginLoader loader = new PluginLoader();
        Map<String, Map<String, Object>> plugins = loader.loadPluginRegistry(PathConfig.getPluginYamlPath());

        displayAvailablePlugins(plugins);
    }

    private static void displayAvailablePlugins(Map<String, Map<String, Object>> plugins) {
        if (plugins == null || plugins.isEmpty()) {
            System.out.println("\nNo plugins installed.");
            System.out.println("Install plugins by:");
            System.out.println("1. Copying plugin JAR files to: " + PathConfig.getPluginsDirectory());
            System.out.println("2. Registering them in: " + PathConfig.getPluginYamlPath());
            return;
        }

        System.out.println("\nInstalled Plugins:");
        for (Map.Entry<String, Map<String, Object>> entry : plugins.entrySet()) {
            String name = entry.getKey();
            Map<String, Object> config = entry.getValue();
            String description = (String) config.get("description");
            String version = (String) config.get("version");

            if (description != null && version != null) {
                System.out.println("  " + name + " (v" + version + ") - " + description);
            } else if (description != null) {
                System.out.println("  " + name + " - " + description);
            } else {
                System.out.println("  " + name);
            }
        }
        System.out.println("\nUse 'jex <plugin-name> --help' to see plugin-specific options.");
    }

    private static void generatePlugin(String pluginName, String javaPackage) {
        if (pluginName == null || pluginName.trim().isEmpty()) {
            System.err.println("Error: Plugin name is required.");
            System.out.println("Usage: jex --generate-plugin <plugin-name> [--java-package <package>]");
            return;
        }

        PluginGenerator.generate(pluginName, javaPackage);
    }
}
