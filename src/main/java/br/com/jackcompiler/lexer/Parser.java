package br.com.jackcompiler.lexer;

import java.util.List;
import br.com.jackcompiler.xml.XmlParserGenerator;
import br.com.jackcompiler.compiler.SymbolTable;
import br.com.jackcompiler.compiler.SymbolTable.Kind;
import br.com.jackcompiler.compiler.SymbolTable.Symbol;
import br.com.jackcompiler.compiler.VMWriter;
import br.com.jackcompiler.compiler.VMWriter.Segment;
import br.com.jackcompiler.compiler.VMWriter.Command;

public class Parser {
    private List<Token> tokens;
    private int current;
    private XmlParserGenerator xml;

    // >>> ADICIONADO: ferramentas do gerador de código
    private SymbolTable symbolTable = new SymbolTable();
    private VMWriter vmWriter = new VMWriter();
    private String className = "";      // guarda "Main", "Point", etc.
    private int ifLabelNum = 0;         // garante labels únicos: IF_TRUE0, IF_TRUE1...
    private int whileLabelNum = 0;      // garante labels únicos: WHILE_EXP0, WHILE_EXP1...

    public Parser(List<Token> tokens, XmlParserGenerator xml) {
        this.tokens = tokens;
        this.current = 0;
        this.xml = xml;
    }

    // >>> ADICIONADO: retorna o código VM gerado (chamado no Main para gravar o .vm)
    public String getVMOutput() {
        return vmWriter.getOutput();
    }

    // >>> ADICIONADO: converte Kind da SymbolTable para Segment da VM
    // FIELD  → THIS   (campos do objeto ficam no segmento this)
    // STATIC → STATIC
    // VAR    → LOCAL
    // ARG    → ARG
    private Segment kindToSegment(Kind kind) {
        switch (kind) {
            case FIELD:  return Segment.THIS;
            case STATIC: return Segment.STATIC;
            case VAR:    return Segment.LOCAL;
            case ARG:    return Segment.ARG;
            default: throw new RuntimeException("Kind inválido: " + kind);
        }
    }

// ==================== TOKEN UTILS ====================
// (nenhuma alteração aqui)

    private Token peek() {
        if (current < tokens.size()) {
            return tokens.get(current);
        }
        return null;
    }

    private Token advance() {
        if (current < tokens.size()) {
            return tokens.get(current++);
        }
        return null;
    }

    private boolean check(TokenType type) {
        Token token = peek();
        if (token == null) return false;
        return token.getType() == type;
    }

    private boolean match(TokenType type) {
        if (check(type)) {
            xml.writeToken(advance());
            return true;
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            Token token = advance();
            xml.writeToken(token);
            return token;
        }
        Token token = peek();
        throw new RuntimeException(
            message + " | Encontrado: " +
            (token != null ? token.getLexeme() : "EOF")
        );
    }

    private boolean checkKeyword(String word) {
        Token t = peek();
        return t != null && t.getType() == TokenType.KEYWORD && t.getLexeme().equals(word);
    }

    private Token consumeKeyword(String word) {
        Token t = peek();
        if (t != null && t.getType() == TokenType.KEYWORD && t.getLexeme().equals(word)) {
            Token token = advance();
            xml.writeToken(token);
            return token;
        }
        throw new RuntimeException(
            "Esperado keyword '" + word + "' | Encontrado: " +
            (t != null ? t.getLexeme() : "EOF")
        );
    }

    private boolean checkSymbol(String symbol) {
        Token t = peek();
        return t != null && t.getType() == TokenType.SYMBOL && t.getLexeme().equals(symbol);
    }

    private Token consumeSymbol(String symbol) {
        Token t = peek();
        if (t != null && t.getType() == TokenType.SYMBOL && t.getLexeme().equals(symbol)) {
            Token token = advance();
            xml.writeToken(token);
            return token;
        }
        throw new RuntimeException(
            "Esperado símbolo '" + symbol + "' | Encontrado: " +
            (t != null ? t.getLexeme() : "EOF")
        );
    }

    private void advanceAndWrite() {
        Token t = advance();
        xml.writeToken(t);
    }

// ==================== GRAMMAR HELPERS ====================
// (nenhuma alteração aqui)

    private void parseType() {
        Token t = peek();
        if (t == null) {
            throw new RuntimeException("Tipo esperado, encontrado EOF");
        }
        if (t.getType() == TokenType.KEYWORD &&
            (t.getLexeme().equals("int") || t.getLexeme().equals("char") || t.getLexeme().equals("boolean"))) {
            xml.writeToken(advance());
        } else if (t.getType() == TokenType.IDENTIFIER) {
            xml.writeToken(advance());
        } else {
            throw new RuntimeException("Tipo esperado, encontrado: " + t.getLexeme());
        }
    }

