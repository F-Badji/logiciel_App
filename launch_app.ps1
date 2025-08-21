# Script PowerShell pour lancer le Logiciel de Déblocage Mobile
# Thème moderne professionnel

Write-Host "🎨 Lancement du Logiciel de Déblocage Mobile avec le nouveau thème moderne..." -ForegroundColor Cyan
Write-Host "📱 Thème: Moderne Professionnel Clair et Lisible" -ForegroundColor Yellow
Write-Host ""

# Chemin vers Java d'IntelliJ IDEA
$JAVA_PATH = "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2\jbr\bin\java.exe"

# Vérifier si Java est disponible
if (Test-Path $JAVA_PATH) {
    $javaVersion = & $JAVA_PATH -version 2>&1 | Select-String "version" | ForEach-Object { $_.ToString().Split('"')[1] }
    Write-Host "✅ Java $javaVersion détecté (IntelliJ IDEA)" -ForegroundColor Green
} else {
    Write-Host "❌ ERREUR: Java d'IntelliJ IDEA non trouvé" -ForegroundColor Red
    exit 1
}

# Nettoyer et compiler le projet
Write-Host "🔨 Compilation du projet..." -ForegroundColor Yellow

if (Test-Path "target") {
    Remove-Item -Recurse -Force "target"
}

# Compiler avec Maven si disponible
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    Write-Host "📦 Compilation avec Maven..." -ForegroundColor Yellow
    $env:JAVA_HOME = "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2\jbr"
    mvn clean compile
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Compilation réussie" -ForegroundColor Green
    } else {
        Write-Host "❌ Erreur lors de la compilation" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "⚠️  Maven non trouvé, compilation manuelle..." -ForegroundColor Yellow
    # Créer les dossiers nécessaires
    New-Item -ItemType Directory -Force -Path "target/classes" | Out-Null
    New-Item -ItemType Directory -Force -Path "target/classes/com/logicielapp" | Out-Null
    New-Item -ItemType Directory -Force -Path "target/classes/fxml" | Out-Null
    New-Item -ItemType Directory -Force -Path "target/classes/styles" | Out-Null
    New-Item -ItemType Directory -Force -Path "target/classes/css" | Out-Null
    New-Item -ItemType Directory -Force -Path "target/classes/images" | Out-Null
    
    # Copier les ressources
    if (Test-Path "src/main/resources") {
        Copy-Item -Recurse -Force "src/main/resources/*" "target/classes/" -ErrorAction SilentlyContinue
        Write-Host "✅ Ressources copiées" -ForegroundColor Green
    }
}

# Lancer l'application
Write-Host "🚀 Lancement de l'application..." -ForegroundColor Cyan
Write-Host ""

# Essayer de lancer avec Maven
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    $env:JAVA_HOME = "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2\jbr"
    mvn exec:java -Dexec.mainClass="com.logicielapp.Main"
} else {
    # Lancer directement avec Java d'IntelliJ IDEA
    & $JAVA_PATH -cp "target/classes;target/dependency/*" com.logicielapp.Main
}

Write-Host ""
Write-Host "👋 Application fermée" -ForegroundColor Green
