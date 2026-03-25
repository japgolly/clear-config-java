package japgolly.clearconfig;

import java.util.List;
import java.util.stream.Collectors;

import japgolly.clearconfig.util.AsciiTable;
import japgolly.clearconfig.util.Internals;

public class ConfigReport {
    private final ConfigSources sources;

    public ConfigReport(ConfigSources sources) {
        this.sources = sources;
    }

    @Override
    public String toString() {
        return full();
    }

    public String full() {
        return String.format("%s\n\n%s\n\n%s", sources(), used(), unused());
    }

    public String sources() {
        var count = sources.sources.size() + 1;
        var sourceList = sources.sources.stream().map(s -> "  - " + s.name()).collect(Collectors.joining("\n"));
        return String.format("%d sources (highest to lowest priority):\n%s\n  - Default", count, sourceList);
    }

    public String used() {
        var header = sources.sources.stream().map(s -> s.name()).collect(Collectors.toList());
        header.addFirst("Key");
        header.addLast("Default");
        var keys = sources.seen().keySet().stream().sorted().collect(Collectors.toList());
        var rows = keys.stream().map(this::seenRow).collect(Collectors.toList());
        var table = AsciiTable.withHeader(header, rows);
        return String.format("Used keys (%d):\n%s", rows.size(), table);
    }

    public String unused() {
        var header = sources.sources.stream().map(s -> s.name()).collect(Collectors.toList());
        header.addFirst("Key");
        var seenKeys = sources.seen().keySet();
        var allKeys = sources.sources.stream()
                .flatMap(s -> s.all().keySet().stream())
                .filter(k -> !seenKeys.contains(k))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        var rows = allKeys.stream().map(this::unusedRow).collect(Collectors.toList());
        var table = AsciiTable.withHeader(header, rows);
        return String.format("Unused keys (%d):\n%s", rows.size(), table);
    }

    private List<String> seenRow(String key) {
        var ctx = sources.seen().get(key);
        var row = sources.sources.stream().map(s -> cell(s.get(key), ctx.secret)).collect(Collectors.toList());
        row.addFirst(key);
        row.addLast(cell(ctx.defaultValue.map(v -> "" + v).orElse(null), ctx.secret));
        return row;
    }

    private List<String> unusedRow(String key) {
        var secret = Internals.IMPLICITLY_SECRET.matcher(key).matches();
        var row = sources.sources.stream().map(s -> cell(s.get(key), secret)).collect(Collectors.toList());
        row.addFirst(key);
        return row;
    }

    private String cell(String value, boolean secret) {
        if (value == null)
            value = "";
        else if (secret)
            value = String.format("Obfuscated (%08X)", (value + "!").repeat(7).hashCode());
        return value;
    }
}
