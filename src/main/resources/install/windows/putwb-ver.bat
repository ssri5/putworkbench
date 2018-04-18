@echo off
for %%i in ("%~dp0") do SET "appdir=%%~fi"
SET "jarfile=putwb-${project.version}-complete.jar"
SET "fullpath=%appdir%%jarfile%"
java -cp "%fullpath%" in.ac.iitk.cse.putwb.experiment.Verifier %*