    private boolean isKeywordConstant(Token t) {
        if (t.getType() != TokenType.KEYWORD) return false;
        String l = t.getLexeme();
        return l.equals("true") || l.equals("false") ||
               l.equals("null") || l.equals("this");
    }


// ==================== EXPRESSIONS ====================

    private void parseTerm() {
        xml.openTag("term");
        Token t = peek();

        if (t.getType() == TokenType.INTEGER_CONSTANT) {
            advanceAndWrite();
            // >>> ADICIONADO: número inteiro → push constant N
            vmWriter.writePush(Segment.CONST, Integer.parseInt(t.getLexeme()));

        } else if (t.getType() == TokenType.STRING_CONSTANT) {
            advanceAndWrite();
            // >>> ADICIONADO: string → aloca com String.new e adiciona char por char
            String str = t.getLexeme();
            vmWriter.writePush(Segment.CONST, str.length());
            vmWriter.writeCall("String.new", 1);
            for (char c : str.toCharArray()) {
                vmWriter.writePush(Segment.CONST, (int) c);
                vmWriter.writeCall("String.appendChar", 2);
            }

        } else if (isKeywordConstant(t)) {
            advanceAndWrite();
            // >>> ADICIONADO: true/false/null/this
            switch (t.getLexeme()) {
                case "true":
                    vmWriter.writePush(Segment.CONST, 0);
                    vmWriter.writeArithmetic(Command.NOT); // true = ~0 = -1
                    break;
                case "false":
                case "null":
                    vmWriter.writePush(Segment.CONST, 0);
                    break;
                case "this":
                    vmWriter.writePush(Segment.POINTER, 0);
                    break;
            }

        } else if (checkSymbol("(")) {
            consumeSymbol("(");
            parseExpression();
            consumeSymbol(")");
            // sem geração de código aqui: a expressão dentro já gerou

        } else if (checkSymbol("-") || checkSymbol("~")) {
            String op = t.getLexeme(); // >>> ADICIONADO: guarda o operador antes de avançar
            advanceAndWrite();
            parseTerm();
            // >>> ADICIONADO: operador unário → neg ou not
            if (op.equals("-")) vmWriter.writeArithmetic(Command.NEG);
            else                 vmWriter.writeArithmetic(Command.NOT);

        } else if (t.getType() == TokenType.IDENTIFIER) {
            Token next = tokens.get(current + 1);

            if (next.getLexeme().equals("[")) {
                // >>> ADICIONADO: array[i] → calcula endereço e lê valor
                Token varToken = advance();
                xml.writeToken(varToken);
                Symbol sym = symbolTable.resolve(varToken.getLexeme());

                consumeSymbol("[");
                parseExpression();                                      // empilha o índice
                consumeSymbol("]");

                vmWriter.writePush(kindToSegment(sym.kind()), sym.index()); // empilha base do array
                vmWriter.writeArithmetic(Command.ADD);                      // base + índice
                vmWriter.writePop(Segment.POINTER, 1);                      // that = endereço
                vmWriter.writePush(Segment.THAT, 0);                        // empilha valor

            } else if (next.getLexeme().equals("(") || next.getLexeme().equals(".")) {
                parseSubroutineCall();

            } else {
                // >>> ADICIONADO: variável simples → push do segmento correto
                Token varToken = advance();
                xml.writeToken(varToken);
                Symbol sym = symbolTable.resolve(varToken.getLexeme());
                if (sym != null) {
                    vmWriter.writePush(kindToSegment(sym.kind()), sym.index());
                }
            }
        }

        xml.closeTag("term");
    }

    private void parseExpression() {
        xml.openTag("expression");
        parseTerm();

        while (checkSymbol("+") || checkSymbol("-") || checkSymbol("*") ||
               checkSymbol("/") || checkSymbol("&") || checkSymbol("|") ||
               checkSymbol("<") || checkSymbol(">") || checkSymbol("=")) {

            String op = peek().getLexeme(); // >>> ADICIONADO: guarda operador antes de avançar
            advanceAndWrite();
            parseTerm();

            // >>> ADICIONADO: emite o comando VM do operador
            switch (op) {
                case "+": vmWriter.writeArithmetic(Command.ADD); break;
                case "-": vmWriter.writeArithmetic(Command.SUB); break;
                case "*": vmWriter.writeCall("Math.multiply", 2); break;
                case "/": vmWriter.writeCall("Math.divide", 2);   break;
                case "&": vmWriter.writeArithmetic(Command.AND); break;
                case "|": vmWriter.writeArithmetic(Command.OR);  break;
                case "<": vmWriter.writeArithmetic(Command.LT);  break;
                case ">": vmWriter.writeArithmetic(Command.GT);  break;
                case "=": vmWriter.writeArithmetic(Command.EQ);  break;
            }
        }

        xml.closeTag("expression");
    }

