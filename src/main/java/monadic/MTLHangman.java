package monadic;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import functional.Tuple;
import functional.Unit;
import functional.monad.Either;
import functional.monad.EitherTReader;
import model.HangmanState;
import services.*;

import static functional.Unit.unit;
import static functional.monad.EitherTReader.of;
import static functional.monad.Either.*;
import static model.HangmanState.initialize;

public class MTLHangman {

	private static final Predicate<String> isNameValid = s -> !(s.isEmpty());
	private static final Function<String,
			                        Either<String, String>> validateName = name -> isNameValid.test(name) ? right(name) :
			                                                                                                left("Invalid name");

	private static final Function<String,
			                        EitherTReader<Environment, String, Unit>> printLn = m -> of(e -> right(e.println(m)));

	private static final EitherTReader<Environment, String, Unit> askForName = printLn.apply("Please enter your name: ");
	private static final EitherTReader<Environment, String, String> readName = of(e -> validateName.apply(e.readLine()));
	private static final EitherTReader<Environment, String, String> getPlayersName = askForName.zipRight(readName);

	private static final EitherTReader<Environment, String, String> pickWord = of(e -> right(e.pickWord()));

	private static final EitherTReader<Environment, String, HangmanState> initializeGameState =
			getPlayersName.zip(pickWord.map(String::toUpperCase))
					        .map(t -> initialize(t._1(), t._2()));

	private static final Function<HangmanState,
			                        EitherTReader<Environment, String, HangmanState>> printGameState =
			hs -> printLn.apply(hs.toString())
			             .map(x -> hs);

	private static final EitherTReader<Environment, String, Unit> askForLetter = printLn.apply("Please enter a letter from a-z or any other to quit: ");
	private static final EitherTReader<Environment, String, Character> readCharacter = of(e -> fromTry(e::readCharacter).leftMap(t -> "Invalid character"));
	private static final EitherTReader<Environment, String, Character> getLetter = askForLetter.zipRight(readCharacter).map(Character::toUpperCase);

	private static final Function<Tuple<HangmanState, Character>,
			                        HangmanState> evaluateLetter = t -> t._1().play(t._2());

	private static final Function<HangmanState,
			                        EitherTReader<Environment, String, HangmanState>> lift = hs -> of(e -> right(hs));

	private static final Function<HangmanState,
			                        EitherTReader<Environment, String, HangmanState>> turn = hs -> lift.apply(hs)
			                                                                                           .zip(getLetter)
			                                                                                           .map(evaluateLetter)
			                                                                                           .flatMap(printGameState);

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	private static final Function<Either<String, HangmanState>,
			                        EitherTReader<Environment, String, HangmanState>> gameLoop =
			s -> of(e -> Stream.iterate(s, ns -> ns.flatMap(hs -> turn.apply(hs).run(e)))
			                   .filter(hs -> hs.fold(x -> true, HangmanState::isGameFinished))
			                   .findFirst()
			                   .get());

	private static final EitherTReader<Environment, String, HangmanState> hangman =
			initializeGameState.flatMap(printGameState)
			                   .flatMap(hs -> gameLoop.apply(right(hs)));

	private static final Function<HangmanState,
			                        EitherTReader<Environment, String, Unit>> printResult = hs -> printLn.apply(hs.getPlayer() + " has " +
			                                                                                                      hs.getStatus() + "!!!");

	public static void main(String[] args) {
		hangman.flatMap(printResult)
				 .run(new LiveEnvironment(new LiveConsole(), () -> "CAT")).fold(x -> { System.out.println(x); return unit(); },
						                                                          y -> unit());
	}

}
