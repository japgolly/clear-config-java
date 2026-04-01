package japgolly.clearconfig;

import java.util.List;
import java.util.stream.Collectors;

import japgolly.clearconfig.util.AsciiTable;
import japgolly.clearconfig.util.Internals;

/**
 * A detailed report of the configuration lookup process.
 *
 * Includes information about all sources, all keys looked up (used), and all keys present in sources
 * but not looked up (unused).
 */
public class ConfigReport {
    private final ConfigSources cfgSrcs;

    public ConfigReport(ConfigSources cfgSrcs) {
        this.cfgSrcs = cfgSrcs;
    }

    @Override
    public String toString() {
        return full();
    }

    public String full() {
        return String.format("%s\n\n%s\n\n%s", sources(), used(), unused());
    }

    public String sources() {
        var count = cfgSrcs.sources.size() + 1;
        var sourceList = cfgSrcs.sources.stream().map(s -> "  - " + s.name()).collect(Collectors.joining("\n"));
        return String.format("%d sources (highest to lowest priority):\n%s\n  - Default", count, sourceList);
    }

    public String used() {
        var header = cfgSrcs.sources.stream().map(s -> s.name()).collect(Collectors.toList());
        header.addFirst("Key");
        header.addLast("Default");
        var keys = cfgSrcs.seen().keySet().stream().sorted().collect(Collectors.toList());
        var rows = keys.stream().map(this::seenRow).collect(Collectors.toList());
        var table = rows.isEmpty() ? "No data to report." : AsciiTable.withHeader(header, rows);
        return String.format("Used keys (%d):\n%s", rows.size(), table);
    }

    public String unused() {
        var allUnusedKeys = cfgSrcs.sources.stream()
                .flatMap(s -> {
                    var hits = cfgSrcs.sourceHits(s);
                    return s.all().keySet().stream().filter(k -> !hits.contains(k));
                })
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (allUnusedKeys.isEmpty()) {
            return "Unused keys (0):\nNo data to report.";
        }

        var activeSources = cfgSrcs.sources.stream()
                .filter(s -> s.all().keySet().stream().anyMatch(allUnusedKeys::contains))
                .collect(Collectors.toList());

        var header = activeSources.stream().map(ConfigSource::name).collect(Collectors.toList());
        header.addFirst("Key");

        var rows = allUnusedKeys.stream().map(key -> unseenRow(activeSources, key)).collect(Collectors.toList());
        var table = rows.isEmpty() ? "No data to report." : AsciiTable.withHeader(header, rows);
        return String.format("Unused keys (%d):\n%s", rows.size(), table);
    }

    private List<String> seenRow(String key) {
        var ctx = cfgSrcs.seen().get(key);
        var row = cfgSrcs.sources.stream().map(s -> cell(s.get(key), ctx.secret)).collect(Collectors.toList());
        row.addFirst(key);
        row.addLast(cell(ctx.defaultValue.map(v -> "" + v).orElse(null), ctx.secret));
        return row;
    }

    private List<String> unseenRow(List<ConfigSource> activeSources, String key) {
        var secret = Internals.IMPLICITLY_SECRET.matcher(key).matches();
        var row = activeSources.stream().map(s -> {
            var v = cfgSrcs.sourceHits(s).contains(key) ? null : s.get(key);
            return cell(v, secret);
        }).collect(Collectors.toList());
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
