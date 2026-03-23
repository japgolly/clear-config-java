package japgolly.clearconfig.util;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public interface Internals {
    public static final Pattern REGEX_TRUE = Pattern.compile("^(?:t(?:rue)?|y(?:es)?|1|on|enabled?)$", Pattern.CASE_INSENSITIVE);
    public static final Pattern REGEX_FALSE = Pattern.compile("^(?:f(?:alse)?|n(?:o)?|0|off|disabled?)$", Pattern.CASE_INSENSITIVE);

    public static Map<String, ChronoUnit> textToChronoUnitMap() {
        final Map<String, ChronoUnit> m = new HashMap<>();
        for (ChronoUnit u : ChronoUnit.values()) {
            switch (u) {
                case ChronoUnit.NANOS:
                    m.put("ns", u);
                    m.put("nano", u);
                    m.put("nanos", u);
                    m.put("nanosecond", u);
                    m.put("nanoseconds", u);
                    break;
                case ChronoUnit.MICROS:
                    m.put("μs", u);
                    m.put("us", u);
                    m.put("micro", u);
                    m.put("micros", u);
                    m.put("microsecond", u);
                    m.put("microseconds", u);
                    break;
                case ChronoUnit.MILLIS:
                    m.put("ms", u);
                    m.put("milli", u);
                    m.put("millis", u);
                    m.put("millisecond", u);
                    m.put("milliseconds", u);
                    break;
                case ChronoUnit.SECONDS:
                    m.put("s", u);
                    m.put("sec", u);
                    m.put("second", u);
                    m.put("seconds", u);
                    break;
                case ChronoUnit.MINUTES:
                    m.put("min", u);
                    m.put("minute", u);
                    m.put("minutes", u);
                    break;
                case ChronoUnit.HOURS:
                    m.put("hr", u);
                    m.put("hour", u);
                    m.put("hours", u);
                    break;
                case ChronoUnit.HALF_DAYS:
                    m.put("halfday", u);
                    m.put("halfdays", u);
                    break;
                case ChronoUnit.DAYS:
                    m.put("d", u);
                    m.put("day", u);
                    m.put("days", u);
                    break;
                case ChronoUnit.WEEKS:
                    m.put("w", u);
                    m.put("week", u);
                    m.put("weeks", u);
                    break;
                case ChronoUnit.MONTHS:
                    m.put("month", u);
                    m.put("months", u);
                    break;
                case ChronoUnit.YEARS:
                    m.put("y", u);
                    m.put("yr", u);
                    m.put("year", u);
                    m.put("years", u);
                    break;
                case ChronoUnit.DECADES:
                    m.put("decade", u);
                    m.put("decades", u);
                    break;
                case ChronoUnit.CENTURIES:
                    m.put("century", u);
                    m.put("centuries", u);
                    break;
                case ChronoUnit.MILLENNIA:
                    m.put("millennium", u);
                    m.put("millennia", u);
                    break;
                case ChronoUnit.ERAS:
                    m.put("era", u);
                    m.put("eras", u);
                    break;
                case ChronoUnit.FOREVER:
                    m.put("forever", u);
                    break;
            }
        }
        return m;
    }
}
