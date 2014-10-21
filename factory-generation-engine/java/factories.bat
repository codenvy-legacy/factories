@echo off
SETLOCAL
SET JAVA_HOME="%JAVA_HOME%"
SET FACTORIES_HOME="C:\codenvy\factories\java"
cmd /c "%JAVA_HOME%\bin\java -jar %FACTORIES_HOME%\target\codenvy-factories-1.0-SNAPSHOT.jar %*"
exit /b %errorlevel%