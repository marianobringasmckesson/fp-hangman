package functional;

import java.util.function.Function;

import static functional.Tuple.tuple2;

public final class FunctionUtils {

	private FunctionUtils() {}

	// x -> (u = f(x), n = g(x)) -> (u, n) // product ~> zip ~> ap ~> map2 // f <*> g
	public static <A, B, C> Function<A, Tuple<B, C>> zip(Function<A, B> f, Function<A, C> g) {
		return a -> tuple2(f.apply(a), g.apply(a));
	}

	// f *> g
	public static <A, B, C> Function<A, C> zipRight(Function<A, B> f, Function<A, C> g) {
		return zip(f, g).andThen(Tuple::_2);
	}

	// f <* g
	public static <A, B, C> Function<A, B> zipLeft(Function<A, B> f, Function<A, C> g) {
		return zip(f, g).andThen(Tuple::_1);
	}

	public static <A, B> Function<A, B> flatten(Function<A, Function<A, B>> f) { return a -> f.apply(a).apply(a); }

	public static <A, B, C> Function<A, C> flatMap(Function<A, B> f, Function<B, Function<A, C>> g) { return a -> g.apply(f.apply(a)).apply(a); }

}
