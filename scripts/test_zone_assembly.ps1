param(
    [string]$Java = 'java',
    [string]$BotJar = (Join-Path $PSScriptRoot '../target/PurpleWave-1.0.jar'),
    [switch]$UseJarImplementation
)

$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
$compiler = @('scala-compiler', 'scala-library', 'scala-reflect') | ForEach-Object {
    Join-Path $env:USERPROFILE ".m2/repository/org/scala-lang/$_/2.12.18/$_-2.12.18.jar"
}
$macros = Join-Path $repo 'target/PurpleWave-macros.jar'
$variant = if ($UseJarImplementation) { 'baseline' } else { 'patched' }
$output = Join-Path $repo "target/zone-assembly/$variant-classes"
New-Item -ItemType Directory -Path $output -Force | Out-Null
$expectedRoot = [IO.Path]::GetFullPath((Join-Path $repo 'target/zone-assembly')) + [IO.Path]::DirectorySeparatorChar
if (![IO.Path]::GetFullPath($output).StartsWith($expectedRoot, [StringComparison]::OrdinalIgnoreCase)) {
    throw "Test class output is outside $expectedRoot"
}
Get-ChildItem -LiteralPath $output -Filter '*.class' -Recurse | ForEach-Object { Remove-Item -LiteralPath $_.FullName }
$sources = @((Join-Path $repo 'tests/Information/Geography/WriteZoneAssemblySnapshot.scala'))
if (!$UseJarImplementation) {
    $sources += @(
        'src/Information/Geography/Calculations/MergeMainZones.scala',
        'src/Information/Geography/GeographyBuilder.scala',
        'src/Information/Geography/Types/Edge.scala',
        'tests/Information/Geography/MergeMainZonesTest.scala'
    ) | ForEach-Object { Join-Path $repo $_ }
}
& $Java -cp (($compiler + $BotJar + $macros) -join ';') scala.tools.nsc.Main -usejavacp -feature -language:postfixOps -d $output @sources
if ($LASTEXITCODE -ne 0) { throw 'Zone assembly test compilation failed.' }
if (!$UseJarImplementation) {
    & $Java -cp "$output;$BotJar" Information.Geography.MergeMainZonesTest (Join-Path $repo 'tests/Information/Geography/MainZoneMaps.tsv')
    if ($LASTEXITCODE -ne 0) { throw 'Zone assembly regression tests failed.' }
}
Write-Output "Diagnostic classes: $output"
