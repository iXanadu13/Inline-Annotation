package pers.xanadu.annotation.processor.shade.lombok.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LogDeclaration implements ConfigurationValueType {
    private static final Pattern PARAMETERS_PATTERN = Pattern.compile("(?:\\(([A-Z,]*)\\))");
    private static final Pattern DECLARATION_PATTERN = Pattern.compile("^(?:([^ ]+) )?([^(]+)\\.([^(]+)(" + PARAMETERS_PATTERN.pattern() + "+)$");

    public enum LogFactoryParameter {
        TYPE, NAME, TOPIC, NULL;
    }

    private final TypeName loggerType;
    private final TypeName loggerFactoryType;
    private final IdentifierName loggerFactoryMethod;
    private final List<LogFactoryParameter> parametersWithoutTopic;
    private final List<LogFactoryParameter> parametersWithTopic;

    private LogDeclaration(TypeName loggerType, TypeName loggerFactoryType, IdentifierName loggerFactoryMethod, List<LogFactoryParameter> parametersWithoutTopic, List<LogFactoryParameter> parametersWithTopic) {
        this.loggerType = loggerType;
        this.loggerFactoryType = loggerFactoryType;
        this.loggerFactoryMethod = loggerFactoryMethod;
        this.parametersWithoutTopic = parametersWithoutTopic;
        this.parametersWithTopic = parametersWithTopic;
    }

    public static LogDeclaration valueOf(String declaration) {
        if (declaration == null) return null;

        Matcher matcher = DECLARATION_PATTERN.matcher(declaration);
        if (!matcher.matches()) throw new IllegalArgumentException("The declaration must follow the pattern: [LoggerType ]LoggerFactoryType.loggerFactoryMethod(loggerFactoryMethodParams)[(loggerFactoryMethodParams)]");

        TypeName loggerFactoryType = TypeName.valueOf(matcher.group(2));
        TypeName loggerType = TypeName.valueOf(matcher.group(1));
        if (loggerType == null) loggerType = loggerFactoryType;
        IdentifierName loggerFactoryMethod = IdentifierName.valueOf(matcher.group(3));
        List<List<LogFactoryParameter>> allParameters = parseParameters(matcher.group(4));

        List<LogFactoryParameter> parametersWithoutTopic = null;
        List<LogFactoryParameter> parametersWithTopic = null;
        for (List<LogFactoryParameter> parameters: allParameters) {
            if (parameters.contains(LogFactoryParameter.TOPIC)) {
                if (parametersWithTopic != null) throw new IllegalArgumentException("There is more than one parameter definition that includes TOPIC: " + parametersWithTopic + " and " + parameters);
                parametersWithTopic = parameters;
            } else {
                if (parametersWithoutTopic != null) throw new IllegalArgumentException("There is more than one parmaeter definition that does not include TOPIC: " + parametersWithoutTopic + " and " + parameters);
                parametersWithoutTopic = parameters;
            }
        }

        // sanity check (the pattern should disallow this situation
        if (parametersWithoutTopic == null && parametersWithTopic == null) throw new IllegalArgumentException("No logger factory method parameters specified.");

        return new LogDeclaration(loggerType, loggerFactoryType, loggerFactoryMethod, parametersWithoutTopic, parametersWithTopic);
    }

    private static List<List<LogFactoryParameter>> parseParameters(String parametersDefinitions) {
        List<List<LogFactoryParameter>> allParameters = new ArrayList<List<LogFactoryParameter>>();
        Matcher matcher = PARAMETERS_PATTERN.matcher(parametersDefinitions);
        while (matcher.find()) {
            String parametersDefinition = matcher.group(1);
            List<LogFactoryParameter> parameters = new ArrayList<LogFactoryParameter>();
            if (!parametersDefinition.isEmpty()) {
                for (String parameter : parametersDefinition.split(",")) {
                    parameters.add(LogFactoryParameter.valueOf(parameter));
                }
            }
            allParameters.add(parameters);
        }
        return allParameters;
    }

    public static String description() {
        return "custom-log-declaration";
    }

    public static String exampleValue() {
        return "my.cool.Logger my.cool.LoggerFactory.createLogger()(TOPIC,TYPE)";
    }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof LogDeclaration)) return false;
        return loggerType.equals(((LogDeclaration) obj).loggerType)
                && loggerFactoryType.equals(((LogDeclaration) obj).loggerFactoryType)
                && loggerFactoryMethod.equals(((LogDeclaration) obj).loggerFactoryMethod)
                && parametersWithoutTopic == ((LogDeclaration) obj).parametersWithoutTopic || parametersWithoutTopic.equals(((LogDeclaration) obj).parametersWithoutTopic)
                && parametersWithTopic == ((LogDeclaration) obj).parametersWithTopic || parametersWithTopic.equals(((LogDeclaration) obj).parametersWithTopic);
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + loggerType.hashCode();
        result = prime * result + loggerFactoryType.hashCode();
        result = prime * result + loggerFactoryMethod.hashCode();
        result = prime * result + ((parametersWithTopic == null) ? 0 : parametersWithTopic.hashCode());
        result = prime * result + ((parametersWithoutTopic == null) ? 0 : parametersWithoutTopic.hashCode());
        return result;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(loggerType);
        sb.append(" ");
        sb.append(loggerFactoryType);
        sb.append(".");
        sb.append(loggerFactoryMethod);
        appendParams(sb, parametersWithoutTopic);
        appendParams(sb, parametersWithTopic);
        return sb.toString();
    }

    private static void appendParams(StringBuilder sb, List<LogFactoryParameter> params) {
        if (params != null) {
            sb.append("(");
            boolean first = true;
            for (LogFactoryParameter param : params) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append(param);
            }
            sb.append(")");
        }
    }

    public TypeName getLoggerType() {
        return loggerType;
    }

    public TypeName getLoggerFactoryType() {
        return loggerFactoryType;
    }

    public IdentifierName getLoggerFactoryMethod() {
        return loggerFactoryMethod;
    }

    public List<LogFactoryParameter> getParametersWithoutTopic() {
        return parametersWithoutTopic;
    }

    public List<LogFactoryParameter> getParametersWithTopic() {
        return parametersWithTopic;
    }
}