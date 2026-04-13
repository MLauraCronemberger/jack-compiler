package br.com.jackcompiler;

import br.com.jackcompiler.lexer.Scanner;
import br.com.jackcompiler.lexer.Token;
import br.com.jackcompiler.xml.XmlGenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.out.println("Uso: java Main <arquivo.jack> <saida.xml>");
            return;
        }

        String inputPath = args[0];
        String outputPath = args[1];

        gerarXml(inputPath, outputPath);
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

}