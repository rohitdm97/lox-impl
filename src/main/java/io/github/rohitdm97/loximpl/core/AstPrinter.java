package io.github.rohitdm97.loximpl.core;

import java.util.ArrayList;
import java.util.List;

public class AstPrinter implements Expr.Visitor<String> {

    String print(Expr expr) {
        return expr.accept(this);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder sb = new StringBuilder();

        sb.append("(").append(name);
        for (Expr expr : exprs) {
            sb.append(" ");
            sb.append(expr.accept(this));
        }
        sb.append(")");

        return sb.toString();
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        final String initializer = expr.value == null ? null : expr.value.accept(this);
        return String.format("(assign (%s) (%s))", expr.name.lexeme, initializer);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        String calle = expr.callee.accept(this);

        List<String> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(argument.accept(this));
        }

        return String.format("%s[ %s ]", calle, arguments);
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        final String object = expr.object.accept(this);
        return String.format("(get %s %s)", object, expr.name);
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        final String object = expr.object.accept(this);
        final String value = expr.value.accept(this);
        return String.format("(set %s %s ---> %s)", object, expr.name.lexeme, value);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }

}
