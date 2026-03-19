package japgolly.clearconfig;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import japgolly.clearconfig.util.*;

public final class ConfigSources {
    private final List<ConfigSource> _sources;

    public List<ConfigSource> sources() {
        return _sources;
    }

    public ConfigSources(List<ConfigSource> sources) {
        this._sources =  Collections.unmodifiableList(sources);
    }

    public <A> Either<ErrorMsg, Optional<A>> get(String key, ConfigValueParser<A> parser) {
        for (ConfigSource s : sources()) {
            final var value = s.get(key);
            if (value != null) {
                return parser.parse(value).map(a -> Optional.of(a));
            }
        }
        return new Either.Success<>(Optional.empty());
    }

    public static ConfigSources of() {
        return new ConfigSources(List.of());
    }

    public static ConfigSources of(ConfigSource s1) {
        return new ConfigSources(List.of(s1));
    }
}
