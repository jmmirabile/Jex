package org.jex.cli;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.InputStream;
import java.util.Properties;

/**
 * Maven utilities for Jex framework.
 * Provides helper methods for Maven operations including version detection,
 * artifact installation, and repository management.
 */
public class JexMavenUtil {
    private static String cachedVersion = null;

    /**
     * Get the current Jex version from Maven metadata.
     * Result is cached after first call for performance.
     *
     * Tries multiple detection strategies in order:
     * 1. Package implementation version (from MANIFEST.MF)
     * 2. Maven pom.xml using maven-model API
     * 3. Maven pom.properties
     * 4. Hardcoded fallback
     *
     * @return The Jex version string (e.g., "1.0.1")
     */
    public static String getVersion() {
        if (cachedVersion != null) {
            return cachedVersion;
        }

        cachedVersion = detectVersion();
        return cachedVersion;
    }

    /**
     * Detect Jex version using multiple strategies.
     */
    private static String detectVersion() {
        // Strategy 1: Try to read from package manifest (set by Maven JAR plugin)
        try {
            Package pkg = JexMavenUtil.class.getPackage();
            String version = pkg.getImplementationVersion();
            if (version != null && !version.isEmpty()) {
                return version;
            }
        } catch (Exception e) {
            // Continue to next strategy
        }

        // Strategy 2: Read from pom.xml using maven-model API
        try (InputStream is = JexMavenUtil.class.getResourceAsStream(
                "/META-INF/maven/org.jex.cli/Jex/pom.xml")) {
            if (is != null) {
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(is);
                String version = model.getVersion();
                if (version != null && !version.isEmpty()) {
                    return version;
                }
            }
        } catch (Exception e) {
            // Continue to next strategy
        }

        // Strategy 3: Fallback to pom.properties (manual parsing)
        try (InputStream is = JexMavenUtil.class.getResourceAsStream(
                "/META-INF/maven/org.jex.cli/Jex/pom.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                String version = props.getProperty("version");
                if (version != null && !version.isEmpty()) {
                    return version;
                }
            }
        } catch (Exception e) {
            // Continue to fallback
        }

        // Strategy 4: This should never be reached if JAR is properly built
        System.err.println("ERROR: Could not establish Jex version from MANIFEST.MF, pom.xml, or pom.properties");
        System.err.println("       Build may have failed or JAR is corrupted");
        return "UNKNOWN";
    }

    // TODO: Future Maven utility methods can be added here:
    // - public static String getLocalRepoPath()
    // - public static boolean installArtifact(String jarPath, String groupId, String artifactId, String version)
    // - public static boolean artifactExists(String groupId, String artifactId, String version)

    // Private constructor - utility class should not be instantiated
    private JexMavenUtil() {
        throw new AssertionError("JexMavenUtil is a utility class and should not be instantiated");
    }
}
