package lox.lox;

import java.util.HashMap;
import java.util.Map;

class Environment {
	private final Map<String, Object> values = new HashMap<>();

	Object get(Token name) {
		if (values.containsKey(name.lexeme)) {
			return values.get(name.lexeme);
		}

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

		throw new RuntimeError(name,
				"Undefined variable '" + name.lexeme + ";.");
	}

	void define(String name, Object value) {
		values.put(name, value);
	}


}
