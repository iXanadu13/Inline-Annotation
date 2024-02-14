package pers.xanadu.annotation.processor.shade.lombok.javac;

import java.lang.reflect.Method;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;

import pers.xanadu.annotation.processor.util.Permit;

// Supports JDK6-9
public class PackageName {
    private static final Method packageNameMethod = getPackageNameMethod();

    private static Method getPackageNameMethod() {
        try {
            return Permit.getMethod(JCCompilationUnit.class, "getPackageName");
        } catch (Exception e) {
            return null;
        }
    }

    public static String getPackageName(JCCompilationUnit cu) {
        JCTree t = getPackageNode(cu);
        return t != null ? t.toString() : null;
    }

    public static JCTree getPackageNode(JCCompilationUnit cu) {
        if (packageNameMethod != null) try {
            Object pkg = packageNameMethod.invoke(cu);
            return (pkg instanceof JCFieldAccess || pkg instanceof JCIdent) ? (JCTree) pkg : null;
        } catch (Exception e) {}
        return cu.pid instanceof JCFieldAccess || cu.pid instanceof JCIdent ? cu.pid : null;
    }
}
