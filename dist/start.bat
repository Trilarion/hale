@rem  hale startup script for Windows

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_HOME=%DIRNAME%

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

goto fail

:init

@rem Setup the command line
set CLASSPATH=%APP_HOME%*;%APP_HOME%lib\*

@rem Execute hale
"%JAVA_EXE%" -classpath "%CLASSPATH%" -Djava.library.path="%APP_HOME%lib\native" net.sf.hale.Game

:fail