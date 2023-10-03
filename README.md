# jlox

Learning how to implement an interpreter🔨 from scratch.

There are several parts in Jlox, I don't know how to write the makefile to run Java project right
now, so I use *Android Studio*.

When running the scan section code, the interpreter can recognize certain characters. Like `!`, `(`
etc.

Just like the following: 
```shell
> 
+ - * !
PLUS + null
MINUS - null
STAR * null
BANG ! null
EOF  null
> 
can you do this () ? int main() { println() };
IDENTIFIER can null
IDENTIFIER you null
IDENTIFIER do null
THIS this null
LEFT_PAREN ( null
RIGHT_PAREN ) null
IDENTIFIER int null
IDENTIFIER main null
LEFT_PAREN ( null
RIGHT_PAREN ) null
LEFT_BRACE { null
IDENTIFIER println null
LEFT_PAREN ( null
RIGHT_PAREN ) null
RIGHT_BRACE } null
SEMICOLON ; null
EOF  null
```

## Representing Code

Before we do all that, let's focus on the main goal - a representation for code. It should be simple
for the parser to produce and easy for the interpreter to consume.
`1 + 2 * 3 - 4`, you know the multiplication is evaluated before the addition or subtraction. One 
way to visualize that precedence is using a tree. Leaf nodes are numbers, and interior nodes are 
operators with the branches for each of their operands.

In order to evaluate an arithmetic node, you need to know the numeric values of its subtrees, so you
have to evaluate those first. That means working your way from the leaves up to the root - *post-order*
traversal:
![post-order traversal](pic/post-order.png)
1. Starting with the full tree, evaluate the bottom-most operation, 2 * 3
2. Now we can evaluate the +
3. Next, the -
4. The final answer.

