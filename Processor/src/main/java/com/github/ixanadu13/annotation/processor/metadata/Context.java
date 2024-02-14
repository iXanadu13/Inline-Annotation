package com.github.ixanadu13.annotation.processor.metadata;

import com.sun.tools.javac.tree.JCTree;

import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;

public class Context {
    public final Element ancestor;
    public final java.util.List<String> prefix;
    public final Map<XMethodSymbol, JCTree.JCMethodDecl> mp;
    public Context(Element ancestor, List<String> prefix,Map<XMethodSymbol, JCTree.JCMethodDecl> mp){
        this.ancestor = ancestor;
        this.prefix = prefix;
        this.mp = mp;
    }
}
