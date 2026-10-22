param([ValidateSet('patch','minor','major')][string]$Bump = 'patch')
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
Import-Module (Join-Path $PSScriptRoot 'release-tools.psm1') -Force
$project = Split-Path $PSScriptRoot -Parent
$previousLocation = Get-Location
$repository = 'carellice/piccole-gioie'
$privateDirectory = Join-Path $project '.release'
$pendingPath = Join-Path $privateDirectory 'pending.json'
$versionPath = Join-Path $project 'version.properties'
$releaseLock = $null
$versionBefore = $null
$committed = $false
try {
    Set-Location -LiteralPath $project
    foreach ($command in @('git','gh','java')) { Get-Command $command -ErrorAction Stop | Out-Null }
    if (-not (Test-Path -LiteralPath (Join-Path $privateDirectory 'signing.properties'))) {
        throw 'Firma assente: configura scripts/setup-signing.ps1 oppure ripristina il backup privato .release. Non viene mai creata una nuova chiave durante una release.'
    }
    $releaseLock = [IO.File]::Open((Join-Path $privateDirectory 'publish.lock'), 'OpenOrCreate', 'ReadWrite', 'None')
    Invoke-ReleaseCommand 'gh' @('auth','status','--hostname','github.com')
    $remote = & git remote get-url origin
    if ($LASTEXITCODE -ne 0 -or $remote -notmatch '^https://github\.com/carellice/piccole-gioie(?:\.git)?$|^git@github\.com:carellice/piccole-gioie(?:\.git)?$') {
        throw 'origin deve puntare a carellice/piccole-gioie.'
    }
    $branch = & git branch --show-current
    if ($branch -ne 'main') { throw 'Esegui la pubblicazione dal branch main.' }
    $changes = & git status --porcelain
    if ($LASTEXITCODE -ne 0 -or $changes) { throw 'Committa le modifiche prima di pubblicare: la cartella di lavoro deve essere pulita.' }
    Invoke-ReleaseCommand 'git' @('fetch','origin','main','--tags')

    if (Test-Path -LiteralPath $pendingPath) {
        $pending = Get-Content -LiteralPath $pendingPath -Raw | ConvertFrom-Json
        $headCommit = & git rev-parse HEAD
        if ($pending.commit -ne $headCommit) { throw 'La pubblicazione in sospeso appartiene a un altro commit. Completa quella versione prima di crearne una nuova.' }
        $next = [pscustomobject]@{ Name = $pending.versionName; Code = $pending.versionCode }
        $tag = $pending.tag
        $outputDirectory = Join-Path $privateDirectory $tag
        $committed = $true
        Write-Host "Riprendo la pubblicazione $tag senza incrementare di nuovo la versione."
    } else {
        $headCommit = & git rev-parse HEAD
        $remoteCommit = & git rev-parse origin/main
        if ($headCommit -ne $remoteCommit) { throw 'main deve essere sincronizzato con origin/main: esegui prima git pull oppure git push.' }
        $versionBefore = [IO.File]::ReadAllText($versionPath)
        $version = ConvertFrom-StringData $versionBefore
        $next = Get-NextAppVersion -Name $version.versionName -Code ([int]$version.versionCode) -Bump $Bump
        $tag = "v$($next.Name)"
        & git show-ref --verify --quiet "refs/tags/$tag"
        if ($LASTEXITCODE -eq 0) { throw "Il tag $tag esiste gia." }
        Write-ReleaseUtf8 $versionPath "versionCode=$($next.Code)`nversionName=$($next.Name)`n"
        Write-Host "Compilazione $tag (versionCode $($next.Code))..."
        & (Join-Path $project 'verifica.ps1') -Release
        if ($LASTEXITCODE -ne 0) { throw 'Build, test o lint non superati. La versione viene ripristinata.' }
        $builtApk = Join-Path $project 'app\build\outputs\apk\release\app-release.apk'
        if (-not (Test-Path -LiteralPath $builtApk)) { throw 'APK firmato non trovato: controlla la configurazione della firma.' }
        $outputDirectory = Join-Path $privateDirectory $tag
        New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
        $apkPath = Join-Path $outputDirectory 'piccole-gioie.apk'
        Copy-Item -LiteralPath $builtApk -Destination $apkPath -Force
        $hash = Get-ReleaseSha256 $apkPath
        $metadata = [ordered]@{ schemaVersion=1; applicationId='it.piccolegioie.app'; versionCode=$next.Code;
            versionName=$next.Name; apkName='piccole-gioie.apk'; sha256=$hash; size=(Get-Item -LiteralPath $apkPath).Length }
        Write-ReleaseUtf8 (Join-Path $outputDirectory 'piccole-gioie-update.json') ($metadata | ConvertTo-Json)
        Write-ReleaseUtf8 (Join-Path $outputDirectory 'SHA256SUMS.txt') "$hash  piccole-gioie.apk`n"
        Write-ReleaseUtf8 (Join-Path $outputDirectory 'release-notes.md') "piccole gioie $($next.Name)`n`nScarica **piccole-gioie.apk** oppure cerca gli aggiornamenti nella pagina **Rituale** dell'app. L'aggiornamento conserva i ricordi e richiede la conferma di Android.`n`nAndroid 8.0 o successivo. Version code: $($next.Code). Integrita: SHA256SUMS.txt.`n"
        Invoke-ReleaseCommand 'git' @('add','--','version.properties')
        Invoke-ReleaseCommand 'git' @('commit','-m',"Release $tag")
        $committed = $true
        $headCommit = & git rev-parse HEAD
        $pending = [ordered]@{ tag=$tag; versionName=$next.Name; versionCode=$next.Code; commit=$headCommit }
        Write-ReleaseUtf8 $pendingPath ($pending | ConvertTo-Json)
    }

    # Una ripresa verifica i file gia compilati prima di eseguire operazioni remote.
    $apkPath = Join-Path $outputDirectory 'piccole-gioie.apk'
    $metadataPath = Join-Path $outputDirectory 'piccole-gioie-update.json'
    $checksumPath = Join-Path $outputDirectory 'SHA256SUMS.txt'
    $notesPath = Join-Path $outputDirectory 'release-notes.md'
    $metadata = Get-Content -LiteralPath $metadataPath -Raw | ConvertFrom-Json
    if ($metadata.versionName -ne $next.Name -or $metadata.versionCode -ne $next.Code -or
        $metadata.sha256 -ne (Get-ReleaseSha256 $apkPath) -or
        $metadata.size -ne (Get-Item -LiteralPath $apkPath).Length) { throw 'I file della release non corrispondono: pubblicazione interrotta.' }
    & git show-ref --verify --quiet "refs/tags/$tag"
    if ($LASTEXITCODE -ne 0) { Invoke-ReleaseCommand 'git' @('tag','-a',$tag,'-m',"piccole gioie $($next.Name)",$headCommit) }
    $tagCommit = & git rev-parse "$tag^{}"
    if ($tagCommit -ne $headCommit) { throw 'Il tag punta a un commit differente.' }
    Invoke-ReleaseCommand 'git' @('push','--atomic','origin','HEAD:main',"refs/tags/${tag}:refs/tags/$tag")
    $releasesText = & gh release list --repo $repository --limit 1000 --json tagName,isDraft
    if ($LASTEXITCODE -ne 0) { throw 'Non riesco a leggere le release: rilancia lo script quando GitHub e disponibile.' }
    $existing = @($releasesText | ConvertFrom-Json) | Where-Object { $_.tagName -eq $tag } | Select-Object -First 1
    if ($existing) {
        if (-not $existing.isDraft) {
            # Se la pubblicazione era riuscita ma il processo si era interrotto subito dopo,
            # controlla i digest remoti e completa senza caricare o sostituire alcun asset.
            $publishedText = & gh api "repos/$repository/releases/tags/$tag"
            if ($LASTEXITCODE -ne 0) { throw 'Non riesco a verificare la release gia pubblicata.' }
            $published = $publishedText | ConvertFrom-Json
            foreach ($assetPath in @($apkPath,$metadataPath,$checksumPath)) {
                $assetName = Split-Path $assetPath -Leaf
                $remoteAsset = $published.assets | Where-Object { $_.name -eq $assetName } | Select-Object -First 1
                $expectedDigest = 'sha256:' + (Get-ReleaseSha256 $assetPath)
                if (-not $remoteAsset -or $remoteAsset.digest -ne $expectedDigest) { throw 'La release pubblica non corrisponde ai file locali; non viene modificata.' }
            }
            Remove-Item -LiteralPath $pendingPath
            Write-Host "Release gia pubblicata e verificata: https://github.com/$repository/releases/tag/$tag"
            exit 0
        }
    } else {
        Invoke-ReleaseCommand 'gh' @('release','create',$tag,'--repo',$repository,'--verify-tag','--draft',
            '--title',"piccole gioie $($next.Name)",'--notes-file',$notesPath)
    }
    Invoke-ReleaseCommand 'gh' @('release','upload',$tag,$apkPath,$metadataPath,$checksumPath,'--repo',$repository,'--clobber')
    Invoke-ReleaseCommand 'gh' @('release','edit',$tag,'--repo',$repository,'--draft=false','--latest')
    Remove-Item -LiteralPath $pendingPath
    Write-Host "Pubblicato: https://github.com/$repository/releases/tag/$tag"
} catch {
    if (-not $committed -and $null -ne $versionBefore) {
        Write-ReleaseUtf8 $versionPath $versionBefore
        & git restore --staged -- version.properties 2>$null
    }
    Write-Error $_ -ErrorAction Continue
    exit 1
} finally {
    if ($releaseLock) { $releaseLock.Dispose() }
    Set-Location $previousLocation
}
