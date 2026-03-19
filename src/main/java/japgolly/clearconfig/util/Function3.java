package japgolly.clearconfig.util;

@FunctionalInterface
public interface Function3<A, B, C, Z> {
    public Z apply(A a, B b, C c);
}
