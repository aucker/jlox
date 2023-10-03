//> Appendix II stmt
package lox.lox;

import java.util.List;

abstract class Stmt {
  interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);
    R visitPrintStmt(Print stmt);
  }

  // Nested Stmt classes here...
//> stmt-expression
  static class Expression extends Stmt {
    Expression(Expr expression) {
    this.expression = expression;
  }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
  }
//< stmt-expression
//> stmt-print
  static class Print extends Stmt {
    Print(Expr expression) {
    this.expression = expression;
  }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitPrintStmt(this);
    }

    final Expr expression;
  }
//< stmt-print

  abstract <R> R accept(Visitor<R> visitor);
}
//< Appendix II stmt
