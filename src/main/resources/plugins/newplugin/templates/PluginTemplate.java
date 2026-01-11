package ${PACKAGE_NAME};

import org.jex.cli.JexPlugin;
import org.jex.cli.ArgumentParser;
import org.apache.commons.cli.*;

public class ${CLASS_NAME} implements JexPlugin {

    @Override
    public String getName() {
        return "${PLUGIN_NAME}";
    }

    @Override
    public void execute(String[] args) {
        // Load options from bundled arguments.yaml
        Options options = ArgumentParser.loadOptionsFromResource("/arguments.yaml", this.getClass());

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                formatter.printHelp("jex ${PLUGIN_NAME}", options);
                return;
            }

            // TODO: Implement your plugin logic here
            System.out.println("${CLASS_NAME_CAPITALIZED} plugin executed successfully!");

        } catch (ParseException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            formatter.printHelp("jex ${PLUGIN_NAME}", options);
            System.exit(1);
        }
    }
}