package lox.lox;

import java.util.List;

class LoxFunction implements LoxCallable {
	private final Stmt.Function declaration;
	LoxFunction(Stmt.Function declaration) {
		this.declaration = declaration;
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
		Environment environment = new Environment(interpreter.globals);
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
