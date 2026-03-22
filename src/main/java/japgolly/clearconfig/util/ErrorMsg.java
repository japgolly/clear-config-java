package japgolly.clearconfig.util;

public record ErrorMsg(String msg) {

    public static ErrorMsg missingKey(String key) {
        return new ErrorMsg("Missing key: " + key);
    }

    public static ErrorMsg uncaughtParsingError(String key, String value, Throwable e) {
        return new ErrorMsg(String.format("Failed to parse key '%s' with value '%s': %s", key, value, e.getClass().getName()));
    }
}
