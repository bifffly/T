package t;

import java.util.List;

public abstract class Expr {
    interface Visitor<T> {
        T visitDeclarationExpr(Declaration expr);
        T visitVarExpr(Var expr);
        T visitLiteralExpr(Literal expr);
        T visitGroupingExpr(Grouping expr);
        T visitAssignExpr(Assign expr);
        T visitBinaryExpr(Binary expr);
        T visitUnaryExpr(Unary expr);
        T visitLogicalExpr(Logical expr);
        T visitCallExpr(Call expr);
        T visitGetExpr(Get expr);
        T visitSliceExpr(Slice expr);
        T visitReturnExpr(Return expr);
        T visitThisExpr(This expr);
        T visitSuperExpr(Super expr);
        T visitParamExpr(Param expr);
        T visitMemberExpr(Member expr);
    }

    abstract <T> T accept(Expr.Visitor<T> visitor);

    public static class Declaration extends Expr {
        private final Token type;
        private final Token name;
        private final Expr value;

        public Declaration(Token type, Token name, Expr value) {
            this.type = type;
            this.name = name;
            this.value = value;
        }

        public Token getType() {
            return type;
        }

        public Token getName() {
            return name;
        }

        public Expr getValue() {
            return value;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitDeclarationExpr(this);
        }
    }

    public static class Var extends Expr {
        private final Token name;

        public Var(Token name) {
            this.name = name;
        }

        public Token getName() {
            return name;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitVarExpr(this);
        }
    }

    public static class Literal extends Expr {
        private final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    public static class Grouping extends Expr {
        private final Expr expr;

        public Grouping(Expr expr) {
            this.expr = expr;
        }

        public Expr getExpr() {
            return expr;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    public static class Assign extends Expr {
        private final Expr target;
        private final Expr value;
        private final Token operator;

        public Assign(Expr target, Expr value, Token operator) {
            this.target = target;
            this.value = value;
            this.operator = operator;
        }

        public Expr getTarget() {
            return target;
        }

        public Expr getValue() {
            return value;
        }

        public Token getOperator() {
            return operator;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    public static class Binary extends Expr {
        private final Expr left;
        private final Token operator;
        private final Expr right;

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public Expr getLeft() {
            return left;
        }

        public Token getOperator() {
            return operator;
        }

        public Expr getRight() {
            return right;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    public static class Unary extends Expr {
        private final Expr expr;
        private final Token operator;

        public Unary(Expr expr, Token operator) {
            this.expr = expr;
            this.operator = operator;
        }

        public Expr getExpr() {
            return expr;
        }

        public Token getOperator() {
            return operator;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    public static class Logical extends Expr {
        private final Expr left;
        private final Token operator;
        private final Expr right;

        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public Expr getLeft() {
            return left;
        }

        public Token getOperator() {
            return operator;
        }

        public Expr getRight() {
            return right;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    public static class Call extends Expr {
        private final Expr callee;
        private final List<Expr> args;
        private final Token marker;

        public Call(Expr callee, List<Expr> args, Token marker) {
            this.callee = callee;
            this.args = args;
            this.marker = marker;
        }

        public Expr getCallee() {
            return callee;
        }

        public List<Expr> getArgs() {
            return args;
        }

        public Token getMarker() {
            return marker;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    public static class Get extends Expr {
        private final Expr expr;
        private final Token name;

        public Get(Expr expr, Token name) {
            this.expr = expr;
            this.name = name;
        }

        public Expr getExpr() {
            return expr;
        }

        public Token getName() {
            return name;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    public static class Slice extends Expr {
        private final Token token;
        private final Expr slicee;
        private final Expr slicing;

        public Slice(Token token, Expr slicee, Expr slicing) {
            this.token = token;
            this.slicee = slicee;
            this.slicing = slicing;
        }

        public Token getToken() {
            return token;
        }

        public Expr getSlicee() {
            return slicee;
        }

        public Expr getSlicing() {
            return slicing;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitSliceExpr(this);
        }
    }

    public static class Return extends Expr {
        private final Token token;
        private final Expr expr;

        public Return(Token token, Expr expr) {
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
            return visitor.visitReturnExpr(this);
        }
    }

    public static class This extends Expr {
        private final Token token;

        public This(Token token) {
            this.token = token;
        }

        public Token getToken() {
            return token;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitThisExpr(this);
        }
    }

    public static class Super extends Expr {
        private final Token token;
        private final Token func;

        public Super(Token token, Token func) {
            this.token = token;
            this.func = func;
        }

        public Token getToken() {
            return token;
        }

        public Token getFunction() {
            return func;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitSuperExpr(this);
        }
    }

    public static class Param extends Expr {
        private final Token type;
        private final Token name;

        public Param(Token type, Token name) {
            this.type = type;
            this.name = name;
        }

        public Token getType() {
            return type;
        }

        public Token getName() {
            return name;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitParamExpr(this);
        }
    }

    public static class Member extends Expr {
        private final Token protection;
        private final Token type;
        private final Token name;

        public Member(Token protection, Token type, Token name) {
            this.protection = protection;
            this.type = type;
            this.name = name;
        }

        public Token getProtection() {
            return protection;
        }

        public Token getType() {
            return type;
        }

        public Token getName() {
            return name;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitMemberExpr(this);
        }
    }
}
