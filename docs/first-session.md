# First session (11-MAR-22)

During the first session, we covered the following:

## FP Principles

1. All functions must be total
2. All functions must be referentially transparent
3. All functions must be deterministic

### Function totality

A function is said to be **total** if it has a result defined for every possible input provided. 
If there's an input for which there's no result possible, the function is said to be **partial**.
Partial functions present the challenge that will throw exceptions if the input is not defined.
A simple and well known example of this happens when dividing two integers:
```java
public class Sample {
	
  public static int divide(int num, int den) {
    return num / den;
  }
  
}
```
Most of us realize that this function is not total: If ```den == 0``` we know that this code will throw an ```ArithmeticException```, which will interrupt the flow of the program.
Similar cases can be found when parsing numbers or dates out of Strings.

The presence of Partial Functions within our code makes the code challenging to understand for several reasons:
1. In order to understand what might be wrong, I need to understand what the function I'm calling might or might not do in case of errors (in the case of division, the function might capture the exception and wrap it in another exception, return a magic number, etc). This is not evident to me by simply looking at the function signature, so I need to go to the implementation of it. This need to having to go deeper into the code increases our **cognitive load** (what we need to keep in our minds to understand what's going on), making it harder to understand.
2. The previous point usually leads towards "defensive coding". We've seen this applied all over the place: checking for nulls "just in case", catching general "Exception" because you might not know what it can throw, etc. This defensive coding increases the "noise to code" ratio (amount of boilerplate surrounding our business logic), hiding the "real work" under a pile of ```try-catch-finally``` or ```if-then-else``` blocks.

By requiring Functions to be Total, we can make safe assumptions about them and reduce the need to go deeper.

#### What can I do if I have a Partial Function?

We can always turn a Partial Function into a total one by:

- Constraining our inputs to the ones the Function can handle
- Extending our outputs, describing a "Context"

##### Constraining inputs

```java
public class Sample {
	
  public static int divide(int num, NonZeroInt den) {
    return num / den.getValue();
  }
  
}
```

Here we can see in our Function signature that divide requites a num that can be any Integer, but den will be an instance of NonZeroInt (custom type that only allows Integer values that are non-zero). Because this is a safe operation, ```divide``` can always give us back a result.

##### Extending outputs using Contexts

A **Context** describes a specific type of situation. We're used to working with them though we usually don't refer to them as "Contexts". For instance, ```Optional<T>``` is a Context that describes the possibility of a value being absent or present. In the division example:
```java
import java.util.Optional;

public class Sample {
	
  public static Optional<Integer> divide(int num, int den) {
    return (den == 0) ? Optional.empty() : Optional.of(num / den);
  }
  
}
```
It's clear to the caller that the action of dividing two integers might not render a result. 
There are interesting things about Contexts:
1. They provide a way to force **Inversion of Control**: The caller needs to decide what to do in case of error.
2. They allow to defer that decision by providing ways to operate with the value within the Context (i.e. ```map```)
3. They compose (you can chain dependent operations, join independent operations, etc) within the Context.

We'll get into the details of Contexts incrementally throughout the sessions.

### Referential Transparency

A function is said to be Referentially transparent if it only depends on its input. What does this mean:

- The function doesn't access global state
```java
public class Sample {
	
	int value = 7;	
	
	public int nonReferential(int x) {
		return value + x;
    }
	
	public int referential(int x) {
		return 7 + x;
    }
	 
}
```

- The function doesn't do I/O
```java
public class Sample {
	
	public String nonReferentialAskForSomething(String request) {
		System.out.println(request);
		return System.console().readLine();
    }
	
	interface ConsoleService {
		
		void printLn(String message);
		String readLn();
		
    } 
	 
	public String askForSomethingReferentially(ConsoleService console, String message) {
		console.printLn(message);
		return console.readLn();
    } 
	 
}
```
- The function doesn't mutate state
```java
public class Sample {
	
	int value = 0;
	
	public int nonReferentialIncrease() {
		return value++;
    }
	 
	public int referentialIncrease(int prev) {
		return prev + 1;
    } 
	 
}
```

### Determinism

