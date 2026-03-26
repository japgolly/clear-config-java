package japgolly.clearconfig.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import japgolly.clearconfig.ConfigDef;
import japgolly.clearconfig.ConfigParser;

public interface Logback {

    public static void warn(String msg) {
        System.err.println("[clear-config warning] " + msg);
    }

    public static List<String> extractSubstitutions(String input) {
        final var results = new ArrayList<String>();
        var depth = 0;
        var startIndex = -1;
        var i = 0;

        while (i < input.length()) {
            var ch = input.charAt(i);

            if (depth == 0) {
                // We are searching for a new start "${"
                if (ch == '$' && i + 1 < input.length() && input.charAt(i + 1) == '{') {
                    startIndex = i;
                    depth = 1;
                    i++; // Skip the '{' to avoid counting it as a nested open brace immediately
                }
            } else {
                // We are currently inside a variable declaration
                if (ch == '{') {
                    depth++;
                } else if (ch == '}') {
                    depth--;
                    // If we are back to 0, we found the closing brace for the top-level var
                    if (depth == 0) {
                        results.add(input.substring(startIndex, i + 1));
                    }
                }
            }
            i++;
        }

        return results;
    }

    public static ConfigDef<Void> logbackXmlContent(String content) {
        final Pattern getOrElse = Pattern.compile("(.*?):-(.*)");

        class Parser {
            ConfigDef<Void> result = ConfigDef.unit();

            Optional<String> parseExpr(String expr) {
                // Remove "${" and "}" (equivalent to expr.drop(2).dropRight(1))
                final String body = expr.substring(2, Math.max(2, expr.length() - 1));

                final Matcher matcher = getOrElse.matcher(body);

                if (matcher.matches()) {
                    final String lhs = matcher.group(1);
                    final String rhs = matcher.group(2);

                    if (!extractSubstitutions(lhs).isEmpty()) {
                        warn("Ignoring unsupported expression: " + lhs);
                        return Optional.empty();
                    } else {
                        final var defaultValue = parseDefault(rhs);
                        if (defaultValue.isPresent())
                            result = ConfigParser.String.getOrUse(lhs, defaultValue.get()).andThen(result);
                        else
                            result = ConfigParser.String.get(lhs).andThen(result);
                        return defaultValue;
                    }
                } else {
                    if (!extractSubstitutions(body).isEmpty()) {
                        warn("Ignoring unsupported expression: " + body);
                    } else {
                        result = ConfigParser.String.need(body).andThen(result);
                    }
                    return Optional.empty();
                }
            }

            Optional<String> parseDefault(String expr) {
                if (extractSubstitutions(expr).isEmpty()) {
                    return Optional.of(expr);
                } else {
                    return parseExpr(expr);
                }
            }
        }

        final var parser = new Parser();
        for (String expr : extractSubstitutions(content)) {
            parser.parseExpr(expr);
        }
        return parser.result;
    }
}
