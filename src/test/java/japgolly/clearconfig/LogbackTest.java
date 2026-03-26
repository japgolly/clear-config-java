package japgolly.clearconfig;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import org.junit.Test;

public class LogbackTest {

    @Test
    public void test() throws Exception {
        var fakeEnv = ConfigSource.ofMap("Env", Map.of("LOG_LEVEL_SHIPREQ", "DEBUG"));
        var sources = ConfigSources.of(fakeEnv);
        var result = ConfigDef.logbackXmlOnClasspath().withReport().runOrThrow(sources);
        var report = result.report();
        var actual = report.full();
        var expect = """
                2 sources (highest to lowest priority):
                  - Env
                  - Default

                Used keys (4):
                +-------------------+-------+---------+
                | Key               | Env   | Default |
                +-------------------+-------+---------+
                | LOG_APPENDER      |       | JSON    |
                | LOG_LEVEL_MAIN    |       | INFO    |
                | LOG_LEVEL_ROOT    |       | INFO    |
                | LOG_LEVEL_SHIPREQ | DEBUG |         |
                +-------------------+-------+---------+

                Unused keys (0):
                No data to report.
                """.trim();
        assertEquals(expect, actual);
    }
}
