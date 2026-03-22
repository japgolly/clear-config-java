package japgolly.clearconfig.util;

@FunctionalInterface
public interface BiConsumer<A, B> {
   void accept(A a, B b);
}