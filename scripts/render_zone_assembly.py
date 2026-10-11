"""Validate real-game zone snapshots and render every extended main before/after."""
import argparse
from collections import Counter, defaultdict
from dataclasses import dataclass
import html
import json
from pathlib import Path
import re

from PIL import Image, ImageDraw, ImageFont


@dataclass
class Snapshot:
    name: str
    digest: str
    width: int
    height: int
    rows: dict
    tiles: dict
    bases: dict
    owner: dict
    path: Path


def read(path):
    rows = defaultdict(list)
    for line in path.read_text().splitlines():
        row = line.split('\t')
        rows[row[0]].append(row[1:])
    if not rows['PASS']:
        return None
    name, digest, width, height = rows['MAP'][0]
    tiles = {(int(t[0]), int(t[1])): (int(t[2]), int(t[3]), t[4] == 'true', t[5] == 'true', int(t[6]))
             for t in rows['TILE']}
    bases = {(int(b[0]), int(b[1])): (int(b[2]), b[3] == 'true', (int(b[4]), int(b[5]))) for b in rows['BASE']}
    owner = {}
    for region, zone, *_ in tiles.values():
        assert owner.get(region, zone) == zone, f'{name}: split BWEM region {region}'
        owner[region] = zone
    return Snapshot(name, digest, int(width), int(height), rows, tiles, bases, owner, path)


def load(directory):
    maps = {}
    for path in sorted(directory.rglob('zone-assembly.tsv')):
        snapshot = read(path)
        if snapshot:
            maps[snapshot.digest] = snapshot
    return maps


def validate(before, after):
    name = before.name
    assert before.rows['REGION'] == after.rows['REGION'], f'{name}: BWEM regions changed'
    # BWEM may choose a different mineral in a blocking stack as a zero-width
    # pseudo-choke's point. Its endpoints/topology still have to be identical.
    def choke_key(row):
        return tuple(row[:3]) + (tuple(row[3:]) if float(row[2]) > 0 else ())
    assert sorted(map(choke_key, before.rows['CHOKE'])) == sorted(map(choke_key, after.rows['CHOKE'])), f'{name}: BWEM chokes changed'
    assert before.bases.keys() == after.bases.keys(), f'{name}: base locations changed'
    assert before.tiles.keys() == after.tiles.keys(), f'{name}: missing tiles'
    contraction = defaultdict(set)
    for xy, old in before.tiles.items():
        new = after.tiles[xy]
        assert old[0] == new[0] and old[2:] == new[2:], f'{name}: terrain changed'
        contraction[old[1]].add(new[1])
    assert all(len(zones) == 1 for zones in contraction.values()), f'{name}: split original zone'
    for xy, (_, main, _) in before.bases.items():
        assert after.bases[xy][1] == main, f'{name}: main identity changed'
        assert after.bases[xy][2] == before.bases[xy][2], f'{name}: natural association changed'
    expected_edges = {}
    expected_owners = Counter()
    for choke in after.rows['CHOKE']:
        a, b = (after.owner[int(r)] for r in choke[:2])
        if a != b:
            expected_owners[a, b] += 1
            # Edge expands point-like mineral-blocked chokes to terrain cross-sections.
            # Their owner pairs/counts must match; real chokes also retain exact geometry.
            if float(choke[2]) > 0:
                expected_edges[tuple(map(int, choke[3:]))] = (a, b)
    actual_edges = {tuple(map(int, e[2:])): tuple(map(int, e[:2])) for e in after.rows['EDGE']}
    actual_owners = Counter(tuple(map(int, e[:2])) for e in after.rows['EDGE'])
    assert expected_owners == actual_owners, f'{name}: external edge ownership mismatch'
    assert all(actual_edges.get(geometry) == owners for geometry, owners in expected_edges.items()), f'{name}: external choke geometry changed'
    changes = []
    for xy, (old_zone, is_main, _) in sorted(before.bases.items()):
        if not is_main:
            continue
        new_zone = after.bases[xy][0]
        old_tiles = {t for t, data in before.tiles.items() if data[1] == old_zone}
        new_tiles = {t for t, data in after.tiles.items() if data[1] == new_zone}
        assert old_tiles <= new_tiles, f'{name}: main lost tiles'
        added = new_tiles - old_tiles
        if added:
            added_regions = sorted({before.tiles[t][0] for t in added})
            absorbed_bases = [t for t in before.bases if t in added]
            assert all(t == before.bases[xy][2] for t in absorbed_bases), f'{name}: absorbed a base other than the natural'
            changes.append({'main': xy, 'before_tiles': len(old_tiles), 'after_tiles': len(new_tiles),
                            'added_tiles': len(added), 'added_regions': added_regions, 'absorbed_bases': absorbed_bases})
    main_zones = {zone for zone, is_main, _ in after.bases.values() if is_main}
    merged = defaultdict(list)
    for old_zone, new_zones in contraction.items():
        merged[next(iter(new_zones))].append(old_zone)
    assert all(len(old) == 1 or new in main_zones for new, old in merged.items()), f'{name}: merged a non-main zone'
    return changes


