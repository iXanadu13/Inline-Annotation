package pers.xanadu.annotation.processor.shade.lombok.javac;

import java.util.HashSet;
import java.util.Set;

import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Options;
import pers.xanadu.annotation.processor.shade.lombok.configuration.FormatPreferences;

public abstract class LombokOptions extends Options {
    private boolean deleteLombokAnnotations = false;
    private final Set<JCCompilationUnit> changed = new HashSet<JCCompilationUnit>();
    private FormatPreferences formatPreferences = new FormatPreferences(null);

    public boolean isChanged(JCCompilationUnit ast) {
        return changed.contains(ast);
    }

    public void setFormatPreferences(FormatPreferences formatPreferences) {
        this.formatPreferences = formatPreferences;
    }

    public FormatPreferences getFormatPreferences() {
        return this.formatPreferences;
    }

    public static void markChanged(Context context, JCCompilationUnit ast) {
        LombokOptions options = LombokOptionsFactory.getDelombokOptions(context);
        options.changed.add(ast);
    }

    public static boolean shouldDeleteLombokAnnotations(Context context) {
        LombokOptions options = LombokOptionsFactory.getDelombokOptions(context);
        return options.deleteLombokAnnotations;
    }

    protected LombokOptions(Context context) {
        super(context);
    }

    public abstract void putJavacOption(String optionName, String value);

    public void deleteLombokAnnotations() {
        this.deleteLombokAnnotations = true;
    }
}
