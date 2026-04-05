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
}

