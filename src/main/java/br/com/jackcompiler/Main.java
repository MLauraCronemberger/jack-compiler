package br.com.jackcompiler;

import br.com.jackcompiler.lexer.Scanner;
import br.com.jackcompiler.lexer.Token;
import br.com.jackcompiler.xml.XmlGenerator;
import br.com.jackcompiler.lexer.Parser;
import br.com.jackcompiler.xml.XmlParserGenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
    if (args.length == 0) {
        FilesAndValidationRunner.runAllTests();
        return;
    }

    if (args.length == 3 && args[0].equals("--parser")) {
        gerarXmlParser(args[1], args[2]);
        return;
    }

    if (args.length == 2) {
        gerarXml(args[0], args[1]);
        return;
    }

    System.out.println("Uso: java Main <arquivo.jack> <saida.xml>");
    System.out.println("     java Main --parser <arquivo.jack> <saida.xml>");
}

    private static void gerarXml(String inputPath, String outputPath) throws Exception {

    String code = Files.readString(Path.of(inputPath));

    Scanner scanner = new Scanner(code);
    List<Token> tokens = scanner.tokenize();

    String xml = XmlGenerator.generate(tokens);

    Files.createDirectories(Path.of(outputPath).getParent());
    Files.writeString(Path.of(outputPath), xml);

    System.out.println("XML gerado: " + outputPath);
}

private static void gerarXmlParser(String inputPath, String outputPath) throws Exception {
    String code = Files.readString(Path.of(inputPath));

    Scanner scanner = new Scanner(code);
    List<Token> tokens = scanner.tokenize();

    XmlParserGenerator xmlGen = new XmlParserGenerator();
    Parser parser = new Parser(tokens, xmlGen);
    parser.parseClass();

    Files.createDirectories(Path.of(outputPath).getParent());
    Files.writeString(Path.of(outputPath), xmlGen.getXml());
    System.out.println("XML gerado: " + outputPath);
}
}