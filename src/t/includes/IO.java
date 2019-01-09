package t.includes;

import t.*;
import t.types.Char;
import t.types.Function;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static t.TokenType.*;

public class IO {
    private Environment env;
    private List<Function> functions = new ArrayList<>();

    public IO(Environment env) {
        this.env = env;
        functions.add(print());
        functions.add(println());
        functions.add(input());
        functions.add(fprint());
    }

    public List<Function> getFunctions() {
        return functions;
    }

    private Function print() {
        return new Function(new Stmt.Function(new Token(VOID, "void", null, 0),
                new Token(ID, "print", null, 0),
                Arrays.asList(
                        new Expr.Param(new Token(FREE, "free", null, 0),
                                new Token(ID, "arg", null, 0))
                ), new ArrayList<>()), env, false, false) {
            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Object arg = args.get(0);
                System.out.print(interpreter.stringify(arg));
                return null;
            }

            @Override
            public String toString() {
                return "<void fn print>";
            }
        };
    }

    private Function println() {
        return new Function(new Stmt.Function(new Token(VOID, "void", null, 0),
                new Token(ID, "println", null, 0),
                Arrays.asList(
                        new Expr.Param(new Token(FREE, "free", null, 0),
                                new Token(ID, "arg", null, 0))
                ), new ArrayList<>()), env, false, false) {
            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Object arg = args.get(0);
                System.out.println(interpreter.stringify(arg));
                return null;
            }

            @Override
            public String toString() {
                return "<void fn println>";
            }
        };
    }

    private Function input() {
        return new Function(new Stmt.Function(new Token(VOID, "char", null, 0),
                new Token(ID, "input", null, 0),
                new ArrayList<>(), new ArrayList<>()), env, false, false) {
            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Scanner s = new Scanner(System.in);
                String string = s.nextLine();
                s.close();
                return new Char(string);
            }

            @Override
            public String toString() {
                return "<void fn print>";
            }
        };
    }

    private Function fprint() {
        return new Function(new Stmt.Function(new Token(VOID, "void", null, 0),
                new Token(ID, "fprint", null, 0),
                Arrays.asList(
                        new Expr.Param(new Token(CHAR, "char"), new Token(ID, "file")),
                        new Expr.Param(new Token(FREE, "char", null, 0),
                                new Token(ID, "arg", null, 0))
                ), new ArrayList<>()), env, false, false) {
            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Object arg0 = args.get(0);
                Object arg1 = args.get(1);
                if (arg0 instanceof Char && arg1 instanceof Char) {
                    String fileName = ((Char)arg0).getValue();
                    String toWrite = ((Char)arg1).getValue();
                    try {
                        PrintWriter pw = new PrintWriter(new FileWriter(fileName, true));
                        pw.write(toWrite);
                    }
                    catch (IOException e) {
                        throw new RuntimeError(new Token(ID, "fprint"), "FileError", "No such file.");
                    }
                }
                return null;
            }

            @Override
            public String toString() {
                return "<void fn print>";
            }
        };
    }
}
