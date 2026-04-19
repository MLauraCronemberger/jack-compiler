package br.com.jackcompiler.lexer;

import java.util.List;
import br.com.jackcompiler.xml.XmlGenerator;

public class Parser {
        private List<Token> tokens;
    private int current;
    private XmlGenerator xml;

    public Parser(List<Token> tokens, XmlGenerator xml) {
        this.tokens = tokens;
        this.current = 0;
        this.xml = xml;
    }
    
}
