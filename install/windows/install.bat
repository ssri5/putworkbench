@echo off
echo This installation script attempts to add the PUTWorkbench directory to your user's Path variable
pause
for %%i in ("%~dp0") do SET "appdir=%%~fi"
setx path "%path%;%appdir%"
pause