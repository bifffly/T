package t;

import java.util.ArrayList;
import java.util.List;

import static t.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {
        ParseError() {
            super();
        }

        ParseError(String message) {
            super(message);
        }
    }
    private List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private boolean isAtEnd() {
        return peek().getType() == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token peekNext() {
        return tokens.get(current + 1);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        else {
            return peek().getType() == type;
        }
    }

    private boolean check(TokenType... types) {
        boolean checked = false;
        for (TokenType type : types) {
            if (check(type)) {
                checked = true;
            }
        }
        return checked;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
    }

    private Token consume(String message, TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                return advance();
            }
        }
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        T.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
        if (peek().getType() == SEMICOLON) {
            return;
        }
        while (!isAtEnd()) {
            switch (peek().getType()) {
                case FN:
                case STRUCT:
                case ENUM:
                case PRIVATE:
                case PUBLIC:
                case FOR:
                case IF:
                case WHILE:
                case BLOCK:
                case REAL:
                case CHAR:
                case BOOL:
                case VOID:
                case FREE:
                case NAMESPACE:
                    return;
            }
            advance();
        }
    }

    //=========================================================================

    public List<Stmt> parse() {
        List<Stmt> stmts = new ArrayList<>();
        while (!isAtEnd()) {
            stmts.add(declaration());
        }
        return stmts;
    }

    //===============================Declaration===============================

    private Stmt declaration() {
        try {
            if (check(REAL, CHAR, BOOL, VOID, FREE, CONS, ID)) {
                Token type = peek();
                if (peekNext().getType() == FN) {
                    consume("", REAL, CHAR, BOOL, VOID, FREE, CONS, ID);
                    consume(FN, "");
                    return function(type);
                }
            }
            if (match(STRUCT)) {
                return struct();
            }
            if (match(NAMESPACE)) {
                return namespace();
            }
            if (match(ENUM)) {
                return enum_();
            }
            return statement();
        }
        catch (ParseError e) {
            synchronize();
            return null;
        }
    }

    private Expr varDeclaration(Token type) {
        Token name = consume(ID, "Expect identifier after type declaration.");
        Expr value = null;
        if (match(ASSIGN)) {
            value = expression();
        }
        return new Expr.Declaration(type, name, value);
    }

    private Stmt function(Token type) {
        List<Expr.Param> params = new ArrayList<>();
        Token name = consume(ID, "Expect identifier after function declaration.");
        consume(LEFT_PAREN, "Expect '(' after function name.");
        if (!check(RIGHT_PAREN)) {
            do {
                if (params.size() >= 32) {
                    throw error(peek(), "Parameter count exceeds 32.");
                }
                params.add(paramDeclaration());
            } while (match(COMMA) && !isAtEnd());
        }
        consume(RIGHT_PAREN, "Expect ')' after parameter list declaration.");
        consume(LEFT_CURLY, "Expect '{' before function body.");
        List<Stmt> body = new ArrayList<>();
        while (!check(RIGHT_CURLY) && !isAtEnd()) {
            body.add(declaration());
        }
        consume(RIGHT_CURLY, "Expect '}' after function body.");
        return new Stmt.Function(type, name, params, body);
    }

    private Expr.Param paramDeclaration() {
        Token type = consume("Expect parameter type", REAL, CHAR, BOOL, FREE, ID);
        Token name = consume(ID, "Expect parameter name");
        return new Expr.Param(type, name);
    }

    private Stmt struct() {
        Token name = consume(ID, "Expect identifier after struct declaration.");
        Expr.Var superstruct = null;
        if (match(EXTENDS)) {
            superstruct = new Expr.Var(consume(ID, "Expect identifier after 'extends' keyword."));
        }
        consume(LEFT_CURLY, "Expect '{' before struct body.");
        List<Expr.Member> privateAttrs = null;
        List<Expr.Member> publicAttrs = null;
        List<Expr.Member> protectedAttrs = null;
        for (int i = 0; i < 3; i++) {
            if (match(PRIVATE, PUBLIC, PROTECTED)) {
                Token protection = previous();
                consume(LEFT_CURLY, "Expect '{' before attribute block.");
                List<Expr.Member> attrs = memberList(protection);
                consume(RIGHT_CURLY, "Expect '}' after attribute block.");
                switch (protection.getType()) {
                    case PRIVATE: privateAttrs = attrs; break;
                    case PUBLIC: publicAttrs = attrs; break;
                    case PROTECTED: protectedAttrs = attrs; break;
                }
            }
        }
        List<Stmt.Function> functions = new ArrayList<>();
        List<Stmt.Struct> structs = new ArrayList<>();
        while (!check(RIGHT_CURLY) && !isAtEnd()) {
            Stmt stmt = declaration();
            if (stmt instanceof Stmt.Function) {
                Stmt.Function funcStmt = (Stmt.Function)stmt;
                funcStmt.setMethod(true);
                functions.add(funcStmt);
            }
            if (stmt instanceof Stmt.Struct) {
                structs.add((Stmt.Struct)stmt);
            }
        }
        consume(RIGHT_CURLY, "Expect '}' after struct body.");
        return new Stmt.Struct(name, functions, structs, privateAttrs, publicAttrs,
                protectedAttrs, superstruct);
    }

    private List<Expr.Member> memberList(Token protection) {
        List<Expr.Member> members = new ArrayList<>();
        while (!check(RIGHT_CURLY) && !isAtEnd()) {
            Expr.Param param = paramDeclaration();
            Expr.Member member = new Expr.Member(protection, param.getType(), param.getName());
            consume(SEMICOLON, "Expect ';' after attribute declaration.");
            members.add(member);
        }
        return members;
    }

    private Stmt namespace() {
        Token name = consume(ID, "Expect identifier after namespace declaration.");
        List<Stmt.Struct> structs = new ArrayList<>();
        List<Stmt.Function> functions = new ArrayList<>();
        List<Stmt.Namespace> namespaces = new ArrayList<>();
        List<Stmt> body = new ArrayList<>();
        consume(LEFT_CURLY, "Expec '{' before namespace body.");
        while (!check(RIGHT_CURLY) && !isAtEnd()) {
            Stmt stmt = declaration();
            if (stmt instanceof Stmt.Struct) {
                structs.add((Stmt.Struct)stmt);
            }
            if (stmt instanceof Stmt.Function) {
                functions.add((Stmt.Function)stmt);
            }
            if (stmt instanceof Stmt.Namespace) {
                namespaces.add((Stmt.Namespace)stmt);
            }
            body.add(stmt);
        }
        consume(RIGHT_CURLY, "Expect '}' after namespace body.");
        return new Stmt.Namespace(name, functions, structs, namespaces, body);
    }

    private Stmt enum_() {
        Token name = consume(ID, "Expect identifier after enum declaration.");
        List<Token> enums = new ArrayList<>();
        consume(LEFT_CURLY, "Expect '{' before enum body.");
        if (!check(RIGHT_CURLY)) {
            do {
                enums.add(consume(ID, "Expect enum element identifier."));
            } while (match(COMMA));
        }
        consume(RIGHT_CURLY, "Expect '}' after enum body.");
        return new Stmt.Enum(name, enums);
    }

    //=================================Statement===============================

    private Stmt statement() {
        if (match(FOR)) {
            return forStatement();
        }
        if (match(IF)) {
            return ifStatement("if");
        }
        if (match(ERROR)) {
            return errorStatement();
        }
        if (match(WHILE)) {
            return whileStatement();
        }
        if (match(INCLUDE)) {
            return includeStatement();
        }
        return expressionStatement();
    }

    private Stmt includeStatement() {
        Token include = previous();
        Expr name = expression();
        consume(SEMICOLON, "Expect ';' after include statement.");
        return new Stmt.Include(include, name);
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' before while condition.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after while condition.");
        consume(LEFT_CURLY, "Expect '{' before while body.");
        List<Stmt> body = new ArrayList<>();
        while (!check(RIGHT_PAREN) && !isAtEnd()) {
            body.add(declaration());
        }
        consume(RIGHT_CURLY, "Expect '}' after while body.");
        return new Stmt.While(condition, new Stmt.Block(body));
    }

    private Stmt errorStatement() {
        Token error = previous();
        consume(LEFT_PAREN, "Expect '(' after error declaration.");
        Token type = consume(ID, "Expect error type.");
        consume(COMMA, "Expect comma.");
        Token message = consume(STRING, "Expect error message.");
        consume(RIGHT_PAREN, "Expect ')' after error declaration.");
        return new Stmt.Error(error, type, message);
    }

    private Stmt ifStatement(String type) {
        consume(LEFT_PAREN, "Expect '(' before " + type + " condition.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after " + type + " condition.");
        consume(LEFT_CURLY, "Expect '{' before " + type + " body.");
        List<Stmt> thenStmts = new ArrayList<>();
        while (!check(RIGHT_CURLY) && !isAtEnd()) {
            thenStmts.add(declaration());
        }
        consume(RIGHT_CURLY, "Expect '}' after " + type + " body.");
        Stmt.Block thenBlock = new Stmt.Block(thenStmts);
        Stmt elseBranch = null;
        if (match(ELIF)) {
            elseBranch = ifStatement("elif");
        }
        if (match(ELSE)) {
            elseBranch = elseStatement();
        }
        return new Stmt.If(condition, thenBlock, elseBranch);
    }

    private Stmt elseStatement() {
        List<Stmt> stmts = new ArrayList<>();
        consume(LEFT_CURLY, "Expect '{' before else body.");
        while (!check(RIGHT_CURLY) && !isAtEnd()) {
            stmts.add(declaration());
        }
        consume(RIGHT_CURLY, "Expect '}' after else body.");
        return new Stmt.Block(stmts);
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after for loop declaration.");

        Expr initializer;
        if (match(SEMICOLON)) {
            throw error(previous(), "Expect for loop initializer.");
        }
        else {
            initializer = expression();
        }
        consume(SEMICOLON, "Expect ';' after for loop initializer.");

        Expr condition;
        if (match(SEMICOLON)) {
            throw error(previous(), "Expect for loop condition.");
        }
        else {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after for loop condition.");

        Expr increment;
        if (match(SEMICOLON)) {
            throw error(previous(), "Expect for loop increment.");
        }
        else {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for loop increment.");
        consume(LEFT_CURLY, "Expect '{' before for loop body.");
        List<Stmt> body = new ArrayList<>();
        while (!check(RIGHT_CURLY) && !isAtEnd()) {
            body.add(declaration());
        }
        consume(RIGHT_CURLY, "Expect '}' after for loop body.");
        return new Stmt.For(initializer, condition, increment, new Stmt.Block(body));
    }

    //===============================Expression================================

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();
        if (match(ASSIGN)) {
            Token assign = previous();
            Expr value = assignment();
            return new Expr.Assign(expr, value, assign);
        }
        return expr;
    }

    private Expr or() {
        Expr expr = and();
        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(NOT_EQ, EQ)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = addition();
        while (match(GREATER, GREATER_EQ, LESS, LESS_EQ)) {
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr addition() {
        Expr expr = multiplication();
        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr multiplication() {
        Expr expr = unary();
        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(NOT, MINUS)) {
            Token operator = previous();
            Expr expr = unary();
            return new Expr.Unary(expr, operator);
        }
        return increment();
    }

    private Expr increment() {
        Expr expr = call();
        if (match(INCR, DECR)) {
            Token operator = previous();
            return new Expr.Unary(expr, operator);
        }
        return expr;
    }

    private Expr call() {
        Expr expr = get();
        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            }
            else {
                break;
            }
        }
        return expr;
    }

    private Expr finishCall(Expr expr) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 32) {
                    error(peek(), "Argument count exceeds 32.");
                }
                arguments.add(expression());
            } while (match(COMMA) && !isAtEnd());
        }
        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments list.");
        return new Expr.Call(expr, arguments, paren);
    }

    private Expr get() {
        Expr expr = slice();
        while (match(DOT)) {
            Token name = consume(ID, "Expect identifier after '.'.");
            expr = new Expr.Get(expr, name);
        }
        return expr;
    }

    private Expr slice() {
        Expr expr = primary();
        while (match(LEFT_SQ)) {
            Token slice = previous();
            Expr slicing = expression();
            consume(RIGHT_SQ, "Expect ']' after slicing.");
            expr = new Expr.Slice(slice, expr, slicing);
        }
        return expr;
    }

    private Expr primary() {
        if (match(BOOLEAN, NIL, NUMBER, STRING)) {
            return new Expr.Literal(previous().getLiteral());
        }
        if (match(REAL, BOOL, CHAR, FREE, ID)) {
            Token type = previous();
            if (type.getType() == ID && !check(ID)) {
                return new Expr.Var(type);
            }
            else {
                return varDeclaration(type);
            }
        }
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        if (match(RETURN)) {
            Token ret = previous();
            Expr expr = expression();
            return new Expr.Return(ret, expr);
        }
        if (match(THIS)) {
            return new Expr.This(previous());
        }
        if (match(SUPER)) {
            Token sup = previous();
            consume(DOT, "Expect '.' after reference to super.");
            Token func = consume(ID, "Expect superstruct method here.");
            return new Expr.Super(sup, func);
        }
        throw error(peek(), "Expect expression.");
    }
}
