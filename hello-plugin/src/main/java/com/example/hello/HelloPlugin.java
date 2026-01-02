package com.example.hello;

import solutions.cloudbusiness.cli.Plugin;
import org.apache.commons.cli.*;

public class HelloPlugin implements Plugin {

    @Override
    public String getName() {
        return "hello";
    }

    @Override
    public void execute(String[] args) {
        // Create options
        Options options = new Options();
        options.addOption("n", "name", true, "Name to greet");
        options.addOption("h", "help", false, "Display help information");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                formatter.printHelp("commander hello", options);
                return;
            }

            String name = cmd.getOptionValue("name", "World");
            System.out.println("Hello, " + name + "!");

        } catch (ParseException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            formatter.printHelp("commander hello", options);
            System.exit(1);
        }
    }

    @Override
    public String[] getCommandLineOptions() {
        return new String[]{"--name", "-n", "--help", "-h"};
    }
}