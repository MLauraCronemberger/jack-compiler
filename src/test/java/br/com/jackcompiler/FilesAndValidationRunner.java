package br.com.jackcompiler;

import br.com.jackcompiler.lexer.Scanner;
import br.com.jackcompiler.lexer.Token;
import br.com.jackcompiler.xml.XmlGenerator;

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

    public static void runAllTests() throws Exception {

        int total = 0;
        int passed = 0;

        if (runTest("Main")) passed++;
        total++;

        if (runTest("Square")) passed++;
        total++;

        if (runTest("SquareGame")) passed++;
        total++;

        System.out.println(passed + "/" + total + " arquivos validados com sucesso!");
    }


    public static void main(String[] args) throws Exception {
    runAllTests();
}


}