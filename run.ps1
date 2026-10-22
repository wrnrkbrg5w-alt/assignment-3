param([ValidateSet('demo', 'test', 'benchmark')][string]$Mode = 'demo', [int]$Source = 0)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    if ($env:JAVA_HOME) {
        $compiler = Join-Path $env:JAVA_HOME 'bin\javac.exe'
        $runtime = Join-Path $env:JAVA_HOME 'bin\java.exe'
    } else {
        $compiler = 'javac'
        $runtime = 'java'
    }
    New-Item -ItemType Directory -Force out | Out-Null
    $sources = Get-ChildItem src,test -Recurse -Filter '*.java' | ForEach-Object FullName
    & $compiler --release 17 -encoding UTF-8 -d out $sources
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed. Use JDK 17 or newer.' }
    switch ($Mode) {
        'demo' { & $runtime -cp out Main $Source }
        'test' { & $runtime -cp out Tests }
        'benchmark' { & $runtime -cp out Benchmark }
    }
    if ($LASTEXITCODE -ne 0) { throw 'Program failed.' }
} finally {
    Pop-Location
}
