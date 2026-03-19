package japgolly.clearconfig;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import japgolly.clearconfig.util.*;

public interface ConfigValueParser<A> {
    public Either<ErrorMsg, A> parse(String s);

    public default <B> ConfigValueParser<B> map(Function<? super A, ? extends B> f) {
        return s -> parse(s).map(f);
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

}
