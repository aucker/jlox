package lox.lox;

class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Binary expr) {
//        return null;
        return parenthesize(expr.operator.lexeme,
                            expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Grouping expr) {
//        return null;
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Literal expr) {
//        return null;
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Unary expr) {
//        return null;
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    public static void main(String[] args) {
        Expr expression = new Expr.Visitor.Binary(
                new Expr.Visitor.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Visitor.Literal(123)
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Visitor.Grouping(
                        new Expr.Visitor.Literal(45.67)
                )
        );
        System.out.println(new AstPrinter().print(expression));
    }

    @Override
    public <R> R accept(Expr.Visitor<R> visitor) {
        return null;
    }

}
