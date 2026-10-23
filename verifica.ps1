param([switch]$Release)
# Un percorso ASCII evita un problema del class loader Java su Windows con cartelle accentate.
$ErrorActionPreference = 'Stop'
$projectDirectory = $PSScriptRoot
$temporaryDrive = $null
$previousLocation = Get-Location
try {
    if ($projectDirectory -match '[^\x00-\x7F]') {
    foreach ($letter in @('P','Q','R','S','T','U','V','W','X','Y','Z')) {
        if (-not (Test-Path "${letter}:\")) {
            & subst.exe "${letter}:" $projectDirectory
            if ($LASTEXITCODE -eq 0) { $temporaryDrive = "${letter}:"; break }
        }
    }
    if (-not $temporaryDrive) { throw 'Non trovo una lettera libera per il percorso di compilazione temporaneo.' }
    Set-Location "${temporaryDrive}\"
    } else { Set-Location -LiteralPath $projectDirectory }
    if ($Release) {
        & .\gradlew.bat assembleRelease testReleaseUnitTest lintRelease --console=plain
    } else {
        & .\gradlew.bat assembleDebug testDebugUnitTest lintDebug --console=plain
    }
    $verificationExit = $LASTEXITCODE
} finally {
    Set-Location $previousLocation
    if ($temporaryDrive) { & subst.exe $temporaryDrive /D }
}
exit $verificationExit
