package br.com.jackcompiler.xml;

import java.util.List;
import br.com.jackcompiler.lexer.Token;
import br.com.jackcompiler.lexer.TokenType;

public class XmlGenerator {

    public static String generate(List<Token> tokens) {
        StringBuilder xml = new StringBuilder();
        xml.append("<tokens>\n");

        for (Token token : tokens) {
            if (token.getType() != TokenType.EOF) {
                xml.append(token.toXML()).append("\n");
            }
        }

        xml.append("</tokens>\n");
        return xml.toString();
    }
}
