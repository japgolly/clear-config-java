package japgolly.clearconfig;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import japgolly.clearconfig.util.*;

public interface ConfigParser<A> {
    public default Either<ErrorMsg, A> parse(String s) {
        try {
            return parseOrThrow(s);
        } catch (Throwable e) {
            return new Either.Failure<>(ErrorMsg.parsingError(e));
        }
    }

    public Either<ErrorMsg, A> parseOrThrow(String s);

    public default <B> ConfigParser<B> map(Function<? super A, ? extends B> f) {
        return flatMap(a -> new Either.Success<>(f.apply(a)));
    }

    public default <B> ConfigParser<B> mapToNonNull(Function<? super A, ? extends B> f, ErrorMsg errorMsg) {
        return flatMap(a -> {
            var b = f.apply(a);
            if (b == null)
                return new Either.Failure<>(errorMsg);
            else
                return new Either.Success<>(b);
        });
    }

    public default <B> ConfigParser<B> mapToNonEmpty(Function<? super A, ? extends Optional<B>> f, ErrorMsg errorMsg) {
        return flatMap(a -> {
            var o = f.apply(a);
            if (o.isEmpty())
                return new Either.Failure<>(errorMsg);
            else
                return new Either.Success<>(o.get());
        });
    }

    public default <B> ConfigParser<B> flatMap(Function<? super A, Either<ErrorMsg, B>> f) {
        return s -> parse(s).flatMap(f);
    }

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

    // ================================================================================================================

    public static <A> ConfigParser<A> constPass(A a) {
        return constEither(new Either.Success<>(a));
    }

    public static <A> ConfigParser<A> constFail(ErrorMsg e) {
        return constEither(new Either.Failure<>(e));
    }

    public static <A> ConfigParser<A> constEither(Either<ErrorMsg, A> e) {
        return s -> e;
    }

    // ================================================================================================================

    public static final ConfigParser<String> String =
        s -> new Either.Success<>(s.replaceFirst("#.*", "").trim());

    public static final ConfigParser<String> StringRaw =
        s -> new Either.Success<>(s);

    public static final ConfigParser<Integer> Integer =
        String.map(java.lang.Integer::parseInt);

    public static final ConfigParser<Long> Long =
        String.map(java.lang.Long::parseLong);

    public static final ConfigParser<Double> Double =
        String.map(java.lang.Double::parseDouble);

    public static final ConfigParser<Float> Float =
        String.map(java.lang.Float::parseFloat);

    public static final ConfigParser<Short> Short =
        String.map(java.lang.Short::parseShort);

    public static final ConfigParser<Boolean> Boolean =
        String.flatMap(s -> {
            if (Internals.REGEX_TRUE.matcher(s).matches())
                return new Either.Success<>(true);
            else if (Internals.REGEX_FALSE.matcher(s).matches())
                return new Either.Success<>(false);
            else
                return new Either.Failure<>(new ErrorMsg("Invalid boolean"));
        });

    public static final ConfigParser<java.net.InetAddress> InetAddress =
        String.flatMap(s -> {
            try {
                return new Either.Success<>(java.net.InetAddress.getByName(s));
            } catch (Exception e) {
                return new Either.Failure<>(new ErrorMsg("Invalid InetAddress"));
            }
        });

    public static final ConfigParser<java.util.UUID> UUID =
        String.map(java.util.UUID::fromString);

    public static final ConfigParser<ChronoUnit> ChronoUnit =
        String.mapToNonNull(
            s -> Internals.textToChronoUnitMap().get(s.toLowerCase()),
            new ErrorMsg("Invalid ChronoUnit"));

    public static final ConfigParser<Duration> Duration =
        String.mapToNonNull(
            s -> Internals.parseDuration(s.toLowerCase()),
            new ErrorMsg("Invalid Duration"));
}
