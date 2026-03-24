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
        assertEquals(new Example(Optional.empty(), 789, 123, 456), result.value());
    }

    @Test
    public void testWithKeyPrefix() throws Throwable {
        var source = ConfigSource.ofMap("Test", Map.of("test.need", "456", "test.getOrUse", "0"));
        var sources = ConfigSources.of(source);
        var configDef = configDefWithDefaults.withKeyPrefix("test_").mapKeys(s -> s.replace('_', '.'));
        var result = configDef.withReport().runOrThrow(sources);
        var actual = result.report().seenTable();
        var expected = """
                +-----------------+------+---------+
                | Key             | Test | Default |
                +-----------------+------+---------+
                | test.get        |      |         |
                | test.getOrParse |      | 789     |
                | test.getOrUse   | 0    | 123     |
                | test.need       | 456  |         |
                +-----------------+------+---------+
                """.trim();
        assertEquals(expected, actual);
        assertEquals(new Example(Optional.empty(), 789, 0, 456), result.value());
    }

    @Test
    public void testSecrets() throws Throwable {
        var source = ConfigSource.ofMap("Test", Map.of("explicit", "x", "Secret", "pe"));
        var sources = ConfigSources.of(source);
        var configDef = ConfigDef.apply(
            ConfigParser.String.getOrUse("Secret", "p1"),
            ConfigParser.String.getOrUse("db.password", "p2"),
            ConfigParser.String.getOrUse("other", "o"),
            ConfigParser.String.getOrUse("explicit", "pe").secret(),
            (a, b, c, d) -> null
        );
        var result = configDef.withReport().runOrThrow(sources);
        var actual = result.report().seenTable();
        var expected = """
                +-------------+-----------------------+-----------------------+
                | Key         | Test                  | Default               |
                +-------------+-----------------------+-----------------------+
                | Secret      | Obfuscated (8461AB4C) | Obfuscated (D8E5DA80) |
                | db.password |                       | Obfuscated (EFE34FBF) |
                | explicit    | Obfuscated (5CE2935F) | Obfuscated (8461AB4C) |
                | other       |                       | o                     |
                +-------------+-----------------------+-----------------------+
                """.trim();
        assertEquals(expected, actual);
    }

    @Test
    public void testMapValues() throws Throwable {
        var source = ConfigSource.ofMap("Test", Map.of("need", "456"));
        var sources = ConfigSources.of(source.mapValues(s -> "[" + s + "]"));
        var configDef = ConfigParser.String.need("need");
        var result = configDef.withReport().runOrThrow(sources);
        var actual = result.report().seenTable();
        var expected = """
                +------+-------+---------+
                | Key  | Test  | Default |
                +------+-------+---------+
                | need | [456] |         |
                +------+-------+---------+
                """.trim();
        assertEquals(expected, actual);
        assertEquals("[456]", result.value());
    }
}
