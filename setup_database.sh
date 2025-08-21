#!/bin/bash

# Script de configuration de la base de données pour le Logiciel de Déblocage Mobile
# Ce script initialise la base de données MySQL avec le schéma requis

echo "🚀 Initialisation de la base de données pour le Logiciel de Déblocage Mobile"
echo "=================================================================="

# Configuration MySQL (modifiez selon votre environnement)
MYSQL_HOST="localhost"
MYSQL_PORT="3306"
MYSQL_USER="root"
MYSQL_PASSWORD=""
DB_NAME="logiciel_App"
SQL_FILE="database/logiciel_app_schema.sql"

# Vérifier si MySQL est accessible
echo "🔍 Vérification de la connexion MySQL..."
/Applications/XAMPP/xamppfiles/bin/mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD -e "SELECT 1;" > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "❌ Erreur: Impossible de se connecter à MySQL"
    echo "   Vérifiez que MySQL est démarré et que les identifiants sont corrects"
    echo "   Host: $MYSQL_HOST:$MYSQL_PORT"
    echo "   User: $MYSQL_USER"
    exit 1
fi

echo "✅ Connexion MySQL établie"

# Vérifier si le fichier SQL existe
if [ ! -f "$SQL_FILE" ]; then
    echo "❌ Erreur: Fichier SQL introuvable: $SQL_FILE"
    exit 1
fi

echo "✅ Fichier SQL trouvé: $SQL_FILE"

# Créer la base de données
echo "🔨 Création de la base de données..."
/Applications/XAMPP/xamppfiles/bin/mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD < "$SQL_FILE"
if [ $? -eq 0 ]; then
    echo "✅ Base de données '$DB_NAME' créée avec succès"
else
    echo "❌ Erreur lors de la création de la base de données"
    exit 1
fi

# Vérifier la création des tables
echo "🔍 Vérification des tables créées..."
TABLES=$(/Applications/XAMPP/xamppfiles/bin/mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD -D$DB_NAME -e "SHOW TABLES;" --skip-column-names)
TABLE_COUNT=$(echo "$TABLES" | wc -l)

echo "✅ Tables créées ($TABLE_COUNT):"
echo "$TABLES" | while read table; do
    echo "   - $table"
done

# Afficher quelques statistiques
echo ""
echo "📊 Statistiques de la base de données:"
echo "   - Utilisateurs: $(/Applications/XAMPP/xamppfiles/bin/mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD -D$DB_NAME -e "SELECT COUNT(*) FROM utilisateurs;" --skip-column-names)"
echo "   - Appareils supportés: $(/Applications/XAMPP/xamppfiles/bin/mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD -D$DB_NAME -e "SELECT COUNT(*) FROM appareils_supportes;" --skip-column-names)"
echo "   - Serveurs IMEI: $(/Applications/XAMPP/xamppfiles/bin/mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD -D$DB_NAME -e "SELECT COUNT(*) FROM serveurs_imei;" --skip-column-names)"

echo ""
echo "🎉 Configuration de la base de données terminée avec succès!"
echo ""
echo "📝 Informations de connexion pour l'application:"
echo "   URL: jdbc:mysql://$MYSQL_HOST:$MYSQL_PORT/$DB_NAME"
echo "   Utilisateur: $MYSQL_USER"
echo "   Base de données: $DB_NAME"
echo ""
echo "🔐 Comptes utilisateurs créés:"
echo "   - Admin: admin@logiciel-app.com / admin123"
echo "   - Technicien: tech@logiciel-app.com / tech123"
echo ""
echo "🚀 Vous pouvez maintenant démarrer l'application Java!"
