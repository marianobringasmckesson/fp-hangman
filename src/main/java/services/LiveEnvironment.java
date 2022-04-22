package services;

import functional.Unit;


public class LiveEnvironment implements Environment {

	private Console console;

	private Dictionary dictionary;

	public LiveEnvironment(Console console, Dictionary dictionary) {
		this.console    = console;
		this.dictionary = dictionary;
	}

	@Override public String readLine() {
		return console.readLine();
	}

	@Override public Unit println(String message) {
		return console.println(message);
	}

	@Override public String pickWord() {
		return dictionary.pickWord();
	}

	@Override
	public Character readCharacter() {
		return readLine().charAt(0);
	}
}
