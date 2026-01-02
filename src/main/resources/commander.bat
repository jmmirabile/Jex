@echo off
REM Commander wrapper script for Windows

REM Set the installation directory
set COMMANDER_JAR=%LOCALAPPDATA%\Programs\Commander\commander.jar

REM Check if JAR exists
if not exist "%COMMANDER_JAR%" (
    echo Error: Commander JAR not found at %COMMANDER_JAR%
    echo Please run 'java -jar commander.jar --setup' to install Commander.
    exit /b 1
)

REM Run Commander with all arguments passed through
java -jar "%COMMANDER_JAR%" %*