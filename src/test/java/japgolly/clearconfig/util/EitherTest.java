package japgolly.clearconfig.util;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class EitherTest {

    @Test
    public void usage() {
        var step1 = new Either.Success<String, Integer>(1);
        var step2 = step1.map(n -> n.toString());
        assertEquals("1", step2.getOrThrow());
    }
}
