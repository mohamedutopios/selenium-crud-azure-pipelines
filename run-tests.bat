@echo off
echo ============================================
echo   Lancement des tests Selenium End-to-End
echo ============================================
echo.
echo Les tests vont s'executer avec Chrome visible
echo Vous allez voir le navigateur jouer les tests
echo.

mvn clean test

echo.
echo ============================================
echo   Tests termines
echo ============================================
pause
