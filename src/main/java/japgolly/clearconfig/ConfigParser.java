package japgolly.clearconfig;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

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

    public default ConfigParser<A> preprocess(Function<String, String> f) {
        return s -> parse(f.apply(s));
    }

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

    public default ConfigDef<Boolean> exists(String key) {
        return get(key).map(Optional::isPresent);
    }

    public default ConfigDef<Optional<A>> get(String key) {
        return sources -> sources.get(key, this, Optional.empty()).mapFailure(e -> Set.of(e));
    }

    public default ConfigDef<A> getOrUse(String key, A defaultValue) {
        return sources -> sources.get(key, this, Optional.of(defaultValue))
            .mapFailure(e -> Set.of(e))
            .map(o -> o.orElseGet(() -> defaultValue));
    }

    public default ConfigDef<A> getOrParse(String key, String defaultValue) {
        return sources -> sources.get(key, this, Optional.of(defaultValue))
            .mapFailure(e -> Set.of(e))
            .flatMap(o -> {
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
        return getOptionalAndSet(key, Optional.empty(), f);
    }

    public default <B> ConfigDef<Consumer<B>> getOptionalAndSet(String key, Optional<Object> defaultValue, BiConsumer<B, Optional<A>> f) {
        return sources -> get(key).run(sources).map(o -> b -> f.accept(b, o));
    }

    public default <B> ConfigDef<Consumer<B>> getAndSet(String key, BiConsumer<B, A> f) {
        return getOptionalAndSet(key, (b, o) -> {
            if (o.isPresent())
                f.accept(b, o.get());
        });
    }

    public default <B> ConfigDef<Consumer<B>> getOrUseAndSet(String key, A defaultValue, BiConsumer<B, A> f) {
        return getOptionalAndSet(key, Optional.of(defaultValue), (b, o) -> f.accept(b, o.orElseGet(() -> defaultValue)));
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

    public static <A extends Enum<A>> ConfigParser<A> Enum(Class<A> cls) {
        return String.flatMap(s -> {
            try {
                return new Either.Success<>(Enum.valueOf(cls, s));
            } catch (IllegalArgumentException e) {
                return new Either.Failure<>(new ErrorMsg("Invalid " + cls.getSimpleName()));
            }
        });
    }

    public static <A> ConfigParser<A> ofMap(Map<String, A> map) {
        return ofMap(map, new ErrorMsg("Invalid value"));
    }

    public static <A> ConfigParser<A> ofMap(Map<String, A> map, ErrorMsg errorMsg) {
        return String.flatMap(s -> {
            if (map.containsKey(s))
                return new Either.Success<>(map.get(s));
            else
                return new Either.Failure<>(errorMsg);
        });
    }

    // ================================================================================================================

    public static final ConfigParser<String> String =
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

    public static final ConfigParser<InetAddress> InetAddress =
        String.flatMap(s -> {
            try {
                return new Either.Success<>(java.net.InetAddress.getByName(s));
            } catch (Exception e) {
                return new Either.Failure<>(new ErrorMsg("Invalid InetAddress"));
            }
        });

    public static final ConfigParser<UUID> UUID =
        String.map(java.util.UUID::fromString);

    public static final ConfigParser<ChronoUnit> ChronoUnit =
        String.mapToNonNull(
            s -> Internals.textToChronoUnitMap().get(s.toLowerCase()),
            new ErrorMsg("Invalid ChronoUnit"));

    public static final ConfigParser<Duration> Duration =
        String.mapToNonNull(
            s -> Internals.parseDuration(s.toLowerCase()),
            new ErrorMsg("Invalid Duration"));

    public static final ConfigParser<Period> Period =
        String.map(java.time.Period::parse);

    public static final ConfigParser<OffsetDateTime> OffsetDateTime =
        String.map(java.time.OffsetDateTime::parse);

    public static final ConfigParser<ZonedDateTime> ZonedDateTime =
        String.map(java.time.ZonedDateTime::parse);

    public static final ConfigParser<LocalDateTime> LocalDateTime =
        String.map(java.time.LocalDateTime::parse);

    public static final ConfigParser<LocalDate> LocalDate =
        String.map(java.time.LocalDate::parse);

    public static final ConfigParser<LocalTime> LocalTime =
        String.map(java.time.LocalTime::parse);

    public static final ConfigParser<URI> URI =
        String.flatMap(s -> {
            try {
                return new Either.Success<>(new java.net.URI(s));
            } catch (Exception e) {
                return new Either.Failure<>(new ErrorMsg("Invalid URI"));
            }
        });

    public static final ConfigParser<URL> URL =
        URI.flatMap(u -> {
            try {
                return new Either.Success<>(u.toURL());
            } catch (Exception e) {
                return new Either.Failure<>(new ErrorMsg("Invalid URL"));
            }
        });

    public static final ConfigParser<File> File =
      String.map(File::new);

    public static final ConfigParser<Pattern> Pattern =
      String.map(java.util.regex.Pattern::compile);

}
