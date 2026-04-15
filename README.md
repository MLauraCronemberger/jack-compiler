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
├── src/
│   ├── main/
│   │   └── java/
│   │       └── br/com/jackcompiler/
│   │           ├── Main.java                      🔹Ponto de entrada da aplicação (execução manual e testes)
│   │           ├── FilesAndValidationRunner.java  🔹Executa os testes e valida a saída com os XMLs de referência
│   │           │
│   │           ├── lexer/                         🔹Implementação do analisador léxico
│   │           │   ├── Scanner.java
│   │           │   ├── Token.java
│   │           │   └── TokenType.java
│   │           │
│   │           └── xml/                           🔹Responsável pela geração da saída em XML
│   │               └── XmlGenerator.java
│   │
│   └── test/
│       └── resources/
│           ├── expected-output-nand2tetris/       🔹XMLs oficiais do nand2tetris usados como referência para validação
│           │   ├── MainT.xml
│           │   ├── SquareT.xml
│           │   └── SquareGameT.xml
│           │
│           └── resources-jack/                    🔹Arquivos .jack usados como entrada nos testes
│               ├── Main.jack
│               ├── Square.jack
│               └── SquareGame.jack
│
│
├── output/                        🔹XMLs gerados pelo compilador para comparação e validação
│
├── pom.xml                        🔹Arquivo de configuração do Maven (build, compilação e execução)
│
├── README.md                      🔹Documentação do projeto
│
└── .gitignore                     🔹Arquivos e pastas ignorados pelo Git

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

O projeto utiliza duas abordagens:

* **FilesAndValidationRunner** → gera os tokens dos arquivos .jack e valida com o padrão oficial
* **Main** → execução livre (modo usuário)

---

## 🚀 Como executar

### ☕ Via Maven (recomendado)

* Não é necessário compilar manualmente com javac  
* O Maven gerencia build e empacotamento automaticamente  
* Pré-requisitos: ter `Java 17+` e `Maven` instalados  

#### 🔹 1. Build do projeto

```bash
mvn clean package
```

#### 🔹 2. Rodar todos os testes e validar com o Nand2Tetris

Lê os arquivos `.jack` oficiais, gera os XMLs e compara com o gabarito:

```bash
java -jar target/jack-compiler.jar
```

Saída esperada:

```
Main.jack -> MainT.xml PASSED
Square.jack -> SquareT.xml PASSED
SquareGame.jack -> SquareGameT.xml PASSED
3/3 arquivos validados com sucesso!
```

---

#### 🔹 3. Compilar um arquivo `.jack` manualmente

Permite usar o compilador em qualquer arquivo `.jack` via terminal:

```bash
java -jar target/jack-compiler.jar \
  src/test/resources/resources-jack/Main.jack output/MainT.xml
```

Saída esperada:

```
XML gerado: output/MainT.xml
```

> Os XMLs gerados são salvos na pasta `output/` do projeto.

---


## 📌 Observações

* O diretório `output/` contém apenas arquivos produzidos pelo próprio compilador
* Os arquivos em `expected-output-nand2tetris/` são a referência oficial de validação

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
