package japgolly.clearconfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import japgolly.clearconfig.util.*;

public final class ConfigSources {
    final List<ConfigSource> sources;
    final Map<String, KeyCtx> seen = new HashMap<>();
    private boolean inSecretBlock = false;

    static final class KeyCtx {
        boolean secret = false;
        Optional<Object> defaultValue = Optional.empty();
    };

    /** @param sources Highest priority first */
    public ConfigSources(List<ConfigSource> sources) {
        this.sources = Collections.unmodifiableList(sources);
    }

    public ConfigSources map(Function<ConfigSource, ConfigSource> f) {
        return new ConfigSources(sources.stream().map(s -> f.apply(s)).toList());
    }

    public <A> A secretly(Supplier<A> f) {
        final var prev = inSecretBlock;
        inSecretBlock = true;
        try {
            return f.get();
        } finally {
            inSecretBlock = prev;
        }
    }

    private static final Pattern IMPLICITLY_SECRET = Pattern.compile(".*(?:secret|password).*", Pattern.CASE_INSENSITIVE);

    public <A> Either<ErrorMsg, Optional<A>> get(String key, ConfigParser<A> parser, Optional<Object> defaultValue) {

        // Register each key lookup
        var ctx = this.seen.get(key);
        if (ctx == null) {
            ctx = new KeyCtx();
            this.seen.put(key, ctx);
        } else {
            // TODO: Warning or error
        }

        // Mark as secret if necessary
        if (inSecretBlock || IMPLICITLY_SECRET.matcher(key).matches())
            ctx.secret = true;

        ctx.defaultValue = defaultValue;

        // Parse the first matching value
        for (ConfigSource src : sources) {
            final var value = src.get(key);
            if (value != null) {
                return switch (parser.parse(value)) {
                    case Either.Success<ErrorMsg, A> s ->
                        s.map(a -> Optional.of(a));
                    case Either.Failure<ErrorMsg, A> f -> {
                        var newErrMsg = f.failure().addKeyValueContext(key, value);
                        yield new Either.Failure<>(newErrMsg);
                    }
                };
            }
        }
        return new Either.Success<>(Optional.empty());
    }

    public ConfigSources mapKeyQueries(Function<String, String> f) {
        return new ConfigSources(sources.stream().map(s -> s.mapKeyQueries(f)).toList());
    }

    // =================================================================================================================

    public static ConfigSources of() {
        return new ConfigSources(List.of());
    }

    public static ConfigSources of(ConfigSource s1) {
        return new ConfigSources(List.of(s1));
    }

    /** Highest priority first */
    public static ConfigSources of(ConfigSource s1, ConfigSource s2) {
        return new ConfigSources(List.of(s1, s2));
    }

    /** Highest priority first */
    public static ConfigSources of(ConfigSource s1, ConfigSource s2, ConfigSource s3) {
        return new ConfigSources(List.of(s1, s2, s3));
    }

    /** Highest priority first */
    public static ConfigSources of(ConfigSource s1, ConfigSource s2, ConfigSource s3, ConfigSource s4) {
        return new ConfigSources(List.of(s1, s2, s3, s4));
    }

    /** Highest priority first */
    public static ConfigSources of(ConfigSource s1, ConfigSource s2, ConfigSource s3, ConfigSource s4, ConfigSource s5) {
        return new ConfigSources(List.of(s1, s2, s3, s4, s5));
    }

    /** Highest priority first */
    public static ConfigSources of(ConfigSource s1, ConfigSource s2, ConfigSource s3, ConfigSource s4, ConfigSource s5, ConfigSource s6) {
        return new ConfigSources(List.of(s1, s2, s3, s4, s5, s6));
    }

    /** Highest priority first */
    public static ConfigSources of(ConfigSource s1, ConfigSource s2, ConfigSource s3, ConfigSource s4, ConfigSource s5, ConfigSource s6, ConfigSource s7) {
        return new ConfigSources(List.of(s1, s2, s3, s4, s5, s6, s7));
    }

    /** Highest priority first */
    public static ConfigSources of(ConfigSource s1, ConfigSource s2, ConfigSource s3, ConfigSource s4, ConfigSource s5, ConfigSource s6, ConfigSource s7, ConfigSource s8) {
        return new ConfigSources(List.of(s1, s2, s3, s4, s5, s6, s7, s8));
    }
}
