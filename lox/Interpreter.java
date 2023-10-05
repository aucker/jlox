package lox.lox;

import java.util.ArrayList;
import java.util.List;

import static lox.lox.TokenType.MINUS;

// when we use public class, it will be accessed everywhere
// but a default *class* is project protected
class Interpreter implements Expr.Visitor<Object>,
                             Stmt.Visitor<Void> {
    final Environment globals = new Environment();
    //private Environment environment = new Environment();
    /*
    `environment` field tracks the *current* environment.
    `globals` field holds a fixed reference to the outermost global environment.
     */
    private Environment environment = globals;

    Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() { return 0; }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
        /*
        This defines a variable named "clock". Its value is a Java anonymous class
        that implements LoxCallable. The `clock()` function takes no arguments, so
        its arity is zero. The implementation of `call()` calls the corresponding
        Java function and converts the result to a double value in seconds.
         */
    }

    //void interpret(Expr expression) {
    //    try {
    //        Object value = evaluate(expression);
    //        System.out.println(stringify(value));
    //    } catch (RuntimeError error) {
    //        Lox.runtimeError(error);
    //    }
    //}
    /*
    Our interpreter is able to visit statements now, but we have some work
    to do to feed them to it. First, modify the old `interpret()` method
    int the Interpreter class to accept a list of statements
     */
    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    /*
    Evaluating literals
     */
    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if(!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    /*
    Evaluating parentheses
     */
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    // Evaluating unary expressions
    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
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

    /*
    This simply forwards to the environment which does the heavy lifting to
    make sure the variable is defined. With that, we've got rudimentary varaibles
    working
     */
    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        //return null;
        return environment.get(expr.name);
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

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void executeBlock(List<Stmt> statements,
                      Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    /*
    Up until now, the `environment` field in Interpreter always pointed to the same
    environment - the global one. Now, that field represents the *current* environment.
    That's the environment that corresponds to the innermost scope containing the code
    to be executed.
     */
    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    /*
    Java doesn't let you use `void` as a generic type argument for obscure reasons
    having to do with type erasure and this stack.
    Instead, there is a separate `Void` type specifically for this use. Sort of a
    "boxed void", like "Integer" is for "int".
     */
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }


    /*
    If the variable has an initializer, we evaluate it. If not, we have another
    choice to make. We could have made this a syntax error in the parser by
    requiring an initializer.
     */
    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        // there is one we need to notice is that
        // the PLUS operator is special
        // because It can be used to concatenate to string
        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case MINUS:
                checkNumberOperand(expr.operator, right);
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }

                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings");

//                break;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
        }

        // Unreachable
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        /*
        First, we evaluate the expression of the callee
        typically, this expression is just an identifier that looks up the function by its name,
        but it could be anything.
         */
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        /*
        perform the call. We do that by casting the callee to a LoxCallable and then
        invoking a `call()` method on it.
         */
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        /*
        we need to check the type ourselves first.
        we will throw an exception, but now we're throwing our own exception type,
        one that the interpreter knows to catch and report gracefully.
         */
        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren,
                    "Can only call functions and classes.");
        }

        LoxCallable function = (LoxCallable) callee;
        /*
        before invoking the callable, we check to see if the argument list's length matches
        the callable's arity.
         */
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
        }
        /*
        We *could* push the arity checking into the concrete implementation of call().
        But, since we'll have multiple classes implementing LoxCallable, that would end up
        with redundant validation spread across a few classes.
        Hoisting it up into the visit method lets us do it in one place.
         */
        return function.call(this, arguments);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

}
