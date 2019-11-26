package ru.curoviyxru.phoenix.ui;

import java.util.Vector;
import ru.curoviyxru.j2vk.TextUtil;
import ru.curoviyxru.j2vk.platform.Charset;

/**
 *
 * @author curoviyxru
 */
public class SuperString {
    public byte[] types;
    public String[] entities;
    public boolean hasEmoji;
    public int length;
    
    public SuperString(String raw) {
        parse(raw);
    }
    
    //(\u00a9|\u00ae|[\u2000-\u3300]|\ud83c[\ud000-\udfff]|\ud83d[\ud000-\udfff]|\ud83e[\ud000-\udfff])
    //and if we have such emoji
    public void parse(String raw) {
        if (raw == null){
            types = new byte[0];
            entities = new String[0];
            hasEmoji = false;
            length = 0;
            return;
        }
        
        while (raw.indexOf("\u2026") != -1) {
            raw = TextUtil.easyReplace(raw, "\u2026", "...");
        }
        
        while (raw.indexOf("\u2013") != -1) {
            raw = TextUtil.easyReplace(raw, "\u2013", "-");
        }
        while (raw.indexOf("\uFE0F") != -1) {
            raw = TextUtil.easyReplace(raw, "\uFE0F", "");
        }
        
        char[] chars = raw.toCharArray();
        StringBuffer buffer = new StringBuffer();
        
        boolean hasEmojiV = false;
        Vector entitiesV = new Vector();
        Vector typesV = new Vector();
        
        boolean zxc = false;
        
        for (int i = 0; i < chars.length; ++i) {
            if (chars[i] == 0x200D) {
                if (buffer.length() != 0) {
                    entitiesV.addElement(buffer.toString());
                    typesV.addElement(Boolean.FALSE);
                    buffer.setLength(0);
                }
                if (!entitiesV.isEmpty() && typesV.lastElement().equals(Boolean.TRUE)) {
                    buffer.append(hexToString((String) entitiesV.lastElement()));
                    typesV.removeElementAt(typesV.size() - 1);
                    entitiesV.removeElementAt(entitiesV.size() - 1);
                }
                buffer.append(chars[i]);
                zxc = true;
            } else if (chars[i] == '\u00a9' || chars[i] == '\u00ae' || (chars[i] >= '\u2000' && chars[i] <= '\u3300' && chars[i] != '\u2116')) { //TODO: remove a lot non-emojis
                if (buffer.length() != 0 && !zxc) {
                    entitiesV.addElement(buffer.toString());
                    typesV.addElement(Boolean.FALSE);
                    buffer.setLength(0);
                }
                buffer.append(chars[i]);
                if (i + 2 < chars.length && chars[i + 1] == 0xD83C && chars[i + 2] >= 0xDFFB && chars[i + 2] <= 0xDFFF) {
                    buffer.append(chars[i + 1]);
                    buffer.append(chars[i + 2]);
                    i += 2;
                }
                entitiesV.addElement(stringToHex(buffer.toString()));
                typesV.addElement(Boolean.TRUE);
                buffer.setLength(0);
                zxc = false;
                hasEmojiV = true;
            } else if (i + 1 < chars.length && chars[i] >= '\ud83c' && chars[i] <= '\ud83e' && chars[i + 1] >= '\ud000' && chars[i + 1] <= '\udfff') {
                if (buffer.length() != 0 && !zxc) {
                    entitiesV.addElement(buffer.toString());
                    typesV.addElement(Boolean.FALSE);
                    buffer.setLength(0);
                }
                buffer.append(chars[i]);
                buffer.append(chars[i + 1]);
                if (i + 3 < chars.length && chars[i] == 0xD83C && chars[i + 1] >= 0xDDE6 && chars[i + 1] <= 0xDDFF && chars[i + 2] == 0xD83C && chars[i + 3] >= 0xDDE6 && chars[i + 3] <= 0xDDFF) {
                    buffer.append(chars[i + 2]);
                    buffer.append(chars[i + 3]);
                    i += 2;
                } else if (i + 3 < chars.length && chars[i + 2] == 0xD83C && chars[i + 3] >= 0xDFFB && chars[i + 3] <= 0xDFFF) {
                    buffer.append(chars[i + 2]);
                    buffer.append(chars[i + 3]);
                    i += 2;
                }
                entitiesV.addElement(stringToHex(buffer.toString()));
                typesV.addElement(Boolean.TRUE);
                buffer.setLength(0);
                zxc = false;
                hasEmojiV = true;
                i += 1;
            } else {
                buffer.append(chars[i]);
            }
        }
        
        if (buffer.length() != 0) {
            entitiesV.addElement(buffer.toString());
            typesV.addElement(Boolean.FALSE);
        }
        
        hasEmoji = hasEmojiV;
        types = new byte[typesV.size()];
        length = 0;
        entities = new String[entitiesV.size()];
        entitiesV.copyInto(entities);
        for (int i = 0; i < types.length; ++i) {
            types[i] = ((Boolean) typesV.elementAt(i)).booleanValue() ? (byte) 1 : 0;
            if (types[i] == 1) length += 1;
            else length += entities[i].length();
        }
    }
    
    public static String stringToHex(String str) {
        if (str == null) return null;
        byte[] hashInBytes;
        try {
            hashInBytes = str.getBytes(Charset.current);
        } catch (Exception ignored) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hashInBytes.length; i++) {
            String hex = Integer.toHexString(0xff & hashInBytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    
    public static String hexToString(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        try {
            return new String(data, Charset.current);
        } catch (Exception ignored) {
            return "";
        }
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < types.length; ++i) {
            if (types[i] == 1) {
                buf.append(hexToString(entities[i]));
            } else {
                buf.append(entities[i]);
            }
        }
        return buf.toString();
    }
    
    public SuperString deleteLast() {
        if (types.length > 0) {
            SuperString str = new SuperString(null);
            if (types[types.length - 1] == 1 || entities[types.length - 1].length() < 2) {
                str.entities = new String[types.length - 1];
                System.arraycopy(entities, 0, str.entities, 0, types.length - 1);
                str.types = new byte[types.length - 1];
                System.arraycopy(types, 0, str.types, 0, types.length - 1);
                str.hasEmoji = false;
                for (int i = 0; i < str.types.length; ++i) {
                    if (str.types[i] == 1) {
                        str.hasEmoji = true;
                        break;
                    }
                }
            } else {
                str.entities = new String[types.length];
                System.arraycopy(entities, 0, str.entities, 0, types.length);
                String lastEntry = str.entities[str.entities.length - 1];
                str.entities[str.entities.length - 1] = lastEntry.substring(0, lastEntry.length() - 1);
                str.types = new byte[types.length];
                System.arraycopy(types, 0, str.types, 0, types.length);
                str.hasEmoji = hasEmoji;
            }
            str.length = length - 1;
            return str;
        } else return this;
    }
}
