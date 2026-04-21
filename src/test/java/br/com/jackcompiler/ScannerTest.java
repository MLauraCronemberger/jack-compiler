package br.com.jackcompiler;

import br.com.jackcompiler.lexer.Scanner;
import br.com.jackcompiler.lexer.Token;
import br.com.jackcompiler.lexer.TokenType;
import br.com.jackcompiler.xml.XmlGenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScannerTest {

    // ── utilidades ───────────────────────────────────────────────────────────

    private List<Token> scan(String code) {
        return new Scanner(code).tokenize();
    }

    private String normalize(String content) {
        return content
                .replaceAll("\r", "")
                .replaceAll("[ \t]+", " ")
                .replaceAll("(?m)^ +", "")
                .trim();
    }

    // ── keywords ─────────────────────────────────────────────────────────────

    @Test
    void reconheceKeywordClass() {
        List<Token> tokens = scan("class");
        assertEquals(TokenType.KEYWORD, tokens.get(0).getType());
        assertEquals("class", tokens.get(0).getLexeme());
    }

    @Test
    void reconheceTodasAsKeywords() {
        String todas = "class constructor function method field static var " +
                       "int char boolean void true false null this " +
                       "let do if else while return";
        List<Token> tokens = scan(todas);
        long keywords = tokens.stream()
                .filter(t -> t.getType() == TokenType.KEYWORD)
                .count();
        assertEquals(21, keywords);
    }

    // ── identificadores ──────────────────────────────────────────────────────

    @Test
    void reconheceIdentificador() {
        List<Token> tokens = scan("minhaVariavel");
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("minhaVariavel", tokens.get(0).getLexeme());
    }

    @Test
    void identificadorComUnderscoreEDigito() {
        List<Token> tokens = scan("_var_1");
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
    }

    @Test
    void identificadorNaoComecaComDigito() {
        // "1abc" deve gerar um inteiro (1) e depois um identificador (abc)
        List<Token> tokens = scan("1abc");
        assertEquals(TokenType.INTEGER_CONSTANT, tokens.get(0).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).getType());
    }

    // ── constantes inteiras ──────────────────────────────────────────────────

    @Test
    void reconheceInteiro() {
        List<Token> tokens = scan("42");
        assertEquals(TokenType.INTEGER_CONSTANT, tokens.get(0).getType());
        assertEquals("42", tokens.get(0).getLexeme());
    }

    @Test
    void reconheceZero() {
        List<Token> tokens = scan("0");
        assertEquals(TokenType.INTEGER_CONSTANT, tokens.get(0).getType());
        assertEquals("0", tokens.get(0).getLexeme());
    }

    // ── constantes string ────────────────────────────────────────────────────

    @Test
    void reconheceString() {
        List<Token> tokens = scan("\"hello world\"");
        assertEquals(TokenType.STRING_CONSTANT, tokens.get(0).getType());
        assertEquals("hello world", tokens.get(0).getLexeme());
    }

    @Test
    void stringVazia() {
        List<Token> tokens = scan("\"\"");
        assertEquals(TokenType.STRING_CONSTANT, tokens.get(0).getType());
        assertEquals("", tokens.get(0).getLexeme());
    }

    @Test
    void stringNaoFechadaLancaExcecao() {
        assertThrows(RuntimeException.class, () -> scan("\"sem fechamento"));
    }

    // ── símbolos ─────────────────────────────────────────────────────────────

    @Test
    void reconheceSimbolo() {
        List<Token> tokens = scan("{");
        assertEquals(TokenType.SYMBOL, tokens.get(0).getType());
        assertEquals("{", tokens.get(0).getLexeme());
    }

    @Test
    void reconheceTodosOsSimbolos() {
        String simbolos = "{ } ( ) [ ] . , ; + - * / & | < > = ~";
        List<Token> tokens = scan(simbolos);
        long simboloCount = tokens.stream()
                .filter(t -> t.getType() == TokenType.SYMBOL)
                .count();
        assertEquals(19, simboloCount);
    }

    // ── comentários ──────────────────────────────────────────────────────────

    @Test
    void comentarioLinhaNaoGeraToken() {
        List<Token> tokens = scan("// isso é um comentário\nclass");
        assertEquals(1, tokens.size() - 1); // -1 para o EOF
        assertEquals(TokenType.KEYWORD, tokens.get(0).getType());
    }

    @Test
    void comentarioBlocoNaoGeraToken() {
        List<Token> tokens = scan("/* bloco */ class");
        assertEquals(1, tokens.size() - 1);
        assertEquals(TokenType.KEYWORD, tokens.get(0).getType());
    }

    @Test
    void comentarioBlocoMultilinha() {
        List<Token> tokens = scan("/** \n * doc \n */ class");
        assertEquals(1, tokens.size() - 1);
        assertEquals("class", tokens.get(0).getLexeme());
    }

    // ── whitespace e EOF ─────────────────────────────────────────────────────

    @Test
    void ignoraEspacosETabs() {
        List<Token> tokens = scan("   class   ");
        assertEquals(TokenType.KEYWORD, tokens.get(0).getType());
    }

    @Test
    void sempreTerminaComEof() {
        List<Token> tokens = scan("class Foo { }");
        Token ultimo = tokens.get(tokens.size() - 1);
        assertEquals(TokenType.EOF, ultimo.getType());
    }

    @Test
    void codigoVazioRetornaApenasEof() {
        List<Token> tokens = scan("");
        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).getType());
    }

    // ── XML do tokenizer ─────────────────────────────────────────────────────

    @Test
    void xmlEscapeDeCaracteresEspeciais() {
        List<Token> tokens = scan("< > &");
        String xml = XmlGenerator.generate(tokens);
        assertTrue(xml.contains("&lt;"));
        assertTrue(xml.contains("&gt;"));
        assertTrue(xml.contains("&amp;"));
    }

    @Test
    void xmlContemTagTokens() {
        List<Token> tokens = scan("class Foo");
        String xml = XmlGenerator.generate(tokens);
        assertTrue(xml.startsWith("<tokens>"));
        assertTrue(xml.contains("</tokens>"));
    }

    // ── validação oficial (nand2tetris) ───────────────────────────────────────

    @ParameterizedTest(name = "{0}.jack -> {0}T.xml")
    @CsvSource({"Main", "Square", "SquareGame"})
    void validacaoOficial(String name) throws Exception {
        String jackPath     = "src/test/resources/resources-jack/" + name + ".jack";
        String expectedPath = "src/test/resources/expected-output-nand2tetris/" + name + "T.xml";

        String code = Files.readString(Path.of(jackPath));
        List<Token> tokens = new Scanner(code).tokenize();
        String generated = normalize(XmlGenerator.generate(tokens));
        String expected  = normalize(Files.readString(Path.of(expectedPath)));

        assertEquals(expected, generated,
            "XML gerado para " + name + ".jack diverge do oficial");
    }
}