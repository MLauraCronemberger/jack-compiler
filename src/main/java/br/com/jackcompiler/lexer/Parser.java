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
    if (t.getType() == TokenType.KEYWORD &&
        (t.getLexeme().equals("int") || t.getLexeme().equals("char") || t.getLexeme().equals("boolean"))) {
        xml.writeToken(advance());
    } else if (t.getType() == TokenType.IDENTIFIER) {
        xml.writeToken(advance());  // className
    } else {
        throw new RuntimeException("Tipo esperado, encontrado: " + t.getLexeme());
    }
}

public void parseClass() {
    xml.openTag("class");
    consume(TokenType.KEYWORD, "Esperado 'class'");   // class
    consume(TokenType.IDENTIFIER, "Esperado nome da classe"); // className
    consume(TokenType.SYMBOL, "Esperado '{'");        // {

    while (check(TokenType.KEYWORD) &&
           (peek().getLexeme().equals("static") || peek().getLexeme().equals("field"))) {
        parseClassVarDec();
    }

    while (check(TokenType.KEYWORD) &&
           (peek().getLexeme().equals("constructor") ||
            peek().getLexeme().equals("function") ||
            peek().getLexeme().equals("method"))) {
        parseSubroutineDec();
    }

    consume(TokenType.SYMBOL, "Esperado '}'");        // }
    xml.closeTag("class");
}

// Auxiliar: consume uma keyword específica pelo lexeme
private Token consumeKeyword(String word) {
    Token t = peek();
    if (t != null && t.getType() == TokenType.KEYWORD && t.getLexeme().equals(word)) {
        xml.writeToken(t);
        return advance();
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
        xml.writeToken(t);
        return advance();
    }
    throw new RuntimeException(
        "Esperado símbolo '" + symbol + "' | Encontrado: " +
        (t != null ? t.getLexeme() : "EOF")
    );
}







    

    
}
