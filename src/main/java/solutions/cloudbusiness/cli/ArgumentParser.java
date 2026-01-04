package solutions.cloudbusiness.cli;

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

            for (Map<String, Object> optionConfig : optionsList) {
                Option.Builder builder = Option.builder();

                // Set short option if present
                if (optionConfig.containsKey("short")) {
                    builder.option((String) optionConfig.get("short"));
                }

                // Set long option
                if (optionConfig.containsKey("long")) {
                    builder.longOpt((String) optionConfig.get("long"));
                }

                // Set description
                if (optionConfig.containsKey("description")) {
                    builder.desc((String) optionConfig.get("description"));
                }

                // Set if option requires an argument
                if (optionConfig.containsKey("hasArg")) {
                    boolean hasArg = (Boolean) optionConfig.get("hasArg");
                    builder.hasArg(hasArg);
                }

                // Set argument name if present
                if (optionConfig.containsKey("argName")) {
                    builder.argName((String) optionConfig.get("argName"));
                }

                // Set if required
                if (optionConfig.containsKey("required")) {
                    builder.required((Boolean) optionConfig.get("required"));
                }

                options.addOption(builder.build());
            }

        } catch (Exception e) {
            System.err.println("Error loading arguments from YAML: " + e.getMessage());
        }

        return options;
    }

    public static void printHelp(String commandName, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(commandName, options);
    }

    public static void printJexHelp(Options options) {
        System.out.println("Jex - Plugin-based CLI Framework");
        System.out.println("\nUsage:");
        System.out.println("  jex [options]           - Run Jex built-in commands");
        System.out.println("  jex <plugin> [args...]  - Run a plugin");
        System.out.println("\nBuilt-in Commands:");
        System.out.println("     --install             Install Jex (create directories, install JAR, wrapper scripts)");
        System.out.println("  -l,--list                List all installed plugins");
        System.out.println("  -h,--help                Display help information");

        System.out.println("\nExamples:");
        System.out.println("  jex --install                              Install Jex");
        System.out.println("  jex --list                                 List installed plugins");
        System.out.println("  jex new-plugin my-tool                     Create a new plugin project");
        System.out.println("  jex new-plugin my-tool --package com.example   With custom package");
        System.out.println("  jex <plugin-name> --help                   Show plugin help");
    }
}