import argparse
import contextlib
import hashlib
import http.cookiejar
import io
import json
import subprocess
import shutil
import sys
import textwrap
import os
import urllib.error
import urllib.parse
import urllib.request
import uuid
import zipfile

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
  parser.add_argument('--tp',             action='store_true', default=False, help='Deploy Terran and Protoss to BASIL')
  parser.add_argument('--tz',             action='store_true', default=False, help='Deploy Terran and Zerg to BASIL')
  parser.add_argument('--pz',             action='store_true', default=False, help='Deploy Protoss and Zerg to BASIL')
  parser.add_argument('--tpz',            action='store_true', default=False, help='Deploy all races to BASIL')
  parser.add_argument('--psscait',        action='store_true', default=False, help='Deploy Protoss (PurpleWave) to SSCAIT')
  parser.add_argument('--tsscait',        action='store_true', default=False, help='Deploy Terran (PurpleSpirit) to SSCAIT')
  parser.add_argument('--zsscait',        action='store_true', default=False, help='Deploy Zerg (PurpleSwarm) to SSCAIT')
  parser.add_argument('--rsscait',        action='store_true', default=False, help='Deploy Random (PurpleDestiny) to SSCAIT')
  parser.add_argument('--ticklesscait',   action='store_true', default=False, help='Deploy PurpleTickles to SSCAIT')
  parser.add_argument('--csscait',        action='store_true', default=False, help='Deploy Random cheese (PurpleCheese) to SSCAIT')
  parser.add_argument('--pschnail',       action='store_true', default=False, help='Deploy Protoss (PurpleWave) to SCHNAIL')
  parser.add_argument('--play',           action='store_true', default=False, help='Play ladder games')
  parser.add_argument('--viz',            action='store_true', default=False, help='Construct PurpleSimViz.exe')
  args = parser.parse_args()
  
  args.t = args.t or args.tp or args.tz or args.tpz
  args.p = args.p or args.tp or args.pz or args.tpz
  args.z = args.z or args.tz or args.pz or args.tpz
  
  do_basil = args.t or args.p or args.z
  selected_sscait = [deployment for flag, deployment in sscait_deployments.items() if getattr(args, flag)]
  if do_basil or selected_sscait or args.pschnail:
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
  for bot_name, email_key, password_key in selected_sscait:
    deploy_sscait(bot_name, email_key, password_key)
  if args.pschnail:
    deploy_schnail()
  if args.play:
    did_anything = True
    play()
  if args.viz:
    did_anything = True
    package_sim_viz()
  
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

sscait_deployments = {
  "psscait":      ("PurpleWave",    "SSCAIT_PURPLEWAVE_EMAIL",    "SSCAIT_PURPLEWAVE_PASSWORD"),
  "tsscait":      ("PurpleSpirit",  "SSCAIT_PURPLESPIRIT_EMAIL",  "SSCAIT_PURPLESPIRIT_PASSWORD"),
  "zsscait":      ("PurpleSwarm",   "SSCAIT_PURPLESWARM_EMAIL",   "SSCAIT_PURPLESWARM_PASSWORD"),
  "rsscait":      ("PurpleDestiny", "SSCAIT_PURPLEDESTINY_EMAIL", "SSCAIT_PURPLEDESTINY_PASSWORD"),
  "ticklesscait": ("PurpleTickles", "SSCAIT_PURPLETICKLES_EMAIL", "SSCAIT_PURPLETICKLES_PASSWORD"),
  "csscait":      ("PurpleCheese",  "SSCAIT_PURPLECHEESE_EMAIL",  "SSCAIT_PURPLECHEESE_PASSWORD")}
schnail_deployment = ("PurpleWave", "SCHNAIL_PURPLEWAVE_EMAIL", "SCHNAIL_PURPLEWAVE_PASSWORD")

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

def load_env():
  env_path = path_pw(".env")
  if not os.path.isfile(env_path):
    raise RuntimeError(f"Missing password file: {env_path}")
  with open(env_path, 'r', encoding='utf-8-sig') as env_file:
    for line_number, raw_line in enumerate(env_file, 1):
      line = raw_line.strip()
      if not line or line.startswith('#'):
        continue
      if '=' not in line:
        raise RuntimeError(f"Invalid .env line {line_number}: expected NAME=VALUE")
      key, value = line.split('=', 1)
      key = key.strip()
      value = value.strip()
      if len(value) >= 2 and value[0] == value[-1] and value[0] in "'\"":
        value = value[1:-1]
      os.environ.setdefault(key, value)

