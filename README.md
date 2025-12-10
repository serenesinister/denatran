# DENATRAN

Sistema de gerenciamento de condutores, veículos, multas e IPVA com integração via MQTT.

---

## Descrição

Este projeto simula o sistema DENATRAN com os seguintes módulos:

- **CondutorService**: cadastro de condutores, pontuação e ranking.
- **VeiculoService**: registro e transferência de veículos.
- **MultaService**: lançamento e consulta de multas.
- **IPVAService**: cálculo do IPVA por veículo.
- **MqttUtil**: integração com broker MQTT (HiveMQ).

O sistema possui persistência de dados em arquivos `.txt` dentro da pasta `logs` e comunicação entre módulos via MQTT.

---

## Funcionalidades

### Menu principal

1. Cadastrar Condutor  
2. Emplacar Veículo  
3. Transferir Proprietário  
4. Lançar Multa  
5. Consultar Veículos por Ano  
6. Consultar Multas por Condutor  
7. Consultar Multas por Ano  
8. Consultar Multas por Veículo  
9. Ranking de Pontuação  
10. Calcular IPVA por Placa  
0. Sair

---

### Detalhes dos serviços

**CondutorService**  
- Cadastro de condutores (CPF e Nome).   
- Ranking dos 5 maiores pontuadores.  
- Publicação de eventos MQTT em `detran/condutor`.

**VeiculoService**  
- Registro de veículos (placa, modelo, valor, proprietário, ano).  
- Transferência de proprietário.  
- Consultas por ano ou por condutor.  
- Publicação de eventos MQTT em `detran/veiculo`.

**MultaService**  
- Lançamento de multas por condutor.  
- Consulta de multas por CPF, placa ou ano.  
- Publicação de eventos MQTT em `detran/multa`.

**IPVAService**  
- Cálculo de IPVA (2% do valor ou isento para veículos >20 anos).  
- Publicação de eventos MQTT em `detran/ipva`.

---

### Dependências

- Java 17+  
- [Eclipse Paho MQTT Client v3-1.2.5](https://www.eclipse.org/paho/index.php?page=clients/java/index.php)  
  - Para conectar ao broker MQTT (`broker.hivemq.com:1883`)

---

### MQTT

O sistema publica/consome mensagens nos seguintes tópicos:

Tópico&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;Serviço&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;Evento gerado &nbsp;  
-------------------------------------------------------------------------&nbsp;  
 detran/condutor&nbsp;|&nbsp;CondutorService&nbsp;|&nbsp;cadastro, pontuação  
 detran/veiculo&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;VeiculoService&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;emplacamento, transferência  
 detran/multa&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;MultaService&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;novas multas, consultas  
 detran/ipva&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;IPVAService&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;cálculo de IPVA  

---

### Formato das mensagens: **JSON**

Exemplo de payload:

{  
  "acao": "emplacar",  
  "placa": "XYZ1234",  
  "modelo": "Ford",  
  "valor": 30000,  
  "cpf": "98765432100",  
  "ano": 2023  
}

---

### Diagrama de Funcionamento

![Diagrama de funcionamento](https://drive.usercontent.google.com/download?id=1CLxuSxn0yHAhs4PZ3Ghnatc4ILgP41Z2)

---

## Estrutura de Pastas

Denatran/  
│  
├─ bin/ # _Arquivos compilados_  
│ ├─ CondutorService.class  
│ ├─ VeiculoService.class  
│ ├─ MultaService.class  
│ ├─ IPVAService.class  
│ ├─ MqttUtil.class  
│ └─ Main.class  
│  
├─ logs/ # _Dados persistentes_  
│ ├─ condutores.txt  
│ ├─ multas.txt  
│ └─ veiculos.txt  
│  
├─ jar/ # _Bibliotecas externas_  
│ └─ org.eclipse.paho.client.mqttv3-1.2.5.jar  
│  
├─ src/ # _Código fonte_  
│ ├─ CondutorService.java  
│ ├─ VeiculoService.java  
│ ├─ MultaService.java  
│ ├─ IPVAService.java  
│ ├─ MqttUtil.java  
│ └─ Main.java  
│  
├─ compile.bat # _Script para compilar_  
└─ run.bat # _Script para executar_  

---

## Como Compilar e Executar

Certifique-se de que o `.jar` do Paho MQTT está no caminho `jar/`.  
                                                                                                                                                                         
Rode os scripts que já estão prontos:

### 1. compile.bat → compila
### 2. run.bat → executa o sistema e conecta ao MQTT automaticamente

---

## Links Importantes

**Repositório do Projeto**: [Link para o Repositório no GitHub](https://github.com/serenesinister/denatran)
**Repositório de Entrega do projeto**: [Link para o Repositório de Entrega](https://github.com/rafael-dcomp/atividade-02-u1-detran-mqtt-projeto_sd/tree/main/Denatran)
**Vídeo do Projeto**: [Assista à apresentação e demonstração do projeto no YouTube](https://youtu.be/eUciF2eCX3s)

---

Desenvolvido por **Wemerson Soares**.
