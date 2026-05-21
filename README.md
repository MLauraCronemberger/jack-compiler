# 🧠 Jack Compiler — Compilador para a Linguagem Jack (Nand2Tetris)

Este projeto implementa um **compilador completo** para a linguagem **Jack**, proposta no projeto **Nand2Tetris**.

Desenvolvido em **Java**, o sistema cobre todos os estágios da compilação — da leitura do código-fonte até a geração de código intermediário executável na máquina virtual do nand2tetris:

- **Análise Léxica (Scanner):** lê arquivos `.jack`, reconhece tokens e exporta em XML
- **Análise Sintática (Parser):** consome os tokens e verifica a estrutura do programa seguindo a gramática oficial Jack, gerando a árvore sintática em XML
- **Tabela de Símbolos (SymbolTable):** registra cada variável declarada com seu tipo, escopo e posição de memória — a memória do compilador sobre o programa
- **Geração de Código VM (VMWriter):** traduz cada construção do programa Jack em instruções da linguagem intermediária `.vm`, compatível com o VM Emulator oficial do curso

A saída dos estágios de análise é validada contra os arquivos XML oficiais do nand2tetris. A saída do gerador de código é validada executando os programas do **Project 11** no VM Emulator.

---

## 📁 Estrutura do Projeto

```
jack-compiler/
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── br/com/jackcompiler/
│   │           ├── Main.java                      🔹 Ponto de entrada (execução manual e testes integrados)
│   │           ├── FilesAndValidationRunner.java  🔹 Roda e valida scanner + parser contra o gabarito oficial
│   │           │
│   │           ├── lexer/                         🔹 Analisador léxico e sintático
│   │           │   ├── TokenType.java             🔹 Enum dos tipos de token
│   │           │   ├── Token.java                 🔹 Representação de um token
│   │           │   ├── Scanner.java               🔹 Tokenizador da linguagem Jack
│   │           │   └── Parser.java                🔹 Parser + gerador de código VM integrado
│   │           │
│   │           ├── compiler/                      🔹 Geração de código intermediário (VM)
│   │           │   ├── SymbolTable.java           🔹 Tabela de símbolos com escopos de classe e subrotina
│   │           │   └── VMWriter.java              🔹 Emissor de comandos da linguagem VM
│   │           │
│   │           └── xml/                           🔹 Geração da saída XML
│   │               ├── XmlGenerator.java          🔹 XML do Scanner
│   │               └── XmlParserGenerator.java    🔹 XML do Parser
│   │
│   └── test/
│       ├── java/
│       │   └── br/com/jackcompiler/
│       │       ├── ScannerTest.java               🔹 Testes unitários do analisador léxico (JUnit 5)
│       │       ├── ParserTest.java                🔹 Testes unitários do analisador sintático (JUnit 5)
│       │       ├── SymbolTableTest.java           🔹 Testes unitários da tabela de símbolos (JUnit 5)
│       │       └── VMWriterTest.java              🔹 Testes unitários do emissor de código VM (JUnit 5)
│       │
│       └── resources/
│           ├── expected-output-nand2tetris/       🔹 XMLs oficiais usados como gabarito
│           │   ├── MainT.xml / MainP.xml
│           │   ├── SquareT.xml / SquareP.xml
│           │   └── SquareGameT.xml / SquareGameP.xml
│           │
│           └── resources-jack/                    🔹 Arquivos .jack de entrada
│               ├── Main.jack
│               ├── Square.jack
│               └── SquareGame.jack
│
├── projects/
│   └── 11/                                        🔹 Programas oficiais do Project 11 (nand2tetris)
│       ├── Seven/         └── Main.jack
│       ├── Average/       └── Main.jack
│       ├── ConvertToBin/  └── Main.jack
│       ├── ComplexArrays/ └── Main.jack
│       ├── Square/        ├── Main.jack, Square.jack, SquareGame.jack
│       └── Pong/          ├── Ball.jack, Bat.jack, Main.jack, PongGame.jack
│
├── output/          🔹 XMLs gerados pelo compilador (criados em tempo de execução)
├── pom.xml          🔹 Configuração Maven (dependências, build, plugins)
├── README.md
└── .gitignore
```
---

