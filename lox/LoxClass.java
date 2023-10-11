package lox.lox;

import java.util.List;
import java.util.Map;

//class LoxClass {
class LoxClass implements LoxCallable {
	final String name;
	final LoxClass superclass;
	private final Map<String, LoxFunction> methods;

	//LoxClass(String name) {
	//	this.name = name;
	//}
	//LoxClass(String name, Map<String, LoxFunction> methods) {
	LoxClass(String name, LoxClass superclass,
	         Map<String, LoxFunction> methods) {
		this.superclass = superclass;
		this.name = name;
		this.methods = methods;
	}

	LoxFunction findMethod(String name) {
		if (methods.containsKey(name)) {
			return methods.get(name);
		}

		if (superclass != null) {
			return superclass.findMethod(name);
		}

		// the name is not in HashMap
		return null;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public Object call(Interpreter interpreter,
	                   List<Object> arguments) {
		LoxInstance instance = new LoxInstance(this);
		/*
		When a class is called, after the LoxInstance is created, we look for an "init" method.
		If we find one, we immediately bind and invoke it just like a normal method call.
		 */
		LoxFunction initializer = findMethod("init");
		if (initializer != null) {
			initializer.bind(instance).call(interpreter, arguments);
		}

		return instance;
	}

	@Override
	public int arity() {
		//return 0;
		LoxFunction initializer = findMethod("init");
		if (initializer == null) return 0;
		return initializer.arity();
	}
}
