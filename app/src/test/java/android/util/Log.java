package android.util;

/**
 * This approach copied from
 * https://stackoverflow.com/questions/36787449/how-to-mock-method-e-in-log
 */
public class Log {
    public static int v(String tag, String msg) {
        System.err.println("VERBOSE: " + tag + ": " + msg);
        return 0;
    }

    public static int d(String tag, String msg) {
        System.err.println("DEBUG: " + tag + ": " + msg);
        return 0;
    }

    public static int i(String tag, String msg) {
        System.err.println("INFO: " + tag + ": " + msg);
        return 0;
    }

    public static int w(String tag, String msg) {
        System.err.println("WARN: " + tag + ": " + msg);
        return 0;
    }

    public static int w(String tag, String msg, Throwable ex) {
        w(tag, msg);
        ex.printStackTrace(System.err);
        return 0;
    }

    public static int w(String tag, Throwable ex) {
        w(tag, "");
        ex.printStackTrace(System.err);
        return 0;
    }

    public static int e(String tag, String msg) {
        System.err.println("ERROR: " + tag + ": " + msg);
        return 0;
    }

    public static int e(String tag, String msg, Throwable ex) {
        e(tag, msg);
        ex.printStackTrace(System.err);
        return 0;
    }

    public static boolean isLoggable(String tag, int level) {
        return true;
    }
    // add other methods if required...
}
