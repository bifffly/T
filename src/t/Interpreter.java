package t;

import t.includes.IO;
import t.includes.Time;
import t.types.*;
import t.types.Enum;

import java.util.*;
import java.util.Map;

import static t.TokenType.*;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private final Environment globals = new Environment();
    private final Map<String, String> types = new HashMap<>();
    private Environment env = globals;
    private StructType structStatus = StructType.NONE;
    private final Map<Expr, Integer> locals = new HashMap<>();

    private enum StructType {
        STRUCT, NONE
    }

    public Interpreter() {
        IO io = new IO(env);
        globals.define("io", new Namespace(
                new Token(ID, "io"),
                io.getFunctions(), new ArrayList<>(), new ArrayList<>()));
        Time time = new Time(env);
        globals.define("time", new Namespace(
                new Token(ID, "time"),
                time.getFunctions(), new ArrayList<>(), new ArrayList<>()
        ));
    }

    public void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return env.getAt(distance, name.getLexeme());
        }
        else {
            return globals.get(name);
        }
    }

    public Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt stmt : statements) {
                execute(stmt);
            }
        }
        catch (RuntimeError error) {
            T.runtimeError(error);
        }
    }

    public void execute(Stmt stmt) {
        stmt.accept(this);
    }

    public void executeBlock(List<Stmt> stmts, Environment env) {
        Environment prev = this.env;
        try {
            this.env = env;

            for (Stmt stmt : stmts) {
                execute(stmt);
            }
        }
        finally {
            this.env = prev;
        }
    }

    private void checkRealOperands(Token operator, Object left, Object right) {
        if (left instanceof Real && right instanceof Real) {
            return;
        }
        throw new RuntimeError(operator, "OperatorError",
                "Operands must be reals.");
    }

    private boolean isTruthy(Object o) {
        if (o instanceof Real) {
            return ((Real)o).getValue() != 0;
        }
        if (o instanceof Char) {
            return ((Char)o).getValue() != "";
        }
        if (o instanceof Bool) {
            return ((Bool)o).getValue();
        }
        return o != null;
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null) {
            return false;
        }
        return left.equals(right);
    }

    public String stringify(Object o) {
        if (o == null) {
            return "nil";
        }
        if (o instanceof Bool) {
            boolean bool = ((Bool)o).getValue();
            if (bool) {
                return "true";
            }
            else {
                return "false";
            }
        }
        return o.toString();
    }

    private String typeOf(Object o) {
        if (o instanceof Real) {
            return "real";
        }
        if (o instanceof Char) {
            return "char";
        }
        if (o instanceof Bool) {
            return "bool";
        }
        if (o instanceof Function) {
            return "fn";
        }
        if (o instanceof Struct) {
            return "struct";
        }
        if (o instanceof Instance) {
            return ((Instance)o).getStruct().returnType();
        }
        if (o == null) {
            return "void";
        }
        if (o instanceof Enum.EnumType) {
            return ((Enum.EnumType)o).getEnum().getName().getLexeme();
        }
        return "null";
    }

    public RuntimeError typeError(Token token, String expected, String received) {
        throw new RuntimeError(token, "TypeError", "Expected type " + expected +
                ", received type " + received + ".");
    }

    //=================================Expr====================================

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        if (expr.getTarget() instanceof Expr.Var) {
            Expr.Var varTarget = (Expr.Var)expr.getTarget();
            assignToVar(expr.getValue(), varTarget);
        }
        else if (expr.getTarget() instanceof Expr.Get) {
            Expr.Get getTarget = (Expr.Get)expr.getTarget();
            assignToGet(expr.getValue(), getTarget);
        }
        else {
            throw new RuntimeError(expr.getOperator(), "TargetError",
                    "Invalid assignment target.");
        }
        return null;
    }

    private void assignToVar(Expr expr, Expr.Var target) {
        Object value = evaluate(expr);
        String type = typeOf(value);
        if (types.get(target.getName().getLexeme()).equals("free") ||
                type.equals(types.get(target.getName().getLexeme()))) {
            env.assign(target.getName(), value);
        }
        else {
            throw typeError(target.getName(), types.get(target.getName().getLexeme()), type);
        }
    }

    public void assignToGet(Expr expr, Expr.Get target) {
        Object value = evaluate(expr);
        Object object = evaluate(target.getExpr());
        if (object instanceof Instance) {
            Instance instance = (Instance)object;
            if (structStatus == StructType.NONE &&
                    instance.getStruct().getProtectionFor(target.getName().getLexeme()) != AttrEnum.PUBLIC) {
                throw new RuntimeError(target.getName(), "AccessError",
                        "Cannot assign to private or protected attribute.");
            }
            if (instance.getStruct().getAttrs().containsKey(target.getName().getLexeme())) {
                if (instance.getStruct().getAttrs().get(target.getName().getLexeme()).equals(typeOf(value))) {
                    instance.set(target.getName(), value);
                }
                else {
                    throw typeError(target.getName(), instance.getStruct().getAttrs().get(target.getName().getLexeme()),
                            typeOf(value));
                }
            }
            else {
                throw new RuntimeError(target.getName(), "AttrError",
                        "Instance has no attribute '" + target.getName().getLexeme() + "'.");
            }
        }
        else {
            throw new RuntimeError(target.getName(), "Only instances have properties that can be assigned to.");
        }
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.getValue();
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.getExpr());
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object value = evaluate(expr.getExpr());
        switch (expr.getOperator().getType()) {
            case NOT: return !isTruthy(value);
            case MINUS: {
                if (value instanceof Real) {
                    Real real = (Real)value;
                    return new Real(-real.getValue());
                }
                else {
                    throw new RuntimeError(expr.getOperator(), "OperatorError",
                            "Can only negate reals.");
                }
            }
            case INCR: {
                if (value instanceof Real) {
                    double oldValue = ((Real) value).getValue();
                    double newValue = oldValue + 1;
                    Expr.Literal lit = new Expr.Literal(new Real(newValue));
                    Token assign = new Token(TokenType.ASSIGN, "=", null, expr.getOperator().getLine());
                    Expr.Assign exprAssign = new Expr.Assign(expr.getExpr(), lit, assign);
                    return visitAssignExpr(exprAssign);
                } else {
                    throw new RuntimeError(expr.getOperator(), "OperatorError",
                            "Can only increment reals.");
                }
            }
            case DECR: {
                if (value instanceof Real) {
                    double oldValue = ((Real) value).getValue();
                    double newValue = oldValue - 1;
                    Expr.Literal lit = new Expr.Literal(new Real(newValue));
                    Token assign = new Token(TokenType.ASSIGN, "=", null, expr.getOperator().getLine());
                    Expr.Assign exprAssign = new Expr.Assign(expr.getExpr(), lit, assign);
                    return visitAssignExpr(exprAssign);
                } else {
                    throw new RuntimeError(expr.getOperator(), "OperatorError",
                            "Can only increment reals.");
                }
            }
        }
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.getLeft());
        Object right = evaluate(expr.getRight());

        switch (expr.getOperator().getType()) {
            case MINUS:
                checkRealOperands(expr.getOperator(), left, right);
                return Real.subtract((Real)left, (Real)right);
            case SLASH:
                checkRealOperands(expr.getOperator(), left, right);
                return Real.divide((Real)left, (Real)right);
            case STAR:
                checkRealOperands(expr.getOperator(), left, right);
                return Real.multiply((Real)left, (Real)right);
            case PLUS:
                if (left instanceof Real && right instanceof Real) {
                    return Real.add((Real)left, (Real)right);
                }
                if (left instanceof Char && right instanceof Char) {
                    return new Char(((Char)left).getValue() + ((Char)right).getValue());
                }
                throw new RuntimeError(expr.getOperator(), "OperandError",
                        "Operands must be two reals or two chars.");
            case MOD:
                checkRealOperands(expr.getOperator(), left, right);
                return Real.mod((Real)left, (Real)right);
            case LESS:
                checkRealOperands(expr.getOperator(), left, right);
                return Real.less((Real)left, (Real)right);
            case LESS_EQ:
                checkRealOperands(expr.getOperator(), left, right);
                return Real.lessEq((Real)left, (Real)right);
            case GREATER:
                checkRealOperands(expr.getOperator(), left, right);
                return Real.greater((Real)left, (Real)right);
            case GREATER_EQ:
                checkRealOperands(expr.getOperator(), left, right);
                return Real.greaterEq((Real)left, (Real)right);
            case NOT_EQ:
                return !isEqual(left, right);
            case EQ:
                return isEqual(left, right);
        }
        return null;
    }

    @Override
    public Object visitVarExpr(Expr.Var expr) {
        return lookUpVariable(expr.getName(), expr);
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.getLeft());
        if (expr.getOperator().getType() == OR) {
            if (isTruthy(left)) {
                return left;
            }
        }
        else {
            if (!isTruthy(left)) {
                return left;
            }
        }
        return evaluate(expr.getRight());
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.getCallee());
        if (!(callee instanceof Callable)) {
            throw new RuntimeError(expr.getMarker(), "CallError",
                    "Can only call functions and structs.");
        }
        Callable callable = (Callable)callee;
        if (expr.getArgs().size() != callable.arity()) {
            throw new RuntimeError(expr.getMarker(), "ArgsError",
                    "Expected " + callable.arity() + " arguments, received " + expr.getArgs().size() + ".");
        }
        List<Object> args = new ArrayList<>();
        List<Expr.Param> params = callable.getParams();
        for (int i = 0; i < callable.arity(); i++) {
            Object arg = evaluate(expr.getArgs().get(i));
            Expr.Param param = params.get(i);
            String argType = typeOf(arg);
            String paramType = param.getType().getLexeme();
            if (argType.equals(paramType) || paramType.equals("free")) {
                args.add(arg);
            }
            else {
                throw new RuntimeError(param.getType(), "TypeError",
                        "Function " + callable.toString() + " expects parameter type " +
                                paramType + ", received parameter type " + argType);
            }
        }
        if (callable.isMethod() || callee instanceof Struct) {
            structStatus = StructType.STRUCT;
            Object toReturn = callable.call(this, args);
            if (!(expr.getCallee() instanceof Expr.Super)) {
                structStatus = StructType.NONE;
            }
            return toReturn;
        }
        if (callee instanceof Function) {
            Function func = (Function)callee;
            Object returnValue = callable.call(this, args);
            if (callable.returnType().equals(typeOf(returnValue)) || callable.returnType().equals("free")) {
                return returnValue;
            }
            else {
                Stmt.Function decl = func.getDeclaration();
                String expected = decl.getType().getLexeme();
                String received = typeOf(returnValue);
                throw new RuntimeError(decl.getType(), "TypeError",
                        "Function " + func.toString() + " expects return type " +
                                expected + ", received return type " + received);
            }
        }
        else {
            throw new RuntimeError(expr.getMarker(), "Fuck");
        }
    }

    @Override
    public Object visitSliceExpr(Expr.Slice expr) {
        Object slicee = evaluate(expr.getSlicee());
        if (slicee instanceof Char) {
            Char str = (Char)slicee;
            Object slicing = evaluate(expr.getSlicing());
            if (slicing instanceof Real) {
                int index = (int)Math.round(((Real)slicing).getValue());
                if (index < str.getValue().length()) {
                    return new Char(Character.toString(str.getValue().charAt(index)));
                }
                else {
                    throw new RuntimeError(expr.getToken(), "BoundsError",
                            "Index " + index + " out of bounds.");
                }
            }
            else {
                throw new RuntimeError(expr.getToken(), "SliceError",
                        "Can only slice chars at real indices.");
            }
        }
        else {
            throw new RuntimeError(expr.getToken(), "SliceError",
                    "Can only slice chars.");
        }
    }

    @Override
    public Object visitReturnExpr(Expr.Return expr) {
        Object ret = evaluate(expr.getExpr());
        String type = typeOf(ret);
        throw new Return(ret, type);
    }

    @Override
    public Object visitDeclarationExpr(Expr.Declaration expr) {
        Object value = null;
        if (expr.getValue() != null) {
            value = evaluate(expr.getValue());
            String type = typeOf(value);
            if (!type.equals(expr.getType().getLexeme()) && !expr.getType().getLexeme().equals("free")) {
                throw typeError(expr.getName(), expr.getType().getLexeme(), type);
            }
        }
        env.define(expr.getName().getLexeme(), value);
        types.put(expr.getName().getLexeme(), expr.getType().getLexeme());
        return null;
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.getExpr());
        if (object instanceof Enum) {
            Enum e = (Enum)object;
            return e.getEnum(expr.getName());
        }
        if (object instanceof Instance) {
            Instance instance = (Instance)object;
            if (structStatus == StructType.STRUCT ||
                    instance.getStruct().getProtectionFor(expr.getName().getLexeme()) != AttrEnum.PRIVATE) {
                return instance.get(expr.getName());
            }
            else {
                throw new RuntimeError(expr.getName(), "AccessError", "Cannot read private attribute.");
            }
        }
        if (object instanceof Namespace) {
            Namespace namespace = (Namespace)object;
            if (!namespace.isIncluded()) {
                throw new RuntimeError(expr.getName(), "IncludeError",
                        "Cannot access namespace that has not been included.");
            }
            else {
                Object member = namespace.getFunction(expr.getName());
                if (member == null) {
                    member = namespace.getStruct(expr.getName());
                    if (member == null) {
                        member = namespace.getNamespace(expr.getName());
                        if (member == null) {
                            throw new RuntimeError(expr.getName(), "AttrError",
                                    "Namespace " + namespace.toString() +
                                            " has no member function, struct, or namespace '" +
                                    expr.getName().getLexeme() + "'.");
                        }
                    }
                }
                return member;
            }
        }
        else {
            throw new RuntimeError(expr.getName(), "AttrError",
                    "Only instances and namespaces have attributes.");
        }
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.getToken(), expr);
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int distance = locals.get(expr);
        Struct superstruct = (Struct)env.getAt(distance, "super");
        Instance thisInstance = (Instance)env.getAt(distance - 1, "this");
        Function func = superstruct.getFunction(thisInstance, expr.getFunction().getLexeme());
        if (func == null) {
            throw new RuntimeError(expr.getFunction(), "AttrError",
                    "Superstruct has no attribute '" + expr.getFunction().getLexeme() + "'.");
        }
        return func;
    }

    @Override
    public Object visitParamExpr(Expr.Param expr) {
        return null;
    }

    @Override
    public Object visitMemberExpr(Expr.Member expr) {
        return null;
    }

    //=================================Stmt====================================


    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.getBody(), new Environment(env));
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.getCondition()))) {
            execute(stmt.getThenBlock());
        }
        else if (stmt.getElseBlock() != null) {
            execute(stmt.getElseBlock());
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.getCondition()))) {
            execute(stmt.getBody());
        }
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        execute(new Stmt.Expression(stmt.getInitializer()));
        while (isTruthy(evaluate(stmt.getCondition()))) {
            execute(stmt.getBody());
            execute(new Stmt.Expression(stmt.getIncrement()));
        }
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        Function function = new Function(stmt, env, false, stmt.isMethod());
        env.define(stmt.getName().getLexeme(), function);
        types.put(stmt.getName().getLexeme(), stmt.getType().getLexeme());
        return null;
    }

    @Override
    public Void visitEnumStmt(Stmt.Enum stmt) {
        Enum e = new Enum(stmt.getName(), stmt.getEnums());
        env.define(stmt.getName().getLexeme(), e);
        return null;
    }

    @Override
    public Void visitErrorStmt(Stmt.Error stmt) {
        throw new RuntimeError(stmt.getToken(), stmt.getType().getLexeme(), stmt.getMessage().getLexeme());
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.getExpr());
        return null;
    }

    @Override
    public Void visitStructStmt(Stmt.Struct stmt) {
        Object superstruct = null;
        if (stmt.getSuperstruct() != null) {
            superstruct = evaluate(stmt.getSuperstruct());
            if (!(superstruct instanceof Struct)) {
                throw new RuntimeError(stmt.getSuperstruct().getName(), "Superstruct must be a struct.");
            }
        }
        env.define(stmt.getName().getLexeme(), null);
        if (stmt.getSuperstruct() != null) {
            env = new Environment(env);
            env.define("super", superstruct);
        }
        Map<String, Function> methods = new HashMap<>();
        for (Stmt.Function method : stmt.getFunctions()) {
            boolean cons = method.getType().getType() == CONS;
            boolean name = method.getName().getLexeme().equals(stmt.getName().getLexeme());
            boolean isConstructor = cons && name;
            Function fun = new Function(method, env, isConstructor, method.isMethod());
            methods.put(method.getName().getLexeme(), fun);
        }
        Map<String, String> attrs = new HashMap<>();
        Map<String, AttrEnum> protections = new HashMap<>();
        if (stmt.getPrivateAttrs() != null) {
            for (Expr.Member attr : stmt.getPrivateAttrs()) {
                attrs.put(attr.getName().getLexeme(), attr.getType().getLexeme());
                protections.put(attr.getName().getLexeme(), AttrEnum.PRIVATE);
            }
        }
        if (stmt.getPublicAttrs() != null) {
            for (Expr.Member attr : stmt.getPublicAttrs()) {
                attrs.put(attr.getName().getLexeme(), attr.getType().getLexeme());
                protections.put(attr.getName().getLexeme(), AttrEnum.PUBLIC);
            }
        }
        if (stmt.getProtectedAttrs() != null) {
            for (Expr.Member attr : stmt.getProtectedAttrs()) {
                attrs.put(attr.getName().getLexeme(), attr.getType().getLexeme());
                protections.put(attr.getName().getLexeme(), AttrEnum.PROTECTED);
            }
        }
        Struct struct = new Struct(stmt.getName(), (Struct)superstruct, attrs, protections, methods);
        if (superstruct != null) {
            env = env.getEnclosing();
        }
        env.assign(stmt.getName(), struct);
        types.put(struct.returnType(), "struct");
        return null;
    }

    @Override
    public Void visitNamespaceStmt(Stmt.Namespace stmt) {
        env.define(stmt.getName().getLexeme(), null);
        Environment prev = env;
        Namespace namespace = null;
        try {
            env = new Environment(env);
            Map<String, Struct> structs = new HashMap<>();
            Map<String, Function> functions = new HashMap<>();
            Map<String, Namespace> namespaces = new HashMap<>();
            for (Stmt statement : stmt.getBody()) {
                execute(statement);
            }
            for (Stmt.Struct struct : stmt.getStructs()) {
                structs.put(struct.getName().getLexeme(), (Struct)env.get(struct.getName()));
            }
            for (Stmt.Function function : stmt.getFunctions()) {
                functions.put(function.getName().getLexeme(), (Function)env.get(function.getName()));
            }
            for (Stmt.Namespace nspace : stmt.getNamespaces()) {
                namespaces.put(nspace.getName().getLexeme(), (Namespace)env.get(nspace.getName()));
            }
            namespace = new Namespace(stmt.getName(), functions, structs, namespaces);
        }
        finally {
            env = prev;
        }
        env.assign(stmt.getName(), namespace);
        types.put(namespace.getName().getLexeme(), "namespace");
        return null;
    }

    @Override
    public Void visitIncludeStmt(Stmt.Include stmt) {
        Object object = evaluate(stmt.getExpr());
        if (object instanceof Namespace) {
            Namespace namespace = (Namespace)object;
            env.define(stmt.getToken().getLexeme(), namespace);
            namespace.setIncluded(true);
            return null;
        }
        else {
            throw new RuntimeError(stmt.getToken(), "Can only include namespaces.");
        }
    }
}
