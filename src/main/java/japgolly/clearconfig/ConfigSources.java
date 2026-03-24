package japgolly.clearconfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import japgolly.clearconfig.util.*;

public final class ConfigSources {
    final List<ConfigSource> sources;
    private final State state;
    private final Function<String, String> keyMapper;

    private static final class State {
        final Map<String, KeyCtx> seen = new HashMap<>();
        boolean inSecretBlock = false;
    }

    static final class KeyCtx {
        boolean secret = false;
        Optional<Object> defaultValue = Optional.empty();
    };

    Map<String, KeyCtx> seen() {
        return state.seen;
    }

    /** @param sources Highest priority first */
    public ConfigSources(List<ConfigSource> sources) {
        this(sources, new State(), Function.identity());
    }

    private ConfigSources(List<ConfigSource> sources, State state, Function<String, String> keyMapper) {
        this.sources = Collections.unmodifiableList(sources);
        this.state = state;
        this.keyMapper = keyMapper;
    }

    public ConfigSources map(Function<ConfigSource, ConfigSource> f) {
        return new ConfigSources(sources.stream().map(s -> f.apply(s)).toList(), state, keyMapper);
    }

    public <A> A secretly(Supplier<A> f) {
        final var prev = state.inSecretBlock;
        state.inSecretBlock = true;
        try {
            return f.get();
        } finally {
            state.inSecretBlock = prev;
        }
    }

    private static final Pattern IMPLICITLY_SECRET = Pattern.compile(".*(?:secret|password).*", Pattern.CASE_INSENSITIVE);

    public <A> Either<ErrorMsg, Optional<A>> get(String origKey, ConfigParser<A> parser, Optional<Object> defaultValue) {
        final var key = keyMapper.apply(origKey);

        // Register each key lookup
        var ctx = state.seen.get(key);
        if (ctx == null) {
            ctx = new KeyCtx();
            state.seen.put(key, ctx);
        } else {
            // TODO: Warning or error
        }

        // Mark as secret if necessary
        if (state.inSecretBlock || IMPLICITLY_SECRET.matcher(key).matches())
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
        return new ConfigSources(sources, state, keyMapper.compose(f));
    }

    public ConfigSources mapValues(Function<String, String> f) {
        final var newSources = sources.stream().map(s -> s.mapValues(f)).collect(Collectors.toList());
        return new ConfigSources(newSources, state, keyMapper);
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
