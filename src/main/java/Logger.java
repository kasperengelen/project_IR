
/**
 * Class that provides logging facilities.
 */
public class Logger
{
    /**
     * Log an error message to {@link System#err} with prefix "[ERROR]".
     * @param msg The message used by {@link String#format(String, Object...)}.
     * @param args The arguments for the message used by {@link String#format(String, Object...)}.
     */
    public static void logError(String msg, Object... args)
    {
        System.err.println(String.format("[ERROR] " + msg, args));
    }

    /**
     * Log a debug message to {@link System#out} with prefix "[DEBUG]".
     * @param msg The message used by {@link String#format(String, Object...)}.
     * @param args The arguments for the message used by {@link String#format(String, Object...)}.
     */
    public static void logDebug(String msg, Object... args)
    {
        System.out.println(String.format("[DEBUG] " + msg, args));
    }

    /**
     * Print a message to {@link System#out}.
     * @param msg The message used by {@link String#format(String, Object...)}.
     * @param args The arguments for the message used by {@link String#format(String, Object...)}.
     */
    public static void logOut(String msg, Object... args)
    {
        System.out.println(String.format(msg, args));
    }
}
