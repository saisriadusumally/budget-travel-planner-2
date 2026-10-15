@echo off
REM One-command start (Windows): builds the React app into the backend, then starts everything.
cd /d "%~dp0frontend" || exit /b 1
call npm install || exit /b 1
call npm run build || exit /b 1
cd /d "%~dp0backend" || exit /b 1
call mvn spring-boot:run
