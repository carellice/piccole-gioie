$ErrorActionPreference = 'Stop'
$projectDirectory = $PSScriptRoot
$sdkDirectory = Join-Path $projectDirectory '.tools\android-sdk'
if (-not (Test-Path $sdkDirectory)) {
    $sdkDirectory = $env:ANDROID_HOME
}
if (-not $sdkDirectory) { throw 'Apri il progetto in Android Studio e configura Android SDK.' }
$env:ANDROID_HOME = $sdkDirectory
$env:ANDROID_SDK_ROOT = $sdkDirectory
$env:ANDROID_AVD_HOME = Join-Path $projectDirectory '.tools\avd'
$adbExecutable = Join-Path $sdkDirectory 'platform-tools\adb.exe'
$emulatorExecutable = Join-Path $sdkDirectory 'emulator\emulator.exe'
$apkFile = Join-Path $projectDirectory 'app\build\outputs\apk\debug\app-debug.apk'
if (-not (Test-Path $apkFile)) { throw "Esegui prima .\verifica.ps1 per compilare il file APK." }
$deviceListing = & $adbExecutable devices
$deviceId = ($deviceListing | Where-Object { $_ -match '^emulator-\d+\s+device$' } | Select-Object -First 1) -replace '\s+device$', ''
if (-not $deviceId) {
    # La finestra resta visibile per permettere di provare l’app.
    Start-Process -FilePath $emulatorExecutable -ArgumentList @('-avd','Piccole_Gioie','-no-snapshot','-gpu','host','-feature','-Vulkan','-crash-report-mode','disabled','-no-metrics') -WindowStyle Normal
    Write-Host 'Avvio del Pixel 8…'
    $deadline = (Get-Date).AddMinutes(4)
    do {
        Start-Sleep -Seconds 2
        $deviceListing = & $adbExecutable devices
        $deviceId = ($deviceListing | Where-Object { $_ -match '^emulator-\d+\s+device$' } | Select-Object -First 1) -replace '\s+device$', ''
        $booted = if ($deviceId) { & $adbExecutable -s $deviceId shell getprop sys.boot_completed } else { '' }
    } until ($booted -match '1' -or (Get-Date) -gt $deadline)
    if ($booted -notmatch '1') { throw "Il Pixel non ha terminato il boot. Controlla la finestra del dispositivo virtuale." }
}
& $adbExecutable -s $deviceId install -r $apkFile
if ($LASTEXITCODE -ne 0) { throw 'Installazione non riuscita.' }
& $adbExecutable -s $deviceId shell am start -W -n it.piccolegioie.app/.MainActivity
