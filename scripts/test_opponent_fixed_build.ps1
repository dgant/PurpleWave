param([string]$Java = 'java')
$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
$compiler = @('scala-compiler', 'scala-library', 'scala-reflect') | ForEach-Object {
    Join-Path $env:USERPROFILE ".m2/repository/org/scala-lang/$_/2.12.18/$_-2.12.18.jar"
}
$jar = Join-Path $repo 'target/PurpleWave-1.0.jar'
$output = Join-Path $repo 'target/opponent-fixed-build-tests'
New-Item -ItemType Directory -Path $output -Force | Out-Null
$classpath = (@($jar) + $compiler) -join ';'
& $Java -cp $classpath scala.tools.nsc.Main -usejavacp -d $output (Join-Path $repo 'tests/Configuration/OpponentFixedBuildTest.scala')
if ($LASTEXITCODE -ne 0) { throw 'Opponent fixed-build test compilation failed' }
& $Java -cp "$output;$classpath" Configuration.OpponentFixedBuildTest
if ($LASTEXITCODE -ne 0) { throw 'Opponent fixed-build tests failed' }