    // >>> ADICIONADO: agora retorna int (quantidade de argumentos) em vez de void
    private int parseExpressionList() {
        xml.openTag("expressionList");
        int nArgs = 0;

        if (!checkSymbol(")")) {
            parseExpression();
            nArgs = 1;
            while (checkSymbol(",")) {
                consumeSymbol(",");
                parseExpression();
                nArgs++;
            }
        }

        xml.closeTag("expressionList");
        return nArgs; // >>> ADICIONADO: retorna contagem para writeCall
    }

    private void parseSubroutineCall() {
        Token nameToken = advance();  // >>> ADICIONADO: guarda o token (não só avança)
        xml.writeToken(nameToken);
        String name = nameToken.getLexeme();

        int nArgs = 0;
        String functionName;

        if (checkSymbol(".")) {
            // >>> ADICIONADO: Classe.funcao() ou objeto.metodo()
            consumeSymbol(".");
            Token methodToken = consume(TokenType.IDENTIFIER, "Esperado nome do método");
            String methodName = methodToken.getLexeme();

            Symbol sym = symbolTable.resolve(name);

            if (sym != null) {
                // 'name' é um objeto → método: empilha o objeto como 1º argumento
                functionName = sym.type() + "." + methodName;
                vmWriter.writePush(kindToSegment(sym.kind()), sym.index());
                nArgs = 1;
            } else {
                // 'name' é uma classe → função estática
                functionName = name + "." + methodName;
            }

            consumeSymbol("(");
            nArgs += parseExpressionList();
            consumeSymbol(")");

        } else {
            // >>> ADICIONADO: chamada sem ponto → método da própria classe
            // ex: draw() dentro da própria classe
            functionName = className + "." + name;
            consumeSymbol("(");
            vmWriter.writePush(Segment.POINTER, 0); // empilha 'this'
            nArgs = parseExpressionList() + 1;
            consumeSymbol(")");
        }

        vmWriter.writeCall(functionName, nArgs); // >>> ADICIONADO
    }


// ==================== STATEMENTS ====================

    private void parseReturn() {
        xml.openTag("returnStatement");
        consumeKeyword("return");

        if (!checkSymbol(";")) {
            parseExpression(); // a expressão já empilha o valor de retorno
        } else {
            vmWriter.writePush(Segment.CONST, 0); // >>> ADICIONADO: void → push 0 por convenção
        }

        consumeSymbol(";");
        vmWriter.writeReturn(); // >>> ADICIONADO
        xml.closeTag("returnStatement");
    }

    private void parseDo() {
        xml.openTag("doStatement");
        consumeKeyword("do");
        parseSubroutineCall();
        consumeSymbol(";");
        vmWriter.writePop(Segment.TEMP, 0); // >>> ADICIONADO: descarta retorno (do não usa valor)
        xml.closeTag("doStatement");
    }

    private void parseWhile() {
        xml.openTag("whileStatement");

        // >>> ADICIONADO: labels únicos para este while
        String labelExp = "WHILE_EXP" + whileLabelNum;
        String labelEnd = "WHILE_END" + whileLabelNum;
        whileLabelNum++;

        vmWriter.writeLabel(labelExp);    // >>> ADICIONADO: início do loop

        consumeKeyword("while");
        consumeSymbol("(");
        parseExpression();                // empilha resultado da condição
        consumeSymbol(")");

        vmWriter.writeArithmetic(Command.NOT); // >>> ADICIONADO: inverte: sai se condição falsa
        vmWriter.writeIf(labelEnd);            // >>> ADICIONADO: se ~condição, pula para o fim

        consumeSymbol("{");
        parseStatements();
        consumeSymbol("}");

        vmWriter.writeGoto(labelExp);  // >>> ADICIONADO: volta pro início
        vmWriter.writeLabel(labelEnd); // >>> ADICIONADO: fim do loop

        xml.closeTag("whileStatement");
    }

