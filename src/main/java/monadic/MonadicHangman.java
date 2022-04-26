package monadic;

import java.util.function.Function;
import java.util.stream.Stream;
import functional.Tuple;
import functional.Unit;
import functional.monad.Reader;

import model.HangmanState;
import services.*;

import static functional.monad.Reader.of;

import static model.HangmanState.initialize;

public class MonadicHangman {

	private static final Function<String,
			                        Reader<Environment, Unit>> printLn = m -> of(e -> e.println(m));

	private static final Reader<Environment, Unit> askForName = printLn.apply("Please enter your name: ");
	private static final Reader<Environment, String> readName = of(Console::readLine);
	private static final Reader<Environment, String> getPlayersName = askForName.zipRight(readName);

	private static final Reader<Environment, String> pickWord = of(Dictionary::pickWord);

	private static final Reader<Environment, HangmanState> initializeGameState = getPlayersName.zip(pickWord.map(String::toUpperCase))
			                                                                                     .map(t -> initialize(t._1(), t._2()));

	private static final Function<HangmanState,
			                        Reader<Environment, HangmanState>> printGameState = hs -> printLn.apply(hs.toString())
			                                                                                         .map(x -> hs);

	private static final Reader<Environment, Unit> askForLetter = printLn.apply("Please enter a letter from a-z or any other to quit: ");
	private static final Reader<Environment, Character> readCharacter = of(Console::readCharacter);
	private static final Reader<Environment, Character> getLetter = askForLetter.zipRight(readCharacter).map(Character::toUpperCase);

	private static final Function<Tuple<HangmanState, Character>,
			                        HangmanState> evaluateLetter = t -> t._1().play(t._2());

	private static final Function<HangmanState,
			                        Reader<Environment, HangmanState>> lift = hs -> of(e -> hs);

	private static final Function<HangmanState,
			                        Reader<Environment, HangmanState>> turn = hs -> lift.apply(hs)
			                                                                            .zip(getLetter)
			                                                                            .map(evaluateLetter)
			                                                                            .flatMap(printGameState);

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	private static final Function<HangmanState,
			                        Reader<Environment, HangmanState>> gameLoop = s -> of(e -> Stream.iterate(s, ns -> turn.apply(ns)
			                                                                                                               .run(e))
			                                                                                         .filter(HangmanState::isGameFinished)
			                                                                                         .findFirst()
			                                                                                         .get());

	private static final Reader<Environment, HangmanState> hangman = initializeGameState.flatMap(printGameState)
			                                                                              .flatMap(gameLoop);

	private static final Function<HangmanState,
			                        Reader<Environment, Unit>> printResult = hs -> printLn.apply(hs.getPlayer() + " has " +
			                                                                                       hs.getStatus() + "!!!");

	public static void main(String[] args) {
		hangman.flatMap(printResult)
				 .run(new LiveEnvironment(new LiveConsole(), () -> "CAT"));
	}
}
