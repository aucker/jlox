package lox.lox;

class Return extends RuntimeException {
	final Object value;

	Return(Object value) {
		/*
		The weird super constructor call with those `null` and `false` arguments disables
		some JVM machinery that we don't need. Since we're using our exception class for
		control flow and not actual error handling, we don't need overhead like stack trace.
		 */
		super(null, null, false, false);
		this.value = value;
	}
}
