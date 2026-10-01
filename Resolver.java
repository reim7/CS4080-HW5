package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

// Chapter 11, Challenge 3: report locals that are never read.
class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
  private enum State { DECLARED, DEFINED, READ }
  private enum FunctionType { NONE, FUNCTION }

  private static class Local {
    final Token name;
    State state;
    final boolean checkUnused;
    final int slot;
    Local(Token name, State state, boolean checkUnused, int slot) {
      this.name = name;
      this.state = state;
      this.checkUnused = checkUnused;
      this.slot = slot;
    }
  }

  private final Interpreter interpreter;
  private final Stack<Map<String, Local>> scopes = new Stack<>();
  private final Stack<Integer> nextSlots = new Stack<>();
  private FunctionType currentFunction = FunctionType.NONE;
  private int loopDepth = 0;

  Resolver(Interpreter interpreter) { this.interpreter = interpreter; }
  void resolve(List<Stmt> statements) {
    for (Stmt stmt : statements) resolve(stmt);
  }
  void resolve(Stmt stmt) { stmt.accept(this); }
  void resolve(Expr expr) { expr.accept(this); }

  private void beginScope() { scopes.push(new HashMap<>()); nextSlots.push(0); }
  private void endScope() {
    nextSlots.pop();
    for (Local local : scopes.pop().values()) {
      if (local.checkUnused && local.state != State.READ) {
        Lox.error(local.name, "Local variable '" + local.name.lexeme + "' is never used.");
      }
    }
  }
  private void declare(Token name, boolean checkUnused) {
    if (scopes.isEmpty()) return;
    Map<String, Local> scope = scopes.peek();
    if (scope.containsKey(name.lexeme))
      Lox.error(name, "Already a variable with this name in this scope.");
    int slot = nextSlots.pop();
    nextSlots.push(slot + 1);
    scope.put(name.lexeme, new Local(name, State.DECLARED, checkUnused, slot));
  }
  private void define(Token name) {
    if (scopes.isEmpty()) return;
    Local local = scopes.peek().get(name.lexeme);
    if (local != null) local.state = State.DEFINED;
  }
  private void resolveLocal(Expr expr, Token name, boolean isRead) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      Local local = scopes.get(i).get(name.lexeme);
      if (local != null) {
        interpreter.resolve(expr, scopes.size() - 1 - i, local.slot);
        if (isRead) local.state = State.READ;
        return;
      }
    }
  }
  private void resolveFunction(Expr.Function function) {
    FunctionType previous = currentFunction;
    int previousLoopDepth = loopDepth;
    currentFunction = FunctionType.FUNCTION;
    loopDepth = 0;
    beginScope();
    // Parameters are inputs rather than unused local declarations.
    for (Token param : function.parameters) {
      declare(param, false);
      define(param);
    }
    resolve(function.body);
    endScope();
    currentFunction = previous;
    loopDepth = previousLoopDepth;
  }

  @Override public Void visitBlockStmt(Stmt.Block stmt) {
    beginScope(); resolve(stmt.statements);
    endScope(); return null;
  }
  @Override public Void visitBreakStmt(Stmt.Break stmt) { return null; }
  @Override public Void visitExpressionStmt(Stmt.Expression stmt) {
    resolve(stmt.expression); return null;
  }
  @Override public Void visitFunctionStmt(Stmt.Function stmt) {
    declare(stmt.name, false);
    define(stmt.name);
    if (!scopes.isEmpty()) interpreter.resolveDeclaration(stmt, scopes.peek().get(stmt.name.lexeme).slot);
    resolveFunction(stmt.function);
    return null;
  }
  @Override public Void visitIfStmt(Stmt.If stmt) {
    resolve(stmt.condition);
    resolve(stmt.thenBranch);
    if (stmt.elseBranch != null) resolve(stmt.elseBranch);
    return null;
  }
  @Override public Void visitPrintStmt(Stmt.Print stmt) {
    resolve(stmt.expression); return null;
  }
  @Override public Void visitReturnStmt(Stmt.Return stmt) {
    if (currentFunction == FunctionType.NONE)
      Lox.error(stmt.keyword, "Can't return from top-level code.");
    if (stmt.value != null) resolve(stmt.value);
    return null;
  }
  @Override public Void visitVarStmt(Stmt.Var stmt) {
    declare(stmt.name, true);
    if (!scopes.isEmpty()) interpreter.resolveDeclaration(stmt, scopes.peek().get(stmt.name.lexeme).slot);
    if (stmt.initializer != null) resolve(stmt.initializer);
    define(stmt.name);
    return null;
  }
  @Override public Void visitWhileStmt(Stmt.While stmt) {
    resolve(stmt.condition);
    loopDepth++;
    resolve(stmt.body);
    loopDepth--;
    return null;
  }
  @Override public Void visitAssignExpr(Expr.Assign expr) {
    resolve(expr.value);
    resolveLocal(expr, expr.name, false);
    return null;
  }
  @Override public Void visitBinaryExpr(Expr.Binary expr) {
    resolve(expr.left); resolve(expr.right); return null;
  }
  @Override public Void visitCallExpr(Expr.Call expr) {
    resolve(expr.callee);
    for (Expr arg : expr.arguments) resolve(arg);
    return null;
  }
  @Override public Void visitFunctionExpr(Expr.Function expr) {
    resolveFunction(expr); return null;
  }
  @Override public Void visitGroupingExpr(Expr.Grouping expr) {
    resolve(expr.expression); return null;
  }
  @Override public Void visitLiteralExpr(Expr.Literal expr) { return null; }
  @Override public Void visitLogicalExpr(Expr.Logical expr) {
    resolve(expr.left); resolve(expr.right); return null;
  }
  @Override public Void visitUnaryExpr(Expr.Unary expr) {
    resolve(expr.right); return null;
  }
  @Override public Void visitVariableExpr(Expr.Variable expr) {
    if (!scopes.isEmpty()) {
      Local local = scopes.peek().get(expr.name.lexeme);
      if (local != null && local.state == State.DECLARED)
        Lox.error(expr.name, "Can't read local variable in its own initializer.");
    }
    resolveLocal(expr, expr.name, true);
    return null;
  }
}
