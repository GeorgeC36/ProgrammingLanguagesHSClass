# Welcome to the Z Programming Language!

This language was created by George Corbin as part of the Honors Programming Languages curriculum at Westminster in the
Spring of 2021

# The Basics

## Hello World!

```
print("Hello World!")
```

*Note: The semicolon, `;`, is optional on the end of lines!*

## Variables

Use `var` define a changeable variable and `const` to define a fixed variable that cannot change.

````
var x = 5
const y = 10
````

### Declaring new variables

When defining a variable, the language figures out which data type the variable should take on.

````
var x = 5
````

If the variable is to be undeclared, but the data type is known it can be defined with a colon and its data type.

```
var num: Double
```

### Assigning values to existing variables

When assigning a value to an already existing variable that was defined with `var` the value will be replaced; however,
attempting to assign to an already existing variable that was defined with `const` is an error and is not allowed.

```
var x = 5
x = 7
```

# Functions

## Defining a Function

Define a function by starting with the keyword `func` then the function name followed by its arguments with an arrow
giving the function's data type. When defining arguments for a function, in order to define the type for each argument,
put the variable name followed by a colon and then enter the type.

```
func greet(person: String) -> String {
    let greeting = "Hello, " + person + "!"
    return greeting
}
```

## Calling a Function

Take the function name and put parentheses around all the arguments that it needs to pass in and separate them with
commas if needed. When entering the arguments for the function, it is optional to enter the argument name followed by a
colon.

```
print(greetAgain(person: "George"))

Console:
Hello again, George!
```

# Control Flow

*Note: Parentheses around all Control Flow statements are optional*

## Loops

There are three types of loops: `for`, `for-in`, and `while`.

### Creating a Loop

To create a loop, start with the keyword of the type of loop.

```
for (i = 0; i < size; i++) {
    
}

for i in 1...5 {
    print(i)
}

while i < 6 {
    i++
}
```

*Note: When using a `for` loop, a semicolon must be used between statements.*

## Conditionals

There are two types of conditionals `if-else` statements and `switch-case` statements.

## Creating an if-else Statement

Start the conditional with an `if` and type the conditional after it followed by a `{`. Inside the `{}` type what should
happen when the condition is true. After the ending `}` an `else` statement can be added to include the case when
the `if` statement is not true.

```
var x = 6

if x > 5 {
    print("true")
} else {
    print("false")
}

Console: 
true
```

## Creating a switch-case Statement

Start with the keyword `switch` followed by the variable to compare against with an opening curly brace after it. Then
use the keyword `case`, followed by the value to be matched, followed by a colon. Then enter the statements to be
executed for this case. After all the cases have been stated then add `default` which is similar to a `case` but it is
at the end so that if the other cases do not happen then the program will go to `default` followed by a colon.

*Tip: Make sure to use a `break` at the end of the case unless the program is meant to pass through to the next case.*

```
month = 8
monthString: String 
        
switch month {
            case 1:  monthString = "January";
                     break;
            case 2:  monthString = "February";
                     break;
            case 3:  monthString = "March";
                     break;
            case 4:  monthString = "April";
                     break;
            default: monthString = "Invalid month";
                     break;
        }
```

# Operators

## Math Operators

These operators follow standard mathematical precedence and modulus is in the same category as multiplication and
division.

```
Operator    Logic           Description                                Example

+	    Addition	    Adds together two values	                x + y	
-	    Subtraction	    Subtracts one value from another            x - y	
*	    Multiplication  Multiplies two values                       x * y	
/	    Division	    Divides one value by another	        x / y	
^	    Power	    Raises one value to another	                x / y	
%	    Modulus	    Returns the division remainder	        x % y	
++	    Increment	    Increases the value of a variable by 1	 ++x	
--	    Decrement	    Decreases the value of a variable by 1	 --x
```

## Assignment Operators

``` 
Operator    Example     Equivalent Operation

=	    x = 5	    x = 5	
+=	    x += 3	    x = x + 3	
-=	    x -= 3	    x = x - 3	
*=	    x *= 3	    x = x * 3	
/=	    x /= 3	    x = x / 3	
^=	    x ^= 3	    x = x ^ 3	
```

## Logic Operators

``` 
Operator    Logic   Description                                                 Example

&& , and    and	    Returns true if both statements are true	                x < 5 && x < 10	
|| , or     or	    Returns true if one of the statements is true	        x < 5 || x < 4	
! , not	    not	    Reverse the result, returns false if the result is true	!(x < 5 && x < 10)
```

# Comparators

``` 
Operator                        Name                            Example

== , equals	                Equal to	                x == y	
!= , notequals	                Not equal	                x != y	
> , greaterthan	                Greater than	                x > y	
< , lessthan	                Less than	                x < y	
>= , greaterthanequal	        Greater than or equal to	x >= y	
<= , lessthanequal	        Less than or equal to           x <= y
```

# Collections

## Arrays

*Note: Arrays in Z do not have a fixed size*

In order to create an empty array, create a variable and set it equal to the data type surrounded by brackets, followed
by parentheses.

```
var someInts = [Int]()
```

To create an array and initialize it with values, create a variable, declare its type, and set it equal to a list of the
type it was initialized to surrounded by brackets.

```
var shoppingList: [String] = ["Eggs", "Milk"]
```

In order to add something to the array, use the built-in function `add()` and to remove an item from the list,
use `remove()`.

## Tuples

In Z a tuple type is a comma-separated list of values, enclosed in parentheses. A tuple can contain values of different
types.

```
var someTuple = (top: 10, bottom: 12)
someTuple = (top: 4, bottom: 42)
someTuple = (9, "Thursday")
```

# Built-In Functions

In order to have words, numbers, etc. appear in the console, use the function `print()`.

KeyWords | Description
-------- | -----------
var | Define a variable
const | Define a constant
if | Conditional
else | Introduces alternate condition for an if statement
switch | Conditional
case | Identifies each choice in a switch statement
func | Begins a function
for | Begins a for loop
for-in | Begins a for loop over set interval
while | Begins a while loop
+ | Adds together two values
- | Subtracts one value from another
* | Multiplies two values
/ | Divides one value by another
^ | Raises one value to another
% | Returns the division remainder
++ | Increases the value of a variable by 1
-- | Decreases the value of a variable by 1
= | Equals the value after this symbol
+= | Adds the value before the symbol to the value after the symbol
-= | Subtracts the value after the symbol from the value before the symbol
*= | Multiplies the value before the symbol with the value after the symbol
/= | Divides the value before the symbol by the value after the symbol
^= | Raises the value before the symbol to the value after the symbol
== | Equal to comparator
!= | Not equal to comparator
\> , greaterthan | Greater than comparator
< , lessthan | Less than comparator
\>= , greaterthanequal | Greater than or equal to comparator
<= , lessthanequal | Less than or equal to comparator
&& , and | Returns true if both statements are true
&#124;&#124; , or  | Returns true if one of the statements is true
! , not  | Reverse the result, returns false if the result is true
// | Comment
/* | Start of section comment
*/ | End of section comment


