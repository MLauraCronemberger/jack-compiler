package br.com.jackcompiler.lexer;

import java.util.List;
import br.com.jackcompiler.xml.XmlParserGenerator;

public class Parser {
    private List<Token> tokens;
    private int current;
    private XmlParserGenerator xml;

    public Parser(List<Token> tokens, XmlParserGenerator xml) {
        this.tokens = tokens;
        this.current = 0;
        this.xml = xml;
}

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

// Verifica keyword por lexeme (não só por tipo)
private boolean checkKeyword(String word) {
    Token t = peek();
    return t != null && t.getType() == TokenType.KEYWORD && t.getLexeme().equals(word);
}

// Para consumir um tipo específico (int, char, boolean, ou className)
private void parseType() {
    Token t = peek();
    if (t == null) {
        throw new RuntimeException("Tipo esperado, encontrado EOF");
    }
    if (t.getType() == TokenType.KEYWORD &&
        (t.getLexeme().equals("int") || t.getLexeme().equals("char") || t.getLexeme().equals("boolean"))) {
        xml.writeToken(advance());
    } else if (t.getType() == TokenType.IDENTIFIER) {
        xml.writeToken(advance());  // className
    } else {
        throw new RuntimeException("Tipo esperado, encontrado: " + t.getLexeme());
    }
}

// Auxiliar: consume uma keyword específica pelo lexeme
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

// Auxiliar: consume um símbolo específico pelo lexeme
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

// Auxiliar: checa se o símbolo atual bate com o esperado (sem avançar)
private boolean checkSymbol(String symbol) {
    Token t = peek();
    return t != null && t.getType() == TokenType.SYMBOL && t.getLexeme().equals(symbol);
}

private void advanceAndWrite() {
    Token t = advance();
    xml.writeToken(t);
}

//AGORA METODOS DO PARSER PROPRIAMENTE DITO

public void parseClass() {
    xml.openTag("class");
    consumeKeyword("class");   // class
    consume(TokenType.IDENTIFIER, "Esperado nome da classe"); // className
    consumeSymbol("{");        // {

    while (checkKeyword("static") || checkKeyword("field")) {
        parseClassVarDec();
    }

    while (checkKeyword("constructor") || checkKeyword("function") || checkKeyword("method")) {
        parseSubroutineDec();
    }

    System.out.println("TOKEN ATUAL: " + peek().getLexeme());
    consumeSymbol("}");        // }
    xml.closeTag("class");
}



// Regra: ('static' | 'field') type varName (',' varName)* ';'
private void parseClassVarDec() {
    xml.openTag("classVarDec");

    // 'static' ou 'field'
    if (checkKeyword("static") || checkKeyword("field")) {
    advanceAndWrite();
} else {
    throw new RuntimeException("Esperado 'static' ou 'field'");
}

    // type: int | char | boolean | className
    parseType();

    // varName (obrigatório, pelo menos um)
    consume(TokenType.IDENTIFIER, "Esperado nome de variável");

    // (',' varName)*  — pode haver mais de uma variável na mesma linha
    while (checkSymbol(",")) {
        consumeSymbol(",");
        consume(TokenType.IDENTIFIER, "Esperado nome de variável após ','");
    }

    consumeSymbol(";");
    xml.closeTag("classVarDec");
}

// Regra: ('constructor'|'function'|'method') ('void'|type) subroutineName '(' parameterList ')' subroutineBody
private void parseSubroutineDec() {
    xml.openTag("subroutineDec");

    // 'constructor' | 'function' | 'method'
    if (checkKeyword("constructor") || checkKeyword("function") || checkKeyword("method")) {
    advanceAndWrite();
    } else {
    throw new RuntimeException("Esperado constructor, function ou method");
}

    // 'void' | type
    if (checkKeyword("void")) {
        advanceAndWrite();
    } else {
        parseType();
    }

    // subroutineName (é um identifier)
    consume(TokenType.IDENTIFIER, "Esperado nome da subrotina");

    consumeSymbol("(");
    parseParameterList();   // stub por enquanto
    consumeSymbol(")");

    parseSubroutineBody();  // stub por enquanto

    xml.closeTag("subroutineDec");
}

// ── STUBS para a Fase 2 ─────────────────────────────────────────────────────

private void parseSubroutineBody() {
    xml.openTag("subroutineBody");
    consumeSymbol("{"); // <-- ISSO FALTAVA

        // zero ou mais declarações de variável
    while (checkKeyword("var")) {
        parseVarDec();
    }

    parseStatements(); // precisa existir
    consumeSymbol("}"); // <-- ISSO TAMBÉM
    xml.closeTag("subroutineBody");
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

private void parseStatement() {
    if (checkKeyword("let"))    parseLet();
    else if (checkKeyword("if"))     parseIf();
    else if (checkKeyword("while"))  parseWhile();
    else if (checkKeyword("do"))     parseDo();
    else if (checkKeyword("return")) parseReturn();
}


private void parseReturn() {
    xml.openTag("returnStatement");

    consumeKeyword("return");

    // expressão opcional: se não for ';', tem expressão
    if (!checkSymbol(";")) {
        parseExpression(); // stub Fase 4
    }

    consumeSymbol(";");
    xml.closeTag("returnStatement");
}

private void parseParameterList() {
    xml.openTag("parameterList");

    // Se o próximo token não é ')', há parâmetros
    if (!checkSymbol(")")) {
        parseType();
        consume(TokenType.IDENTIFIER, "Esperado nome do parâmetro");

        while (checkSymbol(",")) {
            consumeSymbol(",");
            parseType();
            consume(TokenType.IDENTIFIER, "Esperado nome do parâmetro");
        }
    }

    xml.closeTag("parameterList");
}

private void parseVarDec() {
    xml.openTag("varDec");

    consumeKeyword("var");
    parseType();
    consume(TokenType.IDENTIFIER, "Esperado nome de variável");

    while (checkSymbol(",")) {
        consumeSymbol(",");
        consume(TokenType.IDENTIFIER, "Esperado nome de variável após ','");
    }

    consumeSymbol(";");
    xml.closeTag("varDec");
}

private void parseLet() {
    xml.openTag("letStatement");

    consumeKeyword("let");
    consume(TokenType.IDENTIFIER, "Esperado nome de variável");

    // acesso a array opcional: '[' expression ']'
    if (checkSymbol("[")) {
        consumeSymbol("[");
        parseExpression(); // stub Fase 4
        consumeSymbol("]");
    }

    consumeSymbol("=");
    parseExpression(); // stub Fase 4
    consumeSymbol(";");

    xml.closeTag("letStatement");
}

private void parseIf() {
    xml.openTag("ifStatement");

    consumeKeyword("if");
    consumeSymbol("(");
    parseExpression(); // stub Fase 4
    consumeSymbol(")");
    consumeSymbol("{");
    parseStatements();
    consumeSymbol("}");

    // else é opcional
    if (checkKeyword("else")) {
        consumeKeyword("else");
        consumeSymbol("{");
        parseStatements();
        consumeSymbol("}");
    }

    xml.closeTag("ifStatement");
}

private void parseWhile() {
    xml.openTag("whileStatement");

    consumeKeyword("while");
    consumeSymbol("(");
    parseExpression(); // stub Fase 4
    consumeSymbol(")");
    consumeSymbol("{");
    parseStatements();
    consumeSymbol("}");

    xml.closeTag("whileStatement");
}

private void parseDo() {
    xml.openTag("doStatement");

    consumeKeyword("do");
    parseSubroutineCall(); // stub Fase 4
    consumeSymbol(";");

    xml.closeTag("doStatement");
}

private void parseExpression() {
    xml.openTag("expression");

    parseTerm();

    // op: + - * / & | < > =
    while (checkSymbol("+") || checkSymbol("-") || checkSymbol("*") ||
           checkSymbol("/") || checkSymbol("&") || checkSymbol("|") ||
           checkSymbol("<") || checkSymbol(">") || checkSymbol("=")) {
        advanceAndWrite(); // consome o operador
        parseTerm();
    }

    xml.closeTag("expression");
}

private void parseSubroutineCall() {
    // consome: name ( '(' expressionList ')' | '.' name '(' expressionList ')' )
    consume(TokenType.IDENTIFIER, "Esperado nome");

    if (checkSymbol(".")) {
        consumeSymbol(".");
        consume(TokenType.IDENTIFIER, "Esperado nome do método");
    }

    consumeSymbol("(");
    parseExpressionList(); // stub
    consumeSymbol(")");
}


private void parseExpressionList() {
    xml.openTag("expressionList");

    if (!checkSymbol(")")) {
        parseExpression();

        while (checkSymbol(",")) {
            consumeSymbol(",");
            parseExpression();
        }
    }

    xml.closeTag("expressionList");
}


private void parseTerm() {
    xml.openTag("term");

    Token t = peek();

    if (t.getType() == TokenType.INTEGER_CONSTANT) {
        advanceAndWrite();

    } else if (t.getType() == TokenType.STRING_CONSTANT) {
        advanceAndWrite();

    } else if (isKeywordConstant(t)) {
        // true | false | null | this
        advanceAndWrite();

    } else if (checkSymbol("(")) {
        // '(' expression ')'
        consumeSymbol("(");
        parseExpression();
        consumeSymbol(")");

    } else if (checkSymbol("-") || checkSymbol("~")) {
        // unaryOp term
        advanceAndWrite();
        parseTerm();

    } else if (t.getType() == TokenType.IDENTIFIER) {
        // LOOKAHEAD: olha o token seguinte sem avançar
        Token next = tokens.get(current + 1);

        if (next.getLexeme().equals("[")) {
            // varName '[' expression ']'
            advanceAndWrite();           // varName
            consumeSymbol("[");
            parseExpression();
            consumeSymbol("]");

        } else if (next.getLexeme().equals("(") || next.getLexeme().equals(".")) {
            // subroutineCall
            parseSubroutineCall();

        } else {
            // varName simples
            advanceAndWrite();
        }
    }

    xml.closeTag("term");
}

private boolean isKeywordConstant(Token t) {
    if (t.getType() != TokenType.KEYWORD) return false;
    String l = t.getLexeme();
    return l.equals("true") || l.equals("false") ||
           l.equals("null") || l.equals("this");
}





    
}
