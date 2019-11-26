package ru.curoviyxru.phoenix.ui.contents;

import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.PopupButton;
import ru.curoviyxru.phoenix.ui.PopupMenu;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class ScrollContent extends Content {

    public Object next, addon;
    public final Object lock = new Object();
    public boolean noNext = false, loading = false;

    public Content imOut() {
        noNext = true;
        return super.imOut();
    }

    public ScrollContent(String s, boolean refresh) {
        super(s);

        rightSoft = new PopupMenu(Localization.get("general.actions"));
        rightSoft.add(new PopupButton(Localization.get("action.refresh")) {
            public void actionPerformed() {
                refresh();
                AppCanvas.instance.closePopup();
            }
        }.setIcon("new/refresh.rle"));

        if (refresh) {
            refresh();
        }
    }

    public void clear() {
        if (loading) return;
        
        synchronized (lock) {
            removeAll();
            next = null;
            addon = null;
            noNext = false;
        }
    }

    public void refresh() {
        if (loading) return;
        
        synchronized (lock) {
            clear();
            loadNext();
        }
    }

    public void onScrollUpdate() {
        if (!noNext && totalHeight - toScrollY - AppCanvas.instance.contentHeight < AppCanvas.instance.perLineSpace) {
            loadNext();
        }
    }

    public void process() {
    }

    public void shorten() {
        refresh();
    }

    public void loadNext() {
        if (loading || noNext) {
            return;
        }
        
        loading = true;
        new Thread() {
            public void run() {
                try {
                    synchronized (lock) {
                        while (!noNext && totalHeight - toScrollY - AppCanvas.instance.contentHeight < AppCanvas.instance.perLineSpace)
                            process();
                        loading = false;
                    }
                } catch (Exception e) {
                    AppCanvas.instance.dropError(e);
                } catch (OutOfMemoryError error) {
                    imOut();
                }
                
                AppCanvas.instance.setProgress(false);
            }
        }.start();
    }
}
