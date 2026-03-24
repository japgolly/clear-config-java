package japgolly.clearconfig;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Optional;

import org.junit.Test;

public class ConfigReportTest {

    record Example(Optional<Integer> a, int b, int c, int d) {
    }

    ConfigDef<Example> configDefWithDefaults = ConfigDef.apply(
            ConfigParser.Integer.get("get"),
            ConfigParser.Integer.getOrParse("getOrParse", "789"),
            ConfigParser.Integer.getOrUse("getOrUse", 123),
            ConfigParser.Integer.need("need"),
            Example::new);

    @Test
    public void testDefaults() throws Throwable {
        var source = ConfigSource.ofMap("Test", Map.of("need", "456"));
        var sources = ConfigSources.of(source);
        var result = configDefWithDefaults.withReport().runOrThrow(sources);
        var actual = result.report().seenTable();
        var expected = """
                +------------+------+---------+
                | Key        | Test | Default |
                +------------+------+---------+
                | get        |      |         |
                | getOrParse |      | 789     |
                | getOrUse   |      | 123     |
                | need       | 456  |         |
                +------------+------+---------+
                """.trim();
        assertEquals(expected, actual);
    }
}
