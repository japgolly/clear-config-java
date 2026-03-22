package japgolly.clearconfig.util;

import java.util.regex.Pattern;

public interface Internals {
    public static final Pattern REGEX_TRUE = Pattern.compile("^(?:t(?:rue)?|y(?:es)?|1|on|enabled?)$", Pattern.CASE_INSENSITIVE);
    public static final Pattern REGEX_FALSE = Pattern.compile("^(?:f(?:alse)?|n(?:o)?|0|off|disabled?)$", Pattern.CASE_INSENSITIVE);
}
