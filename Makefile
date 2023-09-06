.PHONY: all clean
run:
	javac Lox.java
	java Lox

clean:
	rm -f *.class