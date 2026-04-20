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

private void parseParameterList() {
    xml.openTag("parameterList");
    // TODO: Fase 2
    xml.closeTag("parameterList");
}

private void parseSubroutineBody() {
    xml.openTag("subroutineBody");
    // TODO: Fase 2
    xml.closeTag("subroutineBody");
}

    
}
