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
$output = Join-Path $repo 'target/attack-move-tests'
$macros = Join-Path $output 'macros'
New-Item -ItemType Directory -Path $macros -Force | Out-Null
& $Java -cp (($compiler + $BotJar) -join ';') scala.tools.nsc.Main -usejavacp -d $macros (Join-Path $repo 'src-macros/Utilities/TernaryMacros.scala') (Join-Path $repo 'src-macros/Mathematics/Functions/ReduceMacros.scala')
if ($LASTEXITCODE -ne 0) { throw 'Attack-move macro compilation failed.' }
$sources = @('src/Micro/Agency/Commander.scala', 'tests/Micro/AttackMoveTest.scala') | ForEach-Object { Join-Path $repo $_ }
& $Java -cp (($compiler + $BotJar) -join ';') scala.tools.nsc.Main -usejavacp -Ymacro-classpath "$macros;$BotJar" -d $output @sources
if ($LASTEXITCODE -ne 0) { throw 'Attack-move test compilation failed.' }
& $Java -cp "$output;$BotJar" Micro.AttackMoveTest
if ($LASTEXITCODE -ne 0) { throw 'Attack-move tests failed.' }
