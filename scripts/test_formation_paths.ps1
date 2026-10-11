param(
    [string]$Java = 'java',
    [string]$Javac = 'javac',
    [string]$ScalaRepository = (Join-Path $env:USERPROFILE '.m2/repository/org/scala-lang')
)

$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
$scalaVersion = '2.12.18'
$compiler = @('scala-compiler', 'scala-library', 'scala-reflect') | ForEach-Object {
    Join-Path $ScalaRepository "$_/$scalaVersion/$_-$scalaVersion.jar"
}
$botJar = Join-Path $repo 'target/PurpleWave-1.0.jar'
$output = Join-Path $repo 'target/formation-path-tests'
New-Item -ItemType Directory -Path $output -Force | Out-Null
& $Javac -cp $botJar -d $output (Join-Path $repo '3rdparty/jbwapi/src/test/java/bwapi/GameBuilder.java')
if ($LASTEXITCODE -ne 0) { throw 'Map fixture compilation failed.' }
$classpath = (@($output, $botJar) + $compiler) -join ';'
& $Java -cp $classpath scala.tools.nsc.Main -usejavacp -d $output (Join-Path $repo 'tests/Information/Geography/FormationPathTest.scala')
if ($LASTEXITCODE -ne 0) { throw 'Formation/pathfinding test compilation failed.' }
Push-Location (Join-Path $repo '3rdparty/jbwapi')
try {
    & $Java -cp $classpath Information.Geography.FormationPathTest
    if ($LASTEXITCODE -ne 0) { throw 'Formation/pathfinding tests failed.' }
} finally {
    Pop-Location
}
