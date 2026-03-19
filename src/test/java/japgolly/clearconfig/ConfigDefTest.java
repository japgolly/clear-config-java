package japgolly.clearconfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.Test;
import japgolly.clearconfig.util.*;

public class ConfigDefTest {

    // @Test
    // public void integerSuccess() {
    //     var res = ConfigDef.integer.parse("123 # This is a comment");
    //     assertTrue(res instanceof Either.Success);
    //     assertEquals(Integer.valueOf(123), res.getOrThrow());
    // }

    // @Test
    // public void integerFailure() {
    //     var res = ConfigDef.integer.parse("abc");
    //     assertTrue(res instanceof Either.Failure);
    // }

    public record HttpServer(int port, String host) {
    }

    ConfigDef<HttpServer> configDefWithDefaults = ConfigDef.apply2(
            ConfigDef.integer.getOrUse("port", 8080),
            ConfigDef.string.getOrParse("host", "0.0.0.0 # default"),
            (port, host) -> new HttpServer(port, host));

    ConfigDef<HttpServer> configDefWithoutDefaults = ConfigDef.apply2(
            ConfigDef.integer.need("port"),
            ConfigDef.string.need("host"),
            (port, host) -> new HttpServer(port, host));

    private <A> void assertSuccess(A expected, Either<Set<ErrorMsg>, A> actual) {
        assertEquals(new Either.Success<>(expected), actual);
    }

    private <A> void assertFailure(Set<ErrorMsg> errors, Either<Set<ErrorMsg>, A> actual) {
        assertEquals(new Either.Failure<>(errors), actual);
    }

    @Test
    public void withDefaults() {
        var sources = ConfigSources.of();
        var result = configDefWithDefaults.run(sources);
        assertSuccess(new HttpServer(8080, "0.0.0.0"), result);
    }

    @Test
    public void withoutDefaultsPass() {
        var source = ConfigSource.manual("test", Map.of(
            "port", "1234 # This is a comment",
            "host", "127.0.0.1"
        ));
        var sources = ConfigSources.of(source);
        var result = configDefWithoutDefaults.run(sources);
        assertSuccess(new HttpServer(1234, "127.0.0.1"), result);
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
}
