@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------

@IF "%DEBUG%" == "" @ECHO OFF
@SETLOCAL EnableDelayedExpansion EnableExtensions

SET "DIRNAME=%~dp0"
IF "%DIRNAME%" == "" SET "DIRNAME=."
SET "MAVEN_PROJECTBASEDIR=%DIRNAME%"

@REM Set JAVA_HOME if not already set and java is found
IF NOT DEFINED JAVA_HOME (
  FOR /F "tokens=* USEBACKQ" %%F IN (`where java 2^>nul`) DO (
    SET "JAVA_EXE=%%F"
    GOTO :found_java
  )
)
:found_java

SET "WRAPPER_JAR=%DIRNAME%\.mvn\wrapper\maven-wrapper.jar"
SET "WRAPPER_PROP=%DIRNAME%\.mvn\wrapper\maven-wrapper.properties"

IF EXIST "%WRAPPER_JAR%" (
  java -jar "%WRAPPER_JAR%" %*
) ELSE (
  mvn %*
)
