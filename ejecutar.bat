@echo off
cd /d "%~dp0"
if exist jre\bin\java.exe (jre\bin\java.exe -jar app.jar) else (java -jar app.jar)
if errorlevel 1 pause
