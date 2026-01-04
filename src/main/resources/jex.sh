#!/bin/bash
# Jex wrapper script for Unix/Linux/macOS

# Determine the installation directory
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    JEX_JAR="$HOME/Library/Application Support/Jex/jex.jar"
else
    # Linux and other Unix variants
    JEX_JAR="$HOME/.local/lib/jex/jex.jar"
fi

# Check if JAR exists
if [ ! -f "$JEX_JAR" ]; then
    echo "Error: Jex JAR not found at $JEX_JAR"
    echo "Please run 'java -jar jex.jar --install' to install Jex."
    exit 1
fi

# Run Jex with all arguments passed through
exec java -jar "$JEX_JAR" "$@"
