#!/usr/bin/env python3
"""Run a native Pluto game with SC-Docker-compatible inputs and outputs."""

from __future__ import annotations

import argparse
import filecmp
import json
import os
from pathlib import Path
import random
import re
import shlex
import shutil
import string
import subprocess
import sys
import time


CREATE_NO_WINDOW = 0x08000000
SCBW_ROOT = Path(r"E:\scbw")
NATIVE_ROOT = SCBW_ROOT / "native"
TEMPLATE_DIR = NATIVE_ROOT / "template"
BIN_DIR = NATIVE_ROOT / "bin"
BWHEADLESS = BIN_DIR / "bwheadless.exe"
REQUIRED_TEMPLATE_FILES = (
    "BROODAT.MPQ",
    "patch_rt.mpq",
    "StarCraft.exe",
    "StarCraft.mpq",
    "STARDAT.MPQ",
    "storm.dll",
)
RACE_NAMES = {"P": "Protoss", "T": "Terran", "Z": "Zerg", "R": "Random"}


class NativeGameError(RuntimeError):
    pass


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Play PurpleWave against 64-bit Pluto natively."
    )
    parser.add_argument("--bots", nargs=2, required=True, metavar="BOT_NAME[:RACE]")
    parser.add_argument("--map", default="sscai/(2)Benzene.scx")
    parser.add_argument("--headless", action="store_true")
    parser.add_argument("--game_name", default=random_name())
    parser.add_argument("--game_type", default="FREE_FOR_ALL")
    parser.add_argument("--game_speed", type=int, default=0)
    parser.add_argument("--seed_override", type=int)
    parser.add_argument("--timeout", type=int)
    parser.add_argument("--timeout_at_frame", type=int)
    parser.add_argument("--hide_names", action="store_true")
    parser.add_argument("--random_names", action="store_true")
    parser.add_argument("--allow_input", action="store_true")
    parser.add_argument("--read_overwrite", action="store_true")
    parser.add_argument("--bot_dir", default=str(SCBW_ROOT / "bots"))
    parser.add_argument("--game_dir", default=str(SCBW_ROOT / "games"))
    parser.add_argument("--map_dir", default=str(SCBW_ROOT / "maps"))
    parser.add_argument("--bwapi_data_bwta_dir", default=str(SCBW_ROOT / "bwapi-data" / "BWTA"))
    parser.add_argument("--bwapi_data_bwta2_dir", default=str(SCBW_ROOT / "bwapi-data" / "BWTA2"))
    parser.add_argument("--docker_image", default="native-pluto")
    parser.add_argument("--vnc_base_port", type=int, default=5900)
    parser.add_argument("--vnc_host", default="localhost")
    parser.add_argument("--capture_movement", action="store_true")
    parser.add_argument("--auto_launch", action="store_true")
    parser.add_argument("--show_all", action="store_true")
    parser.add_argument("--plot_realtime", action="store_true")
    parser.add_argument("--nano_cpus", type=int)
    parser.add_argument("--mem_limit")
    parser.add_argument("--keep_runtime", action="store_true", help=argparse.SUPPRESS)
    args = parser.parse_args()

    if not args.headless:
        parser.error("native Pluto games currently require --headless")
    if args.game_type != "FREE_FOR_ALL":
        parser.error("native Pluto games currently support FREE_FOR_ALL only")
    unsupported = (
        args.capture_movement,
        args.auto_launch,
        args.show_all,
        args.plot_realtime,
        args.nano_cpus is not None,
        args.mem_limit is not None,
    )
    if any(unsupported):
        parser.error("VNC, plotting, and Docker resource flags are not available natively")
    return args


def random_name() -> str:
    return "".join(random.choice(string.hexdigits.upper()[:16]) for _ in range(8))


def path_is_within(path: Path, parent: Path) -> bool:
    try:
        path.resolve().relative_to(parent.resolve())
        return True
    except ValueError:
        return False


def robocopy(source: Path, destination: Path, mirror: bool = False) -> None:
    destination.mkdir(parents=True, exist_ok=True)
    completed = subprocess.run(
        [
            "robocopy",
            str(source),
            str(destination),
            "/MIR" if mirror else "/E",
            "/COPY:DAT",
            "/DCOPY:DAT",
            "/R:2",
            "/W:1",
            "/NFL",
            "/NDL",
            "/NJH",
            "/NJS",
            "/NP",
        ],
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
        creationflags=CREATE_NO_WINDOW,
    )
    if completed.returncode >= 8:
        raise NativeGameError(f"robocopy failed from {source} to {destination}: {completed.returncode}")


