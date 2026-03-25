package japgolly.clearconfig;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;

public class ConfigSourceTest {

    @Test
    public void testMapKeyQueries() {
        var source = ConfigSource.ofMap("test", Map.of("BLAH.PORT", "1234"));
        var mapped = source.mapKeyQueries(s -> s.toUpperCase());
        assertEquals("1234", mapped.get("blah.port"));
        assertEquals(null, mapped.get("other"));
    }

    @Test
    public void testMapValues() {
        var source = ConfigSource.ofMap("test", Map.of("port", "1234"));
        var mapped = source.mapValues(s -> "[" + s + "]");
        assertEquals("[1234]", mapped.get("port"));
        assertEquals(null, mapped.get("other"));
    }

    @Test
    public void testOfPropFileOnClasspath() throws IOException {
        var source = ConfigSource.ofPropFileOnClasspath("test.properties", true);
        assertEquals("9999", source.get("test.port"));
    }

    @Test
    public void testOfPropFileOnClasspathOptional() throws IOException {
        var source = ConfigSource.ofPropFileOnClasspath("non-existent.properties", false);
        assertEquals(null, source.get("anything"));
    }

    @Test(expected = IOException.class)
    public void testOfPropFileOnClasspathMandatoryFail() throws IOException {
        ConfigSource.ofPropFileOnClasspath("non-existent.properties", true);
    }

    @Test
    public void testOfPropFile() throws IOException {
        var file = File.createTempFile("clearconfig", ".properties");
        try {
            var p = new Properties();
            p.setProperty("test.port", "8888");
            try (var out = new FileOutputStream(file)) {
                p.store(out, null);
            }

            var source = ConfigSource.ofPropFile(file, true);
            assertEquals("8888", source.get("test.port"));
        } finally {
            file.delete();
        }
    }

    @Test
    public void testOfPropFileOptional() throws IOException {
        var file = new File("non-existent.properties");
        var source = ConfigSource.ofPropFile(file, false);
        assertEquals(null, source.get("anything"));
    }

    @Test(expected = IOException.class)
    public void testOfPropFileMandatoryFail() throws IOException {
        var file = new File("non-existent.properties");
        ConfigSource.ofPropFile(file, true);
    }
}
