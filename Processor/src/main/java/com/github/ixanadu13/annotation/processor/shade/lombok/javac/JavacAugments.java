package com.github.ixanadu13.annotation.processor.shade.lombok.javac;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCImport;

public final class JavacAugments {
    private JavacAugments() {
        // Prevent instantiation
    }

    public static final FieldAugment<JCTree, Boolean> JCTree_handled = FieldAugment.augment(JCTree.class, boolean.class, "lombok$handled");
    public static final FieldAugment<JCTree, JCTree> JCTree_generatedNode = FieldAugment.circularSafeAugment(JCTree.class, JCTree.class, "lombok$generatedNode");
    public static final FieldAugment<JCImport, Boolean> JCImport_deletable = FieldAugment.circularSafeAugment(JCImport.class, Boolean.class, "lombok$deletable");
    public static final FieldAugment<JCTree, Boolean> JCTree_keepPosition = FieldAugment.augment(JCTree.class, boolean.class, "lombok$keepPosition");
}
