package japgolly.clearconfig;

import java.util.List;
import java.util.stream.Collectors;

import japgolly.clearconfig.ConfigSources.KeyCtx;
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
        var keys = sources.seen().keySet().stream().sorted().collect(Collectors.toList());
        var rows = keys.stream().map(this::seenRow).collect(Collectors.toList());
        return AsciiTable.withHeader(header, rows);
    }

    private List<String> seenRow(String key) {
        var ctx = sources.seen().get(key);
        var row = sources.sources.stream().map(s -> cell(s.get(key), ctx)).collect(Collectors.toList());
        row.addFirst(key);
        row.addLast(cell(ctx.defaultValue.map(v -> "" + v).orElse(null), ctx));
        return row;
    }

    private String cell(String value, KeyCtx ctx) {
        if (value == null)
            value = "";
        else if (ctx.secret)
            value = String.format("Obfuscated (%08X)", (value + "!").repeat(7).hashCode());
        return value;
    }
}
