package functional.monad;

import java.util.function.Function;
import java.util.function.Supplier;
import functional.Tuple;

import static java.util.Objects.nonNull;

import static functional.Tuple.tuple2;

public final class Reader<R, A> {

	private final Function<? super R, ? extends A> run;

	private Reader(Function<? super R, ? extends A> f) {
		assert nonNull(f);

		this.run = f;
	}

	public static <A> Reader<? super Object, A> of(Supplier<? extends A> s) { return new Reader<>(x -> s.get()); }

	public static <R, A> Reader<R, A> of(Function<? super R, ? extends A> f) { return new Reader<>(f); }

	public static <R, A> Reader<R, A> lift(A value) { return of(x -> value); }

	public <B> Reader<R, B> map(Function<? super A, ? extends B> f) {
		assert nonNull(f);

		return of(run.andThen(f));
	}

	public <B> Reader<R, B> andThen(Reader<? super A, ? extends B> r) {
		assert nonNull(r);

		return map(r.run);
	}

	public <R1 extends R, B> Reader<R, Tuple<A, B>> zip(Reader<R1, ? extends B> r2) {
		assert nonNull(r2);

		return flatMap(a -> r2.map(b -> tuple2(a, b)));
	}

	public <R1 extends R, B> Reader<R, Tuple<A, B>> both(Reader<R1, ? extends B> r2) {
		return zip(r2);
	}

	public <R1 extends R, B> Reader<R, A> zipLeft(Reader<R1, ? extends B> r2) {
		return zip(r2).map(Tuple::_1);
	}

	public <R1 extends R, B> Reader<R, B> zipRight(Reader<R1, ? extends B> r2) {
		return zip(r2).map(Tuple::_2);
	}

	@SuppressWarnings("unchecked")
	public <R1 extends R, B> Reader<R, B> flatMap(Function<? super A, ? extends Reader<R1, B>> f) {
		assert nonNull(f);

		return of(e -> f.apply(run(e)).run((R1) e));
	}

	public A run(R environment) {
		assert nonNull(environment);

		return run.apply(environment);
	}

}
