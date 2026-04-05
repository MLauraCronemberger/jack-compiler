package br.com.jackcompiler.lexer;

public class Token {
    private TokenType type;
    private String lexeme;
    private int line;

    public TokenType getType() {
    return type;
    }

    public Token(TokenType type, String lexeme, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }

    private String escapeXML(String text) {
    return text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
    }

    private String getCategory() {
        switch (type) {
            case KEYWORD: return "keyword";
            case SYMBOL: return "symbol";
            case IDENTIFIER: return "identifier";
            case INTEGER_CONSTANT: return "integerConstant";
            case STRING_CONSTANT: return "stringConstant";
            default: return "";
        }
    }
    
    public String toXML() {
        String value = escapeXML(lexeme);
        return "<" + getCategory() + "> " + value + " </" + getCategory() + ">";
    }
}
