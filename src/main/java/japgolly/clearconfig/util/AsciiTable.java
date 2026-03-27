package japgolly.clearconfig.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility for generating ASCII tables, used primarily for configuration reports.
 */
public interface AsciiTable {

    public static String withHeader(List<String> header, List<List<String>> rows) {

        final var cols = header.size();

        // Determine column sizes
        final var colSizes = new int[cols];
        for (int i = 0; i < cols; i++) {
            final var j = i;
            var hdr = header.get(i);
            var row = rows.stream().map(r -> r.get(j));
            colSizes[i] = maxSize(hdr, row);
        }

        final var sep = Arrays.stream(colSizes).mapToObj(i -> "-".repeat(i)).collect(Collectors.joining("-+-", "+-", "-+"));
        final var rowFmt = Arrays.stream(colSizes).mapToObj(i -> "%-" + i + "s").collect(Collectors.joining(" | ", "| ", " |"));

        final var sb = new StringBuilder();
        sb.append(sep);
        sb.append('\n');
        appendRow(sb, rowFmt, header);
        sb.append('\n');
        sb.append(sep);
        for (var row : rows) {
            sb.append('\n');
            appendRow(sb, rowFmt, row);
        }
        sb.append('\n');
        sb.append(sep);
        return sb.toString();
    }

    private static int maxSize(String header, Stream<String> rows) {
        var rowMax = rows.map(String::length).reduce(0, Math::max);
        return Math.max(header.length(), rowMax);
    }

    private static void appendRow(StringBuilder sb, String fmt, List<String> row) {
        sb.append(String.format(fmt, row.toArray()));
    }
}
