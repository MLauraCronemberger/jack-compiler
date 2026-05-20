package br.com.jackcompiler.compiler;
 
import java.util.HashMap;
import java.util.Map;
 
/**
 * Tabela de símbolos do compilador Jack.
 *
 * Mantém dois escopos simultâneos:
 *   - classScope: variáveis field e static (duram a classe toda)
 *   - subroutineScope: variáveis arg e local (resetam a cada subrotina)
 */
public class SymbolTable {
 
    // As quatro categorias de variáveis em Jack
    public enum Kind {
        STATIC,  // variável de classe, compartilhada entre instâncias
        FIELD,   // variável de instância, cada objeto tem a sua
        ARG,     // parâmetro de subrotina
        VAR      // variável local de subrotina
    }
 
    // Um símbolo é um registro com as 4 informações que o gerador de código precisa
    public record Symbol(String name, String type, Kind kind, int index) {}
 
    private Map<String, Symbol> classScope;
    private Map<String, Symbol> subroutineScope;
 
    // Contadores separados por kind para atribuir os índices corretamente
    private Map<Kind, Integer> counters;
 
    public SymbolTable() {
        classScope      = new HashMap<>();
        subroutineScope = new HashMap<>();
        counters        = new HashMap<>();
 
        // Inicializa todos os contadores em zero
        counters.put(Kind.STATIC, 0);
        counters.put(Kind.FIELD,  0);
        counters.put(Kind.ARG,    0);
        counters.put(Kind.VAR,    0);
    }
 
    /**
     * Chamado ao entrar em uma nova subrotina.
     * Limpa apenas o escopo de subrotina — o escopo de classe permanece intacto.
     */
    public void startSubroutine() {
        subroutineScope.clear();
        counters.put(Kind.ARG, 0);
        counters.put(Kind.VAR, 0);
    }
 
    /**
     * Registra uma nova variável na tabela.
     * O índice é atribuído automaticamente com base no contador do kind.
     *
     * Exemplos:
     *   define("x", "int", FIELD)   → Symbol("x", "int", FIELD, 0)
     *   define("y", "int", FIELD)   → Symbol("y", "int", FIELD, 1)
     *   define("dx", "int", VAR)    → Symbol("dx", "int", VAR, 0)
     */
    public void define(String name, String type, Kind kind) {
        int index = counters.get(kind);
        Symbol symbol = new Symbol(name, type, kind, index);
 
        // FIELD e STATIC vão para o escopo de classe
        if (kind == Kind.FIELD || kind == Kind.STATIC) {
            classScope.put(name, symbol);
        } else {
            // ARG e VAR vão para o escopo de subrotina
            subroutineScope.put(name, symbol);
        }
 
        // Incrementa o contador desse kind
        counters.put(kind, index + 1);
    }
 
    /**
     * Busca uma variável pelo nome.
     * Procura primeiro na subrotina (escopo mais interno),
     * depois na classe (escopo externo).
     * Retorna null se não encontrar.
     */
    public Symbol resolve(String name) {
        Symbol s = subroutineScope.get(name);
        if (s != null) return s;
        return classScope.get(name);
    }
 
    /**
     * Retorna quantas variáveis de um dado kind foram definidas.
     * Usado para saber quantas variáveis locais uma função tem:
     *   varCount(VAR) → número que vai em "function Main.foo N"
     * E quantos fields uma classe tem:
     *   varCount(FIELD) → número que vai em "push constant N / call Memory.alloc 1"
     */
    public int varCount(Kind kind) {
        return counters.get(kind);
    }
}