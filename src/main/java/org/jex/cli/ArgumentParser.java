package org.jex.cli;

import org.apache.commons.cli.*;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class ArgumentParser {

    public static Options loadOptionsFromYaml(String yamlPath) {
        Options options = new Options();
        Yaml yaml = new Yaml();

        try (InputStream inputStream = Files.newInputStream(Paths.get(yamlPath))) {
            Map<String, Object> config = yaml.load(inputStream);

            if (config == null || !config.containsKey("options")) {
                return options;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> optionsList = (List<Map<String, Object>>) config.get("options");
            return buildOptionsFromList(optionsList);

        } catch (Exception e) {
            System.err.println("Error loading arguments from YAML: " + e.getMessage());
        }

        return options;
    }

    /**
     * Load CLI options from a bundled resource file (arguments.yaml).
     * Package-private convenience method for internal org.jex.cli classes only.
     * External plugins should use loadOptionsFromResource(String, Class) instead.
     */
    static Options loadOptionsFromResource(String resourcePath) {
        return loadOptionsFromResource(resourcePath, ArgumentParser.class);
    }

    /**
     * Load CLI options from a bundled resource file using a specific class context.
     * This is the public API for loading plugin resources.
     *
     * @param resourcePath Path to the YAML file (e.g., "/arguments.yaml")
     * @param contextClass Class to use for loading resources (use this.getClass() or YourPlugin.class)
     * @return Parsed Options object
     */
    public static Options loadOptionsFromResource(String resourcePath, Class<?> contextClass) {
        Options options = new Options();

        try (InputStream inputStream = contextClass.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                System.err.println("Warning: Could not find " + resourcePath + " in plugin resources");
                return options;
            }

            org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
            @SuppressWarnings("unchecked")
            Map<String, Object> config = yaml.load(inputStream);

            if (config == null || !config.containsKey("options")) {
                return options;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> optionsList = (List<Map<String, Object>>) config.get("options");
            return buildOptionsFromList(optionsList);
        } catch (Exception e) {
            System.err.println("Error loading arguments from resource: " + e.getMessage());
        }

        return options;
    }

    /**
     * Build Options from a list of option configurations.
     * Extracts common logic for parsing option configurations.
     *
     * @param optionsList List of option configuration maps from YAML
     * @return Options object with all parsed options
     */
    private static Options buildOptionsFromList(List<Map<String, Object>> optionsList) {
        Options options = new Options();

        for (Map<String, Object> optionConfig : optionsList) {
            Option.Builder builder = Option.builder();

            if (optionConfig.containsKey("short")) {
                builder.option((String) optionConfig.get("short"));
            }

            if (optionConfig.containsKey("long")) {
                builder.longOpt((String) optionConfig.get("long"));
            }

            if (optionConfig.containsKey("description")) {
                builder.desc((String) optionConfig.get("description"));
            }

            if (optionConfig.containsKey("hasArg")) {
                boolean hasArg = (Boolean) optionConfig.get("hasArg");
                builder.hasArg(hasArg);
            }

            if (optionConfig.containsKey("argName")) {
                builder.argName((String) optionConfig.get("argName"));
            }

            if (optionConfig.containsKey("required")) {
                builder.required((Boolean) optionConfig.get("required"));
            }

            options.addOption(builder.build());
        }

        return options;
    }

    public static void printHelp(String commandName, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(commandName, options);
    }

}