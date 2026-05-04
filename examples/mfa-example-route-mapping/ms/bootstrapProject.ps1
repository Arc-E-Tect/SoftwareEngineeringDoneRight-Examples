#!/usr/bin/env pwsh
# Bootstrap the Family Ties test environment (keystores + .env files).
# Run from the family-ties/ project root. Assumes a clean or resettable state.
#
# Required environment variable:
#   SSL_KEYSTORE_PASSWORD — must be set before running this script
#
# Optional environment variables (defaults applied when absent):
#   H2_USERNAME       — default: sa
#   H2_PASSWORD       — default: generated random (not written to .env)
#   POSTGRES_DB       — default: familyties
#   POSTGRES_USER     — default: familyties
#   POSTGRES_PASSWORD — default: generated random (not written to .env)
#   SERVER_PORT       — default: 8443
#
# This script first removes any existing .env and keystore files so the bootstrap
# always writes fresh, then delegates to setup-test-env.sh which runs non-interactively
# when all env vars are pre-set.
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrWhiteSpace($env:SSL_KEYSTORE_PASSWORD)) {
    Write-Host 'ERROR: SSL_KEYSTORE_PASSWORD must be set before running bootstrapProject.ps1' -ForegroundColor Red
    exit 1
}

# Track which env vars we inject so we can clean them up afterwards.
$injectedVars = [System.Collections.Generic.List[string]]::new()

function Set-EnvDefault {
    param([string]$Name, [string]$Default)
    if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($Name, 'Process'))) {
        [Environment]::SetEnvironmentVariable($Name, $Default, 'Process')
        $script:injectedVars.Add($Name)
    }
}

function Set-RandomEnvIfAbsent {
    param([string]$Name)
    if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($Name, 'Process'))) {
        $random = [System.Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(16))
        [Environment]::SetEnvironmentVariable($Name, $random, 'Process')
        $script:injectedVars.Add($Name)
    }
}

Set-EnvDefault     'H2_USERNAME'       'sa'
Set-RandomEnvIfAbsent 'H2_PASSWORD'
Set-EnvDefault     'POSTGRES_DB'       'familyties'
Set-EnvDefault     'POSTGRES_USER'     'familyties'
Set-RandomEnvIfAbsent 'POSTGRES_PASSWORD'
Set-EnvDefault     'SERVER_PORT'       '8443'

$exitCode = 0

Push-Location $PSScriptRoot
try {
    # Remove existing .env and keystore files so bootstrap always writes fresh.
    # Excludes build/cache/vcs directories that would never contain these files.
    $skipPattern = '[\\/](\.git|\.gradle|build|node_modules|\.angular|\.cache)[\\/]'
    $filesToClean = Get-ChildItem -Path . -Recurse -File -ErrorAction SilentlyContinue |
        Where-Object { ($_.Name -eq '.env' -or $_.Name -eq 'keystore.p12') -and $_.FullName -notmatch $skipPattern }
    foreach ($f in $filesToClean) {
        Remove-Item $f.FullName -Force
        Write-Host "  Removed: $($f.FullName)" -ForegroundColor DarkGray
    }

    # setup-test-env.sh is fully non-interactive when all env vars above are set.
    & bash ./setup-test-env.sh
    $exitCode = $LASTEXITCODE
} finally {
    Pop-Location
    # Remove env vars we injected to avoid polluting the caller's environment.
    foreach ($name in $injectedVars) {
        try { Remove-Item "Env:$name" -ErrorAction SilentlyContinue } catch {}
    }
}

if ($exitCode -ne 0) {
    Write-Host "ERROR: setup-test-env.sh failed with exit code $exitCode" -ForegroundColor Red
    exit 1
}

Write-Host 'Family Ties bootstrap completed successfully.' -ForegroundColor Green
