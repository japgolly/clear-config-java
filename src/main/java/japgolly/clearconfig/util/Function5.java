package japgolly.clearconfig.util;

@FunctionalInterface
public interface Function5<A, B, C, D, E, Z> {
    public Z apply(A a, B b, C c, D d, E e);
}
