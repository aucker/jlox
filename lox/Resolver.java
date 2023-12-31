package lox.lox;

import java.util.*;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
	private final Interpreter interpreter;
	private final Stack<Map<String, Boolean>> scopes = new Stack<>();
	//private Stmt.Var stmt;
	private FunctionType currentFunction = FunctionType.NONE;

	Resolver(Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	private enum FunctionType {
		NONE,
		FUNCTION,
		INITIALIZER,
		METHOD,
	}

	private enum ClassType {
		NONE,
		CLASS,
		SUBCLASS
	}

	private ClassType currentClass = ClassType.NONE;

	void resolve(List<Stmt> statements) {
		for (Stmt statement : statements) {
			resolve(statement);
		}
	}

	@Override
	public Void visitBlockStmt(Stmt.Block stmt) {
		return null;
	}

	@Override
	public Void visitClassStmt(Stmt.Class stmt) {
		ClassType enclosingClass = currentClass;
		currentClass = ClassType.CLASS;

		declare(stmt.name);
		define(stmt.name);

		if (stmt.superclass != null &&
			stmt.name.lexeme.equals(stmt.superclass.name.lexeme)) {
			Lox.error(stmt.superclass.name,
					"A class can't inherit from itself.");
		}

		/*
		We only resolve the case when the superclass is not null.
		 */
		if (stmt.superclass != null) {
			currentClass = ClassType.SUBCLASS;
			//resolve(Collections.singletonList(stmt.superclass));
			resolve(stmt.superclass);
		}

		/*
		A minor optimization, but we only create the superclass environment if the class actually *has* a superclass.
		There's no point creating it when there isn't a superclass
		since there'd be no superclass to store in it anyway
		 */
		if (stmt.superclass != null) {
			beginScope();
			scopes.peek().put("super", true);
		}

		beginScope();
		scopes.peek().put("this", true);

		for (Stmt.Function method : stmt.methods) {
			FunctionType declaration = FunctionType.METHOD;
			if (method.name.lexeme.equals("init")) {
				declaration = FunctionType.INITIALIZER;
			}

			resolveFunction(method, declaration);
		}

		endScope();

		if (stmt.superclass != null) endScope();

		currentClass = enclosingClass;
		return null;
	}

	@Override
	public Void visitExpressionStmt(Stmt.Expression stmt) {
		resolve(stmt.expression);
		return null;
	}

	@Override
	public Void visitFunctionStmt(Stmt.Function stmt) {
		declare(stmt.name);
		define(stmt.name);

		//resolveFunction(stmt);
		resolveFunction(stmt, FunctionType.FUNCTION);
		return null;
	}

	@Override
	public Void visitIfStmt(Stmt.If stmt) {
		resolve(stmt.condition);
		resolve(stmt.thenBranch);
		if (stmt.elseBranch != null) resolve(stmt.elseBranch);
		return null;
	}

	@Override
	public Void visitPrintStmt(Stmt.Print stmt) {
		resolve(stmt.expression);
		return null;
	}

	@Override
	public Void visitReturnStmt(Stmt.Return stmt) {
		if (currentFunction == FunctionType.NONE) {
			Lox.error(stmt.keyword, "Can't return from top-level code.");
		}

		if (stmt.value != null) {
			/*
			When we later traverse into a `return` statement, we check that field and make it an
			error to return a value from inside an `init()` method
			 */
			if (currentFunction == FunctionType.INITIALIZER) {
				Lox.error(stmt.keyword,
						"Can't return a value from an initializer.");
			}
			resolve(stmt.value);
		}

		return null;
	}

	@Override
	public Void visitVarStmt(Stmt.Var stmt) {
		declare(stmt.name);
		if (stmt.initializer != null) {
			resolve(stmt.initializer);
		}
		define(stmt.name);
		return null;
	}

	@Override
	public Void visitWhileStmt(Stmt.While stmt) {
		resolve(stmt.condition);
		resolve(stmt.body);
		return null;
	}

	@Override
	public Void visitAssignExpr(Expr.Assign expr) {
		resolve(expr.value);
		resolveLocal(expr, expr.name);
		return null;
	}

	@Override
	public Void visitBinaryExpr(Expr.Binary expr) {
		resolve(expr.left);
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visitCallExpr(Expr.Call expr) {
		resolve(expr.callee);

		for (Expr argument : expr.arguments) {
			resolve(argument);
		}

		return null;
	}

	@Override
	public Void visitGetExpr(Expr.Get expr) {
		resolve(expr.object);
		return null;
	}

	@Override
	public Void visitLiteralExpr(Expr.Literal expr) {
		return null;
	}

	@Override
	public Void visitLogicalExpr(Expr.Logical expr) {
		resolve(expr.left);
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visitSetExpr(Expr.Set expr) {
		resolve(expr.value);
		resolve(expr.object);
		return null;
	}

	@Override
	public Void visitSuperExpr(Expr.Super expr) {
		if (currentClass == ClassType.NONE) {
			Lox.error(expr.keyword,
					"Can't use 'super' outside of a class.");
		} else if (currentClass != ClassType.SUBCLASS) {
			Lox.error(expr.keyword,
					"Can't use 'super' in a class with no superclass");
		}

		resolveLocal(expr, expr.keyword);
		return null;
	}

	@Override
	public Void visitThisExpr(Expr.This expr) {
		if (currentClass == ClassType.NONE) {
			Lox.error(expr.keyword,
					"Can't use 'this' outside of a class.");
			return null;
		}

		resolveLocal(expr, expr.keyword);
		return null;
	}

	@Override
	public Void visitUnaryExpr(Expr.Unary expr) {
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visitGroupingExpr(Expr.Grouping expr) {
		resolve(expr.expression);
		return null;
	}

	@Override
	public Void visitVariableExpr(Expr.Variable expr) {
		if (!scopes.isEmpty() &&
				scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
			Lox.error(expr.name, "Can't read local variable in " +
					"its own initializer.");
		}

		resolveLocal(expr, expr.name);
		return null;
	}

	private void resolve(Stmt stmt) {
		stmt.accept(this);
	}

	private void resolve(Expr expr) {
		expr.accept(this);
	}

	//private void resolveFunction(Stmt.Function function) {
	private void resolveFunction(Stmt.Function function, FunctionType type) {
		FunctionType enclosingFunction = currentFunction;
		currentFunction = type;

		beginScope();
		for (Token param : function.params) {
			declare(param);
			define(param);
		}
		resolve(function.body);
		endScope();
		/*
		We could use an explicit stack of FunctionType values for that, but instead we'll piggyback
		on the JVM. We store the previous value in a local on the Java stack. When we're done resolving the
		function body, we restore the field to that value.
		 */
		currentFunction = enclosingFunction;
	}

	private void beginScope() {
		scopes.push(new HashMap<String, Boolean>());
	}

	private void endScope() {
		scopes.pop();
	}
	/*
	Now we can push and pop a stack of empty scopes.
	We still need to push something in them.
	 */

	/*
	In order to do that, as we visit expressions, we need to know if we're
	inside the initializer for some variable. We do that by splitting binding
	into two steps. First, declare
	 */
	private void declare(Token name) {
		if (scopes.isEmpty()) return;

		Map<String, Boolean> scope = scopes.peek();
		if (scope.containsKey(name.lexeme)) {
			Lox.error(name,
					"Already a variable with this name in this scope.");
		}

		scope.put(name.lexeme, false);
	}

	/*
	After declaring the variable, we resolve its initializer expression in that
	same scope where the new variable now exists but is unavailable. Once the
	initializer expression is done, the variable is ready for prime time. We do
	that by defining it.
	 */
	private void define(Token name) {
		if (scopes.isEmpty()) return;
		scopes.peek().put(name.lexeme, true);
	}

	private void resolveLocal(Expr expr, Token name) {
		for (int i = scopes.size() - 1; i >= 0; i--) {
			if (scopes.get(i).containsKey(name.lexeme)) {
				interpreter.resolve(expr, scopes.size() - 1 - i);
				return;
			}
		}
	}


}
