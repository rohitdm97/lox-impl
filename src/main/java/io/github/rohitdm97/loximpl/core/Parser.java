package io.github.rohitdm97.loximpl.core;

import io.github.rohitdm97.loximpl.error.ErrorReport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.rohitdm97.loximpl.core.TokenType.AND;
import static io.github.rohitdm97.loximpl.core.TokenType.BANG;
import static io.github.rohitdm97.loximpl.core.TokenType.BANG_EQUAL;
import static io.github.rohitdm97.loximpl.core.TokenType.CLASS;
import static io.github.rohitdm97.loximpl.core.TokenType.COMMA;
import static io.github.rohitdm97.loximpl.core.TokenType.DOT;
import static io.github.rohitdm97.loximpl.core.TokenType.ELSE;
import static io.github.rohitdm97.loximpl.core.TokenType.EOF;
import static io.github.rohitdm97.loximpl.core.TokenType.EQUAL;
import static io.github.rohitdm97.loximpl.core.TokenType.EQUAL_EQUAL;
import static io.github.rohitdm97.loximpl.core.TokenType.FALSE;
import static io.github.rohitdm97.loximpl.core.TokenType.FOR;
import static io.github.rohitdm97.loximpl.core.TokenType.FUN;
import static io.github.rohitdm97.loximpl.core.TokenType.GREATER;
import static io.github.rohitdm97.loximpl.core.TokenType.GREATER_EQUAL;
import static io.github.rohitdm97.loximpl.core.TokenType.IDENTIFIER;
import static io.github.rohitdm97.loximpl.core.TokenType.IF;
import static io.github.rohitdm97.loximpl.core.TokenType.LEFT_BRACE;
import static io.github.rohitdm97.loximpl.core.TokenType.LEFT_PAREN;
import static io.github.rohitdm97.loximpl.core.TokenType.LESS;
import static io.github.rohitdm97.loximpl.core.TokenType.LESS_EQUAL;
import static io.github.rohitdm97.loximpl.core.TokenType.MINUS;
import static io.github.rohitdm97.loximpl.core.TokenType.NIL;
import static io.github.rohitdm97.loximpl.core.TokenType.NUMBER;
import static io.github.rohitdm97.loximpl.core.TokenType.OR;
import static io.github.rohitdm97.loximpl.core.TokenType.PLUS;
import static io.github.rohitdm97.loximpl.core.TokenType.PRINT;
import static io.github.rohitdm97.loximpl.core.TokenType.RETURN;
import static io.github.rohitdm97.loximpl.core.TokenType.RIGHT_BRACE;
import static io.github.rohitdm97.loximpl.core.TokenType.RIGHT_PAREN;
import static io.github.rohitdm97.loximpl.core.TokenType.SEMICOLON;
import static io.github.rohitdm97.loximpl.core.TokenType.SLASH;
import static io.github.rohitdm97.loximpl.core.TokenType.STAR;
import static io.github.rohitdm97.loximpl.core.TokenType.STRING;
import static io.github.rohitdm97.loximpl.core.TokenType.THIS;
import static io.github.rohitdm97.loximpl.core.TokenType.TRUE;
import static io.github.rohitdm97.loximpl.core.TokenType.VAR;
import static io.github.rohitdm97.loximpl.core.TokenType.WHILE;

class Parser {
    private static final Token END = new Token(EOF, "", null, Integer.MAX_VALUE);

    private final List<Token> tokens;
    private final ErrorReport report;
    private int current = 0;

    public Parser(List<Token> tokens, ErrorReport report) {
        this.tokens = tokens;
        this.report = report;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(CLASS)) return classDeclaration();
            if (match(FUN)) return funDeclaration("function");
            if (match(VAR)) return varDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER, "Expect class name.");
        consume(LEFT_BRACE, "Expect '{' before class body.");

        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(funDeclaration("method"));
        }

        consume(RIGHT_BRACE, "Expect '}' after class body.");
        return new Stmt.Class(name, methods);
    }

    // kind can be method/function
    private Stmt.Function funDeclaration(String kind) {
        Token name = consume(IDENTIFIER, String.format("Expect %s name.", kind));
        consume(LEFT_PAREN, String.format("Expect '(' after %s name.", kind));
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    // not exactly syntax error
                    //noinspection ThrowableNotThrown
                    error(peek(), "Can't have more than 255 parameters.");
                }

                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");

        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after the block.");
        return statements;
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");

        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");
        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");
        Stmt body = statement();

        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(
                    body,
                    new Stmt.Expression(increment)
            ));
        }
        if (condition == null) {
            condition = new Expr.Literal(true);
        }
        body = new Stmt.While(condition, body);
        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(
                    initializer,
                    body
            ));
        }

        return body;
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

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

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.object, get.name, value);
            }

            // parsing does not need to stop, not throwing the error
            //noinspection ThrowableNotThrown
            error(equals, "Invalid assignment target.");
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

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if(match(DOT)) {
                final Token name = consume(IDENTIFIER, "Expect property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(THIS)) return new Expr.This(previous());

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
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

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    // not exactly a syntax error
                    //noinspection ThrowableNotThrown
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return current >= tokens.size() || peek().type == EOF;
    }

    private Token peek() {
        if (current >= tokens.size()) return END;
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        if (token.type == EOF) {
            report.error("at end: " + message, token.line, "");
        } else {
            report.error("at " + token.lexeme + ": " + message, token.line, "");
        }
        return new ParseError();
    }

    private static class ParseError extends RuntimeException {
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

}
