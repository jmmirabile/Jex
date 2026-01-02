package solutions.cloudbusiness.cli;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class PluginLoader {

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
    public Plugin loadPlugin(String pluginName, Map<String, Object> pluginConfig) {
        try {
            String jarFileName = (String) pluginConfig.get("jar");
            String className = (String) pluginConfig.get("class");

            if (jarFileName == null || className == null) {
                System.err.println("Error: Plugin configuration missing 'jar' or 'class' field");
                return null;
            }

            // Construct full path to JAR file
            String pluginsDir = getConfigDirectory() + File.separator + "plugins";
            Path jarPath = Paths.get(pluginsDir, jarFileName);

            if (!Files.exists(jarPath)) {
                System.err.println("Error: Plugin JAR not found: " + jarPath);
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

            // Verify it implements the Plugin interface
            if (!(pluginInstance instanceof Plugin)) {
                System.err.println("Error: Class " + className + " does not implement Plugin interface");
                return null;
            }

            return (Plugin) pluginInstance;

        } catch (Exception e) {
            System.err.println("Error loading plugin '" + pluginName + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
