package pers.xanadu.annotation.processor;

import javax.lang.model.element.TypeElement;

public class AsmUtil{
    public static String getType(Class<?> clazz){
        return clazz.getName().replace('.','/');
    }
    public static String getType(TypeElement element){
        return element.getQualifiedName().toString().replace('.','/');
    }
    public static String getSuperType(TypeElement element){
        return element.getSuperclass().toString().replace('.','/');
    }
    public static String getDescriptor(Class<?> clazz){
        if(clazz.isPrimitive()){
            if(clazz == void.class) return "V";
            else if(clazz == boolean.class) return "Z";
            else if(clazz == char.class) return "C";
            else if(clazz == byte.class) return "B";
            else if(clazz == short.class) return "S";
            else if(clazz == int.class) return "I";
            else if(clazz == float.class) return "F";
            else if(clazz == long.class) return "J";
            else if(clazz == double.class) return "D";
            else throw new IllegalArgumentException(clazz.getName());
        }
        else if(clazz.isArray()){
            return "["+getDescriptor(clazz.getComponentType());
        }
        else{
            return "L"+getType(clazz)+";";
        }
    }
    public static String getDescriptor(Class<?> ret,Class<?>... args){
        StringBuilder res = new StringBuilder("(");
        if(args != null){
            for(Class<?> clazz : args){
                res.append(getDescriptor(clazz));
            }
        }
        res.append(")").append(getDescriptor(ret));
        return res.toString();
    }
}
