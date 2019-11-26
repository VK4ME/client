package ru.curoviyxru.phoenix.ui;

import ru.curoviyxru.phoenix.ui.contents.ContentController;
import ru.curoviyxru.phoenix.ui.contents.ScrollContent;
import ru.curoviyxru.phoenix.ui.contents.TwoScrollContent;
import java.util.Hashtable;
import ru.curoviyxru.phoenix.Theming;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.midlet.Midlet;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class Content {

    public Content parent;
    private SoftButton parentBack;
    public Hashtable screenCache = new Hashtable();
    private Vector container = new Vector(10, 20);
    public int totalHeight, scrollY, toScrollY, selectedY;
    private float kineticScroll;
    private float kineticStore;
    //totalHeight is items height
    public int perOneY;
    private boolean needUpdate;
    public SuperString title;
    public SuperString renderTitle;
    public int lW;
    public int lastWidth, lastHeight;
    public int contentHeight; //kinda renderHeight
    public PopupMenu rightSoft;
    public String cornerIcon;

    public Content setCornerIcon(String cornerIcon) {
        if ((this.cornerIcon == null && cornerIcon != null) || (this.cornerIcon != null && cornerIcon == null)) {
            this.renderTitle = null;
        }
        this.cornerIcon = cornerIcon;
        return this;
    }

    //public String desc;
    //public String renderDesc;
    //public Image renderImage, image;
    public Content parent(final Content parent) {
        this.parent = parent;
        if (parent == null && !(Midlet.instance.config.gui_useDrawer && Midlet.instance.config.gui_touchHud && ContentController.menu != null)) {
            parentBack = null;
        } else {
            parentBack = new SoftButton(Localization.get("action.back"), true) {
                public void trigger() {
                    if (Midlet.instance.config.gui_useDrawer && Midlet.instance.config.gui_touchHud && ContentController.menu != null) {
                        if (AppCanvas.instance.drawerBack == null) {
                            if (ContentController.menu != null) {
                                AppCanvas.instance.backTo(ContentController.menu.parent(Content.this));
                            }
                            AppCanvas.instance.drawerBack = Content.this;
                        } else {
                            AppCanvas.instance.goTo(AppCanvas.instance.drawerBack);
                            if (ContentController.menu != null) {
                                ContentController.menu.parent(null);
                            }
                            AppCanvas.instance.drawerBack = null;
                        }
                    } else {
                        if (ContentController.menu != null) {
                            ContentController.menu.parent(null);
                        }
                        AppCanvas.instance.backTo(parent);
                    }
                }
            };
        }
        if (AppCanvas.instance.content != null && AppCanvas.instance.content.equals(this)) {
            AppCanvas.instance.setLeftSoft(parentBack);
        }
        return this;
    }

    public void goBack() {
        AppCanvas.instance.backTo(parent);
    }

    public void opened() {
        AppCanvas.instance.setLeftSoft(parentBack);
    }

    public void renderIfNeeded() {
        if (AppCanvas.instance.content != null && AppCanvas.instance.content.hashCode() == this.hashCode()) {
            AppCanvas.instance.render();
        }
    }

    public void itemRendered(PaneItem i, Graphics g, int pY, int pX) {
    }

    public Content(String s) {
        setTitle(s);
    }

    public Content imOut() {
        removeAll();

        AppCanvas.instance.dropError(Localization.get("general.outOfMemory"));
        AppCanvas.instance.setProgress(false);

        return this;
    }

    public int getWidth() {
        return AppCanvas.instance.lW;
    }

    public void updateHeightsFromEnd(int renderWidth, int i) {
        updateHeights(renderWidth, -1, i, false);
    }

    public void updateHeights(int renderWidth, int i) {
        updateHeights(renderWidth, i, -1, false);
    }

    public void updateHeightsAdd(int renderWidth, int i) {
        updateHeights(renderWidth, i, -1, true);
    }

    public void updateHeights(int renderWidth, int updFromStart, int updFromEnd, boolean addHeight) {
        synchronized (this) {
            int x = AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace;
            int y = 0;
            int width = renderWidth - (AppCanvas.instance.perLineSpace * 4); // - (contentHeight < totalHeight ? (AppCanvas.instance.scrollWidth * 2) : 0);

            for (int i = 0; i < container.size(); i++) {
                PaneItem item = (PaneItem) container.elementAt(i);
                if (item == null) {
                    continue;
                }
                item.content = this;

                if (item instanceof PaneItem.ItemThatUsesFullWidth) {
                    item.width = renderWidth;
                    item.x = 0;
                } else {
                    item.width = width;
                    item.x = x;
                }

                if (item.separatorBefore) {
                    y++;
                }

                item.y = y;

                if (i > container.size() - updFromEnd - 1 || i < updFromStart) {
                    item.updateHeight(); //check all elements
                }
                y += item.height + (item.separatorAfter ? 1 : 0);
            }

            if (updFromStart != -1 && selectedY >= updFromStart) {
                scrollY += y - totalHeight;
                toScrollY += y - totalHeight;
            }

            totalHeight = y;
        }
    }

    public void paint(Graphics g, int pX) {
        paint(g, pX, AppCanvas.instance.contentY, AppCanvas.instance.getWidth(), AppCanvas.instance.contentOriginalHeight, AppCanvas.instance.getHeight(), true);
    }

    public void paint(Graphics g, int h, int pX) {
        paint(g, pX, AppCanvas.instance.contentY, AppCanvas.instance.getWidth(), h, AppCanvas.instance.getHeight(), true);
    }

    public void paint(Graphics g, int pX, int pY, int renderWidth, int renderHeight, int fullHeight, boolean drawBG) {
        synchronized (this) {
            contentHeight = renderHeight;
            AppCanvas.instance.contentHeight = contentHeight;
            int oldClipX = g.getClipX();
            int oldClipY = g.getClipY();
            int oldClipW = g.getClipWidth();
            int oldClipH = g.getClipHeight();
            g.setClip(pX, pY, renderWidth, renderHeight);

            if (drawBG) {
                g.setColor(Theming.now.backgroundColor);
                g.fillRect(pX, pY, renderWidth, renderHeight);
            }

            boolean needAnim = false;

            if (kineticScroll != 0/* || kineticStore != 0*/) {
                kineticScroll *= (AppCanvas.instance.pressed ? 0.5f : 0.96f);
                //kineticStore *= (AppCanvas.instance.pressed ? 0.5f : 0.96f);

                /*if(!AppCanvas.instance.pressed) {
                 kineticScroll += kineticStore;
                 kineticStore = 0;
                 }*/

                if (kineticScroll > 1 || kineticScroll < -1) {
                    toScrollY += (int) kineticScroll;
                    checkY();
                    scrollY = toScrollY;
                } else {
                    kineticScroll = 0;
                }

                needAnim = true;
            }

            checkY();
            if (toScrollY != scrollY) {
                if (!Midlet.instance.config.gui_smoothScroll) {
                    scrollY = toScrollY;
                } else if (Math.abs(toScrollY - scrollY) < 4) {
                    scrollY += toScrollY > scrollY ? 1 : -1;
                } else {
                    scrollY += (toScrollY - scrollY) / 4;
                }

                if (toScrollY != scrollY) {
                    needAnim = true;
                }
            }

            //checkYValidity();

            int x = AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace;
            int y = 0;
            int pad = pY - scrollY;
            int width = renderWidth - (AppCanvas.instance.perLineSpace * 4); // - (contentHeight < totalHeight ? (AppCanvas.instance.scrollWidth * 2) : 0);

            for (int i = 0; i < container.size(); i++) {
                PaneItem item = (PaneItem) container.elementAt(i);
                if (item == null) {
                    continue;
                }
                item.content = this;

                if (AppCanvas.instance.showSelection) {
                    item.pressed = (i == selectedY);
                }

                if (item instanceof PaneItem.ItemThatUsesFullWidth) {
                    item.width = renderWidth;
                    item.x = 0;
                } else {
                    item.width = width;
                    item.x = x;
                }

                if (item.separatorBefore) {
                    g.setColor(Theming.now.itemSeparatorColor);
                    g.drawLine(pX, y + pad, renderWidth + pX, y + pad);
                    y++;
                }
                item.y = y;

                //item.updateHeight();

                if (item.y + item.height - scrollY >= 0) {
                    if (item.y - scrollY < renderHeight) {
                        itemRendered(item, g, pad, pX);
                        item.paint(g, pad, pX);

                        if (item.separatorAfter) {
                            g.setColor(Theming.now.itemSeparatorColor);
                            g.drawLine(pX, y + pad + item.height, renderWidth + pX, y + pad + item.height);
                        }
                    } else if (item.y - scrollY <= fullHeight) {
                        item.updateHeight();
                    }
                }

                y += item.height + (item.separatorAfter ? 1 : 0);
            }
            needUpdate = (lastHeight != renderHeight && lastWidth != renderWidth) || (totalHeight < y && totalHeight <= contentHeight) || (totalHeight > y && y <= contentHeight);
            lastWidth = renderWidth;
            lastHeight = renderHeight;
            totalHeight = y;

            if (totalHeight > contentHeight && toScrollY + contentHeight > totalHeight) {
                toScrollY = totalHeight - contentHeight;
            } else if (toScrollY < 0) {
                toScrollY = 0;
            }

            if (toScrollY != scrollY) {
                needAnim = true;
            }

            g.setClip(oldClipX, oldClipY, oldClipW, oldClipH);

            if (needUpdate) {
                for (int i = 0; i < container.size(); i++) {
                    ((PaneItem) container.elementAt(i)).resetCache();
                }
                AppCanvas.instance.render();
                return;
            }

            if (!needUpdate && needAnim) {
                AppCanvas.instance.render();
            }
        }
    }

    public void onScrollUpdate() {
    }

    public void pointerPressed(int x, int y) {
        for (int i = 0; i < container.size(); i++) {
            PaneItem item = (PaneItem) container.elementAt(i);
            if (item != null) {
                boolean oldPressed = item.pressed;
                item.pointerPressed(x, y, toScrollY); //think about performance
                if (!oldPressed && item.pressed) {
                    selectedY = i;
                }
            }
        }
    }

    public void pointerReleased(int x, int y) {
        for (int i = 0; i < container.size(); i++) {
            PaneItem item = (PaneItem) container.elementAt(i);
            if (item != null) {
                item.pointerReleased(x, y, toScrollY); //think about performance
            }
        }
        //kineticStore += move; //kinetic scroll
        kineticScroll += kineticStore;
        kineticStore = 0;
    }

    //Fix weird scrolling
    public void pointerDragged(int x, int y) {
        //lxy - previous
        //pxy - current
        //fxy - first
        boolean skipScroll = false;
        for (int i = 0; i < container.size(); i++) {
            PaneItem item = (PaneItem) container.elementAt(i);
            if (item != null) {
                item.pointerDragged(x, y, toScrollY); //think about performance
                if (item instanceof Slider) {
                    Slider s = (Slider) item;
                    if (s.pressed) {
                        skipScroll = true;
                    }
                }
            }
        }

        if (skipScroll) {
            return;
        }
        int move = AppCanvas.instance.lY - AppCanvas.instance.pY;
        toScrollY += move;
        kineticStore = move; //kinetic scroll
        //kineticStore += move; //kinetic scroll
        checkY();
        scrollY = toScrollY;

        onScrollUpdate(); //think about performance      
    }

    public boolean parseKeyRepeated(int p) {
        switch (p) {
            case AppCanvas.UP:
                if (container.size() > 1) {
                    up();
                    return true;
                }
                break;
            case AppCanvas.DOWN:
                if (container.size() > 1) {
                    down();
                    return true;
                }
                break;
            case AppCanvas.KEY_NUM2:
                if (container.size() > 1) {
                    pageScroll(true);
                    return true;
                }
                break;
            case AppCanvas.KEY_NUM8:
                if (container.size() > 1) {
                    pageScroll(false);
                    return true;
                }
                break;
        }
        return false;
    }

    public boolean parseKeyPressed(int p) {
        switch (p) {
            case AppCanvas.UP:
            case AppCanvas.DOWN:
            case AppCanvas.KEY_NUM2:
            case AppCanvas.KEY_NUM8:
                return parseKeyRepeated(p);
        }
        return false;
    }

    public void keyPressed(int p) {
        if (parseKeyPressed(p)) {
            return;
        }

        PaneItem item = getSelected();
        if (item != null) {
            item.keyPressed(p, toScrollY);
        }
    }

    public void keyReleased(int p) {
        PaneItem item = getSelected();
        if (item != null) {
            item.keyReleased(p, toScrollY);
        }
    }

    public void keyRepeated(int p) {
        if (parseKeyRepeated(p)) {
            return;
        }

        PaneItem item = getSelected();
        if (item != null) {
            item.keyRepeated(p, toScrollY);
        }
    }

    public void down() {
        AppCanvas.instance.showScroll();
        PaneItem item = getSelected();
        if (item != null) {
            if (item.height + AppCanvas.instance.perLineSpace * 2 > contentHeight && perOneY != item.height) {
                int max = item.y + item.height - contentHeight + AppCanvas.instance.perLineSpace;
                toScrollY += contentHeight / 5;
                if (toScrollY >= max) {
                    perOneY = item.height;
                    toScrollY = max;
                } else {
                    perOneY += contentHeight / 5;
                }
                return;
            }
        }

        selectedY++;
        checkYValidity();
        scrollToItemDown();
        checkSkippableDown();
        checkYValidity();

        scrollToItemUp();
        scrollToItemDown();

        onScrollUpdate();
    }

    private void up() {
        AppCanvas.instance.showScroll();
        PaneItem item = getSelected();
        if (item != null) {
            if (item.height + AppCanvas.instance.perLineSpace * 2 > contentHeight && perOneY != 0) {
                int min = item.y - AppCanvas.instance.perLineSpace;
                toScrollY -= contentHeight / 5;
                if (toScrollY <= min) {
                    perOneY = 0;
                    toScrollY = min;
                } else {
                    perOneY -= contentHeight / 5;
                }
                return;
            }
        }

        selectedY--;
        checkYValidity();
        scrollToItemUp();
        checkSkippableUp();
        checkYValidity();

        scrollToItemDown();
        scrollToItemUp();

        onScrollUpdate();
    }

    private void pageScroll(boolean up) {
        AppCanvas.instance.showScroll();
        toScrollY += (contentHeight - AppCanvas.instance.normalFont.getHeight()) * (up ? -1 : 1);
        checkY();
        selectElementInCenter();
        onScrollUpdate();
    }

    public void selectElementInCenter() {
        PaneItem selected = getSelected();
        if (selected == null) {
            return;
        }

        int elementY = selected.y + selected.height / 2;

        int step = 1;
        if (toScrollY < elementY) {
            step = -1;
        }

        int distance = Integer.MAX_VALUE;
        int nearestId = -1;

        for (int i = selectedY; i >= 0 && i < container.size(); i += step) {
            //Идём от текущего элемента в сторону скролла
            PaneItem item = (PaneItem) container.elementAt(i);
            if (item.skipSelection) {
                continue;
            }

            if (item.y < toScrollY + contentHeight / 2 && item.y + item.height - toScrollY > toScrollY + contentHeight / 2) {
                selectedY = i;
                break; //Если элемент в центре экрана
            }

            int d = Math.abs(toScrollY + contentHeight / 2 - (item.y + item.height / 2)) - item.height / 2;
            //Если элемент не в центре экрана, то замеряем расстояние от краёв элемента до центра экрана
            //(вычитание половины высоты элемента нужно для того, чтобы мы замеряли расстояние ОТ КРАЁВ элемента)
            if (d < distance) {
                distance = d;
                nearestId = i;
            } else {
                selectedY = i - step;
                break; //Если расстояние становится больше, значит мы уходим от центра
            }
        }

        if (nearestId != -1) {
            /*
             Расстояние не стало увеличиваться, 
             скорее всего мы упёрлись в край экрана,
             так что берём самый близкий к центру элемент.
             */
            selectedY = nearestId;
        }
    }

    public PaneItem getSelected() {
        if (selectedY >= 0 && selectedY < container.size()) {
            return (PaneItem) container.elementAt(selectedY);
        }

        return null;
    }

    public Field getSelectedField() {
        PaneItem obj = getSelected();
        return obj != null && obj instanceof Field ? (Field) obj : null;
    }

    boolean isFieldSelected() {
        Object obj = getSelectedField();
        return obj != null;
    }

    public void setTitle(String t) {
        this.title = t == null ? null : new SuperString(t);
        this.renderTitle = null;
        if (AppCanvas.instance.content != null && AppCanvas.instance.content.equals(this)) {
            AppCanvas.instance.setTitle(t == null ? "VK4ME" : t);
        }
    }

    public String getRightSoft() {
        if (rightSoft == null) {
            return null;
        }

        return rightSoft.title.toString();
    }

    public void rightSoft() {
        if (AppCanvas.instance.popupOpened()) {
            AppCanvas.instance.closePopup();
            return;
        }
        if (rightSoft != null) {
            AppCanvas.instance.showRightsoftPopup(rightSoft);
        }
    }

    public void scrollTo(int selectedY) {
        this.selectedY = selectedY;
        checkYValidity();
        PaneItem item = getSelected();

        if (item == null) {
            return;
        }

        scrollToItemUp();
        scrollToItemDown();
        checkSkippableUp();
        checkYValidity();

        AppCanvas.instance.render();
    }

    private void checkY() {
        if (contentHeight < totalHeight) {
            if (toScrollY < 0) {
                toScrollY = 0;
            }
            int maxS = totalHeight - contentHeight;
            if (toScrollY > maxS) {
                toScrollY = maxS;
            }
        } else {
            toScrollY = 0;
        }
    }

    private void checkSkippableDown() {
        int old = selectedY - 1;
        boolean been = false;
        while (selectedY != old) {
            PaneItem i = getSelected();
            if (i != null && !i.skipSelection) {
                return;
            }
            selectedY++;
            if (selectedY >= container.size()) {
                selectedY = 0;
                if (been) {
                    break;
                }
                been = true;
            }
        }
    }

    private void checkSkippableUp() {
        int old = selectedY + 1;
        boolean been = false;
        while (selectedY != old) {
            PaneItem i = getSelected();
            if (i != null && !i.skipSelection) {
                return;
            }
            selectedY--;
            if (selectedY < 0) {
                selectedY = container.isEmpty() ? 0 : size() - 1;
                if (been) {
                    break;
                }
                been = true;
            }
        }
    }

    private void checkYValidity() {
        if (selectedY > container.size() - 1) {
            selectedY = this instanceof ScrollContent
                    || this instanceof TwoScrollContent ? container.size() - 1 : 0;
            checkSkippableDown();
        }
        if (selectedY < 0) {
            selectedY = this instanceof ScrollContent
                    || this instanceof TwoScrollContent || container.isEmpty() ? 0 : container.size() - 1;
            checkSkippableUp();
        }
    }

    private void scrollToItemDown() {
        PaneItem item = getSelected();

        if (item == null) {
            return;
        }

        boolean bigItem = item.height + AppCanvas.instance.perLineSpace * 2 > contentHeight;

        if (!bigItem) {
            if (item.y - toScrollY + item.height + AppCanvas.instance.perLineSpace >= contentHeight) {
                toScrollY = item.y + item.height - contentHeight + AppCanvas.instance.perLineSpace;
                perOneY = item.height;
            }
        } else {
            if (item.y + item.height - toScrollY >= contentHeight) {
                toScrollY = item.y - AppCanvas.instance.perLineSpace;
                perOneY = contentHeight - AppCanvas.instance.perLineSpace;
            }
        }

        AppCanvas.instance.render();
    }

    private void scrollToItemUp() {
        PaneItem item = getSelected();

        if (item == null) {
            return;
        }

        boolean bigItem = item.height + AppCanvas.instance.perLineSpace * 2 > contentHeight;

        if (!bigItem) {
            if (item.y - toScrollY - AppCanvas.instance.perLineSpace <= 0) {
                toScrollY = item.y - AppCanvas.instance.perLineSpace;
                perOneY = 0;
            }
        } else {
            if (item.y + item.height - contentHeight - toScrollY <= 0) {
                toScrollY = item.y + item.height - contentHeight + AppCanvas.instance.perLineSpace;
                perOneY = item.height - contentHeight + AppCanvas.instance.perLineSpace;
            }
        }

        AppCanvas.instance.render();
    }

    public Content removeAt(int i) {
        if (i < 0 || i >= size()) {
            return this;
        }
        container.removeElementAt(i);
        return this;
    }

    public Content insert(PaneItem item, int i) {
        if (item == null) {
            return this;
        }
        item.content = this;
        if (item instanceof ImageProvider) {
            AppCanvas.instance.queue((ImageProvider) item);
        }
        container.insertElementAt(item, i);
        return this;
    }

    public Content remove(PaneItem i) {
        return removeAt(indexOf(i));

    }

    public int indexOf(PaneItem item) {
        return container.indexOf(item);
    }

    public boolean contains(PaneItem i) {
        return indexOf(i) != -1;
    }

    public Content add(PaneItem item) {
        if (item == null) {
            return this;
        }
        item.content = this;
        if (item instanceof ImageProvider) {
            AppCanvas.instance.queue((ImageProvider) item);
        }
        container.addElement(item);
        return this;
    }

    public PaneItem at(int i) {
        return (PaneItem) container.elementAt(i);
    }

    public int size() {
        return container.size();
    }

    public boolean isEmpty() {
        return container.isEmpty();
    }

    public PaneItem last() {
        return (PaneItem) container.lastElement();
    }

    public Content removeAll() {
        container.removeAllElements();
        System.gc();
        return this;
    }
}
