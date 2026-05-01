#!/usr/bin/env pwsh
# stop-frontend.ps1 — stop the Blue Needle Angular dev server
#
# Usage:
#   ./stop-frontend.ps1

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

if ($IsWindows) {
    $existing = Get-NetTCPConnection -LocalPort 4200 -ErrorAction SilentlyContinue
    if (-not $existing) {
        Write-Host "No process found on port 4200."
        exit 0
    }
    $owningPids = $existing | Select-Object -ExpandProperty OwningProcess -Unique
    Write-Host "Stopping Angular dev server (PID(s): $($owningPids -join ', '))…"
    $owningPids | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }
    $retries = 10
    while ($retries -gt 0 -and (Get-NetTCPConnection -LocalPort 4200 -ErrorAction SilentlyContinue)) {
        Start-Sleep -Milliseconds 500
        $retries--
    }
    if (Get-NetTCPConnection -LocalPort 4200 -ErrorAction SilentlyContinue) {
        Write-Host "Warning: port 4200 still in use after stopping."
        exit 1
    }
} else {
    $pids = lsof -ti :4200 2>/dev/null
    if (-not $pids) {
        Write-Host "No process found on port 4200."
        exit 0
    }
    Write-Host "Stopping Angular dev server (PID(s): $($pids -join ', '))…"
    $pids -split "`n" | Where-Object { $_ } | ForEach-Object { kill -9 $_ 2>/dev/null }
    $retries = 10
    while ($retries -gt 0 -and (lsof -ti :4200 2>/dev/null)) {
        Start-Sleep -Milliseconds 500
        $retries--
    }
    if (lsof -ti :4200 2>/dev/null) {
        Write-Host "Warning: port 4200 still in use after stopping."
        exit 1
    }
}

Write-Host "Angular dev server stopped."
