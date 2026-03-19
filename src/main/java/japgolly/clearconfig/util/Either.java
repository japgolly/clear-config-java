package japgolly.clearconfig.util;

import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface Either<E, A> {

    record Success<E, A>(A value) implements Either<E, A> {

        @Override
        public <B> Either<E, B> map(Function<? super A, ? extends B> f) {
            return new Success<>(f.apply(value));
        }

        @Override
        public <B> Either<E, B> flatMap(Function<? super A, ? extends Either<E, B>> f) {
            return f.apply(value);
        }

        @Override
        public A getOrThrow() {
            return value;
        }

        @Override
        public void foreachFailure(Consumer<? super E> f) {
        }

        @Override
        @SuppressWarnings("unchecked")
        public <F> Either<F, A> mapFailure(Function<? super E, ? extends F> f) {
            return (Either<F, A>) this;
        }
    }

    record Failure<E, A>(E failure) implements Either<E, A> {

        @Override
        @SuppressWarnings("unchecked")
        public <B> Either<E, B> map(Function<? super A, ? extends B> f) {
            return (Either<E, B>) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <B> Either<E, B> flatMap(Function<? super A, ? extends Either<E, B>> f) {
            return (Either<E, B>) this;
        }

        @Override
        public A getOrThrow() {
            throw new RuntimeException("Either is left: " + failure);
        }

        @Override
        public void foreachFailure(Consumer<? super E> f) {
            f.accept(failure);
        }

        @Override
        public <F> Either<F, A> mapFailure(Function<? super E, ? extends F> f) {
            return new Either.Failure<>(f.apply(failure));
        }
    }

    public A getOrThrow();
    public <B> Either<E, B> map(Function<? super A, ? extends B> f);
    public <B> Either<E, B> flatMap(Function<? super A, ? extends Either<E, B>> f);
    public void foreachFailure(Consumer<? super E> f);
    public <F> Either<F, A> mapFailure(Function<? super E, ? extends F> f);
}
