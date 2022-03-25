package services;

import java.util.Locale;
import java.util.Scanner;
import functional.Unit;

import static functional.Unit.unit;

public class LiveConsole implements Console {

	private Scanner scanner = new Scanner(System.in);

	@Override public String readLine() {
//		return System.console().readLine();
		return scanner.nextLine();
	}

	@Override public Character readCharacter() {
		return scanner.nextLine().toUpperCase().charAt(0);
	}

	@Override public Unit println(String message) {
		System.out.println(message);
		return unit();
	}
}
