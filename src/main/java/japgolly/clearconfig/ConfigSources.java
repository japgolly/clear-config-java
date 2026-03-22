package japgolly.clearconfig;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import japgolly.clearconfig.util.*;

public final class ConfigSources {
    private final List<ConfigSource> sources;

    public List<ConfigSource> sources() {
        return sources;
    }

    /** @param sources Highest priority first */
    public ConfigSources(List<ConfigSource> sources) {
        this.sources = Collections.unmodifiableList(sources);
    }

    public <A> Either<ErrorMsg, Optional<A>> get(String key, ConfigParser<A> parser) {
        for (ConfigSource s : sources()) {
            final var value = s.get(key);
            if (value != null) {
                try {
                    return parser.parse(value).map(a -> Optional.of(a));
                } catch (Throwable e) {
                    var err = ErrorMsg.uncaughtParsingError(key, value, e);
                    return new Either.Failure<>(err);
                }
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
}
