package br.com.jackcompiler.xml;

import br.com.jackcompiler.lexer.Token;

public class XmlParserGenerator {
    private StringBuilder xml = new StringBuilder();
    private int indentLevel = 0;

    private String indent() {
        return "  ".repeat(indentLevel);
    }

    public void openTag(String tag) {
        xml.append(indent()).append("<").append(tag).append(">\n");
        indentLevel++;
    }

    public void closeTag(String tag) {
    indentLevel = Math.max(0, indentLevel - 1);
    xml.append(indent()).append("</").append(tag).append(">\n");
}

    public void writeToken(Token token) {
        xml.append(indent()).append(token.toXML()).append("\n");
    }

    public String getXml() {
        return xml.toString();
    }
}
