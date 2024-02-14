package pers.xanadu.annotation.processor.shade.lombok;

import java.util.Collection;

public interface ImportList {
    /**
     * If there is an explicit import of the stated unqualified type name, return that. Otherwise, return null.
     */
    String getFullyQualifiedNameForSimpleName(String unqualified);

    /**
     * If there is an explicit import of the stated unqualified type name, return that. Otherwise, return null.
     * Do not translate the produced fully qualified name to the alias.
     */
    String getFullyQualifiedNameForSimpleNameNoAliasing(String unqualified);

    /**
     * Returns true if the package name is explicitly star-imported, OR the packageName refers to this source file's own package name, OR packageName is 'java.lang'.
     */
    boolean hasStarImport(String packageName);

    /**
     * Takes all explicit non-static star imports whose first element is equal to {@code startsWith}, replaces the star with {@code unqualified}, and returns these.
     */
    Collection<String> applyNameToStarImports(String startsWith, String unqualified);

    String applyUnqualifiedNameToPackage(String unqualified);
}
