package com.github.ixanadu13.annotation.processor.shade.lombok.javac;

import java.lang.annotation.Annotation;

import com.github.ixanadu13.annotation.processor.shade.lombok.AnnotationValues;
import com.github.ixanadu13.annotation.processor.shade.lombok.SpiLoadUtil;

import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;

/**
 * Implement this interface if you want to be triggered for a specific annotation.
 *
 * You MUST replace 'T' with a specific annotation type, such as:
 *
 * {@code public class HandleGetter extends JavacAnnotationHandler<Getter>}
 *
 * Because this generics parameter is inspected to figure out which class you're interested in.
 *
 * You also need to register yourself via SPI discovery as being an implementation of {@code JavacAnnotationHandler}.
 */
public abstract class JavacAnnotationHandler<T extends Annotation> {
    protected Trees trees;

    /**
     * Called when an annotation is found that is likely to match the annotation you're interested in.
     *
     * Be aware that you'll be called for ANY annotation node in the source that looks like a match. There is,
     * for example, no guarantee that the annotation node belongs to a method, even if you set your
     * TargetType in the annotation to methods only.
     *
     * @param annotation The actual annotation - use this object to retrieve the annotation parameters.
     * @param ast The javac AST node representing the annotation.
     * @param annotationNode The Lombok AST wrapper around the 'ast' parameter. You can use this object
     * to travel back up the chain (something javac AST can't do) to the parent of the annotation, as well
     * as access useful methods such as generating warnings or errors focused on the annotation.
     */
    public abstract void handle(AnnotationValues<T> annotation, JCAnnotation ast, JavacNode annotationNode);

    /**
     * This handler is a handler for the given annotation; you don't normally need to override this class
     * as the annotation type is extracted from your {@code extends EclipseAnnotationHandler<AnnotationTypeHere>}
     * signature.
     */
    @SuppressWarnings("unchecked") public Class<T> getAnnotationHandledByThisHandler() {
        return (Class<T>) SpiLoadUtil.findAnnotationClass(getClass(), JavacAnnotationHandler.class);
    }

    public void setTrees(Trees trees) {
        this.trees = trees;
    }
}
