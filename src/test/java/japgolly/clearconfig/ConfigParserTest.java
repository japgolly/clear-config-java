package japgolly.clearconfig;

import static org.junit.Assert.assertEquals;

import japgolly.clearconfig.util.*;
import org.junit.Test;
import java.net.InetAddress;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class ConfigParserTest {

    private <A> void assertPass(A expected, Either<ErrorMsg, A> actual) {
        assertEquals(new Either.Success<>(expected), actual);
    }

    private <A> void assertFail(String expected, Either<ErrorMsg, A> actual) {
        assertEquals(new Either.Failure<>(new ErrorMsg(expected)), actual);
    }

    @Test
    public void string() {
        assertPass("hello", ConfigParser.String.parse("hello"));
        assertPass("trimmed", ConfigParser.String.parse("  trimmed  "));
        assertPass("no comment", ConfigParser.String.parse("no comment # with comment"));
    }

    @Test
    public void stringRaw() {
        assertPass("  hello # world  ", ConfigParser.StringRaw.parse("  hello # world  "));
    }

    @Test
    public void integer() {
        assertPass(123, ConfigParser.Integer.parse("123"));
        assertPass(-45, ConfigParser.Integer.parse("-45"));
        assertFail("java.lang.NumberFormatException", ConfigParser.Integer.parse("what"));
    }

    @Test
    public void long_() {
        assertPass(123L, ConfigParser.Long.parse("123"));
    }

    @Test
    public void double_() {
        assertPass(1.23, ConfigParser.Double.parse("1.23"));
    }

    @Test
    public void float_() {
        assertPass(1.23f, ConfigParser.Float.parse("1.23"));
    }

    @Test
    public void short_() {
        assertPass((short) 123, ConfigParser.Short.parse("123"));
    }

    @Test
    public void bool() {
        for (String s : new String[]{"t", "T", "true", "True", "y", "yes", "1", "on", "enable", "enabled"}) {
            assertPass(true, ConfigParser.Boolean.parse(s));
        }
        for (String s : new String[]{"f", "F", "false", "False", "n", "no", "0", "off", "disable", "disabled"}) {
            assertPass(false, ConfigParser.Boolean.parse(s));
        }
        assertFail("Invalid boolean", ConfigParser.Boolean.parse("what"));
    }

    @Test
    public void inetAddress() throws Exception {
        assertPass(InetAddress.getByName("127.0.0.1"), ConfigParser.InetAddress.parse("127.0.0.1"));
        assertFail("Invalid InetAddress", ConfigParser.InetAddress.parse("!!!"));
    }

    @Test
    public void uuid() {
        UUID uuid = UUID.randomUUID();
        assertPass(uuid, ConfigParser.UUID.parse(uuid.toString()));
    }

    @Test
    public void chronoUnit() {
        assertPass(ChronoUnit.NANOS, ConfigParser.ChronoUnit.parse("ns"));
        assertPass(ChronoUnit.NANOS, ConfigParser.ChronoUnit.parse("NANOSECONDS"));
        assertPass(ChronoUnit.MICROS, ConfigParser.ChronoUnit.parse("us"));
        assertPass(ChronoUnit.MICROS, ConfigParser.ChronoUnit.parse("μs"));
        assertPass(ChronoUnit.MILLIS, ConfigParser.ChronoUnit.parse("ms"));
        assertPass(ChronoUnit.MILLIS, ConfigParser.ChronoUnit.parse("millis"));
        assertPass(ChronoUnit.SECONDS, ConfigParser.ChronoUnit.parse("s"));
        assertPass(ChronoUnit.SECONDS, ConfigParser.ChronoUnit.parse("SEC"));
        assertPass(ChronoUnit.MINUTES, ConfigParser.ChronoUnit.parse("min"));
        assertPass(ChronoUnit.HOURS, ConfigParser.ChronoUnit.parse("HR"));
        assertPass(ChronoUnit.HOURS, ConfigParser.ChronoUnit.parse("hour"));
        assertPass(ChronoUnit.DAYS, ConfigParser.ChronoUnit.parse("d"));
        assertPass(ChronoUnit.WEEKS, ConfigParser.ChronoUnit.parse("w"));
        assertPass(ChronoUnit.MONTHS, ConfigParser.ChronoUnit.parse("month"));
        assertPass(ChronoUnit.YEARS, ConfigParser.ChronoUnit.parse("y"));
        assertPass(ChronoUnit.YEARS, ConfigParser.ChronoUnit.parse("YR"));
        assertFail("Invalid ChronoUnit", ConfigParser.ChronoUnit.parse("what"));
    }
}
