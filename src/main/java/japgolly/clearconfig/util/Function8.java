package japgolly.clearconfig.util;

@FunctionalInterface
public interface Function8<A, B, C, D, E, F, G, H, Z> {
    public Z apply(A a, B b, C c, D d, E e, F f, G g, H h);
}
