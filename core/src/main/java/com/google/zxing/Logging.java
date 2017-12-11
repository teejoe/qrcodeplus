package com.google.zxing;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Logging {
    public static final class LEVELS {
        public static final int DEBUG = 0;
        public static final int INFO = 1;
        public static final int ERROR = 2;
        public static final int FATAL = 3;
        public static final int DISABLED = 4;
    }

    private static int sLevel = LEVELS.DISABLED;

    private static String TAG_PREFIX = "m2x_log_";

    public static void enableLog(boolean enable) {
        if (enable) {
            sLevel = LEVELS.DEBUG;
        } else {
            sLevel = LEVELS.DISABLED;
        }
    }

    public static String getStackTrace(Throwable throwable) {
        final StringWriter result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        result.flush();
        return result.toString();
    }

    public static String getMethodName(final int depth) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();

        //System. out.println(ste[ste.length-depth].getClassName()+"#"+ste[ste.length-depth].getMethodName());
        // return ste[ste.length - depth].getMethodName();  //Wrong, fails for depth = 0
        return ste[2 + depth].getMethodName(); //Thank you Tom Tresansky
    }

    public static void logWithMethodName(String msg) {
        Log.d(TAG_PREFIX, "\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Log.d(TAG_PREFIX, getMethodName(2) + "() " + msg);
        logStackTrace(5);
    }

    public static void logStackTrace(int depth) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        for (int i = 0; i < depth; i++) {
            Log.d(TAG_PREFIX, ste[4 + i].getClassName() + "." + ste[4 + i].getMethodName() + "()");
        }
    }

    public static final String getTag() {
        StackTraceElement[] cause = Thread.currentThread().getStackTrace();
        StringBuilder tag = new StringBuilder(TAG_PREFIX);
        for (int i = 1; i < cause.length; i++) {
            if (cause[i - 1].getClassName().equals(Logging.class.getName())
                    && !cause[i].getClassName().equals(Logging.class.getName())) {
                tag.append(cause[i].getClassName().replaceAll("^.*\\.", ""));
                break;
            }
        }
        return tag.toString();
    }


    /**
     * Debug level logger
     */
    public static final void d(String s) {
        if (isLevelLogging(LEVELS.DEBUG)) {
            Log.d(getTag(), s);
        }
    }

    /**
     * Info level logger
     */
    public static final void i(String s) {
        if (isLevelLogging(LEVELS.INFO)) {
            Log.i(getTag(), s);
        }
    }
    
    /*
     * There is no warning level since warnings are supposed to be treated as errors
     */

    /**
     * Error level logger
     */
    public static final void e(String s) {
        if (isLevelLogging(LEVELS.ERROR)) {
            Log.e(getTag(), s);
        }
    }

    public static void setLogLevel(int level) {
        sLevel = level;
    }

    public static boolean isLevelLogging(int level) {
        // only debug mode allowed
        return sLevel <= level;
    }

    public static void logStackTrace(Throwable throwable) {
        if (isLevelLogging(LEVELS.ERROR)) {
            final StringWriter result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            throwable.printStackTrace(printWriter);
            result.flush();
            e(result.toString());
        }
    }

    public static void logStackTop(Throwable throwable) {
        if (throwable.getStackTrace().length > 0) {
            d(throwable.getStackTrace()[0].toString());
        }
    }

    public static void d(String tag, String msg) {
        d("" + tag + ": " + msg);
    }

    public static void e(String tag, String msg) {
        e("" + tag + ": " + msg);
    }
}
