package ru.curoviyxru.phoenix.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import ru.curoviyxru.phoenix.Theming;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class PopupMenu extends Content {

    int x, y;
    public boolean open, active = true, renderShadow;
    boolean pointerPressedInPopup, pointerPressedInMenu, pointerWasPressed;
    public static int circleSize;
    public static Image circleImage;

    public PopupMenu(String s) {
        super(s);
    }

    public PopupMenu() {
        this(null);
    }

    public void actionPerformed(int x, int y, int anchor) {
        this.x = x;
        this.y = y;

        int w = getWidth();
        int h = getHeight();

        if ((anchor & Graphics.HCENTER) == Graphics.HCENTER) {
            this.x -= w / 2;
        } else if ((anchor & Graphics.RIGHT) == Graphics.RIGHT) {
            this.x -= w;
        }

        if ((anchor & Graphics.VCENTER) == Graphics.VCENTER) {
            this.y -= h / 2;
        } else if ((anchor & Graphics.BOTTOM) == Graphics.BOTTOM) {
            this.y -= h;
        }

        this.x = Math.min(Math.max(0, this.x), AppCanvas.instance.getWidth() - w);
        this.y = Math.min(Math.max(0, this.y), AppCanvas.instance.getHeight() - h);

        scrollY = toScrollY = 0;
        selectedY = 0;
        if (open || !active) {
            open = false;
        } else {
            open = !isEmpty();
        }

        renderShadow = open;

        pointerWasPressed = false;
    }

    public int getHeight() {
        if (totalHeight == 0) {
            updateHeights(getWidth(), size());
        }
        return Math.min(totalHeight + circleSize + circleSize, AppCanvas.instance.getHeight());
    }

    public int getHeightItems() {
        if (totalHeight == 0) {
            updateHeights(getWidth(), size());
        }
        return Math.min(totalHeight, AppCanvas.instance.getHeight() - circleSize - circleSize);
    }

    public int getWidth() {
        return AppCanvas.instance.popupWidth;
    }

    public void paint(Graphics g, int pX) {
        if (!open || isEmpty()) {
            return;
        }

        g.setColor(Theming.now.popupColor);
        if (renderShadow) {
            RenderUtil.renderShadow(g, x, y, getWidth(), getHeight());
            
            if (circleImage != null) {
                g.fillRect(x + circleSize, y, getWidth() - circleSize - circleSize, circleSize);
                g.fillRect(x + circleSize, y + getHeight() - circleSize, getWidth() - circleSize - circleSize, circleSize);
                
                g.drawImage(circleImage, x, y, Graphics.TOP | Graphics.LEFT);
                g.drawRegion(circleImage, 
                        0, 0, circleSize, circleSize, Sprite.TRANS_ROT90, 
                        x + getWidth() - circleSize, y, 0);
                g.drawRegion(circleImage, 
                        0, 0, circleSize, circleSize, Sprite.TRANS_ROT180, 
                        x + getWidth() - circleSize, y + getHeight() - circleSize, 0);
                g.drawRegion(circleImage, 
                        0, 0, circleSize, circleSize, Sprite.TRANS_ROT270, 
                        x, y + getHeight() - circleSize, 0);
            } else {
                g.fillRect(x, y, getWidth(), circleSize);
                g.fillRect(x, y + getHeight() - circleSize, getWidth(), circleSize);
            }
            
            renderShadow = false;
        }

        g.fillRect(x, y + circleSize, getWidth(), getHeight() - circleSize - circleSize);

        paint(g, x, y + circleSize,
                getWidth(), getHeightItems(), getHeightItems(), false);

    }

    public void keyPressed(int key) {
        super.keyPressed(key);
        open = key == AppCanvas.DOWN || key == AppCanvas.UP
                || key == AppCanvas.ENTER || key == AppCanvas.FIRE
                || key == AppCanvas.KEY_NUM8 || key == AppCanvas.KEY_NUM2
                || ((key == AppCanvas.LEFT || key == AppCanvas.RIGHT)
                && getSelected() instanceof PaneItem.ItemThatUsesLeftAndRight);
    }

    public boolean isPointInMenu(int x, int y) {
        return !(y < this.y + AppCanvas.instance.boldFont.getHeight() / 2
                || x < this.x
                || y >= this.y + AppCanvas.instance.boldFont.getHeight() / 2 + getHeightItems()
                || x >= this.x + getWidth());
    }

    public boolean isPointInBox(int x, int y) {
        return !(y < this.y
                || x < this.x
                || y >= this.y + getHeight()
                || x >= this.x + getWidth());
    }

    public void pointerPressed(int x, int y) {
        pointerWasPressed = true;
        pointerPressedInPopup = isPointInBox(x, y);
        pointerPressedInMenu = isPointInMenu(x, y);

        if (pointerPressedInMenu) {
            super.pointerPressed(x - this.x, y + AppCanvas.instance.contentY - this.y - AppCanvas.instance.boldFont.getHeight() / 2);
        }

    }

    public void pointerReleased(int x, int y) {
        if (!pointerWasPressed) {
            return;
        }
        pointerWasPressed = false;

        if (pointerPressedInMenu) {
            //todo
            /*PaneItem item = getSelected();
             if(item != null && item.pressed && item.focusable) open = false;*/

            super.pointerReleased(x - this.x, y + AppCanvas.instance.contentY - this.y - AppCanvas.instance.boldFont.getHeight() / 2);
        } else if (!isPointInBox(x, y) && !pointerPressedInPopup) {
            open = false;
        }
    }

    public void pointerDragged(int x, int y) {
        if (pointerPressedInMenu) {
            super.pointerDragged(x - this.x, y + AppCanvas.instance.contentY - this.y - AppCanvas.instance.boldFont.getHeight() / 2);
        }
    }
}