def make_junction(link: Path, target: Path) -> None:
    target.mkdir(parents=True, exist_ok=True)
    link.parent.mkdir(parents=True, exist_ok=True)
    completed = subprocess.run(
        ["cmd", "/d", "/c", "mklink", "/J", str(link), str(target)],
        capture_output=True,
        text=True,
        creationflags=CREATE_NO_WINDOW,
    )
    if completed.returncode != 0:
        raise NativeGameError(f"could not junction {link} to {target}: {completed.stderr.strip()}")


def remove_junction(link: Path) -> None:
    if link.exists():
        os.rmdir(link)


def copy_if_changed(source: Path, destination: Path) -> None:
    if not destination.is_file() or not filecmp.cmp(source, destination, shallow=False):
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, destination)


def ensure_player_install(install: Path) -> None:
    install.mkdir(parents=True, exist_ok=True)
    private_files = {"bncache.dat", "wmode.ini"}
    for source in TEMPLATE_DIR.iterdir():
        if not source.is_file():
            continue
        destination = install / source.name
        if destination.exists():
            continue
        if source.name.lower() in private_files:
            shutil.copy2(source, destination)
        else:
            os.link(source, destination)
    characters = install / "characters"
    if not characters.exists():
        shutil.copytree(TEMPLATE_DIR / "characters", characters)
    (install / "maps" / "replays").mkdir(parents=True, exist_ok=True)


def validate_installation() -> None:
    required = [BWHEADLESS]
    required.extend(TEMPLATE_DIR / name for name in REQUIRED_TEMPLATE_FILES)
    missing = [str(path) for path in required if not path.is_file()]
    if missing:
        raise NativeGameError("native Pluto installation is incomplete:\n" + "\n".join(missing))


def parse_bot(spec: str, bot_dir: Path) -> dict:
    directory_name, separator, race_override = spec.rpartition(":")
    if not separator or race_override.upper() not in RACE_NAMES:
        directory_name = spec
        race_override = ""
    source = bot_dir / directory_name
    try:
        metadata = json.loads((source / "bot.json").read_text(encoding="utf-8"))
    except FileNotFoundError as exc:
        raise NativeGameError(f"bot is missing bot.json: {source}") from exc
    bot_type = metadata["botType"].upper()
    extension = {
        "AI_MODULE": ".dll",
        "DLL": ".dll",
        "JAVA": ".jar",
        "JAVA_JNI": ".jar",
        "JAVA_MIRROR": ".jar",
    }.get(bot_type)
    if extension is None:
        raise NativeGameError(f"unsupported native bot type for {directory_name}: {bot_type}")
    binaries = [path for path in (source / "AI").glob(f"*{extension}") if "BWAPI" not in path.name.upper()]
    if len(binaries) != 1:
        raise NativeGameError(f"expected one {extension} launcher in {source / 'AI'}, found {len(binaries)}")
    race_value = race_override.upper() or metadata["race"][0].upper()
    if race_value not in RACE_NAMES:
        raise NativeGameError(f"unsupported race for {directory_name}: {race_value}")
    return {
        "spec": spec,
        "directory_name": directory_name,
        "source": source,
        "metadata": metadata,
        "name": metadata["name"],
        "race": race_value,
        "type": "dll" if extension == ".dll" else "jar",
        "binary_name": binaries[0].name,
    }


def write_bwapi_ini(player: dict, install: Path, args: argparse.Namespace) -> None:
    ai = f"bwapi-data/AI/{player['binary_name']}" if player["type"] == "dll" else "NULL"
    shared_memory = "OFF" if player["type"] == "dll" else "ON"
    seed = "" if args.seed_override is None else str(args.seed_override)
    content = (
        "[ai]\n"
        f"ai = {ai}\n"
        "ai_dbg = NULL\n"
        "tournament = NULL\n"
        "\n"
        "[auto_menu]\n"
        "auto_menu = OFF\n"
        "save_replay = bwapi-data/write/native-game.rep\n"
        "\n"
        "[config]\n"
        "holiday = OFF\n"
        f"shared_memory = {shared_memory}\n"
        "console_attach_on_startup = FALSE\n"
        "console_alloc_on_startup = FALSE\n"
        "\n"
        "[starcraft]\n"
        "sound = OFF\n"
        "screenshots = gif\n"
        f"speed_override = {args.game_speed}\n"
        f"seed_override = {seed}\n"
        "drop_players = OFF\n"
    )
    (install / "bwapi-data" / "bwapi.ini").write_text(content, encoding="ascii", newline="\r\n")


