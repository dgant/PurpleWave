@echo off
if "%PROCESSOR_ARCHITECTURE%"=="AMD64" (
  echo "64-Bit OS"
  if exist "bwapi-data/AI/jre/bin/java.exe" (
    echo "Found bundled JRE."
    bwapi-data/AI/run64.bat
  ) else (
    echo "Did not find bundled JRE."
    bwapi-data/AI/run32.bat
  )  
 ) else (
  echo "32-Bit OS"
  bwapi-data/AI/run32.bat
)