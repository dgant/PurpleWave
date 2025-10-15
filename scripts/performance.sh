#!/usr/bin/bash
echo "Good performance:      $(find /c/Users/d/AppData/Roaming/scbw/games/*/logs_0 -name "bot.log" | xargs grep "performance was good" | wc -l)"
echo "Dangerous performance: $(find /c/Users/d/AppData/Roaming/scbw/games/*/logs_0 -name "bot.log" | xargs grep "performance was DANGEROUS" | wc -l)"
echo "Bad performance:       $(find /c/Users/d/AppData/Roaming/scbw/games/*/logs_0 -name "bot.log" | xargs grep "performance was BAD" | wc -l)"