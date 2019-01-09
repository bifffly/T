package t;

import java.util.List;

public abstract class Stmt {
    interface Visitor<T> {
        T visitFunctionStmt(Function stmt);
        T visitStructStmt(Struct stmt);
        T visitNamespaceStmt(Namespace stmt);
        T visitEnumStmt(Enum stmt);
        T visitForStmt(For stmt);
        T visitBlockStmt(Block stmt);
        T visitExpressionStmt(Expression stmt);
        T visitIfStmt(If stmt);
        T visitErrorStmt(Error stmt);
        T visitWhileStmt(While stmt);
        T visitIncludeStmt(Include stmt);
    }

    abstract <T> T accept(Visitor<T> visitor);

    public static class Function extends Stmt {
        private final Token type;
        private final Token name;
        private final List<Expr.Param> params;
        private boolean isMethod = false;
        private final List<Stmt> body;

        public Function(Token type, Token name, List<Expr.Param> params, List<Stmt> body) {
            this.type = type;
            this.name = name;
            this.params = params;
            this.body = body;
        }

        public Token getType() {
            return type;
        }

        public Token getName() {
            return name;
        }

        public List<Expr.Param> getParams() {
            return params;
        }

        public void setMethod(boolean method) {
            isMethod = method;
        }

        public boolean isMethod() {
            return isMethod;
        }

        public List<Stmt> getBody() {
            return body;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitFunctionStmt(this);
        }
    }

    public static class Struct extends Stmt {
        private final Token name;
        private final List<Struct> structs;
        private final List<Function> functions;
        private final List<Expr.Member> privateAttrs;
        private final List<Expr.Member> publicAttrs;
        private final List<Expr.Member> protectedAttrs;
        private final Expr.Var superstruct;

        public Struct(Token name, List<Function> functions, List<Struct> structs,
                      List<Expr.Member> privateAttrs, List<Expr.Member> publicAttrs,
                      List<Expr.Member> protectedAttrs, Expr.Var superstruct) {
            this.name = name;
            this.structs = structs;
            this.functions = functions;
            this.privateAttrs = privateAttrs;
            this.publicAttrs = publicAttrs;
            this.protectedAttrs = protectedAttrs;
            this.superstruct = superstruct;
        }

        public Token getName() {
            return name;
        }

        public List<Struct> getStructs() {
            return structs;
        }

        public List<Function> getFunctions() {
            return functions;
        }

        public List<Expr.Member> getPrivateAttrs() {
            return privateAttrs;
        }

        public List<Expr.Member> getPublicAttrs() {
            return publicAttrs;
        }

        public List<Expr.Member> getProtectedAttrs() {
            return protectedAttrs;
        }

        public Expr.Var getSuperstruct() {
            return superstruct;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitStructStmt(this);
        }
    }

    public static class Namespace extends Stmt {
        private final Token name;
        private final List<Function> functions;
        private final List<Struct> structs;
        private final List<Namespace> namespaces;
        private final List<Stmt> body;

        public Namespace(Token name, List<Function> functions, List<Struct> structs,
                         List<Namespace> namespaces, List<Stmt> body) {
            this.name = name;
            this.functions = functions;
            this.structs = structs;
            this.namespaces = namespaces;
            this.body = body;
        }

        public Token getName() {
            return name;
        }

        public List<Function> getFunctions() {
            return functions;
        }

        public List<Struct> getStructs() {
            return structs;
        }

        public List<Namespace> getNamespaces() {
            return namespaces;
        }

        public List<Stmt> getBody() {
            return body;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitNamespaceStmt(this);
        }
    }

    public static class Enum extends Stmt {
        private final Token name;
        private final List<Token> enums;

        public Enum(Token name, List<Token> enums) {
            this.name = name;
            this.enums = enums;
        }

        public Token getName() {
            return name;
        }

        public List<Token> getEnums() {
            return enums;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitEnumStmt(this);
        }
    }

    public static class For extends Stmt {
        private final Expr initializer;
        private final Expr condition;
        private final Expr increment;
        private final Block body;

        public For(Expr initializer, Expr condition, Expr increment, Block body) {
            this.initializer = initializer;
            this.condition = condition;
            this.increment = increment;
            this.body = body;
        }

        public Expr getInitializer() {
            return initializer;
        }

        public Expr getCondition() {
            return condition;
        }

        public Expr getIncrement() {
            return increment;
        }

        public Block getBody() {
            return body;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitForStmt(this);
        }
    }

    public static class Block extends Stmt {
        private final List<Stmt> body;

        public Block(List<Stmt> body) {
            this.body = body;
        }

        public List<Stmt> getBody() {
            return body;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    public static class Expression extends Stmt {
        private final Expr expr;

        public Expression(Expr expr) {
            this.expr = expr;
        }

        public Expr getExpr() {
            return expr;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    public static class If extends Stmt {
        private final Expr condition;
        private final Stmt thenBlock;
        private final Stmt elseBlock;

        public If(Expr condition, Stmt thenBlock, Stmt elseBlock) {
            this.condition = condition;
            this.thenBlock = thenBlock;
            this.elseBlock = elseBlock;
        }

        public Expr getCondition() {
            return condition;
        }

        public Stmt getThenBlock() {
            return thenBlock;
        }

        public Stmt getElseBlock() {
            return elseBlock;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    public static class Error extends Stmt {
        private final Token token;
        private final Token type;
        private final Token message;

        public Error(Token token, Token type, Token message) {
            this.token = token;
            this.type = type;
            this.message = message;
        }

        public Token getToken() {
            return token;
        }

        public Token getType() {
            return type;
        }

        public Token getMessage() {
            return message;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitErrorStmt(this);
        }
    }

    public static class While extends Stmt {
        private final Expr condition;
        private final Stmt body;

        public While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        public Expr getCondition() {
            return condition;
        }

        public Stmt getBody() {
            return body;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    public static class Include extends Stmt {
        private final Token token;
        private final Expr expr;

        public Include(Token token, Expr expr) {
            this.token = token;
            this.expr = expr;
        }

        public Token getToken() {
            return token;
        }

        public Expr getExpr() {
            return expr;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitIncludeStmt(this);
        }
    }
}
