package pers.xanadu.annotation.processor.shade.lombok.javac;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import pers.xanadu.annotation.processor.shade.lombok.javac.JavacTreeMaker.TypeTag;

import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

/**
 * Makes a copy of any AST node, with some exceptions.
 * Exceptions:<ul>
 * <li>The symbol ('sym') of a copied variable isn't copied.
 * <li>all labels are removed.
 * </ul>
 *
 * The purpose of this class is to make a copy, and then the copy is attributed (resolution info is added). These exceptions
 * are to work around apparent bugs (or at least inconsistencies) in javac sources.
 */
public class TreeMirrorMaker extends TreeCopier<Void> {
    private final IdentityHashMap<JCTree, JCTree> originalToCopy = new IdentityHashMap<JCTree, JCTree>();

    public TreeMirrorMaker(JavacTreeMaker maker, Context context) {
        super(maker.getUnderlyingTreeMaker());
    }

    @Override public <T extends JCTree> T copy(T original) {
        T copy = super.copy(original);
        putIfAbsent(originalToCopy, original, copy);
        return copy;
    }

    @Override public <T extends JCTree> T copy(T original, Void p) {
        T copy = super.copy(original, p);
        putIfAbsent(originalToCopy, original, copy);
        return copy;
    }

    @Override public <T extends JCTree> List<T> copy(List<T> originals) {
        List<T> copies = super.copy(originals);
        if (originals != null) {
            Iterator<T> it1 = originals.iterator();
            Iterator<T> it2 = copies.iterator();
            while (it1.hasNext()) putIfAbsent(originalToCopy, it1.next(), it2.next());
        }
        return copies;
    }

    @Override public <T extends JCTree> List<T> copy(List<T> originals, Void p) {
        List<T> copies = super.copy(originals, p);
        if (originals != null) {
            Iterator<T> it1 = originals.iterator();
            Iterator<T> it2 = copies.iterator();
            while (it1.hasNext()) putIfAbsent(originalToCopy, it1.next(), it2.next());
        }
        return copies;
    }

    public Map<JCTree, JCTree> getOriginalToCopyMap() {
        return Collections.unmodifiableMap(originalToCopy);
    }

    // Monitor the following issues when making changes here.
    // - https://github.com/projectlombok/lombok/issues/278
    // - https://github.com/projectlombok/lombok/issues/729
    @Override public JCTree visitVariable(VariableTree node, Void p) {
        JCVariableDecl original = node instanceof JCVariableDecl ? (JCVariableDecl) node : null;
        JCVariableDecl copy = (JCVariableDecl) super.visitVariable(node, p);
        if (original == null) return copy;
        copy.sym = original.sym;
        if (copy.sym != null) copy.type = original.type;
        if (copy.type != null) {
            boolean wipeSymAndType = copy.type.isErroneous();
            if (!wipeSymAndType) {
                TypeTag typeTag = TypeTag.typeTag(copy.type);
                wipeSymAndType = (Javac.CTC_NONE.equals(typeTag) || Javac.CTC_ERROR.equals(typeTag) || Javac.CTC_UNKNOWN.equals(typeTag) || Javac.CTC_UNDETVAR.equals(typeTag));
            }

            if (wipeSymAndType) {
                copy.sym = null;
                copy.type = null;
            } else {
                if (original.vartype != null) {
                    copy.vartype.type = original.vartype.type;
                    original.vartype.accept(new TreeScanner() {
                        @Override public void scan(JCTree tree) {
                            super.scan(tree);
                            originalToCopy.get(tree).type = tree.type;
                        }

                        @Override public void visitSelect(JCFieldAccess tree) {
                            super.visitSelect(tree);
                            ((JCFieldAccess) originalToCopy.get(tree)).sym = tree.sym;
                        }
                    });
                }
            }
        }

        return copy;
    }

    // Fix for NPE in HandleVal. See https://github.com/projectlombok/lombok/issues/372
    // This and visitVariable is rather hacky but we're working around evident bugs or at least inconsistencies in javac.
    @Override public JCTree visitLabeledStatement(LabeledStatementTree node, Void p) {
        return node.getStatement().accept(this, p);
    }

    private <K, V> void putIfAbsent(Map<K, V> map, K key, V value) {
        if (!map.containsKey(key)) {
            map.put(key, value);
        }
    }
}
