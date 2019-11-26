package ru.curoviyxru.phoenix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.Vector;
import ru.curoviyxru.j2vk.HTTPClient;
import ru.curoviyxru.j2vk.TextUtil;
import ru.curoviyxru.j2vk.platform.Charset;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.AnimationInterp;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.RenderUtil;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class Theming {

     public static class ThemeEntry {

        public String name = "null", version = "0.0.1";
        
        public void write(DataOutputStream os) throws Exception {
            os.write("VK4ME Theme\n".getBytes(Charset.current));
            os.write(((name != null ? name.trim() : "null") + '\n').getBytes(Charset.current));
            os.write(((version != null ? version.trim() : "0.0.1") + '\n').getBytes(Charset.current));
        }
        
        public void read(DataInputStream is) throws Exception {
            StringBuffer buf = new StringBuffer();
            int c;
            while ((c = is.read()) != '\n') {
                buf.append((char) c);
            }
            if (!buf.toString().trim().equals("VK4ME Theme")) throw new IllegalArgumentException("This is not a theme");
            buf.setLength(0);
            while ((c = is.read()) != '\n') {
                buf.append((char) c);
            }
            name = buf.toString().trim();
            buf.setLength(0);
            while ((c = is.read()) != '\n') {
                buf.append((char) c);
            }
            version = buf.toString().trim();
        }
    }
    public static ThemeEntry[] avaliable;
    public static AnimationInterp interp = new AnimationInterp(AnimationInterp.INTERP_SIN);
    public static Theme now;
    public static int[] from, to;

    public static void changeTheme(Theme newTheme) {
        if (now == null) {
            now = newTheme;
            return;
        }
        
        interp.endValue = 255;
        interp.startValue = 0;

        if (Midlet.instance.config.gui_animations) {
            interp.start(2000);
        } else {
            interp.value = 255;
        }

        from = now.writeTheme();
        to = newTheme.writeTheme();
        updateTheme();
    }

    public static void updateTheme() {
        if (interp.update()) {
            now.readTheme(to);
            from = null;
            to = null;
            interp.end();
        } else {
            int[] temp = new int[100];
            int val = interp.value;
            for (int i = 0; i < temp.length; ++i) {
                temp[i] = RenderUtil.mix(from[i], to[i], val);
            }
            now.readTheme(temp);
        }

        AppCanvas.instance.render();
    }
    
    private static boolean exists(String language_code) {
        try {
            InputStream s = Runtime.getRuntime().getClass().getResourceAsStream("/themes/" + language_code + ".theme");
            s.close();
            return true;
        } catch (Exception e) {
        }
        return false;
    }
    
    public static ThemeEntry[] avaliable() {
        if (avaliable != null) {
            return avaliable;
        }
        Vector ava = new Vector();
        try {
            String strs = new String(HTTPClient.readStream(Runtime.getRuntime().getClass().getResourceAsStream("/themes/list.themes")), Charset.current);
            Vector sp = TextUtil.split(strs, "\n");
            for (int i = 0; i < sp.size(); i++) {
                String sss = (String) sp.elementAt(i);
                if (sss == null || sss.startsWith("#")) {
                    continue;
                }
                Vector s = TextUtil.split(sss, ";");
                ThemeEntry item = new ThemeEntry();
                if (s.size() > 1) {
                    item.version = ((String) s.elementAt(1)).trim();
                    item.name = ((String) s.elementAt(0)).trim();
                }
                if (!TextUtil.isNullOrEmpty(item.name) && exists(item.name)) {
                    ava.addElement(item);
                }
            }
            avaliable = new ThemeEntry[ava.size()];
            ava.copyInto(avaliable);
        } catch (Exception e) {
            avaliable = new ThemeEntry[0];
            System.gc();
        }
        return avaliable;
    }
    
    public static String getStandard() {
        return "Classic Dark";
    }
    
    public static void loadSelected(String name, boolean fromRMS) {
        Theme th;
        
        try {
            if (!fromRMS) th = new Theme(HTTPClient.readStream(Runtime.getRuntime().getClass().getResourceAsStream("/themes/" + name + ".theme")));
            else th = new Theme(RmsController.openStore(name + "_theme"));
        } catch (Throwable e) {
            Logger.l(e);
            System.gc();
            th = new Theme();
        }
        
        changeTheme(th);
    }
}