def require_env(key):
  load_env()
  value = os.environ.get(key, '')
  if not value:
    raise RuntimeError(f"Populate {key} in {path_pw('.env')}")
  return value

def request_bytes(request, opener=None):
  open_request = urllib.request.urlopen if opener is None else opener.open
  try:
    with open_request(request, timeout=180) as response:
      return response.read()
  except urllib.error.HTTPError as error:
    details = error.read().decode('utf-8', errors='replace').strip()
    raise RuntimeError(f"HTTP {error.code} from {request.full_url}: {details}") from error
  except urllib.error.URLError as error:
    raise RuntimeError(f"Could not reach {request.full_url}: {error.reason}") from error

def request_json(request, opener=None):
  response = request_bytes(request, opener)
  try:
    return json.loads(response.decode('utf-8'))
  except (UnicodeDecodeError, json.JSONDecodeError) as error:
    raise RuntimeError(f"Invalid JSON from {request.full_url}: {response[:500]!r}") from error

def multipart_form(fields, files):
  boundary = f"----PurpleWave{uuid.uuid4().hex}"
  body = bytearray()
  for name, value in fields:
    body.extend(f"--{boundary}\r\n".encode())
    body.extend(f'Content-Disposition: form-data; name="{name}"\r\n\r\n'.encode())
    body.extend(str(value).encode('utf-8'))
    body.extend(b"\r\n")
  for name, path in files:
    filename = os.path.basename(path)
    body.extend(f"--{boundary}\r\n".encode())
    body.extend(f'Content-Disposition: form-data; name="{name}"; filename="{filename}"\r\n'.encode())
    body.extend(b"Content-Type: application/zip\r\n\r\n")
    with open(path, 'rb') as upload_file:
      body.extend(upload_file.read())
    body.extend(b"\r\n")
  body.extend(f"--{boundary}--\r\n".encode())
  return bytes(body), f"multipart/form-data; boundary={boundary}"

def zip_manifest(source, excluded_files=()):
  excluded_files = {filename.lower() for filename in excluded_files}
  with zipfile.ZipFile(source, 'r') as archive:
    corrupt_file = archive.testzip()
    if corrupt_file is not None:
      raise RuntimeError(f"Corrupt ZIP member: {corrupt_file}")
    files = [entry for entry in archive.infolist() if not entry.is_dir() and entry.filename.lower() not in excluded_files]
    names = [entry.filename for entry in files]
    if len(names) != len(set(names)):
      raise RuntimeError("ZIP contains duplicate filenames")
    return {entry.filename: hashlib.sha256(archive.read(entry)).hexdigest() for entry in files}

def verify_downloaded_zip(package_path, downloaded_zip, excluded_files=()):
  expected = zip_manifest(package_path, excluded_files)
  actual = zip_manifest(io.BytesIO(downloaded_zip))
  if expected != actual:
    missing = sorted(expected.keys() - actual.keys())
    unexpected = sorted(actual.keys() - expected.keys())
    changed = sorted(name for name in expected.keys() & actual.keys() if expected[name] != actual[name])
    raise RuntimeError(f"Downloaded ZIP does not match upload; missing={missing}, unexpected={unexpected}, changed={changed}")
  log(f"Verified downloaded ZIP ({len(actual)} files)")

def deploy_sscait(bot_name, email_key, password_key):
  log()
  log(f"DEPLOYING {bot_name} TO SSCAIT")
  package_path = path_staging("PurpleWaveSSCAIT.zip")
  email = require_env(email_key)
  password = require_env(password_key)
  cookie_jar = http.cookiejar.CookieJar()
  opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cookie_jar))

  login_request = urllib.request.Request(
    "https://sscaitournament.com/users/login_submit.php",
    data=urllib.parse.urlencode({"username": email, "password": password}).encode(),
    headers={"Content-Type": "application/x-www-form-urlencoded", "User-Agent": "PurpleWave deployment tool"})
  login_response = request_json(login_request, opener)
  if login_response.get("status") != 1:
    raise RuntimeError(f"SSCAIT login failed for {bot_name}: {login_response.get('txt', login_response)}")

  upload_body, content_type = multipart_form([("user", bot_name)], [("bot_binary", package_path)])
  upload_request = urllib.request.Request(
    "https://sscaitournament.com/users/users/new_binary_submit.php",
    data=upload_body,
    headers={"Content-Type": content_type, "User-Agent": "PurpleWave deployment tool"})
  upload_response = request_json(upload_request, opener)
  if upload_response.get("status") != 1:
    raise RuntimeError(f"SSCAIT upload failed for {bot_name}: {upload_response.get('txt', upload_response)}")

  download_url = "https://sscaitournament.com/bot_binary.php?" + urllib.parse.urlencode({"bot": bot_name, "verify": uuid.uuid4().hex})
  downloaded_zip = request_bytes(urllib.request.Request(download_url, headers={"User-Agent": "PurpleWave deployment tool"}))
  verify_downloaded_zip(package_path, downloaded_zip, {"bwapi.dll"})

