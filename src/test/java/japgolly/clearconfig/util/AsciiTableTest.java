package japgolly.clearconfig.util;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

public class AsciiTableTest {

    @Test
    public void empty() {
        var actual = AsciiTable.withHeader(
            List.of("a", "bb", "ccccc"),
            List.of()
        );
        var expected = """
                +---+----+-------+
                | a | bb | ccccc |
                +---+----+-------+
                +---+----+-------+
                """.trim();
        assertEquals(expected, actual);
    }

    @Test
    public void populated() {
        var actual = AsciiTable.withHeader(
            List.of("a", "bb", "ccccc"),
            List.of(
                List.of("", "b", "c"),
                List.of("", "bbb", "")
            )
        );
        var expected = """
                +---+-----+-------+
                | a | bb  | ccccc |
                +---+-----+-------+
                |   | b   | c     |
                |   | bbb |       |
                +---+-----+-------+
                """.trim();
        assertEquals(expected, actual);
    }
}
