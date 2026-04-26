package japgolly.clearconfig;

import static org.junit.Assert.assertEquals;
import java.util.Set;
import org.junit.Test;

public class MultiNeedFailureTest {

    @Test
    public void multipleNeedsReportAllMissingKeys() {
        ConfigDef<String> configDef = ConfigDef.apply(
                ConfigParser.String.need("key1"),
                ConfigParser.String.need("key2"),
                ConfigParser.String.need("key3"),
                (k1, k2, k3) -> k1 + k2 + k3);

        ConfigSources sources = ConfigSources.of();
        Either<Set<ErrorMsg>, String> result = configDef.run(sources);

        Set<ErrorMsg> expectedErrors = Set.of(
                ErrorMsg.missingKey("key1"),
                ErrorMsg.missingKey("key2"),
                ErrorMsg.missingKey("key3")
        );

        assertEquals(new Either.Failure<>(expectedErrors), result);
    }

    @Test
    public void multipleNeedsWithSomePresent() {
        ConfigDef<String> configDef = ConfigDef.apply(
                ConfigParser.String.need("key1"),
                ConfigParser.String.need("key2"),
                ConfigParser.String.need("key3"),
                (k1, k2, k3) -> k1 + k2 + k3);

        ConfigSources sources = ConfigSources.of(
                ConfigSource.ofMap("test", java.util.Map.of("key2", "value2"))
        );
        Either<Set<ErrorMsg>, String> result = configDef.run(sources);

        Set<ErrorMsg> expectedErrors = Set.of(
                ErrorMsg.missingKey("key1"),
                ErrorMsg.missingKey("key3")
        );

        assertEquals(new Either.Failure<>(expectedErrors), result);
    }

    @Test
    public void multipleNeedsRunOrThrow() {
        ConfigDef<String> configDef = ConfigDef.apply(
                ConfigParser.String.need("key1"),
                ConfigParser.String.need("key2"),
                (k1, k2) -> k1 + k2);

        try {
            configDef.runOrThrow(ConfigSources.of());
            org.junit.Assert.fail("Should have thrown UnsatisfiedConfigException");
        } catch (UnsatisfiedConfigException e) {
            String msg = e.getMessage();
            org.junit.Assert.assertTrue("Message should contain key1", msg.contains("key1"));
            org.junit.Assert.assertTrue("Message should contain key2", msg.contains("key2"));
        }
    }
}
