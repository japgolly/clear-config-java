package japgolly.clearconfig.util;

@FunctionalInterface
public interface Function4<A, B, C, D, Z> {
    public Z apply(A a, B b, C c, D d);
}
