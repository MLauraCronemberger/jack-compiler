package br.com.jackcompiler;

import br.com.jackcompiler.lexer.Scanner;
import br.com.jackcompiler.lexer.Token;
import br.com.jackcompiler.xml.XmlGenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

    // modo teste (sem argumentos)
    if (args.length == 0) {
        FilesAndValidationRunner.runAllTests();
        return;
    }

    // modo normal
    if (args.length != 2) {
        System.out.println("Uso: java Main <arquivo.jack> <saida.xml>");
        return;
    }

    String inputPath = args[0];
    String outputPath = args[1];

    gerarXml(inputPath, outputPath);
}

}