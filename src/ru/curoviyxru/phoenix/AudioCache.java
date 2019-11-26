package ru.curoviyxru.phoenix;

import ru.curoviyxru.j2vk.api.objects.attachments.Audio;
import ru.curoviyxru.phoenix.midlet.Midlet;

/**
 *
 * @author curoviyxru
 */
public class AudioCache {
    
    public static final String[] transTable = {
        "A", "B", "V", "G", "D", "E", "ZH", "Z", "I", "I", "K", "L", "M",
        "N", "O", "P", "R", "S", "T", "U", "F", "KH", "TS", "CH", "SH", "SHCH",
        "IE", "Y", "", "E", "IU", "IA",
        "a", "b", "v", "g", "d", "e", "zh", "z", "i", "i", "k", "l", "m",
        "n", "o", "p", "r", "s", "t", "u", "f", "kh", "ts", "ch", "sh", "shch",
        "ie", "y", "", "e", "iu", "ia"
    };
    
    public static String get(Audio a) {
        if (a == null) return null;
        return getName(a) + ".mp3";
    }
    
    private static String translit(String s) {
        if (!Midlet.instance.config.translitFiles) return s;
        if (s == null) return null;
        StringBuffer buffer = new StringBuffer();
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            char ch = chars[i];
            if (ch >= 'А' && ch <= 'я') buffer.append(transTable[ch - 'А']);
            else if (ch == 'ё') buffer.append('e');
            else if (ch == 'Ё') buffer.append('E');
            else buffer.append(ch);
        }
        return buffer.toString();
    }

    private static String getName(Audio a) {
        if (a == null) return null;
        String artist = translit(a.artist), 
                title = translit(a.title);
        String s = cleanNLimit(artist) + " - " + cleanNLimit(title) + " - " + getID(a);
        if (s.length() <= 46) return s;
        s = cleanNLimit(title) + " - " + getID(a);
        if (s.length() <= 46) return s;
        return getID(a);
    }

    private static String getID(Audio a) {
        return new String(HexMe.basify(new byte[] {
            (byte) (a.owner_id >>> 24),
            (byte) (a.owner_id >>> 16),
            (byte) (a.owner_id >>> 8),
            (byte) a.owner_id,
            (byte) (a.id >>> 24),
            (byte) (a.id >>> 16),
            (byte) (a.id >>> 8),
            (byte) a.id,
            (byte) 0
        }));
    }

    private static String cleanNLimit(String title) {
        if (title == null) return null;
        char[] chs = title.toCharArray();
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < chs.length; i++) {
            char c = chs[i];
            if (Character.isDigit(c) || Character.isLowerCase(c) || Character.isUpperCase(c)) {
                b.append(c);
            } else b.append(' ');
        }
        if (b.length() > 20) {
            b.setLength(20);
        }
        return b.toString().trim();
    }
}