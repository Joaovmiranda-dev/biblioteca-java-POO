$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
if (!(Test-Path build/biblioteca.jar) -or !(Test-Path build/lib/h2-2.3.232.jar)) {
    & "$PSScriptRoot/build.ps1"
}
& java "-Dfile.encoding=UTF-8" -cp "build/biblioteca.jar;build/lib/h2-2.3.232.jar" br.com.biblioteca.Main
if ($LASTEXITCODE -ne 0) { throw "Aplicação encerrada com erro." }

