package japgolly.clearconfig;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import japgolly.clearconfig.util.*;

public class ConfigDefTest {

    record HttpServer(int port, InetAddress host) {
    }

    ConfigDef<HttpServer> configDefWithDefaults = ConfigDef.apply(
            ConfigParser.Integer.getOrUse("port", 8080),
            ConfigParser.InetAddress.getOrParse("host", "0.0.0.0"),
            HttpServer::new);

    ConfigDef<HttpServer> configDefWithoutDefaults = ConfigDef.apply(
            ConfigParser.Integer.need("port"),
            ConfigParser.InetAddress.need("host"),
            HttpServer::new);

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
        var source = ConfigSource.ofMap("test", Map.of(
            "port", "1234",
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
    public void mappedKeys() {
        var source = ConfigSource.ofMap("test", Map.of("blah.port", "1234"));
        var sources = ConfigSources.of(source);
        var def = ConfigParser.Integer.getOrUse("port", 8080).withKeyPrefix("blah.");
        var result = def.run(sources);
        assertSuccess(1234, result);
    }

    @Test
    public void parserError() {
        var source = ConfigSource.ofMap("test", Map.of("port", "x"));
        var sources = ConfigSources.of(source);
        var def = ConfigParser.Integer.need("port");
        var result = def.run(sources);
        assertFailure(Set.of(
            ErrorMsg.parsingError(new NumberFormatException()).addKeyValueContext("port", "x")
        ), result);
    }

    @Test
    public void runOrThrow() {
        var sources = ConfigSources.of();
        try {
            configDefWithoutDefaults.runOrThrow(sources);
            org.junit.Assert.fail("Should have thrown an exception");
        } catch (UnsatisfiedConfigException e) {
            assertEquals(UnsatisfiedConfigException.PREFIX + "Missing key: host\nMissing key: port", e.getMessage());
        }
    }

    @Test
    public void multipleSources() throws UnknownHostException {
        var source1 = ConfigSource.ofMap("test", Map.of(
            "host", "127.0.0.1"
        ));
        var source2 = ConfigSource.ofMap("test", Map.of(
            "port", "1234",
            "host", "0.0.0.0"
        ));
        var sources = ConfigSources.of(source1, source2);
        var result = configDefWithoutDefaults.run(sources);
        assertSuccess(new HttpServer(1234, InetAddress.getByName("127.0.0.1")), result);
    }

    class Settable {
        String v, w, x, y, z;
        public void setV(String v) { this.v = v; }
        public void setW(String w) { this.w = w; }
        public void setX(String x) { this.x = x; }
        public void setY(String y) { this.y = y; }
        public Settable setZ(String z) { this.z = z; return this; }
    }

    @Test
    public void setters() throws UnsatisfiedConfigException {
        var configDef = ConfigDef.setters(
            ConfigParser.String.needAndSet("v", Settable::setV),
            ConfigParser.String.getAndSet("w", Settable::setW),
            ConfigParser.String.getAndSet("x", Settable::setX),
            ConfigParser.String.getOrUseAndSet("y", "default #", Settable::setY),
            ConfigParser.String.getOrParseAndSet("z", "def #", Settable::setZ)
        );
        var source = ConfigSource.ofMap("test", Map.of("v", "vee", "x", "eks"));
        var sources = ConfigSources.of(source);
        var consumer = configDef.runOrThrow(sources);
        var s = new Settable();
        consumer.accept(s);
        assertEquals("vee", s.v);
        assertEquals(null, s.w);
        assertEquals("eks", s.x);
        assertEquals("default #", s.y);
        assertEquals("def #", s.z);
    }

    @Test
    public void testMap() {
        var source = ConfigSource.ofMap("test", Map.of("port", "123"));
        var sources = ConfigSources.of(source);
        var def = ConfigParser.Integer.need("port").map(i -> i * 2);
        var result = def.run(sources);
        assertSuccess(246, result);
    }
}
