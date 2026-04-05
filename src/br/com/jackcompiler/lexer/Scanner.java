package br.com.jackcompiler.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Scanner {

    private String code;
    private int current = 0;
    private int line = 1;
    private List<Token> tokens = new ArrayList<>();

    private static final Set<String> KEYWORDS = Set.of(
        "class", "constructor", "function", "method", "field", "static", "var",
        "int", "char", "boolean", "void",
        "true", "false", "null", "this",
        "let", "do", "if", "else", "while", "return"
    );

    private static final Set<Character> SYMBOLS = Set.of(
        '{','}','(',')','[',']','.',';',',','+','-','*','/','&','|','<','>','=','~'
    );

    private char peek() {
    if (current >= code.length()) return '\0';
    return code.charAt(current);
    }

    private void advance() {
    if (current < code.length()) {
        if (code.charAt(current) == '\n') line++;
        current++;
    }
}   

    private void skipWhitespace() {
    while (true) {
        char c = peek();

        if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
            advance();
        } else {
            break;
        }
    }
}

    public Scanner(String code) {
        this.code = code;
    }

private Token readNumber() {
    int start = current;

        while (Character.isDigit(peek())) {
            advance();
        }
        
        String lexeme = code.substring(start, current);  
        return new Token(TokenType.INTEGER_CONSTANT, lexeme, line);    
}

private Token readString() {
    advance(); 
    int start = current;

    while (peek() != '"' && peek() != '\0') {
        if (peek() == '\n') {
            throw new RuntimeException("String não fechada na linha " + line);
        }
        advance();
    }

    if (peek() == '\0') {
        throw new RuntimeException("String não fechada na linha " + line);
    }

    String lexeme = code.substring(start, current);
    advance(); 
    return new Token(TokenType.STRING_CONSTANT, lexeme, line);
}

private Token readIdentifierAndKeyword() {
    int start = current;

    while (Character.isLetterOrDigit(peek()) || peek() == '_') {
        advance();
    }

    String lexeme = code.substring(start, current);

    if (KEYWORDS.contains(lexeme)) {
        return new Token(TokenType.KEYWORD, lexeme, line);
    }

    return new Token(TokenType.IDENTIFIER, lexeme, line);
}

private Token readSymbol() {
    char c = peek();

    if (SYMBOLS.contains(c)) {
        advance();
        return new Token(TokenType.SYMBOL, String.valueOf(c), line);
    }

    throw new RuntimeException("Símbolo inválido na linha " + line);
}


    public List<Token> tokenize() {
    while (current < code.length()) {
        skipWhitespace();
        if (current >= code.length()) break;

        char c = peek();

        if (Character.isDigit(c)) {
            tokens.add(readNumber());
        } else if (c == '"') {
             tokens.add(readString());
        } else if (Character.isLetter(c) || c == '_') {
            tokens.add(readIdentifierAndKeyword());
        }else if (SYMBOLS.contains(c)) {
            tokens.add(readSymbol());
        }
        else {
            advance(); 
        }
    }

    tokens.add(new Token(TokenType.EOF, "", line));

    return tokens;
    }


}

