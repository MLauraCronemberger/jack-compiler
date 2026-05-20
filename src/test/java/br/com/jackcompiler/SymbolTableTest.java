package br.com.jackcompiler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import br.com.jackcompiler.compiler.SymbolTable;
import br.com.jackcompiler.compiler.SymbolTable.Kind;
import br.com.jackcompiler.compiler.SymbolTable.Symbol;

public class SymbolTableTest {

    private SymbolTable table;

    @BeforeEach
    void setup() {
        table = new SymbolTable();
    }

    // ─── Testes de define e resolve ───────────────────────────────────────────

    @Test
    void testDefineFieldEResolve() {
        // "field int x, y;" → duas chamadas define
        table.define("x", "int", Kind.FIELD);
        table.define("y", "int", Kind.FIELD);

        Symbol x = table.resolve("x");
        Symbol y = table.resolve("y");

        // x deve ter índice 0, y deve ter índice 1
        assertNotNull(x);
        assertEquals("x",    x.name());
        assertEquals("int",  x.type());
        assertEquals(Kind.FIELD, x.kind());
        assertEquals(0,      x.index());

        assertNotNull(y);
        assertEquals(1, y.index()); // y vem depois de x
    }

    @Test
    void testDefineStatic() {
        // "static int count;" → índice começa do zero para STATIC (independente de FIELD)
        table.define("count", "int", Kind.STATIC);

        Symbol s = table.resolve("count");
        assertNotNull(s);
        assertEquals(Kind.STATIC, s.kind());
        assertEquals(0, s.index());
    }

    @Test
    void testDefineLocalEArg() {
        // Simula a subrotina: method int distance(Point other) { var int dx, dy; }
        table.define("this",  "Point", Kind.ARG);
        table.define("other", "Point", Kind.ARG);
        table.define("dx",    "int",   Kind.VAR);
        table.define("dy",    "int",   Kind.VAR);

        assertEquals(0, table.resolve("this").index());
        assertEquals(1, table.resolve("other").index());
        assertEquals(0, table.resolve("dx").index()); // VAR tem contador próprio
        assertEquals(1, table.resolve("dy").index());
    }

    @Test
    void testResolveSubrotinaAntesDeClasse() {
        // Se existe "x" na classe E na subrotina, deve retornar o da subrotina
        table.define("x", "int", Kind.FIELD); // escopo de classe

        table.startSubroutine();
        table.define("x", "int", Kind.VAR);   // escopo de subrotina (mesmo nome!)

        Symbol x = table.resolve("x");
        // Deve retornar o da subrotina (VAR), não o da classe (FIELD)
        assertEquals(Kind.VAR, x.kind());
    }

    @Test
    void testResolveNaoEncontrado() {
        // Variável que não existe deve retornar null
        assertNull(table.resolve("variavelQueNaoExiste"));
    }

    // ─── Testes de varCount ───────────────────────────────────────────────────

    @Test
    void testVarCount() {
        table.define("x", "int", Kind.FIELD);
        table.define("y", "int", Kind.FIELD);
        table.define("n", "int", Kind.STATIC);

        // 2 fields, 1 static
        assertEquals(2, table.varCount(Kind.FIELD));
        assertEquals(1, table.varCount(Kind.STATIC));
        assertEquals(0, table.varCount(Kind.VAR));   // nenhuma local ainda
    }

    // ─── Testes de startSubroutine ────────────────────────────────────────────

    @Test
    void testStartSubroutineLimpaLocais() {
        // Adiciona variáveis de subrotina
        table.define("this", "Point", Kind.ARG);
        table.define("dx",   "int",   Kind.VAR);

        assertEquals(1, table.varCount(Kind.ARG));
        assertEquals(1, table.varCount(Kind.VAR));

        // Entra em nova subrotina — deve limpar ARG e VAR
        table.startSubroutine();

        assertEquals(0, table.varCount(Kind.ARG)); // zerou
        assertEquals(0, table.varCount(Kind.VAR)); // zerou
        assertNull(table.resolve("dx"));            // não existe mais
    }

    @Test
    void testStartSubroutinePreservaClasse() {
        // Variáveis de classe não devem ser apagadas ao trocar de subrotina
        table.define("x", "int", Kind.FIELD);

        table.startSubroutine();

        // x ainda deve existir depois de trocar de subrotina
        assertNotNull(table.resolve("x"));
        assertEquals(Kind.FIELD, table.resolve("x").kind());
    }
}