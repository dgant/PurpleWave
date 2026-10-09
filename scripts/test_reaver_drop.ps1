param(
    [string]$Java = 'java',
    [string]$ScalaRepository = (Join-Path $env:USERPROFILE '.m2/repository/org/scala-lang')
)

$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
$scalaVersion = '2.12.18'
$compiler = @('scala-compiler', 'scala-library', 'scala-reflect') | ForEach-Object {
    Join-Path $ScalaRepository "$_/$scalaVersion/$_-$scalaVersion.jar"
}
$classes = Join-Path $repo 'target/classes'
$macros = Join-Path $repo 'target/PurpleWave-macros.jar'
foreach ($path in @($classes, $macros) + $compiler) {
    if (!(Test-Path -LiteralPath $path)) { throw "Missing dependency: $path. Run Maven compile first." }
}
$output = Join-Path $repo 'target/reaver-drop-tests'
New-Item -ItemType Directory -Path $output -Force | Out-Null
$classpath = (@($classes, $macros) + $compiler) -join ';'
$sources = @(
    'src/ProxyBwapi/UnitInfo/UnitInfo.scala',
    'src/Planning/Blackboard.scala',
    'src/Tactic/Missions/DropManifest.scala',
    'src/Tactic/Missions/DropRoute.scala',
    'src/Tactic/Missions/MissionDrop.scala',
    'src/Tactic/Missions/MissionReaverDrop.scala',
    'src/Micro/Actions/Protoss/Shuttle/ShuttleAdoptPassenger.scala',
    'src/Micro/Agency/Commander.scala',
    'tests/Tactic/ReaverDropTest.scala'
) | ForEach-Object { Join-Path $repo $_ }
& $Java -cp $classpath scala.tools.nsc.Main -usejavacp -d $output $sources
if ($LASTEXITCODE -ne 0) { throw 'Reaver drop compilation failed.' }
& $Java -cp "$output;$classpath" Tactic.Missions.ReaverDropTest
if ($LASTEXITCODE -ne 0) { throw 'Reaver drop tests failed.' }
