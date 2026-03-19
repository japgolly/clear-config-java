package japgolly.clearconfig;

import static org.junit.Assert.assertEquals;
import java.util.Map;
import org.junit.Test;

public class ConfigSourceTest {

    @Test
    public void testMapKeyQueries() {
        var source = ConfigSource.ofMap("test", Map.of("BLAH.PORT", "1234"));
        var mapped = source.mapKeyQueries(s -> s.toUpperCase());
        assertEquals("1234", mapped.get("blah.port"));
        assertEquals(null, mapped.get("other"));
    }
}