def stage_player(
    player: dict,
    nth_player: int,
    runtime: Path,
    result_dir: Path,
    map_source: Path,
    args: argparse.Namespace,
) -> dict:
    install = runtime / f"player_{nth_player}"
    ensure_player_install(install)
    data_dir = install / "bwapi-data"
    ai_dir = data_dir / "AI"
    read_dir = data_dir / "read"
    for junction in (data_dir / "write", data_dir / "BWTA", data_dir / "BWTA2", install / "Errors"):
        remove_junction(junction)
    robocopy(player["source"] / "AI", ai_dir, mirror=True)
    if (player["source"] / "supplementalAI").is_dir():
        robocopy(player["source"] / "supplementalAI", ai_dir)
    robocopy(player["source"] / "read", read_dir, mirror=True)
    if (player["source"] / "supplementalRead").is_dir():
        robocopy(player["source"] / "supplementalRead", read_dir)
    copy_if_changed(player["source"] / "BWAPI.dll", data_dir / "BWAPI.dll")
    for name in ("pluto.log", "pluto_infer.log", "wmode.ini", "__log.txt"):
        path = install / name
        if path.is_file():
            path.unlink()

    logs_dir = result_dir / f"logs_{nth_player}"
    crashes_dir = result_dir / f"crashes_{nth_player}"
    write_dir = result_dir / f"write_{nth_player}"
    for directory in (logs_dir, crashes_dir, write_dir):
        directory.mkdir(parents=True, exist_ok=True)
    make_junction(data_dir / "write", write_dir)
    make_junction(data_dir / "BWTA", Path(args.bwapi_data_bwta_dir))
    make_junction(data_dir / "BWTA2", Path(args.bwapi_data_bwta2_dir))
    make_junction(install / "Errors", crashes_dir)

    replay = result_dir / f"player_{nth_player}.rep"
    write_bwapi_ini(player, install, args)
    return {
        **player,
        "nth_player": nth_player,
        "install": install,
        "map": map_source,
        "logs": logs_dir,
        "write": write_dir,
        "replay": replay,
    }


def launch(log_path: Path, command: list[str], cwd: Path, environment: dict[str, str]) -> tuple[subprocess.Popen, object]:
    handle = log_path.open("w", encoding="utf-8", errors="replace")
    process = subprocess.Popen(
        command,
        cwd=cwd,
        env=environment,
        stdout=handle,
        stderr=subprocess.STDOUT,
        creationflags=CREATE_NO_WINDOW,
    )
    return process, handle


def find_starcraft_pid(launcher: subprocess.Popen) -> int:
    command = (
        f"(Get-CimInstance Win32_Process -Filter \"ParentProcessId={launcher.pid}\" "
        "| Where-Object Name -eq 'StarCraft.exe' | Select-Object -First 1 -ExpandProperty ProcessId)"
    )
    for _ in range(200):
        if launcher.poll() is not None:
            raise NativeGameError(f"bwheadless exited before StarCraft started ({launcher.returncode})")
        output = subprocess.run(
            ["powershell", "-NoProfile", "-Command", command],
            capture_output=True,
            text=True,
            creationflags=CREATE_NO_WINDOW,
        ).stdout.strip()
        if output:
            return int(output)
        time.sleep(0.1)
    raise NativeGameError("could not find the StarCraft child process")


def wait_for_host(log_path: Path, process: subprocess.Popen) -> None:
    for _ in range(300):
        if process.poll() is not None:
            raise NativeGameError(f"host exited before creating the game ({process.returncode})")
        if log_path.exists() and "game hosted" in log_path.read_text(errors="replace"):
            return
        time.sleep(0.1)
    raise NativeGameError("host did not create the game within 30 seconds")


