package ru.curoviyxru.phoenix.ui;

import ru.curoviyxru.phoenix.Theming;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class Footer extends PaneItem implements PaneItem.ItemThatUsesFullWidth {

    public Footer() {
        height = (AppCanvas.instance.normalFont.getHeight() / 2) + 1;
        setFocusable(true);
        skipSelection(true);
    }

    public void paint(Graphics g, int paintY, int paintX) {
		g.setColor(Theming.now.footerLineColor);
		g.drawLine(x + paintX, y + paintY, x + paintX + width, y + paintY);
		
        g.setColor(Theming.now.footerColor);
        g.fillRect(x + paintX, y + paintY + 1, width, height - 1);
    }

    public void updateHeight() {
    }
}
