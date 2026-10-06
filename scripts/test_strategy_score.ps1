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
foreach ($jar in $compiler) {
    if (!(Test-Path -LiteralPath $jar)) { throw "Missing Scala dependency: $jar. Run Maven package first." }
}
$output = Join-Path $repo 'target/strategy-score-tests'
New-Item -ItemType Directory -Path $output -Force | Out-Null
$sources = @(
    (Join-Path $repo 'src/Strategery/Selection/StrategyScore.scala'),
    (Join-Path $repo 'tests/Strategery/Selection/StrategyScoreTest.scala')
)
& $Java -cp ($compiler -join ';') scala.tools.nsc.Main -usejavacp -d $output @sources
if ($LASTEXITCODE -ne 0) { throw 'Strategy scoring test compilation failed.' }
& $Java -cp "$output;$($compiler[1])" Strategery.Selection.StrategyScoreTest
if ($LASTEXITCODE -ne 0) { throw 'Strategy scoring tests failed.' }
