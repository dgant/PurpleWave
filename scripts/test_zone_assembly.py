"""Run frame-limited zone diagnostics using PurpleBench's SC-Docker workflow.

Compile WriteZoneAssemblySnapshot.scala (and the implementation under test) into
--classes first. Only the isolated diagnostic jar replaces WriteMapCSV.
"""
import argparse
from concurrent.futures import ThreadPoolExecutor
import csv
import hashlib
import json
import re
from pathlib import Path
import shutil
import subprocess
import zipfile


def game_passed(result, snapshot, frames):
    if not snapshot or not snapshot.exists() or '\nPASS\t' not in snapshot.read_text():
        return False
    result_file = result / 'result.json'
    if not result_file.exists():
        return False
    game = json.loads(result_file.read_text())
    if game.get('is_realtime_outed') or game.get('is_crashed'):
        return False
    for player in range(2):
        scores_file = result / f'logs_{player}/scores.json'
        if not scores_file.exists() or not (result / f'player_{player}.rep').exists():
            return False
        scores = json.loads(scores_file.read_text())
        if scores.get('is_crashed') or scores.get('is_nostart'):
            return False
    with (result / 'logs_0/frames.csv').open() as stream:
        observed = max((int(r['frame_count']) for r in csv.DictReader(stream)), default=0)
    # SC-Docker returns exit 1 for the intentional frame limit as well as actual failures.
    return observed >= frames


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--phase', required=True)
    parser.add_argument('--classes', type=Path, required=True)
    parser.add_argument('--scbw-root', type=Path, default=Path('E:/scbw'))
    parser.add_argument('--jar', type=Path, default=Path('target/PurpleWave-1.0.jar'))
    parser.add_argument('--runtime-bot', default='PurpleProtoss')
    parser.add_argument('--workers', type=int, default=3)
    parser.add_argument('--frames', type=int, default=96)
    parser.add_argument('--seed', type=int, default=42)
    parser.add_argument('--filter', default='', help='Optional map-name regular expression')
    args = parser.parse_args()
    repo = Path(__file__).resolve().parents[1]
    output = repo / 'target/zone-assembly' / args.phase
    bots = output / 'bots'
    bot_name = 'PurpleZoneAssembly'
    bot = bots / bot_name
    ai = bot / 'AI'
    ai.mkdir(parents=True, exist_ok=True)
    (bot / 'read').mkdir(exist_ok=True)
    source = args.scbw_root / 'bots' / args.runtime_bot
    for name in ['jre', 'run32.bat', 'run64.bat', 'PurpleWaveLocalDocker.config.json']:
        path = source / 'AI' / name
        if path.is_dir():
            shutil.copytree(path, ai / name, dirs_exist_ok=True)
        elif path.is_file():
            shutil.copy2(path, ai / name)
    shutil.copy2(source / 'BWAPI.dll', bot / 'BWAPI.dll')
    metadata = {'name': bot_name, 'race': 'Protoss', 'botType': 'JAVA_MIRROR', 'wineArchitecture': 'win64'}
    (bot / 'bot.json').write_text(json.dumps(metadata))
    shutil.copytree(args.scbw_root / 'bots/UITtest', bots / 'UITtest', dirs_exist_ok=True)
    overlays = {p.relative_to(args.classes).as_posix(): p for p in args.classes.rglob('*.class')}
    build_hash = hashlib.sha256(args.jar.read_bytes())
    for name, path in sorted(overlays.items()):
        build_hash.update(name.encode())
        build_hash.update(path.read_bytes())
    signature = build_hash.hexdigest()
    with zipfile.ZipFile(args.jar) as source_jar, zipfile.ZipFile(ai / 'PurpleWave.jar', 'w', zipfile.ZIP_DEFLATED) as jar:
        for entry in source_jar.infolist():
            if entry.filename not in overlays:
                jar.writestr(entry, source_jar.read(entry))
        for name, path in overlays.items():
            jar.write(path, name)
    (output / 'sources.json').write_text(json.dumps({
        'jar': str(args.jar.resolve()), 'classes': str(args.classes.resolve()),
        'frames': args.frames, 'maps': 'aiide plus sscai/Andromeda', 'filter': args.filter,
        'signature': signature, 'seed': args.seed,
    }, indent=2))
    maps = sorted(p for p in (args.scbw_root / 'maps/aiide').iterdir() if p.suffix.lower() in ('.scx', '.scm'))
    maps.append(args.scbw_root / 'maps/sscai/(4)Andromeda.scx')
    maps = [p for p in maps if re.search(args.filter, p.name, re.IGNORECASE)]
    # SC-Docker moves replays with os.rename, so results must share the maps' drive.
    games = args.scbw_root / 'games' / ('zone-assembly-' + args.phase)
    games.mkdir(parents=True, exist_ok=True)
    saved = output / 'snapshots'
    saved.mkdir(exist_ok=True)
    launcher = shutil.which('scbw.play')
    if not launcher:
        raise RuntimeError('Install SC-Docker / scbw.play as described in PurpleBench.')

    def run(item):
        index, map_path = item
        destination = saved / map_path.stem
        receipt = destination / 'result.json'
        map_signature = hashlib.sha256((signature + str(args.seed) + hashlib.sha256(map_path.read_bytes()).hexdigest()).encode()).hexdigest()
        if receipt.exists():
            prior = json.loads(receipt.read_text())
            prior['pass'] = game_passed(Path(prior['result']), Path(prior['snapshot']) if prior['snapshot'] else None, args.frames)
            if prior['pass'] and prior.get('signature') == map_signature:
                receipt.write_text(json.dumps(prior, indent=2))
                print(f'PASS (cached) {map_path.name}', flush=True)
                return prior
        passed = False
        snapshot = None
        for retry in range(3):
            attempt = 1
            name = f'Z_{args.phase[:8]}_{index:02d}_{attempt}'
            result = games / ('GAME_' + name)
            while result.exists():
                attempt += 1
                name = f'Z_{args.phase[:8]}_{index:02d}_{attempt}'
                result = games / ('GAME_' + name)
            command = [launcher, '--bots', bot_name, 'UITtest', '--headless',
                       '--map', str(map_path.relative_to(args.scbw_root / 'maps')),
                       '--map_dir', str(args.scbw_root / 'maps'), '--bot_dir', str(bots),
                       '--game_dir', str(games), '--game_name', name, '--game_speed', '0',
                       '--seed_override', str(args.seed), '--timeout_at_frame', str(args.frames), '--timeout', '180',
                       '--bwapi_data_bwta_dir', str(args.scbw_root / 'bwapi-data/BWTA'),
                       '--bwapi_data_bwta2_dir', str(args.scbw_root / 'bwapi-data/BWTA2')]
            with (output / (name + '.log')).open('w') as log:
                completed = subprocess.run(command, stdout=log, stderr=subprocess.STDOUT)
            snapshots = list(result.rglob('zone-assembly.tsv'))
            if snapshots:
                snapshot = snapshots[0]
                passed = len(snapshots) == 1 and game_passed(result, snapshot, args.frames)
                break
            print(f'RETRY {map_path.name}: game did not initialize ({retry+1}/3)', flush=True)
        destination.mkdir(exist_ok=True)
        if snapshot:
            shutil.copy2(snapshot, destination / 'zone-assembly.tsv')
        record = {'map': map_path.name, 'pass': passed, 'snapshot': str(snapshot) if snapshot else None,
                  'result': str(result), 'exit': completed.returncode, 'signature': map_signature}
        receipt.write_text(json.dumps(record, indent=2))
        print(f'{"PASS" if passed else "FAIL"} {map_path.name}', flush=True)
        return record

    with ThreadPoolExecutor(max_workers=args.workers) as executor:
        results = list(executor.map(run, enumerate(maps)))
    (output / 'results.json').write_text(json.dumps(results, indent=2))
    print(f'{sum(r["pass"] for r in results)}/{len(results)} maps passed.', flush=True)
    if not all(r['pass'] for r in results):
        raise SystemExit(1)


if __name__ == '__main__':
    main()
