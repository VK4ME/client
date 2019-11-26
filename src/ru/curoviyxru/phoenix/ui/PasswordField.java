package ru.curoviyxru.phoenix.ui;

import ru.curoviyxru.phoenix.Theming;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class PasswordField extends Field {

    SuperString hideText;
    int showLastChar = 0;

    public void showLastChar() {
        if (showLastChar > 0) {
            showLastChar--;
            if (showLastChar == 0) {
                hideText = null;
            }
        }
    }
    
    public PasswordField(String caption, String text) {
        super(caption, text);
    }

    public PasswordField(String caption) {
        super(caption, null);
    }

    public PasswordField() {
        super(null);
    }

    public void setText(String s) {
        text = s == null ? null : new SuperString(s);
        renderText = null;
        hideText = null;
    }

    public void paint(Graphics g, int pY, int pX) {
        pY += AppCanvas.instance.perLineSpace;
        RenderUtil.drawTextField(g, x + pX, y + pY, width, height - AppCanvas.instance.perLineSpace - AppCanvas.instance.perLineSpace, pressed && focusable, content instanceof PopupMenu);

        if (width > 0) {
            if (text != null && hideText == null) {
                hideText = AppCanvas.instance.normalEmojiFont.limit(AppCanvas.instance.normalEmojiFont.hide(text, showLastChar > 0), width - AppCanvas.instance.perLineSpace - AppCanvas.instance.perLineSpace, false);
            }
            if (caption != null && renderCaption == null) {
                renderCaption = AppCanvas.instance.normalEmojiFont.limit(caption, width - AppCanvas.instance.perLineSpace - AppCanvas.instance.perLineSpace, true);
            }
        }

        if (hideText != null && hideText.length > 0) {
            g.setFont(AppCanvas.instance.normalFont);
            g.setColor(Theming.now.textColor);
            AppCanvas.instance.normalEmojiFont.drawString(g, hideText, x + pX + AppCanvas.instance.perLineSpace, y + pY + (height - AppCanvas.instance.perLineSpace - AppCanvas.instance.perLineSpace - AppCanvas.instance.normalFont.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
        } else if (renderCaption != null) {
            g.setFont(AppCanvas.instance.normalFont);
            g.setColor(Theming.now.captionColor);
            AppCanvas.instance.normalEmojiFont.drawString(g, renderCaption, x + pX + AppCanvas.instance.perLineSpace, y + pY + (height - AppCanvas.instance.perLineSpace - AppCanvas.instance.perLineSpace - AppCanvas.instance.normalFont.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
        }
    }
}
