# Define the Java compiler and compiler flags
JAVAC = javac
JAVAC_FLAGS = -cp .

# Define the source files and the main class
SRC_FILES = Lox.java \
			Scanner.java \
			Token.java \
			TokenType.java
MAIN_CLASS = Lox

# Define the target (executable) name
TARGET = Lox

# Default target to build and run the Java program
all: $(TARGET)

$(TARGET): $(SRC_FILES)
	$(JAVAC) $(JAVAC_FLAGS) $(SRC_FILES)

run: $(TARGET)
	java -cp . $(MAIN_CLASS)

# Clean up complied files
clean:
	rm -f *.class

.PHONY: all run clean