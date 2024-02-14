package com.github.ixanadu13.annotation.processor.shade.lombok.javac;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

/**
 * This class stores any reported errors as {@code CompilerMessage} objects and supports removing some of these.
 * Currently this class is only used for testing purposes.
 */
public class CapturingDiagnosticListener implements DiagnosticListener<JavaFileObject> {
    private final File file;
    private final Collection<CompilerMessage> messages;

    public CapturingDiagnosticListener(File file, Collection<CompilerMessage> messages) {
        this.file = file;
        this.messages = messages;
    }

    @Override public void report(Diagnostic<? extends JavaFileObject> d) {
        String msg = d.getMessage(Locale.ENGLISH);
        Matcher m = Pattern.compile(
                "^" + Pattern.quote(file.getAbsolutePath()) +
                        "\\s*:\\s*\\d+\\s*:\\s*(?:warning:\\s*)?(.*)$", Pattern.DOTALL).matcher(msg);
        if (m.matches()) msg = m.group(1);
        if (msg.equals("deprecated item is not annotated with @Deprecated")) {
            // This is new in JDK9; prior to that you don't see this. We shall ignore these.
            return;
        }
        messages.add(new CompilerMessage(d.getLineNumber(), d.getStartPosition(), d.getKind() == Kind.ERROR, msg));
    }

    public void suppress(int start, int end) {
        Iterator<CompilerMessage> it = messages.iterator();
        while (it.hasNext()) {
            long pos = it.next().getPosition();
            if (pos >= start && pos < end) it.remove();
        }
    }

    public static final class CompilerMessage {
        /** Line Number (starting at 1) */
        private final long line;

        private final long position;
        private final boolean isError;
        private final String message;

        public CompilerMessage(long line, long position, boolean isError, String message) {
            this.line = line;
            this.position = position;
            this.isError = isError;
            this.message = message;
        }

        public long getLine() {
            return line;
        }

        public long getPosition() {
            return position;
        }

        public boolean isError() {
            return isError;
        }

        public String getMessage() {
            return message;
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (isError ? 1231 : 1237);
            result = prime * result + (int) (line ^ (line >>> 32));
            result = prime * result + ((message == null) ? 0 : message.hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            CompilerMessage other = (CompilerMessage) obj;
            if (isError != other.isError) return false;
            if (line != other.line) return false;
            if (message == null) {
                if (other.message != null) return false;
            } else if (!message.equals(other.message)) return false;
            return true;
        }

        @Override public String toString() {
            return String.format("%d %s %s", line, isError ? "ERROR" : "WARNING", message);
        }
    }
}