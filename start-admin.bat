@echo off
title AfriMarket Admin Server
echo.
echo  =====================================================
echo   AfriMarket Backend ^& Admin - Demarrage du serveur
echo  =====================================================
echo.

cd /d "%~dp0"

echo [1/2] Construction du projet...
call mvn package -DskipTests -q
if errorlevel 1 (
    echo ERREUR : La compilation a echoue. Verifiez les logs Maven.
    pause
    exit /b 1
)

echo [2/2] Demarrage du serveur Spring Boot...
echo.
echo  URL Admin    : http://localhost:8080/admin/login
echo  API Base URL : http://localhost:8080/api/v1
echo  Login Admin  : admin / Admin@2026
echo  Login Prod.  : +237677123401 / Prod@2026 (test mobile)
echo.
echo  Appuyez sur Ctrl+C pour arreter le serveur.
echo.

java -Djdk.net.unixdomain.tmpdir=%TEMP% ^
     -Djava.net.preferIPv4Stack=true ^
     --add-opens=java.base/sun.nio.ch=ALL-UNNAMED ^
     -jar target\AfriMarket_backend-0.0.1-SNAPSHOT.jar

pause
