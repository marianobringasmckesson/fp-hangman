package services;

import functional.Unit;

import static functional.Unit.unit;

public class LiveConsole implements Console {

	@Override public String readLine() {
		return System.console().readLine();
	}

	@Override public Unit println(String message) {
		System.out.println(message);
		return unit();
	}
}
