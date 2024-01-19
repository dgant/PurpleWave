#!/

import argparse
import contextlib
import subprocess
import shutil
import sys
import os

def main():
  parser = argparse.ArgumentParser(description="Compile, stage, and package PurpleWave and its JREs.")
  parser.add_argument('--all',     action='store_true', default=False,  help='Perform all steps')
  parser.add_argument('--jre',     action='store_true', default=False,  help='Step 1: (Skipped by default) Package lightweight JRE using jdeps+jlink, then stage them for packaging')
  parser.add_argument('--stage',   action='store_true', default=True,   help='Step 2: Record deployment information, construct EXE via launch4j, then copy bot binaries and deployment information')
  parser.add_argument('--package', action='store_true', default=True,   help='Step 3: Re-zip directories')
  args = parser.parse_args()
  if args.all or args.jre:
    makejre()
    copyjre()
  if args.all or args.stage:
    stage()
  if args.all or args.package:
    package()

def pathjoin(*args):
  result = '/'.join(map(str, args))
  while '//' in result:
    result = result.replace('//', '/')
  return result  

file_launch4j = "c:/Program Files (x86)/Launch4j/Launch4jc.exe"
dir_jre       = "c:/p/graalvm-jdk"
dir_bwapidata = "c:/p/bw/bwapi-data/"
java_version  = "21"
dir_pw        = "c:/p/pw"
dir_bots      = "c:/Users/d/AppData/Roaming/scbw/bots/"
dir_out       = pathjoin(dir_pw,  "out/")
dir_configs   = pathjoin(dir_pw,  "configs/")
dir_staging   = pathjoin(dir_out, "staging/")

def path_pw(path):
  return pathjoin(dir_pw, path)
def path_out(path):
  return pathjoin(dir_out, path)  
def path_configs(path):
  return pathjoin(dir_configs, path)  
def path_staging(path):
  return pathjoin(dir_staging, path)
def path_bots(path):
  return pathjoin(dir_bots, path)
  
dir_jar   = pathjoin(dir_out, "artifacts", "PurpleWave")
file_jar  = pathjoin(dir_jar, "PurpleWave.jar")

def log(*args, **kwargs):
  print(*args, **kwargs, flush=True)
def logf(f, *args, **kwargs):
  log(f.__name__, args, kwargs)
  f(*args, **kwargs)
  
def rmtree(dir):
  if os.path.exists(dir):
    shutil.rmtree(dir)

def makejre():
  log()
  log("MAKING JRE")
  jdeps = pathjoin(dir_jre, "bin", "jdeps.exe")
  log(jdeps)
  jdeps_stdout = subprocess.run(
    [ jdeps,
      "--print-module-deps",
      "--multi-release",
      java_version,
      "-recursive",
      "-cp",
      dir_jar,
      file_jar ],
    capture_output=True,
    text=True).stdout
  print(jdeps_stdout)
  modules = jdeps_stdout.strip().split(',')
  print(modules)
  logf(rmtree, path_staging("jre"))
  
  jlink = pathjoin(dir_jre, "bin", "jlink.exe")
  log(jlink)
  subprocess.run(
    [ jlink,
      "--add-modules",
      "jdk.management.agent," + ",".join(modules),
      "--output",
      path_staging("jre") ])

def copyjre():   
  log()
  log("COPYING JRE")
  for race in ["Protoss", "Terran", "Zerg", "Random"]:
    dir_jre = path_bots(f"Purple{race}/AI/jre")    
    logf(rmtree, dir_jre)
    logf(shutil.copytree, path_staging("jre"), dir_jre)

