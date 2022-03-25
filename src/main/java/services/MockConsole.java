package services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import functional.Unit;

import static functional.Unit.unit;

public class MockConsole implements Console {

	private Iterator<String> inputs;

	private List<String> outputs = new ArrayList<>();

	public MockConsole(List<String> inputs) {
		this.inputs = inputs.iterator();
	}

	public List<String> getOutputs() {
		return outputs;
	}

	@Override public String readLine() {
		return inputs.next();
	}

	@Override public Character readCharacter() {
		return null;
	}

	@Override public Unit println(String message) {
		outputs.add(message);
		return unit();
	}
}
