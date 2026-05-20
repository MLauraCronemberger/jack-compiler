package br.com.jackcompiler.compiler;
 
public class VMWriter {
 
    private StringBuilder output = new StringBuilder();
 
    // ─── Segmentos de memória da VM ───────────────────────────────────────────
    public enum Segment {
        CONST("constant"),   
        ARG("argument"),     
        LOCAL("local"),      
        STATIC("static"),    
        THIS("this"),        
        THAT("that"),        
        POINTER("pointer"),  
        TEMP("temp");        
 
        public final String value;
        Segment(String value) { this.value = value; }
    }
 
    // ─── Operações aritméticas e lógicas da VM ────────────────────────────────
    public enum Command {
        ADD,  // a + b
        SUB,  // a - b
        NEG,  // -a
        EQ,   // a == b
        GT,   // a > b
        LT,   // a < b
        AND,  // a & b
        OR,   // a | b
        NOT   // ~a
    }
 
    // ─── Métodos de escrita ───────────────────────────────────────────────────
 
    /** push segment index  →  coloca um valor na pilha */
    public void writePush(Segment segment, int index) {
        output.append(String.format("push %s %d\n", segment.value, index));
    }
 
    /** pop segment index  →  tira o topo da pilha e guarda no destino */
    public void writePop(Segment segment, int index) {
        output.append(String.format("pop %s %d\n", segment.value, index));
    }
 
    /** add / sub / neg / eq / gt / lt / and / or / not */
    public void writeArithmetic(Command command) {
        output.append(command.name().toLowerCase()).append("\n");
    }
 
    /** label L  →  marca um ponto de destino para saltos */
    public void writeLabel(String label) {
        output.append(String.format("label %s\n", label));
    }
 
    /** goto L  →  salto incondicional */
    public void writeGoto(String label) {
        output.append(String.format("goto %s\n", label));
    }
 
    /** if-goto L  →  salto condicional (salta se topo da pilha != 0) */
    public void writeIf(String label) {
        output.append(String.format("if-goto %s\n", label));
    }
 
    /** call functionName nArgs  →  chama uma função com N argumentos */
    public void writeCall(String name, int nArgs) {
        output.append(String.format("call %s %d\n", name, nArgs));
    }
 
    /** function functionName nLocals  →  declara uma função com N variáveis locais */
    public void writeFunction(String name, int nLocals) {
        output.append(String.format("function %s %d\n", name, nLocals));
    }
 
    /** return  →  retorna da função atual */
    public void writeReturn() {
        output.append("return\n");
    }
 
    /** Retorna todo o código VM gerado até agora como string */
    public String getOutput() {
        return output.toString();
    }
}