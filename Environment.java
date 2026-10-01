package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

class Environment {
  final Environment enclosing;
  private final Map<String, Object> values = new HashMap<>();

  Environment() {
    enclosing = null;
  }

  Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme);
    }

    if (enclosing != null) return enclosing.get(name);

    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }

  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      return;
    }

    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }

    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }

  void define(String name, Object value) {
    values.put(name, value);
  }
  Environment ancestor(int distance) {
    Environment current = this;
    for (int i = 0; i < distance; i++) current = current.enclosing;
    return current;
  }

  Object getAt(int distance, Token name) {
    Environment target = ancestor(distance);
    if (!target.values.containsKey(name.lexeme))
      throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    return target.values.get(name.lexeme);
  }

  void assignAt(int distance, Token name, Object value) {
    Environment target = ancestor(distance);
    if (!target.values.containsKey(name.lexeme))
      throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    target.values.put(name.lexeme, value);
  }
}
