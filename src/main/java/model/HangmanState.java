package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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


}
