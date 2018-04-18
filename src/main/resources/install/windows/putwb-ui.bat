@echo off
for %%i in ("%~dp0") do SET "appdir=%%~fi"
SET "jarfile=putwb-${project.version}-complete.jar"
SET "fullpath=%appdir%%jarfile%"
java -jar "%fullpath%"