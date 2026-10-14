param(
    [string]$Java = 'java',
    [string]$ScalaRepository = (Join-Path $env:USERPROFILE '.m2/repository/org/scala-lang'),
    [string]$BotJar = (Join-Path $PSScriptRoot '../target/PurpleWave-1.0.jar'),
    [switch]$UseJarImplementation
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
# Separate outputs prevent a baseline run from picking up patched classes.
$variant = if ($UseJarImplementation) { 'baseline' } else { 'patched' }
$output = Join-Path $repo "target/placement-reservation-tests/$variant"
New-Item -ItemType Directory -Path $output -Force | Out-Null
$sources = @((Join-Path $repo 'tests/Placement/PlacementReservationTest.scala'))
$compilerOptions = @()
if (!$UseJarImplementation) {
    $macros = Join-Path $repo 'target/PurpleWave-macros.jar'
    if (!(Test-Path -LiteralPath $macros)) { throw "Missing dependency: $macros. Build the bot first." }
    $compilerOptions = @('-Ymacro-classpath', "$macros;$BotJar")
    $sources += @(
        (Join-Path $repo 'src/Placement/Templating/Template.scala'),
        (Join-Path $repo 'src/Placement/Templating/TemplatePointRequirement.scala'),
        (Join-Path $repo 'src/Placement/Access/Fits.scala'),
        (Join-Path $repo 'src/Placement/PlacementCache.scala'),
        (Join-Path $repo 'src/Placement/Generation/TemplatesProtoss.scala'),
        (Join-Path $repo 'src/Placement/Generation/TemplatesTerran.scala'),
        (Join-Path $repo 'src/Placement/Generation/Fitter.scala')
    )
}
& $Java -cp (($compiler + $BotJar) -join ';') scala.tools.nsc.Main -usejavacp @compilerOptions -d $output @sources
if ($LASTEXITCODE -ne 0) { throw 'Placement reservation test compilation failed.' }
& $Java -cp "$output;$BotJar" Placement.PlacementReservationTest
if ($LASTEXITCODE -ne 0) { throw 'Placement reservation tests failed.' }
