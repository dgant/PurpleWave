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
$output = Join-Path $repo 'target/macro-min-insert-tests'
New-Item -ItemType Directory -Path $output -Force | Out-Null
$classpath = (@($BotJar) + $compiler) -join ';'
& $Java -cp $classpath scala.tools.nsc.Main -usejavacp -d $output `
    (Join-Path $repo 'tests/Macro/Scheduling/MacroMinInsertTest.scala')
if ($LASTEXITCODE -ne 0) { throw 'Macro insertion-bound test compilation failed.' }
& $Java -cp "$output;$BotJar" Macro.Scheduling.MacroMinInsertTest
if ($LASTEXITCODE -ne 0) { throw 'Macro insertion-bound tests failed.' }
