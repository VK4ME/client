package ru.curoviyxru.phoenix.ui;

import ru.curoviyxru.phoenix.Theming;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import ru.curoviyxru.phoenix.Localization;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class Field extends PaneItem implements PaneItem.ItemThatUsesLeftAndRight {

    public SuperString text, caption, renderCaption, renderText;
    public static int circleSize;
    public static Image borderImage, borderImageSelected;

    public void setCaption(String s) {
        caption = s == null ? null : new SuperString(s);
        renderCaption = null;
    }

    public void setText(SuperString s) {
        text = s;
        renderText = null;
    }
    
    public void setText(String s) {
        text = s == null ? null : new SuperString(s);
        renderText = null;
    }

    public Field(String caption, String text) {
        this.cAction = Localization.get("general.holdField");
        setCaption(caption);
        setText(text);
        height = AppCanvas.instance.normalFont.getHeight() + AppCanvas.instance.perLineSpace * 4;
    }

    public Field(String caption) {
        this(caption, null);
    }

    public Field() {
        this(null);
    }

    public void actionPerformed() {
        AppCanvas.instance.showField(this);
    }

    public void paint(Graphics g, int pY, int pX) {
        pY += AppCanvas.instance.perLineSpace;
        RenderUtil.drawTextField(g, x + pX, y + pY, width, height - AppCanvas.instance.perLineSpace - AppCanvas.instance.perLineSpace, pressed && focusable, content instanceof PopupMenu);

        if (width > 0) {
            if (text != null && renderText == null) {
                renderText =  AppCanvas.instance.normalEmojiFont.limit(text, width - AppCanvas.instance.perLineSpace - AppCanvas.instance.perLineSpace, true);
            }
            if (caption != null && renderCaption == null) {
                renderCaption =  AppCanvas.instance.normalEmojiFont.limit(caption, width - AppCanvas.instance.perLineSpace - AppCanvas.instance.perLineSpace, true);
            }
        }

        if (renderText != null && renderText.length > 0) {
            g.setFont(AppCanvas.instance.normalFont);
            g.setColor(Theming.now.textColor);
            AppCanvas.instance.normalEmojiFont.drawString(g, renderText, x + pX + AppCanvas.instance.perLineSpace, y + pY + (height - AppCanvas.instance.perLineSpace - AppCanvas.instance.perLineSpace - AppCanvas.instance.normalFont.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
        } else if (renderCaption != null) {
            g.setFont(AppCanvas.instance.normalFont);
            g.setColor(Theming.now.captionColor);
            AppCanvas.instance.normalEmojiFont.drawString(g, renderCaption, x + pX + AppCanvas.instance.perLineSpace, y + pY + (height - AppCanvas.instance.perLineSpace - AppCanvas.instance.perLineSpace - AppCanvas.instance.normalFont.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
        }
    }

    public String getText() {
        return text == null ? null : text.toString();
    }

    public void updateHeight() {
    }
}
