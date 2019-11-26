package ru.curoviyxru.phoenix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;
import ru.curoviyxru.j2vk.TextUtil;
import ru.curoviyxru.j2vk.HTTPClient;
import ru.curoviyxru.j2vk.platform.Charset;

/**
 *
 * @author curoviyxru
 */
public class Localization {

    public static class LocaleEntry {

        public String code, name, version;
        
        public void write(DataOutputStream os) throws Exception {
            os.writeUTF(code == null ? "null" : code);
            os.writeUTF(name == null ? "null" : name);
            os.writeUTF(version == null ? "null" : version);
        }
        
        public void read(DataInputStream is) throws Exception {
            code = is.readUTF();
            name = is.readUTF();
            version = is.readUTF();
        }
    }
    public static LocaleEntry[] avaliable;
    public static Localization locale;
    public Hashtable table = new Hashtable();
    
    public Localization(String file) {
        parse(file);
    }

    public String getString(String key) {
        if (key == null) {
            return null;
        }
        String s = (String) table.get(key);
        if (s == null) {
            //System.out.println("Warning: key not found " + key);
            return key;
        }
        return s;
    }

    private void parse(String file) {
        if (file == null) {
            return;
        }
        try {
            Vector strs = TextUtil.split(file, "\n");
            for (int i = 0; i < strs.size(); i++) {
                String str = (String) strs.elementAt(i);
                if (str == null || str.startsWith("#")) {
                    continue;
                }
                int index = str.indexOf('=');
                if (index == -1) {
                    continue;
                }
                String value = str.substring(index + 1);
                String key = str.substring(0, str.length() - 1 - value.length()).trim();

                //if (table.containsKey(key)) {
                //    System.out.println("Warning: duplicated key " + key);
                //}
                table.put(key, value.trim());
            }
        } catch (OutOfMemoryError er) {
            table = new Hashtable();
            System.gc();
        }
    }

    public static LocaleEntry[] avaliable() {
        if (avaliable != null) {
            return avaliable;
        }
        Vector ava = new Vector();
        try {
            String strs = new String(HTTPClient.readStream(Runtime.getRuntime().getClass().getResourceAsStream("/lang/list.langs")), Charset.current);
            Vector sp = TextUtil.split(strs, "\n");
            for (int i = 0; i < sp.size(); i++) {
                String sss = (String) sp.elementAt(i);
                if (sss == null || sss.startsWith("#")) {
                    continue;
                }
                Vector s = TextUtil.split(sss, ";");
                LocaleEntry item = new LocaleEntry();
                if (s.size() > 2) {
                    item.version = ((String) s.elementAt(2)).trim();
                    item.name = ((String) s.elementAt(1)).trim();
                    item.code = ((String) s.elementAt(0)).trim();
                }
                if (!TextUtil.isNullOrEmpty(item.code) && exists(item.code)) {
                    ava.addElement(item);
                }
            }
            avaliable = new LocaleEntry[ava.size()];
            ava.copyInto(avaliable);
        } catch (Exception e) {
            avaliable = new LocaleEntry[0];
            System.gc();
        }
        return avaliable;
    }

    public static String get(String key) {
        if (key == null) {
            return key;
        }
        return locale == null ? key : locale.getString(key);
    }

    public static String get(String key, String replacement) {
        return get(key, replacement, null);
    }
    
    public static String get(String key, String replacement, String replacement2) {
        if (key == null) {
            return key;
        }
        String g = get(key);
        if (replacement == null) {
            return g;
        }
        g = TextUtil.easyReplace(g, "%%", replacement);
        if (replacement2 == null) {
            return g;
        }
        g = TextUtil.easyReplace(g, "%%", replacement2);
        return g;
    }

    private static boolean exists(String language_code) {
        try {
            InputStream s = Runtime.getRuntime().getClass().getResourceAsStream("/lang/" + language_code + ".lang");
            s.close();
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public static String getSystem() {
        String microLocale = System.getProperty("microedition.locale");
        if (microLocale == null) {
            return "en";
        }

        LocaleEntry[] v = avaliable();
        for (int i = 0; i < v.length; ++i) {
            LocaleEntry item = v[i];
            if (item != null && item.code != null && microLocale.indexOf(item.code) != -1) {
                return item.code;
            }
        }

        return "en";
    }
    
    public static void loadSelected(String code) {
        try {
            locale = new Localization(new String(HTTPClient.readStream(Runtime.getRuntime().getClass().getResourceAsStream("/lang/" + code + ".lang")), Charset.current));
        } catch (Throwable e) {
            Logger.l(e);
            System.gc();
            locale = new Localization(null);
            //Midlet.instance.config.localeCode = getSystem();
        }
    }
}
