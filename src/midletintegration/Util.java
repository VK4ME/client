package midletintegration;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import ru.curoviyxru.j2vk.platform.Charset;

/**
 * Part of MIDlet Integration library
 * 
 * @author Shinovon
 * 
 */
public class Util {
	
	public static Hashtable parseArgs(String str) {
		if(str == null) {
			return null;
		}
		Hashtable ht = new Hashtable();
		int index = str.indexOf(';');
		boolean b = true;
		while (b) {
			if (index == -1) {
				b = false;
				index = str.length();
			}
			String token = str.substring(0, index).trim();
			int index2 = token.indexOf("=");
			ht.put(token.substring(0, index2).trim(), token.substring(index2 + 1));
			if (b) {
				str = str.substring(index + 1);
				index = str.indexOf(';');
			}
		}
		return ht;
	}
	
	public static String decodeURL(String s) {
		if(s == null) {
			return null;
		}
		boolean needToChange = false;
		int numChars = s.length();
		StringBuffer sb = new StringBuffer(numChars > 500 ? numChars / 2 : numChars);
		int i = 0;
		char c;
		byte[] bytes = null;
		while (i < numChars) {
			c = s.charAt(i);
			switch (c) {
			case '%':
				try {
					if (bytes == null)
						bytes = new byte[(numChars - i) / 3];
					int pos = 0;
					while (((i + 2) < numChars) && (c == '%')) {
						int v = Integer.parseInt(s.substring(i + 1, i + 3), 16);
						if (v < 0)
							throw new IllegalArgumentException();
						bytes[pos++] = (byte) v;
						i += 3;
						if (i < numChars)
							c = s.charAt(i);
					}
					if ((i < numChars) && (c == '%'))
						throw new IllegalArgumentException();
					sb.append(new String(bytes, 0, pos, Charset.current));
				} catch (UnsupportedEncodingException e) {
					throw new IllegalArgumentException();
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException();
				}
				needToChange = true;
				break;
			default:
				sb.append(c);
				i++;
				break;
			}
		}
		return (needToChange ? sb.toString() : s);
	}
	
	public static String encodeURL(String s) {
		if(s == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			int c = chars[i];
			if (65 <= c && c <= 90) {
				sb.append((char) c);
			} else if (97 <= c && c <= 122) {
				sb.append((char) c);
			} else if (48 <= c && c <= 57) {
				sb.append((char) c);
			} else if (c == ' ') {
				sb.append("%20");
			} else if (c == 45 || c == 95 || c == 46 || c == 33 || c == 126 || c == 42 || c == 39 || c == 40
					|| c == 41) {
				sb.append((char) c);
			} else if (c <= 127) {
				sb.append(hex(c));
			} else if (c <= 2047) {
				sb.append(hex(0xC0 | c >> 6));
				sb.append(hex(0x80 | c & 0x3F));
			} else {
				sb.append(hex(0xE0 | c >> 12));
				sb.append(hex(0x80 | c >> 6 & 0x3F));
				sb.append(hex(0x80 | c & 0x3F));
			}
		}
		return sb.toString();
	}

	private static String hex(int i) {
		String s = Integer.toHexString(i);
		return "%" + (s.length() < 2 ? "0" : "") + s;
	}
}
