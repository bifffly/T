package t.types;

import t.Expr;
import t.Interpreter;
import t.Token;

import java.util.List;

public interface Callable {
    String returnType();
    Token getName();
    Object call(Interpreter interpreter, List<Object> args);
    List<Expr.Param> getParams();
    int arity();
    boolean isMethod();
}
