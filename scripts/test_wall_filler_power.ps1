param(
    [string]$Java = 'java',
    [string]$ScalaRepository = (Join-Path $env:USERPROFILE '.m2/repository/org/scala-lang'),
    [string]$BotJar = (Join-Path $PSScriptRoot '../target/PurpleWave-1.0.jar')
)

$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
$scalaVersion = '2.12.18'
$compiler = @('scala-compiler', 'scala-library', 'scala-reflect') | ForEach-Object {
    Join-Path $ScalaRepository "$_/$scalaVersion/$_-$scalaVersion.jar"
}
$macros = Join-Path $repo 'target/PurpleWave-macros.jar'
foreach ($jar in @($BotJar, $macros) + $compiler) {
    if (!(Test-Path -LiteralPath $jar)) { throw "Missing dependency: $jar. Build the bot first." }
}
$output = Join-Path $repo 'target/wall-filler-power-tests'
New-Item -ItemType Directory -Path $output -Force | Out-Null
$sources = @(
    (Join-Path $repo 'src/Placement/Walls/WallFinder.scala'),
    (Join-Path $repo 'tests/Placement/WallFillerPowerTest.scala')
)
& $Java -cp (($compiler + $BotJar) -join ';') scala.tools.nsc.Main -usejavacp -Ymacro-classpath "$macros;$BotJar" -d $output @sources
if ($LASTEXITCODE -ne 0) { throw 'Wall filler power test compilation failed.' }
& $Java -cp "$output;$BotJar" Placement.WallFillerPowerTest
if ($LASTEXITCODE -ne 0) { throw 'Wall filler power tests failed.' }
