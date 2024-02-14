package pers.xanadu.annotation.processor.shade.lombok.javac;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

import pers.xanadu.annotation.processor.shade.lombok.*;
import pers.xanadu.annotation.processor.shade.lombok.configuration.ConfigurationKeysLoader;

import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

/**
 * This class tracks 'handlers' and knows how to invoke them for any given AST node.
 *
 * This class can find the handlers (via SPI discovery) and will set up the given AST node, such as
 * building an AnnotationValues instance.
 */
public class HandlerLibrary {
    private final TypeLibrary typeLibrary = new TypeLibrary();
    private final Map<String, List<AnnotationHandlerContainer<?>>> annotationHandlers = new HashMap<String, List<AnnotationHandlerContainer<?>>>();
    private final Collection<VisitorContainer> visitorHandlers = new ArrayList<VisitorContainer>();
    private final Messager messager;

    /**
     * Creates a new HandlerLibrary that will report any problems or errors to the provided messager.
     * You probably want to use {@link #load(Messager, Trees)} instead.
     */
    public HandlerLibrary(Messager messager) {
        ConfigurationKeysLoader.LoaderLoader.loadAllConfigurationKeys();
        this.messager = messager;
    }

    private static class VisitorContainer {
        private final JavacASTVisitor visitor;
        private final long priority;
        private final boolean resolutionResetNeeded;

        VisitorContainer(JavacASTVisitor visitor) {
            this.visitor = visitor;
            HandlerPriority hp = visitor.getClass().getAnnotation(HandlerPriority.class);
            this.priority = hp == null ? 0L : (((long) hp.value()) << 32) + hp.subValue();
            this.resolutionResetNeeded = visitor.getClass().isAnnotationPresent(ResolutionResetNeeded.class);
        }

        public long getPriority() {
            return priority;
        }

        public boolean isResolutionResetNeeded() {
            return resolutionResetNeeded;
        }
    }

    private static class AnnotationHandlerContainer<T extends Annotation> {
        private final JavacAnnotationHandler<T> handler;
        private final Class<T> annotationClass;
        private final long priority;
        private final boolean resolutionResetNeeded;
        private final boolean evenIfAlreadyHandled;

        AnnotationHandlerContainer(JavacAnnotationHandler<T> handler, Class<T> annotationClass) {
            this.handler = handler;
            this.annotationClass = annotationClass;
            HandlerPriority hp = handler.getClass().getAnnotation(HandlerPriority.class);
            this.priority = hp == null ? 0L : (((long) hp.value()) << 32) + hp.subValue();
            this.resolutionResetNeeded = handler.getClass().isAnnotationPresent(ResolutionResetNeeded.class);
            this.evenIfAlreadyHandled = handler.getClass().isAnnotationPresent(AlreadyHandledAnnotations.class);
        }

        public void handle(final JavacNode node) {
            handler.handle(JavacHandlerUtil.createAnnotation(annotationClass, node), (JCAnnotation) node.get(), node);
        }

        public long getPriority() {
            return priority;
        }

        public boolean isResolutionResetNeeded() {
            return resolutionResetNeeded;
        }

        public boolean isEvenIfAlreadyHandled() {
            return evenIfAlreadyHandled;
        }
    }

    private SortedSet<Long> priorities;
    private SortedSet<Long> prioritiesRequiringResolutionReset;

    public SortedSet<Long> getPriorities() {
        return priorities;
    }

    public SortedSet<Long> getPrioritiesRequiringResolutionReset() {
        return prioritiesRequiringResolutionReset;
    }

    private void calculatePriorities() {
        SortedSet<Long> set = new TreeSet<Long>();
        SortedSet<Long> resetNeeded = new TreeSet<Long>();
        for (List<AnnotationHandlerContainer<?>> containers : annotationHandlers.values()) {
            for (AnnotationHandlerContainer<?> container : containers) {
                set.add(container.getPriority());
                if (container.isResolutionResetNeeded()) resetNeeded.add(container.getPriority());
            }
        }
        for (VisitorContainer container : visitorHandlers) {
            set.add(container.getPriority());
            if (container.isResolutionResetNeeded()) resetNeeded.add(container.getPriority());
        }
        this.priorities = Collections.unmodifiableSortedSet(set);
        this.prioritiesRequiringResolutionReset = Collections.unmodifiableSortedSet(resetNeeded);
    }

    /**
     * Creates a new HandlerLibrary that will report any problems or errors to the provided messager,
     * then uses SPI discovery to load all annotation and visitor based handlers so that future calls
     * to the handle methods will defer to these handlers.
     */
    public static HandlerLibrary load(Messager messager, Trees trees) {
        HandlerLibrary library = new HandlerLibrary(messager);

        try {
            loadAnnotationHandlers(library, trees);
            loadVisitorHandlers(library, trees);
        } catch (IOException e) {
            System.err.println("Lombok isn't running due to misconfigured SPI files: " + e);
        }

        library.calculatePriorities();

        return library;
    }

