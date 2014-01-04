@echo off

set programdir="D:\Programing"
set packagedir="%programdir%\Packages"
set repodir="%programdir%\Minecraft"
set forgedir="%repodir%\1.6.4"
set github="%forgedir%\GitHub"
set fmldir="%forgedir%\fml"
set mcpdir="%forgedir%\mcp"
set littleblocks="%github%\DynamicTransport"
set euryscore="%github%\SlimevoidLibrary"
cd %mcpdir%

if not exist %euryscore% GOTO :LBFAIL
if exist %littleblocks% GOTO :LITTLEBLOCKS
GOTO :LBFAIL

:LITTLEBLOCKS
echo Little blocks
pause
if exist %mcpdir%\src GOTO :COPYSRC
GOTO :LBFAIL

:COPYSRC
if not exist "%mcpdir%\src-work" GOTO :CREATESRC
GOTO :LBFAIL

:CREATESRC
echo create
pause
mkdir "%mcpdir%\src-work"
xcopy "%mcpdir%\src\*.*" "%mcpdir%\src-work\" /S
if exist "%mcpdir%\src-work" GOTO :COPYLB
GOTO :LBFAIL

:COPYLB
echo ready to copy
pause
xcopy "%euryscore%\SV-common\*.*" "%mcpdir%\src\minecraft\" /S
xcopy "%littleblocks%\DT-source\*.*" "%mcpdir%\src\minecraft\" /S
pause
call %mcpdir%\recompile.bat
call %mcpdir%\reobfuscate.bat
echo Recompile and Reobf Completed Successfully
pause

:REPACKAGE
if not exist "%mcpdir%\reobf" GOTO :LBFAIL
if exist "%packagedir%\LittleBlocks" (
del "%packagedir%\LittleBlocks\*.*" /S /Q
rmdir "%packagedir%\LittleBlocks" /S /Q
)
mkdir "%packagedir%\LittleBlocks\slimevoid\littleblocks"
xcopy "%mcpdir%\reobf\minecraft\slimevoid\littleblocks\*.*" "%packagedir%\LittleBlocks\slimevoid\littleblocks\" /S
xcopy "%littleblocks%\LB-resources\*.*" "%packagedir%\LittleBlocks\" /S
echo "LittleBlocks Packaged Successfully
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
GOTO :LBCOMPLETE

:LBFAIL
echo Could not compile littleblocks
GOTO :LBEND

:LBCOMPLETE
echo Littleblocks completed compile successfully
GOTO :LBEND

:LBEND
pause