#!/bin/bash
# Commander wrapper script for Unix/Linux/macOS

# Determine the installation directory
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    COMMANDER_JAR="$HOME/Library/Application Support/Commander/commander.jar"
else
    # Linux and other Unix variants
    COMMANDER_JAR="$HOME/.local/lib/commander/commander.jar"
fi

# Check if JAR exists
if [ ! -f "$COMMANDER_JAR" ]; then
    echo "Error: Commander JAR not found at $COMMANDER_JAR"
    echo "Please run 'java -jar commander.jar --setup' to install Commander."
    exit 1
fi

# Run Commander with all arguments passed through
exec java -jar "$COMMANDER_JAR" "$@"