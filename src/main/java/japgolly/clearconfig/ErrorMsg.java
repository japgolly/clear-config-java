package japgolly.clearconfig;

public record ErrorMsg(String msg) {

    public boolean isMissingKey() {
        return msg.startsWith(MISSING_KEY_PREFIX);
    }

    private static final String MISSING_KEY_PREFIX = "Missing key: ";

    public static ErrorMsg missingKey(String key) {
        return new ErrorMsg(MISSING_KEY_PREFIX + key);
    }

    public static ErrorMsg parsingError(Throwable e) {
        return new ErrorMsg(String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage()));
    }

    public ErrorMsg addKeyValueContext(String key, String value) {
        return new ErrorMsg(String.format("Failed to parse key \"%s\" with value \"%s\": %s", key, value, msg));
    }
}
