package br.com.jackcompiler;

import br.com.jackcompiler.lexer.Token;
import br.com.jackcompiler.lexer.TokenType;

public class Main {
    public static void main(String[] args) {
        
        Token t = new Token(TokenType.SYMBOL, "<", 1);
        System.out.println(t.toXML());
    }
}