    private void parseIf() {
        xml.openTag("ifStatement");

        // >>> ADICIONADO: labels únicos para este if
        String labelTrue = "IF_TRUE"  + ifLabelNum;
        String labelFalse = "IF_FALSE" + ifLabelNum;
        String labelEnd   = "IF_END"   + ifLabelNum;
        ifLabelNum++;

        consumeKeyword("if");
        consumeSymbol("(");
        parseExpression();               // empilha resultado da condição
        consumeSymbol(")");

        // >>> ADICIONADO: saltos
        vmWriter.writeIf(labelTrue);     // se condição verdadeira, vai para IF_TRUE
        vmWriter.writeGoto(labelFalse);  // senão, vai para IF_FALSE
        vmWriter.writeLabel(labelTrue);  // bloco do if começa aqui

        consumeSymbol("{");
        parseStatements();
        consumeSymbol("}");

        if (checkKeyword("else")) {
            vmWriter.writeGoto(labelEnd);    // >>> ADICIONADO: pula o else
            vmWriter.writeLabel(labelFalse); // >>> ADICIONADO: bloco else começa aqui
            consumeKeyword("else");
            consumeSymbol("{");
            parseStatements();
            consumeSymbol("}");
            vmWriter.writeLabel(labelEnd);   // >>> ADICIONADO: fim do if-else
        } else {
            vmWriter.writeLabel(labelFalse); // >>> ADICIONADO: sem else, falso só pula o bloco
        }

        xml.closeTag("ifStatement");
    }

    private void parseLet() {
        xml.openTag("letStatement");
        consumeKeyword("let");

        Token varToken = consume(TokenType.IDENTIFIER, "Esperado nome de variável");
        Symbol sym = symbolTable.resolve(varToken.getLexeme()); // >>> ADICIONADO

        boolean isArray = false; // >>> ADICIONADO

        if (checkSymbol("[")) {
            // >>> ADICIONADO: let arr[i] = expr
            isArray = true;
            consumeSymbol("[");
            parseExpression();                                          // empilha índice
            consumeSymbol("]");
            vmWriter.writePush(kindToSegment(sym.kind()), sym.index()); // empilha base
            vmWriter.writeArithmetic(Command.ADD);                       // endereço = base + índice
        }

        consumeSymbol("=");
        parseExpression(); // empilha valor do lado direito

        // >>> ADICIONADO: armazena o valor
        if (isArray) {
            // precisa de temp porque a expressão do lado direito pode ter mudado 'that'
            vmWriter.writePop(Segment.TEMP, 0);     // guarda valor em temp
            vmWriter.writePop(Segment.POINTER, 1);  // that = endereço calculado
            vmWriter.writePush(Segment.TEMP, 0);    // recupera valor
            vmWriter.writePop(Segment.THAT, 0);     // armazena no array
        } else {
            vmWriter.writePop(kindToSegment(sym.kind()), sym.index()); // variável simples
        }

        consumeSymbol(";");
        xml.closeTag("letStatement");
    }

    private void parseStatement() {
        if      (checkKeyword("let"))    parseLet();
        else if (checkKeyword("if"))     parseIf();
        else if (checkKeyword("while"))  parseWhile();
        else if (checkKeyword("do"))     parseDo();
        else if (checkKeyword("return")) parseReturn();
    }

    private void parseStatements() {
        xml.openTag("statements");
        while (checkKeyword("let") || checkKeyword("if") ||
               checkKeyword("while") || checkKeyword("do") ||
               checkKeyword("return")) {
            parseStatement();
        }
        xml.closeTag("statements");
    }


// ==================== SUBROUTINES ====================

    private void parseVarDec() {
        xml.openTag("varDec");
        consumeKeyword("var");

        // >>> ADICIONADO: captura tipo e registra variáveis locais na SymbolTable
        Token typeToken = peek();
        parseType();
        String type = typeToken.getLexeme();

        Token nameToken = consume(TokenType.IDENTIFIER, "Esperado nome de variável");
        symbolTable.define(nameToken.getLexeme(), type, Kind.VAR); // >>> ADICIONADO

        while (checkSymbol(",")) {
            consumeSymbol(",");
            Token nextName = consume(TokenType.IDENTIFIER, "Esperado nome de variável após ','");
            symbolTable.define(nextName.getLexeme(), type, Kind.VAR); // >>> ADICIONADO
        }

        consumeSymbol(";");
        xml.closeTag("varDec");
    }

    private void parseParameterList() {
        xml.openTag("parameterList");

        if (!checkSymbol(")")) {
            // >>> ADICIONADO: captura tipo e registra argumentos na SymbolTable
            Token typeToken = peek();
            parseType();
            String type = typeToken.getLexeme();

            Token nameToken = consume(TokenType.IDENTIFIER, "Esperado nome do parâmetro");
            symbolTable.define(nameToken.getLexeme(), type, Kind.ARG); // >>> ADICIONADO

            while (checkSymbol(",")) {
                consumeSymbol(",");
                Token nextType = peek();
                parseType();
                type = nextType.getLexeme();

                Token nextName = consume(TokenType.IDENTIFIER, "Esperado nome do parâmetro");
                symbolTable.define(nextName.getLexeme(), type, Kind.ARG); // >>> ADICIONADO
            }
        }

        xml.closeTag("parameterList");
    }

