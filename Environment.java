package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Environment {
  final Environment enclosing;
  private final Map<String, Object> globals = new HashMap<>();
  private final ArrayList<Object> slots = new ArrayList<>();

  Environment() { enclosing = null; }
  Environment(Environment enclosing) { this.enclosing = enclosing; }

  // Globals retain name-based access. Resolved locals use array indexes.
  void define(String name, Object value) { globals.put(name, value); }
  Object get(Token name) {
    if (globals.containsKey(name.lexeme)) return globals.get(name.lexeme);
    if (enclosing != null) return enclosing.get(name);
    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }
  void assign(Token name, Object value) {
    if (globals.containsKey(name.lexeme)) { globals.put(name.lexeme, value); return; }
    if (enclosing != null) { enclosing.assign(name, value); return; }
    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }
  void setSlot(int index, Object value) {
    while (slots.size() <= index) slots.add(null);
    slots.set(index, value);
  }
  Environment ancestor(int distance) {
    Environment env = this;
    for (int i = 0; i < distance; i++) env = env.enclosing;
    return env;
  }
  Object getAt(int distance, int index) { return ancestor(distance).slots.get(index); }
  void assignAt(int distance, int index, Object value) {
    ancestor(distance).slots.set(index, value);
  }
}
