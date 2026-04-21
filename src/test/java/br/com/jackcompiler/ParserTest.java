package br.com.jackcompiler;

import br.com.jackcompiler.lexer.Parser;
import br.com.jackcompiler.lexer.Scanner;
import br.com.jackcompiler.lexer.Token;
import br.com.jackcompiler.xml.XmlParserGenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    // ── utilidades ───────────────────────────────────────────────────────────

    private String parse(String jackCode) {
        List<Token> tokens = new Scanner(jackCode).tokenize();
        XmlParserGenerator xml = new XmlParserGenerator();
        new Parser(tokens, xml).parseClass();
        return xml.getXml();
    }

    private String normalize(String content) {
        return content
                .replaceAll("\r", "")
                .replaceAll("[ \t]+", " ")
                .replaceAll("(?m)^ +", "")
                .trim();
    }

    // ── estrutura da classe ──────────────────────────────────────────────────

    @Test
    void classeMinima() {
        String xml = parse("class Foo { function void f() { return; } }");
        assertTrue(xml.contains("<class>"));
        assertTrue(xml.contains("<identifier> Foo </identifier>"));
        assertTrue(xml.contains("</class>"));
    }

    @Test
    void classVarDecStatic() {
        String xml = parse("class Foo { static int x; function void f() { return; } }");
        assertTrue(xml.contains("<classVarDec>"));
        assertTrue(xml.contains("<keyword> static </keyword>"));
        assertTrue(xml.contains("<keyword> int </keyword>"));
    }

    @Test
    void classVarDecField() {
        String xml = parse("class Foo { field boolean flag; function void f() { return; } }");
        assertTrue(xml.contains("<keyword> field </keyword>"));
        assertTrue(xml.contains("<keyword> boolean </keyword>"));
    }

    @Test
    void classVarDecMultiplasVariaveis() {
        String xml = parse("class Foo { field int x, y, z; function void f() { return; } }");
        assertTrue(xml.contains("<symbol> , </symbol>"));
        assertTrue(xml.contains("<identifier> y </identifier>"));
        assertTrue(xml.contains("<identifier> z </identifier>"));
    }

    // ── subrotinas ───────────────────────────────────────────────────────────

    @Test
    void subroutineDecFunction() {
        String xml = parse("class Foo { function void f() { return; } }");
        assertTrue(xml.contains("<subroutineDec>"));
        assertTrue(xml.contains("<keyword> function </keyword>"));
        assertTrue(xml.contains("<keyword> void </keyword>"));
    }

    @Test
    void subroutineDecMethod() {
        String xml = parse("class Foo { method int f() { return; } }");
        assertTrue(xml.contains("<keyword> method </keyword>"));
    }

    @Test
    void subroutineDecConstructor() {
        String xml = parse("class Foo { constructor Foo new() { return; } }");
        assertTrue(xml.contains("<keyword> constructor </keyword>"));
    }

    @Test
    void parameterListVazia() {
        String xml = parse("class Foo { function void f() { return; } }");
        String stripped = normalize(xml);
        assertTrue(stripped.contains("<parameterList>\n</parameterList>") ||
                   stripped.contains("<parameterList></parameterList>"));
    }

    @Test
    void parameterListComUmParametro() {
        String xml = parse("class Foo { function void f(int x) { return; } }");
        assertTrue(xml.contains("<keyword> int </keyword>"));
        assertTrue(xml.contains("<identifier> x </identifier>"));
    }

    @Test
    void parameterListComMultiplosParametros() {
        String xml = parse("class Foo { method void f(int a, boolean b, char c) { return; } }");
        assertTrue(xml.contains("<identifier> a </identifier>"));
        assertTrue(xml.contains("<identifier> b </identifier>"));
        assertTrue(xml.contains("<identifier> c </identifier>"));
    }

    @Test
    void varDec() {
        String xml = parse("class Foo { function void f() { var int x; return; } }");
        assertTrue(xml.contains("<varDec>"));
        assertTrue(xml.contains("<keyword> var </keyword>"));
    }

    @Test
    void varDecMultiplas() {
        String xml = parse("class Foo { function void f() { var int x, y; return; } }");
        assertTrue(xml.contains("<identifier> y </identifier>"));
    }

    // ── statements ───────────────────────────────────────────────────────────

    @Test
    void letSimples() {
        String xml = parse("class Foo { function void f() { var int x; let x = 1; return; } }");
        assertTrue(xml.contains("<letStatement>"));
        assertTrue(xml.contains("<keyword> let </keyword>"));
    }

    @Test
    void letComArray() {
        String xml = parse("class Foo { function void f() { var Array a; let a[0] = 1; return; } }");
        assertTrue(xml.contains("<symbol> [ </symbol>"));
        assertTrue(xml.contains("<symbol> ] </symbol>"));
    }

    @Test
    void ifSemElse() {
        String xml = parse("class Foo { function void f() { var int x; if (x) { return; } return; } }");
        assertTrue(xml.contains("<ifStatement>"));
        assertFalse(xml.contains("<keyword> else </keyword>"));
    }

    @Test
    void ifComElse() {
        String xml = parse("class Foo { function void f() { var int x; if (x) { return; } else { return; } } }");
        assertTrue(xml.contains("<keyword> else </keyword>"));
    }

    @Test
    void whileStatement() {
        String xml = parse("class Foo { function void f() { var int x; while (x) { let x = 0; } return; } }");
        assertTrue(xml.contains("<whileStatement>"));
    }

    @Test
    void doStatementFuncao() {
        String xml = parse("class Foo { function void f() { do foo(); return; } }");
        assertTrue(xml.contains("<doStatement>"));
        assertTrue(xml.contains("<identifier> foo </identifier>"));
    }

    @Test
    void doStatementMetodo() {
        String xml = parse("class Foo { function void f() { do Output.printInt(1); return; } }");
        assertTrue(xml.contains("<identifier> Output </identifier>"));
        assertTrue(xml.contains("<identifier> printInt </identifier>"));
    }

    @Test
    void returnSemExpressao() {
        String xml = parse("class Foo { function void f() { return; } }");
        assertTrue(xml.contains("<returnStatement>"));
        // sem expressão, não deve ter a tag <expression>
        assertFalse(xml.contains("<expression>"));
    }

    @Test
    void returnComExpressao() {
        String xml = parse("class Foo { function int f() { var int x; return x; } }");
        assertTrue(xml.contains("<expression>"));
    }

    // ── expressões ───────────────────────────────────────────────────────────

    @Test
    void expressaoSoma() {
        String xml = parse("class Foo { function void f() { var int x; let x = 1 + 2; return; } }");
        assertTrue(xml.contains("<symbol> + </symbol>"));
    }

    @Test
    void expressaoUnariaNegativa() {
        String xml = parse("class Foo { function void f() { var int x; let x = -1; return; } }");
        assertTrue(xml.contains("<symbol> - </symbol>"));
    }

    @Test
    void expressaoUnariaComplemento() {
        String xml = parse("class Foo { function void f() { var boolean b; let b = ~b; return; } }");
        assertTrue(xml.contains("<symbol> ~ </symbol>"));
    }

    @Test
    void expressaoParenteses() {
        String xml = parse("class Foo { function void f() { var int x; let x = (1 + 2); return; } }");
        assertTrue(xml.contains("<symbol> ( </symbol>"));
    }

    @Test
    void keywordConstantTrue() {
        String xml = parse("class Foo { function void f() { var boolean b; let b = true; return; } }");
        assertTrue(xml.contains("<keyword> true </keyword>"));
    }

    @Test
    void keywordConstantNull() {
        String xml = parse("class Foo { function void f() { var String s; let s = null; return; } }");
        assertTrue(xml.contains("<keyword> null </keyword>"));
    }

    @Test
    void stringConstant() {
        String xml = parse("class Foo { function void f() { var String s; let s = \"ola\"; return; } }");
        assertTrue(xml.contains("<stringConstant> ola </stringConstant>"));
    }

    @Test
    void acessoArray() {
        String xml = parse("class Foo { function void f() { var Array a; var int x; let x = a[2]; return; } }");
        assertTrue(xml.contains("<symbol> [ </symbol>"));
        assertTrue(xml.contains("<integerConstant> 2 </integerConstant>"));
    }

    @Test
    void expressaoListaMultipla() {
        String xml = parse("class Foo { function void f() { do foo(1, 2, 3); return; } }");
        long commas = xml.lines().filter(l -> l.contains("<symbol> , </symbol>")).count();
        assertEquals(2, commas);
    }

    // ── erros ────────────────────────────────────────────────────────────────

    @Test
    void erroClasseInvalida() {
        assertThrows(RuntimeException.class, () -> parse("notAClass Foo { }"));
    }

    @Test
    void erroFaltaChaveAbrindo() {
        assertThrows(RuntimeException.class, () ->
            parse("class Foo function void f() { return; } }"));
    }

    @Test
    void erroFaltaSemicolon() {
        assertThrows(RuntimeException.class, () ->
            parse("class Foo { function void f() { var int x return; } }"));
    }

    // ── validação oficial (nand2tetris) ───────────────────────────────────────

    @ParameterizedTest(name = "{0}.jack -> {0}P.xml")
    @CsvSource({"Main", "Square", "SquareGame"})
    void validacaoOficial(String name) throws Exception {
        String jackPath     = "src/test/resources/resources-jack/" + name + ".jack";
        String expectedPath = "src/test/resources/expected-output-nand2tetris/" + name + "P.xml";

        String code = Files.readString(Path.of(jackPath));
        List<Token> tokens = new Scanner(code).tokenize();
        XmlParserGenerator xmlGen = new XmlParserGenerator();
        new Parser(tokens, xmlGen).parseClass();

        String generated = normalize(xmlGen.getXml());
        String expected  = normalize(Files.readString(Path.of(expectedPath)));

        assertEquals(expected, generated,
            "XML gerado para " + name + ".jack diverge do oficial");
    }
}