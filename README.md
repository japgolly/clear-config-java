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




# Scala version

There is a Scala version of this library here:
https://github.com/japgolly/clear-config


##### Support:
If you like what I do
—my OSS libraries, my contributions to other OSS libs, [my programming blog](https://japgolly.blogspot.com)—
and you'd like to support me, more content, more lib maintenance, [please become a patron](https://www.patreon.com/japgolly)!
I do all my OSS work unpaid so showing your support will make a big difference.
