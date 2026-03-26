package japgolly.clearconfig.examples;

import japgolly.clearconfig.*;
import java.time.Duration;

public class QuickStart {

    // 1. Define your config class
    public record AppConfig(int port, String host, Duration timeout) {}

    public static void main(String[] args) throws Exception {

        // 2. Define how to parse your config
        ConfigDef<AppConfig> appConfigDef = ConfigDef.apply(
            ConfigParser.Integer.getOrUse("port", 8080),
            ConfigParser.String.need("host"),
            ConfigParser.Duration.getOrParse("timeout", "1 min 30 sec"),
            AppConfig::new)
            .withKeyPrefix("demo.");

        // 3. Specify config sources
        ConfigSources sources = ConfigSources.of(
            ConfigSource.ofPropFileOnClasspath("demo.properties", true),
            ConfigSource.Environment.filter(s -> s.contains("demo")),
            ConfigSource.SystemProps.filter(s -> s.contains("demo"))
        );

        // 4. Load the config with a report
        ConfigReportAndValue<AppConfig> result = appConfigDef.withReport().runOrThrow(sources);
        AppConfig config = result.value();
            
        // 5. Display the report
        System.out.println(result.report());
    }
}
