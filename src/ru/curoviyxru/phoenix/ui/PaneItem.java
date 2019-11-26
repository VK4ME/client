package ru.curoviyxru.phoenix.ui;

import java.util.Vector;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author curoviyxru
 */
public abstract class PaneItem {

    public Content content;
    public int width, height, x, y;
    public boolean pressed, focusable = true;
    public String cAction;
    boolean separatorAfter, separatorBefore, skipSelection;
    int lx, ly, dist;

    public void resetCache() {
        
    }
    
    public PaneItem separatorBefore(boolean yea) {
        separatorBefore = yea;
        return this;
    }

    public PaneItem skipSelection(boolean yea) {
        skipSelection = yea;
        return this;
    }

    public PaneItem separatorAfter(boolean yea) {
        separatorAfter = yea;
        return this;
    }
    
    public static interface ItemThatUsesLeftAndRight {
    }

    public static interface ItemThatUsesFullWidth {
    }

    public PaneItem keyPressed(int key, int scrollY) {
        return this;
    }

    public PaneItem keyReleased(int key, int scrollY) {
        if ((key == AppCanvas.FIRE || key == AppCanvas.ENTER) && pressed && focusable) {
            actionPerformed();
        }
        return this;
    }

    public PaneItem keyRepeated(int key, int scrollY) {
        return this;
    }

    public PaneItem pointerPressed(int x, int y, int scrollY) {
        this.pressed = this.focusable && x < this.x + this.width && x > this.x && y < this.y + this.height + AppCanvas.instance.contentY - scrollY && y > this.y + AppCanvas.instance.contentY - scrollY;
        lx = x;
        ly = y - scrollY;
        dist = 0;

        return this;
    }

    public PaneItem pointerDragged(int x, int y, int scrollY) {
        //item.pressed = x <= item.x + item.width && x >= item.x && y <= item.y + AppCanvas.instance.contentY - scrollY + item.height && y >= item.y + AppCanvas.instance.contentY - scrollY;
        dist += (lx - x) * (lx - x) + (y - scrollY - ly) * (y - scrollY - ly);
        if (dist > AppCanvas.instance.fingerDist) {
            pressed = false;
        }

        lx = x;
        ly = y - scrollY;

        return this;
    }

    public PaneItem pointerReleased(int x, int y, int scrollY) {
        if (pressed && focusable) {
            actionPerformed();
        }
        pressed = false;

        return this;
    }

    public PaneItem setFocusable(boolean b) {
        this.focusable = b;
        return this;
    }

    public abstract void updateHeight();

    public abstract void paint(Graphics g, int paintY, int paintX);

    public static String limit(String str, int width, boolean withDots, Font f) {
        if (str == null || str.length() == 0) {
            return str;
        }
        
        StringBuffer b = new StringBuffer();

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '\n') {
                b.append(' ');
            } else b.append(str.charAt(i));

            if (f.stringWidth(b.toString().trim() + (withDots ? "..." : "")) > width) {
                b.setLength(b.length() - 1);
                return b.toString().trim() + (withDots ? "..." : ""); 
            }
        }

        return b.toString();
    }
    
    public static String[] multiline(String str, int width, Font f) {
        if (str == null || str.length() == 0) {
            return new String[0];
        }
        Vector v = new Vector();
        
        StringBuffer b = new StringBuffer();

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '\n') {
                v.addElement(b.toString());
                b.setLength(0);
                continue;
            }
            
            b.append(str.charAt(i));

            if (f.stringWidth(b.toString()) > width) {
                if (b.toString().lastIndexOf(' ') != -1) {
                    String stt = b.toString();
                    b.setLength(0);
                    int ind = stt.lastIndexOf(' ');
                    v.addElement(stt.substring(0, ind));
                    b.append(stt.substring(ind + 1, stt.length()));
                } else {
                    b.setLength(b.length() - 1);
                    v.addElement(b.toString());
                    b.setLength(0);
                    b.append(str.charAt(i));
                }
            }
        }

        if (b.length() > 0) {
            v.addElement(b.toString());
        }

        String[] sa = new String[v.size()];
        v.copyInto(sa);
        return sa;
    }

    public static String hide(String renderText, boolean showLast) {
        if (renderText == null) return null;

        char[] stars = new char[renderText.length()];

        for (int i = stars.length - (showLast ? 2 : 1); i >= 0; i--) {
            stars[i] = '*';
        }
        if (showLast && renderText.length() > 0) stars[stars.length - 1] = renderText.charAt(stars.length - 1);

        return new String(stars);
    }

    public void actionPerformed() {
    }
}
