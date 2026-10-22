param([switch]$ImportExistingDebugKey)
$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot 'release-tools.psm1') -Force
$project = Split-Path $PSScriptRoot -Parent
$privateDirectory = Join-Path $project '.release'
$propertiesPath = Join-Path $privateDirectory 'signing.properties'
if (Test-Path -LiteralPath $propertiesPath) { throw 'La firma e gia configurata. Non sovrascrivo la chiave.' }
$keytool = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\keytool.exe' } else { (Get-Command keytool -ErrorAction Stop).Source }
New-Item -ItemType Directory -Path $privateDirectory -Force | Out-Null
$keystore = Join-Path $privateDirectory 'piccole-gioie.p12'
if (Test-Path -LiteralPath $keystore) { throw 'Esiste gia una chiave: ripristina signing.properties dal tuo backup.' }
$random = [byte[]]::new(32)
$generator = [Security.Cryptography.RandomNumberGenerator]::Create()
try { $generator.GetBytes($random) } finally { $generator.Dispose() }
$password = [Convert]::ToBase64String($random)
# Password passata a keytool attraverso l'ambiente, mai nella riga di comando o nei log.
$env:PICCOLE_GIOIE_KEY_PASSWORD = $password
try {
    if ($ImportExistingDebugKey) {
        $existing = Join-Path $env:USERPROFILE '.android\debug.keystore'
        if (-not (Test-Path -LiteralPath $existing)) { throw 'Chiave della build esistente non trovata.' }
        Invoke-ReleaseCommand $keytool @('-importkeystore','-srckeystore',$existing,'-srcstorepass','android',
            '-srcalias','androiddebugkey','-srckeypass','android','-destkeystore',$keystore,'-deststoretype','PKCS12',
            '-deststorepass:env','PICCOLE_GIOIE_KEY_PASSWORD','-destkeypass:env','PICCOLE_GIOIE_KEY_PASSWORD',
            '-destalias','piccole-gioie','-noprompt')
    } else {
        Invoke-ReleaseCommand $keytool @('-genkeypair','-keystore',$keystore,'-storetype','PKCS12',
            '-storepass:env','PICCOLE_GIOIE_KEY_PASSWORD','-keypass:env','PICCOLE_GIOIE_KEY_PASSWORD',
            '-alias','piccole-gioie','-keyalg','RSA','-keysize','3072','-validity','10000',
            '-dname','CN=piccole gioie','-noprompt')
    }
    Write-ReleaseUtf8 $propertiesPath "storeFile=.release/piccole-gioie.p12`nstorePassword=$password`nkeyAlias=piccole-gioie`nkeyPassword=$password`n"
    Write-Host 'Firma configurata. Conserva un backup privato della cartella .release: serve per tutti gli aggiornamenti.'
} finally { Remove-Item Env:\PICCOLE_GIOIE_KEY_PASSWORD -ErrorAction SilentlyContinue }