## 🔍 Como o compilador funciona
 
O código-fonte Jack passa por três estágios em sequência antes de virar código executável:
 
```
Arquivo .jack
     │
     ▼
┌─────────────────────────────────────────────┐
│  1. SCANNER (Analisador Léxico)             │
│                                             │
│  Lê o texto caractere por caractere e       │
│  agrupa em tokens com tipo e valor.         │
│                                             │
│  "let x = 10 + y;"                         │
│   → [let] [x] [=] [10] [+] [y] [;]        │
└─────────────────┬───────────────────────────┘
                  │ lista de tokens
                  ▼
┌─────────────────────────────────────────────┐
│  2. PARSER (Analisador Sintático)           │
│                                             │
│  Consome os tokens e verifica se a          │
│  estrutura respeita a gramática Jack.       │
│  Percorre: classe → subrotinas →            │
│  statements → expressões                   │
│                                             │
│  Ao mesmo tempo, alimenta a SymbolTable     │
│  com cada variável declarada.               │
└──────┬──────────────────────┬───────────────┘
       │                      │
       ▼                      ▼
┌─────────────┐     ┌─────────────────────────┐
│ XML gerado  │     │  3. GERADOR DE CÓDIGO   │
│ (debug /    │     │                         │
│  gabarito)  │     │  SymbolTable            │
└─────────────┘     │  Guarda cada variável   │
                    │  com tipo, escopo e      │
                    │  índice de memória.      │
                    │                         │
                    │  VMWriter               │
                    │  Emite os comandos VM   │
                    │  para cada construção   │
                    │  encontrada pelo parser.│
                    └────────────┬────────────┘
                                 │
                                 ▼
                        Arquivo .vm gerado
                    (compatível com VM Emulator)
```
 
### O que cada parte faz
 
- **Scanner** — transforma texto bruto em tokens. Sabe distinguir keyword (`if`, `while`, `class`) de identificador (`x`, `Point`), número inteiro, string, e símbolo (`{`, `+`, `[`).
 
- **Parser** — percorre a sequência de tokens seguindo a gramática oficial Jack. Garante que a estrutura está correta (ex: todo `if` tem `(`, expressão, `)`, `{`, statements, `}`). É o maestro: chama o Scanner, a SymbolTable e o VMWriter.
 
- **SymbolTable** — funciona como dicionário de variáveis. Para cada variável declarada, guarda nome, tipo, categoria (`field`, `static`, `arg`, `local`) e índice. Tem dois escopos simultâneos: o da classe (dura a classe toda) e o da subrotina (reseta a cada método ou função). Quando o parser encontra uma variável numa expressão, consulta a SymbolTable para saber qual segmento e índice usar na VM.
 
- **VMWriter** — é a caneta. Não tem lógica de compilação — só sabe formatar as instruções da linguagem VM (`push`, `pop`, `call`, `label`, `if-goto`, etc.) e acumular tudo numa string que vira o arquivo `.vm`.
 
### Exemplo 
 
O seguinte código Jack:
```jack
let x = 10 + y;
```
 
Passa pelo Scanner e vira tokens. O Parser reconhece um `letStatement`. A SymbolTable diz que `x` é `local 0` e `y` é `local 1`. O VMWriter emite:
```
push constant 10
push local 1
add
pop local 0
```
 

---

## 🚀 Como executar

### Pré-requisitos

- Java 17+
- Maven 3.6+

### 🔹 Build

```bash
mvn clean package
```

---

### 🔹 Modo 1 — Rodar todos os testes de integração

Lê os três arquivos `.jack` oficiais, gera os XMLs de scanner e parser e compara com o gabarito:

```bash
java -jar target/jack-compiler.jar
```

Saída esperada:

