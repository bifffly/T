package t.types;

import t.RuntimeError;
import t.Token;

import java.util.HashMap;

public class Instance {
    private Struct struct;
    private final java.util.Map<String, Object> attrs = new HashMap<>();

    Instance(Struct struct) {
        this.struct = struct;
    }

    public Struct getStruct() {
        return struct;
    }

    public Object get(Token name) {
        if (attrs.containsKey(name.getLexeme())) {
            return attrs.get(name.getLexeme());
        }
        Function function = struct.getFunction(this, name.getLexeme());
        if (function != null) {
            return function;
        }
        throw new RuntimeError(name, "Undefined property '" + name.getLexeme() + "'.");
    }

    public void set(Token name, Object value) {
        attrs.put(name.getLexeme(), value);
    }

    @Override
    public String toString() {
        return "<instance of struct " + struct.getName().getLexeme() + ">";
    }
}
