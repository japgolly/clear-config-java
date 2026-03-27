package japgolly.clearconfig;

/**
 * A container for both the successfully parsed configuration value and its associated report.
 */
public record ConfigReportAndValue<A>(ConfigReport report, A value) {
}
