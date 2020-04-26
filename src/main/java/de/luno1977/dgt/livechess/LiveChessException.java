package de.luno1977.dgt.livechess;

import java.util.ArrayList;
import java.util.List;

public class LiveChessException extends RuntimeException {
    public LiveChessException(String msg, Exception e) {
        super(msg, e);
    }

    public LiveChessException(String msg, String printedStackTrace) {
        super(msg, convert(printedStackTrace));
    }

    static Throwable convert(String printedStackTrace) {
        String[] split = printedStackTrace.split("\\n");
        FromBoard fromBoard = new FromBoard(split[0]);

        List<StackTraceElement> stackTraceElements = new ArrayList<>();
        if (split.length > 1) {
            for (int i=1; i < split.length; i++) {
                if (split[i].startsWith("\tat ")) {
                    String remaining = split[i].substring(split[i].indexOf("at ")+3);
                    int lastIndexOfOpenBracket = remaining.lastIndexOf("(");
                    int lastIndexOfClosingBracket = remaining.lastIndexOf(")");
                    String classAndMethod = remaining.substring(0, lastIndexOfOpenBracket);
                    int lastDotInClassAndMethod = classAndMethod.lastIndexOf(".");
                    String declaringClass = classAndMethod.substring(0, lastDotInClassAndMethod);
                    String methodName = classAndMethod.substring(lastDotInClassAndMethod+1);
                    String fileNameAndLineNumber = remaining.substring(lastIndexOfOpenBracket+1, lastIndexOfClosingBracket);
                    int doublePointAt = fileNameAndLineNumber.lastIndexOf(":");
                    if (doublePointAt > 0) {
                        String fileName = fileNameAndLineNumber.substring(0, doublePointAt);
                        int lineNumber = Integer.parseInt(fileNameAndLineNumber.substring(doublePointAt+1));
                        stackTraceElements.add(new StackTraceElement(declaringClass, methodName, fileName, lineNumber));
                    } else {
                        stackTraceElements.add(new StackTraceElement(declaringClass, methodName, null,0));
                    }
                } else {
                    //Don't go further with converting.
                    break;
                }
            }
        }

        fromBoard.setStackTrace(stackTraceElements.toArray(new StackTraceElement[0]));
        return fromBoard;
    }

    private static class FromBoard extends Exception {
        public FromBoard(String msg) {
            super(msg);
        }
    }
}