def wait_for_java(log_path: Path, process: subprocess.Popen) -> None:
    for _ in range(600):
        if process.poll() is not None:
            raise NativeGameError(f"Java bot exited before connecting ({process.returncode})")
        if log_path.exists():
            text = log_path.read_text(errors="replace")
            if "Connection successful" in text or "Connected to StarCraft with BWAPI" in text:
                return
        time.sleep(0.1)
    raise NativeGameError("Java bot did not connect to BWAPI within 60 seconds")


def player_environment(player: dict, args: argparse.Namespace) -> dict[str, str]:
    environment = os.environ.copy()
    environment.update(
        {
            "PLAYER_NAME": player["name"],
            "PLAYER_RACE": player["race"],
            "NTH_PLAYER": str(player["nth_player"]),
            "NUM_PLAYERS": "2",
            "GAME_NAME": "GAME_" + args.game_name,
            "GAME_TYPE": args.game_type,
            "TM_LOG_RESULTS": str(player["logs"] / "scores.json"),
            "TM_LOG_FRAMETIMES": str(player["logs"] / "frames.csv"),
            "TM_LOG_UNIT_EVENTS": str(player["logs"] / "unit_events.csv"),
            "TM_SPEED_OVERRIDE": str(args.game_speed),
            "TM_SEED_OVERRIDE": "" if args.seed_override is None else str(args.seed_override),
            "TM_ALLOW_USER_INPUT": "1" if args.allow_input else "0",
            "TM_TIME_OUT_AT_FRAME": str(args.timeout_at_frame or -1),
        }
    )
    return environment


def bwheadless_command(player: dict, host: bool, game_name: str) -> list[str]:
    display_name = player["display_name"]
    command = [
        str(BWHEADLESS),
        "--host" if host else "--join",
        "--name",
        display_name,
        "--race",
        RACE_NAMES[player["race"]],
        "--dll",
        str(player["install"] / "bwapi-data" / "BWAPI.dll"),
        "--installpath",
        str(player["install"]),
        "--exe",
        str(player["install"] / "StarCraft.exe"),
        "--game",
        game_name,
        "--lan",
    ]
    if host:
        command.extend(["--map", str(player["map"])])
    return command


def launch_java(player: dict, starcraft_pid: int, environment: dict[str, str]) -> tuple[subprocess.Popen, object]:
    ai_dir = player["install"] / "bwapi-data" / "AI"
    bundled_java = ai_dir / "jre" / "bin" / "java.exe"
    if bundled_java.is_file():
        java = bundled_java
    else:
        java_home = os.environ.get("JAVA_HOME")
        java = Path(java_home) / "bin" / "java.exe" if java_home else Path("java.exe")
    options = shlex.split(player["metadata"].get("javaOpts", ""), posix=False)
    if bundled_java.is_file():
        options.extend(["--add-opens=java.base/java.nio=ALL-UNNAMED"])
    command = [
        str(java),
        f"-Dbwapi.serverProcessId={starcraft_pid}",
        *options,
        "-jar",
        str(ai_dir / player["binary_name"]),
    ]
    return launch(player["logs"] / "bot.log", command, player["install"], environment)


def terminate_tree(process: subprocess.Popen) -> None:
    if process.poll() is None:
        subprocess.run(
            ["taskkill", "/PID", str(process.pid), "/T", "/F"],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
            creationflags=CREATE_NO_WINDOW,
        )


def archive_native_logs(player: dict) -> None:
    for name in ("pluto.log", "pluto_infer.log", "wmode.ini", "__log.txt"):
        source = player["install"] / name
        if source.is_file():
            shutil.copy2(source, player["logs"] / name)


def clear_shared_replays() -> None:
    replay_dirs = [TEMPLATE_DIR / "maps" / "replays"]
    replay_dirs.extend((NATIVE_ROOT / "players").glob("player_*/maps/replays"))
    for replay_dir in replay_dirs:
        replay_dir.mkdir(parents=True, exist_ok=True)
        for path in replay_dir.glob("LastReplay*.rep"):
            path.unlink()


def archive_replays(players: list[dict]) -> None:
    missing = []
    for player in players:
        candidates = sorted(player["write"].glob("native-game*.rep"))
        if candidates:
            shutil.move(str(candidates[0]), player["replay"])
        else:
            missing.append(player)
    for player in missing:
        local_replays = sorted((player["install"] / "maps" / "replays").glob("LastReplay*.rep"))
        if local_replays:
            shutil.copy2(local_replays[0], player["replay"])
    for player in players:
        for path in (player["install"] / "maps" / "replays").glob("LastReplay*.rep"):
            path.unlink()


