package br.com.jackcompiler.lexer;

import java.util.List;
import br.com.jackcompiler.xml.XmlParserGenerator;
import br.com.jackcompiler.compiler.SymbolTable;
import br.com.jackcompiler.compiler.SymbolTable.Kind;
import br.com.jackcompiler.compiler.SymbolTable.Symbol;
import br.com.jackcompiler.compiler.VMWriter;
import br.com.jackcompiler.compiler.VMWriter.Segment;
import br.com.jackcompiler.compiler.VMWriter.Command;

public class Parser {
    private List<Token> tokens;
    private int current;
    private XmlParserGenerator xml;

    private SymbolTable symbolTable = new SymbolTable();
    private VMWriter vmWriter = new VMWriter();
    private String className = "";      
    private int ifLabelNum = 0;         
    private int whileLabelNum = 0;      

    public Parser(List<Token> tokens, XmlParserGenerator xml) {
        this.tokens = tokens;
        this.current = 0;
        this.xml = xml;
    }

    // retorna o código VM gerado (chamado no Main para gravar o .vm)
    public String getVMOutput() {
        return vmWriter.getOutput();
    }

    private Segment kindToSegment(Kind kind) {
        switch (kind) {
            case FIELD:  return Segment.THIS;
            case STATIC: return Segment.STATIC;
            case VAR:    return Segment.LOCAL;
            case ARG:    return Segment.ARG;
            default: throw new RuntimeException("Kind inválido: " + kind);
        }
    }

// ==================== TOKEN UTILS ====================

    private Token peek() {
        if (current < tokens.size()) {
            return tokens.get(current);
        }
        return null;
    }

    private Token advance() {
        if (current < tokens.size()) {
            return tokens.get(current++);
        }
        return null;
    }

    private boolean check(TokenType type) {
        Token token = peek();
        if (token == null) return false;
        return token.getType() == type;
    }

    private boolean match(TokenType type) {
        if (check(type)) {
            xml.writeToken(advance());
            return true;
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            Token token = advance();
            xml.writeToken(token);
            return token;
        }
        Token token = peek();
        throw new RuntimeException(
            message + " | Encontrado: " +
            (token != null ? token.getLexeme() : "EOF")
        );
    }

    private boolean checkKeyword(String word) {
        Token t = peek();
        return t != null && t.getType() == TokenType.KEYWORD && t.getLexeme().equals(word);
    }

    private Token consumeKeyword(String word) {
        Token t = peek();
        if (t != null && t.getType() == TokenType.KEYWORD && t.getLexeme().equals(word)) {
            Token token = advance();
            xml.writeToken(token);
            return token;
        }
        throw new RuntimeException(
            "Esperado keyword '" + word + "' | Encontrado: " +
            (t != null ? t.getLexeme() : "EOF")
        );
    }

    private boolean checkSymbol(String symbol) {
        Token t = peek();
        return t != null && t.getType() == TokenType.SYMBOL && t.getLexeme().equals(symbol);
    }

    private Token consumeSymbol(String symbol) {
        Token t = peek();
        if (t != null && t.getType() == TokenType.SYMBOL && t.getLexeme().equals(symbol)) {
            Token token = advance();
            xml.writeToken(token);
            return token;
        }
        throw new RuntimeException(
            "Esperado símbolo '" + symbol + "' | Encontrado: " +
            (t != null ? t.getLexeme() : "EOF")
        );
    }

    private void advanceAndWrite() {
        Token t = advance();
        xml.writeToken(t);
    }

// ==================== GRAMMAR HELPERS ====================

    private void parseType() {
        Token t = peek();
        if (t == null) {
            throw new RuntimeException("Tipo esperado, encontrado EOF");
        }
        if (t.getType() == TokenType.KEYWORD &&
            (t.getLexeme().equals("int") || t.getLexeme().equals("char") || t.getLexeme().equals("boolean"))) {
            xml.writeToken(advance());
        } else if (t.getType() == TokenType.IDENTIFIER) {
            xml.writeToken(advance());
        } else {
            throw new RuntimeException("Tipo esperado, encontrado: " + t.getLexeme());
        }
    }

