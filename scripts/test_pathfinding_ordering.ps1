param(
    [string]$Java = 'java',
    [string]$ScalaRepository = (Join-Path $env:USERPROFILE '.m2/repository/org/scala-lang'),
    [string]$BotJar = (Join-Path $PSScriptRoot '../target/PurpleWave-1.0.jar')
)

$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
$compiler = @('scala-compiler', 'scala-library', 'scala-reflect') | ForEach-Object {
    Join-Path $ScalaRepository "$_/2.12.18/$_-2.12.18.jar"
}
$output = Join-Path $repo 'target/pathfinding-ordering-tests'
New-Item -ItemType Directory -Path $output -Force | Out-Null
$classpath = (@($BotJar) + $compiler) -join ';'
& $Java -cp $classpath scala.tools.nsc.Main -usejavacp -d $output `
    (Join-Path $repo 'src/Information/Geography/Pathfinding/TileState.scala') `
    (Join-Path $repo 'tests/Information/Geography/PathfindingOrderingTest.scala')
if ($LASTEXITCODE -ne 0) { throw 'Pathfinding ordering test compilation failed.' }
& $Java -cp "$output;$BotJar" Information.Geography.Pathfinding.PathfindingOrderingTest
if ($LASTEXITCODE -ne 0) { throw 'Pathfinding ordering tests failed.' }
