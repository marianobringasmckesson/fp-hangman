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

	private static final Function<String, Function<Environment, Unit>> askFor = m -> e -> e.println(m);

	private static final Function<Environment, Unit> askForName = askFor.apply("Please enter your name: ");
	private static final Function<Environment, String> readName = Console::readLine;

	private static final Function<Environment, String> getPlayersName = zipRight(askForName, readName);
	private static final Function<Environment, String> pickWord = Dictionary::pickWord;

	private static final Function<Tuple<String, String>, HangmanState> initState = t -> initialize(t._1(), t._2());
	private static final Function<Environment, HangmanState> initGame = zip(getPlayersName, pickWord.andThen(String::toUpperCase)).andThen(initState);

	private static final Function<HangmanState, Function<Environment, HangmanState>> printState = hs -> zipRight(askFor.apply(hs.toString()),
			                                                                                                       e -> hs);

	private static final Function<Environment, HangmanState> beginGame = flatMap(initGame, printState);

	private static final Function<Environment, Character> askForLetter = zipRight(askFor.apply("Please enter a letter from a-z or any other to quit: "),
			                                                                        Console::readCharacter);

	private static final Function<Tuple<HangmanState, Character>, HangmanState> evaluateLetter = t -> t._1().play(t._2());

	private static final Function<HangmanState, Function<Environment, HangmanState>> turn = hs -> flatMap(zip(e -> hs, askForLetter.andThen(Character::toUpperCase)).andThen(evaluateLetter), printState);

	// Homework: find a way to define loop as a Function variable so that it can be combined with beginGame.
	private static HangmanState loop(Environment environment, HangmanState currentState) {
		return currentState.isGameFinished() ? currentState : loop(environment, turn.apply(currentState).apply(environment));
	}

	public static void main(String[] args) {
		Environment environment = new LiveEnvironment(new LiveConsole(), new Dictionary() {
			@Override public String pickWord() {
				return "cat";
			}
		});
		HangmanState state = beginGame.apply(environment);
		state = loop(environment, state);
		System.out.println(state.getStatus());
	}

}
