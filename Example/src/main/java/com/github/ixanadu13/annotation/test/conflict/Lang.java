package com.github.ixanadu13.annotation.test.conflict;

import com.github.ixanadu13.annotation.Inline;

public class Lang {
    @Inline
    public static void info(String... str){
        System.out.println(str);
        System.out.println("conflict.Lang.info()");
    }
}
