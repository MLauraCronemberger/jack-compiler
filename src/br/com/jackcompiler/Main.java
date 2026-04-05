package br.com.jackcompiler;

import java.util.List;

import br.com.jackcompiler.lexer.Token;
import br.com.jackcompiler.lexer.TokenType;
import br.com.jackcompiler.lexer.Scanner;

public class Main {
    public static void main(String[] args) {

        // Token t = new Token(TokenType.SYMBOL, "<", 1);
        // System.out.println(t.toXML());

        Scanner scanner = new Scanner("class funcaoTeste funcao_Teste funcao_123 while");
        List<Token> tokens = scanner.tokenize();

        for (Token t : tokens) {
            if (t.getType() != TokenType.EOF) {
                System.out.println(t.toXML());
            }
        }




    }
}
