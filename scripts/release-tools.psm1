Set-StrictMode -Version Latest

function Get-NextAppVersion {
    param([Parameter(Mandatory)][string]$Name, [Parameter(Mandatory)][int]$Code,
        [ValidateSet('patch','minor','major')][string]$Bump = 'patch')
    if ($Name -notmatch '^(\d+)\.(\d+)\.(\d+)$' -or $Code -lt 1 -or $Code -ge [int]::MaxValue) {
        throw 'version.properties deve contenere una versione major.minor.patch e un versionCode incrementabile.'
    }
    $major = [int]$Matches[1]; $minor = [int]$Matches[2]; $patch = [int]$Matches[3]
    switch ($Bump) {
        'major' { $major++; $minor = 0; $patch = 0 }
        'minor' { $minor++; $patch = 0 }
        'patch' { $patch++ }
    }
    [pscustomobject]@{ Name = "$major.$minor.$patch"; Code = $Code + 1 }
}

function Write-ReleaseUtf8 {
    param([string]$Path, [string]$Text)
    [System.IO.File]::WriteAllText($Path, $Text, [System.Text.UTF8Encoding]::new($false))
}

function Invoke-ReleaseCommand {
    param([string]$Executable, [string[]]$Arguments)
    & $Executable @Arguments | Out-Host
    if ($LASTEXITCODE -ne 0) { throw "$Executable non riuscito (codice $LASTEXITCODE)." }
}

Export-ModuleMember -Function Get-NextAppVersion, Write-ReleaseUtf8, Invoke-ReleaseCommand
