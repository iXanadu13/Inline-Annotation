package pers.xanadu.annotation.test;

import pers.xanadu.annotation.Inline;
import pers.xanadu.annotation.test.subpkg.OtherClass;

import java.util.ArrayList;
import java.util.List;

public class Lang {
    public static final List<String> list = new ArrayList<>();
    @Inline
    public static void info(String str){
        System.out.println(str);
    }
    @Inline
    public static Object newInstance(){
        return new OtherClass();
    }
    @Inline
    public static void staticImportTest(){
        Lang.list.add("Xanadu13");
    }
}
