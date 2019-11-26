package ru.curoviyxru.phoenix;

import java.io.OutputStream;
import javax.microedition.io.Connector;
import ru.curoviyxru.j2vk.ILogger;
import ru.curoviyxru.j2vk.TextUtil;
import ru.curoviyxru.j2vk.platform.Charset;
import ru.curoviyxru.phoenix.ui.AppCanvas;

/**
 *
 * @author curoviyxru
 */
public class Logger implements ILogger {
    
    static StringBuffer buf = new StringBuffer();
    static boolean enabled = false;

    public static void clear() {
        buf.setLength(0);
        System.gc();
    }
    
    public void log(String s) {
        l(s);
    }
    
    public void log(Throwable s) {
        l(s);
    }
    
    public static void l(Throwable s) {
        if (s != null) {
            s.printStackTrace();
            l(s.toString() + ": " + s.getMessage());
        }
    }
    
    public static void l(String s) {
        if (s == null) return;
        try {
            if (enabled) {
                s = "[" + TextUtil.getTimeString(System.currentTimeMillis() / 1000, true) + "] " + s;
                buf.append(s).append('\n');
                System.out.println(s);
            }
        } catch (OutOfMemoryError e) {
            clear();
        }
    }

    public static void flushToFile(String path) {
        try {
            String filename = System.currentTimeMillis() + "-vk4me.log";
            javax.microedition.io.file.FileConnection file = (javax.microedition.io.file.FileConnection) Connector.open(path + filename, Connector.READ_WRITE);
            if (!file.exists()) file.create();
            OutputStream os = file.openOutputStream();
            os.write(buf.toString().getBytes(Charset.current));
            buf.setLength(0);
            os.close();
            file.close();
            AppCanvas.instance.dropMessage(Localization.get("general.message"), Localization.get("general.logSaved", filename));
        } catch (Throwable e) {
            AppCanvas.instance.dropError(e);
        }
    }
}
