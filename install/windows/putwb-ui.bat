@echo off
for %%i in ("%~dp0") do SET "appdir=%%~fi"
SET "jarfile=putwb-1.51-complete.jar"
SET "fullpath=%appdir%%jarfile%"
java -jar "%fullpath%"