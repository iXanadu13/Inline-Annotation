package pers.xanadu.annotation.test;

import pers.xanadu.annotation.Inline;

public class Lang {
    @Inline
    public static void info(String str){
        System.out.println(str);
    }
}
