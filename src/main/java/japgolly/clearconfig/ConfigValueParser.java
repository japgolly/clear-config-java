package japgolly.clearconfig;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import japgolly.clearconfig.util.*;

public interface ConfigValueParser<A> {
    public Either<ErrorMsg, A> parse(String s);

    public default <B> ConfigValueParser<B> map(Function<? super A, ? extends B> f) {
        return flatMap(a -> new Either.Success<>(f.apply(a)));
    }

    public default <B> ConfigValueParser<B> flatMap(Function<? super A, Either<ErrorMsg, B>> f) {
        return s -> parse(s).flatMap(f);
    }

    // ================================================================================================================

    public default ConfigDef<Optional<A>> get(String key) {
        return sources -> sources.get(key, this).mapFailure(e -> Set.of(e));
    }

    public default ConfigDef<A> getOrUse(String key, A defaultValue) {
        return sources -> get(key).run(sources).map(o -> o.orElseGet(() -> defaultValue));
    }

    public default ConfigDef<A> getOrParse(String key, String defaultValue) {
        return sources -> get(key).run(sources).flatMap(o -> {
            if (o.isEmpty())
                return parse(defaultValue).mapFailure(e -> Set.of(e));
            else
                return new Either.Success<>(o.get());
        });
    }

    public default ConfigDef<A> need(String key) {
        return sources -> get(key).run(sources).flatMap(o -> {
            if (o.isEmpty())
                return new Either.Failure<>(Set.of(ErrorMsg.missingKey(key)));
            else
                return new Either.Success<>(o.get());
        });
    }

    public default <B> ConfigDef<Consumer<B>> getOptionalAndSet(String key, BiConsumer<B, Optional<A>> f) {
        return sources -> get(key).run(sources).map(o -> b -> f.accept(b, o));
    }

    public default <B> ConfigDef<Consumer<B>> getAndSet(String key, BiConsumer<B, A> f) {
        return getOptionalAndSet(key, (b, o) -> {
            if (o.isPresent())
                f.accept(b, o.get());
        });
    }

    public default <B> ConfigDef<Consumer<B>> getOrUseAndSet(String key, A defaultValue, BiConsumer<B, A> f) {
        return getOptionalAndSet(key, (b, o) -> f.accept(b, o.orElseGet(() -> defaultValue)));
    }

    public default <B> ConfigDef<Consumer<B>> getOrParseAndSet(String key, String defaultValue, BiConsumer<B, A> f) {
        return sources -> getOrParse(key, defaultValue).run(sources).map(a -> b -> f.accept(b, a));
    }

    public default <B> ConfigDef<Consumer<B>> needAndSet(String key, BiConsumer<B, A> f) {
        return sources -> need(key).run(sources).map(a -> b -> f.accept(b, a));
    }
}
