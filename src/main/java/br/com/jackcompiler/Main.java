package br.com.jackcompiler;

import br.com.jackcompiler.lexer.Scanner;
import br.com.jackcompiler.lexer.Token;
import br.com.jackcompiler.xml.XmlGenerator;
import br.com.jackcompiler.lexer.Parser;
import br.com.jackcompiler.xml.XmlParserGenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            FilesAndValidationRunner.runAllTests();
            return;
        }

        // Compilar um diretório inteiro
        // Uso: java Main --vm <diretório ou arquivo.jack>
        if (args.length == 2 && args[0].equals("--vm")) {
            compilarVM(args[1]);
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
        System.out.println("     java Main --vm <arquivo.jack ou diretório>");
    }

    // Decide se é arquivo único ou diretório e compila para .vm
    private static void compilarVM(String inputPath) throws Exception {
        Path path = Path.of(inputPath);

        if (Files.isDirectory(path)) {
            List<Path> arquivosJack = Files.list(path)
                .filter(p -> p.toString().endsWith(".jack"))
                .collect(Collectors.toList());

            if (arquivosJack.isEmpty()) {
                System.out.println("Nenhum arquivo .jack encontrado em: " + inputPath);
                return;
            }

            for (Path arquivo : arquivosJack) {
                compilarArquivoVM(arquivo);
            }

        } else if (inputPath.endsWith(".jack")) {
            compilarArquivoVM(path);

        } else {
            System.out.println("Entrada inválida: informe um arquivo .jack ou um diretório.");
        }
    }

    // Compila um único arquivo .jack e grava o .vm no mesmo diretório
    private static void compilarArquivoVM(Path jackFile) throws Exception {
        String code = Files.readString(jackFile);

        Scanner scanner = new Scanner(code);
        List<Token> tokens = scanner.tokenize();

        XmlParserGenerator xmlGen = new XmlParserGenerator();
        Parser parser = new Parser(tokens, xmlGen);
        parser.parseClass();

        String vmFileName = jackFile.getFileName().toString().replace(".jack", ".vm");
        Path vmFile = jackFile.getParent().resolve(vmFileName);

        Files.writeString(vmFile, parser.getVMOutput());
        System.out.println("Compilado: " + jackFile.getFileName() + " → " + vmFileName);
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