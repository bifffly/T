package t;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;

public class T {
    private final static Interpreter interpreter = new Interpreter();
    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Idiot...");
            System.out.println("Usage: tc <file>");
            System.exit(1);
        }
        else {
            runFile(args[0]);
        }
    }

    private static void runFile(String path) throws IOException {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            run(new String(bytes, Charset.defaultCharset()));
        }
        catch (NoSuchFileException e) {
            System.err.println("File not found.");
            System.exit(2);
        }
        if (hadError) {
            System.exit(5);
        }
        if (hadRuntimeError) {
            System.exit(10);
        }
    }

    private static void run(String text) {
        Tokenizer tokenizer = new Tokenizer(text);
        List<Token> tokens = tokenizer.tokenize();
        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();
        if (hadError) {
            return;
        }
        Resolver resolver = new Resolver(interpreter);
        resolver.resolveStmts(stmts);
        if (hadError) {
            return;
        }
        interpreter.interpret(stmts);
    }

    public static void error(Token token, String message) {
        error(token.getLine(), message);
    }

    public static void error(int line, String message) {
        report(line, message);
    }

    static void report(int line, String message) {
        System.err.println("[line " + line + "]: " + message);
        hadError = true;
    }

    static void runtimeError(RuntimeError e) {
        System.err.println("[line " + e.token.getLine() + "] " + e.getMessage());
        hadRuntimeError = true;
    }
}
