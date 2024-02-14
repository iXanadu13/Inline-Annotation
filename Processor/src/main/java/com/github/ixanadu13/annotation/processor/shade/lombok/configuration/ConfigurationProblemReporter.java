package com.github.ixanadu13.annotation.processor.shade.lombok.configuration;

//import lombok.eclipse.handlers.EclipseHandlerUtil;

public interface ConfigurationProblemReporter {
    void report(String sourceDescription, String problem, int lineNumber, CharSequence line);

    ConfigurationProblemReporter CONSOLE = new ConfigurationProblemReporter() {
        @Override public void report(String sourceDescription, String problem, int lineNumber, CharSequence line) {
            try {
                // The console (System.err) is non-existent in eclipse environments, so we should try to
                // log into at least the error log. This isn't really the appropriate place (should go in the
                // relevant file instead, most people never see anything in the error log either!), but at least
                // there is a way to see it, vs. System.err, which is completely invisible.

                //EclipseHandlerUtil.warning(String.format("%s (%s:%d)", problem, sourceDescription, lineNumber), null);
            } catch (Throwable ignore) {}
            System.err.printf("%s (%s:%d)\n", problem, sourceDescription, lineNumber);
        }
    };
}
