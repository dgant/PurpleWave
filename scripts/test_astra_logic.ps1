param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$MavenRepository = "$env:USERPROFILE\.m2\repository",
    [string]$Jar
)
$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
if (-not $JavaHome) { throw 'Set JAVA_HOME or pass -JavaHome (JDK 8).' }
if (-not $Jar) { $Jar = Join-Path $repo 'target\PurpleWave-1.0.jar' }
if (-not (Test-Path -LiteralPath $Jar)) { throw 'Run Maven package first.' }
$classes = Join-Path $repo 'target\astra-tests'
New-Item -ItemType Directory -Force $classes | Out-Null
$compiler = @('scala-compiler', 'scala-library', 'scala-reflect') | ForEach-Object {
    Join-Path $MavenRepository "org\scala-lang\$_\2.12.18\$_-2.12.18.jar"
}
foreach ($dependency in $compiler) {
    if (-not (Test-Path -LiteralPath $dependency)) { throw "Missing Scala build dependency: $dependency" }
}
& (Join-Path $JavaHome 'bin\javac.exe') -encoding UTF-8 -cp $Jar -d $classes (Join-Path $repo 'tests\astra\bwapi\HeadlessBWAPI.java')
if ($LASTEXITCODE) { throw 'Headless BWAPI fixture compilation failed.' }
& (Join-Path $JavaHome 'bin\java.exe') -Xmx512m -cp ($compiler -join ';') scala.tools.nsc.Main -classpath "$Jar;$classes" -d $classes (Join-Path $repo 'tests\astra\AstraLogicRegression.scala')
if ($LASTEXITCODE) { throw 'Regression test compilation failed.' }
& (Join-Path $JavaHome 'bin\java.exe') -Xmx512m -cp "$classes;$Jar" Regression.AstraLogicRegression
if ($LASTEXITCODE) { throw 'Regression tests failed.' }
