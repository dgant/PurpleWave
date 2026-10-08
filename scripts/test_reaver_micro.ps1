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
foreach ($path in @($classes) + $compiler) {
    if (!(Test-Path -LiteralPath $path)) { throw "Missing dependency: $path. Run Maven compile first." }
}
$output = Join-Path $repo 'target/reaver-micro-tests'
New-Item -ItemType Directory -Path $output -Force | Out-Null
$classpath = (@($classes) + $compiler) -join ';'
$source = Join-Path $repo 'tests/Micro/ReaverMicroTest.scala'
& $Java -cp $classpath scala.tools.nsc.Main -usejavacp -d $output $source
if ($LASTEXITCODE -ne 0) { throw 'Reaver micro test compilation failed.' }
& $Java -cp "$output;$classpath" Micro.ReaverMicroTest
if ($LASTEXITCODE -ne 0) { throw 'Reaver micro tests failed.' }
