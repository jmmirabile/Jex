package org.jex.cli;

import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.net.URL;
import java.net.URLDecoder;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

/**
 * Jex - JexPlugin-based CLI Framework
 */
public class Jex
{

    private static void println(String msg) {
        System.out.println(msg);
    }

    public static void printJexHelp() {
        System.out.println("Jex - JexPlugin-based CLI Framework");
        System.out.println("\nUsage:");
        System.out.println("  jex [options]           - Run Jex built-in commands");
        System.out.println("  jex <plugin> [args...]  - Run a plugin");
        System.out.println("\nBuilt-in Commands:");
        System.out.println("     --install                          Install Jex (create directories, install JAR, wrapper scripts)");
        System.out.println("  -l,--list                             List all installed plugins");
        System.out.println("  -h,--help                             Display help information");
        System.out.println("  -v,--version                          Display version");
        System.out.println("\nPlugin Management:");
        System.out.println("     --install-plugin <name> --jar <file>    Install a plugin");
        System.out.println("     --update-plugin <name> --jar <file>     Update an existing plugin");
        System.out.println("     --uninstall-plugin <name>               Uninstall a plugin");

        System.out.println("\nExamples:");
        System.out.println("  jex --install                                  Install Jex");
        System.out.println("  jex --list                                     List installed plugins");
        System.out.println("  jex new-plugin my-tool                         Create a new plugin project");
        System.out.println("  jex new-plugin my-tool --package com.example   With custom package");
        System.out.println("  jex --install-plugin my-tool --jar target/my-tool-plugin.jar");
        System.out.println("  jex --update-plugin my-tool --jar target/my-tool-plugin.jar");
        System.out.println("  jex --uninstall-plugin my-tool");
        System.out.println("  jex <plugin-name> --help                       Show plugin help");
    }

    public static void main(String[] args) {
        // Check for --install, -h, --help FIRST (before loading arguments.yaml which may not exist yet)
        if (args.length > 0) {
            String firstArg = args[0];

            if (firstArg.equals("--install")) {
                Install.run();
                return;
            }

            if (firstArg.equals("-h") || firstArg.equals("--help")) {
                printJexHelp();
                return;
            }

            if (firstArg.equals("-l") || firstArg.equals("--list")) {
                listPlugins();
                return;
            }

            if (firstArg.equals("-v") || firstArg.equals("--version")) {
                System.out.println(JexMavenUtil.getVersion());
                return;
            }

            // Plugin management commands
            if (firstArg.equals("--install-plugin")) {
                if (args.length < 3 || !args[2].equals("--jar")) {
                    System.err.println("Error: Usage: jex --install-plugin <name> --jar <jar-file>");
                    System.exit(1);
                }
                String name = args.length > 1 ? args[1] : null;
                String jarPath = args.length > 3 ? args[3] : null;

                if (name == null || jarPath == null) {
                    System.err.println("Error: Both plugin name and JAR file are required");
                    System.exit(1);
                }

                try {
                    PluginManager manager = new PluginManager();
                    manager.installPlugin(name, jarPath);
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                    System.exit(1);
                }
                return;
            }

            if (firstArg.equals("--update-plugin")) {
                if (args.length < 3 || !args[2].equals("--jar")) {
                    System.err.println("Error: Usage: jex --update-plugin <name> --jar <jar-file>");
                    System.exit(1);
                }
                String name = args.length > 1 ? args[1] : null;
                String jarPath = args.length > 3 ? args[3] : null;

                if (name == null || jarPath == null) {
                    System.err.println("Error: Both plugin name and JAR file are required");
                    System.exit(1);
                }

                try {
                    PluginManager manager = new PluginManager();
                    manager.updatePlugin(name, jarPath);
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                    System.exit(1);
                }
                return;
            }

            if (firstArg.equals("--uninstall-plugin")) {
                if (args.length < 2) {
                    System.err.println("Error: Usage: jex --uninstall-plugin <name>");
                    System.exit(1);
                }
                String name = args[1];

                try {
                    PluginManager manager = new PluginManager();
                    manager.uninstallPlugin(name);
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                    System.exit(1);
                }
                return;
            }

        }

        //String argumentsYamlPath = PathConfig.getArgumentsYamlPath();

        // Load Jex's options from arguments.yaml
        //Options jexOptions = ArgumentParser.loadOptionsFromYaml(argumentsYamlPath);

        // Parse command line arguments
        //CommandLineParser parser = new DefaultParser();
        //CommandLine cmd = null;
        /**
        try {
            // Try to parse as Jex arguments (only if args start with -)
            if (args.length > 0 && args[0].startsWith("-")) {
                cmd = parser.parse(jexOptions, args, false);
            }
        } catch (ParseException e) {
            // If parsing fails, it might be a plugin command
            cmd = null;
        }
         **/


        // If no Jex options matched, try to execute as a plugin
        if (args.length > 0) {
            String pluginName = args[0];

            // Check internal plugins first
            Map<String, JexPlugin> internalPlugins = discoverInternalPlugins();
            if (internalPlugins.containsKey(pluginName)) {
                JexPlugin plugin = internalPlugins.get(pluginName);

                // Pass remaining arguments to the plugin (skip the plugin name)
                String[] pluginArgs = new String[args.length - 1];
                System.arraycopy(args, 1, pluginArgs, 0, args.length - 1);

                // Execute the plugin
                plugin.execute(pluginArgs);
                return;
            }

            // Load external plugin registry
            PluginLoader loader = new PluginLoader();
            Map<String, Map<String, Object>> plugins = loader.loadPluginRegistry(PathConfig.getPluginYamlPath());

            if (plugins != null && plugins.containsKey(pluginName)) {
                // Load and execute the plugin
                Map<String, Object> pluginConfig = plugins.get(pluginName);
                JexPlugin plugin = loader.loadPlugin(pluginName, pluginConfig);

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
            printJexHelp();
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

    /**
     * Discover all internal plugins in the org.jex.plugins package.
     * Automatically scans for classes implementing JexPlugin interface.
     */
    private static Map<String, JexPlugin> discoverInternalPlugins() {
        Map<String, JexPlugin> plugins = new HashMap<>();

        try {
            String packageName = "org.jex.plugins";
            ClassLoader classLoader = Jex.class.getClassLoader();
            String path = packageName.replace('.', '/');

            // Find all resources in org/jex/plugins package
            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();

                if (resource.getProtocol().equals("jar")) {
                    // Extract classes from JAR
                    String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
                    JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));

                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();

                        // Check if it's in org/jex/plugins/ and is a .class file
                        if (name.startsWith("org/jex/plugins/") && name.endsWith(".class")) {
                            // Convert to class name
                            String className = name.replace('/', '.').substring(0, name.length() - 6);

                            try {
                                Class<?> clazz = Class.forName(className);

                                // Check if it implements JexPlugin interface and is not an interface itself
                                if (JexPlugin.class.isAssignableFrom(clazz) && !clazz.isInterface()) {
                                    JexPlugin plugin = (JexPlugin) clazz.getDeclaredConstructor().newInstance();
                                    plugins.put(plugin.getName(), plugin);
                                }
                            } catch (Exception e) {
                                // Skip classes that can't be loaded or instantiated
                            }
                        }
                    }
                    jar.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Error discovering internal plugins: " + e.getMessage());
        }

        return plugins;
    }
}
