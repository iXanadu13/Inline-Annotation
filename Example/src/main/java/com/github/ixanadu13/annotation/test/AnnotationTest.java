package com.github.ixanadu13.annotation.test;

import com.github.ixanadu13.annotation.InlineAt;
import com.github.ixanadu13.annotation.Inline;
import com.github.ixanadu13.annotation.test.subpkg.OtherClass;

import java.util.*;

import static com.github.ixanadu13.annotation.test.Lang.newInstance;
import static com.github.ixanadu13.annotation.test.conflict.Lang.*;
import static com.github.ixanadu13.annotation.test.subpkg.OtherClass.*;

public class AnnotationTest {

    public int all_statement_test(){
        StringBuilder sb = new StringBuilder("1");
        String string = sb.append("2").toString();
        @InlineAt int value = AnnotationTest.to_be_inlined(string);
        return value;
    }

    @Inline
    public static int to_be_inlined(String str){
        if (str.equals("1")) return 1;
        else if (str.equals("2")) return 2;
        else{
            if (str.equals("3")){
                str = str.replace("3","replaced");
                return 3;
            }
            switch (str){
                case "4": return 4;
                case "5": return 5;
                default: {
                    for (int i=6;i<=10;++i){
                        String str_i = String.valueOf(i);
                        if (str_i.equals(str)){
                            try{
                                return Integer.parseInt(str_i);
                            }catch (Throwable throwable){
                                return -1;
                            }
                        }
                    }
                    //JCBlock
                    {
                        //JCLabeledStatement
                        label1: if ("11".equals(str)) return 11;
                        label2:{
                            if ("12".equals(str)) return 12;
                            else break label2;
                        }
                        int i = 13;
                        while (i++<15){
                            if (String.valueOf(i).equals(str)) return i;
                        }
                    }
                    List<Integer> list = new ArrayList<>();
                    Collections.addAll(list,15,16,17,18);
                    for (Integer integer : list){
                        if (integer.toString().equals(str)) return integer;
                    }
                    int j = 19;
                    do {
                        if (str.equals(String.valueOf(j))) return j;
                        ++j;
                    }while (j<25);
                    synchronized (AnnotationTest.class){
                        try{
                            return Integer.parseInt(str);
                        }catch (Throwable ignored){}
                        finally {
                            return -1;
                        }
                    }
                }
            }
        }
    }

    @Inline
    private static void doSomething(){
        StringBuilder sb = new StringBuilder();
        if (new Random().nextBoolean()) sb.append("123");
        else sb.append("456");
    }
    @Inline
    public static String test(String str){
        if(str.equals("testtesttest")) return "op";
        if(str.equals("???")) return null;
        return "123";
    }
    public static int outer_class_test(){
        @InlineAt String string = AnnotationTest.test("123");

        // specified className in @InlineAt
        // or add import com.github.ixanadu13.annotation.test.Lang;
        @InlineAt("com.github.ixanadu13.annotation.test.Lang")
        Object test1 = Lang.info("123");

        //require: import static com.github.ixanadu13.annotation.test.Lang.list;
        //because Lang.list is not specified in function staticImportTest()
        @InlineAt("com.github.ixanadu13.annotation.test.Lang")
        Object test2 = Lang.staticImportTest();

        //require: import static com.github.ixanadu13.annotation.test.subpkg.OtherClass.*;
        //because OtherClass.mp is not specified in function method_other_okg_test()
        //"Lang" can be omitted because it has been specified in @InlineAt
        @InlineAt("com.github.ixanadu13.annotation.test.Lang")
        Object obj = newInstance();

        @InlineAt
        Object test3 = OtherClass.method_other_okg_test();

        test(string);
        return 1;
    }

    public void innerClass_test(){
        @InlineAt("com.github.ixanadu13.annotation.test.AnnotationTest.InnerClass")
        Object inline = InnerClass.static_test();
    }
    @Inline
    public int recursive(int i0){
        if(i0==5) return -1;
        recursive(i0-1);
        return 0;
    }
    @Inline
    public TestRet recursive_test(){
        @InlineAt int res = recursive(1);
        return null;
    }
    public static class InnerClass{
        @Inline
        public void test(){

        }
        @Inline
        public static void static_test(){
            new InnerClass().test();
        }
    }
}