@echo off
title Compilar - Sistema Denatran MQTT

echo ===========================
echo Compilando o sistema...
echo ===========================

:: Caminho base do projeto
cd /d "%~dp0"

:: Criar pasta BIN se não existir
if not exist bin mkdir bin

:: Compilar tudo que está no src, incluindo o jar do MQTT
javac -cp "jar/org.eclipse.paho.client.mqttv3-1.2.5.jar;." -d bin src\*.java

if %errorlevel% neq 0 (
    echo.
    echo *** ERRO NA COMPILACAO ***
    pause
    exit /b
)

echo.
echo *** COMPILACAO CONCLUIDA COM SUCESSO ***
pause
