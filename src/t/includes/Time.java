package t.includes;

import t.*;
import t.types.Function;
import t.types.Real;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static t.TokenType.*;
import static t.TokenType.ID;

public class Time {
    private Environment env;
    private List<Function> functions;

    public Time(Environment env) {
        this.env = env;
        this.functions = new ArrayList<>();
        functions.add(clock());
    }

    public List<Function> getFunctions() {
        return functions;
    }

    private Function clock() {
        return new Function(new Stmt.Function(new Token(REAL, "real"),
                new Token(ID, "clock", null, 0),
                new ArrayList<>(), new ArrayList<>()), env, false, false) {
            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                return new Real(System.currentTimeMillis());
            }

            @Override
            public String toString() {
                return "<real fn clock>";
            }
        };
    }
}
