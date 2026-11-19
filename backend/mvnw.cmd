@IF "%DEBUG%" == "" @ECHO OFF
@SETLOCAL EnableDelayedExpansion EnableExtensions

SET "DIRNAME=%~dp0"
IF "%DIRNAME:~-1%" == "\" SET "DIRNAME=%DIRNAME:~0,-1%"
SET "MAVEN_PROJECTBASEDIR=%DIRNAME%"

IF NOT DEFINED JAVA_HOME (
  IF EXIST "C:\Program Files\Java\jdk-25.0.5.0.3" (
    SET "JAVA_HOME=C:\Program Files\Java\jdk-25.0.5.0.3"
  )
)

IF DEFINED JAVA_HOME (
  SET "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) ELSE (
  SET "JAVA_EXE=java"
)

SET "WRAPPER_JAR=%DIRNAME%\.mvn\wrapper\maven-wrapper.jar"

IF EXIST "%WRAPPER_JAR%" (
  "%JAVA_EXE%" -cp "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
) ELSE (
  mvn %*
)
