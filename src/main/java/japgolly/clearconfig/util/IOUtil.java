package japgolly.clearconfig.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public interface IOUtil {

    public static Optional<String> readResource(String filename) throws IOException {
        filename = filename.replaceFirst("^/*", "/");
        final var is = IOUtil.class.getResourceAsStream(filename);
        if (is == null)
            return Optional.empty();
        try {
            return Optional.of(new String(is.readAllBytes(), StandardCharsets.UTF_8));
        } finally {
            is.close();
        }
    }

    public static Optional<String> readFirstResource(String head, String... tail) throws IOException {
        var result = readResource(head);
        if (result.isPresent())
            return result;

        for (String filename : tail) {
            result = readResource(filename);
            if (result.isPresent())
                return result;
        }

        return Optional.empty();
    }
}
