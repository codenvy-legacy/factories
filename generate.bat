@echo off
SETLOCAL
SET CODENVY_CLI=c:\codenvy\cli\codenvy.bat
SET CODENVY_FACTORY=c:\codenvy\factories\java\factories.bat

if "%1"=="" goto blank

REM Generate an updated Codenvy token
cmd /c %CODENVY_CLI% auth --newToken --save

REM Each JSON file, generate a .endpoint file using 'codenvy remote factory:create' command.
REM Take the new .endpoint file and execute 'factories.bat' command.  
REM Do not pass in full file name to factories.bat.  Only the initial half, ie. 'spring' if 'spring.endpoint'
REM TODO: Only run this loop on .JSON files that are newer than any generated HTML or .factory files.
FOR %%A in (%1) DO (
	cmd /c %CODENVY_CLI% remote factory:create --in %%A --encoded --rel self > %%~nA.endpoint
	cmd /c %CODENVY_FACTORY% %%~nA
	)
GOTO end

:blank
ECHO USAGE: GENERATE [name_of_json_files]

:end