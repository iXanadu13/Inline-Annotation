package pers.xanadu.annotation.test.subpkg;

import pers.xanadu.annotation.Inline;

import java.util.HashMap;
import java.util.Map;

public class OtherClass {
    public static final Map<String,String> mp = new HashMap<>();
    @Inline
    public static void method_other_okg_test(){
        mp.put("foo","123");
    }
}
