package pers.xanadu.annotation.test.conflict;

import pers.xanadu.annotation.Inline;

public class Lang {
    @Inline
    public static void info(String... str){
        System.out.println(str);
        System.out.println("conflict.Lang.info()");
    }
}