    /** Uses SPI Discovery to find implementations of {@link JavacAnnotationHandler}. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void loadAnnotationHandlers(HandlerLibrary lib, Trees trees) throws IOException {
        //No, that seemingly superfluous reference to JavacAnnotationHandler's classloader is not in fact superfluous!
        for (JavacAnnotationHandler handler : SpiLoadUtil.findServices(JavacAnnotationHandler.class, JavacAnnotationHandler.class.getClassLoader())) {
            handler.setTrees(trees);
            Class<? extends Annotation> annotationClass = handler.getAnnotationHandledByThisHandler();
            AnnotationHandlerContainer<?> container = new AnnotationHandlerContainer(handler, annotationClass);
            String annotationClassName = container.annotationClass.getName().replace("$", ".");
            List<AnnotationHandlerContainer<?>> list = lib.annotationHandlers.get(annotationClassName);
            if (list == null) lib.annotationHandlers.put(annotationClassName, list = new ArrayList<AnnotationHandlerContainer<?>>(1));
            list.add(container);
            lib.typeLibrary.addType(container.annotationClass.getName());
        }
    }

    /** Uses SPI Discovery to find implementations of {@link JavacASTVisitor}. */
    private static void loadVisitorHandlers(HandlerLibrary lib, Trees trees) throws IOException {
        //No, that seemingly superfluous reference to JavacASTVisitor's classloader is not in fact superfluous!
        for (JavacASTVisitor visitor : SpiLoadUtil.findServices(JavacASTVisitor.class, JavacASTVisitor.class.getClassLoader())) {
            visitor.setTrees(trees);
            lib.visitorHandlers.add(new VisitorContainer(visitor));
        }
    }

    /** Generates a warning in the Messager that was used to initialize this HandlerLibrary. */
    public void javacWarning(String message) {
        javacWarning(message, null);
    }

    /** Generates a warning in the Messager that was used to initialize this HandlerLibrary. */
    public void javacWarning(String message, Throwable t) {
        messager.printMessage(Diagnostic.Kind.WARNING, message + (t == null ? "" : (": " + t)));
    }

    /** Generates an error in the Messager that was used to initialize this HandlerLibrary. */
    public void javacError(String message) {
        javacError(message, null);
    }

    /** Generates an error in the Messager that was used to initialize this HandlerLibrary. */
    public void javacError(String message, Throwable t) {
        messager.printMessage(Diagnostic.Kind.ERROR, message + (t == null ? "" : (": " + t)));
        if (t != null) t.printStackTrace();
    }

    private boolean checkAndSetHandled(JCTree node) {
        return !JavacAugments.JCTree_handled.getAndSet(node, true);
    }

    /**
     * Handles the provided annotation node by first finding a qualifying instance of
     * {@link JavacAnnotationHandler} and if one exists, calling it with a freshly cooked up
     * instance of {@link lombok.core.AnnotationValues}.
     *
     * Note that depending on the printASTOnly flag, the {@link lombok.core.PrintAST} annotation
     * will either be silently skipped, or everything that isn't {@code PrintAST} will be skipped.
     *
     * The HandlerLibrary will attempt to guess if the given annotation node represents a lombok annotation.
     * For example, if {@code lombok.*} is in the import list, then this method will guess that
     * {@code Getter} refers to {@code lombok.Getter}, presuming that {@link lombok.javac.handlers.HandleGetter}
     * has been loaded.
     *
     * @param unit The Compilation Unit that contains the Annotation AST Node.
     * @param node The Lombok AST Node representing the Annotation AST Node.
     * @param annotation 'node.get()' - convenience parameter.
     */
    public void handleAnnotation(JCCompilationUnit unit, JavacNode node, JCAnnotation annotation, long priority) {
        TypeResolver resolver = new TypeResolver(node.getImportList());
        String rawType = annotation.annotationType.toString();
        String fqn = resolver.typeRefToFullyQualifiedName(node, typeLibrary, rawType);
        if (fqn == null) return;
        List<AnnotationHandlerContainer<?>> containers = annotationHandlers.get(fqn);
        if (containers == null) return;

        for (AnnotationHandlerContainer<?> container : containers) {
            try {
                if (container.getPriority() == priority) {
                    if (checkAndSetHandled(annotation)) {
                        container.handle(node);
                    } else {
                        if (container.isEvenIfAlreadyHandled()) container.handle(node);
                    }
                }
            } catch (AnnotationValues.AnnotationValueDecodeFail fail) {
                fail.owner.setError(fail.getMessage(), fail.idx);
            } catch (Throwable t) {
                String sourceName = "(unknown).java";
                if (unit != null && unit.sourcefile != null) sourceName = unit.sourcefile.getName();
                javacError(String.format("Lombok annotation handler %s failed on " + sourceName, container.handler.getClass()), t);
            }
        }
    }

    /**
     * Will call all registered {@link JavacASTVisitor} instances.
     */
    public void callASTVisitors(JavacAST ast, long priority) {
        for (VisitorContainer container : visitorHandlers) try {
            if (container.getPriority() == priority) ast.traverse(container.visitor);
        } catch (Throwable t) {
            javacError(String.format("Lombok visitor handler %s failed", container.visitor.getClass()), t);
        }
    }
}
