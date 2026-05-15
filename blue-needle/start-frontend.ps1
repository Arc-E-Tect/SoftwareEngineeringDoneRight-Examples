#!/usr/bin/env pwsh
# start-frontend.ps1 — start the Blue Needle Angular dev server
#
# Usage:
#   ./start-frontend.ps1            # proxy to real IFF (Mode B, iff:up must be running)
#   ./start-frontend.ps1 --open     # open browser automatically
#
# The Angular dev server will be available at http://localhost:4200

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

# Kill any existing process occupying port 4200
if ($IsWindows) {
    $existing = Get-NetTCPConnection -LocalPort 4200 -ErrorAction SilentlyContinue
    if ($existing) {
        $owningPids = $existing | Select-Object -ExpandProperty OwningProcess -Unique
        Write-Host "Stopping existing process(es) on port 4200: $($owningPids -join ', ')"
        $owningPids | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }
        $retries = 10
        while ($retries -gt 0 -and (Get-NetTCPConnection -LocalPort 4200 -ErrorAction SilentlyContinue)) {
            Start-Sleep -Milliseconds 500
            $retries--
        }
    }
} else {
    $existingPids = lsof -ti :4200 2>/dev/null
    if ($existingPids) {
        Write-Host "Stopping existing process(es) on port 4200: $existingPids"
        $existingPids -split "`n" | Where-Object { $_ } | ForEach-Object { kill -9 $_ 2>/dev/null }
        $retries = 10
        while ($retries -gt 0 -and (lsof -ti :4200 2>/dev/null)) {
            Start-Sleep -Milliseconds 500
            $retries--
        }
    }
}

& node_modules/.bin/ng serve --proxy-config proxy.conf.json @args
