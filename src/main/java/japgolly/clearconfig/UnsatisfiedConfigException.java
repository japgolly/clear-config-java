package japgolly.clearconfig;

import java.util.Set;
import java.util.stream.Collectors;

import japgolly.clearconfig.util.ErrorMsg;

public class UnsatisfiedConfigException extends Exception {

    public UnsatisfiedConfigException(String msg) {
        super(msg);
    }

    public UnsatisfiedConfigException(Set<ErrorMsg> errMsgs) {
        this(PREFIX + errMsgs
                .stream()
                .map(ErrorMsg::msg)
                .sorted()
                .collect(Collectors.joining("\n")));
    }

    public static final String PREFIX = "Unsatisfied configuration:\n";
}
