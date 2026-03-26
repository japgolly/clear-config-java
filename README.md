# ClearConfig for Java

A modern, type-safe, and highly composable configuration library for Java 21+.

# What's special about this?

The biggest and most unique feature is:
**CLARITY.**

Haven't we all had enough of situations like:

* changing an environment variable setting, pushing all the way though to an environment, testing and
  then discovering that your expected change didn't occur. Was the new setting picked up?
  What setting did it use? Where did it come from?

* after hours of frustration: *"That setting isn't even used any more?! But it's still in-place in all of our deployment config."*

This library endeavours to provide **clarity**.
When you get an instance of your config, you also get a report that describes:

* where config comes from
* how config sources override other sources
* what values each config source provided
* what config keys are in use
* what the total, resulting config is
* which config is still hanging around but is actually stale and no longer in use

*(sample reports below)*

Other features include:

- **Error Accumulation:** Instead of failing on the first error, it collects *all* configuration errors and reports them together.
- **Type-Safety:** Move from raw strings to rich types (Records, enums, durations, etc.) immediately.
- **Composability:** Build small, reusable configuration definitions and combine them into larger ones.
- **Security:** Easily mark sensitive keys as secrets to ensure they are obfuscated in reports.

# Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>com.github.japgolly.clearconfig</groupId>
  <artifactId>core-java</artifactId>
  <version>1.0.0</version>
</dependency>
```

# Quick Start

The following example demonstrates how to define a configuration model,
specify prioritised sources,
load the configuration,
and produce a detailed report.

```java
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
```

which might produce a report like this:

```txt
4 sources (highest to lowest priority):
  - cp:/demo.properties
  - Environment
  - System Properties
  - Default

Used keys (3):
+--------------+---------------------+-------------+-------------------+--------------+
| Key          | cp:/demo.properties | Environment | System Properties | Default      |
+--------------+---------------------+-------------+-------------------+--------------+
| demo.host    | localhost           |             |                   |              |
| demo.port    |                     |             |                   | 8080         |
| demo.timeout | 5s                  |             |                   | 1 min 30 sec |
+--------------+---------------------+-------------+-------------------+--------------+

Unused keys (2):
+--------------+---------------------+-------------------+
| Key          | cp:/demo.properties | System Properties |
+--------------+---------------------+-------------------+
| demo_timeout |                     | 10sec             |
| demo.prot    | 1234                |                   |
+--------------+---------------------+-------------------+
```

# Usage

### General

First start by choosing the type of your target config value:

```txt
ConfigParser.Boolean
ConfigParser.ChronoUnit
ConfigParser.Double
ConfigParser.Duration
ConfigParser.Enum()
ConfigParser.File
ConfigParser.Float
ConfigParser.InetAddress
ConfigParser.Integer
ConfigParser.LocalDate
ConfigParser.LocalDateTime
ConfigParser.LocalTime
ConfigParser.Long
ConfigParser.OffsetDateTime
ConfigParser.ofMap()
ConfigParser.Pattern
ConfigParser.Period
ConfigParser.Short
ConfigParser.String
ConfigParser.URI
ConfigParser.URL
ConfigParser.UUID
ConfigParser.ZonedDateTime
```

Then you'll usually want to call one of the following methods:

```java
// gets value of Optional<A> if key is specified, else returns Optional.empty()
.get(String key)

// gets value of A if key is specified, else parses a default string into an A
.getOrParse(String key, String defaultValue)

// gets value of A if key is specified, else uses a default A
.getOrUse(String key, A defaultValue)

// gets value of A if key is specified, else generates an error
.need(String key)
```

By now you'll have a `ConfigDef` value.
You'll likely want to compose a number of them together.
To do so, use `ConfigDef.apply(...)`, for example:

```java
public record AppConfig(int port, String host, Duration timeout) {}

ConfigDef<AppConfig> appConfigDef = ConfigDef.apply(
    ConfigParser.Integer.getOrUse("port", 8080),
    ConfigParser.String.need("host"),
    ConfigParser.Duration.getOrParse("timeout", "1 min 30 sec"),
    AppConfig::new)
```

It's also common to want to namespace keys after composition.
To do so, call `.withKeyPrefix(String prefix)`,
or for more power, call `.mapKeys(Function<String, String> f)`.

### Setters

What if you've got a config model that is mutable and expects you to call setters?

Firstly, instead of methods like `.getOrUse`, append `AndSet` to the name.
Secondly, compose them all together using `ConfigDef.consumer`.

Example:

```java
class Settable {
    private String w, x, y, z;
    public void setW(String w) { this.w = w; }
    public void setX(String x) { this.x = x; }
    public void setY(String y) { this.y = y; }
    public void setZ(String z) { this.z = z; }
}

var configDef = ConfigDef.consumer(
    ConfigParser.String.needAndSet      ("w",            Settable::setW),
    ConfigParser.String.getAndSet       ("x",            Settable::setX),
    ConfigParser.String.getOrUseAndSet  ("y", "default", Settable::setY),
    ConfigParser.String.getOrParseAndSet("z", "default", Settable::setZ));