    private boolean isKeywordConstant(Token t) {
        if (t.getType() != TokenType.KEYWORD) return false;
        String l = t.getLexeme();
        return l.equals("true") || l.equals("false") ||
               l.equals("null") || l.equals("this");
    }


// ==================== EXPRESSIONS ====================

    private void parseTerm() {
        xml.openTag("term");
        Token t = peek();

        if (t.getType() == TokenType.INTEGER_CONSTANT) {
            advanceAndWrite();
            vmWriter.writePush(Segment.CONST, Integer.parseInt(t.getLexeme()));

        } else if (t.getType() == TokenType.STRING_CONSTANT) {
            advanceAndWrite();
            String str = t.getLexeme();
            vmWriter.writePush(Segment.CONST, str.length());
            vmWriter.writeCall("String.new", 1);
            for (char c : str.toCharArray()) {
                vmWriter.writePush(Segment.CONST, (int) c);
                vmWriter.writeCall("String.appendChar", 2);
            }

        } else if (isKeywordConstant(t)) {
            advanceAndWrite();
            switch (t.getLexeme()) {
                case "true":
                    vmWriter.writePush(Segment.CONST, 0);
                    vmWriter.writeArithmetic(Command.NOT); // true = ~0 = -1
                    break;
                case "false":
                case "null":
                    vmWriter.writePush(Segment.CONST, 0);
                    break;
                case "this":
                    vmWriter.writePush(Segment.POINTER, 0);
                    break;
            }

        } else if (checkSymbol("(")) {
            consumeSymbol("(");
            parseExpression();
            consumeSymbol(")");
            // sem geração de código aqui: a expressão dentro já gerou

        } else if (checkSymbol("-") || checkSymbol("~")) {
            String op = t.getLexeme(); //guarda o operador antes de avançar
            advanceAndWrite();
            parseTerm();
            // operador unário → neg ou not
            if (op.equals("-")) vmWriter.writeArithmetic(Command.NEG);
            else                 vmWriter.writeArithmetic(Command.NOT);

        } else if (t.getType() == TokenType.IDENTIFIER) {
            Token next = tokens.get(current + 1);

            if (next.getLexeme().equals("[")) {
                //array[i] → calcula endereço e lê valor
                Token varToken = advance();
                xml.writeToken(varToken);
                Symbol sym = symbolTable.resolve(varToken.getLexeme());

                consumeSymbol("[");
                parseExpression();                                      // empilha o índice
                consumeSymbol("]");

                vmWriter.writePush(kindToSegment(sym.kind()), sym.index()); // empilha base do array
                vmWriter.writeArithmetic(Command.ADD);                      // base + índice
                vmWriter.writePop(Segment.POINTER, 1);                      // that = endereço
                vmWriter.writePush(Segment.THAT, 0);                        // empilha valor

            } else if (next.getLexeme().equals("(") || next.getLexeme().equals(".")) {
                parseSubroutineCall();

            } else {
                //variável simples → push do segmento correto
                Token varToken = advance();
                xml.writeToken(varToken);
                Symbol sym = symbolTable.resolve(varToken.getLexeme());
                if (sym != null) {
                    vmWriter.writePush(kindToSegment(sym.kind()), sym.index());
                }
            }
        }

        xml.closeTag("term");
    }

