package japgolly.clearconfig;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Optional;
import org.junit.Test;

public class ConfigSourcesTest {

    @Test
    public void testMapValues() {
        var source = ConfigSource.ofMap("test", Map.of("port", "1234"));
        var sources = ConfigSources.of(source);
        var mapped = sources.mapValues(s -> "[" + s + "]");
        var res = mapped.get("port", ConfigParser.String, Optional.empty());
        assertEquals("[1234]", res.getOrThrow().get());
    }
}
