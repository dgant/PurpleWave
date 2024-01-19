echo "Running with bundled JRE."
./bwapi-data/AI/jre/bin/java.exe -jar -XX:MaxGCPauseMillis=15 -Xms1536m -Xmx1536m --add-opens=java.base/java.nio=ALL-UNNAMED ./bwapi-data/AI/PurpleWave.jar
