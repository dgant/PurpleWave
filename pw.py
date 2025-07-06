import argparse
import contextlib
import subprocess
import shutil
import sys
import os

def main():
  parser = argparse.ArgumentParser(description="Compile, stage, and package PurpleWave and its JREs.")
  parser.add_argument('--all',            action='store_true', default=False, help='Perform all steps')
  parser.add_argument('--jre',            action='store_true', default=False, help='Step 1: (Skipped by default) Package lightweight JRE using jdeps+jlink, then stage them for packaging')
  parser.add_argument('--maven',          action='store_true', default=False, help='Step 2: Compile PurpleWave with Maven')
  parser.add_argument('--schnail',        action='store_true', default=False, help='Step 3: Compile SCHNAIL launcher')
  parser.add_argument('--stage',          action='store_true', default=False, help='Step 4: Record deployment information, construct EXE via launch4j, then copy bot binaries and deployment information')
  parser.add_argument('--package',        action='store_true', default=False, help='Step 5: Re-zip directories')
  parser.add_argument('--t',              action='store_true', default=False, help='Deploy Terran (PurpleSpirit) to BASIL')
  parser.add_argument('--p',              action='store_true', default=False, help='Deploy Protoss (PurpleSpirit) to BASIL')
  parser.add_argument('--z',              action='store_true', default=False, help='Deploy Zerg (PurpleSpirit) to BASIL')
  
  args = parser.parse_args()
  
  do_basil = args.t or args.p or args.z
  if do_basil:
    args.stage = True
    args.package = True
  
  did_anything = False
  if args.all or args.jre:
    did_anything = True
    makejre()
  if args.all or args.maven:
    did_anything = True
    maven_build()
  if args.all or args.schnail:
    did_anything = True
    schnail()
  if args.all or args.stage:
    did_anything = True
    stage()
  if args.all or args.package:
    did_anything = True
    package()
  if do_basil:
    copy_basil(args.t, args.p, args.z)
  
  if not did_anything:
    maven_build()
    schnail()
    stage()
    package()

def pathjoin(*args):
  result = '/'.join(map(str, args))
  while '//' in result:
    result = result.replace('//', '/')
  return result  

file_launch4j     = "c:/Program Files (x86)/Launch4j/Launch4jc.exe"
dir_jre           = "c:/p/graalvm-jdk/"
dir_bwapidata     = "c:/p/bw/bwapi-data/"
java_version      = "21"
dir_pw            = "c:/p/pw/"
dir_maven_target  = "c:/p/pw/target/"
dir_scbwbots      = "c:/Users/d/AppData/Roaming/scbw/bots/"
dir_localschnail  = "c:/Program Files (x86)/SCHNAIL Client/bots/PurpleWave/"
dir_basiluploads  = "d:/Dropbox/StarcraftBots/"
dir_out           = pathjoin(dir_pw,  "out/")
dir_configs       = pathjoin(dir_pw,  "configs/")
dir_staging       = pathjoin(dir_out, "staging/")
dir_jar           = pathjoin(dir_out, "artifacts", "PurpleWave")
file_jar          = pathjoin(dir_jar, "PurpleWave.jar")

def path_pw(path):
  return pathjoin(dir_pw, path)
def path_out(path):
  return pathjoin(dir_out, path)  
def path_configs(path):
  return pathjoin(dir_configs, path)  
def path_staging(path):
  return pathjoin(dir_staging, path)
def path_scbwbots(path):
  return pathjoin(dir_scbwbots, path)
def path_basiluploads(path):
  return pathjoin(dir_basiluploads, path)
  
def log(*args, **kwargs):
  print(*args, **kwargs, flush=True)
def logf(f, *args, **kwargs):
  log(f.__name__, args, kwargs)
  f(*args, **kwargs)
  
def rmtree(dir):
  if os.path.exists(dir):
    logf(shutil.rmtree, dir)
    
def maven_build():
  log()
  log("RUNNING MAVEN BUILD")
  environment = os.environ.copy()
  # Maven's default heap space is 512mb. We must increase it or compiler inlining will fail due to "Error while emitting"/"Java heap space"
  environment["MAVEN_OPTS"] = "\"-Xmx1024m\"" 
  logf(subprocess.run, ["mvn", "clean", "install"], cwd=dir_pw, env=environment, shell=True)
  dir_jar  = dir_maven_target
  global file_jar
  file_jar = pathjoin(dir_maven_target, "PurpleWave.jar")  
  logf(shutil.copy2, pathjoin(dir_maven_target, "PurpleWave-1.0.jar"), file_jar)

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
  logf(
    subprocess.run,
    [ pathjoin(dir_jre, "bin", "jlink.exe"),
      "--add-modules",
      "jdk.management.agent," + ",".join(modules),
      "--output",
      path_staging("jre") ])


schnail_exe_source="PurpleWaveSCHNAILCPP.exe"
def schnail():
  log()
  log("BUILDING SCHNAIL LAUNCHER")
  subprocess.run(file_launch4j + " "+  path_configs("launch4jSCHNAIL.xml"))
  subprocess.run(["x86_64-w64-mingw32-g++", "-static", "-o", path_staging("PurpleWaveSCHNAILCPP.exe"), path_pw("/src/PurpleWaveSCHNAIL.exe.cpp")])    
  logf(shutil.copy2, path_staging(schnail_exe_source), path_staging("PurpleWaveSCHNAIL.exe"))
  
