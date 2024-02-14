package com.github.ixanadu13.annotation.processor.metadata;

import com.sun.tools.javac.tree.JCTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class XMethodSymbol {
    public final String class_name;
    public final String meth_name;
    public final XTypeSymbol ret;
    final List<XTypeSymbol> param;
    boolean implicit;

    public XMethodSymbol(String class_name, JCTree.JCMethodDecl jcMethod) {
        this.class_name = class_name;
        this.meth_name = jcMethod.getName().toString();
        this.ret = new XTypeSymbol(jcMethod.restype);
        this.param = new ArrayList<>();
        this.implicit = false;
        int size = jcMethod.getParameters().length();
        int i = 0;
        for (JCTree.JCVariableDecl variableDecl : jcMethod.getParameters()) {
            XTypeSymbol typeSymbol = new XTypeSymbol(variableDecl.vartype);
            this.param.add(typeSymbol);
            if (++i == size) {
                if (typeSymbol.type == XTypeSymbol.Type.ARRAY && variableDecl.toString().split(" ")[0].endsWith("...")) {
                    this.implicit = true;
                }
            }
        }
    }

//    /**
//     * 需要确保传入的methodInvocation是解析后的
//     * @param class_name 方法申明的类全限定名
//     * @param methodInvocation 此处的方法调用
//     */
//    public XMethodSymbol(String class_name, JCTree.JCMethodInvocation methodInvocation,JCTree.JCExpression restype){
//        this.class_name = class_name;
//        String[] splits = methodInvocation.meth.toString().split("\\.");
//        this.meth_name = splits[splits.length-1];
//        this.ret = new XTypeSymbol(restype);
//        this.param = new ArrayList<>();
//        this.implicit = false;
//        // TODO: 如果参数是...的,会出现问题
//        // TODO: 目前匹配的时候直接看类名和方法名，但一定有更好的方法
//        int size = methodInvocation.getArguments().length();
//        int i = 0;
//        for (JCTree.JCExpression expression : methodInvocation.getArguments()) {
//            XTypeSymbol typeSymbol = new XTypeSymbol(variableDecl.vartype);
//            this.param.add(typeSymbol);
//            if (++i == size) {
//                if (typeSymbol.type == XTypeSymbol.Type.ARRAY && variableDecl.toString().split(" ")[0].endsWith("...")) {
//                    this.implicit = true;
//                }
//            }
//        }
//    }

    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if(!(obj instanceof XMethodSymbol)) return false;
        XMethodSymbol rhs = (XMethodSymbol) obj;
        if(!Objects.equals(this.class_name, rhs.class_name)) return false;
        if(!Objects.equals(this.meth_name, rhs.meth_name)) return false;
        if(!Objects.equals(this.ret,rhs.ret)) return false;
        if(this.param.size() != rhs.param.size()) return false;
        int size = this.param.size();
        for(int i=0;i<size;i++){
            if(!this.param.get(0).equals(rhs.param.get(0))) return false;
        }
        return true;
    }
    @Override
    public int hashCode(){
        return toString().hashCode();
    }
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(ret.toString()).append(" ").append(class_name).append(".").append(meth_name).append("(");
        int size = param.size();
        for(int i=0;i<size;i++){
            sb.append(param.get(i));
            if(i<size-1) sb.append(",");
        }
        sb.append(")");
        return sb.toString();
    }
}
