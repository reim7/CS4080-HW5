# CS4080-HW5
CS 4080 - Homework 5

Chapter 10 - Functions

Anonymous Functions

I modified the interpreter to support anonymous functions, allowing functions to be created without names, passed as arguments, and executed while still supporting regular named functions.

Chapter 11 - Resolving and Binding

Unused Local Variables

I updated the resolver to detect local variables that are declared but never used. The program now reports an error when a local variable is never read.

Indexed Local Variables

I modified the resolver and interpreter to store and access local variables using indexes instead of looking them up by name. This makes accessing local variables more efficient while maintaining support for functions and closures.

Testing

I included test files to demonstrate the changes made to the interpreter, including anonymous functions, unused-variable detection, and indexed local variables.

Written Responses

The written responses for the Chapter 10 and Chapter 11 challenges are included in my submitted Homework 5 PDF.
