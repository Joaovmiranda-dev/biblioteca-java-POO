param([switch]$Test)
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
New-Item -ItemType Directory -Force build/classes,build/lib | Out-Null
$driver = Join-Path $PSScriptRoot "build/lib/h2-2.3.232.jar"
if (!(Test-Path $driver)) {
    Invoke-WebRequest "https://repo.maven.apache.org/maven2/com/h2database/h2/2.3.232/h2-2.3.232.jar" -OutFile "$driver.download"
    Move-Item -LiteralPath "$driver.download" -Destination $driver -Force
}
$sources = @(Get-ChildItem src/main/java -Recurse -Filter *.java | ForEach-Object { $_.FullName })
& javac --release 17 -encoding UTF-8 -d build/classes @sources
if ($LASTEXITCODE -ne 0) { throw "Falha na compilação." }
Copy-Item sql/schema.sql build/classes/schema.sql -Force
& jar "-J-Djava.io.tmpdir=$PSScriptRoot/build" --create --file build/biblioteca.jar --main-class br.com.biblioteca.Main -C build/classes .
if ($LASTEXITCODE -ne 0) { throw "Falha ao criar JAR." }
if ($Test) {
    New-Item -ItemType Directory -Force build/test-classes | Out-Null
    & javac --release 17 -encoding UTF-8 -cp "build/classes;$driver" -d build/test-classes src/test/java/br/com/biblioteca/IntegrationTest.java
    if ($LASTEXITCODE -ne 0) { throw "Falha ao compilar testes." }
    & java -cp "build/classes;build/test-classes;$driver" br.com.biblioteca.IntegrationTest
    if ($LASTEXITCODE -ne 0) { throw "Testes falharam." }
}
Write-Host "Compilação concluída. Execute ./run.ps1"

