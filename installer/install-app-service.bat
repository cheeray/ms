@echo off & SETLOCAL ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION
rem -------------------------------------------------------------------------
rem Install MicroApp as service and create uninstaller script
rem @Chengwei
rem -------------------------------------------------------------------------

rem $Id$

PUSHD "%~dp0"
SET FD="%~dp0"

rem Prepare uninstall script ... 
echo @echo off > uninstall.bat
echo CD /D %FD% >> uninstall.bat
del install.log >nul
goto :eof

:create
CD /D %FD%
rem install service
MsAppExample.exe //IS//MsAppExample-1 --DisplayName="MsAppExample-1" --Description="Micro App example service 1" ^
                            --Startup=auto --Install="%CD%\MsAppExample.exe" --Jvm="%CD%\bin\server\jvm.dll" --JvmOptions=-Dms.package=com.cheeray ++JvmOptions=-Xmx96m --Classpath="%CD%\example.jar" ^
                            --StartMode=jvm --StartClass=com.cheeray.ms.MicroService --StartMethod=start --StartParams=start ^
                            --StopMode=jvm --StopClass=com.cheeray.ms.MicroService --StopMethod=stop --StopParams=stop; ^
                            --StdOutput=auto --StdError=auto --LogPath="%CD%\log" --LogLevel=Debug --LogPrefix=example.log ^

rem create uninstall script
echo MsAppExample.exe //SS//MsAppExample-1 >> uninstall.bat
echo MsAppExample.exe //DS//MsAppExample-1 >> uninstall.bat
rem log installation
FOR /F "tokens=*" %%D IN ('SC GetDisplayName MsAppExample-1 ^| FINDSTR "FAILED"') DO timeout /t 1
WMIC.exe SERVICE WHERE "name like 'MsAppExample-1'" GET DisplayName >> install.log
MsAppExample.exe //ES//MsAppExample-1
goto :eof

:: toupper & tolower; makes use of the fact that string 
:: replacement (via SET) is not case sensitive
:toupper
for %%L IN (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) DO SET %1=!%1:%%L=%%L!
goto :EOF

:tolower
for %%L IN (a b c d e f g h i j k l m n o p q r s t u v w x y z) DO SET %1=!%1:%%L=%%L!
goto :EOF