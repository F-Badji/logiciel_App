#!/bin/bash

echo "🎨 Lancement du Logiciel de Déblocage Mobile avec le nouveau thème professionnel..."
echo "📱 Thème: Bleu Marine Sombre Professionnel"
echo ""

# Vérifier si Java est installé
if ! command -v java &> /dev/null; then
    echo "❌ ERREUR: Java n'est pas installé ou n'est pas dans le PATH"
    echo "📥 Veuillez installer Java 11 ou supérieur"
    exit 1
fi

# Vérifier la version de Java
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 11 ]; then
    echo "❌ ERREUR: Java 11 ou supérieur est requis (version actuelle: $JAVA_VERSION)"
    exit 1
fi

echo "✅ Java $JAVA_VERSION détecté"

# Nettoyer et compiler le projet
echo "🔨 Compilation du projet..."
if [ -d "target" ]; then
    rm -rf target
fi

# Compiler avec Maven si disponible
if command -v mvn &> /dev/null; then
    echo "📦 Compilation avec Maven..."
    mvn clean compile
    if [ $? -eq 0 ]; then
        echo "✅ Compilation réussie"
    else
        echo "❌ Erreur lors de la compilation"
        exit 1
    fi
else
    echo "⚠️  Maven non trouvé, compilation manuelle..."
    # Créer les dossiers nécessaires
    mkdir -p target/classes
    mkdir -p target/classes/com/logicielapp
    mkdir -p target/classes/fxml
    mkdir -p target/classes/styles
    mkdir -p target/classes/css
    mkdir -p target/classes/images
    
    # Copier les ressources
    cp -r src/main/resources/* target/classes/ 2>/dev/null || true
    echo "✅ Ressources copiées"
fi

# Lancer l'application
echo "🚀 Lancement de l'application..."
echo ""

# Essayer de lancer avec Maven
if command -v mvn &> /dev/null; then
    mvn exec:java -Dexec.mainClass="com.logicielapp.Main"
else
    # Lancer directement avec Java
    java -cp "target/classes:target/dependency/*" com.logicielapp.Main
fi

echo ""
echo "👋 Application fermée"
