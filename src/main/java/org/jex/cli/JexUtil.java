package org.jex.cli;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Utility methods for Jex operations.
 */
public class JexUtil {

    /**
     * Scan a JAR file and find the first class that implements JexPlugin.
     *
     * @param jarPath Path to the JAR file
     * @return Instance of the plugin, or null if none found
     */
    public static JexPlugin findPluginInJar(Path jarPath) throws Exception {
        // Load the JAR file
        URL jarUrl = jarPath.toUri().toURL();
        URLClassLoader classLoader = new URLClassLoader(
            new URL[]{jarUrl},
            JexUtil.class.getClassLoader()
        );

        // Scan JAR for JexPlugin implementation
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            java.util.Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.endsWith(".class")) {
                    String className = name.replace('/', '.').substring(0, name.length() - 6);

                    try {
                        Class<?> clazz = classLoader.loadClass(className);

                        if (JexPlugin.class.isAssignableFrom(clazz) && !clazz.isInterface()) {
                            Object instance = clazz.getDeclaredConstructor().newInstance();
                            return (JexPlugin) instance;
                        }
                    } catch (Exception e) {
                        // Skip classes that can't be loaded
                    }
                }
            }
        }

        return null;
    }
}
