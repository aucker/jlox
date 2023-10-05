# Functions

> And that is also the way the human mind works - by the compounding of old ideas into new structures that become new
> ideas that can themselves be used in compounds, and round and round endlessly, growing ever more remote from the basic
> earthbound imagery that is each language's soil.  
> -- Douglas R. Hofstadter, I Am a Strange Loop

## Function Calls

You're certainly familiar with C-style function call syntax, but the grammar is more subtle than you may realize. Calls
are typically to named functions like:
```shell
average(1, 2);
```
![call-syntax](../pic/call-syntax.png)
But the name of the function being called isn't actually part of the call syntax. The thing being called-the **callee**-
can be any expression that evaluates to a function. (Well, it does have to be a pretty *high precedence* expression, but
parentheses take care of that.) E.g.:
```shell
getCallback()();
```
There are two call expressions here. The first pair of parentheses has `getCallback` as its callee. But the second call
has the entire `getCallback()` expression as its callee. It is the parentheses following an expression that indicate a
function call. You can think of a call as sort of like a postfix operator that starts with `(`.

> [More **callee** and **caller** explained](https://cs61.seas.harvard.edu/site/2018/Asm2/)

This "operator" has higher precedence than any other operator, even the unary ones. So we slot it into the grammar by 
having the `unary` rule bubble up to a new `call` rule.
```shell
unary               -> ( "!" | "-" ) unary | call ;
call                -> primary ( "(" arguments? ")" )* ;
```
This rule matches a primary expression followed by zero or more function calls. If there are no parentheses, this parses
a bare primary expression. Otherwise, each call is recognized by a pair of parentheses with an optional list of arguments
inside. The argument list grammar is:
```shell
arguments            -> expression ( "," expression )* ;
```
This rule requires at least one argument expression, followed by zero or more other expressions, each preceded by a 
comma. To handle zero-argument calls, the `call` rule itself considers the entire `arguments` production to be optional.

### *Maximum argument counts*

Right now, the loop where we parse arguments has no bound. If you want to call a function and pass a million arguments 
to it, the parser would have no problem with it. Do we want to limit that?

Other languages have various approaches. The C standard says a conforming implementation has to support *at least* 127 
arguments to a function, but doesn't say there's any upper limit. The Java specification says a method can accept *no
more than* 255 arguments.

> The limit is 254 arguments if the method is an instance method. That's because `this` - the receiver of the method - 
> works like an argument that is implicitly passed to the method, so it claims one of the slots.

### *Interpreting function calls*

### *Call type errors*

Before we get to implementing LoxCallable, we need to make the visit method a little more robust. It currently ignores 
a couple of failure modes that we can't pretend won't occur. First, what happens if the callee isn't actually something
you can call? What if you try to do this:
```shell
"totally not a function"();
```
Strings aren't callable in Lox. The runtime representation of a Lox string is a Java string, so when we cast that to 
LoxCallable, the JVM will throw a ClassCastException. We don't want our interpreter to vomit out some nasty Java stack 
trace and die. Instead, we need to check the type ourselves first.

### *Checking arity*

The other problem relates to the function's **arity**. Arity is the fancy term for the number of arguments a function
or operation expects. Unary operators have arity one, binary operators two, etc. With functions, the arity is determined
by the number of parameters it declares.
```shell
fun add(a, b, c) {
  print a + b + c;
}
```
This function defines three parameters, `a`, `b`, and `c`, so its arity is three and it expects three arguments. So what
if you try to call it like this:
```shell
add(1, 2, 3, 4);  // Too many
add(1, 2);  // Too few.
```
Different languages take different approaches to this problem. Of course, most statically typed languages check this at
compile time and refuse to compile the code if the argument count doesn't match the function's arity. JavaScript 
discards any extra arguments you pass. If you don't pass enough, it fills in the missing parameters with the magic 
sort-of-like-null-but-not-really value `undefined`. Python is stricter. It raises a runtime error if the argument list
is too short or too long.

## Native Functions

We can theoretically call functions, but we have no functions to call yet. Before we get to user-defined functions, now
is a good time to introduce a vital but ofter overlooked facet of language implementations-**native functions**. These
are functions that the interpreter exposes to user code but that are implemented in the host language (in our case 
Java), not the language being implemented (Lox).

Sometimes these are called **primitives**, **external functions**, or **foreign functions**. Since these functions can
be called while the user's program is running, they form part of the implementation's runtime. A lot of programming
language books gloss over these because they aren't conceptually interesting. They're mostly grunt work.

![foreign](../pic/foreign.png)
> Curiously, two names for these functions - "native" and "foreign" - are antonyms. Maybe it depends on the perspective 
> of the person choosing the term. If you think of yourself as "living" within the runtime's implementation (in our 
> case, Java), then functions written in that are "native". But if you have the mindset of a *user* of your language,
> then the runtime is implemented in some other "foreign" language.
> 
> Or it may be that "native" refers to the machine code language of the underlying hardware. In Java, "native" methods
> are ones implemented in C or C++ and compiled to native machine code.

But when it comes to making your language actually good at doing useful stuff, the native functions your implementation
provides are key. They provide access to the fundamental services that all programs are defined in terms of. If you 
don't provide native functions to access the file system, a user's going to have hell of time writing a program that
reads and displays a file.

Many languages also allow users to provide their own native functions. The mechanism for doing so is call a **foreign
function interface (FFI), native extension, native interface**, or something along those lines. These are nice because
they free the language implementer from providing access to every single capability the underlying platform supports.
We won't define an FFI for jlox, but we will add one native function to give you an idea of what it looks like.

> A classic native function almost every language provides is one to print text to stdout. In Lox, I made `print` a 
> built-in statement so that we could get stuff on screen in the chapters before this one.
> 
> Once we have functions, we could simplify the language by tearing out the old print syntax and replacing it with a 
> native function. But that would mean that examples early in the book wouldn't run on the interpreter from later 
> chapters and vice versa.

### *Telling time*

When we get to Part III and starting working on a much more efficient implementation of Lox, we're going to care deeply 
about performance. Performance work requires measurement, and that in turn means **benchmarks**. These are programs 
that measure the time it takes to exercise some corner of the interpreter.

We could measure the time it takes to start up the interpreter, run the benchmark, and exit, but that adds a lot of 
overhead - JVM startup time, OS shenanigans, etc. That stuff does matter, of course, but if you're just trying to 
validate an optimization to some piece of the interpreter, you don't want that overhead obscuring your results.

A nice solution is to have the benchmark script itself measure the time elapsed between two points in the code. To do
that, a Lox program needs to be able to tell time. There's no way to do that now -- you can't implement a useful clock 
"from scratch" w/o access to the underlying clock on the computer.

So we'll add `clock()`, a native function that returns the number of seconds that have passed since some fixed point in
time. The difference between two successive invocations tells you how much time elapsed between the two calls. This 
function is defined in the global scope, so let's ensure the interpreter has access to that.

> In Lox, functions and variables occupy the same namespace. In Common Lisp, the two live in their own worlds. A 
> function and variable with the same name don't collide. If you call the name, it looks up the variable. This does 
> require jumping through some hoops when you do want to refer to a function as a first-class value.
> 
> Richard P. Gabriel and Kent Pitman coined the terms "Lisp-1" to refer to languages like Scheme that put functions and
> variables in the same namespace, and "Lisp-2" for languages like Common Lisp that partition them. Despite being 
> totally opaque, those names have since stuck. Lox is a Lisp-1.

