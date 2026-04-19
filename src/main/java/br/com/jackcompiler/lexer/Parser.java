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

    

    
}