    private void parseExpression() {
        xml.openTag("expression");
        parseTerm();

        while (checkSymbol("+") || checkSymbol("-") || checkSymbol("*") ||
               checkSymbol("/") || checkSymbol("&") || checkSymbol("|") ||
               checkSymbol("<") || checkSymbol(">") || checkSymbol("=")) {

            String op = peek().getLexeme(); //guarda operador antes de avançar
            advanceAndWrite();
            parseTerm();

            //emite o comando VM do operador
            switch (op) {
                case "+": vmWriter.writeArithmetic(Command.ADD); break;
                case "-": vmWriter.writeArithmetic(Command.SUB); break;
                case "*": vmWriter.writeCall("Math.multiply", 2); break;
                case "/": vmWriter.writeCall("Math.divide", 2);   break;
                case "&": vmWriter.writeArithmetic(Command.AND); break;
                case "|": vmWriter.writeArithmetic(Command.OR);  break;
                case "<": vmWriter.writeArithmetic(Command.LT);  break;
                case ">": vmWriter.writeArithmetic(Command.GT);  break;
                case "=": vmWriter.writeArithmetic(Command.EQ);  break;
            }
        }

        xml.closeTag("expression");
    }

    //agora retorna int (quantidade de argumentos) em vez de void
    private int parseExpressionList() {
        xml.openTag("expressionList");
        int nArgs = 0;

        if (!checkSymbol(")")) {
            parseExpression();
            nArgs = 1;
            while (checkSymbol(",")) {
                consumeSymbol(",");
                parseExpression();
                nArgs++;
            }
        }

        xml.closeTag("expressionList");
        return nArgs;
    }

    private void parseSubroutineCall() {
        Token nameToken = advance();
        xml.writeToken(nameToken);
        String name = nameToken.getLexeme();

        int nArgs = 0;
        String functionName;

        if (checkSymbol(".")) {
            consumeSymbol(".");
            Token methodToken = consume(TokenType.IDENTIFIER, "Esperado nome do método");
            String methodName = methodToken.getLexeme();

            Symbol sym = symbolTable.resolve(name);

            if (sym != null) {
                // 'name' é um objeto → método: empilha o objeto como 1º argumento
                functionName = sym.type() + "." + methodName;
                vmWriter.writePush(kindToSegment(sym.kind()), sym.index());
                nArgs = 1;
            } else {
                // 'name' é uma classe → função estática
                functionName = name + "." + methodName;
            }

            consumeSymbol("(");
            nArgs += parseExpressionList();
            consumeSymbol(")");

        } else {
            functionName = className + "." + name;
            consumeSymbol("(");
            vmWriter.writePush(Segment.POINTER, 0);
            nArgs = parseExpressionList() + 1;
            consumeSymbol(")");
        }

        vmWriter.writeCall(functionName, nArgs);
    }


// ==================== STATEMENTS ====================

    private void parseReturn() {
        xml.openTag("returnStatement");
        consumeKeyword("return");

        if (!checkSymbol(";")) {
            parseExpression(); // a expressão já empilha o valor de retorno
        } else {
            vmWriter.writePush(Segment.CONST, 0);
        }

        consumeSymbol(";");
        vmWriter.writeReturn();
        xml.closeTag("returnStatement");
    }

    private void parseDo() {
        xml.openTag("doStatement");
        consumeKeyword("do");
        parseSubroutineCall();
        consumeSymbol(";");
        vmWriter.writePop(Segment.TEMP, 0); 
        xml.closeTag("doStatement");
    }

    private void parseWhile() {
        xml.openTag("whileStatement");

        String labelExp = "WHILE_EXP" + whileLabelNum;
        String labelEnd = "WHILE_END" + whileLabelNum;
        whileLabelNum++;

        vmWriter.writeLabel(labelExp);    

        consumeKeyword("while");
        consumeSymbol("(");
        parseExpression();               
        consumeSymbol(")");

        vmWriter.writeArithmetic(Command.NOT); 
        vmWriter.writeIf(labelEnd);           

        consumeSymbol("{");
        parseStatements();
        consumeSymbol("}");

        vmWriter.writeGoto(labelExp); 
        vmWriter.writeLabel(labelEnd); 

        xml.closeTag("whileStatement");
    }

