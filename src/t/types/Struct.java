package t.types;

import t.AttrEnum;
import t.Expr;
import t.Interpreter;
import t.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Struct implements Callable {
    private final Token name;
    private final Struct superstruct;
    private final Map<String, String> attrs;
    private final Map<String, AttrEnum> protections;
    private final Map<String, Function> functions;
    private Function constructor = null;

    public Struct(Token name, Struct superstruct, Map<String, String> attrs,
                  Map<String, AttrEnum> protections, Map<String, Function> functions) {
        this.name = name;
        this.superstruct = superstruct;
        this.attrs = attrs;
        this.protections = protections;
        this.functions = functions;
        Function cons = functions.get(name.getLexeme());
        if (cons != null && cons.isConstructor()) {
            constructor = cons;
        }
    }

    @Override
    public String returnType() {
        return name.getLexeme();
    }

    @Override
    public boolean isMethod() {
        return false;
    }

    public Function getConstructor() {
        return constructor;
    }

    public AttrEnum getProtectionFor(String attr) {
        return protections.getOrDefault(attr, AttrEnum.PUBLIC);
    }

    public Token getName() {
        return name;
    }

    public Map<String, String> getAttrs() {
        return attrs;
    }

    public Function getFunction(Instance instance, String name) {
        if (functions.containsKey(name)) {
            return functions.get(name).bind(instance);
        }
        else if (superstruct != null) {
            return superstruct.getFunction(instance, name);
        }
        else {
            return null;
        }
    }

    public Object call(Interpreter interpreter, List<Object> args) {
        Instance instance = new Instance(this);
        if (constructor != null) {
            constructor.bind(instance).call(interpreter, args);
        }
        return instance;
    }

    @Override
    public List<Expr.Param> getParams() {
        Function constructor = functions.get(name.getLexeme());
        if (constructor != null) {
            return constructor.getParams();
        }
        else {
            return new ArrayList<>();
        }
    }

    public int arity() {
        if (constructor != null) {
            return constructor.arity();
        }
        else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "<struct " + name.getLexeme() + ">";
    }
}
