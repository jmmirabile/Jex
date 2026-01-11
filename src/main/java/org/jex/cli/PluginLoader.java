package org.jex.cli;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class PluginLoader {

    /**
     * Load plugin registry from plugin.yaml
     */
    @SuppressWarnings("unchecked")
    public Map<String, Map<String, Object>> loadPluginRegistry(String configFile) {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = Files.newInputStream(Paths.get(configFile))) {
            Map<String, Map<String, Object>> registry = yaml.load(inputStream);
            // Filter out null entries and comments
            if (registry != null) {
                registry.entrySet().removeIf(entry -> entry.getValue() == null);
            }
            return registry;
        } catch (Exception e) {
            System.err.println("Error loading plugin registry: " + e.getMessage());
            return null;
        }
    }

    /**
     * Load and instantiate a plugin from a JAR file
     */
    public JexPlugin loadPlugin(String pluginName, Map<String, Object> pluginConfig) {
        try {
            String jarFileName = (String) pluginConfig.get("jar");
            String className = (String) pluginConfig.get("class");

            if (jarFileName == null || className == null) {
                System.err.println("Error: JexPlugin configuration missing 'jar' or 'class' field");
                return null;
            }

            // Construct full path to JAR file
            Path jarPath = Paths.get(PathConfig.getPluginsDirectory(), jarFileName);

            if (!Files.exists(jarPath)) {
                System.err.println("Error: JexPlugin JAR not found: " + jarPath);
                return null;
            }

            // Load the JAR file
            URL jarUrl = jarPath.toUri().toURL();
            URLClassLoader classLoader = new URLClassLoader(
                new URL[]{jarUrl},
                this.getClass().getClassLoader()
            );

            // Load the plugin class
            Class<?> pluginClass = classLoader.loadClass(className);

            // Instantiate the plugin
            Object pluginInstance = pluginClass.getDeclaredConstructor().newInstance();

            // Verify it implements the JexPlugin interface
            if (!(pluginInstance instanceof JexPlugin)) {
                System.err.println("Error: Class " + className + " does not implement JexPlugin interface");
                return null;
            }

            return (JexPlugin) pluginInstance;

        } catch (Exception e) {
            System.err.println("Error loading plugin '" + pluginName + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
