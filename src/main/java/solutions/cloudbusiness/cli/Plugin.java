package solutions.cloudbusiness.cli;

public interface Plugin {
    String getName();
    void execute(String[] args);
    String[] getCommandLineOptions(); // Define how to get options for CLI
}