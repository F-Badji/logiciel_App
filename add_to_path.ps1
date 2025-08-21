# Script pour ajouter les chemins des outils au PATH système

Write-Host "AJOUT DES CHEMINS AU PATH SYSTEME" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""

# Chemins à ajouter
$paths = @(
    "C:\platform-tools",
    "C:\libimobiledevice", 
    "C:\checkra1n"
)

Write-Host "Chemins a ajouter:" -ForegroundColor Blue
foreach ($path in $paths) {
    Write-Host "  - $path" -ForegroundColor White
}

Write-Host ""
Write-Host "Ajout au PATH..." -ForegroundColor Yellow

foreach ($path in $paths) {
    if (Test-Path $path) {
        $currentPath = [Environment]::GetEnvironmentVariable("PATH", "Machine")
        if ($currentPath -notlike "*$path*") {
            [Environment]::SetEnvironmentVariable("PATH", "$currentPath;$path", "Machine")
            Write-Host "OK: $path ajoute au PATH" -ForegroundColor Green
        } else {
            Write-Host "INFO: $path deja dans le PATH" -ForegroundColor Blue
        }
    } else {
        Write-Host "ERREUR: $path n'existe pas" -ForegroundColor Red
        Write-Host "   Installez d'abord les outils dans ce dossier" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Rafraichissement du PATH..." -ForegroundColor Yellow
$env:PATH = [System.Environment]::GetEnvironmentVariable("PATH","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("PATH","User")

Write-Host ""
Write-Host "VERIFICATION DES OUTILS:" -ForegroundColor Blue
Write-Host "------------------------" -ForegroundColor Blue

# Vérifier les outils
$tools = @(
    @{Name="ADB"; Command="adb"},
    @{Name="Fastboot"; Command="fastboot"},
    @{Name="idevice_id"; Command="idevice_id"},
    @{Name="ideviceinfo"; Command="ideviceinfo"},
    @{Name="checkra1n"; Command="checkra1n"}
)

foreach ($tool in $tools) {
    if (Get-Command $tool.Command -ErrorAction SilentlyContinue) {
        Write-Host "OK: $($tool.Name) - Disponible" -ForegroundColor Green
    } else {
        Write-Host "ERREUR: $($tool.Name) - Non trouve" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "OPERATION TERMINEE!" -ForegroundColor Green
Write-Host "===================" -ForegroundColor Green

Write-Host ""
Write-Host "PROCHAINES ETAPES:" -ForegroundColor Blue
Write-Host "------------------" -ForegroundColor Blue
Write-Host "1. Redemarrez PowerShell pour que les changements prennent effet" -ForegroundColor White
Write-Host "2. Testez les outils avec: adb version, idevice_id -l, etc." -ForegroundColor White
Write-Host "3. Executez: test_simple.ps1 pour verifier la fiabilite" -ForegroundColor White

Write-Host ""
Write-Host "Script termine le $(Get-Date)" -ForegroundColor Gray
