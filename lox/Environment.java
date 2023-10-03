package lox.lox;

import java.util.HashMap;
import java.util.Map;

class Environment {
	/*
	The final is used to declare a variable, method, or class as final.
	A final variable cannot be changed after init
	A final method cannot be overridden
	A final class cannot be extended
	 */
	final Environment enclosing;
	private final Map<String, Object> values = new HashMap<>();

	// Init the Environment enclosing
	Environment() {
		enclosing = null;
	}

	Environment(Environment enclosing) {
		this.enclosing = enclosing;
	}

	Object get(Token name) {
		if (values.containsKey(name.lexeme)) {
			return values.get(name.lexeme);
		}

		/*
		If the variable isn't found in this environment, we simply try the enclosing one.
		That in turn does the same thing recursively, so this will ultimately walk the entire chain.
		If we reach an environment with no enclosing one and still don't find the variable,
		then we give up and report an error as before.
		 */
		if (enclosing != null) return enclosing.get(name);

		throw new RuntimeError(name,
				"Undefined variable '" + name.lexeme + "'.");
		/*
		This is a little more semantically interesting. If the variable is found, it simply returns
		the value bound to it. But what if it's not? Again, we have a choice:
		* Make it a syntax error
		* Make it a runtime error
		* Allow it and return some default value like `nil`
		 */
	}

	void assign(Token name, Object value) {
		if (values.containsKey(name.lexeme)) {
			values.put(name.lexeme, value);
			return;
		}

		if (enclosing != null) {
			enclosing.assign(name, value);
			return;
		}

		throw new RuntimeError(name,
				"Undefined variable '" + name.lexeme + ";.");
	}

	void define(String name, Object value) {
		values.put(name, value);
	}


}
