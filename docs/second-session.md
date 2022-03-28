# Second session (25-MAR-22)

During the second session, we started by looking into the homework/challenge left: Generalize a piece of code.

```java
// Homework: Try to generalize the asking
private static final Function<Environment, Unit> askForName = c -> c.println("Please enter your name: ");
```

In essence, we need to be able to provide the message to this function instead of hard-coding it.

An option was to enhance the ```Environment``` to also include the message. Although it could potentially resolve the problem, your ```Environment``` is now "polluted". Instead of representing I/O operations, it now starts collecting state as well, 
which is not a common practice.  

Some tried using ```BiFunction```. ```BiFunction``` is similar to function, but instead of only taking one parameter, it takes two: ```Environment``` and a ```String``` representing the message. In this case, the modified code looks like this:
```java
private static final BiFunction<String, Environment, Unit> askForName = (m, c) -> c.println(m);
```
The challenge with this option is that ```BiFunction```s are not easy to directly compose. It's always preferable to have a single-argument-function.

Now there's a trick here that can help us: **Currying**. 

## Currying

Currying is a technique very popular in FP. It allows to fix one by one arguments of a multi-argument function. So, if you have a function bf(x, y) -> z, one can turn this into a function that
first receives the x and gives you another function with all of its x's replaced by the value provided and that's expecting the y to finish its work. An example:
```java
private static final BiFunction<String, Environment, Unit>         askForName  = (m, c) -> c.println(m);
private static final Function<String, Function<Environment, Unit>> askForName2 = m -> c -> c.println(m);
```
It's simple to spot that both functions do the same, but the curried one is expressing two ```Functions```:
The first one is to capture the message. With the message, it creates a new ```Function```, this time, with the message fixed. In our case, we now have a single-argument-function to continue with the process.

