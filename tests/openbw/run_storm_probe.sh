#!/usr/bin/env bash
set -euo pipefail
if [[ $# -ne 5 ]]; then
  echo "usage: run_storm_probe.sh BWAPI_SOURCE OPENBW_SOURCE BUILD_DIR MPQ_DIR MAP" >&2
  exit 2
fi
bwapi=$(realpath "$1")
openbw=$(realpath "$2")
mkdir -p "$3"
build=$(realpath "$3")
mpqs=$(realpath "$4")
map=$(realpath "$5")
probe_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
cmake -S "$bwapi" -B "$build" -DCMAKE_BUILD_TYPE=Release -DOPENBW_DIR="$openbw" -DOPENBW_ENABLE_UI=OFF
cmake --build "$build" --target BWAPILauncher -j4
g++ -std=c++14 -O2 \
  -I"$bwapi/bwapi/include" -I"$bwapi/bwapi/BWAPI/Source" -I"$bwapi/bwapi/OpenBWData" \
  -I"$bwapi/bwapi/Util/Source" -I"$bwapi/bwapi/BWAPICore" \
  "$probe_dir/StormBulletProbe.cpp" -L"$build/lib" -Wl,-rpath,"$build/lib" \
  -lBWAPI -lBWAPILIB -lOpenBWData -pthread -o "$build/StormBulletProbe"
mkdir -p "$build/storm-runtime/bwapi-data"
cd "$build/storm-runtime"
for name in STARDAT.MPQ BROODAT.MPQ patch_rt.mpq; do
  ln -sfn "$mpqs/$name" "$name"
done
for mode in normal orphan enemy-hidden-caster hidden complete; do
  timeout 60 "$build/StormBulletProbe" "$map" "$mode"
done