    private void parseSubroutineBody(String functionName, String subroutineType) { // >>> ADICIONADO: parâmetros novos
        xml.openTag("subroutineBody");
        consumeSymbol("{");

        while (checkKeyword("var")) {
            parseVarDec();
        }

        // >>> ADICIONADO: só aqui sabemos quantas variáveis locais tem → escreve function
        int nLocals = symbolTable.varCount(Kind.VAR);
        vmWriter.writeFunction(functionName, nLocals);

        // >>> ADICIONADO: prologue do construtor — aloca memória para o objeto
        if (subroutineType.equals("constructor")) {
            int nFields = symbolTable.varCount(Kind.FIELD);
            vmWriter.writePush(Segment.CONST, nFields);
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(Segment.POINTER, 0); // this = novo objeto
        }

        // >>> ADICIONADO: prologue do método — aponta this para o objeto recebido
        if (subroutineType.equals("method")) {
            vmWriter.writePush(Segment.ARG, 0);
            vmWriter.writePop(Segment.POINTER, 0); // this = argumento 0
        }

        parseStatements();
        consumeSymbol("}");
        xml.closeTag("subroutineBody");
    }

    private void parseSubroutineDec() {
        xml.openTag("subroutineDec");

        // >>> ADICIONADO: reseta labels e escopo de subrotina
        ifLabelNum = 0;
        whileLabelNum = 0;
        symbolTable.startSubroutine();

        String subroutineType; // >>> ADICIONADO
        if (checkKeyword("constructor") || checkKeyword("function") || checkKeyword("method")) {
            subroutineType = peek().getLexeme(); // >>> ADICIONADO: "constructor", "function" ou "method"
            advanceAndWrite();
        } else {
            throw new RuntimeException("Esperado constructor, function ou method");
        }

        // >>> ADICIONADO: método recebe 'this' como argumento implícito (índice 0)
        if (subroutineType.equals("method")) {
            symbolTable.define("this", className, Kind.ARG);
        }

        if (checkKeyword("void")) {
            advanceAndWrite();
        } else {
            parseType();
        }

        Token nameToken = consume(TokenType.IDENTIFIER, "Esperado nome da subrotina");
        String functionName = className + "." + nameToken.getLexeme(); // >>> ADICIONADO

        consumeSymbol("(");
        parseParameterList();
        consumeSymbol(")");

        parseSubroutineBody(functionName, subroutineType); // >>> ADICIONADO: passa parâmetros

        xml.closeTag("subroutineDec");
    }


// ==================== CLASS ====================

    private void parseClassVarDec() {
        xml.openTag("classVarDec");

        // >>> ADICIONADO: captura kind (field ou static)
        String kindStr = peek().getLexeme();
        Kind kind = kindStr.equals("field") ? Kind.FIELD : Kind.STATIC;

        if (checkKeyword("static") || checkKeyword("field")) {
            advanceAndWrite();
        } else {
            throw new RuntimeException("Esperado 'static' ou 'field'");
        }

        // >>> ADICIONADO: captura tipo
        Token typeToken = peek();
        parseType();
        String type = typeToken.getLexeme();

        Token nameToken = consume(TokenType.IDENTIFIER, "Esperado nome de variável");
        symbolTable.define(nameToken.getLexeme(), type, kind); // >>> ADICIONADO

        while (checkSymbol(",")) {
            consumeSymbol(",");
            Token nextName = consume(TokenType.IDENTIFIER, "Esperado nome de variável após ','");
            symbolTable.define(nextName.getLexeme(), type, kind); // >>> ADICIONADO
        }

        consumeSymbol(";");
        xml.closeTag("classVarDec");
    }

    public void parseClass() {
        xml.openTag("class");
        consumeKeyword("class");

        Token classNameToken = consume(TokenType.IDENTIFIER, "Esperado nome da classe");
        className = classNameToken.getLexeme(); // >>> ADICIONADO: guarda nome da classe

        consumeSymbol("{");

        while (checkKeyword("static") || checkKeyword("field")) {
            parseClassVarDec();
        }

        while (checkKeyword("constructor") || checkKeyword("function") || checkKeyword("method")) {
            parseSubroutineDec();
        }

        consumeSymbol("}");
        xml.closeTag("class");
    }
}