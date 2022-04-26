package functional.monad;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import functional.Tuple;
import functional.Unit;

import static functional.Tuple.tuple2;
import static java.util.Objects.nonNull;

import static functional.Unit.unit;

public abstract class Either<A, B> {

	private Either() {
	}

	@SuppressWarnings("unchecked") public static <A, B> Either<A, B> left(A value) {
		return (Either<A, B>) new Left<>(value);
	}

	@SuppressWarnings("unchecked") public static <A, B> Either<A, B> right(B value) {
		return (Either<A, B>) new Right<>(value);
	}

	public static <A, B> Either<A, B> of(B value) {
		return right(value);
	}

	@SuppressWarnings("unchecked")
	public static <A extends Throwable, B> Either<A, B> fromTry(Supplier<B> f) {
		try {
			return right(f.get());
		} catch (Throwable t) {
			return (Either<A, B>) left(t);
		}
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public static <B> Either<Unit, B> fromOptional(Optional<B> o) {
		return o.<Either<Unit, B>>map(Either::right)
				  .orElseGet(() -> left(unit()));
	}

	public abstract <C> Either<A, C> map(Function<? super B, ? extends C> f);

	public abstract <C> Either<C, B> leftMap(Function<? super A, ? extends C> f);

	public abstract <A1 extends A, C> Either<A, C> flatMap(Function<? super B, ? extends Either<A1, C>> f);

	public abstract <B1 extends B, C> Either<C, B> leftFlatMap(Function<? super A, ? extends Either<C, B1>> f);

	public final <A1 extends A, C> Either<A, Tuple<B, C>> zip(Either<A1, C> e) {
		assert nonNull(e);

		return flatMap(b -> e.map(c -> tuple2(b, c)));
	}

	public final <B1 extends B, C> Either<Tuple<A, C>, B> leftZip(Either<C, B1> e) {
		assert nonNull(e);

		return leftFlatMap(a -> e.leftMap(c -> tuple2(a, c)));
	}

	public abstract <C> C fold(Function<? super A, ? extends C> f, Function<? super B, ? extends C> g);

	public abstract B resolve(Function<? super A, ? extends B> f);

	public abstract boolean isLeft();

	public abstract boolean isRight();

	private static class Left<A> extends Either<A, Object> {

		private final A value;

		private Left(A value) {
			assert nonNull(value);

			this.value = value;
		}

		@SuppressWarnings("unchecked")
		@Override public <C> Either<A, C> map(Function<? super Object, ? extends C> f) {
			return (Either<A, C>) this;
		}

		@Override public <C> Either<C, Object> leftMap(Function<? super A, ? extends C> f) {
			assert nonNull(f);

			return left(f.apply(value));
		}

		@SuppressWarnings("unchecked")
		@Override public <A1 extends A, C> Either<A, C> flatMap(Function<? super Object, ? extends Either<A1, C>> f) {
			return (Either<A, C>) this;
		}

		@SuppressWarnings("unchecked")
		@Override public <B1, C> Either<C, Object> leftFlatMap(Function<? super A, ? extends Either<C, B1>> f) {
			assert nonNull(f);

			return (Either<C, Object>) f.apply(value);
		}

		@Override public <C> C fold(Function<? super A, ? extends C> f, Function<? super Object, ? extends C> g) {
			assert nonNull(f);

			return f.apply(value);
		}

		@Override public Object resolve(Function<? super A, ?> f) {
			assert nonNull(f);

			return f.apply(value);
		}

		@Override public boolean isLeft() {
			return true;
		}

		@Override public boolean isRight() {
			return false;
		}

	}

	private static class Right<B> extends Either<Object, B> {

		private final B value;

		private Right(B value) {
			assert nonNull(value);

			this.value = value;
		}

		@Override public <C> Either<Object, C> map(Function<? super B, ? extends C> f) {
			assert nonNull(f);

			return right(f.apply(value));
		}

		@SuppressWarnings("unchecked")
		@Override public <C> Either<C, B> leftMap(Function<? super Object, ? extends C> f) {
			return (Either<C, B>) this;
		}

		@SuppressWarnings("unchecked")
		@Override public <A1, C> Either<Object, C> flatMap(Function<? super B, ? extends Either<A1, C>> f) {
			assert nonNull(f);

			return (Either<Object, C>) f.apply(value);
		}

		@SuppressWarnings("unchecked")
		@Override public <B1 extends B, C> Either<C, B> leftFlatMap(Function<? super Object, ? extends Either<C, B1>> f) {
			return (Either<C, B>) this;
		}

		@Override public <C> C fold(Function<? super Object, ? extends C> f, Function<? super B, ? extends C> g) {
			assert nonNull(g);

			return g.apply(value);
		}

		@Override public B resolve(Function<? super Object, ? extends B> f) {
			return value;
		}

		@Override public boolean isLeft() {
			return false;
		}

		@Override public boolean isRight() {
			return true;
		}
	}

}
