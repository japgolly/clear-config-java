package japgolly.clearconfig.util;

public record ErrorMsg(String msg) {

    public static ErrorMsg missingKey(String key) {
        return new ErrorMsg("Missing key: " + key);
    }
}
