package pers.xanadu.annotation.processor.shade.lombok.configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CheckerFrameworkVersion implements ConfigurationValueType {
    private final int version;
    private static final int DEFAULT = 3200;
    private static final int MAX_SUPPORTED = 4000;

    public static final String NAME__SIDE_EFFECT_FREE = "org.checkerframework.dataflow.qual.SideEffectFree";
    public static final String NAME__PURE = "org.checkerframework.dataflow.qual.Pure";
    public static final String NAME__UNIQUE = "org.checkerframework.common.aliasing.qual.Unique";
    public static final String NAME__RETURNS_RECEIVER = "org.checkerframework.common.returnsreceiver.qual.This";
    public static final String NAME__CALLED = "org.checkerframework.checker.calledmethods.qual.CalledMethods";

    public static final CheckerFrameworkVersion NONE = new CheckerFrameworkVersion(0);

    private CheckerFrameworkVersion(int v) {
        this.version = v;
    }

    private static final Pattern VERSION = Pattern.compile("^(\\d+)(?:\\.(\\d+))?(?:\\.\\d+)*$");

    public boolean generateSideEffectFree() {
        return version > 0;
    }

    public boolean generateUnique() {
        return version > 2899;
    }

    public boolean generatePure() {
        return version > 0;
    }

    public boolean generateReturnsReceiver() {
        return version >= 3100;
    }

    public boolean generateCalledMethods() {
        return version >= 3100;
    }

    public static CheckerFrameworkVersion valueOf(String versionString) {
        if (versionString != null) versionString = versionString.trim();
        if (versionString == null || versionString.equalsIgnoreCase("false") || versionString.equals("0")) return new CheckerFrameworkVersion(0);
        if (versionString.equalsIgnoreCase("true")) return new CheckerFrameworkVersion(DEFAULT);
        Matcher m = VERSION.matcher(versionString);
        if (!m.matches()) throw new IllegalArgumentException("Expected 'true' or 'false' or a major/minor version, such as '2.9'");
        int major = Integer.parseInt(m.group(1));
        int minor = (m.group(2) != null && !m.group(2).isEmpty()) ? Integer.parseInt(m.group(2)) : 0;
        if (minor > 999) throw new IllegalArgumentException("Minor version must be between 0 and 999");
        int v = major * 1000 + minor;
        if (v > MAX_SUPPORTED) {
            String s = (v / 1000) + "." + (v % 1000);
            throw new IllegalArgumentException("Lombok supports at most v" + s + "; reduce the value of key 'checkerframework' to " + s);
        }
        return new CheckerFrameworkVersion(v);
    }

    public static String description() {
        return "checkerframework-version";
    }

    public static String exampleValue() {
        String s = (MAX_SUPPORTED / 1000) + "." + (MAX_SUPPORTED % 1000);
        return "major.minor (example: 3.2 - and no higher than " + s + ") or true or false";
    }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof CheckerFrameworkVersion)) return false;
        return version == ((CheckerFrameworkVersion) obj).version;
    }

    @Override public int hashCode() {
        return version;
    }
}