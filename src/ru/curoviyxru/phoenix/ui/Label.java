package ru.curoviyxru.phoenix.ui;

import ru.curoviyxru.phoenix.Theming;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class Label extends PaneItem {

    public SuperString text;
    public SuperString[] v;
    public Integer color;
    public boolean bold;
    public int align;

    public void resetCache() {
        v = null;
    }
    
    public Label setColor(Integer color) {
        this.color = color;
        return this;
    }
    
    public Label setColor(int color) {
        return setColor(new Integer(color));
    }

    public Label setAlign(int i) {
        align = i;
        return this;
    }

    public Label(String text) {
        setText(text);
        setFocusable(true);
    }

    public Label setFont(boolean bold) {
        this.bold = bold;
        return this;
    }

    public Label setText(String text) {
        this.text = text == null ? null : new SuperString(text);
        v = null;

        return this;
    }

    public void paint(Graphics g, int pY, int pX) {
        if (align != Graphics.HCENTER && align != Graphics.RIGHT) {
            align = Graphics.LEFT;
        }
        int origpy = pY;
        pY += AppCanvas.instance.normalEmojiFont.height / 2;

        updateHeight();

        if (v != null) {
            g.setColor(color != null ? color.intValue() : Theming.now.textColor);
            int ci = calcI(origpy);
            FontWithEmoji fn = bold ? AppCanvas.instance.boldEmojiFont : AppCanvas.instance.normalEmojiFont;
            pY += Math.min(ci, v.length) * fn.height;
            int cm = calcMax(origpy);
            origpy = pY;
            for (int i = ci; i < cm; i++) {
                int padd = align == Graphics.HCENTER ? width / 2 : align == Graphics.RIGHT ? width : 0;

                fn.drawString(g, v[i], x + pX + padd, y + pY, Graphics.TOP | align);
                pY += fn.height;
            }
        }

        if (pressed && focusable) {
            g.setColor(Theming.now.focusedBorderColor);
            g.drawRect(x + pX - 2, y + origpy - 2, width + 4, height - AppCanvas.instance.normalEmojiFont.height + 4);
        }
    }

    private int calcI(int pY) {
        int pos = this.y + pY - (!(content instanceof PopupMenu) ? AppCanvas.instance.contentY : AppCanvas.instance.popupY()) + AppCanvas.instance.normalEmojiFont.height / 2;
        int i = 0;
        if (pos < 0) {
            i += -pos / (AppCanvas.instance.normalEmojiFont.height);
        }
        return i;
    }

    private int calcMax(int pY) {
        int pos = this.y + pY - (!(content instanceof PopupMenu) ? AppCanvas.instance.contentY : AppCanvas.instance.popupY()) + height - AppCanvas.instance.normalEmojiFont.height / 2 - (!(content instanceof PopupMenu) ? AppCanvas.instance.contentHeight : AppCanvas.instance.popupHeight());
        int i = v.length;
        if (pos > 0) {
            i += -pos / (AppCanvas.instance.normalEmojiFont.height);
        }
        return i;
    }

    public void updateHeight() {
        if (width > 0 && v == null && text != null) {
            v = AppCanvas.instance.normalEmojiFont.multiline(text, width);
            height = (1 + v.length) * AppCanvas.instance.normalEmojiFont.height;
        }
    }
}
