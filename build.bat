@echo off
setlocal
cd /d "%~dp0"
where gradle >nul 2>nul
if errorlevel 1 (
  echo ERROR: Gradle was not found on PATH.
  echo Install Gradle 9.x and Java 21, then reopen this window.
  echo Alternatively, open this folder in IntelliJ IDEA and let Gradle sync.
  pause
  exit /b 1
)
java -version
if errorlevel 1 exit /b 1
gradle --no-daemon clean build
if errorlevel 1 (
  echo Build failed. Check the Gradle error above.
  pause
  exit /b 1
)
echo.
echo Build complete. Your mod jar is in build\libs\arena-coach-1.0.0.jar
pause
