package br.com.jackcompiler;

import br.com.jackcompiler.lexer.Parser;
import br.com.jackcompiler.lexer.Scanner;
import br.com.jackcompiler.lexer.Token;
import br.com.jackcompiler.xml.XmlGenerator;
import br.com.jackcompiler.xml.XmlParserGenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FilesAndValidationRunner {

    private static void processFile(String inputPath, String outputPath) throws Exception {

        String code = Files.readString(Path.of(inputPath));

        Scanner scanner = new Scanner(code);
        List<Token> tokens = scanner.tokenize();

        String xml = XmlGenerator.generate(tokens);

        Files.createDirectories(Path.of("output"));
        Files.writeString(Path.of(outputPath), xml);
    }

    // remove diferenças bobas de espaço/quebra de linha
    private static String normalize(String input) {
        return input
                .replaceAll("\\r", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static boolean runTest(String name) throws Exception {

        String inputPath = "src/test/resources/resources-jack/" + name + ".jack";
        String outputPath = "output/" + name + "T-Teste.xml";
        String expectedPath = "src/test/resources/expected-output-nand2tetris/" + name + "T.xml";

        processFile(inputPath, outputPath);

        String generated = normalize(Files.readString(Path.of(outputPath)));
        String expected = normalize(Files.readString(Path.of(expectedPath)));

        boolean isEqual = generated.equals(expected);

        if (isEqual) {
            System.out.println(name + ".jack -> " + name + "T.xml PASSED");
        } else {
            System.out.println(name + ".jack -> " + name + "T.xml FAILED");
        }

        return isEqual;
    }

    private static boolean runParserTest(String name) throws Exception {
    String inputPath  = "src/test/resources/resources-jack/" + name + ".jack";
    String outputPath = "output/" + name + "P-Teste.xml";
    String expectedPath = "src/test/resources/expected-output-nand2tetris/" + name + "P.xml";

    if (!java.nio.file.Files.exists(Path.of(expectedPath))) {
        System.out.println(name + " [PARSER] -> arquivo esperado não encontrado, pulando.");
        return false;
    }

    String code = Files.readString(Path.of(inputPath));
    Scanner scanner = new Scanner(code);
    List<Token> tokens = scanner.tokenize();

    XmlParserGenerator xmlGen = new XmlParserGenerator();
    Parser parser = new Parser(tokens, xmlGen);
    parser.parseClass();

    Files.createDirectories(Path.of("output"));
    Files.writeString(Path.of(outputPath), xmlGen.getXml());

    String generated = normalize(Files.readString(Path.of(outputPath)));
    String expected  = normalize(Files.readString(Path.of(expectedPath)));

    boolean ok = generated.equals(expected);
    System.out.println(name + ".jack -> " + name + "P.xml " + (ok ? "PASSED" : "FAILED"));
    return ok;
}

    public static void runAllTests() throws Exception {
    int total = 0, passed = 0;

    // testes do scanner (já existentes)
    if (runTest("Main"))       passed++; total++;
    if (runTest("Square"))     passed++; total++;
    if (runTest("SquareGame")) passed++; total++;

    System.out.println("--- Parser ---");

    // testes do parser
    if (runParserTest("Main"))       passed++; total++;
    if (runParserTest("Square"))     passed++; total++;
    if (runParserTest("SquareGame")) passed++; total++;

    System.out.println(passed + "/" + total + " testes passaram.");
}


    public static void main(String[] args) throws Exception {
    runAllTests();
}


}