[More information on Currying](https://en.wikipedia.org/wiki/Currying)

It was said as well that Currying is a primitive way of doing **Dependency Injection** in FP. Usually, external dependencies are "fixed" by partially applying them one by one, and Currying helps to implement it.

## Continuing with the game

After sorting out the challenge, we continued building the game from where we left: We retrieved the player, we picked a word and with that, we initialized the state of the game.
The next step in the process is to display such state so that the player can start guessing. This puts us another compositional challenge in the way:
```java
private static final Function<HangmanState, Function<Environment, Unit>> printState = hs -> askFor.apply(hs.toString());
```

The ```initGame``` function provides a ```HangmanState``` instance as long as we can provide it with an ```Environment```. Composing ```printState``` with it will make us lose that state. 
```java
private static final Function<HangmanState, Function<Environment, Unit>> printState = hs -> askFor.apply(hs.toString());
private static final Function<Environment, Function<Environment, Unit>> f = initGame.andThen(printState); 

```

Thus, we'll need to find a way to keep it. One way is a form of composition we're already familiar with: zipping.

```java
	private static final Function<HangmanState, Function<Environment, HangmanState>> printState = hs -> zipRight(askFor.apply(hs.toString()),
			                                                                                                       e -> hs);

	private static final Function<Environment, Function<Environment, HangmanState>> f = initGame.andThen(printState);
```

## FlatMap

This seems to be resolving some of the problem, but there's something in the signature of ```f``` that looks a bit off: 
It's a ```Function``` that takes an ```Environment``` to produce a ```Function``` that takes an ```Environment``` to produce a ```HangmanState```. This is a pattern we've seen in other places.

```java
public void sample(Integer x, Integer y) {
	Optional<Optional<Integer>> v = Optional.ofNullable(x)
                                           .map(v1 -> Optional.ofNullable(y));
}
```
As you can see from the type signature of ```andThen```, it passes the output of one ```Function``` as the input of the next to get a v2. This is exactly the same as to what ```map``` does, and 
this is no accident: ```map``` is the same as ```andThen```. So we're in the same situation as in the sample above: we have two nested ```Optional```s and we only care about one, in this case, 
a ```Function``` taking an ```Environment``` and returns another ```Function``` that takes an ```Environment```. What we need is to **flatten** the structure:

```java
public static <A, B> Function<A, B> flatten(Function<A, Function<A, B>> f) { return a -> f.apply(a).apply(a); }
```
In the case of our ```Function```s, flattening means **invoking** the method (in this case, by calling ```apply```, one per nested ```Function```).

Now we can compose ```initGame``` with ```printState``` safely:
```java
private static final Function<HangmanState, Function<Environment, HangmanState>> printState = hs -> zipRight(askFor.apply(hs.toString()),
			                                                                                                       e -> hs);

private static final Function<Environment, HangmanState> f = flatten(initGame.andThen(printState));
```
The action of **Flattening** after **mapping** (in this case, composing via **andThen**), is so common that it also has a helper method: ```flatMap```:
```java
public static <A, B, C> Function<A, C> flatMap(Function<A, B> f, Function<B, Function<A, C>> g) {
	return flatten(f.andThen(g));
}
```

FlatMap is a very important form of composition as it describes **sequential** computations in a context. 
In this case, it makes it clear that printState needs the output of the beginning of the game. There's no printing of there's no state.

This type of composition is known as **Monadic** composition.

## Functors, Applicatives and Monads

One of the first roadblocks people getting into FP find is the terminology. Inspired in Mathematical ideas, FP tends to leverage math concepts and names, which are sometimes "strange" for people without some background.
**Monad** is one of those things. Legend says that once you understand the concept of a Monad, you immediately loose the capability of explaining it to others. Joke aside, even without knowing, we use Monads constantly.

A Monad is some sort of computational context that is meant to model something about it. A context is something that is not the core of the functionality, but it describes situations around the core.
One example is the context of "Termination" (also used to describe when there's a result or there's not). In Haskell, this is described by the **Maybe Monad**. Scala has **Option** to model this situation. 
Java can leverage **Optional** (although it's not a lawful Monad - I'll cover this later). A None value (empty() in the case of Java) expresses that the computation has halted because you have no value available to proceed. 
This is the reason why map, filter, flatMap, etc. will be ignored if the instance is None (or empty).
Another example is expressing "Multiplicity" (0, 1 or more elements), for which Haskell, Scala and Java have List. When this is an infinite quantity, Streams is used.
"Asynchronicity" is expressed using Futures, CompletableFutures or Promises, etc. This is not meant to be an exhaustive list, but I wanted to show that a lot of structures we use are actually Monads.

What's important about Monads is that they describe **well-behaved sequential composition** within a context.

### The Functor hierarchy

There's a very important hierarchy of structures that describe contexts and how to operate with them. In the root of that hierarchy you can find the concept of a **Functor**.

#### Functor

A Functor is a structure that describes how to operate with something within a context.

```java
import java.util.function.Function;

public interface Functor<A> {

	<B> Functor<B> map(Function<? super A, ? extends B> f);

}
```

In addition to this particular signature, to be a proper Functor you need to meet some laws or properties:

##### Composition

Having any Functor ```Functor<A> fa = ...``` and any two functions, ```B f(A value)``` and ```C g(B value)```, the result of ```fa.map(f).map(g)``` is the same as ```fa.map(f.andThen(g))```.

##### Identity

Having any Functor ```Functor<A> fa = ...```, the result of ```fa.map(Function.identity)``` renders the same ```fa```.

> **Note on the meaning of "Structure"**: Structure is a combination an interface (describing a behavior/operation) and a collection of properties/laws/rules a well behaved implementation should follow. In this case, any appropriate Functor will not only have to implement the ```map``` function, but also needs to meet its two properties: composition and identity. 

#### Applicative Functor

An **Applicative Functor** is a Functor that adds a bit more structure to it:

```java
public interface Applicative<A> extends Functor<A> {

	Applicative<A> pure(A value);

	<B> Applicative<Tuple<A, B>> zip(Applicative<B> ap2);
}
```

In addition to these methods, Applicative Functors need to meet the following laws:

##### Associativity

Given ```Applicative<A> fa = ...```, ```Applicative<B> fb = ...``` and ```Applicative<C> fc = ...```, ```fa.zip(fb.zip(fc))``` is **isomorphic** to ```fa.zip(fb).zip(fc)```. It's isomorphic because ```Tuple<A, Tuple<B, C>>``` is not exactly the same as ```Tuple<Tuple<A, B>, C>```.

##### Identity laws

Given ```Applicative<A> fa = ...```, ```pure(Unit.unit()).zip(fa)``` is isomorphic to ```fa``` (Left identity law).

Given ```Applicative<A> fa = ...```, ```fa.zip(pure(Unit.unit()))``` is isomorphic to ```fa``` (Right identity law). 

The difference between Functor and Applicative is that Applicative allows to handle **multiple independent** effects.

#### Monad

A **Monad** is an Applicative functor with another powerful addition:

```java
import java.util.function.Function;


public interface Monad<A> extends Applicative<A> {
	Monad<A> flatten(Monad<Monad<A>> value);

	default <B> Monad<B> flatMap(Function<? super A, ? extends Monad<B>> f) {
		return flatten(this.map(f));
    }
}
```
The main difference between Monad and Applicative is that Monads model sequential computations. Another big difference between Monads vs Applicative Functors and Functors is that nested Monads don't always compose, whereas nested Applicative Functors or nested Functors do.

[One visual intuition of Functor, Applicative and Monad](https://medium.com/@tzehsiang/javascript-functor-applicative-monads-in-pictures-b567c6415221)

## State change

Dealing with state changes in FP is not the same as with traditional OOP. Mutation is not a possibility, so we need to deal with change by producing a new instance of the data to be modified and use that instead.

In the case of our game, each time we make a guess, we change the state of the game: we need to keep track of the newly played letter and we need to update the status of the game (is it LOST, WON, ABANDONED or are we still playing?).

During our session, we leveraged the OO-way of making the change (infix notation of a class method) by making a ```play``` method within ```HangmanState``` class. This method leverages the ```this``` reference to understand current state, and receives the letter being played to produce a fresh instance of HangmanState with the state changed.

As homework, we need to remove the reference to ```this``` from the method.

## What we learned so far

During our journey while building Hangman, we've encountered certain challenges to make sure we remained functional:

- We isolated our I/O dependencies into an Environment
- We created small pieces for which we found ways of composing them to build more complicated pieces
- We learned different ways of composing: mapping, zipping and flatMapping.
  - For functions that we didn't make use of the environment, a simple ```andThen``` composition (```map```) sufficed.
  - For functions which we needed to combine their effects, both depending on the environment but each being independent of each other, we use ```zip``` (or their left and right alternatives if we want to ignore the outcome of one of the sides)
  - For functions which we needed to combine effects, but one is dependent of the output of the first one, we needed to use ```flatMap```.

This context that allows passing a dependency (in this case, Environment) implicitly is known as ```Reader Monad```. We used the regular Java Function and a set of helper methods defined in FunctionUtils to provide the structure needed to create a Reader Monad.
