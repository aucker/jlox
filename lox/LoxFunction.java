package lox.lox;

import java.util.List;

class LoxFunction implements LoxCallable {
	private final Stmt.Function declaration;
	private final Environment closure;

	//LoxFunction(Stmt.Function declaration) {
	LoxFunction(Stmt.Function declaration, Environment closure) {
		this.closure = closure;
		this.declaration = declaration;
	}

	LoxFunction bind(LoxInstance instance) {
		Environment environment = new Environment(closure);
		environment.define("this", instance);
		return new LoxFunction(declaration, environment);
	}

	@Override
	public String toString() {
		return "<fn " + declaration.name.lexeme + ">";
		/*
		This will give nicer output if a user decides to print a function value.
		fun add(a, b) {
		  print a + b;
		}
		print add;  // "<fn add>"
		 */
	}

	@Override
	public int arity() {
		//return 0;
		return declaration.params.size();
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		//Environment environment = new Environment(interpreter.globals);
		/*
		This creates an environment chain that goes from the function's body out
		through the environments where the function is declared, all the way out
		to the global scope. The runtime environment chain matches the textual
		nesting of the source code like we want.
		 */
		Environment environment = new Environment(closure);
		for (int i = 0; i < declaration.params.size(); i++) {
			environment.define(declaration.params.get(i).lexeme,
					arguments.get(i));
		}

		//interpreter.executeBlock(declaration.body, environment);
		try {
			interpreter.executeBlock(declaration.body, environment);
		} catch (Return returnValue) {
			return returnValue.value;
		}
		return null;
	}
}
