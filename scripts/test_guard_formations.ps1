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
foreach ($jar in @($BotJar) + $compiler) {
    if (!(Test-Path -LiteralPath $jar)) { throw "Missing dependency: $jar. Build the bot or supply -BotJar." }
}
# Macro implementations are compiled separately, just as in the Maven build.
$output = Join-Path $repo 'target/guard-formation-tests'
New-Item -ItemType Directory -Path $output -Force | Out-Null
$macros = Join-Path $output 'macros'
New-Item -ItemType Directory -Path $macros -Force | Out-Null
& $Java -cp (($compiler + $BotJar) -join ';') scala.tools.nsc.Main -usejavacp -d $macros (Join-Path $repo 'src-macros/Utilities/TernaryMacros.scala') (Join-Path $repo 'src-macros/Mathematics/Functions/ReduceMacros.scala')
if ($LASTEXITCODE -ne 0) { throw 'Guard formation macro compilation failed.' }
$sources = @(
    (Join-Path $repo 'src/Micro/Formation/GuardGeometry.scala'),
    (Join-Path $repo 'src/Micro/Formation/FormationStandard.scala'),
    (Join-Path $repo 'tests/Micro/GuardFormationTest.scala')
)
& $Java -cp (($compiler + $BotJar) -join ';') scala.tools.nsc.Main -usejavacp -Ymacro-classpath "$macros;$BotJar" -d $output @sources
if ($LASTEXITCODE -ne 0) { throw 'Guard formation test compilation failed.' }
& $Java -cp "$output;$BotJar" Micro.Formation.GuardFormationTest
if ($LASTEXITCODE -ne 0) { throw 'Guard formation tests failed.' }
