package t.types;

import t.*;

import java.util.List;

public class Function implements Callable {
    private final Stmt.Function declaration;
    private Environment closure;
    private final boolean isConstructor;
    private final boolean isMethod;
    private final Token name;

    public Function(Stmt.Function declaration, Environment closure, boolean isConstructor,
                    boolean isMethod) {
        this.declaration = declaration;
        this.closure = closure;
        this.isConstructor = isConstructor;
        this.isMethod = isMethod;
        this.name = declaration.getName();
    }

    @Override
    public List<Expr.Param> getParams() {
        return declaration.getParams();
    }

    @Override
    public String returnType() {
        return declaration.getType().getLexeme();
    }

    public Token getName() {
        return name;
    }

    public boolean isMethod() {
        return isMethod;
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public Function bind(Instance instance) {
        Environment env = new Environment(closure);
        env.define("this", instance);
        return new Function(declaration, env, isConstructor, isMethod);
    }

    public Object call(Interpreter interpreter, List<Object> args) {
        Environment env = new Environment(closure);
        for (int i = 0; i < declaration.getParams().size(); i++) {
            env.define(declaration.getParams().get(i).getName().getLexeme(), args.get(i));
        }
        try {
            interpreter.executeBlock(declaration.getBody(), env);
        }
        catch (Return ret) {
            return ret.getValue();
        }
        if (isConstructor) {
            return closure.getAt(0, "this");
        }
        return null;
    }

    public int arity() {
        return declaration.getParams().size();
    }

    public String toString() {
        String type = "";
        if (declaration.getType() != null) {
            type = declaration.getType().getLexeme() + " ";
        }
        return "<" + type + "fn " + declaration.getName().getLexeme() + ">";
    }

    public Stmt.Function getDeclaration() {
        return declaration;
    }
}
