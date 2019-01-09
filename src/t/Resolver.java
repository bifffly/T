package t;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Stmt.Visitor<Void>, Expr.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private StructType currentStruct = StructType.NONE;

    private enum FunctionType {
        NONE, FUNCTION, INIT, METHOD
    }

    private enum StructType {
        NONE, STRUCT, SUBSTRUCT
    }

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    void resolveStmts(List<Stmt> statements) {
        for (Stmt stmt : statements) {
            resolve(stmt);
        }
    }

    private void resolve(Stmt statement) {
        statement.accept(this);
    }

    private void resolveExprs(List<Expr> exprs) {
        for (Expr expr : exprs) {
            resolve(expr);
        }
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.getLexeme())) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }

    private void resolveFunction(Stmt.Function stmt, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();
        for (Expr.Param param : stmt.getParams()) {
            declare(param.getName());
            define(param.getName());
        }
        resolveStmts(stmt.getBody());
        endScope();
        currentFunction = enclosingFunction;
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (!scopes.isEmpty()) {
            Map<String, Boolean> scope = scopes.peek();
            scope.put(name.getLexeme(), false);
        }
    }

    private void define(Token name) {
        if (!scopes.isEmpty()) {
            Map<String, Boolean> scope = scopes.peek();
            scope.put(name.getLexeme(), true);
        }
    }

    //=========================================================================

    @Override
    public Void visitVarExpr(Expr.Var expr) {
        if (!scopes.isEmpty()) {
            Map<String, Boolean> scope = scopes.peek();
            String name = expr.getName().getLexeme();
            boolean defined = true;
            if (scope.containsKey(name)) {
                defined = scope.get(name);
            }
            if (!defined) {
                T.error(expr.getName(), "Cannot read local variable in its own initializer.");
            }
            else {
                resolveLocal(expr, expr.getName());
            }
        }
        else {
            resolveLocal(expr, expr.getName());
        }
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.getValue());
        if (expr.getTarget() instanceof Expr.Var) {
            Expr.Var var = (Expr.Var)expr.getTarget();
            resolveLocal(expr, var.getName());
        }
        if (expr.getTarget() instanceof Expr.Get) {
            Expr.Get get = (Expr.Get)expr.getTarget();
            resolve(get);
        }
        if (expr.getTarget() instanceof Expr.Slice) {
            Expr.Slice slice = (Expr.Slice)expr.getTarget();
            resolve(slice);
        }
        return null;
    }

    @Override
    public Void visitReturnExpr(Expr.Return expr) {
        if (currentFunction == FunctionType.NONE) {
            T.error(expr.getToken(), "Cannot return from global scope.");
        }
        if (expr.getExpr() != null) {
            if (currentFunction == FunctionType.INIT) {
                T.error(expr.getToken(), "Cannot return from init.");
            }
            resolve(expr.getExpr());
        }
        return null;
    }

    @Override
    public Void visitSliceExpr(Expr.Slice expr) {
        resolve(expr.getSlicing());
        resolve(expr.getSlicee());
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.getLeft());
        resolve(expr.getRight());
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.getCallee());
        for (Expr arg : expr.getArgs()) {
            resolve(arg);
        }
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.getExpr());
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.getLeft());
        resolve(expr.getRight());
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.getExpr());
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.getExpr());
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        resolveLocal(expr, expr.getToken());
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        resolveLocal(expr, expr.getToken());
        return null;
    }

    @Override
    public Void visitParamExpr(Expr.Param expr) {
        return null;
    }

    @Override
    public Void visitDeclarationExpr(Expr.Declaration expr) {
        declare(expr.getName());
        if (expr.getValue() != null) {
            resolve(expr.getValue());
        }
        define(expr.getName());
        return null;
    }

    @Override
    public Void visitMemberExpr(Expr.Member expr) {
        return null;
    }

    //=========================================================================

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolveStmts(stmt.getBody());
        endScope();
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.getName());
        define(stmt.getName());
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.getExpr());
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.getCondition());
        resolve(stmt.getThenBlock());
        if (stmt.getElseBlock() != null) {
            resolve(stmt.getElseBlock());
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.getCondition());
        resolve(stmt.getBody());
        return null;
    }

    @Override
    public Void visitStructStmt(Stmt.Struct stmt) {
        StructType enclosingType = currentStruct;
        currentStruct = StructType.STRUCT;
        declare(stmt.getName());
        if (stmt.getSuperstruct() != null) {
            currentStruct = StructType.SUBSTRUCT;
            resolve(stmt.getSuperstruct());
        }
        define(stmt.getName());
        if (stmt.getSuperstruct() != null) {
            beginScope();
            scopes.peek().put("super", true);
        }
        beginScope();
        scopes.peek().put("this", true);
        for (Stmt.Function method : stmt.getFunctions()) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.getName().getLexeme().equals("init")) {
                declaration = FunctionType.INIT;
            }
            resolveFunction(method, declaration);
        }
        endScope();
        if (stmt.getSuperstruct() != null) {
            endScope();
        }
        currentStruct = enclosingType;
        return null;
    }

    @Override
    public Void visitNamespaceStmt(Stmt.Namespace stmt) {
        define(stmt.getName());
        beginScope();
        for (Stmt.Struct struct : stmt.getStructs()) {
            resolve(struct);
        }
        for (Stmt.Function function : stmt.getFunctions()) {
            resolve(function);
        }
        for (Stmt.Namespace namespace : stmt.getNamespaces()) {
            resolve(namespace);
        }
        resolveStmts(stmt.getBody());
        endScope();
        return null;
    }

    @Override
    public Void visitErrorStmt(Stmt.Error stmt) {
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        resolve(stmt.getInitializer());
        resolve(stmt.getCondition());
        resolve(stmt.getIncrement());
        resolve(stmt.getBody());
        return null;
    }

    @Override
    public Void visitEnumStmt(Stmt.Enum stmt) {
        define(stmt.getName());
        return null;
    }

    @Override
    public Void visitIncludeStmt(Stmt.Include stmt) {
        resolve(stmt.getExpr());
        return null;
    }
}