def copy_learning(players: list[dict]) -> None:
    for player in players:
        destination = player["source"] / "read"
        destination.mkdir(parents=True, exist_ok=True)
        for source in player["write"].iterdir():
            target = destination / source.name
            if source.is_dir():
                shutil.copytree(source, target, dirs_exist_ok=True)
            else:
                shutil.copy2(source, target)


def synthesize_scores(players: list[dict], realtime_outed: bool, gametime_outed: bool) -> None:
    purple_text = ""
    pluto_text = ""
    for player in players:
        if player["directory_name"].lower() == "pluto":
            log_path = player["logs"] / "pluto.log"
            if log_path.is_file():
                pluto_text = log_path.read_text(errors="replace")
        else:
            log_path = player["logs"] / "bot.log"
            if log_path.is_file():
                purple_text = log_path.read_text(errors="replace")
    purple_outcomes = re.findall(r"Game ended in (victory|defeat)", purple_text)
    pluto_outcomes = re.findall(r"onEnd \(isWinner=([01])\)", pluto_text)
    purple_won = purple_outcomes[-1] == "victory" if purple_outcomes else None
    pluto_won = pluto_outcomes[-1] == "1" if pluto_outcomes else None
    if purple_won is None and pluto_won is not None:
        purple_won = not pluto_won
    if pluto_won is None and purple_won is not None:
        pluto_won = not purple_won
    valid_outcome = purple_won is not None and pluto_won is not None and purple_won != pluto_won
    crashed = not realtime_outed and not gametime_outed and not valid_outcome
    for player in players:
        is_pluto = player["directory_name"].lower() == "pluto"
        winner = pluto_won if is_pluto else purple_won
        score = {
            "is_winner": bool(winner) if valid_outcome else False,
            "is_crashed": crashed,
            "is_nostart": False,
            "timed_out": gametime_outed,
            "building_score": 0,
            "kill_score": 0,
            "razing_score": 0,
            "unit_score": 0,
        }
        (player["logs"] / "scores.json").write_text(json.dumps(score, indent="\t") + "\n", encoding="utf-8")
        for name in ("frames.csv", "unit_events.csv"):
            path = player["logs"] / name
            if not path.exists():
                path.touch()


def last_purple_frame(players: list[dict]) -> int | None:
    for player in players:
        if player["directory_name"].lower().startswith("purple"):
            log_path = player["logs"] / "bot.log"
            if log_path.is_file():
                frames = [int(value) for value in re.findall(r"(?m)^(\d+) \|", log_path.read_text(errors="replace"))]
                return max(frames) if frames else None
    return None


def game_has_started(players: list[dict]) -> bool:
    return all(
        (player["logs"] / "game.log").is_file()
        and "game start!" in (player["logs"] / "game.log").read_text(errors="replace")
        for player in players
    )


def lock_owner_is_active(lock_path: Path) -> bool:
    try:
        owner = int(lock_path.read_text(errors="replace").strip())
    except (OSError, ValueError):
        return False
    command = (
        f"$process = Get-CimInstance Win32_Process -Filter \"ProcessId={owner}\"; "
        "if ($process) { $process.CommandLine }"
    )
    completed = subprocess.run(
        ["powershell", "-NoProfile", "-Command", command],
        capture_output=True,
        text=True,
        creationflags=CREATE_NO_WINDOW,
    )
    return completed.returncode == 0 and "scbw_play_pluto.py" in completed.stdout


def acquire_lock(lock_path: Path) -> int:
    for _ in range(2):
        try:
            lock_fd = os.open(lock_path, os.O_CREAT | os.O_EXCL | os.O_WRONLY)
            os.write(lock_fd, str(os.getpid()).encode("ascii"))
            return lock_fd
        except FileExistsError as exc:
            if lock_owner_is_active(lock_path):
                owner = lock_path.read_text(errors="replace").strip() or "unknown"
                raise NativeGameError(f"another native Pluto game owns {lock_path} (PID {owner})") from exc
            lock_path.unlink(missing_ok=True)
    raise NativeGameError(f"could not acquire native Pluto game lock: {lock_path}")


