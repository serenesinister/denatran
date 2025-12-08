@echo off
title Rodar - Sistema Denatran MQTT

:: Caminho base do projeto
cd /d "%~dp0"

:: Rodar a aplicação incluindo o jar do MQTT no classpath
java -cp "bin;jar/org.eclipse.paho.client.mqttv3-1.2.5.jar" Main

pause
