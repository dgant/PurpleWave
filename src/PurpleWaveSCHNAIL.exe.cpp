#include <iostream>
#include <fstream>
#include <windows.h>

std::string absolutify(const std::string& relative) {
  char fullPath[MAX_PATH];
  if (GetFullPathName("../bots/PurpleWave/jre/bin/java.exe", MAX_PATH, fullPath, nullptr) != 0) {
    std::ifstream fileStream(fullPath);
    if (fileStream.good()) {
      fileStream.close();
      return std::string(fullPath);
    }
  }
  return std::string();
}

int main() {
  const std::string javaPath = "../bots/PurpleWave/jre/bin/java.exe";
  const std::string jarPath = "bwapi-data/AI/PurpleWave.jar";  
  
  bool is64Bit = (sizeof(void*) == 8);
  std::cout << "PurpleWave SCHNAIL Launcher: Detected " << (is64Bit ? "64" : "32") << "-bit OS" << std::endl;
  
  char currentDir[MAX_PATH];
  GetCurrentDirectory(MAX_PATH, currentDir);
  std::cout << "PurpleWave SCHNAIL Launcher: Working directory is: " << currentDir << std::endl;
  
  auto absoluteJarPath = absolutify(jarPath);
  std::cout << "PurpleWave SCHNAIL Launcher: " << (absoluteJarPath.empty() ? "Did not find " : "Found ") << jarPath << std::endl;
  
  bool ranBundledJava = false;
  if (is64Bit) {    
    auto absoluteJavaPath = absolutify(javaPath);
    std::cout << "PurpleWave SCHNAIL Launcher: " << (absoluteJavaPath.empty() ? "Did not find " : "Found ") << "bundled JRE at " << absoluteJavaPath << std::endl;
    
    if ( ! absoluteJavaPath.empty()) {
      ranBundledJava = true;
      std::string command = "\""
        + absoluteJavaPath
        + "\" -jar -XX:MaxGCPauseMillis=15 -Xms1536m -Xmx1536m --add-opens=java.base/java.nio=ALL-UNNAMED "
        + jarPath
        + " > ./bwapi-data/write/PurpleWaveSCHNAIL.exe.txt 2>&1";
      std::cout << "PurpleWave SCHNAIL Launcher: Launching bot with: " << command << std::endl;
      system(command.c_str());
      std::cout << "PurpleWave SCHNAIL Launcher: Done launching bot." << std::endl;
    }
  }
  
  if ( ! ranBundledJava) {
    std::cout << "PurpleWave SCHNAIL Launcher: Launching bot with run32.bat" << std::endl;
    system("./bwapi-data/AI/run32.bat");
  }

  return 0;
}
