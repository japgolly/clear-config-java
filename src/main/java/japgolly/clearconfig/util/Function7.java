package japgolly.clearconfig.util;

@FunctionalInterface
public interface Function7<A, B, C, D, E, F, G, Z> {
    public Z apply(A a, B b, C c, D d, E e, F f, G g);
}
