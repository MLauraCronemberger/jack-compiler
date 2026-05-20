package br.com.jackcompiler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import br.com.jackcompiler.compiler.VMWriter.Segment;
import br.com.jackcompiler.compiler.VMWriter;
import br.com.jackcompiler.compiler.VMWriter.Command;

public class VMWriterTest {

    private VMWriter vm;

    @BeforeEach
    void setup() {
        vm = new VMWriter();
    }

    // ─── push e pop ───────────────────────────────────────────────────────────

    @Test
    void testWritePushConstant() {
        vm.writePush(Segment.CONST, 10);
        assertEquals("push constant 10\n", vm.getOutput());
    }

    @Test
    void testWritePushLocal() {
        vm.writePush(Segment.LOCAL, 0);
        assertEquals("push local 0\n", vm.getOutput());
    }

    @Test
    void testWritePushThis() {
        vm.writePush(Segment.THIS, 2);
        assertEquals("push this 2\n", vm.getOutput());
    }

    @Test
    void testWritePopLocal() {
        vm.writePop(Segment.LOCAL, 1);
        assertEquals("pop local 1\n", vm.getOutput());
    }

    @Test
    void testWritePopTemp() {
        vm.writePop(Segment.TEMP, 0);
        assertEquals("pop temp 0\n", vm.getOutput());
    }

    // ─── operações aritméticas ────────────────────────────────────────────────

    @Test
    void testWriteAdd() {
        vm.writeArithmetic(Command.ADD);
        assertEquals("add\n", vm.getOutput());
    }

    @Test
    void testWriteNeg() {
        vm.writeArithmetic(Command.NEG);
        assertEquals("neg\n", vm.getOutput());
    }

    @Test
    void testWriteNot() {
        vm.writeArithmetic(Command.NOT);
        assertEquals("not\n", vm.getOutput());
    }

    // ─── controle de fluxo ───────────────────────────────────────────────────

    @Test
    void testWriteLabel() {
        vm.writeLabel("WHILE_EXP0");
        assertEquals("label WHILE_EXP0\n", vm.getOutput());
    }

    @Test
    void testWriteGoto() {
        vm.writeGoto("WHILE_END0");
        assertEquals("goto WHILE_END0\n", vm.getOutput());
    }

    @Test
    void testWriteIf() {
        vm.writeIf("IF_TRUE0");
        assertEquals("if-goto IF_TRUE0\n", vm.getOutput());
    }

    // ─── funções ─────────────────────────────────────────────────────────────

    @Test
    void testWriteFunction() {
        vm.writeFunction("Main.main", 2);
        assertEquals("function Main.main 2\n", vm.getOutput());
    }

    @Test
    void testWriteCall() {
        vm.writeCall("Math.multiply", 2);
        assertEquals("call Math.multiply 2\n", vm.getOutput());
    }

    @Test
    void testWriteReturn() {
        vm.writeReturn();
        assertEquals("return\n", vm.getOutput());
    }

    // ─── múltiplos comandos em sequência ─────────────────────────────────────

    @Test
    void testSequenciaComandos() {
        // Simula: push constant 5 / push constant 3 / add / return
        vm.writePush(Segment.CONST, 5);
        vm.writePush(Segment.CONST, 3);
        vm.writeArithmetic(Command.ADD);
        vm.writeReturn();

        String expected =
            "push constant 5\n" +
            "push constant 3\n" +
            "add\n" +
            "return\n";

        assertEquals(expected, vm.getOutput());
    }

    @Test
    void testFuncaoCompleta() {
        // Simula uma função simples: function Main.soma 0 / push arg 0 / push arg 1 / add / return
        vm.writeFunction("Main.soma", 0);
        vm.writePush(Segment.ARG, 0);
        vm.writePush(Segment.ARG, 1);
        vm.writeArithmetic(Command.ADD);
        vm.writeReturn();

        String expected =
            "function Main.soma 0\n" +
            "push argument 0\n" +
            "push argument 1\n" +
            "add\n" +
            "return\n";

        assertEquals(expected, vm.getOutput());
    }
}