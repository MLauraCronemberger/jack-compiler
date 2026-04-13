package br.com.jackcompiler;

import java.util.List;

import br.com.jackcompiler.lexer.Scanner;
import br.com.jackcompiler.lexer.Token;
import br.com.jackcompiler.lexer.TokenType;
import br.com.jackcompiler.xml.XmlGenerator;

public class LexerTest {

    public static void main(String[] args) {

        testNumero();
        testString();
        testIdentifierEKeyword();
        testSimbolos();
        testComentarios();

        System.out.println("\nTODOS OS TESTES FINALIZADOS");
    }

    private static void runTest(String name, String code) {
        System.out.println("\n=== " + name + " ===");

        Scanner scanner = new Scanner(code);
        List<Token> tokens = scanner.tokenize();

        String xml = XmlGenerator.generate(tokens);

        System.out.println(xml);
    }

    private static void testNumero() {
        runTest("Numero", "123 45 ");
    }

    private static void testString() {
        runTest("String", "\"hello\"");
    }

    private static void testIdentifierEKeyword() {
        runTest("Identifier e Keywords", "abc boolean varName class");
    }

    private static void testSimbolos() {
        runTest("Simbolos", "x + y;");
    }

    private static void testComentarios() {
        runTest("Comentarios", "let x = 10/2 ; // comentario");
    }
}

