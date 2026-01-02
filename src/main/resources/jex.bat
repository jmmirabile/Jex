@echo off
REM Jex wrapper script for Windows

REM Set the installation directory
set JEX_JAR=%LOCALAPPDATA%\Programs\Jex\jex.jar

REM Check if JAR exists
if not exist "%JEX_JAR%" (
    echo Error: Jex JAR not found at %JEX_JAR%
    echo Please run 'java -jar jex.jar --setup' to install Jex.
    exit /b 1
)

REM Run Jex with all arguments passed through
java -jar "%JEX_JAR%" %*
