package pers.xanadu.annotation.processor.shade.lombok.javac;

import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Options;

public class LombokOptionsFactory {
    enum LombokOptionCompilerVersion {
        JDK7_AND_LOWER {
            @Override
            LombokOptions createAndRegisterOptions(Context context) {
                return Javac6BasedLombokOptions.replaceWithDelombokOptions(context);
            }
        },

        JDK8 {
            @Override LombokOptions createAndRegisterOptions(Context context) {
                return Javac8BasedLombokOptions.replaceWithDelombokOptions(context);
            }
        },

        JDK9 {
            @Override LombokOptions createAndRegisterOptions(Context context) {
                return Javac9BasedLombokOptions.replaceWithDelombokOptions(context);
            }
        };

        abstract LombokOptions createAndRegisterOptions(Context context);
    }

    public static LombokOptions getDelombokOptions(Context context) {
        Options rawOptions = Options.instance(context);
        if (rawOptions instanceof LombokOptions) return (LombokOptions) rawOptions;

        LombokOptions options;
        if (Javac.getJavaCompilerVersion() < 8) {
            options = LombokOptionCompilerVersion.JDK7_AND_LOWER.createAndRegisterOptions(context);
        } else if (Javac.getJavaCompilerVersion() == 8) {
            options = LombokOptionCompilerVersion.JDK8.createAndRegisterOptions(context);
        } else {
            options = LombokOptionCompilerVersion.JDK9.createAndRegisterOptions(context);
        }
        return options;
    }
}