def stage():
  log()
  log("STAGING")
  log("Post-build steps")
  logf(shutil.copy2, file_jar, dir_staging)
  
  with open(path_staging("revision.txt"), 'w') as revision_file:
    subprocess.run(["git", "rev-parse", "HEAD"], stdout=revision_file, text=True, cwd=dir_pw)
  with open(path_staging("timestamp.txt"), 'w') as timestamp_file:
    subprocess.run(["date"], stdout=timestamp_file, text=True)

  log("Building EXE with Launch4J")
  subprocess.run(file_launch4j + " "+  path_pw("launch4j.xml"))
  
  log()
  log("Populate local testing")
  populate_bwapidata(dir_bwapidata)
  logf(shutil.copy2, path_configs("PurpleWaveLocalMetal.config.json"), pathjoin(dir_bwapidata, "AI") )
  
  log()
  log("Populate SC-Docker")  
  for race in ["Protoss", "Terran", "Zerg", "Random"]:    
    name = f"Purple{race}"
    log(race)
    log(name)
    dir_bot_bwapidata = path_bots(name)
    populate_bwapidata(dir_bot_bwapidata)
    logf(shutil.copy2, path_configs("PurpleWaveLocalDocker.config.json"),  pathjoin(dir_bot_bwapidata, "ai"))    
    # Customize bot.json
    bot_json = pathjoin(dir_bot_bwapidata, "bot.json")
    content = ""
    with open(bot_json, 'r') as file:
      content = file.read().replace("Protoss", race).replace("PurpleWave", name)
    with open(bot_json, 'w') as file:
      file.write(content)
      
def populate_bwapidata(dir_bwapidata):
  dir_ai = pathjoin(dir_bwapidata, "ai")
  log(dir_bwapidata)
  log(dir_ai)
  logf(os.makedirs,                                   dir_ai, exist_ok=True)
  logf(shutil.copy2, path_staging("PurpleWave.jar"),  dir_ai)
  logf(shutil.copy2, path_staging("PurpleWave.exe"),  dir_ai)
  logf(shutil.copy2, path_staging("timestamp.txt"),   dir_ai)
  logf(shutil.copy2, path_staging("revision.txt"),    dir_ai)  
  logf(shutil.copy2, path_configs("run_proxy.bat"),   dir_ai)
  logf(shutil.copy2, path_configs("run64.bat"),       dir_ai)
  logf(shutil.copy2, path_configs("run32.bat"),       dir_ai)
  logf(shutil.copy2, path_configs("bot.json"),        dir_bwapidata)
  logf(shutil.copy2, path_configs("bwapi.dll"),       dir_bwapidata)

def package():
  log()
  log("PACKAGING")
  for package in ["AIIDE", "SSCAIT", "SCHNAIL"]:
    package_name = f"PurpleWave{package}"
    package_dir  = path_staging(package_name)
    log(package_name)
    log(package_dir)
    logf(rmtree,        package_dir)
    logf(os.makedirs,   package_dir)
    logf(shutil.copy2,  path_staging("PurpleWave.jar"),               package_dir)
    logf(shutil.copy2,  path_staging("PurpleWave.exe"),               package_dir)
    logf(shutil.copy2,  path_staging("timestamp.txt"),                package_dir)
    logf(shutil.copy2,  path_staging("revision.txt"),                 package_dir)
    logf(shutil.copy2,  path_configs(f"{package_name}.config.json"),  package_dir)
  
  log()
  log("Copying package-specific resources")
  logf(shutil.copy2, path_configs("BWAPI.dll"),     path_staging("PurpleWaveSSCAIT"))
  logf(shutil.copy2, path_configs("BWAPI.dll"),     path_staging("PurpleWaveSCHNAIL"))
  logf(shutil.copy2, path_configs("run_proxy.bat"), path_staging("PurpleWaveSSCAIT"))
  logf(shutil.copy2, path_configs("run_proxy.bat"), path_staging("PurpleWaveAIIDE"))
  open(path_staging(pathjoin("PurpleWaveAIIDE", "PurpleWave.dll")), 'w').close()
  
  log()
  log("Replacing zips")
  for package in ["AIIDE", "SSCAIT", "SCHNAIL"]:
    package_name = f"PurpleWave{package}"
    package_dir  = path_staging(package_name)
    log(package_name)
    log(package_dir)
    with contextlib.suppress(FileNotFoundError):
      logf(os.remove,         path_staging(f"{package_name}.zip"))
    logf(shutil.make_archive, path_staging(package_name), 'zip', path_staging(package_name))
    
if __name__ == "__main__":
  main()

