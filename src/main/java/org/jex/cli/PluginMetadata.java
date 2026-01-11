package org.jex.cli;

/**
 * Immutable data class representing plugin metadata.
 */
public class PluginMetadata {
    private final String name;
    private final String jarFile;
    private final String className;
    private final String version;
    private final String description;

    public PluginMetadata(String name, String jarFile, String className,
                          String version, String description) {
        this.name = name;
        this.jarFile = jarFile;
        this.className = className;
        this.version = version;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getJarFile() {
        return jarFile;
    }

    public String getClassName() {
        return className;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }
}