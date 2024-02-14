package pers.xanadu.annotation.processor.shade.lombok;

import java.util.List;

import pers.xanadu.annotation.processor.shade.lombok.AST.Kind;

/**
 * Capable of resolving a simple type name such as 'String' into 'java.lang.String'.
 * <p>
 * NB: This resolver gives wrong answers when there's a class in the local package with the same name as a class in a star-import,
 *  and this importer also can't find inner types from superclasses/interfaces.
 */
public class TypeResolver {
    private ImportList imports;

    /**
     * Creates a new TypeResolver that can be used to resolve types in a source file with the given package and import statements.
     */
    public TypeResolver(ImportList importList) {
        this.imports = importList;
    }

    public boolean typeMatches(LombokNode<?, ?, ?> context, String fqn, String typeRef) {
        return typeRefToFullyQualifiedName(context, TypeLibrary.createLibraryForSingleType(fqn), typeRef) != null;
    }

    public String typeRefToFullyQualifiedName(LombokNode<?, ?, ?> context, TypeLibrary library, String typeRef) {
        // When asking if 'Foo' could possibly be referring to 'bar.Baz', the answer is obviously no.
        List<String> qualifieds = library.toQualifieds(typeRef);
        if (qualifieds == null || qualifieds.isEmpty()) return null;

        // When asking if 'lombok.Getter' could possibly be referring to 'lombok.Getter', the answer is obviously yes.
        if (qualifieds.contains(typeRef)) return LombokInternalAliasing.processAliases(typeRef);

        // When asking if 'Getter' could possibly be referring to 'lombok.Getter' if 'import lombok.Getter;' is in the source file, the answer is yes.
        int firstDot = typeRef.indexOf('.');
        if (firstDot == -1) firstDot = typeRef.length();
        String firstTypeRef = typeRef.substring(0, firstDot);
        String fromExplicitImport = imports.getFullyQualifiedNameForSimpleNameNoAliasing(firstTypeRef);
        if (fromExplicitImport != null) {
            String fqn = fromExplicitImport + typeRef.substring(firstDot);
            if (qualifieds.contains(fqn)) return LombokInternalAliasing.processAliases(fqn);
            // ... and if 'import foobar.Getter;' is in the source file, the answer is no.
            return null;
        }

        // When asking if 'Getter' could possibly be referring to 'lombok.Getter' and 'import lombok.*; / package lombok;' isn't in the source file. the answer is no.
        for (String qualified : qualifieds) {
            String pkgName = qualified.substring(0, qualified.length() - typeRef.length() - 1);
            if (!imports.hasStarImport(pkgName)) continue;

            // Now the hard part: Given that there is a star import, 'Getter' most likely refers to 'lombok.Getter', but type shadowing may occur in which case it doesn't.
            LombokNode<?, ?, ?> n = context;

            mainLoop:
            while (n != null) {
                if (n.getKind() == Kind.TYPE && firstTypeRef.equals(n.getName())) {
                    // Our own class or one of our outer classes is named 'typeRef' so that's what 'typeRef' is referring to, not one of our type library classes.
                    return null;
                }

                if (n.getKind() == Kind.STATEMENT || n.getKind() == Kind.LOCAL) {
                    LombokNode<?, ?, ?> newN = n.directUp();
                    if (newN == null) break mainLoop;

                    if (newN.getKind() == Kind.STATEMENT || newN.getKind() == Kind.INITIALIZER || newN.getKind() == Kind.METHOD) {
                        for (LombokNode<?, ?, ?> child : newN.down()) {
                            // We found a method local with the same name above our code. That's the one 'typeRef' is referring to, not
                            // anything in the type library we're trying to find, so, no matches.
                            if (child.getKind() == Kind.TYPE && firstTypeRef.equals(child.getName())) return null;
                            if (child == n) break;
                        }
                    }
                    n = newN;
                    continue mainLoop;
                }

                if (n.getKind() == Kind.TYPE || n.getKind() == Kind.COMPILATION_UNIT) {
                    for (LombokNode<?, ?, ?> child : n.down()) {
                        // Inner class that's visible to us has 'typeRef' as name, so that's the one being referred to, not one of our type library classes.
                        if (child.getKind() == Kind.TYPE && firstTypeRef.equals(child.getName())) return null;
                    }
                }

                n = n.directUp();
            }

            // If no shadowing thing has been found, the star import 'wins', so, return that.
            return LombokInternalAliasing.processAliases(qualified);
        }

        // No star import matches either.
        return null;
    }
}