def deploy_schnail():
  bot_name, email_key, password_key = schnail_deployment
  log()
  log(f"DEPLOYING {bot_name} TO SCHNAIL")
  package_path = path_staging("PurpleWaveSCHNAIL.zip")
  email = require_env(email_key)
  password = require_env(password_key)
  base_url = "https://app.schnail.com:8181/schnail-serv/rest"

  login_body = json.dumps({"email": email, "password": password}).encode('utf-8')
  login_request = urllib.request.Request(
    f"{base_url}/user/auth",
    data=login_body,
    headers={"Content-Type": "application/json", "User-Agent": "PurpleWave deployment tool"})
  login_response = request_json(login_request)
  if not login_response.get("success") or not login_response.get("token") or not login_response.get("guid"):
    raise RuntimeError(f"SCHNAIL login failed: {login_response.get('message', login_response)}")
  authorization = f"Bearer {login_response['token']}"

  bots_url = f"{base_url}/bots/botInfosForAuthor?" + urllib.parse.urlencode({"guid": login_response["guid"]})
  bots_request = urllib.request.Request(bots_url, headers={"Authorization": authorization, "User-Agent": "PurpleWave deployment tool"})
  bots = request_json(bots_request)
  matching_bots = [bot for bot in bots if bot.get("name") == bot_name]
  if len(matching_bots) != 1:
    raise RuntimeError(f"Expected one SCHNAIL bot named {bot_name}; found {len(matching_bots)}")
  bot = matching_bots[0]
  bot_guid = bot["guid"]

  upload_body, content_type = multipart_form(
    [
      ("botGuid", bot_guid),
      ("file.name", os.path.basename(package_path)),
      ("maplist", ",".join(bot.get("mapGuids", [])))],
    [("file", package_path)])
  upload_request = urllib.request.Request(
    f"{base_url}/bots/updateBot",
    data=upload_body,
    headers={"Authorization": authorization, "Content-Type": content_type, "User-Agent": "PurpleWave deployment tool"})
  upload_response = request_json(upload_request)
  if not upload_response.get("success"):
    raise RuntimeError(f"SCHNAIL upload failed: {upload_response.get('message', upload_response)}")

  download_url = f"{base_url}/bots/requestBotFile?" + urllib.parse.urlencode({"guid": bot_guid})
  downloaded_zip = request_bytes(urllib.request.Request(download_url, headers={"User-Agent": "PurpleWave deployment tool"}))
  verify_downloaded_zip(package_path, downloaded_zip)

def play(threads=3):
  ladder_command = "/c/p/shorts/ladder.sh"
  log_path = "/c/Users/d/AppData/Roaming/scbw/ladder.log"
  bash_command = textwrap.dedent(fr'''
    set -e
    trap 'kill -- -$$' INT TERM EXIT
    LOG="{log_path}"
    THREADS={threads}
    t=0
    while [ $t -lt $THREADS ]; do
      THREADS=$THREADS THREAD=$t sh -lc "{ladder_command} | tee -a \"$LOG\"" &
      t=$((t+1))
    done
    tail -f "$LOG" &
    wait
  ''')
  bash_process = subprocess.Popen(["bash", "-lc", bash_command])
  try:
    bash_process.wait()
  except KeyboardInterrupt:
    bash_process.wait()
    subprocess.Popen(["dockerstop.sh"])
    
def package_sim_viz():
  subprocess.run(file_launch4j + " " +  path_configs("launch4jPurpleSimViz.xml"))
    
if __name__ == "__main__":  
  main()