var sources = ConfigSources.of(...);
var consumer = configDef.runOrThrow(sources);

var s = new Settable();
consumer.accept(s); // this sets all the fields specified by the config
```

### Secrets

Security is a first-class citizen in ClearConfig.
When generating reports, sensitive values are automatically obfuscated to prevent accidental exposure in logs or UI.

There are two ways a value is considered a secret:

1. **Implicitly**: If a configuration key contains the word "password" or "secret" (case-insensitive), it is automatically treated as a secret.
2. **Explicitly**: You can manually mark any `ConfigDef` as a secret by calling the `.secret()` method.

Example:

```java
import japgolly.clearconfig.*;
import java.util.Map;

public class SecretExample {
    public record DbConfig(String url, String password, String apiKey) {}

    public static void main(String[] args) {
        ConfigDef<DbConfig> dbConfigDef = ConfigDef.apply(
            ConfigParser.String.need("db.url"),
            ConfigParser.String.need("db.password"), // Automatically obfuscated due to name
            ConfigParser.String.need("api.key").secret(), // Manually obfuscated
            DbConfig::new
        );

        ConfigSources sources = ConfigSources.of(
            ConfigSource.ofMap("Demo", Map.of(
                "db.url",      "jdbc:postgresql://localhost/db",
                "db.password", "super-secret-password",
                "api.key",     "12345-ABCDE"
            ))
        );

        var result = dbConfigDef.withReport().runOrThrow(sources);
        System.out.println(result.report().used());
    }
}
```

Output:

```txt
Used keys (3):
+-------------+--------------------------------+---------+
| Key         | Demo                           | Default |
+-------------+--------------------------------+---------+
| api.key     | Obfuscated (5CE2935F)          |         |
| db.password | Obfuscated (EFE34FBF)          |         |
| db.url      | jdbc:postgresql://localhost/db |         |
+-------------+--------------------------------+---------+
```

### Custom Parsers

You can easily create your own parsers or transform existing ones.

#### Creating a parser from scratch

A `ConfigParser<A>` is simply a functional interface that takes a `String` and returns an `Either<ErrorMsg, A>`.

```java
import japgolly.clearconfig.*;

ConfigParser<Integer> binaryParser = s -> {
    try {
        return new Either.Success<>(Integer.parseInt(s, 2));
    } catch (NumberFormatException e) {
        return new Either.Failure<>(new ErrorMsg("Invalid binary number"));
    }
};
```

Because unchecked exceptions are automatically caught, this can be even simpler:

```java
ConfigParser<Integer> binaryParser =
    ConfigParser.String.map(s -> Integer.parseInt(s, 2));
```

#### Transforming existing parsers

You can use `map`, `flatMap`, and `preprocess` to adapt existing parsers.

* `map(A -> B)`: Transform the successfully parsed value.
* `flatMap(A -> Either<ErrorMsg, B>)`: Transform the successfully parsed value.
* `preprocess(String -> String)`: Transform the input string *before* it is parsed.

Example using `preprocess` for a case-insensitive map lookup:

```java
import java.util.Map;
import japgolly.clearconfig.*;

Map<String, Integer> protocolMap = Map.of(
    "HTTP", 80,
    "HTTPS", 443,
    "SSH", 22
);

// Create a parser that is case-insensitive by preprocessing input to uppercase
// This will now successfully parse "http", "  Ssh ", etc.
ConfigParser<Integer> protocolParser = ConfigParser
    .ofMap(protocolMap)
    .preprocess(String::trim)
    .preprocess(String::toUpperCase);
```

### Logback

Have you ever wondered what configuration keys your `logback.xml` depends on?
Often, logging configuration is managed separately from application configuration, making it difficult to see the full picture of what keys your application actually uses.

ClearConfig provides `ConfigDef.logbackXmlOnClasspath()` which scans your logback configuration files for variable substitutions like `${LOG_LEVEL:-INFO}`.

By composing this into your main `ConfigDef`, those keys will appear in your configuration report just like any other key, providing complete transparency.

Example:

```java
// Sample application config
ConfigDef<Integer> appConfigDef = ConfigParser.Integer.getOrUse("port", 8080);

// Compose app config with logback config
ConfigDef<Integer> fullConfigDef = ConfigDef.logbackXmlOnClasspath().andThen(appConfigDef);
```

This ensures that even "hidden" configuration dependencies in your XML files are brought to light in your configuration reports.

# Scala version

There is a Scala version of this library here:
https://github.com/japgolly/clear-config


# Support
If you like what I do
—my OSS libraries, my contributions to other OSS libs, [my programming blog](https://japgolly.blogspot.com)—
and you'd like to support me, more content, more lib maintenance, [please become a patron](https://www.patreon.com/japgolly)!
I do all my OSS work unpaid so showing your support will make a big difference.
