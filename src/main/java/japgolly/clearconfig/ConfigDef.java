package japgolly.clearconfig;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import japgolly.clearconfig.util.*;

public interface ConfigDef<A> {

    public Either<Set<ErrorMsg>, A> run(ConfigSources sources);

    public default ConfigDef<A> mapKeys(Function<String, String> f) {
        return sources -> {
            List<ConfigSource> mappedSources = sources.sources().stream()
                .map(s -> (ConfigSource) new ConfigSource() {
                    @Override public String name() { return s.name(); }
                    @Override public String get(String key) { return s.get(f.apply(key)); }
                })
                .toList();
            return run(new ConfigSources(mappedSources));
        };
    }

    public default ConfigDef<A> withKeyPrefix(String prefix) {
        return mapKeys(s -> prefix + s);
    }

    // =================================================================================================================

    public static ConfigValueParser<String> string =
        s -> new Either.Success<>(s.replaceFirst("#.*", "").trim());

    public static ConfigValueParser<String> stringRaw =
        s -> new Either.Success<>(s);

    public static ConfigValueParser<Integer> integer =
        string.flatMap(s -> {
            try {
                return new Either.Success<>(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                return new Either.Failure<>(new ErrorMsg("Invalid integer: " + s));
            }
        });

    public static ConfigValueParser<InetAddress> inetAddress =
        string.flatMap(s -> {
            try {
                return new Either.Success<>(InetAddress.getByName(s));
            } catch (Exception e) {
                return new Either.Failure<>(new ErrorMsg("Invalid InetAddress: " + s));
            }
        });

    // =================================================================================================================

    public static <A, Z> ConfigDef<Z> apply1(ConfigDef<A> ca, Function<A, Z> f) {
        return sources -> ca.run(sources).map(f);
    }

    public static <A, B, Z> ConfigDef<Z> apply2(ConfigDef<A> ca, ConfigDef<B> cb, BiFunction<A, B, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            ea.foreachFailure(es -> errors.addAll(es));
            eb.foreachFailure(es -> errors.addAll(es));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, Z> ConfigDef<Z> apply3(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, Function3<A, B, C, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            ea.foreachFailure(es -> errors.addAll(es));
            eb.foreachFailure(es -> errors.addAll(es));
            ec.foreachFailure(es -> errors.addAll(es));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, Z> ConfigDef<Z> apply4(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, Function4<A, B, C, D, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            ea.foreachFailure(es -> errors.addAll(es));
            eb.foreachFailure(es -> errors.addAll(es));
            ec.foreachFailure(es -> errors.addAll(es));
            ed.foreachFailure(es -> errors.addAll(es));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }
}
