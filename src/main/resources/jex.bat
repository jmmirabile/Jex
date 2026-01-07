@echo off
REM Jex wrapper script for Windows

REM Set the installation directory
set JEX_JAR=%LOCALAPPDATA%\Programs\Jex\jex.jar

REM Check if JAR exists
if not exist "%JEX_JAR%" (
    echo Error: Jex JAR not found at %JEX_JAR%
    echo Please run 'java -jar jex.jar --install' to install Jex.
    exit /b 1
)

REM Separate Java options from Jex arguments
set JAVA_OPTS=
set JEX_ARGS=

:parse_args
if "%~1"=="" goto run_jex
set arg=%~1
if "%arg:~0,2%"=="-D" (
    set JAVA_OPTS=%JAVA_OPTS% %arg%
) else if "%arg:~0,2%"=="-X" (
    set JAVA_OPTS=%JAVA_OPTS% %arg%
) else if "%arg:~0,11%"=="-javaagent:" (
    set JAVA_OPTS=%JAVA_OPTS% %arg%
) else (
    set JEX_ARGS=%JEX_ARGS% %arg%
)
shift
goto parse_args

:run_jex
REM Run Jex with Java options and Jex arguments separated
java %JAVA_OPTS% -jar "%JEX_JAR%" %JEX_ARGS%