Tied to the other two characteristics is the idea of determinism, which means that for the same input, you should expect the same output. 
This is to be expected if the function is referentially transparent (no dependencies on global state or mutation, only relying on its inputs to produce an output) and total (it always has an output no matter which input is provided).
This concept is sometimes a bit complicated to understand when dealing with effects (such as doing I/O or picking a Random number). The thing is, when you're performing these types of operations, what you get back is not a result of I/O or a Random number, but rather a "description" of how to obtain it, which will never get executed until the last moment (what it's known as "The end of the world" in FP -> the ```main``` method).

### Pure Functions

A **pure** function is a function that meets the three characteristics described above: total, deterministic and referentially transparent. These types of functions are very valuable as they have a very interesting trait: <u>**THEY COMPOSE**</u>. 

## Function composition

It's common in Software Engineering to say that the way to deal with complexity is through a process of **ABSTRACTION**. When you abstract, you build a simplified model of the reality you're analyzing and start dividing the different pieces into smaller ones for a better understanding of each. Once the pieces are apart and have been studied, the next natural step is to do the opposite of Abstraction (it's dual): **TO COMPOSE**.

Composition is what gives FP a way to manage complexity by stitching together a solution from smaller pieces that fit together based on certain rules that can assure things will work. Pure functions give us those guarantees: they will always have an output, so we can always count on the flow to continue. Because of determinism and referential transparency, it's behavior is always going to be consistent.

### Types of Function composition

In this session we covered two different ways of composing functions:

1. Sequential composition
2. Product composition

#### Sequential composition

This type of composition happens when the output of a function is used as the input of the next function. It's a very common way of composing:

```java
import java.util.function.Function;

public class Sample {

  private static final Function<String, String> removeAllSpaces = s -> s.replaceAll("\\s", "");
  private static final Function<String, Integer> count = String::length;
  private static final Function<String, Integer> countNonSpaceOnly = removeAllSpaces.andThen(count);
	
}
```

Here, ```removeAllSpaces``` takes a ```String``` as an input, and produces a ```String``` without spaces as an output. ```count``` expects a ```String``` as input and produces an ```Integer``` as an output counting the number of characters in the given ```String```. ```countNonSpaceOnly``` puts these two functions together by taking the output of ```removeAllSpaces``` and passing it as the input to ```count```.

In essence:

```java
import java.util.function.Function;

public class Sample {
	
	public static <A, B, C> Function<A, C> andThen(Function<A, B> f, Function<B, C> g) {
		return a -> g.apply(f.apply(a));
    }
	
}
```

#### Product composition

This is usually known as **Applicative** composition, **zip** or **ap** in most FP languages/libraries. 
The main difference with sequential composition is that Product composition takes two functions expecting the same input, and return a function that will call both of them and collect both outputs.

```java
import java.util.function.Function;

class Tuple<A, B> {
	private A fst;
	private B snd;
	
	public Tuple(A fst, B snd) {
		this.fst = fst;
		this.snd = snd;
    }
	 
	public A _1() {
		return this.fst;
    }
	
	public B _2() {
		return this.snd;
    }
	 
	// equals, hashCode and toString 
}

public class Sample {
	
	public static <A, B, C> Function<A, Tuple<B, C>> zip(Function<A, B> f, Function<A, C> g) {
		return a -> new Tuple<>(f.apply(a), g.apply(a));
    }
	 
	public static <A, B, C> Function<A, B> zipLeft(Function<A, B> f, Function<A, C> g) {
		return zip().andThen(Tuple::_1);
    }

    public static <A, B, C> Function<A, C> zipRight(Function<A, B> f, Function<A, C> g) {
        return zip().andThen(Tuple::_2);
    }

}
```

It is common to sometimes discard one of the outputs of the combination (we're only interested in the function being evaluated but want to ignore its output), so there's usually two helper functions (**zipLeft** and **zipRight**) that zip and then only focuses on the desired outcome.
This type of composition is highly used, specially when there's no dependency between the combined functions, as it would allow for parallel computing if so desired. 
Because of its use being so frequent, languages that allow defining operators with symbols (such as Haskell or Scala) usually define **<\*>** (zip), **\*>** (zipRight) and **<*** (zipLeft) as handy operators:
```
f <*> g == zip(f, g)
f *> g  == zipRight(f, g)
f <* g  == zipLeft(f, g)
```
