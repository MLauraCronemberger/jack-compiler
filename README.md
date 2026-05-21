# 🧠 Jack Compiler — Analisador Léxico e Sintático (Nand2Tetris)

Este projeto implementa um **compilador** para a linguagem **Jack**, proposta no projeto **Nand2Tetris**.

Desenvolvido em **Java**, o sistema cobre os dois primeiros estágios da compilação:

- **Análise Léxica (Scanner):** lê arquivos `.jack`, reconhece tokens e exporta em XML
- **Análise Sintática (Parser):** consome os tokens e gera a árvore sintática em XML, seguindo a gramática oficial Jack

A saída de ambos os estágios é validada contra os arquivos XML oficiais do nand2tetris.

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
│   │           │   └── Parser.java                🔹 Parser seguindo a gramática oficial Jack
│   │           │
│   │           └── xml/                           🔹 Geração da saída XML
│   │               ├── XmlGenerator.java          🔹 XML do Scanner
│   │               └── XmlParserGenerator.java    🔹 XML do Parser
│   │
│   └── test/
│       ├── java/
│       │   └── br/com/jackcompiler/
│       │       ├── ScannerTest.java               🔹 Testes unitários do analisador léxico (JUnit 5)
│       │       └── ParserTest.java                🔹 Testes unitários do analisador sintático (JUnit 5)
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
├── output/          🔹 XMLs gerados pelo compilador (criados em tempo de execução)
├── pom.xml          🔹 Configuração Maven (dependências, build, plugins)
├── README.md
└── .gitignore
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

### 🔹 Modo 4 — Rodar os testes unitários (JUnit 5)

```bash
mvn test
```

Saída esperada:

```
Tests run: 59, Failures: 0, Errors: 0, Skipped: 0

Results:
Tests run: 59, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS
```

Os testes cobrem:

| Classe | Testes | O que cobre |
|---|---|---|
| `ScannerTest` | 23 | Keywords, identificadores, inteiros, strings, símbolos, comentários, whitespace, escape XML, validação oficial |
| `ParserTest` | 36 | Estrutura de classe, subrotinas, todos os statements, expressões, erros sintáticos, validação oficial |

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
