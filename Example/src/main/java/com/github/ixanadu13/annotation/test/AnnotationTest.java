package com.github.ixanadu13.annotation.test;

import com.github.ixanadu13.annotation.InlineAt;
import com.github.ixanadu13.annotation.Inline;
import com.github.ixanadu13.annotation.test.subpkg.OtherClass;

import java.util.*;

import static com.github.ixanadu13.annotation.test.Lang.newInstance;
import static com.github.ixanadu13.annotation.test.conflict.Lang.*;
import static com.github.ixanadu13.annotation.test.subpkg.OtherClass.*;

public class AnnotationTest {
    private Object obj;
    public static void test2(Object arg){
        @InlineAt Object inline = AnnotationTest.doSomething();
        @InlineAt Object inline2 = AnnotationTest.doSomethingWithArgs("123");
        label1:{
            //AnnotationTest.test("this class");
        }
        //参数隐式转换推断(String -> String[])
        info("other class");//import static 推断
        Lang.info("name");//package
    }
    @Inline
    private static void doSomething(){
        StringBuilder sb = new StringBuilder();
        if (new Random().nextBoolean()) sb.append("123");
        else sb.append("456");
    }
    @Inline
    public static void doSomethingWithArgs(String str){
        StringBuilder sb2 = new StringBuilder(str);
        sb2.append("123");
    }
    //@Inline
    public static int gen(){
        @InlineAt String string = AnnotationTest.test("123");
        //require: import static com.github.ixanadu13.annotation.test.Lang.list;
        //because Lang.list is not specified in function staticImportTest()
        @InlineAt("com.github.ixanadu13.annotation.test.Lang")
        Object test = Lang.staticImportTest();
        //require: import static com.github.ixanadu13.annotation.test.subpkg.OtherClass.*;
        //because OtherClass.mp is not specified in function method_other_okg_test()
        @InlineAt
        Object test2 = OtherClass.method_other_okg_test();
        test(string);
        //test(test("嵌套"));
        Thread thread = new Thread(){
            @Override
            public void run(){

            }
        };
        return 1;
    }
    @Inline
    public static String test(String str){
        if(str.equals("testtesttest")) return "op";
        if(str.equals("???")) return null;
        return "123";
    }
    @Inline
    public void test2(){
        String test2 = "2";
        String modifier_test = null;
        this.virtual_method(10);
        if(new Random().nextBoolean()) return;
        test2 = "3";
        int x=666;
        switch_test(++x);
        Void void_test = null;
        String[][] str = null;
        TestRet.InnerType innerType = new TestRet.InnerType();
        Class clazz = null;
        String test = "123";
        Map<String,UUID> mp = new HashMap<>();
    }


    @Inline
    public static int switch_test(int x){
        String res = "123";
        switch (x){
            case 0: return 10;
            case 1: {
                res = "456";
                if(new Random().nextBoolean()) return 666;
                else break;
            }
            case 2: return 8;
            default: return 0;
        }
        test(res);
        return -1;
    }
    //@Inline
    private String synchronized_test(){
        // specified className in @InlineAt
        // or add import com.github.ixanadu13.annotation.test.Lang;
        @InlineAt("com.github.ixanadu13.annotation.test.Lang")
        Object test = Lang.info("123");
        //"Lang" can be omitted because it has been specified in @InlineAt
        @InlineAt("com.github.ixanadu13.annotation.test.Lang")
        Object obj = newInstance();
        synchronized (AnnotationTest.class){
            obj = new ArrayList<>();
            if(new Random().nextBoolean()) return "123";
        }
        return "456";
    }
    @Inline
    public String virtual_method(int i0){
        {
            String test = "abc";
        }
        String res = new String();
        //label1:
        for(int i=0;i<10;i++){
            if(i==5) {
                res = "yes";
                break;
            }
            else {
                res = "test";
            }
        }
        return res;
    }
    //@Inline
    public void test_multi_param(int i0,int i1,int i2){
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
    public TestRet testRet(){
        @InlineAt int res = recursive(1);
        test_multi_param(res,3,6);
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