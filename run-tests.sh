#!/bin/bash

echo "============================================"
echo "  Lancement des tests Selenium End-to-End"
echo "============================================"
echo ""
echo "Les tests vont s'exécuter avec Chrome visible"
echo "Vous allez voir le navigateur jouer les tests"
echo ""

mvn clean test

echo ""
echo "============================================"
echo "  Tests terminés"
echo "============================================"