    private void parseIf() {
        xml.openTag("ifStatement");

        String labelTrue = "IF_TRUE"  + ifLabelNum;
        String labelFalse = "IF_FALSE" + ifLabelNum;
        String labelEnd   = "IF_END"   + ifLabelNum;
        ifLabelNum++;

        consumeKeyword("if");
        consumeSymbol("(");
        parseExpression();             
        consumeSymbol(")");

        // >>> ADICIONADO: saltos
        vmWriter.writeIf(labelTrue);     // se condição verdadeira, vai para IF_TRUE
        vmWriter.writeGoto(labelFalse);  // senão, vai para IF_FALSE
        vmWriter.writeLabel(labelTrue);  // bloco do if começa aqui

        consumeSymbol("{");
        parseStatements();
        consumeSymbol("}");

        if (checkKeyword("else")) {
            vmWriter.writeGoto(labelEnd);    // pula o else
            vmWriter.writeLabel(labelFalse); //bloco else começa aqui
            consumeKeyword("else");
            consumeSymbol("{");
            parseStatements();
            consumeSymbol("}");
            vmWriter.writeLabel(labelEnd);   //fim do if-else
        } else {
            vmWriter.writeLabel(labelFalse); //sem else, falso só pula o bloco
        }

        xml.closeTag("ifStatement");
    }

    private void parseLet() {
        xml.openTag("letStatement");
        consumeKeyword("let");

        Token varToken = consume(TokenType.IDENTIFIER, "Esperado nome de variável");
        Symbol sym = symbolTable.resolve(varToken.getLexeme()); 

        boolean isArray = false;

        if (checkSymbol("[")) {
            isArray = true;
            consumeSymbol("[");
            parseExpression();                                         
            consumeSymbol("]");
            vmWriter.writePush(kindToSegment(sym.kind()), sym.index()); 
            vmWriter.writeArithmetic(Command.ADD);                       
        }

        consumeSymbol("=");
        parseExpression(); // empilha valor do lado direito

        if (isArray) {
            vmWriter.writePop(Segment.TEMP, 0);     
            vmWriter.writePop(Segment.POINTER, 1);  
            vmWriter.writePush(Segment.TEMP, 0);    
            vmWriter.writePop(Segment.THAT, 0);     
        } else {
            vmWriter.writePop(kindToSegment(sym.kind()), sym.index()); 
        }

        consumeSymbol(";");
        xml.closeTag("letStatement");
    }

    private void parseStatement() {
        if      (checkKeyword("let"))    parseLet();
        else if (checkKeyword("if"))     parseIf();
        else if (checkKeyword("while"))  parseWhile();
        else if (checkKeyword("do"))     parseDo();
        else if (checkKeyword("return")) parseReturn();
    }

    private void parseStatements() {
        xml.openTag("statements");
        while (checkKeyword("let") || checkKeyword("if") ||
               checkKeyword("while") || checkKeyword("do") ||
               checkKeyword("return")) {
            parseStatement();
        }
        xml.closeTag("statements");
    }


// ==================== SUBROUTINES ====================

    private void parseVarDec() {
        xml.openTag("varDec");
        consumeKeyword("var");

        // captura tipo e registra variáveis locais na SymbolTable
        Token typeToken = peek();
        parseType();
        String type = typeToken.getLexeme();

        Token nameToken = consume(TokenType.IDENTIFIER, "Esperado nome de variável");
        symbolTable.define(nameToken.getLexeme(), type, Kind.VAR);

        while (checkSymbol(",")) {
            consumeSymbol(",");
            Token nextName = consume(TokenType.IDENTIFIER, "Esperado nome de variável após ','");
            symbolTable.define(nextName.getLexeme(), type, Kind.VAR); 
        }

        consumeSymbol(";");
        xml.closeTag("varDec");
    }

