package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
		List<Character> updatedLetters = Stream.concat(played.stream(), Stream.of(l)).collect(Collectors.toList());
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

	private HangmanState abandoned() {
		return new HangmanState(player, word, played, HangmanGameStatus.ABANDONED);
	}


}
