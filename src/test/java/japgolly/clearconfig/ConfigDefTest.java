package japgolly.clearconfig;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import japgolly.clearconfig.util.*;

public class ConfigDefTest {

    public record HttpServer(int port, InetAddress host) {
    }

    ConfigDef<HttpServer> configDefWithDefaults = ConfigDef.apply2(
            ConfigDef.integer.getOrUse("port", 8080),
            ConfigDef.inetAddress.getOrParse("host", "0.0.0.0"),
            (port, host) -> new HttpServer(port, host));

    ConfigDef<HttpServer> configDefWithoutDefaults = ConfigDef.apply2(
            ConfigDef.integer.need("port"),
            ConfigDef.inetAddress.need("host"),
            (port, host) -> new HttpServer(port, host));

    private <A> void assertSuccess(A expected, Either<Set<ErrorMsg>, A> actual) {
        assertEquals(new Either.Success<>(expected), actual);
    }

    private <A> void assertFailure(Set<ErrorMsg> errors, Either<Set<ErrorMsg>, A> actual) {
        assertEquals(new Either.Failure<>(errors), actual);
    }

    @Test
    public void withDefaults() throws UnknownHostException {
        var sources = ConfigSources.of();
        var result = configDefWithDefaults.run(sources);
        assertSuccess(new HttpServer(8080, InetAddress.getByName("0.0.0.0")), result);
    }

    @Test
    public void withoutDefaultsPass() throws UnknownHostException {
        var source = ConfigSource.manual("test", Map.of(
            "port", "1234 # This is a comment",
            "host", "127.0.0.1"
        ));
        var sources = ConfigSources.of(source);
        var result = configDefWithoutDefaults.run(sources);
        assertSuccess(new HttpServer(1234, InetAddress.getByName("127.0.0.1")), result);
    }

    @Test
    public void withoutDefaultsFail() {
        var sources = ConfigSources.of();
        var result = configDefWithoutDefaults.run(sources);
        assertFailure(Set.of(
            ErrorMsg.missingKey("host"),
            ErrorMsg.missingKey("port")
        ), result);
    }

    @Test
    public void testMapKeys() {
        var source = ConfigSource.manual("test", Map.of("blah.port", "1234"));
        var sources = ConfigSources.of(source);
        var def = ConfigDef.integer.getOrUse("port", 8080).withKeyPrefix("blah.");
        var result = def.run(sources);
        assertSuccess(1234, result);
    }

    @Test
    public void testRunOrThrow() {
        var sources = ConfigSources.of();
        try {
            configDefWithoutDefaults.runOrThrow(sources);
            org.junit.Assert.fail("Should have thrown an exception");
        } catch (UnsatisfiedConfigException e) {
            assertEquals(UnsatisfiedConfigException.PREFIX + "Missing key: host\nMissing key: port", e.getMessage());
        }
    }
}
