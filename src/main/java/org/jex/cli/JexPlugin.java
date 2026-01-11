package org.jex.cli;

public interface JexPlugin {
    String getName();
    void execute(String[] args);

}