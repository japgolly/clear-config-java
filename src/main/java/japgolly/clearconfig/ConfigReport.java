package japgolly.clearconfig;

import java.util.List;
import java.util.stream.Collectors;

import japgolly.clearconfig.util.AsciiTable;

public class ConfigReport {
    private final ConfigSources sources;

    public ConfigReport(ConfigSources sources) {
        this.sources = sources;
    }

    public String seenTable() {
        var header = sources.sources.stream().map(s -> s.name()).collect(Collectors.toList());
        header.addFirst("Key");
        header.addLast("Default");
        var keys = sources.seen.keySet().stream().sorted().collect(Collectors.toList());
        var rows = keys.stream().map(this::seenRow).collect(Collectors.toList());
        return AsciiTable.withHeader(header, rows);
    }

    private List<String> seenRow(String key) {
        var ctx = sources.seen.get(key);
        var row = sources.sources.stream().map(s -> cell(s, key)).collect(Collectors.toList());
        row.addFirst(key);
        row.addLast("" + ctx.defaultValue.orElse(""));
        return row;
    }

    private String cell(ConfigSource s, String key) {
        var value = s.get(key);
        return value == null ? "" : value;
    }
}
