package functional;

public class Unit {

	private Unit() {}

	private static final Unit instance = new Unit();

	public static Unit unit() {
		return instance;
	}

	@Override public boolean equals(Object obj) {
		return (obj == this);
	}

	@Override public int hashCode() {
		return 1;
	}

	@Override public String toString() {
		return "()";
	}
}
