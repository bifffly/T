package t.types;


import t.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Namespace {
    private final Token name;
    private final Map<String, Function> functions;
    private final Map<String, Struct> structs;
    private final Map<String, Namespace> namespaces;
    private boolean isIncluded = false;

    public Namespace(Token name, List<Function> functionList, List<Struct> structList,
              List<Namespace> namespaceList) {
        this.name = name;
        functions = new HashMap<>();
        for (Function function: functionList) {
            functions.put(function.getName().getLexeme(), function);
        }
        structs = new HashMap<>();
        for (Struct struct : structList) {
            structs.put(struct.getName().getLexeme(), struct);
        }
        namespaces = new HashMap<>();
        for (Namespace nspace : namespaceList) {
            namespaces.put(nspace.getName().getLexeme(), nspace);
        }
    }

    public Namespace(Token name, Map<String, Function> functions, Map<String, Struct> structs,
                      Map<String, Namespace> namespaces) {
        this.name = name;
        this.functions = functions;
        this.structs = structs;
        this.namespaces = namespaces;
    }

    public Token getName() {
        return name;
    }

    public boolean isIncluded() {
        return isIncluded;
    }

    public void setIncluded(boolean isIncluded) {
        this.isIncluded = isIncluded;
    }

    public Function getFunction(Token name) {
        return functions.getOrDefault(name.getLexeme(), null);
    }

    public Struct getStruct(Token name) {
        return structs.getOrDefault(name.getLexeme(), null);
    }

    public Namespace getNamespace(Token name) {
        return namespaces.getOrDefault(name.getLexeme(), null);
    }

    @Override
    public String toString() {
        return "<namespace " + name.getLexeme() + ">";
    }
}