    private void parseParameterList() {
        xml.openTag("parameterList");

        if (!checkSymbol(")")) {
            // captura tipo e registra argumentos na SymbolTable
            Token typeToken = peek();
            parseType();
            String type = typeToken.getLexeme();

            Token nameToken = consume(TokenType.IDENTIFIER, "Esperado nome do parâmetro");
            symbolTable.define(nameToken.getLexeme(), type, Kind.ARG); 

            while (checkSymbol(",")) {
                consumeSymbol(",");
                Token nextType = peek();
                parseType();
                type = nextType.getLexeme();

                Token nextName = consume(TokenType.IDENTIFIER, "Esperado nome do parâmetro");
                symbolTable.define(nextName.getLexeme(), type, Kind.ARG); 
            }
        }

        xml.closeTag("parameterList");
    }

    private void parseSubroutineBody(String functionName, String subroutineType) { 
        xml.openTag("subroutineBody");
        consumeSymbol("{");

        while (checkKeyword("var")) {
            parseVarDec();
        }

        //só aqui sabemos quantas variáveis locais tem → escreve function
        int nLocals = symbolTable.varCount(Kind.VAR);
        vmWriter.writeFunction(functionName, nLocals);

        if (subroutineType.equals("constructor")) {
            int nFields = symbolTable.varCount(Kind.FIELD);
            vmWriter.writePush(Segment.CONST, nFields);
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(Segment.POINTER, 0); 
        }


        if (subroutineType.equals("method")) {
            vmWriter.writePush(Segment.ARG, 0);
            vmWriter.writePop(Segment.POINTER, 0); 
        }

        parseStatements();
        consumeSymbol("}");
        xml.closeTag("subroutineBody");
    }

    private void parseSubroutineDec() {
        xml.openTag("subroutineDec");

        ifLabelNum = 0;
        whileLabelNum = 0;
        symbolTable.startSubroutine();

        String subroutineType; 
        if (checkKeyword("constructor") || checkKeyword("function") || checkKeyword("method")) {
            subroutineType = peek().getLexeme(); 
            advanceAndWrite();
        } else {
            throw new RuntimeException("Esperado constructor, function ou method");
        }

        if (subroutineType.equals("method")) {
            symbolTable.define("this", className, Kind.ARG);
        }

        if (checkKeyword("void")) {
            advanceAndWrite();
        } else {
            parseType();
        }

        Token nameToken = consume(TokenType.IDENTIFIER, "Esperado nome da subrotina");
        String functionName = className + "." + nameToken.getLexeme(); 

        consumeSymbol("(");
        parseParameterList();
        consumeSymbol(")");

        parseSubroutineBody(functionName, subroutineType); 

        xml.closeTag("subroutineDec");
    }


// ==================== CLASS ====================

    private void parseClassVarDec() {
        xml.openTag("classVarDec");

        String kindStr = peek().getLexeme();
        Kind kind = kindStr.equals("field") ? Kind.FIELD : Kind.STATIC;

        if (checkKeyword("static") || checkKeyword("field")) {
            advanceAndWrite();
        } else {
            throw new RuntimeException("Esperado 'static' ou 'field'");
        }

        Token typeToken = peek();
        parseType();
        String type = typeToken.getLexeme();

        Token nameToken = consume(TokenType.IDENTIFIER, "Esperado nome de variável");
        symbolTable.define(nameToken.getLexeme(), type, kind); 

        while (checkSymbol(",")) {
            consumeSymbol(",");
            Token nextName = consume(TokenType.IDENTIFIER, "Esperado nome de variável após ','");
            symbolTable.define(nextName.getLexeme(), type, kind);
        }

        consumeSymbol(";");
        xml.closeTag("classVarDec");
    }

    public void parseClass() {
        xml.openTag("class");
        consumeKeyword("class");

        Token classNameToken = consume(TokenType.IDENTIFIER, "Esperado nome da classe");
        className = classNameToken.getLexeme(); 

        consumeSymbol("{");

        while (checkKeyword("static") || checkKeyword("field")) {
            parseClassVarDec();
        }

        while (checkKeyword("constructor") || checkKeyword("function") || checkKeyword("method")) {
            parseSubroutineDec();
        }

        consumeSymbol("}");
        xml.closeTag("class");
    }
}