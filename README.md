# 🧠 Jack Compiler - Analisador Léxico (Nand2Tetris)

Este projeto implementa um **analisador léxico (lexer)** para a linguagem **Jack**, proposta no projeto **Nand2Tetris**.

O sistema, feito na linguagem **Java**, lê arquivos `.jack`, gera seus respectivos tokens e exporta a saída em formato **XML**, seguindo o padrão oficial do projeto.

---

## 🎯 Objetivo

Este projeto faz parte da construção de um compilador completo para a linguagem Jack, sendo o **primeiro estágio: análise léxica**.

---

## 📁 Estrutura do Projeto

A organização do projeto está dividida da seguinte forma:

```
jack-compiler/
│
├── expected-output-nand2tetris/   🔹XMLs oficiais do nand2tetris usados como referência para validação
│   ├── MainT.xml
│   ├── SquareT.xml
│   └── SquareGameT.xml
│
├── output/                        🔹XMLs gerados pelo compilador para comparação e validação
│   └── .gitkeep                   🔹Mantém a pasta versionada mesmo vazia
│
├── src/
│   ├── main/
│   │   ├── Main.java              🔹Ponto de entrada para execução manual via terminal
│   │   │
│   │   ├── lexer/                 🔹Implementação do analisador léxico
│   │   │   ├── Scanner.java
│   │   │   ├── Token.java
│   │   │   └── TokenType.java
│   │   │
│   │   └── xml/                   🔹Responsável pela geração da saída em XML
│   │       └── XmlGenerator.java
│   │
│   └── test/
│       ├── FilesAndValidationRunner.java  🔹Executa o compilador e valida a saída com os XMLs de referência
│       ├── LexerTest.java                 🔹Testes unitários do analisador léxico
│       │
│       └── resources-jack/               🔹Arquivos .jack usados como entrada nos testes
│           ├── Main.jack
│           ├── Square.jack
│           └── SquareGame.jack
│
├── dockerfile                    🔹Configuração do ambiente Docker para execução simplificada
├── docker-compose.yml            🔹Orquestração dos containers (se necessário)
├── .gitignore                    🔹Arquivos e pastas ignorados pelo Git
└── README.md                     🔹Documentação do projeto
```

---

## ⚙️ Funcionalidades

✔ Tokenização completa da linguagem Jack  
✔ Identificação de:  

* Keywords
* Symbols
* Integer constants
* String constants
* Identifiers

✔ Geração de saída XML no padrão oficial  
✔ Validação com arquivos do Nand2Tetris  

---

## 🧪 Sobre os testes

O projeto utiliza três abordagens:

* **LexerTest** → valida manualmente os tokens
* **FilesAndValidationRunner** → valida com o padrão oficial
* **Main** → execução livre (modo usuário)

---

## 🚀 Como executar

### 🐳 Via Docker (recomendado)
* Não é necessário ter Java instalado nem se preocupar com versões.
* Pré-requisito: ter o **Docker Desktop** instalado e aberto.


#### 🔹 1. Build da imagem (primeira vez ou após alterar o código)

```bash
docker compose build
```

---

#### 🔹 2.  Rodar testes do Lexer — exibe os tokens gerados no console:

```bash
docker compose run --rm lexer
```
---

#### 🔹 3. Validar com o Nand2Tetris:

Este teste:

* Lê os arquivos `.jack` oficiais 
* Gera os XMLs com base no compilador feito
* Compara com os XMLs oficiais e valida

```bash
docker compose up validation
```
---

#### 🔹4. Compilar um arquivo .jack manualmente
* Os XMLs gerados são salvos na pasta output/ do projeto.
* Exemplo:

```bash
docker compose run --rm compiler src/test/java/br/com/jackcompiler/Main.jack output/MainT.xml
```

---


### ☕ Localmente (requer Java 21+)


#### 🔹 1. Rodar testes do Lexer (visualização simples)

Exibe os tokens teste gerados em XML no console:

```bash
Remove-Item -Recurse -Force bin

mkdir bin

javac -d bin (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })

java -cp bin br.com.jackcompiler.LexerTest
```

---

#### 🔹 2. Gerar XML e validar com o Nand2Tetris

Este teste lê os .jack oficiais, gera os XMLs e compara com o gabarito do Nand2Tetris.

```bash
Remove-Item -Recurse -Force bin

mkdir bin

javac -d bin (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })

java -cp bin br.com.jackcompiler.FilesAndValidationRunner
```

---

#### 🔹 3. Gerar XML de qualquer arquivo `.jack`

Permite usar o compilador manualmente via terminal:

```bash
Remove-Item -Recurse -Force bin

javac -d bin (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })

java -cp bin br.com.jackcompiler.Main <caminho.arquivo.jack> <output/nome-arquivo.xml>
```

#### ✅ Exemplo:

```bash
java -cp bin br.com.jackcompiler.Main src/test/resources-jack/Main.jack output/MainT.xml
```

Saída esperada:

```
XML gerado: output/MainT.xml
```

---


## 📌 Observações

* O diretório `output/` contém apenas arquivos produzidos pelo próprio compilador
* Os arquivos em `nand2tetris/` são a referência oficial de validação

---

## 👥 Créditos

**Aluna:** Maria Laura Rangel Urbano Cronemberger  
**Disciplina:** EECP0026 — Compiladores  
**Professor:** Prof. Dr. Sergio Souza Costa  
**Instituição:** UFMA — Universidade Federal do Maranhão  
**Semestre:** 2026.1 

---

<div align="center">

**Este repositório possui fins acadêmicos.**

</div>

---
