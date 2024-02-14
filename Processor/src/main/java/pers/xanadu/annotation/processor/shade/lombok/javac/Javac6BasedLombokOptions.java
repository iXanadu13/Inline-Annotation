package pers.xanadu.annotation.processor.shade.lombok.javac;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Options;
import pers.xanadu.annotation.processor.util.Permit;

public class Javac6BasedLombokOptions extends LombokOptions {
    private static final Method optionName_valueOf;
    private static final Method options_put;

    static {
        try {
            Class<?> optionNameClass = Class.forName("com.sun.tools.javac.main.OptionName");
            optionName_valueOf = Permit.getMethod(optionNameClass, "valueOf", String.class);
            options_put = Permit.getMethod(Class.forName("com.sun.tools.javac.util.Options"), "put", optionNameClass, String.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't initialize Javac6-based lombok options due to reflection issue.", e);
        }
    }

    public static Javac6BasedLombokOptions replaceWithDelombokOptions(Context context) {
        Options options = instance(context);
        context.put(optionsKey, (Options)null);
        Javac6BasedLombokOptions result = new Javac6BasedLombokOptions(context);
        result.putAll(options);
        return result;
    }

    private Javac6BasedLombokOptions(Context context) {
        super(context);
    }

    @Override public void putJavacOption(String optionName, String value) {
        try {
            Permit.invoke(options_put, this, Permit.invoke(optionName_valueOf, null, optionName), value);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Can't initialize Javac6-based lombok options due to reflection issue.", e);
        } catch (InvocationTargetException e) {
            //throw Lombok.sneakyThrow(e.getCause());
            throw Permit.sneakyThrow(e.getCause());
        }
    }
}
