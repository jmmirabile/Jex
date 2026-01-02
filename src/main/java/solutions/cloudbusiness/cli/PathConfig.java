package solutions.cloudbusiness.cli;

import java.io.File;

/**
 * Centralized OS detection and path configuration for Jex.
 * Detects the operating system once and provides consistent path management.
 */
public class PathConfig {

    // OS Detection (done once, statically)
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_WINDOWS = OS_NAME.contains("win");
    private static final boolean IS_MAC = OS_NAME.contains("mac");
    private static final boolean IS_UNIX = !IS_WINDOWS && !IS_MAC; // Linux, BSD, etc.

    private static final String APP_NAME = "Jex";

    // Prevent instantiation
    private PathConfig() {
        throw new AssertionError("PathConfig is a utility class and should not be instantiated");
    }

    /**
     * Get the configuration directory path for Jex.
     * Windows: %APPDATA%/Jex
     * macOS: ~/Library/Application Support/Jex
     * Linux: ~/.config/Jex
     */
    public static String getConfigDirectory() {
        if (IS_WINDOWS) {
            return System.getenv("APPDATA") + File.separator + APP_NAME;
        } else if (IS_MAC) {
            return System.getProperty("user.home") + File.separator + "Library" +
                   File.separator + "Application Support" + File.separator + APP_NAME;
        } else {
            return System.getProperty("user.home") + File.separator + ".config" +
                   File.separator + APP_NAME;
        }
    }

    /**
     * Get the library directory where jex.jar is installed.
     * Windows: %LOCALAPPDATA%/Programs/Jex
     * macOS: ~/Library/Application Support/Jex
     * Linux: ~/.local/lib/jex
     */
    public static String getLibDirectory() {
        if (IS_WINDOWS) {
            return System.getenv("LOCALAPPDATA") + File.separator + "Programs" +
                   File.separator + APP_NAME;
        } else if (IS_MAC) {
            return System.getProperty("user.home") + File.separator + "Library" +
                   File.separator + "Application Support" + File.separator + APP_NAME;
        } else {
            return System.getProperty("user.home") + File.separator + ".local" +
                   File.separator + "lib" + File.separator + "jex";
        }
    }

    /**
     * Get the bin directory where wrapper scripts are installed.
     * Windows: Same as lib directory
     * macOS/Linux: ~/.local/bin
     */
    public static String getBinDirectory() {
        if (IS_WINDOWS) {
            return getLibDirectory(); // On Windows, keep in same directory
        } else {
            return System.getProperty("user.home") + File.separator + ".local" +
                   File.separator + "bin";
        }
    }

    /**
     * Get the plugins directory path.
     */
    public static String getPluginsDirectory() {
        return getConfigDirectory() + File.separator + "plugins";
    }

    /**
     * Get the plugin.yaml file path.
     */
    public static String getPluginYamlPath() {
        return getConfigDirectory() + File.separator + "plugin.yaml";
    }

    /**
     * Get the arguments.yaml file path.
     */
    public static String getArgumentsYamlPath() {
        return getConfigDirectory() + File.separator + "arguments.yaml";
    }

    // OS Detection Helpers
    public static boolean isWindows() {
        return IS_WINDOWS;
    }

    public static boolean isMac() {
        return IS_MAC;
    }

    public static boolean isUnix() {
        return IS_UNIX;
    }

    public static String getOsName() {
        return OS_NAME;
    }
}