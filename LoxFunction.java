package com.craftinginterpreters.lox;
import java.util.List;
class LoxFunction implements LoxCallable {
  private final String name;
  private final Expr.Function declaration;
  private final Environment closure;
  LoxFunction(String name, Expr.Function declaration, Environment closure) {
    this.name = name; this.declaration = declaration; this.closure = closure;
  }
  @Override public int arity() { return declaration.parameters.size(); }
  @Override public Object call(Interpreter interpreter, List<Object> arguments) {
    Environment local = new Environment(closure);
    for (int i = 0; i < declaration.parameters.size(); i++)
      local.define(declaration.parameters.get(i).lexeme, arguments.get(i));
    try { interpreter.executeBlock(declaration.body, local); }
    catch (Return result) { return result.value; }
    return null;
  }
  @Override public String toString() { return name == null ? "<fn>" : "<fn " + name + ">"; }
}
