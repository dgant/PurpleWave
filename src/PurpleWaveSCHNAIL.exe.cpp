#include <iostream>
#include <fstream>
#include <windows.h>

std::string absolutify(const std::string& relative) {
  char fullPath[MAX_PATH];
  if (GetFullPathName(relative.c_str(), MAX_PATH, fullPath, nullptr) != 0) {
    std::ifstream fileStream(fullPath);
    if (fileStream.good()) {
      fileStream.close();
      return std::string(fullPath);
    }
  }
  return std::string();
}

void launchBot(const std::string& command, const std::string& logPath, std::ostream& outerLogStream) {
  HANDLE hStdoutRead, hStdoutWrite;
  CreatePipe(&hStdoutRead, &hStdoutWrite, nullptr, 0);
  SetHandleInformation(hStdoutRead, HANDLE_FLAG_INHERIT, 0);

  HANDLE hStderrRead, hStderrWrite;
  CreatePipe(&hStderrRead, &hStderrWrite, nullptr, 0);
  SetHandleInformation(hStderrRead, HANDLE_FLAG_INHERIT, 0);

  STARTUPINFO si = { sizeof(STARTUPINFO) };
  PROCESS_INFORMATION pi;

  si.hStdOutput = hStdoutWrite;
  si.hStdError = hStderrWrite;
  si.dwFlags |= STARTF_USESTDHANDLES;

  ZeroMemory(&si, sizeof(STARTUPINFO));
  si.cb = sizeof(STARTUPINFO);
  si.dwFlags |= STARTF_USESHOWWINDOW;
  si.wShowWindow = SW_HIDE; // Hide the console window
  si.hStdOutput = hStdoutWrite;
  si.hStdError = hStderrWrite;
  si.dwFlags |= STARTF_USESTDHANDLES;

  if (CreateProcess(
    nullptr,                // No module name (use command line)
    const_cast<char*>(command.c_str()), // Command line
    nullptr,                // Process handle not inheritable
    nullptr,                // Thread handle not inheritable
    true,                   // Set handle inheritance to FALSE
    0,                      // No creation flags
    nullptr,                // Use parent's environment block
    nullptr,                // Use parent's starting directory 
    &si,                    // Pointer to STARTUPINFO structure
    &pi                     // Pointer to PROCESS_INFORMATION structure
  )) {
      CloseHandle(hStdoutWrite);
      CloseHandle(hStderrWrite);

      char buffer[4096];
      DWORD bytesRead;
      HANDLE hLogFile = CreateFile(logPath.c_str(), GENERIC_WRITE, FILE_SHARE_WRITE, nullptr, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, nullptr);
      if (hLogFile == INVALID_HANDLE_VALUE) {
        DWORD error = GetLastError();
        outerLogStream << "Failed to open log file. Error code: " << error << std::endl;
        // Handle the error accordingly
      }
      while (ReadFile(hStdoutRead, buffer, sizeof(buffer), &bytesRead, nullptr) || ReadFile(hStderrRead, buffer, sizeof(buffer), &bytesRead, nullptr)) {
        if (bytesRead > 0) {
          WriteFile(hLogFile, buffer, bytesRead, nullptr, nullptr);
        }
      }

      WaitForSingleObject(pi.hProcess, INFINITE);

      CloseHandle(pi.hProcess);
      CloseHandle(pi.hThread);
      CloseHandle(hStdoutRead);
      CloseHandle(hStderrRead);
      CloseHandle(hLogFile);
    } else {
      CloseHandle(hStdoutRead);
      CloseHandle(hStdoutWrite);
      CloseHandle(hStderrRead);
      CloseHandle(hStderrWrite);
    }
}

int main() {
  const std::string javaPath8   = "../jre/bin/java.exe";
  const std::string javaPath21  = "../bots/PurpleWave/jre/bin/java.exe";  
  const std::string jarPath     = "bwapi-data/AI/PurpleWave.jar";
  const std::string logPath     = "./bwapi-data/write/PurpleWaveSCHNAIL.exe.txt";
  const std::string javaLogPath = "./bwapi-data/write/java.exe.txt";
  
  std::ofstream logFile(logPath);
  std::ostream& logStream = logFile;
  
  char currentDir[MAX_PATH];
  auto cwd = GetCurrentDirectory(MAX_PATH, currentDir);  
  logStream << "PurpleWave SCHNAIL Launcher: Working directory is: " << currentDir << std::endl;
  
  bool is64Bit = (sizeof(void*) == 8);
  logStream << "PurpleWave SCHNAIL Launcher: Detected " << (is64Bit ? "64" : "32") << "-bit OS" << std::endl;
  
  auto absoluteJarPath = absolutify(jarPath);
  logStream << "PurpleWave SCHNAIL Launcher: " << (absoluteJarPath.empty() ? "Did not find" : "Found") << " bot at " << absoluteJarPath << std::endl;
  
  bool ranBundledJava = false;
  
  const bool allowBundledJava = false;
  if (is64Bit) {
    auto absoluteJavaPath = absolutify(javaPath21);
    logStream << "PurpleWave SCHNAIL Launcher: " << (absoluteJavaPath.empty() ? "Did not find" : "Found") << " bundled JRE at " << absoluteJavaPath << std::endl;
    
    if (allowBundledJava && ! absoluteJavaPath.empty()) {
      ranBundledJava = true;
      std::string command =
         "\""
        + absoluteJavaPath
        + "\""
        + " -jar -XX:MaxGCPauseMillis=15 -Xms1024m -Xmx1024m "
        + " --add-opens=java.base/java.nio=ALL-UNNAMED "
        + jarPath
        + " > "
        + javaLogPath
        + " 2>&1";
      logStream << "PurpleWave SCHNAIL Launcher: Launching bot with bundled JRE: " << command << std::endl;
      system(command.c_str());
    }
  }
  
  if ( ! ranBundledJava) {
    auto absoluteJavaPath = absolutify(javaPath8);
    logStream << "PurpleWave SCHNAIL Launcher: " << (absoluteJavaPath.empty() ? "Did not find" : "Found") << " SCHNAIL JRE at " << absoluteJavaPath << std::endl;
    std::string command =
       "\""
      + absoluteJavaPath
      + "\""
      + " -jar -XX:MaxGCPauseMillis=15 -Xms1024m -Xmx1024m "
      + jarPath
      + " > "
      + javaLogPath
      + " 2>&1";
    logStream << "PurpleWave SCHNAIL Launcher: Launching bot with SCHNAIL JRE: " << command << std::endl;
    system(command.c_str());      
  }
    
  logStream << "PurpleWave SCHNAIL Launcher: Done launching bot." << std::endl;
  
  logFile.close();
  return 0;
}

