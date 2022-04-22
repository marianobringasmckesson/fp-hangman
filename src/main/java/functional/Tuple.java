package functional;

import java.util.Objects;
import java.util.function.Function;


public class Tuple<A, B> {

	private final A fst;

	private final B snd;

	private Tuple(A fst, B snd) {
		assert Objects.nonNull(fst) && Objects.nonNull(snd);

		this.fst = fst;
		this.snd = snd;
	}

	public static <A, B> Tuple<A, B> tuple2(A fst, B snd) {
		return new Tuple<>(fst, snd);
	}

	public A _1() {
		return this.fst;
	}

	public B _2() {
		return this.snd;
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Tuple<?, ?> tuple = (Tuple<?, ?>) o;
		return fst.equals(tuple.fst) && snd.equals(tuple.snd);
	}

	@Override public int hashCode() {
		return Objects.hash(fst, snd);
	}

	@Override public String toString() {
		return "(" + fst + ", " + snd + ")";
	}

	public <C, D> Tuple<C, D> bimap(Function<? super A, ? extends C> f, Function<? super B, ? extends D> g) {
		return tuple2(f.apply(fst), g.apply(snd));
	}
}
