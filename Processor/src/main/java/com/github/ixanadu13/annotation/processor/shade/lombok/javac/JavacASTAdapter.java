package com.github.ixanadu13.annotation.processor.shade.lombok.javac;

import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

/**
 * Standard adapter for the {@link JavacASTVisitor} interface. Every method on that interface
 * has been implemented with an empty body. Override whichever methods you need.
 */
public class JavacASTAdapter implements JavacASTVisitor {
    /** {@inheritDoc} */
    @Override public void setTrees(Trees trees) {}

    /** {@inheritDoc} */
    @Override public void visitCompilationUnit(JavacNode top, JCCompilationUnit unit) {}

    /** {@inheritDoc} */
    @Override public void endVisitCompilationUnit(JavacNode top, JCCompilationUnit unit) {}

    /** {@inheritDoc} */
    @Override public void visitType(JavacNode typeNode, JCClassDecl type) {}

    /** {@inheritDoc} */
    @Override public void visitAnnotationOnType(JCClassDecl type, JavacNode annotationNode, JCAnnotation annotation) {}

    /** {@inheritDoc} */
    @Override public void endVisitType(JavacNode typeNode, JCClassDecl type) {}

    /** {@inheritDoc} */
    @Override public void visitField(JavacNode fieldNode, JCVariableDecl field) {}

    /** {@inheritDoc} */
    @Override public void visitAnnotationOnField(JCVariableDecl field, JavacNode annotationNode, JCAnnotation annotation) {}

    /** {@inheritDoc} */
    @Override public void endVisitField(JavacNode fieldNode, JCVariableDecl field) {}

    /** {@inheritDoc} */
    @Override public void visitInitializer(JavacNode initializerNode, JCBlock initializer) {}

    /** {@inheritDoc} */
    @Override public void endVisitInitializer(JavacNode initializerNode, JCBlock initializer) {}

    /** {@inheritDoc} */
    @Override public void visitMethod(JavacNode methodNode, JCMethodDecl method) {}

    /** {@inheritDoc} */
    @Override public void visitAnnotationOnMethod(JCMethodDecl method, JavacNode annotationNode, JCAnnotation annotation) {}

    /** {@inheritDoc} */
    @Override public void endVisitMethod(JavacNode methodNode, JCMethodDecl method) {}

    /** {@inheritDoc} */
    @Override public void visitMethodArgument(JavacNode argumentNode, JCVariableDecl argument, JCMethodDecl method) {}

    /** {@inheritDoc} */
    @Override public void visitAnnotationOnMethodArgument(JCVariableDecl argument, JCMethodDecl method, JavacNode annotationNode, JCAnnotation annotation) {}

    /** {@inheritDoc} */
    @Override public void endVisitMethodArgument(JavacNode argumentNode, JCVariableDecl argument, JCMethodDecl method) {}

    /** {@inheritDoc} */
    @Override public void visitLocal(JavacNode localNode, JCVariableDecl local) {}

    /** {@inheritDoc} */
    @Override public void visitAnnotationOnLocal(JCVariableDecl local, JavacNode annotationNode, JCAnnotation annotation) {}

    /** {@inheritDoc} */
    @Override public void endVisitLocal(JavacNode localNode, JCVariableDecl local) {}

    /** {@inheritDoc} */
    @Override public void visitTypeUse(JavacNode typeUseNode, JCTree typeUse) {}

    /** {@inheritDoc} */
    @Override public void visitAnnotationOnTypeUse(JCTree typeUse, JavacNode annotationNode, JCAnnotation annotation) {}

    /** {@inheritDoc} */
    @Override public void endVisitTypeUse(JavacNode typeUseNode, JCTree typeUse) {}

    /** {@inheritDoc} */
    @Override public void visitStatement(JavacNode statementNode, JCTree statement) {}

    /** {@inheritDoc} */
    @Override public void endVisitStatement(JavacNode statementNode, JCTree statement) {}
}
