package functional.monad;

import java.util.function.Function;
import java.util.function.Supplier;
import functional.Tuple;

import static java.util.Objects.nonNull;

import static functional.Tuple.tuple2;

public class EitherTReader<R, E, A> {

	private final Reader<? super R, Either<E, A>> run;

	private EitherTReader(Reader<? super R, ? extends A> r) {
		assert nonNull(r);

		this.run = r.map(Either::right);
	}

	private EitherTReader(Function<? super R, ? extends Either<E, A>> f) {
		assert nonNull(f);

		this.run = Reader.of(f);
	}

	public static <R, E, A> EitherTReader<R, E, A> of(Reader<? super R, ? extends A> r) {
		return new EitherTReader<>(r);
	}

	public static <R, E, A> EitherTReader<R, E, A> of(Function<? super R, ? extends Either<E, A>> f) {
		return new EitherTReader<>(f);
	}

	public static <R, A> EitherTReader<R, ?, A> lift(Function<? super R, ? extends A> f) {
		return of(f.andThen(Either::right));
	}

	public static <A> EitherTReader<? super Object, ?, A> of(Supplier<? extends A> s) {
		return lift((Function<? super Object, ? extends A>) x -> s.get());
	}

	public <B> EitherTReader<R, E, B> map(Function<? super A, ? extends B> f) {
		assert nonNull(f);

		return of(e -> run(e).map(f));
	}

	@SuppressWarnings("unchecked")
	public <R1 extends R, E1 extends E, B> EitherTReader<R, E, Tuple<A, B>> zip(EitherTReader<R1, E1, ? extends B> r2) {
		assert nonNull(r2);

		return flatMap(a -> (EitherTReader<R1, E, Tuple<A, B>>) r2.map(b -> (Tuple<A, B>) tuple2(a, b)));
	}

	public <R1 extends R, E1 extends E, B> EitherTReader<R, E, Tuple<A, B>> both(EitherTReader<R1, E1, ? extends B> r2) {
		return zip(r2);
	}

	public <R1 extends R, E1 extends E, B> EitherTReader<R, E, A> zipLeft(EitherTReader<R1, E1, ? extends B> r2) {
		return zip(r2).map(Tuple::_1);
	}

	public <R1 extends R, E1 extends E, B> EitherTReader<R, E, B> zipRight(EitherTReader<R1, E1, ? extends B> r2) {
		return zip(r2).map(Tuple::_2);
	}

	@SuppressWarnings("unchecked")
	public <R1 extends R, E1 extends E, B> EitherTReader<R, E, B> flatMap(Function<? super A, ? extends EitherTReader<R1, E1, B>> f) {
		assert nonNull(f);

		return of(r -> run(r).flatMap(a -> f.apply(a).run((R1) r)));
	}

	public Either<E, A> run(R environment) {
		assert nonNull(environment);

		return run.run(environment);
	}

}
