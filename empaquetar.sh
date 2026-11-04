#!/bin/bash
# Genera ProyectoFinal.zip. Requiere Maven y JDK 17+. Uso: ./empaquetar.sh
set -e
mvn -q -B package
rm -rf dist && mkdir -p dist/ProyectoFinal/sql
cp target/app.jar config.properties ejecutar.sh ejecutar.command ejecutar.bat LEEME.txt dist/ProyectoFinal/
cp sql/schema.sql dist/ProyectoFinal/sql/
cp -r src pom.xml dist/ProyectoFinal/
(cd dist && zip -qr ../ProyectoFinal.zip ProyectoFinal)
echo "Listo: ProyectoFinal.zip"