def read_scores(players: list[dict]) -> tuple[bool, int | None, bool, bool]:
    scores = []
    for player in players:
        score_path = player["logs"] / "scores.json"
        if not score_path.is_file():
            return False, None, True, False
        try:
            scores.append(json.loads(score_path.read_text(encoding="utf-8")))
        except (json.JSONDecodeError, OSError):
            return False, None, True, False
    crashed = any(score.get("is_crashed", False) for score in scores)
    timed_out = any(score.get("timed_out", False) for score in scores)
    winners = [index for index, score in enumerate(scores) if score.get("is_winner", False)]
    valid = not crashed and not timed_out and len(winners) == 1
    invalid_outcome_crash = not timed_out and len(winners) != 1
    return valid, winners[0] if valid else None, crashed or invalid_outcome_crash, timed_out


def cleanup_runtime(players: list[dict]) -> None:
    for player in players:
        data = player["install"] / "bwapi-data"
        for junction in (data / "write", data / "BWTA", data / "BWTA2", player["install"] / "Errors"):
            remove_junction(junction)


def run_game(args: argparse.Namespace) -> int:
    validate_installation()
    if re.fullmatch(r"[A-Za-z0-9_.-]+", args.game_name) is None:
        raise NativeGameError("--game_name may contain only letters, digits, dot, underscore, and hyphen")
    game_dir = Path(args.game_dir)
    bot_dir = Path(args.bot_dir)
    map_dir = Path(args.map_dir)
    for writable in (game_dir, Path(args.bwapi_data_bwta_dir), Path(args.bwapi_data_bwta2_dir), NATIVE_ROOT):
        if not path_is_within(writable, SCBW_ROOT):
            raise NativeGameError(f"native game writes must stay under {SCBW_ROOT}: {writable}")
    map_relative = Path(args.map)
    if map_relative.is_absolute() or ".." in map_relative.parts:
        raise NativeGameError("--map must be relative to --map_dir")
    map_source = map_dir / map_relative
    if not map_source.is_file():
        raise NativeGameError(f"map does not exist: {map_source}")

    players = [parse_bot(spec, bot_dir) for spec in args.bots]
    if sum(player["directory_name"].lower() == "pluto" for player in players) != 1:
        raise NativeGameError("exactly one player must be the Pluto bot directory")
    if not any(player["directory_name"].lower().startswith("purple") for player in players):
        raise NativeGameError("the other player must be a PurpleWave bot directory")
    if not players[0]["directory_name"].lower().startswith("purple"):
        raise NativeGameError("the PurpleWave bot must be listed first so its client can connect before game start")

    game_name = "GAME_" + args.game_name
    result_dir = game_dir / game_name
    runtime = NATIVE_ROOT / "players"
    if result_dir.exists():
        answer = input(f"Game {game_name} already exists; replace it? (Y/n) ")
        if answer.lower() not in ("", "y", "yes"):
            return 1
        shutil.rmtree(result_dir)
    result_dir.mkdir(parents=True)
    runtime.mkdir(parents=True, exist_ok=True)

    staged = []
    processes: list[subprocess.Popen] = []
    handles = []
    realtime_outed = False
    gametime_outed = False
    started = time.time()
    overall_deadline = None if args.timeout is None else started + args.timeout
    startup_deadline = started + 120
    if overall_deadline is not None:
        startup_deadline = min(startup_deadline, overall_deadline)
    try:
        clear_shared_replays()
        for index, player in enumerate(players):
            staged_player = stage_player(player, index, runtime, result_dir, map_source, args)
            if args.random_names:
                staged_player["display_name"] = random_name()
            elif args.hide_names:
                staged_player["display_name"] = "player"
            else:
                staged_player["display_name"] = player["name"]
            staged.append(staged_player)

        game_log_0 = staged[0]["logs"] / "game.log"
        env_0 = player_environment(staged[0], args)
        host, host_handle = launch(
            game_log_0,
            bwheadless_command(staged[0], True, game_name),
            staged[0]["install"],
            env_0,
        )
        processes.append(host)
        handles.append(host_handle)
        wait_for_host(game_log_0, host)
        pid_0 = find_starcraft_pid(host)
        if staged[0]["type"] == "jar":
            bot, bot_handle = launch_java(staged[0], pid_0, env_0)
            processes.append(bot)
            handles.append(bot_handle)
            wait_for_java(staged[0]["logs"] / "bot.log", bot)
        else:
            (staged[0]["logs"] / "bot.log").write_text("Module bot started by BWAPI\n", encoding="utf-8")

        env_1 = player_environment(staged[1], args)
        join, join_handle = launch(
            staged[1]["logs"] / "game.log",
            bwheadless_command(staged[1], False, game_name),
            staged[1]["install"],
            env_1,
        )
        processes.append(join)
        handles.append(join_handle)
        pid_1 = find_starcraft_pid(join)
        if staged[1]["type"] == "jar":
            bot, bot_handle = launch_java(staged[1], pid_1, env_1)
            processes.append(bot)
            handles.append(bot_handle)
        else:
            (staged[1]["logs"] / "bot.log").write_text("Module bot started by BWAPI\n", encoding="utf-8")

        started_game = False
        while host.poll() is None or join.poll() is None:
            now = time.time()
            if not started_game:
                started_game = game_has_started(staged)
                if not started_game and now >= startup_deadline:
                    realtime_outed = True
                    break
            if overall_deadline is not None and now >= overall_deadline:
                realtime_outed = True
                break
            if args.timeout_at_frame is not None:
                frame = last_purple_frame(staged)
                if frame is not None and frame >= args.timeout_at_frame:
                    gametime_outed = True
                    break
            time.sleep(1)
    finally:
        for process in processes:
            terminate_tree(process)
        for handle in handles:
            handle.close()
        for player in staged:
            archive_native_logs(player)
        archive_replays(staged)

    synthesize_scores(staged, realtime_outed, gametime_outed)
    valid, winner_index, crashed, gametime_outed = read_scores(staged)
    if realtime_outed:
        valid = False
    if args.read_overwrite and valid:
        copy_learning(staged)
    elapsed = time.time() - started
    info = {
        "headless": True,
        "game_name": game_name,
        "map_name": args.map,
        "game_type": args.game_type,
        "game_speed": args.game_speed,
        "seed_override": "" if args.seed_override is None else str(args.seed_override),
        "timeout": args.timeout,
        "timeout_at_frame": args.timeout_at_frame,
        "hide_names": args.hide_names,
        "drop_players": False,
        "allow_input": args.allow_input,
        "auto_launch": False,
        "random_names": args.random_names,
        "game_dir": game_dir.as_posix(),
        "bot_dir": bot_dir.as_posix(),
        "map_dir": map_dir.as_posix(),
        "bwapi_data_bwta_dir": Path(args.bwapi_data_bwta_dir).as_posix(),
        "bwapi_data_bwta2_dir": Path(args.bwapi_data_bwta2_dir).as_posix(),
        "vnc_base_port": args.vnc_base_port,
        "vnc_host": args.vnc_host,
        "capture_movement": False,
        "docker_image": "native-pluto",
        "nano_cpus": None,
        "mem_limit": None,
        "read_overwrite": args.read_overwrite,
        "bots": args.bots,
        "is_crashed": crashed,
        "is_gametime_outed": gametime_outed,
        "is_realtime_outed": realtime_outed,
        "game_time": elapsed,
        "winner": staged[winner_index]["name"] if valid else None,
        "loser": staged[1 - winner_index]["name"] if valid else None,
        "winner_race": staged[winner_index]["race"] if valid else None,
        "loser_race": staged[1 - winner_index]["race"] if valid else None,
    }
    (result_dir / "result.json").write_text(json.dumps(info) + "\n", encoding="utf-8")
    if not args.keep_runtime:
        cleanup_runtime(staged)

    print(winner_index if valid else "")
    print(f"Game results: {result_dir}", file=sys.stderr)
    return 0 if valid else 1


def main() -> int:
    args = parse_args()
    lock_path = NATIVE_ROOT / "active-game.lock"
    lock_fd = None
    try:
        NATIVE_ROOT.mkdir(parents=True, exist_ok=True)
        lock_fd = acquire_lock(lock_path)
        return run_game(args)
    except (NativeGameError, OSError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1
    finally:
        if lock_fd is not None:
            os.close(lock_fd)
            try:
                if lock_path.read_text(errors="replace").strip() == str(os.getpid()):
                    lock_path.unlink(missing_ok=True)
            except OSError:
                pass


if __name__ == "__main__":
    raise SystemExit(main())
