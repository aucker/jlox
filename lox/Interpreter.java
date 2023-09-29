package lox.lox;

import static lox.lox.TokenType.MINUS;

// when we use public class, it will be accessed everywhere
// but a default *class* is project protected
class Interpreter implements Expr.Visitor<Object> {

    /*
    Evaluating literals
     */
    @Override
    public Object visitLiteralExpr(Expr.Visitor.Literal expr) {
        return expr.value;
    }

    /*
    Evaluating parentheses
     */
    @Override
    public Object visitGroupingExpr(Expr.Visitor.Grouping expr) {
        return evaluate(expr.expression);
    }

    // Evaluating unary expressions
    @Override
    public Object visitUnaryExpr(Expr.Visitor.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                return -(double)right;
        }

        // Unreachable
        return null;
    }

    @Override
    public <R> R accept(Expr.Visitor<R> visitor) {
        return null;
    }

    // Truthiness and falsiness
    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitBinaryExpr(Expr.Visitor.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        // there is one we need to notice is that
        // the PLUS operator is special
        // because It can be used to concatenate to string
        switch (expr.operator.type) {
            case GREATER:
                return (double)left > (double)right;
            case GREATER_EQUAL:
                return (double)left >= (double)right;
            case LESS:
                return (double)left < (double)right;
            case LESS_EQUAL:
                return (double)left <= (double)right;
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }

                break;
            case SLASH:
                return (double)left / (double)right;
            case STAR:
                return (double)left * (double)right;
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
        }

        // Unreachable
        return null;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

}
