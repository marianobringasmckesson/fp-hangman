package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static functional.FunctionUtils.zip;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import functional.Tuple;


public class HangmanState {

	private static final int THRESHOLD = 7;

	private String player;
	private String word;
	private List<Character> played;
	private HangmanGameStatus status;

	private HangmanState(String player, String word, List<Character> played, HangmanGameStatus status) {
		this.player = player;
		this.word   = word;
		this.played = new ArrayList<>(played);
		this.status = status;
	}

	public static HangmanState initialize(String player, String word) {
		return new HangmanState(player, word, Collections.emptyList(), HangmanGameStatus.PLAYING);
	}

	/*
	- Any letter not in A-Z means abandonment (done)
   - Finished games are not altered (done)
   - Played an already played letter
     WINDOW -> {W, I, N, D, O}

		Homework: OOP way -> FP way
	 */
	public HangmanState play(Character l) {
		if(isGameFinished() || played.contains(l)) return this;
		if(!Character.isAlphabetic(l)) return abandoned();
		Set<Character> wordLetters = word.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
		Set<Character> remaining = wordLetters.stream().filter(wl -> !played.contains(wl)).collect(Collectors.toSet());
		List<Character> updatedLetters = concat(played.stream(), Stream.of(l)).collect(toList());
		if(remaining.contains(l)) {
			if(remaining.size() == 1) {
				return new HangmanState(player, word, updatedLetters, HangmanGameStatus.WON);
			} else {
				return new HangmanState(player, word, updatedLetters, status);
			}
		} else {
			if(updatedLetters.stream().filter(pl -> !wordLetters.contains(pl)).count() >= THRESHOLD) {
				return new HangmanState(player, word, updatedLetters, HangmanGameStatus.LOST);
			} else {
				return new HangmanState(player, word, updatedLetters, status);
			}
		}
	}

	public boolean isGameFinished() {
		return HangmanGameStatus.ABANDONED.equals(status) ||
				 HangmanGameStatus.WON.equals(status) ||
				 HangmanGameStatus.LOST.equals(status);
	}

	public String getStatus() {
		return status.toString();
	}

	public String getPlayer() {
		return player;
	}

	private static final String HEAD        = "           O ";
	private static final String BODY        = "           | ";
	private static final String RIGHT_ARM   = "           |\\ ";
	private static final String BOTH_ARMS   = "          /|\\ ";
	private static final String RIGHT_LEG   = "            \\ ";
	private static final String BOTH_LEGS   = "          / \\ ";
	private static final String GALLOWS_LINE = " | ";
	private static final String HANGED_HEAD = "           \\O";

	private static final Function<Stream<String>, Stream<String>> prependUpperSection = s -> concat(of(" |------------| ", " |            | "), s);

	private static final Function<Stream<String>, Stream<String>> appendLowerSection = s -> concat(s, of(GALLOWS_LINE, GALLOWS_LINE, "____ "));

	private static final Function<Stream<String>, String> buildGallows = s -> s.collect(joining("\n"));

	private static final List<String> GALLOWS_STATE = of(
			of(GALLOWS_LINE, GALLOWS_LINE, GALLOWS_LINE),
			of(GALLOWS_LINE + HEAD, GALLOWS_LINE, GALLOWS_LINE),
			of(GALLOWS_LINE + HEAD, GALLOWS_LINE + BODY, GALLOWS_LINE),
			of(GALLOWS_LINE + HEAD, GALLOWS_LINE + RIGHT_ARM, GALLOWS_LINE),
			of(GALLOWS_LINE + HEAD, GALLOWS_LINE + BOTH_ARMS, GALLOWS_LINE),
			of(GALLOWS_LINE + HEAD, GALLOWS_LINE + BOTH_ARMS, GALLOWS_LINE + RIGHT_LEG),
			of(GALLOWS_LINE + HEAD, GALLOWS_LINE + BOTH_ARMS, GALLOWS_LINE + BOTH_LEGS),
			of(GALLOWS_LINE + HANGED_HEAD, GALLOWS_LINE + BOTH_ARMS, GALLOWS_LINE + BOTH_LEGS)
	).map(prependUpperSection.andThen(appendLowerSection).andThen(buildGallows))
			.collect(toList());

	public String toString() {
		return join.apply(zip(hint, missed).apply(word)
				                             .bimap(hf -> hf.andThen(s -> "Hint: " + s).apply(played),
				                                    mf -> zip(gallows, misses.andThen(s -> "Mistakes: " + s)).apply(mf.apply(played))))
				     .collect(joining("\n"));
	}

	private static final Function<Tuple<String,
			                              Tuple<String, String>>,
			                        Stream<String>> join = t -> of(t._2()._1(), t._1(), t._2()._2());

	private static final Function<String,
			                        Function<List<Character>, List<Character>>> missed = w ->
			                                                                             cs -> cs.stream()
																														    .filter(c -> !w.contains(c.toString()))
																														    .collect(toList());

	private static final Function<String,
			                        Function<List<Character>, String>> hint = w -> cs -> w.chars()
																												   .mapToObj(c -> (char) c)
																												   .map(c -> cs.contains(c) ? c.toString() : "_")
																												   .collect(joining(" "));

	private static final Function<List<Character>, String> misses = cs -> cs.stream()
			                                                                  .map(Object::toString)
			                                                                  .collect(joining(", "));

	private static final Function<List<Character>, String> gallows = cs -> GALLOWS_STATE.get(cs.size());


	private HangmanState abandoned() {
		return new HangmanState(player, word, played, HangmanGameStatus.ABANDONED);
	}


}
