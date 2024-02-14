package pers.xanadu.annotation.processor.metadata;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;

import java.util.Objects;

public class XTypeSymbol {
    Type type;
    TypeTag pri_ret;
    String ret;
    XTypeSymbol arr_element;

    private XTypeSymbol(){}
    public XTypeSymbol(JCTree.JCExpression restype) {
        if(restype instanceof JCTree.JCPrimitiveTypeTree){
            JCTree.JCPrimitiveTypeTree typeTree = (JCTree.JCPrimitiveTypeTree) restype;
            this.type = Type.PRIMITIVE;
            this.pri_ret = typeTree.typetag;
        }
        else if(restype instanceof JCTree.JCIdent){
            JCTree.JCIdent jcIdent = (JCTree.JCIdent) restype;
            withJCIdent(this,jcIdent);
        }
        else if(restype instanceof JCTree.JCFieldAccess){
            JCTree.JCFieldAccess jcFieldAccess = (JCTree.JCFieldAccess) restype;
            Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) jcFieldAccess.sym;
            this.type = Type.WRAPPED;
            this.ret = classSymbol.getQualifiedName().toString();
        }
        else if(restype instanceof JCTree.JCArrayTypeTree){
            JCTree.JCArrayTypeTree arrayTypeTree = (JCTree.JCArrayTypeTree) restype;
            this.arr_element = new XTypeSymbol(arrayTypeTree.elemtype);
            this.type = Type.ARRAY;
        }
        //有泛型
        else if(restype instanceof JCTree.JCTypeApply){
            JCTree.JCTypeApply jcTypeApply = (JCTree.JCTypeApply) restype;
            withJCIdent(this, (JCTree.JCIdent) jcTypeApply.clazz);
        }
        else {
            throw new RuntimeException(restype.getClass().toString());
            //throw new RuntimeException("Unknown method return type: "+restype);
        }
    }
    //AnnotatedTypeTree, ArrayTypeTree, IntersectionTypeTree, ParameterizedTypeTree,
    //PrimitiveTypeTree, UnionTypeTree

    public static XTypeSymbol fromType(com.sun.tools.javac.code.Type type){
        if(type.isPrimitiveOrVoid()){
            XTypeSymbol typeSymbol = new XTypeSymbol();
            typeSymbol.type = Type.PRIMITIVE;
            typeSymbol.pri_ret = type.getTag();
            return typeSymbol;
        }
        if(type instanceof com.sun.tools.javac.code.Type.ArrayType){
            com.sun.tools.javac.code.Type.ArrayType arrayType = (com.sun.tools.javac.code.Type.ArrayType) type;
            XTypeSymbol typeSymbol = new XTypeSymbol();
            typeSymbol.type = Type.ARRAY;
            typeSymbol.arr_element = fromType(arrayType.elemtype);
            return typeSymbol;
        }
        XTypeSymbol typeSymbol = new XTypeSymbol();
        typeSymbol.type = Type.WRAPPED;
        //去除泛型
        typeSymbol.ret = type.toString().split("<")[0];
        return typeSymbol;
    }
    private static void withJCIdent(XTypeSymbol self, JCTree.JCIdent jcIdent){
        Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) jcIdent.sym;
        self.type = Type.WRAPPED;
        self.ret = classSymbol.getQualifiedName().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof XTypeSymbol)) return false;
        XTypeSymbol rhs = (XTypeSymbol) obj;
        if(this.type != rhs.type) return false;
        if(this.type == Type.PRIMITIVE){
            return this.pri_ret == rhs.pri_ret;
        }
        else if(this.type == Type.WRAPPED){
            return Objects.equals(this.ret,rhs.ret);
        }
        return this.arr_element.equals(rhs.arr_element);
    }
    @Override
    public String toString(){
        if(this.type == Type.PRIMITIVE) return pri_ret.name();
        else if(this.type == Type.WRAPPED) return ret;
        return this.arr_element.toString()+"[]";
    }
    public enum Type{
        PRIMITIVE(0),
        WRAPPED(1),
        ARRAY(2);

        Type(int type) {

        }
        public static Type valueOf(int type){
            if(type == 0) return PRIMITIVE;
            else if(type == 1) return WRAPPED;
            return ARRAY;
        }
    }
}