def font(size, bold=False):
    return ImageFont.truetype('C:/Windows/Fonts/' + ('arialbd.ttf' if bold else 'arial.ttf'), size)


MAIN = (141, 104, 218)
ANNEX = [(57, 184, 177), (239, 171, 78), (97, 167, 226)]
BG = (17, 23, 34)


def render(before, after, change, destination):
    xy = tuple(change['main'])
    old_zone, _, natural = before.bases[xy]
    new_zone = after.bases[xy][0]
    added_regions = change['added_regions']
    new_tiles = [t for t, data in after.tiles.items() if data[1] == new_zone]
    left = max(0, min(t[0] for t in new_tiles) - 9)
    top = max(0, min(t[1] for t in new_tiles) - 9)
    right = min(before.width, max(t[0] for t in new_tiles) + 10)
    bottom = min(before.height, max(t[1] for t in new_tiles) + 10)
    scale = min(9, 500 // max(right-left, bottom-top))
    panel_width = (right-left) * scale
    panel_height = (bottom-top) * scale
    width = max(880, panel_width * 2 + 72)
    img = Image.new('RGB', (width, panel_height + 170), BG)
    draw = ImageDraw.Draw(img)
    draw.text((24, 15), f'Main ({xy[0]}, {xy[1]})', font=font(24, True), fill='white')
    draw.text((24, 46), f'+{change["added_tiles"]:,} tiles  |  {len(added_regions)} adjoining BWEM area(s)',
              font=font(17), fill='#b9c5d6')
    offsets = [(24, 101), (width // 2 + 12, 101)]
    for index, (snapshot, offset) in enumerate(zip([before, after], offsets)):
        ox, oy = offset
        count = change['before_tiles'] if index == 0 else change['after_tiles']
        draw.text((ox, 77), f'{"BEFORE" if index == 0 else "AFTER"}  /  {count:,} tiles', font=font(17, True), fill='white')
        for y in range(top, bottom):
            for x in range(left, right):
                region, zone, walkable, buildable, _ = snapshot.tiles[x, y]
                color = (66, 74, 85)
                if before.tiles[x, y][1] == old_zone or (index == 1 and zone == new_zone):
                    color = MAIN
                elif region in added_regions:
                    color = ANNEX[added_regions.index(region) % len(ANNEX)]
                shade = 1.0 if buildable else .76 if walkable else .27
                color = tuple(int(c * shade) for c in color)
                px, py = ox + (x-left) * scale, oy + (y-top) * scale
                draw.rectangle((px, py, px+scale-1, py+scale-1), fill=color)
        # Clip choke geometry to the panel by drawing it on a temporary layer.
        layer = img.crop((ox, oy, ox+panel_width, oy+panel_height))
        ld = ImageDraw.Draw(layer)
        for edge in snapshot.rows['EDGE']:
            x1, y1, x2, y2 = map(int, edge[2:])
            ld.line(((x1/32-left)*scale, (y1/32-top)*scale,
                     (x2/32-left)*scale, (y2/32-top)*scale), fill='#f4f5f7', width=2)
        for base_xy, (_, main, _) in snapshot.bases.items():
            x, y = base_xy
            if left <= x and x+4 <= right and top <= y and y+3 <= bottom:
                px, py = (x-left)*scale, (y-top)*scale
                ld.rectangle((px, py, px+4*scale, py+3*scale), outline='#ffec9c' if base_xy == xy else '#c7d1e1', width=2)
                label = 'M' if base_xy == xy else 'N' if base_xy == natural else 'B'
                ld.text((px+2, py+2), label, font=font(13, True), fill='white', stroke_width=1, stroke_fill='black')
        img.paste(layer, (ox, oy))
    y = panel_height + 124
    draw.rectangle((24, y, 38, y+14), fill=MAIN)
    draw.text((45, y-2), 'Main zone', font=font(15), fill='#d7dfed')
    draw.rectangle((160, y, 174, y+14), fill=ANNEX[0])
    if len(added_regions) > 1:
        draw.rectangle((177, y, 191, y+14), fill=ANNEX[1])
    draw.text((201 if len(added_regions) > 1 else 181, y-2), 'Absorbed area(s)', font=font(15), fill='#d7dfed')
    draw.text((360, y-2), 'White lines: zone edges   M: main   N: natural   B: other base', font=font(14), fill='#d7dfed')
    img.save(destination)
    return img


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--before', type=Path, required=True)
    parser.add_argument('--after', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--allow-partial', action='store_true')
    args = parser.parse_args()
    before, after = load(args.before), load(args.after)
    if not args.allow_partial:
        assert before.keys() == after.keys(), 'Before/after map coverage differs'
    args.output.mkdir(parents=True, exist_ok=True)
    report, cards = [], []
    for digest in sorted(before.keys() & after.keys(), key=lambda d: before[d].name):
        a, b = before[digest], after[digest]
        changes = validate(a, b)
        record = {'map': a.name, 'hash': digest, 'mains': sum(v[1] for v in a.bases.values()),
                  'before_zones': len(a.rows['ZONE']), 'after_zones': len(b.rows['ZONE']),
                  'before_edges': len(a.rows['EDGE']), 'after_edges': len(b.rows['EDGE']), 'changes': changes,
                  'before_snapshot': str(a.path), 'after_snapshot': str(b.path)}
        report.append(record)
        if changes:
            slug = re.sub('[^a-z0-9]+', '-', a.name.lower()).strip('-')
            images = []
            for change in changes:
                filename = f'{slug}-{change["main"][0]}-{change["main"][1]}.png'
                change['diagram'] = filename
                images.append(render(a, b, change, args.output / filename))
            columns = 2 if len(images) > 2 else 1
            cell_width, cell_height = max(i.width for i in images), max(i.height for i in images)
            sheet = Image.new('RGB', (cell_width*columns, 68 + cell_height*((len(images)+columns-1)//columns)), BG)
            ImageDraw.Draw(sheet).text((24, 20), a.name, font=font(29, True), fill='white')
            for i, image in enumerate(images):
                sheet.paste(image, ((i % columns)*cell_width, 68+(i//columns)*cell_height))
            filename = slug + '.png'
            sheet.save(args.output / filename)
            record['overview'] = filename
            cards.append(f'<h2>{html.escape(a.name)}</h2><a href="{filename}"><img src="{filename}" loading="lazy"></a>')
    count = sum(len(r['changes']) for r in report)
    summary = f'{len(report)} maps / {sum(r["mains"] for r in report)} mains checked; {count} main zones extended on {sum(bool(r["changes"]) for r in report)} maps.'
    rows = ''.join(f'<tr><td>{html.escape(r["map"])}</td><td>{r["mains"]}</td><td>{len(r["changes"])}</td><td>{r["before_zones"]} &rarr; {r["after_zones"]}</td><td>{r["before_edges"]} &rarr; {r["after_edges"]}</td></tr>' for r in report)
    document = ('<!doctype html><meta charset="utf-8"><title>Main zone assembly comparison</title>'
                '<style>body{margin:36px auto;max-width:1500px;padding:0 24px;background:#111722;color:#dbe4f0;font:16px system-ui}'
                'h1,h2{color:white}img{width:100%;height:auto}table{border-collapse:collapse}td,th{padding:7px 16px;text-align:left;border-bottom:1px solid #354052}</style>'
                '<h1>Main zone assembly &mdash; before / after</h1><p>' + summary + '</p>'
                '<p>Rule: absorb base-free areas at the main terrain level when they are enclosed pockets, '
                'or unbranched passages joined by a seam at least eight build tiles wide. '
                'An expansion may also join when it is enclosed and its join is at least eight tiles wide. '
                'Keep narrow expansion chokes, shared approaches, junctions and terrain-level boundaries separate. '
                'Whole BWEM tile sets are united; internal chokes disappear and external chokes retain their geometry.</p>'
                '<p>Purple is the main zone. Teal and amber identify absorbed BWEM areas in the before view. '
                'The after view colors their complete tile sets purple and removes internal edges. Dark cells show unwalkable terrain.</p>'
                + ''.join(cards) + '<h2>Map coverage</h2><table><tr><th>Map</th><th>Mains</th><th>Extended</th><th>Zones</th><th>Edges</th></tr>' + rows + '</table>')
    if (args.output / 'validation.json').exists():
        document += '<p><a href="validation.json" style="color:#ac95ef">Validation details and baseline repeat notes</a></p>'
    (args.output / 'index.html').write_text(document, encoding='utf-8')
    (args.output / 'report.json').write_text(json.dumps(report, indent=2), encoding='utf-8')
    print(summary)


if __name__ == '__main__':
    main()
