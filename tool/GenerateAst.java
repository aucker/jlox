package lox.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
//        if (args.length != 1) {
//            System.err.println("Usage: generate_ast <output directory>");
//            System.exit(64);
//        }
//        String outputDir = args[0];
        String outputDir = "lox";
//>     call-define-ast
        defineAst(outputDir, "Expr", Arrays.asList(
                "Assign     : Token name, Expr value",
                "Binary     : Expr left, Token operator, Expr right",
                "Call       : Expr callee, Token paren, List<Expr> arguments",
                "Get        : Expr object, Token name",
                "Grouping   : Expr expression",
                "Literal    : Object value",
                "Logical    : Expr left, Token operator, Expr right",
                "Set        : Expr object, Token name, Expr value",
                "Super      : Token keyword, Token method",
                "This       : Token keyword",
//>     Statement and State var-expr
                "Unary      : Token operator, Expr right",
                "Variable   : Token name"
//<     Statement and State var-expr
        ));

//>     Statements and State stmt-ast
        defineAst(outputDir, "Stmt", Arrays.asList(
                "Block      : List<Stmt> statements",
                //"Class      : Token name, List<Stmt.Function> methods",
                "Class      : Token name, Expr.Variable superclass," +
                            " List<Stmt.Function> methods",
                "Expression : Expr expression",
                "Function   : Token name, List<Token> params," +
                            " List<Stmt> body",
                "If         : Expr condition, Stmt thenBranch," +
                            " Stmt elseBranch",
//>     var-stmt-ast
                "Print      : Expr expression",
                "Return     : Token keyword, Expr value",
                "Var        : Token name, Expr initializer",
                "While      : Expr condition, Stmt body"
//<     var-stmt-ast
        ));
//<     Statements and State stmt-ast
//<     call-define-ast
    }

    // defineAst func()
    private static void defineAst(
            String outputDir, String baseName, List<String> types)
            throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

//        writer.println("package com.craftinginterpreters.lox");
//>     omit
        writer.println("//> Appendix II " + baseName.toLowerCase());
//<     omit
        writer.println("package lox.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

//>     call-define-visitor
        defineVisitor(writer, baseName, types);
//<     call-define-visitor

//>     omit
        writer.println();
        writer.println("  // Nested " + baseName + " classes here...");
//<     omit
//>     nested-classes
        // The AST classes
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim(); // [robust]
            defineType(writer, baseName, className, fields);
        }
//<     nested-classes
//> base-accept-method

        // The base accept() method.
        writer.println();
        writer.println("  abstract <R> R accept(Visitor<R> visitor);");

//<     base-accept-method
        writer.println("}");
//>     omit
        writer.println("//< Appendix II " + baseName.toLowerCase());
//<     omit
        writer.close();
    }
//<     define-ast
//>     define-visitor
    private static void defineVisitor(
            PrintWriter writer, String baseName, List<String> types) {
        writer.println("  interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("  }");
    }
//<     define-visitor
//>     define-type
    private static void defineType(
            PrintWriter writer, String baseName,
            String className, String fieldList) {
//>     omit
        writer.println("//> " +
                baseName.toLowerCase() + "-" + className.toLowerCase());
//<     omit
        writer.println("  static class " + className + " extends " + baseName + " {");

//>     omit
        // Hack. Stmt.Class has such a long constructor that it overflows
        // the line length on the Appendix II page. Wrap it.
        if (fieldList.length() > 64) {
            fieldList = fieldList.replace(", ", ",\n         ");
        }

//<     omit
        // constructor
        writer.println("    " + className + "(" + fieldList + ") {");

//>     omit
        fieldList = fieldList.replace(",\n         ", ",");
//<     omit

        // Store parameters in fields
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("    this." + name + " = " + name + ";");
        }

        writer.println("  }");

//>     accept-method

        // Visitor pattern
        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("        return visitor.visit" +
                className + baseName + "(this);");
        writer.println("    }");
//<     accept-method

        // Fields
        writer.println();
        for (String field : fields) {
            writer.println("    final " + field + ";");
        }


        writer.println("  }");
//>     omit
        writer.println("//< " +
                baseName.toLowerCase() + "-" + className.toLowerCase());
//<     omit
    }
//<     define-type
}
