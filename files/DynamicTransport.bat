@echo off

set programdir=%CD%\..\..
set repodir=%programdir%\Git
set packagedir=%programdir%\Packages
set forgedir=%programdir%\Forge
set fmldir=%forgedir%\fml
set mcpdir=%forgedir%\mcp
set dynamictrans=%repodir%\DynamicTransport\src\main
set slimelib=%repodir%\SlimevoidLibrary\src\main\java
cd %mcpdir%

if not exist %slimelib% GOTO :DTFAIL
if exist %dynamictrans% GOTO :DYNAMICT
GOTO :DTFAIL

:DYNAMICT
if exist %mcpdir%\src GOTO :COPYSRC
GOTO :DTFAIL

:COPYSRC
if not exist "%mcpdir%\src-work" GOTO :CREATESRC
GOTO :DTFAIL

:CREATESRC
mkdir "%mcpdir%\src-work"
xcopy "%mcpdir%\src\*.*" "%mcpdir%\src-work\" /S
if exist "%mcpdir%\src-work" GOTO :COPYDT
GOTO :DTFAIL

:COPYDT
xcopy "%slimelib%\*.*" "%mcpdir%\src\minecraft\" /S
xcopy "%dynamictrans%\java\*.*" "%mcpdir%\src\minecraft\" /S
pause
call %mcpdir%\recompile.bat
call %mcpdir%\reobfuscate.bat
echo Recompile and Reobf Completed Successfully
pause

:REPACKAGE
if not exist "%mcpdir%\reobf" GOTO :DTFAIL
if exist "%packagedir%\DynamicTransport" (
del "%packagedir%\DynamicTransport\*.*" /S /Q
rmdir "%packagedir%\DynamicTransport" /S /Q
)
mkdir "%packagedir%\DynamicTransport\com\slimevoid\dynamictransport"
xcopy "%mcpdir%\reobf\minecraft\com\slimevoid\dynamictransport\*.*" "%packagedir%\DynamicTransport\com\slimevoid\dynamictransport\" /S
xcopy "%dynamictrans%\resources\*.*" "%packagedir%\DynamicTransport\" /S
echo "Dynamic Transport Packaged Successfully
pause
ren "%mcpdir%\src" src-old
echo Recompiled Source folder renamed
pause
ren "%mcpdir%\src-work" src
echo Original Source folder restored
pause
del "%mcpdir%\src-old" /S /Q
del "%mcpdir%\reobf" /S /Q
if exist "%mcpdir%\src-old" rmdir "%mcpdir%\src-old" /S /Q
if exist "%mcpdir%\reobf" rmdir "%mcpdir%\reobf" /S /Q
echo Folder structure reset
GOTO :DTCOMPLETE

:DTFAIL
echo Could not compile dynamictrans
GOTO :DTEND

:DTCOMPLETE
echo Dynamic Transport completed compile successfully
GOTO :DTEND

:DTEND
pause