```
Main.jack -> MainT.xml PASSED
Square.jack -> SquareT.xml PASSED
SquareGame.jack -> SquareGameT.xml PASSED
--- Parser ---
Main.jack -> MainP.xml PASSED
Square.jack -> SquareP.xml PASSED
SquareGame.jack -> SquareGameP.xml PASSED
6/6 testes passaram.
```

Os XMLs gerados ficam em `output/` com sufixo `-Teste`.

---

### 🔹 Modo 2 — Gerar XML do Scanner para um arquivo `.jack`

```bash
java -jar target/jack-compiler.jar <arquivo.jack> <saida.xml>
```

Exemplo:

```bash
java -jar target/jack-compiler.jar src/test/resources/resources-jack/Main.jack output/MainT.xml
```

Saída esperada:

```
XML gerado: output/MainT.xml
```

---

### 🔹 Modo 3 — Gerar XML do Parser para um arquivo `.jack`

```bash
java -jar target/jack-compiler.jar --parser <arquivo.jack> <saida.xml>
```

Exemplo:

```bash
java -jar target/jack-compiler.jar --parser src/test/resources/resources-jack/Main.jack output/MainP.xml
```

Saída esperada:

```
XML gerado: output/MainP.xml
```

---

### 🔹 Modo 4 — Compilar para código VM (arquivo único)
 
```bash
java -jar target/jack-compiler.jar --vm <arquivo.jack>
```
 
Exemplo:
 
```bash
java -jar target/jack-compiler.jar --vm projects/11/Seven/Main.jack
```
 
Gera `Main.vm` no mesmo diretório do `.jack`.
 
---
 
### 🔹 Modo 5 — Compilar para código VM (diretório inteiro)
 
```bash
java -jar target/jack-compiler.jar --vm <diretório>
```
 
Exemplos:
 
```bash
java -jar target/jack-compiler.jar --vm projects/11/Square/
java -jar target/jack-compiler.jar --vm projects/11/Pong/
```
 
Gera um `.vm` para cada `.jack` encontrado na pasta, no mesmo diretório.
 
---
 
### 🔹 Modo 6 — Rodar os testes unitários (JUnit 5)
 
```bash
mvn test
```
 
Saída esperada:
 
```
Tests run: 83, Failures: 0, Errors: 0, Skipped: 0
 
BUILD SUCCESS
```
 
Os testes cobrem:
 
| Classe | Testes | O que cobre |
|---|---|---|
| `ScannerTest` | 23 | Keywords, identificadores, inteiros, strings, símbolos, comentários, whitespace, escape XML, validação oficial |
| `ParserTest` | 36 | Estrutura de classe, subrotinas, todos os statements, expressões, erros sintáticos, validação oficial |
| `SymbolTableTest` | 8 | Escopos de classe e subrotina, índices, resolução de variáveis, reset entre subrotinas |
| `VMWriterTest` | 16 | Todos os comandos VM: push, pop, aritmética, labels, goto, call, function, return |
 
---

## ✅ Status de validação — Project 11
 
Todos os programas foram compilados com o gerador e executados no VM Emulator oficial do nand2tetris:
 
| Programa | Arquivos | Resultado |
|---|---|---|
| Seven | `Main.jack` | ✅ Passou — imprime 7 |
| Average | `Main.jack` | ✅ Passou |
| ConvertToBin | `Main.jack` | ✅ Passou |
| ComplexArrays | `Main.jack` | ✅ Passou |
| Square | `Main.jack`, `Square.jack`, `SquareGame.jack` | ✅ Passou |
| Pong | `Ball.jack`, `Bat.jack`, `Main.jack`, `PongGame.jack` | ✅ Passou |
 
---

## 📌 Observações

- O diretório `output/` contém apenas arquivos produzidos pelo compilador em tempo de execução
- Os arquivos em `expected-output-nand2tetris/` são o gabarito oficial e não devem ser modificados
- A comparação de XMLs normaliza espaços e quebras de linha antes de comparar, evitando falsos negativos por indentação

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