We need to get more precise about what that grammar is then. Like lexical grammars in the last chap.
, there is a long ton of theory around syntactic grammars. We start by moving one level up the 
[Chomsky hierarchy](https://en.wikipedia.org/wiki/Chomsky_hierarchy)...

### Context-Free Grammars

In the last chapter, the formalism we used for defining the lexical grammar - the rules for how 
characters get grouped into tokens - was called a *regular language*. Which is fine with our scanner
, but not powerful enough to handle expressions which can nest arbitrarily deeply.

We need a bigger hammer - **context-free grammar (CFG)**. It's the next heaviest tool in the toolbox
of [formal grammars](https://en.wikipedia.org/wiki/Formal_grammar).

![Lexical-Syntactic](pic/lexical-syntactic.png)

### *Rules for grammars*

If you start with the rules, you can use them to *generate* strings that are in the grammar. Strings
created this way are called **derivations** because each is *derived* from the rules of the grammar.
In each step of the game, you pick a rule and follow what it tells you to do. Most of the lingo 
around formal grammars comes from playing them in this direction. Rules are called **productions**
because they *produce* strings in the grammar.

Each production in a context-free grammar has a **head** - its name -- and a **body**, which describes
what it generates. In its pure form, the body is simply a list of symbols. Symbols come in two 
delectable flavors:

* A **terminal** is a letter from the grammar's alphabet. You can think of it like a literal value.
In the syntactic grammar we're defining, the terminals are individual lexemes - tokens coming from
scanner like *if* or *1234*.
* A **nonterminal** is a named reference to another rule in the grammar. It means "play that rule 
and insert whatever it produces here". In this way, grammar composes.

To make this concrete, we need a way to write down these production rules. People have been trying 
to crystallize grammar all the way back to Panini's *Ashtadhyayi*, which codified Sanskrit grammar a
mere couple thousand years ago. Not much progress happened until John Backus and company needed a 
notation for specifying ALGOL 58 and came up with [Backus-Naur form](https://en.wikipedia.org/wiki/Backus–Naur_form)
(**BNF**). Since then, nearly everyone uses some flavor of BNF, tweaked to their own tastes.

**Something clean**, each rule is a name, followed by an arrow (->), followed by a sequence of 
symbols, and finally ending with a semicolon(;). Terminals are quoted strings, and nonterminals are 
lowercase words.

Here is a grammar for breakfast menus:
```shell
breakfast  -> protein "with" breakfast "on the side" ;
breakfast  -> protein ;
breakfast  -> bread ;

protein    -> crispiness "crispy" "bacon" ;
protein    -> "sausage" ;
protein    -> cooked "eggs" ;

crispiness -> "really" ;
crispiness -> "really" crispiness ;

cooked     -> "scrambled" ;
cooked     -> "poached" ;
cooked     -> "fried" ;

bread      -> "toast" ;
bread      -> "biscuits" ;
bread      -> "English muffin" ;
```

We can use this grammar to generate random breakfasts. Let's play a round and see how it works.

With that, every nonterminal in the string has been expanded until it finally contains only terminals
and we're left with:
![breakfast expand](pic/breakfast.png)

### *Enhancing our notation*

## Parsing Expressions

The cover of _Compilers: Principles, Techniques, and Tools_ literally has a dragon labeled "complexity
of compiler design" being slain by a knight bearing a sword and shield branched "LAIR parser generator"
and "syntax directed translation". They laid it on thick.

A little self-congratulation is well-deserved, but the truth is you don't need to know most of that
stuff to bang out a high quality parser for a modern machine. As always, I encourage you to broaden your
education and take it in later, but this book omits the trophy case.

## Evaluating Expressions

Lox doesn't do implicit conversions in equality and Java does not either. We do have to handle `nil/null`
specially so that we don't throw a `NullPointerException` if we try to call `equals()` on `null`. Otherwise,
we're fine. Java's `equals()` method on Boolean, Double, and String have the behavior we want for Lox.

What do you expect this to evaluate to:
```
(0 / 0) == (0 / 0)
```
According to [IEEE 754](https://en.wikipedia.org/wiki/IEEE_754), which specifies the behaviors of
double-precision numbers, dividing a zero by zero gives you the special **NaN** ("not a number") value.
Strangely enough, NaN is *not* equal to itself.

In Java, the `==` operator on primitive doubles preserves that behavior, but the `equals()` method on the
Double class does not. Lox uses the latter, so doesn't follow IEEE. These kinds of subtle incompatibilities
occupy a dismaying fraction of language implementers' lives.

We could simply not detect or report a type error at all. This is what C does if you cast a pointer to some
type that doesn't match the data that is actually being pointed to. C gains flexibility and speed by
allowing that, but is also famously dangerous. Once you misinterpret bits in memory, all bets are off.

Few modern languages accept unsafe operations like that. Instead, most are **memory safe** and ensure - through
a combination of static and runtime checks - that a program can never incorrectly interpret the value stored
in a piece of memory.

## **Statements and State**

### *Executing statements*

We're running through the previous couple of chapter in microcosm, working our way through the front end. Our
parser can now produce statement syntax trees, so the next and final step is to interpret them. As in
expressions, we use the **Visitor** pattern, but we have a new visitor interface, `Stmt.Visitor`, to implement
since statements have their own base class.

By the way, from the `Interpreter.java` file.

The class is default `PROTECTED` in java.

### Global Variables

Now that we have statements, we can start working on state. Before we get into all the complexity of lexical
scoping, we'll start off with the easiest kind of variables - globals. We need two new constructs.

1. A **variable declaration** statement brings a new variable into the world.
    ```ts
    var beverage = "espresso";
   ```
   This creates a new binding that associates a name (here "beverage") with a value.
2. Once that's done, a **variable expression** accesses that binding. When the identifier "beverage" is used
    as an expression, it looks up the value bound to that name and returns it.
    ```
    print beverage;  // "espresso"
    ```

#### *Variable syntax*

The clauses in control flow statements - think the then and else branches of an `if` statement or the body of
a `while` - are each a single statement. But that statement is not allowed to be one that declares a name.
This is OK:
```js
if (monday) print "Ugh, already?";
```
But this is not:
```js
if (monday) var beverage = "espresso";
```

We *could* allow the latter, but it's confusing. What is the scope of that `beverage` variable? Does it persist
after the `if` statement? If so, what is its value on days other than Monday? Does the variable exist at all on
those days?

Code like this is weird, so C, Java, and friends all disallow it. It's as if there are two levels of "precedence"
for statements. Some places where a statement is allowed - like inside a block or at the top level - allow any
kind of statement, including declarations. Other allow only the "higher" precedence statements that don't declare
names.

To accommodate the distinction, we add another rule for kinds of statements that declare names.
```shell
program       -> declaration* EOF ;

declaration   -> varDecl
               | statement ;
               
statement     -> exprStmt
               | printStmt ;
```

Declaration statements go under the new `declaration` rule. Right now, it's only variables, but later it will 
include functions and classes. Any place where a declaration is allowed also allows non-declaring statements, 
so the `declaration` rule falls through to `statement`. Obviously, you can declare stuff at the top level of 
a script, so `program` routes to the new rule.

The rule for declaring a variable looks like:
```shell
varDecl         ->  "var" IDENTIFIER ( "=" expression  )? ";" ;
```

Like most statements, it starts with a leading keyword. In this case, `var`. Then an identifier token for the 
name of the variable being declared, followed by an optional initializer expression. Finally, we put a bow on 
it with the semicolon.

To access a variable, we define a new kind of primary expression.
```shell
primary          -> "true" | "false" | "nil"
                  | NUMBER | STRING
                  | "(" expression ")"
                  | IDENTIFIER ;
```

That `IDENTIFIER` clause matches a single identifier token, which is understood to be the name of the 
variable being accessed.

These new grammar rules get their corresponding syntax trees. Over in the AST generator, we add a new 
statement node for a variable declaration.

### **Environments**

The bindings that associate variables to values need to be stored somewhere. Ever since the Lisp folks
invented parentheses, this data structure has been called an **environment**.
![environment](pic/environment.png)

You can think of it like a map where the keys are variable names and the values are the variable's, uh, 
values. In fact, that's how we'll implement it in Java. We could stuff that map and the code to manage 
it right into Interpreter, but since it forms a nicely delineated concept, we'll pull it out into its 
own class.

>Rule about variables and scoping is,
> "When in double, do what Scheme does". The Scheme folks have probably spent more time thinking about
> variable scope than we ever will - one of the main goals of Scheme was to introduce lexical scoping 
> lexical scoping to the world - so it's hard to go wrong if you follow in their footsteps.
> 
> Scheme allows redefining variables at the top level.

### **Assignment**

It's possible to create a language that has variables but does not let you reassign - or **mutate** --
them. Haskell is one example. SML supports only mutable references and arrays - variables cannot be 
reassigned. Rust steers you away from mutation by requiring a `mut` modifier to enable assignment.

Mutating a variable is a side effect and, as the name suggests, some language folks think side effects 
are dirty or inelegant. Code should be pure math that produces values - crystalline, unchanging ones - 
like an act of divine creation. Not some grubby automaton that beats blobs of data into shape, one 
imperative grunt at a time.

Lox is not so austere. Lox is an imperative language, and mutation comes with the territory. Adding 
support for assignment doesn't require much work. Global variables already support redefinition, so 
most of the machinery is there now. Mainly, we're missing an explicit assignment notation.

#### *Assignment syntax*

That little `=` syntax is more complex than it might seem. Like most C-derived languages, assignment 
is an expression and not a statement. As in C, it is the lowest precedence expression form. That means 
the rule slots between `expression` and `equality` (the next lowest precedence expression).
```shell
expression      -> assignment ;
assignment      -> IDENTIFIER "=" assignment
                 | equality ;
```
This says an `assignment` is either an identifier followed by an `=` and an expression for the value, 
or an `equality` (and thus any other) expression. Later, `assignment` will get more complex when we 
add property setters on objects, like:
```shell
instance.field = "value";
```

Consider:
```shell
var a = "before";
a = "value";
```

On the second line, we don't *evaluate* `a` (which would return the string "before").
We figure out what variable `a` refers to, so we know where to store the right-hand side expression's
value. The [classic terms](https://en.wikipedia.org/wiki/Value_(computer_science)#lrvalue) for these 
two constructs are **l-value** and **r-value**. All the expressions that we've seen so far that 
produce values are r-values. An l-values "evaluates" to a storage location that you can assign into.

We want the syntax tree to reflect that an l-value isn't evaluated like a normal expression. That's 
why the Expr.Assign node has a *Token* for the left-hand side, not an Expr. The problem is that the 
parser doesn't know it's parsing an l-value until it hits the =. In a complex l-value, that may 
occur many tokens later.
```shell
makeList().head.next = node;
```

#### *Assignment semantics*

The key difference between assignment and definition is that assignment is not allowed to create a 
*new* variable. In terms of our implementation, that means it's a runtime error if the key doesn't 
already exist in the environment's variable map.

The last thing the `visit()` method does is return the assigned value. That's because assignment is 
an expression that can be nested inside other expressions, like so:
```shell
var a = 1;
print a = 2;  // "2"
```

Our interpreter can now create, read, and modify variables. It's about as sophisticated as early 
BASICs. Global variables are simple, but writing a large program when any two chunks of code can 
accidentally step on each other's state is no fun. We want *local* variables, which means it's 
time for *scope*.


