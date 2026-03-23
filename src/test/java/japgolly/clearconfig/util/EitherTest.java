package japgolly.clearconfig.util;

import static org.junit.Assert.assertEquals;
import java.util.function.Supplier;
import org.junit.Test;

public class EitherTest {

    @Test
    public void usage() {
        var step1 = new Either.Success<String, Integer>(1);
        var step2 = step1.map(n -> n.toString());
        assertEquals("1", step2.getOrThrow());
    }

    @Test
    public void orElseVariance() {
        Either<Object, Number> failure = new Either.Failure<>("err");
        Supplier<Either<String, Integer>> supplier = () -> new Either.Success<>(123);
        Either<Object, Number> res = failure.orElse(supplier);
        assertEquals(123, res.getOrThrow());
    }
}
