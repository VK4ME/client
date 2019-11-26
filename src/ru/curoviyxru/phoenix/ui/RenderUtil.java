package ru.curoviyxru.phoenix.ui;

import ru.curoviyxru.phoenix.Theming;
import java.util.Hashtable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import ru.curoviyxru.phoenix.midlet.Midlet;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class RenderUtil {

    //optimize unused cache, messages
    //todo all bool ? str : str to bool ? int : int or to bool
    public static Hashtable iconsCache = new Hashtable();
    public static Image[] scrollBuffers;
    public static int sLength = -1;
    public static int cloudPadding = AppCanvas.instance.normalEmojiFont.height / 3;
    public static final int TL = 1, TR = 2, BL = 8, BR = 4, ALL = TL | TR | BL | BR;
    public static Image shadow;

    public static void init() {
        if (!Midlet.instance.config.gui_disableDimm && shadow == null) {
            shadow = AppCanvas.loadLocal("new/shadow.png");
        }
    }

    public static void clear() {
        iconsCache.clear();
        System.gc();
    }

    public static Image renderScroll(int color, int h, int opaq) {
        if (h <= 0) {
            h = 1;
        }
        int cLength = h * AppCanvas.instance.scrollWidth;
        if (scrollBuffers == null || sLength != cLength) {
            scrollBuffers = new Image[7];
            sLength = cLength;
        }
        if (scrollBuffers[opaq] == null) {
            int c = (255 * opaq / 10 / 2) << 24;
            c |= color & 0x00ffffff;

            int[] s = new int[sLength];
            for (int i = 0; i < s.length; i++) {
                s[i] = c;
            }
            scrollBuffers[opaq] = Image.createRGBImage(s, AppCanvas.instance.scrollWidth, h, true);
        }

        return scrollBuffers != null ? scrollBuffers[opaq] : null;
    }

    public static Image circlify(
            int w, int h, int cornerSize,
            int color1, int color2,
            int corners,
            boolean inner) {
        return circlify(w, h, cornerSize, color1, color2, corners, 0, h, inner);
    }

    public static Image circlify(
            int w, int h, int cornerSize,
            int color1, int color2,
            int corners,
            int gradY, int gradH,
            boolean inner) {
        if (!Midlet.instance.config.gui_drawGradients) color1 = color2 = mix(color1, color2, 128);
        if (w < 1 || h < 1 || cornerSize < 1) {
            return null;
        }

        if (inner) {
            color1 |= 0xff000000;
            color2 |= 0xff000000;
        }

        int[] rgb = new int[w * h];
        if (color1 == color2) {
            for (int i = 0; i < rgb.length; ++i) {
                rgb[i] = color1;
            }
        } else {
            for (int y = 0; y < h; ++y) {
                int mixed = mix(color1, color2, 255 * (y + gradY) / Math.max(1, (gradH - 1)));
                for (int x = 0; x < w; ++x) {
                    rgb[y * w + x] = mixed;
                }
            }
        }

        int top = Math.max((corners & 1), ((corners >> 1) & 1));
        int bottom = Math.max(((corners >> 2) & 1), ((corners >> 2) & 1));
        boolean onlyTopOrBottom = !(top > 0 && bottom > 0);
        boolean topLeft = (corners & TL) == TL;
        boolean topRight = (corners & TR) == TR;
        boolean bottomLeft = (corners & BL) == BL;
        boolean bottomRight = (corners & BR) == BR;

        if (onlyTopOrBottom) {
            cornerSize = Math.min(cornerSize, Math.min(w, h));
        } else {
            cornerSize = Math.min(cornerSize, Math.min(w / 2, h / 2));
        }

        int size2 = cornerSize * 2;
        int sizeSqr = cornerSize * cornerSize;
        int size2Sqr = sizeSqr * 4;

        for (int y = 0; y < cornerSize; y++) {
            int yD = (cornerSize - y) * (cornerSize - y);
            int y1D = yD * 4;
            int y2D = (size2 - 1 - y * 2) * (size2 - 1 - y * 2);

            for (int x = 0; x < cornerSize; x++) {
                int xD = (cornerSize - x) * (cornerSize - x);
                int x1D = xD * 4;
                int x2D = (size2 - 1 - x * 2) * (size2 - 1 - x * 2);

                int alpha = xD + yD >= sizeSqr ? 63 : 0;
                alpha += x1D + y2D >= size2Sqr ? 64 : 0;
                alpha += x2D + y1D >= size2Sqr ? 64 : 0;
                alpha += x2D + y2D >= size2Sqr ? 64 : 0;

                if (inner) {
                    alpha = 255 - alpha;
                }
                alpha <<= 24;

                int XX = x;
                int YY = y;
                if (topLeft) {
                    rgb[y * w + x] = alpha | (rgb[y * w + x] &= 0x00ffffff);
                }
                x = w - x - 1;
                if (topRight) {
                    rgb[y * w + x] = alpha | (rgb[y * w + x] &= 0x00ffffff);
                }
                y = h - y - 1;
                if (bottomRight) {
                    rgb[y * w + x] = alpha | (rgb[y * w + x] &= 0x00ffffff);
                }
                x = XX;
                if (bottomLeft) {
                    rgb[y * w + x] = alpha | (rgb[y * w + x] &= 0x00ffffff);
                }
                y = YY;
            }
        }

        return Image.createRGBImage(rgb, w, h, true);
    }

    public static Image circlify(int size, Image image, int corners) {
        if (size < 1) {
            return image;
        }
        if (image == null) {
            return null;
        }

        int w = image.getWidth();
        int h = image.getHeight();

        int[] rgb = new int[w * h];
        image.getRGB(rgb, 0, w, 0, 0, w, h);

        int top = Math.max((corners & 1), ((corners >> 1) & 1));
        int bottom = Math.max(((corners >> 2) & 1), ((corners >> 2) & 1));
        boolean onlyTopOrBottom = !(top > 0 && bottom > 0);
        boolean topLeft = (corners & TL) == TL;
        boolean topRight = (corners & TR) == TR;
        boolean bottomLeft = (corners & BL) == BL;
        boolean bottomRight = (corners & BR) == BR;

        if (onlyTopOrBottom) {
            size = Math.min(size, Math.min(w, h));
        } else {
            size = Math.min(size, Math.min(w / 2, h / 2));
        }

        int size2 = size * 2;
        int sizeSqr = size * size;
        int size2Sqr = sizeSqr * 4;

        for (int y = 0; y < size; y++) {
            int yD = (size - y) * (size - y);
            int y1D = yD * 4;
            int y2D = (size2 - 1 - y * 2) * (size2 - 1 - y * 2);

            for (int x = 0; x < size; x++) {
                int xD = (size - x) * (size - x);
                int x1D = xD * 4;
                int x2D = (size2 - 1 - x * 2) * (size2 - 1 - x * 2);

                int alpha = xD + yD >= sizeSqr ? 63 : 0;
                alpha += x1D + y2D >= size2Sqr ? 64 : 0;
                alpha += x2D + y1D >= size2Sqr ? 64 : 0;
                alpha += x2D + y2D >= size2Sqr ? 64 : 0;

                alpha = 255 - alpha;

                int XX = x;
                int YY = y;
                if (topLeft) {
                    int prevc = rgb[y * w + x];
                    rgb[y * w + x] = (((alpha * (prevc >>> 24)) / 255) << 24)
                            | (prevc & 0x00ffffff);
                }
                x = w - x - 1;
                if (topRight) {
                    int prevc = rgb[y * w + x];
                    rgb[y * w + x] = (((alpha * (prevc >>> 24)) / 255) << 24)
                            | (prevc & 0x00ffffff);
                }
                y = h - y - 1;
                if (bottomRight) {
                    int prevc = rgb[y * w + x];
                    rgb[y * w + x] = (((alpha * (prevc >>> 24)) / 255) << 24)
                            | (prevc & 0x00ffffff);
                }
                x = XX;
                if (bottomLeft) {
                    int prevc = rgb[y * w + x];
                    rgb[y * w + x] = (((alpha * (prevc >>> 24)) / 255) << 24)
                            | (prevc & 0x00ffffff);
                }
                y = YY;
            }
        }

        return Image.createRGBImage(rgb, w, h, true);
    }

    public static Image genBorderOutline(int size, int color) {
        if (size < 1) {
            return null;
        }

        int[] rgb = new int[size * size];
        color &= 0x00ffffff;
        int size2 = size * 2;
        int sizeSqr = size * size;

        for (int y = 0; y < size; y++) {
            int yy = y * size;

            int yD = (size - y) * (size - y);
            int y1D = yD * 4;
            int y2D = (size2 - 1 - y * 2) * (size2 - 1 - y * 2);

            for (int x = 0; x < size; x++) {
                int xD = (size - x) * (size - x);
                int x1D = xD * 4;
                int x2D = (size2 - 1 - x * 2) * (size2 - 1 - x * 2);

                int alpha = Math.abs(yD + xD - sizeSqr) <= size ? 63 : 0;
                alpha += Math.abs(y1D / 4 + x2D / 4 - sizeSqr) <= size ? 64 : 0;
                alpha += Math.abs(y2D / 4 + x1D / 4 - sizeSqr) <= size ? 64 : 0;
                alpha += Math.abs(y2D / 4 + x2D / 4 - sizeSqr) <= size ? 64 : 0;

                rgb[x + yy] = (alpha << 24) | color;
            }
        }

        return Image.createRGBImage(rgb, size, size, true);
    }

    public static void drawTextField(Graphics g, int x, int y, int w, int h, boolean sel, boolean isPopup) {
        //g.setColor(isPopup ? Theming.now.popupColor : Theming.now.backgroundColor);
        //g.fillRect(x, y, w, h);
        g.setColor(sel ? Theming.now.focusedBorderColor : Theming.now.borderColor);
        g.drawRect(x, y, w - 1, h - 1);
        
        Image borderImg = sel ? Field.borderImageSelected : Field.borderImage;
        
        if (borderImg != null) {
            g.setColor(isPopup ? Theming.now.popupColor : Theming.now.backgroundColor);
            
            g.fillRect(x, y, Field.circleSize, Field.circleSize);
            g.fillRect(x + w - Field.circleSize, y + h - Field.circleSize, Field.circleSize, Field.circleSize);
            g.fillRect(x + w - Field.circleSize, y, Field.circleSize, Field.circleSize);
            g.fillRect(x, y + h - Field.circleSize, Field.circleSize, Field.circleSize);
            
            g.drawImage(borderImg, x, y, Graphics.TOP | Graphics.LEFT);
            g.drawRegion(borderImg, 
                    0, 0, Field.circleSize, Field.circleSize, Sprite.TRANS_ROT90, 
                    x + w - Field.circleSize, y, 0);
            g.drawRegion(borderImg, 
                    0, 0, Field.circleSize, Field.circleSize, Sprite.TRANS_ROT180, 
                    x + w - Field.circleSize, y + h - Field.circleSize, 0);
            g.drawRegion(borderImg, 
                    0, 0, Field.circleSize, Field.circleSize, Sprite.TRANS_ROT270, 
                    x, y + h - Field.circleSize, 0);
        }
    }

    public static void fillRect(Graphics g, int objX, int objY, int objWidth, int objHeight, int color1, int color2) {
        int oldColor = g.getColor();
        if (!Midlet.instance.config.gui_drawGradients) {
            color1 = color2 = mix(color1, color2, 128);
        }
        if (color1 == color2) {
            g.setColor(color1);
            g.fillRect(objX, objY, objWidth, objHeight);
        } else {
            for (int y = 0; y < objHeight; y++) {
                g.setColor(mix(color1, color2, 255 * (y + 1) / objHeight));
                g.drawLine(objX, objY + y, objX + objWidth - 1, objY + y);
            }
        }
        g.setColor(oldColor);
    }

    public static Image resizeImage(Image sourceImage, int heightI) {
        if (sourceImage == null) {
            return null;
        }

        int imageWidth = sourceImage.getWidth();
        int imageHeight = sourceImage.getHeight();

        if (heightI <= 0) {
            return Image.createImage(1, 1);
        }
        /**
         * Buffer *
         */
        int[] rgbData = new int[imageWidth * imageHeight];
        /**
         * Fill buffer *
         */
        sourceImage.getRGB(rgbData, 0, imageWidth, 0, 0, imageWidth, imageHeight);

        return resizeImage(rgbData, imageWidth, imageHeight, heightI);
    }

    public static Image resizeImage(int[] rgbData, int imageWidth, int imageHeight, int heightI) {
        if (rgbData == null) {
            return null;
        }

        if (heightI <= 0 || imageWidth <= 0 || imageHeight <= 0) {
            return Image.createImage(1, 1);
        }

        int destHeight = heightI;
        int destWidth = imageWidth * heightI / Math.max(imageHeight, 1);
        
        if (destWidth <= 1 || destWidth <= 1) {
            return Image.createImage(1, 1);
        }

        int[] lines = new int[destWidth * imageHeight];
        int[] columns = new int[destWidth * destHeight];
        /**
         * Fast *
         */
        if (destWidth < imageWidth) {
            for (int y = 0; y < imageHeight; y++) { // trough all lines
                int srci = y * imageWidth; // index in old pix
                int desti = y * destWidth; // index in new pix
                int part = destWidth;
                int addon = 0, r = 0, g = 0, b = 0, a = 0;
                for (int x = 0; x < destWidth; x++) {
                    int total = imageWidth;
                    int R = 0, G = 0, B = 0, A = 0;
                    if (addon != 0) {
                        R = r * addon;
                        G = g * addon;
                        B = b * addon;
                        A = a * addon;
                        total -= addon;
                    }
                    while (0 < total) {
                        a = (rgbData[srci] >> 24) & 0xff;
                        r = (rgbData[srci] >> 16) & 0xff;
                        g = (rgbData[srci] >> 8) & 0xff;
                        b = rgbData[srci++] & 0xff;
                        if (total > part) {
                            R += r * part;
                            G += g * part;
                            B += b * part;
                            A += a * part;
                        } else {
                            R += r * total;
                            G += g * total;
                            B += b * total;
                            A += a * total;
                            addon = part - total;
                            lines[desti++] = ((R / imageWidth) << 16)
                                    | ((G / imageWidth) << 8)
                                    | (B / imageWidth)
                                    | ((A / imageWidth) << 24); // A??
                        }
                        total -= part;
                    }
                }
            }
        } else { /// destWidth > imageWidth
            for (int y = 0; y < imageHeight; y++) { // trough all lines
                int srci = y * imageWidth; // index in old pix
                int desti = y * destWidth; // index in new pix

                for (int x = 0; x < destWidth; x++) {
                    int srcxhq = (x << 8) * (imageWidth - 1) / (destWidth - 1);
                    int srcx = srcxhq >>> 8;
                    int srcinterp = srcxhq & 0xff;

                    int col0 = rgbData[srci + srcx];
                    if (srcx < imageWidth - 1) {
                        int col1 = rgbData[srci + srcx + 1];
                        col0 = mix(col0, col1, srcinterp);
                    }

                    //set new pixel
                    lines[desti++] = col0;
                }
            }
        }
        if (destHeight < imageHeight) {
            for (int x = 0; x < destWidth; x++) { // trough columns
                int linesi = x; // index in lines pix
                int desti = x; // index in new pix
                int part = destHeight;
                int addon = 0, r = 0, g = 0, b = 0, a = 0;
                for (int y = 0; y < destHeight; y++) {
                    int total = imageHeight;
                    int R = 0, G = 0, B = 0, A = 0;
                    if (addon != 0) {
                        R = r * addon;
                        G = g * addon;
                        B = b * addon;
                        A = a * addon;
                        total -= addon;
                    }
                    while (0 < total) {
                        a = (lines[linesi] >> 24) & 0xff;// may no rotate
                        //a = lines[i] & 0xff000000;
                        r = (lines[linesi] >> 16) & 0xff;
                        g = (lines[linesi] >> 8) & 0xff;
                        b = lines[linesi] & 0xff;
                        linesi += destWidth;
                        if (total > part) {
                            R += r * part;
                            G += g * part;
                            B += b * part;
                            A += a * part;
                        } else {
                            R += r * total;
                            G += g * total;
                            B += b * total;
                            A += a * total;
                            addon = part - total;
                            ///set new pixel
                            if (0 != A) {
                                columns[desti] = ((R / imageHeight) << 16)
                                        | ((G / imageHeight) << 8)
                                        | (B / imageHeight) | ((A / imageHeight) << 24); // A??
                            } else {
                                columns[desti] = 0;
                                //((R/imageHeight)<<16)
                                //|((G/imageHeight)<<8)
                                //|(B/imageHeight); // A??
                            }
                            desti += destWidth;
                        }
                        total -= part;
                    }
                }
            }
        } else {
            for (int x = 0; x < destWidth; x++) { // trough all columns
                int linesi = x; // index in lines pix
                int desti = x; // index in new pix

                for (int y = 0; y < destHeight; y++) {
                    int srcyhq = (y << 8) * (imageHeight - 1) / (destHeight - 1);
                    int srcy = srcyhq >>> 8;
                    int srcinterp = srcyhq & 0xff;

                    int col0 = lines[linesi + srcy * destWidth];
                    if (srcy < imageHeight - 1) {
                        int col1 = lines[linesi + (srcy + 1) * destWidth];
                        col0 = mix(col0, col1, srcinterp);
                    }

                    //set new pixel
                    columns[desti] = col0;
                    desti += destWidth;
                }
            }
        }

        return Image.createRGBImage(columns, destWidth, destHeight, true);
    }

    public static void renderListItemIcon(Graphics g, int x, int y, String path, int state, int iconColor1, int iconColor2, int height, int anchor) {
        String p = path + "_" + height + "_" + state;
        Integer temp = (Integer) iconsCache.get(p);
        int index = temp == null ? 0 : temp.intValue();
        index = Theming.now.drawImage(g, x, y, index, path, iconColor1, iconColor2, height, anchor);
        if (temp == null || temp.intValue() == 0) {
            iconsCache.put(p, new Integer(index));
        }
    }

    static void renderShadow(Graphics g, int x, int y, int width, int height) {
        if (Midlet.instance.config.gui_disableDimm || shadow == null) {
            return;
        }

        int ts = shadow.getWidth() / 3;
        int hts = ts >> 1;

        int ww = Math.min(hts, width / 2);
        int hh = Math.min(hts, height / 2);
        g.drawRegion(shadow, 0, 0,
                hts + ww, hts + hh,
                0, x - hts, y - hts, 0);

        ww = Math.min(hts, width - width / 2);
        g.drawRegion(shadow, ts * 2 + hts - ww, 0,
                hts + ww, hts + hh,
                0, x + width - ww, y - hts, 0);

        ww = Math.min(hts, width / 2);
        hh = Math.min(hts, height - height / 2);
        g.drawRegion(shadow, 0, ts * 2 + hts - hh,
                hts + ww, hts + hh,
                0, x - hts, y + height - hh, 0);

        ww = Math.min(hts, width - width / 2);
        g.drawRegion(shadow, ts * 2 + hts - ww, ts * 2 + hts - hh,
                hts + ww, hts + hh,
                0, x + width - ww, y + height - hh, 0);

        ww = x + width - hts;

        for (int xx = x + hts; xx < ww; xx += ts) {
            int dw = Math.min(ww - xx, ts);

            g.drawRegion(shadow, ts, 0,
                    dw, ts,
                    0, xx, y - hts, 0);

            g.drawRegion(shadow, ts, ts * 2,
                    dw, ts,
                    0, xx, y + height - hts, 0);
        }

        hh = y + height - hts;

        for (int yy = y + hts; yy < hh; yy += ts) {
            int dh = Math.min(hh - yy, ts);

            g.drawRegion(shadow, 0, ts,
                    ts, dh,
                    0, x - hts, yy, 0);

            g.drawRegion(shadow, ts * 2, ts,
                    ts, dh,
                    0, x + width - hts, yy, 0);
        }
    }

    public static int mix(int col1, int col2, int mix) {
        if (mix == 0) {
            return col1;
        } else if (mix == 255) {
            return col2;
        }

        int imix = 255 - mix;

        int a = (((col1 >>> 24) & 0xff) * imix + ((col2 >>> 24) & 0xff) * mix) / 255;
        int r = (((col1 >> 16) & 0xff) * imix + ((col2 >> 16) & 0xff) * mix) / 255;
        int g = (((col1 >> 8) & 0xff) * imix + ((col2 >> 8) & 0xff) * mix) / 255;
        int b = ((col1 & 0xff) * imix + (col2 & 0xff) * mix) / 255;

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    static int focused(int col) {
        int b = ((col >> 16) & 0xff) + ((col >> 8) & 0xff) + (col & 0xff);

        if (b >= 128 * 3) {
            return mix(col, 0x000000, 22);
        } else {
            return mix(col, 0xffffff, 40);
        }
    }

    static int textColorOn(int col) {
        int b = ((col >> 16) & 0xff) + ((col >> 8) & 0xff) + (col & 0xff);

        if (b >= 128 * 3) {
            return 0x000000;
        } else {
            return 0xffffff;
        }
    }
}
