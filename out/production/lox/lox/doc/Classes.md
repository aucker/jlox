# Classes

## OOP and Classes

There are three broad paths to object-oriented programming: classes, prototypes, and multimethods. Classes came first
and are the most popular style. With the rise of JavaScript (and to a lesser extent Lua), prototypes are more widely 
known than they used to be.

The main goal of object orientation is to bundle data with the code that acts on it. Users do that by declaring a 
`class` that:
1. Exposes a *constructor* to create and initialize new *instance* of the class
2. Provides a way to store and access *fields* on instances
3. Define a set of *methods* shared by all instances of the class that operate on each instance's state.

![oop-circle](../pic/oop-circle.png)

## Class Declarations

Like we do, we're gonna start with syntax. A `class` statement introduces a new name, so it lives in the `declaration` 
grammar rule.
```shell
declaration     -> classDecl
                 | funDecl
                 | varDecl
                 | statement ;
                 
classDecl       -> "class" IDENTIFIER "{" function "}" ;                
```
The new `classDecl` rule relies on the `function` rule we defined earlier.
```shell
function        -> IDENTIFIER "(" parameters? ")" block ;
parameters      -> IDENTIFIER ( "," IDENTIFIER )* ;
```

In plain English, a class definition is the `class` keyword, followed by the class's name, then a curly-braced body. 
Inside that body is a list of method declarations. Unlike function declarations, methods don't have a leading `fun` 
keyword. Each method is a name, parameter list, and body. E.g.:
```shell
class Breakfast {
  cook() {
    print "Egg a-fryin'!";
  }
  
  serve(who) {
    print "Enjoy your breakfast, " + who + ".";
  }
}
```
Like most dynamic typed languages, fields are not explicitly listed in the class declaration. Instances are loose bags
of data, and you can freely add fields to them as you see fit using normal imperative code.

## Creating Instances

We have classes, but they don't do anything yet. Lox doesn't have "static" methods that you can call right on the class
itself, so w/o actual instances, classes are useless. Thus, instances are the next step.

While some syntax and semantics are fairly standard across OOP languages, the way you create new instances isn't. Ruby,
following Smalltalk, creates instances by calling a method on the class object itself, a recursively graceful approach.
Some, like C++ and Java, have a `new` keyword dedicated to birthing a new object. Python has you "call" the class itself
like a function. (JavaScript, ever weird, sort of does both.)

I took a minimal approach with Lox. We already have class objects, and we already have function calls, so we'll use call
expressions on class objects to create new instances. It's as if a class is a factory function that generates instances
of itself. This feels elegant to me, and also spares us the need to introduce syntax like `new`. Therefore, we can skip
past the front end straight into the runtime.

## Properties on Instances

After creating the instances, we should make them useful. We're at a fork in the road. We could add behavior first -
mothods - or we could start with state - properties. We're going to take the latter because, as we can see, the two get
entangled in an interesting way and it will be easier to make sense of them if we get properties working first.

Lox follows JavaScript and Python in how it handles state. Every instance is an open collection of named values. Methods
on the instance's class can access and modify properties, but so can outside code. Properties are accessed using a `.` 
syntax.
```shell
someObject.someProperty
```
An expression followed by `.` and an identifier reads the property with that name from the object the expression 
evaluates to. That dot has the same precedence as the parentheses in a function call expression, so we slot it into the
grammar by replacing the existing `call` rule with:
```shell
call        -> primary ( "(" arguments? ")" | "." IDENTIFIER )* ;
```
After a primary expression, we allow a series of any mixture of parenthesized called and dotted property accesses. 
"Property access" is a mouthful, so from here on out, we'll call these "get expressions".

### *Get expressions*

The outer `while` loop there corresponds to the `*` in the grammar rule. We zip along the tokens building up a chain of
calls and gets as we find parentheses and dots, like so:
![while-loop](../pic/while-loop.png)

### *Set expressions*

Setters use the same syntax as getters, except they appear on the left side of an assignment.
```shell
someObject.someProperty = value;
```
In grammar land, we extend the rule for assignment to allow dotted identifiers on the left-hand side.
```shell
assignment      -> ( call ".")? IDENTIFIER "=" assignment
                 | logic_or ;
```
Unlike getters, setters don't chain. However, the reference to `call` allows any high-precedence expression before the
last dot, including any number of *getters*, as in:
![getter-chain](../pic/getter-chain.png)
Note here that only the *last* part, the `.meat` is the *setter*. The `.omelette` and `.filling` parts are both *get*
expressions.