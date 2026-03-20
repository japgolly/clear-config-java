package japgolly.clearconfig;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import japgolly.clearconfig.util.*;

public interface ConfigDef<A> {

    public Either<Set<ErrorMsg>, A> run(ConfigSources sources);

    public default ConfigDef<A> mapKeys(Function<String, String> f) {
        return sources -> {
            List<ConfigSource> mappedSources = sources.sources().stream()
                .map(s -> s.mapKeyQueries(f))
                .toList();
            return run(new ConfigSources(mappedSources));
        };
    }

    public default ConfigDef<A> withKeyPrefix(String prefix) {
        return mapKeys(s -> prefix + s);
    }

    public default A runOrThrow(ConfigSources sources) throws UnsatisfiedConfigException {
        return switch (run(sources)) {
            case Either.Success<Set<ErrorMsg>, A> s ->
                s.value();
            case Either.Failure<Set<ErrorMsg>, A> f ->
                throw new UnsatisfiedConfigException(f.failure());
        };
    }

    // =================================================================================================================

    public static final ConfigValueParser<String> string =
        s -> new Either.Success<>(s.replaceFirst("#.*", "").trim());

    public static final ConfigValueParser<String> stringRaw =
        s -> new Either.Success<>(s);

    public static final ConfigValueParser<Integer> integer =
        string.flatMap(s -> {
            try {
                return new Either.Success<>(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                return new Either.Failure<>(new ErrorMsg("Invalid integer: " + s));
            }
        });

    public static final ConfigValueParser<InetAddress> inetAddress =
        string.flatMap(s -> {
            try {
                return new Either.Success<>(InetAddress.getByName(s));
            } catch (Exception e) {
                return new Either.Failure<>(new ErrorMsg("Invalid InetAddress: " + s));
            }
        });

    // =================================================================================================================

    @SafeVarargs
    public static <A> ConfigDef<Consumer<A>> consume(ConfigDef<Consumer<A>>... fns) {
        return sources -> {
            final Set<ErrorMsg> errors = new HashSet<>();
            final var consumers = new ArrayList<Consumer<A>>(fns.length);
            for (var fn : fns) {
                var res = fn.run(sources);
                res.foreachFailure(errors::addAll);
                if (res instanceof Either.Success<Set<ErrorMsg>, Consumer<A>> s) {
                    consumers.add(s.value());
                }
            }
            if (errors.isEmpty()) {
                return new Either.Success<>(a -> {
                    for (var c : consumers) {
                        c.accept(a);
                    }
                });
            } else {
                return new Either.Failure<>(errors);
            }
        };
    }

    // =================================================================================================================

    public static <A, Z> ConfigDef<Z> apply1(ConfigDef<A> ca, Function<A, Z> f) {
        return sources -> ca.run(sources).map(f);
    }

    public static <A, B, Z> ConfigDef<Z> apply2(ConfigDef<A> ca, ConfigDef<B> cb, BiFunction<A, B, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
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
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
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
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, Z> ConfigDef<Z> apply5(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, Function5<A, B, C, D, E, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, Z> ConfigDef<Z> apply6(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, Function6<A, B, C, D, E, F, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, Z> ConfigDef<Z> apply7(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, Function7<A, B, C, D, E, F, G, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, Z> ConfigDef<Z> apply8(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, Function8<A, B, C, D, E, F, G, H, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, Z> ConfigDef<Z> apply9(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, Function9<A, B, C, D, E, F, G, H, I, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, Z> ConfigDef<Z> apply10(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, Function10<A, B, C, D, E, F, G, H, I, J, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, Z> ConfigDef<Z> apply11(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, Function11<A, B, C, D, E, F, G, H, I, J, K, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, Z> ConfigDef<Z> apply12(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, Function12<A, B, C, D, E, F, G, H, I, J, K, L, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, Z> ConfigDef<Z> apply13(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, Function13<A, B, C, D, E, F, G, H, I, J, K, L, M, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, Z> ConfigDef<Z> apply14(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, Function14<A, B, C, D, E, F, G, H, I, J, K, L, M, N, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, Z> ConfigDef<Z> apply15(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, ConfigDef<O> co, Function15<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            var eo = co.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            eo.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow(), eo.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Z> ConfigDef<Z> apply16(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, ConfigDef<O> co, ConfigDef<P> cp, Function16<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            var eo = co.run(sources);
            var ep = cp.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            eo.foreachFailure(errs -> errors.addAll(errs));
            ep.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow(), eo.getOrThrow(), ep.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, Z> ConfigDef<Z> apply17(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, ConfigDef<O> co, ConfigDef<P> cp, ConfigDef<Q> cq, Function17<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            var eo = co.run(sources);
            var ep = cp.run(sources);
            var eq = cq.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            eo.foreachFailure(errs -> errors.addAll(errs));
            ep.foreachFailure(errs -> errors.addAll(errs));
            eq.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow(), eo.getOrThrow(), ep.getOrThrow(), eq.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, Z> ConfigDef<Z> apply18(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, ConfigDef<O> co, ConfigDef<P> cp, ConfigDef<Q> cq, ConfigDef<R> cr, Function18<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            var eo = co.run(sources);
            var ep = cp.run(sources);
            var eq = cq.run(sources);
            var er = cr.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            eo.foreachFailure(errs -> errors.addAll(errs));
            ep.foreachFailure(errs -> errors.addAll(errs));
            eq.foreachFailure(errs -> errors.addAll(errs));
            er.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow(), eo.getOrThrow(), ep.getOrThrow(), eq.getOrThrow(), er.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, Z> ConfigDef<Z> apply19(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, ConfigDef<O> co, ConfigDef<P> cp, ConfigDef<Q> cq, ConfigDef<R> cr, ConfigDef<S> cs, Function19<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            var eo = co.run(sources);
            var ep = cp.run(sources);
            var eq = cq.run(sources);
            var er = cr.run(sources);
            var es = cs.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            eo.foreachFailure(errs -> errors.addAll(errs));
            ep.foreachFailure(errs -> errors.addAll(errs));
            eq.foreachFailure(errs -> errors.addAll(errs));
            er.foreachFailure(errs -> errors.addAll(errs));
            es.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow(), eo.getOrThrow(), ep.getOrThrow(), eq.getOrThrow(), er.getOrThrow(), es.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, Z> ConfigDef<Z> apply20(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, ConfigDef<O> co, ConfigDef<P> cp, ConfigDef<Q> cq, ConfigDef<R> cr, ConfigDef<S> cs, ConfigDef<T> ct, Function20<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            var eo = co.run(sources);
            var ep = cp.run(sources);
            var eq = cq.run(sources);
            var er = cr.run(sources);
            var es = cs.run(sources);
            var et = ct.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            eo.foreachFailure(errs -> errors.addAll(errs));
            ep.foreachFailure(errs -> errors.addAll(errs));
            eq.foreachFailure(errs -> errors.addAll(errs));
            er.foreachFailure(errs -> errors.addAll(errs));
            es.foreachFailure(errs -> errors.addAll(errs));
            et.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow(), eo.getOrThrow(), ep.getOrThrow(), eq.getOrThrow(), er.getOrThrow(), es.getOrThrow(), et.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, Z> ConfigDef<Z> apply21(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, ConfigDef<O> co, ConfigDef<P> cp, ConfigDef<Q> cq, ConfigDef<R> cr, ConfigDef<S> cs, ConfigDef<T> ct, ConfigDef<U> cu, Function21<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            var eo = co.run(sources);
            var ep = cp.run(sources);
            var eq = cq.run(sources);
            var er = cr.run(sources);
            var es = cs.run(sources);
            var et = ct.run(sources);
            var eu = cu.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            eo.foreachFailure(errs -> errors.addAll(errs));
            ep.foreachFailure(errs -> errors.addAll(errs));
            eq.foreachFailure(errs -> errors.addAll(errs));
            er.foreachFailure(errs -> errors.addAll(errs));
            es.foreachFailure(errs -> errors.addAll(errs));
            et.foreachFailure(errs -> errors.addAll(errs));
            eu.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow(), eo.getOrThrow(), ep.getOrThrow(), eq.getOrThrow(), er.getOrThrow(), es.getOrThrow(), et.getOrThrow(), eu.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, Z> ConfigDef<Z> apply22(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, ConfigDef<O> co, ConfigDef<P> cp, ConfigDef<Q> cq, ConfigDef<R> cr, ConfigDef<S> cs, ConfigDef<T> ct, ConfigDef<U> cu, ConfigDef<V> cv, Function22<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            var eo = co.run(sources);
            var ep = cp.run(sources);
            var eq = cq.run(sources);
            var er = cr.run(sources);
            var es = cs.run(sources);
            var et = ct.run(sources);
            var eu = cu.run(sources);
            var ev = cv.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            eo.foreachFailure(errs -> errors.addAll(errs));
            ep.foreachFailure(errs -> errors.addAll(errs));
            eq.foreachFailure(errs -> errors.addAll(errs));
            er.foreachFailure(errs -> errors.addAll(errs));
            es.foreachFailure(errs -> errors.addAll(errs));
            et.foreachFailure(errs -> errors.addAll(errs));
            eu.foreachFailure(errs -> errors.addAll(errs));
            ev.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow(), eo.getOrThrow(), ep.getOrThrow(), eq.getOrThrow(), er.getOrThrow(), es.getOrThrow(), et.getOrThrow(), eu.getOrThrow(), ev.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, Z> ConfigDef<Z> apply23(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, ConfigDef<O> co, ConfigDef<P> cp, ConfigDef<Q> cq, ConfigDef<R> cr, ConfigDef<S> cs, ConfigDef<T> ct, ConfigDef<U> cu, ConfigDef<V> cv, ConfigDef<W> cw, Function23<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            var eo = co.run(sources);
            var ep = cp.run(sources);
            var eq = cq.run(sources);
            var er = cr.run(sources);
            var es = cs.run(sources);
            var et = ct.run(sources);
            var eu = cu.run(sources);
            var ev = cv.run(sources);
            var ew = cw.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            eo.foreachFailure(errs -> errors.addAll(errs));
            ep.foreachFailure(errs -> errors.addAll(errs));
            eq.foreachFailure(errs -> errors.addAll(errs));
            er.foreachFailure(errs -> errors.addAll(errs));
            es.foreachFailure(errs -> errors.addAll(errs));
            et.foreachFailure(errs -> errors.addAll(errs));
            eu.foreachFailure(errs -> errors.addAll(errs));
            ev.foreachFailure(errs -> errors.addAll(errs));
            ew.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow(), eo.getOrThrow(), ep.getOrThrow(), eq.getOrThrow(), er.getOrThrow(), es.getOrThrow(), et.getOrThrow(), eu.getOrThrow(), ev.getOrThrow(), ew.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Z> ConfigDef<Z> apply24(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, ConfigDef<O> co, ConfigDef<P> cp, ConfigDef<Q> cq, ConfigDef<R> cr, ConfigDef<S> cs, ConfigDef<T> ct, ConfigDef<U> cu, ConfigDef<V> cv, ConfigDef<W> cw, ConfigDef<X> cx, Function24<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            var eo = co.run(sources);
            var ep = cp.run(sources);
            var eq = cq.run(sources);
            var er = cr.run(sources);
            var es = cs.run(sources);
            var et = ct.run(sources);
            var eu = cu.run(sources);
            var ev = cv.run(sources);
            var ew = cw.run(sources);
            var ex = cx.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            eo.foreachFailure(errs -> errors.addAll(errs));
            ep.foreachFailure(errs -> errors.addAll(errs));
            eq.foreachFailure(errs -> errors.addAll(errs));
            er.foreachFailure(errs -> errors.addAll(errs));
            es.foreachFailure(errs -> errors.addAll(errs));
            et.foreachFailure(errs -> errors.addAll(errs));
            eu.foreachFailure(errs -> errors.addAll(errs));
            ev.foreachFailure(errs -> errors.addAll(errs));
            ew.foreachFailure(errs -> errors.addAll(errs));
            ex.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow(), eo.getOrThrow(), ep.getOrThrow(), eq.getOrThrow(), er.getOrThrow(), es.getOrThrow(), et.getOrThrow(), eu.getOrThrow(), ev.getOrThrow(), ew.getOrThrow(), ex.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z> ConfigDef<Z> apply25(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, ConfigDef<O> co, ConfigDef<P> cp, ConfigDef<Q> cq, ConfigDef<R> cr, ConfigDef<S> cs, ConfigDef<T> ct, ConfigDef<U> cu, ConfigDef<V> cv, ConfigDef<W> cw, ConfigDef<X> cx, ConfigDef<Y> cy, Function25<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            var eo = co.run(sources);
            var ep = cp.run(sources);
            var eq = cq.run(sources);
            var er = cr.run(sources);
            var es = cs.run(sources);
            var et = ct.run(sources);
            var eu = cu.run(sources);
            var ev = cv.run(sources);
            var ew = cw.run(sources);
            var ex = cx.run(sources);
            var ey = cy.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            eo.foreachFailure(errs -> errors.addAll(errs));
            ep.foreachFailure(errs -> errors.addAll(errs));
            eq.foreachFailure(errs -> errors.addAll(errs));
            er.foreachFailure(errs -> errors.addAll(errs));
            es.foreachFailure(errs -> errors.addAll(errs));
            et.foreachFailure(errs -> errors.addAll(errs));
            eu.foreachFailure(errs -> errors.addAll(errs));
            ev.foreachFailure(errs -> errors.addAll(errs));
            ew.foreachFailure(errs -> errors.addAll(errs));
            ex.foreachFailure(errs -> errors.addAll(errs));
            ey.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow(), eo.getOrThrow(), ep.getOrThrow(), eq.getOrThrow(), er.getOrThrow(), es.getOrThrow(), et.getOrThrow(), eu.getOrThrow(), ev.getOrThrow(), ew.getOrThrow(), ex.getOrThrow(), ey.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, A2, Z> ConfigDef<Z> apply26(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, ConfigDef<O> co, ConfigDef<P> cp, ConfigDef<Q> cq, ConfigDef<R> cr, ConfigDef<S> cs, ConfigDef<T> ct, ConfigDef<U> cu, ConfigDef<V> cv, ConfigDef<W> cw, ConfigDef<X> cx, ConfigDef<Y> cy, ConfigDef<A2> ca2, Function26<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, A2, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            var eo = co.run(sources);
            var ep = cp.run(sources);
            var eq = cq.run(sources);
            var er = cr.run(sources);
            var es = cs.run(sources);
            var et = ct.run(sources);
            var eu = cu.run(sources);
            var ev = cv.run(sources);
            var ew = cw.run(sources);
            var ex = cx.run(sources);
            var ey = cy.run(sources);
            var ea2 = ca2.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            eo.foreachFailure(errs -> errors.addAll(errs));
            ep.foreachFailure(errs -> errors.addAll(errs));
            eq.foreachFailure(errs -> errors.addAll(errs));
            er.foreachFailure(errs -> errors.addAll(errs));
            es.foreachFailure(errs -> errors.addAll(errs));
            et.foreachFailure(errs -> errors.addAll(errs));
            eu.foreachFailure(errs -> errors.addAll(errs));
            ev.foreachFailure(errs -> errors.addAll(errs));
            ew.foreachFailure(errs -> errors.addAll(errs));
            ex.foreachFailure(errs -> errors.addAll(errs));
            ey.foreachFailure(errs -> errors.addAll(errs));
            ea2.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow(), eo.getOrThrow(), ep.getOrThrow(), eq.getOrThrow(), er.getOrThrow(), es.getOrThrow(), et.getOrThrow(), eu.getOrThrow(), ev.getOrThrow(), ew.getOrThrow(), ex.getOrThrow(), ey.getOrThrow(), ea2.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, A2, B2, Z> ConfigDef<Z> apply27(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, ConfigDef<O> co, ConfigDef<P> cp, ConfigDef<Q> cq, ConfigDef<R> cr, ConfigDef<S> cs, ConfigDef<T> ct, ConfigDef<U> cu, ConfigDef<V> cv, ConfigDef<W> cw, ConfigDef<X> cx, ConfigDef<Y> cy, ConfigDef<A2> ca2, ConfigDef<B2> cb2, Function27<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, A2, B2, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            var eo = co.run(sources);
            var ep = cp.run(sources);
            var eq = cq.run(sources);
            var er = cr.run(sources);
            var es = cs.run(sources);
            var et = ct.run(sources);
            var eu = cu.run(sources);
            var ev = cv.run(sources);
            var ew = cw.run(sources);
            var ex = cx.run(sources);
            var ey = cy.run(sources);
            var ea2 = ca2.run(sources);
            var eb2 = cb2.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            eo.foreachFailure(errs -> errors.addAll(errs));
            ep.foreachFailure(errs -> errors.addAll(errs));
            eq.foreachFailure(errs -> errors.addAll(errs));
            er.foreachFailure(errs -> errors.addAll(errs));
            es.foreachFailure(errs -> errors.addAll(errs));
            et.foreachFailure(errs -> errors.addAll(errs));
            eu.foreachFailure(errs -> errors.addAll(errs));
            ev.foreachFailure(errs -> errors.addAll(errs));
            ew.foreachFailure(errs -> errors.addAll(errs));
            ex.foreachFailure(errs -> errors.addAll(errs));
            ey.foreachFailure(errs -> errors.addAll(errs));
            ea2.foreachFailure(errs -> errors.addAll(errs));
            eb2.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow(), eo.getOrThrow(), ep.getOrThrow(), eq.getOrThrow(), er.getOrThrow(), es.getOrThrow(), et.getOrThrow(), eu.getOrThrow(), ev.getOrThrow(), ew.getOrThrow(), ex.getOrThrow(), ey.getOrThrow(), ea2.getOrThrow(), eb2.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, A2, B2, C2, Z> ConfigDef<Z> apply28(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, ConfigDef<O> co, ConfigDef<P> cp, ConfigDef<Q> cq, ConfigDef<R> cr, ConfigDef<S> cs, ConfigDef<T> ct, ConfigDef<U> cu, ConfigDef<V> cv, ConfigDef<W> cw, ConfigDef<X> cx, ConfigDef<Y> cy, ConfigDef<A2> ca2, ConfigDef<B2> cb2, ConfigDef<C2> cc2, Function28<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, A2, B2, C2, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            var eo = co.run(sources);
            var ep = cp.run(sources);
            var eq = cq.run(sources);
            var er = cr.run(sources);
            var es = cs.run(sources);
            var et = ct.run(sources);
            var eu = cu.run(sources);
            var ev = cv.run(sources);
            var ew = cw.run(sources);
            var ex = cx.run(sources);
            var ey = cy.run(sources);
            var ea2 = ca2.run(sources);
            var eb2 = cb2.run(sources);
            var ec2 = cc2.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            eo.foreachFailure(errs -> errors.addAll(errs));
            ep.foreachFailure(errs -> errors.addAll(errs));
            eq.foreachFailure(errs -> errors.addAll(errs));
            er.foreachFailure(errs -> errors.addAll(errs));
            es.foreachFailure(errs -> errors.addAll(errs));
            et.foreachFailure(errs -> errors.addAll(errs));
            eu.foreachFailure(errs -> errors.addAll(errs));
            ev.foreachFailure(errs -> errors.addAll(errs));
            ew.foreachFailure(errs -> errors.addAll(errs));
            ex.foreachFailure(errs -> errors.addAll(errs));
            ey.foreachFailure(errs -> errors.addAll(errs));
            ea2.foreachFailure(errs -> errors.addAll(errs));
            eb2.foreachFailure(errs -> errors.addAll(errs));
            ec2.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow(), eo.getOrThrow(), ep.getOrThrow(), eq.getOrThrow(), er.getOrThrow(), es.getOrThrow(), et.getOrThrow(), eu.getOrThrow(), ev.getOrThrow(), ew.getOrThrow(), ex.getOrThrow(), ey.getOrThrow(), ea2.getOrThrow(), eb2.getOrThrow(), ec2.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, A2, B2, C2, D2, Z> ConfigDef<Z> apply29(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, ConfigDef<O> co, ConfigDef<P> cp, ConfigDef<Q> cq, ConfigDef<R> cr, ConfigDef<S> cs, ConfigDef<T> ct, ConfigDef<U> cu, ConfigDef<V> cv, ConfigDef<W> cw, ConfigDef<X> cx, ConfigDef<Y> cy, ConfigDef<A2> ca2, ConfigDef<B2> cb2, ConfigDef<C2> cc2, ConfigDef<D2> cd2, Function29<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, A2, B2, C2, D2, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            var eo = co.run(sources);
            var ep = cp.run(sources);
            var eq = cq.run(sources);
            var er = cr.run(sources);
            var es = cs.run(sources);
            var et = ct.run(sources);
            var eu = cu.run(sources);
            var ev = cv.run(sources);
            var ew = cw.run(sources);
            var ex = cx.run(sources);
            var ey = cy.run(sources);
            var ea2 = ca2.run(sources);
            var eb2 = cb2.run(sources);
            var ec2 = cc2.run(sources);
            var ed2 = cd2.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            eo.foreachFailure(errs -> errors.addAll(errs));
            ep.foreachFailure(errs -> errors.addAll(errs));
            eq.foreachFailure(errs -> errors.addAll(errs));
            er.foreachFailure(errs -> errors.addAll(errs));
            es.foreachFailure(errs -> errors.addAll(errs));
            et.foreachFailure(errs -> errors.addAll(errs));
            eu.foreachFailure(errs -> errors.addAll(errs));
            ev.foreachFailure(errs -> errors.addAll(errs));
            ew.foreachFailure(errs -> errors.addAll(errs));
            ex.foreachFailure(errs -> errors.addAll(errs));
            ey.foreachFailure(errs -> errors.addAll(errs));
            ea2.foreachFailure(errs -> errors.addAll(errs));
            eb2.foreachFailure(errs -> errors.addAll(errs));
            ec2.foreachFailure(errs -> errors.addAll(errs));
            ed2.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow(), eo.getOrThrow(), ep.getOrThrow(), eq.getOrThrow(), er.getOrThrow(), es.getOrThrow(), et.getOrThrow(), eu.getOrThrow(), ev.getOrThrow(), ew.getOrThrow(), ex.getOrThrow(), ey.getOrThrow(), ea2.getOrThrow(), eb2.getOrThrow(), ec2.getOrThrow(), ed2.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, A2, B2, C2, D2, E2, Z> ConfigDef<Z> apply30(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, ConfigDef<O> co, ConfigDef<P> cp, ConfigDef<Q> cq, ConfigDef<R> cr, ConfigDef<S> cs, ConfigDef<T> ct, ConfigDef<U> cu, ConfigDef<V> cv, ConfigDef<W> cw, ConfigDef<X> cx, ConfigDef<Y> cy, ConfigDef<A2> ca2, ConfigDef<B2> cb2, ConfigDef<C2> cc2, ConfigDef<D2> cd2, ConfigDef<E2> ce2, Function30<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, A2, B2, C2, D2, E2, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            var eo = co.run(sources);
            var ep = cp.run(sources);
            var eq = cq.run(sources);
            var er = cr.run(sources);
            var es = cs.run(sources);
            var et = ct.run(sources);
            var eu = cu.run(sources);
            var ev = cv.run(sources);
            var ew = cw.run(sources);
            var ex = cx.run(sources);
            var ey = cy.run(sources);
            var ea2 = ca2.run(sources);
            var eb2 = cb2.run(sources);
            var ec2 = cc2.run(sources);
            var ed2 = cd2.run(sources);
            var ee2 = ce2.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            eo.foreachFailure(errs -> errors.addAll(errs));
            ep.foreachFailure(errs -> errors.addAll(errs));
            eq.foreachFailure(errs -> errors.addAll(errs));
            er.foreachFailure(errs -> errors.addAll(errs));
            es.foreachFailure(errs -> errors.addAll(errs));
            et.foreachFailure(errs -> errors.addAll(errs));
            eu.foreachFailure(errs -> errors.addAll(errs));
            ev.foreachFailure(errs -> errors.addAll(errs));
            ew.foreachFailure(errs -> errors.addAll(errs));
            ex.foreachFailure(errs -> errors.addAll(errs));
            ey.foreachFailure(errs -> errors.addAll(errs));
            ea2.foreachFailure(errs -> errors.addAll(errs));
            eb2.foreachFailure(errs -> errors.addAll(errs));
            ec2.foreachFailure(errs -> errors.addAll(errs));
            ed2.foreachFailure(errs -> errors.addAll(errs));
            ee2.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow(), eo.getOrThrow(), ep.getOrThrow(), eq.getOrThrow(), er.getOrThrow(), es.getOrThrow(), et.getOrThrow(), eu.getOrThrow(), ev.getOrThrow(), ew.getOrThrow(), ex.getOrThrow(), ey.getOrThrow(), ea2.getOrThrow(), eb2.getOrThrow(), ec2.getOrThrow(), ed2.getOrThrow(), ee2.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, A2, B2, C2, D2, E2, F2, Z> ConfigDef<Z> apply31(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, ConfigDef<O> co, ConfigDef<P> cp, ConfigDef<Q> cq, ConfigDef<R> cr, ConfigDef<S> cs, ConfigDef<T> ct, ConfigDef<U> cu, ConfigDef<V> cv, ConfigDef<W> cw, ConfigDef<X> cx, ConfigDef<Y> cy, ConfigDef<A2> ca2, ConfigDef<B2> cb2, ConfigDef<C2> cc2, ConfigDef<D2> cd2, ConfigDef<E2> ce2, ConfigDef<F2> cf2, Function31<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, A2, B2, C2, D2, E2, F2, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            var eo = co.run(sources);
            var ep = cp.run(sources);
            var eq = cq.run(sources);
            var er = cr.run(sources);
            var es = cs.run(sources);
            var et = ct.run(sources);
            var eu = cu.run(sources);
            var ev = cv.run(sources);
            var ew = cw.run(sources);
            var ex = cx.run(sources);
            var ey = cy.run(sources);
            var ea2 = ca2.run(sources);
            var eb2 = cb2.run(sources);
            var ec2 = cc2.run(sources);
            var ed2 = cd2.run(sources);
            var ee2 = ce2.run(sources);
            var ef2 = cf2.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            eo.foreachFailure(errs -> errors.addAll(errs));
            ep.foreachFailure(errs -> errors.addAll(errs));
            eq.foreachFailure(errs -> errors.addAll(errs));
            er.foreachFailure(errs -> errors.addAll(errs));
            es.foreachFailure(errs -> errors.addAll(errs));
            et.foreachFailure(errs -> errors.addAll(errs));
            eu.foreachFailure(errs -> errors.addAll(errs));
            ev.foreachFailure(errs -> errors.addAll(errs));
            ew.foreachFailure(errs -> errors.addAll(errs));
            ex.foreachFailure(errs -> errors.addAll(errs));
            ey.foreachFailure(errs -> errors.addAll(errs));
            ea2.foreachFailure(errs -> errors.addAll(errs));
            eb2.foreachFailure(errs -> errors.addAll(errs));
            ec2.foreachFailure(errs -> errors.addAll(errs));
            ed2.foreachFailure(errs -> errors.addAll(errs));
            ee2.foreachFailure(errs -> errors.addAll(errs));
            ef2.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow(), eo.getOrThrow(), ep.getOrThrow(), eq.getOrThrow(), er.getOrThrow(), es.getOrThrow(), et.getOrThrow(), eu.getOrThrow(), ev.getOrThrow(), ew.getOrThrow(), ex.getOrThrow(), ey.getOrThrow(), ea2.getOrThrow(), eb2.getOrThrow(), ec2.getOrThrow(), ed2.getOrThrow(), ee2.getOrThrow(), ef2.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }

    public static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, A2, B2, C2, D2, E2, F2, G2, Z> ConfigDef<Z> apply32(ConfigDef<A> ca, ConfigDef<B> cb, ConfigDef<C> cc, ConfigDef<D> cd, ConfigDef<E> ce, ConfigDef<F> cf, ConfigDef<G> cg, ConfigDef<H> ch, ConfigDef<I> ci, ConfigDef<J> cj, ConfigDef<K> ck, ConfigDef<L> cl, ConfigDef<M> cm, ConfigDef<N> cn, ConfigDef<O> co, ConfigDef<P> cp, ConfigDef<Q> cq, ConfigDef<R> cr, ConfigDef<S> cs, ConfigDef<T> ct, ConfigDef<U> cu, ConfigDef<V> cv, ConfigDef<W> cw, ConfigDef<X> cx, ConfigDef<Y> cy, ConfigDef<A2> ca2, ConfigDef<B2> cb2, ConfigDef<C2> cc2, ConfigDef<D2> cd2, ConfigDef<E2> ce2, ConfigDef<F2> cf2, ConfigDef<G2> cg2, Function32<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, A2, B2, C2, D2, E2, F2, G2, Z> f) {
        return sources -> {
            Set<ErrorMsg> errors = new HashSet<>();
            var ea = ca.run(sources);
            var eb = cb.run(sources);
            var ec = cc.run(sources);
            var ed = cd.run(sources);
            var ee = ce.run(sources);
            var ef = cf.run(sources);
            var eg = cg.run(sources);
            var eh = ch.run(sources);
            var ei = ci.run(sources);
            var ej = cj.run(sources);
            var ek = ck.run(sources);
            var el = cl.run(sources);
            var em = cm.run(sources);
            var en = cn.run(sources);
            var eo = co.run(sources);
            var ep = cp.run(sources);
            var eq = cq.run(sources);
            var er = cr.run(sources);
            var es = cs.run(sources);
            var et = ct.run(sources);
            var eu = cu.run(sources);
            var ev = cv.run(sources);
            var ew = cw.run(sources);
            var ex = cx.run(sources);
            var ey = cy.run(sources);
            var ea2 = ca2.run(sources);
            var eb2 = cb2.run(sources);
            var ec2 = cc2.run(sources);
            var ed2 = cd2.run(sources);
            var ee2 = ce2.run(sources);
            var ef2 = cf2.run(sources);
            var eg2 = cg2.run(sources);
            ea.foreachFailure(errs -> errors.addAll(errs));
            eb.foreachFailure(errs -> errors.addAll(errs));
            ec.foreachFailure(errs -> errors.addAll(errs));
            ed.foreachFailure(errs -> errors.addAll(errs));
            ee.foreachFailure(errs -> errors.addAll(errs));
            ef.foreachFailure(errs -> errors.addAll(errs));
            eg.foreachFailure(errs -> errors.addAll(errs));
            eh.foreachFailure(errs -> errors.addAll(errs));
            ei.foreachFailure(errs -> errors.addAll(errs));
            ej.foreachFailure(errs -> errors.addAll(errs));
            ek.foreachFailure(errs -> errors.addAll(errs));
            el.foreachFailure(errs -> errors.addAll(errs));
            em.foreachFailure(errs -> errors.addAll(errs));
            en.foreachFailure(errs -> errors.addAll(errs));
            eo.foreachFailure(errs -> errors.addAll(errs));
            ep.foreachFailure(errs -> errors.addAll(errs));
            eq.foreachFailure(errs -> errors.addAll(errs));
            er.foreachFailure(errs -> errors.addAll(errs));
            es.foreachFailure(errs -> errors.addAll(errs));
            et.foreachFailure(errs -> errors.addAll(errs));
            eu.foreachFailure(errs -> errors.addAll(errs));
            ev.foreachFailure(errs -> errors.addAll(errs));
            ew.foreachFailure(errs -> errors.addAll(errs));
            ex.foreachFailure(errs -> errors.addAll(errs));
            ey.foreachFailure(errs -> errors.addAll(errs));
            ea2.foreachFailure(errs -> errors.addAll(errs));
            eb2.foreachFailure(errs -> errors.addAll(errs));
            ec2.foreachFailure(errs -> errors.addAll(errs));
            ed2.foreachFailure(errs -> errors.addAll(errs));
            ee2.foreachFailure(errs -> errors.addAll(errs));
            ef2.foreachFailure(errs -> errors.addAll(errs));
            eg2.foreachFailure(errs -> errors.addAll(errs));
            if (errors.isEmpty())
                return new Either.Success<>(f.apply(ea.getOrThrow(), eb.getOrThrow(), ec.getOrThrow(), ed.getOrThrow(), ee.getOrThrow(), ef.getOrThrow(), eg.getOrThrow(), eh.getOrThrow(), ei.getOrThrow(), ej.getOrThrow(), ek.getOrThrow(), el.getOrThrow(), em.getOrThrow(), en.getOrThrow(), eo.getOrThrow(), ep.getOrThrow(), eq.getOrThrow(), er.getOrThrow(), es.getOrThrow(), et.getOrThrow(), eu.getOrThrow(), ev.getOrThrow(), ew.getOrThrow(), ex.getOrThrow(), ey.getOrThrow(), ea2.getOrThrow(), eb2.getOrThrow(), ec2.getOrThrow(), ed2.getOrThrow(), ee2.getOrThrow(), ef2.getOrThrow(), eg2.getOrThrow()));
            else
                return new Either.Failure<>(errors);
        };
    }
}