def stage():
  log()
  log("STAGING")
  log("Post-build steps")
  logf(shutil.copy2, file_jar, dir_staging)
  
  with open(path_staging("revision.txt"), 'w') as revision_file:
    subprocess.run(["git", "rev-parse", "HEAD"], stdout=revision_file, text=True, cwd=dir_pw)
  with open(path_staging("timestamp.txt"), 'w') as timestamp_file:
    subprocess.run(["date"], stdout=timestamp_file, text=True)
  logf(shutil.copy2, file_jar, path_staging("PurpleWave.jar"))

  log("Building EXE with Launch4J")
  subprocess.run(file_launch4j + " "+  path_configs("launch4j.xml"))
  
  log()
  log("Populate local testing")
  populate_bwapidata(dir_bwapidata)
  logf(shutil.copy2, path_configs("PurpleWaveLocalMetal.config.json"), pathjoin(dir_bwapidata, "AI") )
  
  log()
  if os.path.exists(dir_scbwbots):
    log("Populate SC-Docker")  
    for race in ["Protoss", "Terran", "Zerg", "Random"]:    
      name = f"Purple{race}"
      log(race)
      log(name)
      dir_bot_bwapidata = path_scbwbots(name)
      populate_bwapidata(dir_bot_bwapidata)
      logf(shutil.copy2, path_configs("bot.json"),                          dir_bot_bwapidata)
      logf(shutil.copy2, path_configs("PurpleWaveLocalDocker.config.json"), pathjoin(dir_bot_bwapidata, "ai"))
      # Customize bot.json
      bot_json = pathjoin(dir_bot_bwapidata, "bot.json")
      content = ""
      with open(bot_json, 'r') as file:
        content = file.read().replace("Protoss", race).replace("PurpleWave", name)
      with open(bot_json, 'w') as file:
        file.write(content)
  else:
    log("Did not find SC-Docker")
      
  log()
  if (os.path.exists(dir_localschnail)):
    log("Populate SCHNAIL")
    #populate_bwapidata(dir_localschnail, dir_localschnail)
    #logf(shutil.copy2, path_configs("PurpleWaveSCHNAIL.config.json"), dir_localschnail)
    #logf(shutil.copy2, path_staging("PurpleWaveSCHNAIL.exe"), dir_localschnail)
  else:
    log("Did not find SCHNAIL")
      
def populate_bwapidata(dir_bwapidata, dir_ai=None):
  dir_ai = pathjoin(dir_bwapidata, "ai") if dir_ai is None else dir_ai
  log(dir_bwapidata)
  log(dir_ai)
  logf(os.makedirs,                                   dir_ai, exist_ok=True)  
  logf(shutil.copy2, path_configs("bwapi.dll"),       dir_bwapidata)
  logf(shutil.copy2, path_staging("PurpleWave.jar"),  dir_ai)
  logf(shutil.copy2, path_staging("timestamp.txt"),   dir_ai)
  logf(shutil.copy2, path_staging("revision.txt"),    dir_ai)  
  logf(shutil.copy2, path_configs("run_proxy.bat"),   dir_ai)
  logf(shutil.copy2, path_configs("run64.bat"),       dir_ai)
  logf(shutil.copy2, path_configs("run32.bat"),       dir_ai)

def package():
  log()
  log("PACKAGING")
  for package in ["AIIDE", "BASIL", "SSCAIT", "SCHNAIL"]:
    package_name = f"PurpleWave{package}"
    package_dir  = path_staging(package_name)
    log(package_name)
    log(package_dir)
    logf(rmtree,        package_dir)
    logf(os.makedirs,   package_dir)
    populate_bwapidata(package_dir, package_dir)
    logf(shutil.copy2,  path_configs(f"{package_name}.config.json"),  package_dir)
    if package in ["AIIDE"]:
      #logf(shutil.copytree, path_staging("jre"), pathjoin(package_dir, "jre"))
      open(pathjoin(package_dir, "PurpleWave.dll"), 'w').close()
    # TODO: AIIDE does not want the bwapi.dll
    if package in ["SCHNAIL"]:
      logf(shutil.copy2, path_staging(schnail_exe_source), pathjoin(package_dir, "PurpleWaveSCHNAIL.exe"))
  
  log()
  log("Replacing zips")
  for package in ["AIIDE", "BASIL", "SSCAIT", "SCHNAIL"]:
    package_name = f"PurpleWave{package}"
    package_dir  = path_staging(package_name)
    log(package_name)
    log(package_dir)
    with contextlib.suppress(FileNotFoundError):
      logf(os.remove,         path_staging(f"{package_name}.zip"))
    logf(shutil.make_archive, path_staging(package_name), 'zip', path_staging(package_name))
    logf(rmtree,              path_staging(package_name))
  
def copy_basil(t, p, z):
  if t:
    logf(shutil.copy2, path_staging("PurpleWaveBASIL.zip"), path_basiluploads("PurpleSpiritBASIL.zip"))
  if p:
    logf(shutil.copy2, path_staging("PurpleWaveBASIL.zip"), path_basiluploads("PurpleWaveBASIL.zip"))
  if z:
    logf(shutil.copy2, path_staging("PurpleWaveBASIL.zip"), path_basiluploads("PurpleSwarmBASIL.zip"))
    
if __name__ == "__main__":
  main()

