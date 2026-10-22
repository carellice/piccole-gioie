$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot 'release-tools.psm1') -Force
foreach ($case in @(
    @{Name='1.2.9'; Code=6; Bump='patch'; Expected='1.2.10'},
    @{Name='1.2.9'; Code=6; Bump='minor'; Expected='1.3.0'},
    @{Name='1.2.9'; Code=6; Bump='major'; Expected='2.0.0'})) {
    $next = Get-NextAppVersion -Name $case.Name -Code $case.Code -Bump $case.Bump
    if ($next.Name -ne $case.Expected -or $next.Code -ne 7) { throw 'Incremento versione errato.' }
}
foreach ($invalid in @('1.2', 'v1.2.3', '1.2.3-beta')) {
    $failed = $false
    try { Get-NextAppVersion -Name $invalid -Code 1 | Out-Null } catch { $failed = $true }
    if (-not $failed) { throw 'Versione non valida accettata.' }
}
$temporaryFile = [IO.Path]::GetTempFileName()
try {
    Write-ReleaseUtf8 $temporaryFile 'abc'
    if ((Get-ReleaseSha256 $temporaryFile) -ne 'ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad') { throw 'SHA-256 errato.' }
} finally { Remove-Item -LiteralPath $temporaryFile }
Write-Host '7 controlli dello script release superati.'
