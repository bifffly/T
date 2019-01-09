package t;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public Environment getEnclosing() {
        return enclosing;
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    private Environment ancestor(int n) {
        Environment env = this;
        for (int i = 0; i < n; i++) {
            if (env != null) {
                env = env.enclosing;
            }
        }
        return env;
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.getLexeme())) {
            values.put(name.getLexeme(), value);
            return;
        }
        if (enclosing != null) {
            enclosing.define(name.getLexeme(), value);
        }
        else {
            throw new RuntimeError(name, "Undefined variable '" + name.getLexeme() + "'.");
        }
    }

    public void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.getLexeme(), value);
    }

    public Object get(Token name) {
        if (values.containsKey(name.getLexeme())) {
            return values.get(name.getLexeme());
        }
        if (enclosing != null) {
            return enclosing.get(name);
        }
        else {
            throw new RuntimeError(name, "Undefined variable '" + name.getLexeme() + "'.");
        }
    }

    public Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }
}