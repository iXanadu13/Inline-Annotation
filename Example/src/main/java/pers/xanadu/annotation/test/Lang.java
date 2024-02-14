package pers.xanadu.annotation.test;

import pers.xanadu.annotation.Inline;
import pers.xanadu.annotation.test.subpkg.OtherClass;

public class Lang {
    @Inline
    public static void info(String str){
        System.out.println(str);
    }
    @Inline
    public static Object newInstance(){
        return new OtherClass();
    }
}
