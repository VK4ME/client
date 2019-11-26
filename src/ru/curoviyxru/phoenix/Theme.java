package ru.curoviyxru.phoenix;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import ru.curoviyxru.phoenix.Theming.ThemeEntry;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.RenderUtil;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class Theme {

    //W - 0xFFFFFF
    //B - 0x4A76A8
    //R - 0xd32f2f
    //G - 0x388e3c
    //Gray - 0x537DAD
    //Also, this class contains all gradients (see: paint()), but:
    //white: FFFFFF - D4D4D4
    //gray:  949494 - 646464
    //black: 454449 - 222126
    //red:   ec7575 - bc4747
    //blue:  759dec - 476ebc
    //green: 09d009 - 069706
    public ThemeEntry entry = new ThemeEntry();
    //White theme
    /*public int mainColor = 0x454449,
     mainColor_ = 0x222126,
     onMainTextColor = 0xffffff,
     onMainIconColor = 0xFFFFFF,
     onMainIconColor_ = 0xD4D4D4,
     focusedMainColor = 0x759DEC, //RenderUtil.focused(mainColor),
     focusedMainColor_ = 0x476EBC;
     public int backgroundColor = 0xFFFFFF,
     textColor = 0x000000,
     descriptionColor = 0x3E3E3E,
     captionColor = 0x777777,
     iconColor = 0x949494,
     iconColor_ = 0x646464,
     borderColor = 0xD4D4D4,
     checkboxColor = 0x949494,
     gotoColor = 0x949494,
     checkboxColor_ = 0x646464,
     gotoColor_ = 0x646464,
     focusedBackgroundColor = 0x759DEC, //RenderUtil.focused(backgroundColor),
     focusedBackgroundColor_ = 0x476EBC,
     focusedTextColor = 0xFFFFFF,
     focusedDescriptionColor = 0xFFFFFF,
     focusedCaptionColor = 0xFFFFFF,
     focusedIconColor = 0xFFFFFF,
     focusedIconColor_ = 0xD4D4D4,
     focusedBorderColor = 0x476EBC,
     selectedCheckboxColor = 0x09d009,
     focusedCheckboxColor = 0xFFFFFF,
     focusedGotoColor = 0xFFFFFF,
     selectedCheckboxColor_ = 0x069706,
     focusedCheckboxColor_ = 0xD4D4D4,
     focusedGotoColor_ = 0xD4D4D4,
     activeIconColor = 0x759DEC,
     activeIconColor_ = 0x476EBC,
     unreadBackgroundColor = 0xE9EEF4,
     unreadBackgroundColor_ = 0xE9EEF4,
     unreadCloudForegroundColor = 0xFFFFFF,
     unreadCloudBackgroundColor = 0x759DEC,
     unreadCloudBackgroundColor_ = 0x476EBC,
     focusedUnreadCloudForegroundColor = 0x759DEC,
     focusedUnreadCloudBackgroundColor = 0xFFFFFF,
     focusedUnreadCloudBackgroundColor_ = 0xD4D4D4;
     public int itemSeparatorColor = 0xDEDEDE,
     footerColor = 0xE4E4E4,
     nonLoadedContentColor = 0xFFD4D4D4;
     public int sliderColor = 0xC4C4C4,
     sliderColor_ = 0xE4E4E4,
     sliderLoadedColor = 0x759DEC,
     sliderLoadedColor_ = 0x476EBC,
     sliderButtonColor = 0xE4E4E4,
     sliderButtonColor_ = 0xC4C4C4,
     focusedSliderButtonColor = 0x759DEC,
     focusedSliderButtonColor_ = 0x476EBC,
     sliderButtonBorderColor = 0xb4b4b4;
     public int userMessageColor = 0xCAD9F8,
     userMessageColor_ = 0x91A8D7,
     somebodyMessageColor = 0xE4E4E4,
     somebodyMessageColor_ = 0xC4C4C4,
     userMessageBorderColor = 0x8A9FCA,
     somebodyMessageBorderColor = 0xb4b4b4,
     actionMessageColor = 0x949494,
     userMessageQuoteColor = 0x476ebc,
     somebodyMessageQuoteColor = 0x646464;
     public int messageContentBackgroundColor = 0xffffff;
     public int spinnerIconColor = 0xFFFFFF,
     onlineIconColor = 0x4A76A8,
     onlineBackgroundColor = 0xFFFFFF;
     public int dimmColor = 0x000000,
     msgFieldBackgroundColor = 0xFFFFFF;
     public int likeColor = 0xFF807E,
     likeColor_ = 0xC84947,
     likeTextColor = 0xFF5D5B,
     focusedLikeColor = 0xFF807E,
     focusedLikeColor_ = 0xC84947,
     focusedLikeTextColor = 0xFF5D5B;
     public int scrollColor = 0x000000,
     footerLineColor = 0xDEDEDE,
     popupColor = 0xFFFFFF,
     focusedSliderButtonBorderColor = 0xDEDEDE,
     mutedUnreadCloudBackgroundColor = 0x828282,
     mutedUnreadCloudBackgroundColor_ = 0x666666,
     focusedMutedUnreadCloudBackgroundColor = 0xFFFFFF, 
     focusedMutedUnreadCloudBackgroundColor_ = 0xD4D4D4;*/
    //Dark theme
    public int mainColor = 0x454648,
            mainColor_ = 0x2C2D2F,
            onMainTextColor = 0xF0F1F3,
            onMainIconColor = 0xFFFFFF,
            onMainIconColor_ = 0xE2E3E7,
            focusedMainColor = 0x77787A, //RenderUtil.focused(mainColor),
            focusedMainColor_ = 0x454648;
    public int backgroundColor = 0x191919,
            textColor = 0xE2E3E7,
            descriptionColor = 0x5E5F61,
            captionColor = 0x909497,
            iconColor = 0xDADBDF,
            iconColor_ = 0xAEAFB3,
            borderColor = 0x454648,
            checkboxColor = 0xDADBDF,
            gotoColor = 0xDADBDF,
            checkboxColor_ = 0xAEAFB3,
            gotoColor_ = 0xAEAFB3,
            focusedBackgroundColor = 0x454648, //RenderUtil.focused(backgroundColor),
            focusedBackgroundColor_ = 0x2C2D2F,
            focusedTextColor = 0xFFFFFF,
            focusedDescriptionColor = 0x9B9B99,
            focusedCaptionColor = 0xCDD0CF,
            focusedIconColor = 0xFFFFFF,
            focusedIconColor_ = 0xE2E3E7,
            focusedBorderColor = 0xE2E3E7,
            selectedCheckboxColor = 0xDADBDF,
            focusedCheckboxColor = 0xFFFFFF,
            focusedGotoColor = 0xFFFFFF,
            selectedCheckboxColor_ = 0xAEAFB3,
            focusedCheckboxColor_ = 0xE2E3E7,
            focusedGotoColor_ = 0xE2E3E7,
            activeIconColor = 0x759DEC,
            activeIconColor_ = 0x476EBC,
            unreadBackgroundColor = 0x222222,
            unreadBackgroundColor_ = 0x222222,
            unreadCloudForegroundColor = 0x191919,
            unreadCloudBackgroundColor = 0xDADBDF,
            unreadCloudBackgroundColor_ = 0xAEAFB3,
            focusedUnreadCloudForegroundColor = 0x383A3C, //RenderUtil.mix(focusedBackgroundColor, focusedBackgroundColor_, 128),
            focusedUnreadCloudBackgroundColor = 0xFFFFFF,
            focusedUnreadCloudBackgroundColor_ = 0xE2E3E7;
    public int itemSeparatorColor = 0x353535,
            footerColor = 0x0A0A0A,
            nonLoadedContentColor = 0x222222;
    public int sliderColor = 0x949494,
            sliderColor_ = 0x646464,
            sliderLoadedColor = 0xFFFFFF,
            sliderLoadedColor_ = 0xE2E3E7,
            sliderButtonColor = 0x2C2D2F,
            sliderButtonColor_ = 0x191919,
            focusedSliderButtonColor = 0x454648,
            focusedSliderButtonColor_ = 0x2C2D2F,
            sliderButtonBorderColor = 0x454648;
    public int userMessageColor = 0x4A4C4E,
            userMessageColor_ = 0x383A3C,
            somebodyMessageColor = 0x3A3B3D,
            somebodyMessageColor_ = 0x2C2D2F,
            userMessageBorderColor = 0x595A5D,
            somebodyMessageBorderColor = 0x454648,
            actionMessageColor = 0x949494,
            userMessageQuoteColor = 0xE2E3E7,
            somebodyMessageQuoteColor = 0xE2E3E7;
    public int messageContentBackgroundColor = 0xffffff;
    public int spinnerIconColor = 0xFFFFFF,
            onlineIconColor = 0x4AB34A,
            onlineBackgroundColor = 0x191919;
    public int dimmColor = 0x000000,
            msgFieldBackgroundColor = 0x2C2D2F;
    public int likeColor = 0xFF807E,
            likeColor_ = 0xC84947,
            likeTextColor = 0xFF5D5B,
            focusedLikeColor = 0xFF807E,
            focusedLikeColor_ = 0xC84947,
            focusedLikeTextColor = 0xFF5D5B;
    public int scrollColor = 0xDADBDF,
            footerLineColor = 0x0A0A0A,
            popupColor = 0x1A1A1A,
            focusedSliderButtonBorderColor = 0xE2E3E7,
            mutedUnreadCloudBackgroundColor = 0x6D6D6F,
            mutedUnreadCloudBackgroundColor_ = 0x57585A,
            focusedMutedUnreadCloudBackgroundColor = 0x888888,
            focusedMutedUnreadCloudBackgroundColor_ = 0x717173;
    int iconsCount = 0;
    int[][] iconsColors = new int[10][2];
    Image[] icons = new Image[10];
    //todo кешировать нераспакованные rle?
    Hashtable cachedRLEs = new Hashtable(),
            cachedRLEsW = new Hashtable(),
            cachedRLEsH = new Hashtable();

    public void recache() {
        if (AppCanvas.instance != null && Midlet.instance.config != null) {
            AppCanvas.instance.themeRecache();
        }
    }

    public Theme() {
        entry.name = "Classic Dark";
        entry.version = "3.3.3";
    }

    public Theme(byte[] bytes) throws Exception {
        read(new ByteArrayInputStream(bytes));
    }

    private void enlarge() {
        int[][] iconsColorsn = new int[iconsColors.length + 10][2];
        System.arraycopy(iconsColors, 0, iconsColorsn, 0, iconsColors.length);
        iconsColors = iconsColorsn;

        Image[] iconsn = new Image[icons.length + 10];
        System.arraycopy(icons, 0, iconsn, 0, icons.length);
        icons = iconsn;
    }

    private int getImage(int id, String path, int color1, int color2, int height) {
        if (id == 0) {
            if (iconsCount == icons.length) {
                enlarge();
            }
            id = iconsCount + 1;
            iconsCount++;

            icons[id - 1] = makeImage(path, color1, color2, height);
            iconsColors[id - 1] = new int[]{color1, color2};

        } else if (icons[id - 1] == null
                || icons[id - 1].getHeight() != height
                || iconsColors[id - 1] == null
                || iconsColors[id - 1][0] != color1
                || iconsColors[id - 1][1] != color2) {

            icons[id - 1] = makeImage(path, color1, color2, height);
            iconsColors[id - 1] = new int[]{color1, color2};
        }

        return id;
    }

    public int drawImage(Graphics g, int x, int y,
            int id, String path, int color1, int color2, int height, int anchor) {
        return drawImage(g, x, y, id, path, color1, color2, height, Sprite.TRANS_NONE, anchor);
    }

    public int drawImage(Graphics g, int x, int y,
            int id, String path, int color1, int color2, int height, int rotate, int anchor) {
        int realID = getImage(id, path, color1, color2, height);
        Image img = icons[realID - 1];

        if (img != null) g.drawRegion(img, 0, 0, img.getWidth(), img.getHeight(), rotate, x, y, anchor);

        return realID;
    }

    public void clearCache() {
        for (int i = 0; i < iconsCount; i++) {
            icons[i] = null;
        }

        cachedRLEs.clear();
        cachedRLEsW.clear();
        cachedRLEsH.clear();
    }

    private Image makeImage(String path, int col1, int col2, int resizedH) {
        try {
            int w, h;
            byte[] alpha = (byte[]) cachedRLEs.get(path);

            if (alpha == null) {
                InputStream is = (new Object()).getClass().getResourceAsStream("/res/" + path);
                DataInputStream dis = new DataInputStream(is);

                byte rle[] = new byte[dis.available()];
                dis.read(rle);
                dis.close();

                w = (rle[0] << 24) | (rle[1] << 16) | (rle[2] << 8) | (rle[3] & 0xff);
                h = (rle[4] << 24) | (rle[5] << 16) | (rle[6] << 8) | (rle[7] & 0xff);

                alpha = new byte[w * h];

                readRLE(rle, 8, alpha);

                cachedRLEs.put(path, alpha);
                cachedRLEsW.put(path, new Integer(w));
                cachedRLEsH.put(path, new Integer(h));
            } else {
                w = ((Integer) cachedRLEsW.get(path)).intValue();
                h = ((Integer) cachedRLEsH.get(path)).intValue();
            }

            return paintImage(resizeAlpha(alpha, w, h, resizedH), col1, col2);
        } catch (Exception e) {
            Logger.l(e);
            return RenderUtil.resizeImage(Image.createImage(1, 1), resizedH);
        }
    }

    public Object[] resizeAlpha(byte[] alpha, int imageWidth, int imageHeight, int heightI) {
        if (alpha == null) {
            return null;
        }

        if (heightI <= 0) {
            return new Object[]{new byte[1], new Integer(1), new Integer(1)};
        }

        int destHeight = heightI;
        int destWidth = imageWidth * heightI / imageHeight;

        byte[] lines = new byte[destWidth * imageHeight];
        byte[] columns = new byte[destWidth * destHeight];
        /**
         * Fast *
         */
        if (destWidth < imageWidth) {
            for (int y = 0; y < imageHeight; y++) { // trough all lines
                int srci = y * imageWidth; // index in old pix
                int desti = y * destWidth; // index in new pix
                int part = destWidth;
                int addon = 0, a = 0;
                for (int x = 0; x < destWidth; x++) {
                    int total = imageWidth;
                    int A = 0;
                    if (addon != 0) {
                        A = a * addon;
                        total -= addon;
                    }
                    while (0 < total) {
                        a = (alpha[srci++] & 0xff);
                        if (total > part) {
                            A += a * part;
                        } else {
                            A += a * total;
                            addon = part - total;
                            lines[desti++] = (byte) (A / imageWidth);
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
                    int isrcinterp = 255 - srcinterp;

                    int col0 = alpha[srci + srcx] & 0xff;
                    if (srcx < imageWidth - 1) {
                        int col1 = alpha[srci + srcx + 1] & 0xff;
                        col0 = (col0 * isrcinterp + col1 * srcinterp) / 255;
                    }

                    //set new pixel
                    lines[desti++] = (byte) col0;
                }
            }
        }
        if (destHeight < imageHeight) {
            for (int x = 0; x < destWidth; x++) { // trough columns
                int linesi = x; // index in lines pix
                int desti = x; // index in new pix
                int part = destHeight;
                int addon = 0, a = 0;
                for (int y = 0; y < destHeight; y++) {
                    int total = imageHeight;
                    int A = 0;
                    if (addon != 0) {
                        A = a * addon;
                        total -= addon;
                    }
                    while (0 < total) {
                        a = lines[linesi] & 0xff;// may no rotate
                        linesi += destWidth;
                        if (total > part) {
                            A += a * part;
                        } else {
                            A += a * total;
                            addon = part - total;
                            ///set new pixel
                            columns[desti] = (byte) (A / imageHeight);

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
                    int isrcinterp = 255 - srcinterp;

                    int col0 = lines[linesi + srcy * destWidth] & 0xff;
                    if (srcy < imageHeight - 1) {
                        int col1 = lines[linesi + (srcy + 1) * destWidth] & 0xff;
                        col0 = (col0 * isrcinterp + col1 * srcinterp) / 255;
                    }

                    //set new pixel
                    columns[desti] = (byte) col0;
                    desti += destWidth;
                }
            }
        }

        return new Object[]{columns, new Integer(destWidth), new Integer(destHeight)};
    }

    public Image paintImage(Object[] dataRaw, int col1, int col2) {
        if (dataRaw == null) {
            return Image.createImage(1, 1);
        }
        if (!Midlet.instance.config.gui_drawGradients) col1 = col2 = RenderUtil.mix(col1, col2, 128);
        byte[] alpha = (byte[]) dataRaw[0];
        int w = ((Integer) dataRaw[1]).intValue();
        int h = ((Integer) dataRaw[2]).intValue();
        int[] data = new int[alpha.length];

        col1 &= 0x00FFFFFF;
        col2 &= 0x00FFFFFF;

        if (col1 == col2) {
            for (int i = 0; i < alpha.length; ++i) {
                data[i] = (alpha[i] << 24) | col1;
            }
            return Image.createRGBImage(data, w, h, true);
        } else {
            int ti;
            for (int y = 0; y < h; ++y) {
                for (int x = 0; x < w; ++x) {
                    ti = y * w + x;
                    data[ti] = (alpha[ti] << 24) | (RenderUtil.mix(col1, col2, 255 * (y + 1) / h));
                }
            }
            return Image.createRGBImage(data, w, h, true);
        }
    }

    public int readRLE(byte[] in, int inp, byte[] out) throws Exception {
        int outp = 0;

        while (outp < out.length && in.length - inp >= 3) {
            int count = ((in[inp] & 0xff) << 8) | (in[inp + 1] & 0xff);
            inp += 2;

            boolean repeat = count > 0x7fff;
            count = (count & 0x7fff) + 1;

            if (repeat) {
                byte b = in[inp];
                inp++;
                int fillEnd = outp + count;

                for (; outp < fillEnd; outp++) {
                    out[outp] = b;
                }
            } else {
                System.arraycopy(in, inp, out, outp, count);
                outp += count;
                inp += count;
            }
        }

        return out.length;
    }

    public void readTheme(int[] argb) {
        int i = 0;
        mainColor = argb[i++];
        mainColor_ = argb[i++];
        onMainTextColor = argb[i++];
        onMainIconColor = argb[i++];
        onMainIconColor_ = argb[i++];
        focusedMainColor = argb[i++];
        focusedMainColor_ = argb[i++];
        backgroundColor = argb[i++];
        textColor = argb[i++];
        descriptionColor = argb[i++];
        captionColor = argb[i++];
        iconColor = argb[i++];
        iconColor_ = argb[i++];
        borderColor = argb[i++];
        checkboxColor = argb[i++];
        gotoColor = argb[i++];
        checkboxColor_ = argb[i++];
        gotoColor_ = argb[i++];
        focusedBackgroundColor = argb[i++];
        focusedBackgroundColor_ = argb[i++];
        focusedTextColor = argb[i++];
        focusedDescriptionColor = argb[i++];
        focusedCaptionColor = argb[i++];
        focusedIconColor = argb[i++];
        focusedIconColor_ = argb[i++];
        focusedBorderColor = argb[i++];
        selectedCheckboxColor = argb[i++];
        focusedCheckboxColor = argb[i++];
        focusedGotoColor = argb[i++];
        selectedCheckboxColor_ = argb[i++];
        focusedCheckboxColor_ = argb[i++];
        focusedGotoColor_ = argb[i++];
        activeIconColor = argb[i++];
        activeIconColor_ = argb[i++];
        unreadBackgroundColor = argb[i++];
        unreadBackgroundColor_ = argb[i++];
        unreadCloudForegroundColor = argb[i++];
        unreadCloudBackgroundColor = argb[i++];
        unreadCloudBackgroundColor_ = argb[i++];
        focusedUnreadCloudForegroundColor = argb[i++];
        focusedUnreadCloudBackgroundColor = argb[i++];
        focusedUnreadCloudBackgroundColor_ = argb[i++];
        itemSeparatorColor = argb[i++];
        footerColor = argb[i++];
        nonLoadedContentColor = argb[i++];
        sliderColor = argb[i++];
        sliderColor_ = argb[i++];
        sliderLoadedColor = argb[i++];
        sliderLoadedColor_ = argb[i++];
        sliderButtonColor = argb[i++];
        sliderButtonColor_ = argb[i++];
        focusedSliderButtonColor = argb[i++];
        focusedSliderButtonColor_ = argb[i++];
        sliderButtonBorderColor = argb[i++];
        userMessageColor = argb[i++];
        userMessageColor_ = argb[i++];
        somebodyMessageColor = argb[i++];
        somebodyMessageColor_ = argb[i++];
        userMessageBorderColor = argb[i++];
        somebodyMessageBorderColor = argb[i++];
        actionMessageColor = argb[i++];
        userMessageQuoteColor = argb[i++];
        somebodyMessageQuoteColor = argb[i++];
        messageContentBackgroundColor = argb[i++];
        spinnerIconColor = argb[i++];
        onlineIconColor = argb[i++];
        onlineBackgroundColor = argb[i++];
        dimmColor = argb[i++];
        msgFieldBackgroundColor = argb[i++];
        likeColor = argb[i++];
        likeColor_ = argb[i++];
        likeTextColor = argb[i++];
        focusedLikeColor = argb[i++];
        focusedLikeColor_ = argb[i++];
        focusedLikeTextColor = argb[i++];
        scrollColor = argb[i++];
        footerLineColor = argb[i++];
        popupColor = argb[i++];
        focusedSliderButtonBorderColor = argb[i++];
        mutedUnreadCloudBackgroundColor = argb[i++];
        mutedUnreadCloudBackgroundColor_ = argb[i++];
        focusedMutedUnreadCloudBackgroundColor = argb[i++];
        focusedMutedUnreadCloudBackgroundColor_ = argb[i++];

        recache();
    }

    public int[] writeTheme() {
        int[] argb = new int[100];
        int i = 0;

        argb[i++] = mainColor;
        argb[i++] = mainColor_;
        argb[i++] = onMainTextColor;
        argb[i++] = onMainIconColor;
        argb[i++] = onMainIconColor_;
        argb[i++] = focusedMainColor;
        argb[i++] = focusedMainColor_;
        argb[i++] = backgroundColor;
        argb[i++] = textColor;
        argb[i++] = descriptionColor;
        argb[i++] = captionColor;
        argb[i++] = iconColor;
        argb[i++] = iconColor_;
        argb[i++] = borderColor;
        argb[i++] = checkboxColor;
        argb[i++] = gotoColor;
        argb[i++] = checkboxColor_;
        argb[i++] = gotoColor_;
        argb[i++] = focusedBackgroundColor;
        argb[i++] = focusedBackgroundColor_;
        argb[i++] = focusedTextColor;
        argb[i++] = focusedDescriptionColor;
        argb[i++] = focusedCaptionColor;
        argb[i++] = focusedIconColor;
        argb[i++] = focusedIconColor_;
        argb[i++] = focusedBorderColor;
        argb[i++] = selectedCheckboxColor;
        argb[i++] = focusedCheckboxColor;
        argb[i++] = focusedGotoColor;
        argb[i++] = selectedCheckboxColor_;
        argb[i++] = focusedCheckboxColor_;
        argb[i++] = focusedGotoColor_;
        argb[i++] = activeIconColor;
        argb[i++] = activeIconColor_;
        argb[i++] = unreadBackgroundColor;
        argb[i++] = unreadBackgroundColor_;
        argb[i++] = unreadCloudForegroundColor;
        argb[i++] = unreadCloudBackgroundColor;
        argb[i++] = unreadCloudBackgroundColor_;
        argb[i++] = focusedUnreadCloudForegroundColor;
        argb[i++] = focusedUnreadCloudBackgroundColor;
        argb[i++] = focusedUnreadCloudBackgroundColor_;
        argb[i++] = itemSeparatorColor;
        argb[i++] = footerColor;
        argb[i++] = nonLoadedContentColor;
        argb[i++] = sliderColor;
        argb[i++] = sliderColor_;
        argb[i++] = sliderLoadedColor;
        argb[i++] = sliderLoadedColor_;
        argb[i++] = sliderButtonColor;
        argb[i++] = sliderButtonColor_;
        argb[i++] = focusedSliderButtonColor;
        argb[i++] = focusedSliderButtonColor_;
        argb[i++] = sliderButtonBorderColor;
        argb[i++] = userMessageColor;
        argb[i++] = userMessageColor_;
        argb[i++] = somebodyMessageColor;
        argb[i++] = somebodyMessageColor_;
        argb[i++] = userMessageBorderColor;
        argb[i++] = somebodyMessageBorderColor;
        argb[i++] = actionMessageColor;
        argb[i++] = userMessageQuoteColor;
        argb[i++] = somebodyMessageQuoteColor;
        argb[i++] = messageContentBackgroundColor;
        argb[i++] = spinnerIconColor;
        argb[i++] = onlineIconColor;
        argb[i++] = onlineBackgroundColor;
        argb[i++] = dimmColor;
        argb[i++] = msgFieldBackgroundColor;
        argb[i++] = likeColor;
        argb[i++] = likeColor_;
        argb[i++] = likeTextColor;
        argb[i++] = focusedLikeColor;
        argb[i++] = focusedLikeColor_;
        argb[i++] = focusedLikeTextColor;
        argb[i++] = scrollColor;
        argb[i++] = footerLineColor;
        argb[i++] = popupColor;
        argb[i++] = focusedSliderButtonBorderColor;
        argb[i++] = mutedUnreadCloudBackgroundColor;
        argb[i++] = mutedUnreadCloudBackgroundColor_;
        argb[i++] = focusedMutedUnreadCloudBackgroundColor;
        argb[i++] = focusedMutedUnreadCloudBackgroundColor_;

        return argb;
    }

    public void write(OutputStream os) throws Exception {
        DataOutputStream dos = new DataOutputStream(os);

        entry.write(dos);

        int[] argb = writeTheme();
        byte[] bytebuf = new byte[300];
        for (int i = 0; i < 300; i += 3) {
            bytebuf[i] = (byte) (argb[i / 3] >>> 16);
            bytebuf[i + 1] = (byte) (argb[i / 3] >>> 8);
            bytebuf[i + 2] = (byte) (argb[i / 3]);
        }
        dos.write(HexMe.basify(bytebuf));

        dos.flush();
    }

    public void read(InputStream is) throws Exception {
        DataInputStream dis = new DataInputStream(is);

        entry = new ThemeEntry();
        entry.read(dis);
        int[] argb = new int[100];
        byte[] bytebuf = new byte[400];
        dis.readFully(bytebuf);
        bytebuf = HexMe.unbasify(bytebuf);
        for (int i = 0; i < 300; i += 3) {
            argb[i / 3] = ((bytebuf[i] & 0xFF) << 16) | ((bytebuf[i + 1] & 0xFF) << 8) | (bytebuf[i + 2] & 0xFF);
        }

        readTheme(argb);
    }
}
