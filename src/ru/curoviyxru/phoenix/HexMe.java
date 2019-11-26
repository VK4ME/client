package ru.curoviyxru.phoenix;

/**
 *
 * @author curoviyxru
 */
public class HexMe {

    private static final byte[] EBD = {
        (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G',
        (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N',
        (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U',
        (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z',
        (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g',
        (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n',
        (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u',
        (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z',
        (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5',
        (byte) '6', (byte) '7', (byte) '8', (byte) '9', 
        (byte) '-', //or '+' (standard impl)
        (byte) '_'  //or '/' (standard impl)
    };
    private static final byte[] DBD = {
        80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80,
        80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80,
        80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 62, 80, 62, 80, 63,
        52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 80, 80, 80, 64, 80, 80,
        80, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 80, 80, 80, 80, 63,
        80, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
        41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 80, 80, 80, 80, 80
    };
    
    public static final byte[] basify(byte[] data) {
        byte[] ar = new byte[(data.length / 3 + (data.length % 3 != 0 ? 1 : 0)) * 4];
        int ii = 0;
        for (int i = 0; i < data.length; i += 3) {
            if (i + 2 >= data.length) {
                if (i + 1 >= data.length) {
                    ar[ii++] = EBD[(data[i] & 0xff) >>> 2];
                    ar[ii++] = EBD[((data[i] & 0x03) << 4)];
                    ar[ii++] = '=';
                    ar[ii++] = '=';
                    continue;
                }
                ar[ii++] = EBD[(data[i] & 0xff) >>> 2];
                ar[ii++] = EBD[((data[i] & 0x03) << 4) | ((data[i + 1] & 0xf0) >>> 4)];
                ar[ii++] = EBD[((data[i + 1] & 0x0f) << 2)];
                ar[ii++] = '=';
                continue;
            }
            ar[ii++] = EBD[(data[i] & 0xff) >>> 2];
            ar[ii++] = EBD[((data[i] & 0x03) << 4) | ((data[i + 1] & 0xf0) >>> 4)];
            ar[ii++] = EBD[((data[i + 1] & 0x0f) << 2) | ((data[i + 2] & 0xc0) >>> 6)];
            ar[ii++] = EBD[(data[i + 2] & 0x3f)];
        }
        return ar;
    }

    public static final byte[] unbasify(byte[] data) {
        if (data.length == 0) {
            return data;
        }
        if (data.length % 4 != 0) {
            return null;
        }
        byte[] ar = new byte[data.length / 4 * 3 - (data[data.length - 1] == (byte) '=' ? 1 : 0) - (data[data.length - 2] == (byte) '=' ? 1 : 0)];
        int ii = 0;
        for (int i = 0; i < data.length; i += 4) {
            if (data[i + 3] == '=') {
                if (data[i + 2] == '=') {
                    ar[ii++] = (byte) (DBD[data[i]] << 2 | ((DBD[data[i + 1]] & 0xF0) >>> 4));
                    continue;
                }
                ar[ii++] = (byte) (DBD[data[i]] << 2 | ((DBD[data[i + 1]] & 0xF0) >>> 4));
                ar[ii++] = (byte) (((DBD[data[i + 1]] & 0x0F) << 4) | ((DBD[data[i + 2]] & 0x3C) >>> 2));
                continue;
            }
            ar[ii++] = (byte) (((DBD[data[i]]) << 2) | ((DBD[data[i + 1]] & 0xF0) >>> 4));
            ar[ii++] = (byte) (((DBD[data[i + 1]] & 0x0F) << 4) | ((DBD[data[i + 2]] & 0x3C) >>> 2));
            ar[ii++] = (byte) (((DBD[data[i + 2]] & 0x03) << 6) | ((DBD[data[i + 3]] & 0x3F)));
        }
        return ar;
    }
}
