import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import functional.Tuple;
import functional.Unit;
import model.HangmanState;
import services.*;

import static functional.FunctionUtils.*;
import static model.HangmanState.initialize;

public final class Application {

	private Application() {}

	/*
		Functional Programming:

		1. No mutation / No side effects / Referential Transparency  y = f(x)
		                                                             Tuple2<>(f(x), f(x)) = Tuple2<>(y, y)

		2. You only depend on your inputs to calculate your outputs -> locally reason about what's going on
		3. NO EXCEPTIONS!!!!!!! (Your function signature needs to be sincere) division(n, d): Optional<r> = d == 0 ? None : Some(n / d);
	 */

	/*
		Asking the player for his/her name -> Picking up a word at random -> display the initial state with a hint (i.e. word 'cat' ___)
		                                   (game loop) -> ask for letter -> evaluate the letter |-> letter is not valid -> ABANDONED
		                                                                                        |-> letter is present |-> no more letters to guess -> WON
		                                                                                        |                     |-> more letters to guess -> CONTINUE
		                                                                                        |-> letter is not present |-> threshold is met -> LOST
		                                                                                                                  |-> threshold is not met -> CONTINUE
		                                                  render current state
		                                   -> render the result
	 */

	// Homework: Try to generalize the asking
	private static final Function<Environment, Unit> askForName = c -> c.println("Please enter your name: ");
	private static final Function<Environment, String> readName = Console::readLine;

	private static final Function<Environment, String> getPlayersName = zipRight(askForName, readName);
	private static final Function<Environment, String> pickWord = Dictionary::pickWord;

	private static final Function<Tuple<String, String>, HangmanState> initState = t -> initialize(t._1(), t._2());
	private static final Function<Environment, HangmanState> initGame = zip(getPlayersName, pickWord).andThen(initState);

	// private static final Function<Environment, HangmanState> f = e -> { HangmanState s = initGame.apply(e); e.println(s.toString()); return s; };

	public static void main(String[] args) {

	}

}
