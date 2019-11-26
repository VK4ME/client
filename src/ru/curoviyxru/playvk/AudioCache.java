package ru.curoviyxru.playvk;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import ru.curoviyxru.j2vk.api.objects.attachments.Audio;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.FilePicker;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class AudioCache {

    public static boolean folderExists() {
        return PlayerWrapper.folderExists(Midlet.instance.config.caching_folder);
    }

    public static String get(Audio a) {
        if (a == null || !folderExists()) {
            return null;
        }
        String path = Midlet.instance.config.caching_folder;
        if (!path.endsWith(FilePicker.SEPARATOR)) {
            path = path + FilePicker.SEPARATOR;
        }
        path = path + getName(a) + ".mp3";
        return path;
    }

    public static boolean has(Audio a) {
        if (a == null || !folderExists()) {
            return false;
        }
        String path = get(a);
        try {
            FileConnection conn = (FileConnection) Connector.open(path, Connector.READ);
            boolean e = conn.exists() && conn.canRead();
            conn.close();
            return e;
        } catch (Exception e) {
            return false;
        }
    }

    private static String getName(Audio a) {
        if (a == null) {
            return null;
        }
        String s = cleanNLimit(a.artist) + " - " + cleanNLimit(a.title) + " - " + getID(a);
        if (s.length() <= 46) {
            return s;
        }
        s = cleanNLimit(a.title) + " - " + getID(a);
        if (s.length() <= 46) {
            return s;
        }
        return getID(a);
    }

    private static String getID(Audio a) {
        return toDAE(a.owner_id) + "_" + toDAE(a.id);
    }

    private static String cleanNLimit(String title) {
        if (title == null) {
            return null;
        }
        char[] chs = title.toCharArray();
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < chs.length; i++) {
            char c = chs[i];
            if (Character.isDigit(c) || Character.isLowerCase(c) || Character.isUpperCase(c)) {
                b.append(c);
            } else {
                b.append(' ');
            }
        }
        if (b.length() > 20) {
            b.setLength(20);
        }
        return b.toString().trim();
    }

    public static String toDAE(long l) {
        if (l == 0) {
            return "0";
        }
        StringBuffer b = new StringBuffer();
        boolean minus = l < 0;
        l = Math.abs(l);
        while (l > 0) {
            long r = l % 62;
            l /= 62;
            if (r < 10) {
                b.append((char) ('0' + r));
            } else if (r < 36) {
                b.append((char) ('a' + r - 10));
            } else {
                b.append((char) ('A' + r - 36));
            }
        }
        if (minus) {
            b.append('-');
        }
        b.reverse();

        return b.toString();
    }
}
