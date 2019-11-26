package ru.curoviyxru.phoenix.kernel;

import java.util.Hashtable;

/**
 *
 * @author curoviyxru
 */
public class ProgressKernel {

    private static Hashtable progresses = new Hashtable();

    public static void addProvider(FocusedProgressProvider provider) {
        if (provider == null) {
            return;
        }
        progresses.put(provider.getName(), provider);
    }

    public static FocusedProgressProvider getProvider(String id) {
        if (id == null) {
            return null;
        }
        return (FocusedProgressProvider) progresses.get(id);
    }

    public static void deleteProvider(FocusedProgressProvider provider) {
        if (provider == null) {
            return;
        }
        deleteProvider(provider.getName());
    }

    public static void deleteProvider(String s) {
        if (s == null) {
            return;
        }
        progresses.remove(s);
    }

    public static boolean hasProvider(String s) {
        if (s == null) {
            return false;
        }
        return progresses.containsKey(s);
    }
}
