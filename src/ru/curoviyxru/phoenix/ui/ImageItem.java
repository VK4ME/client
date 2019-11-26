package ru.curoviyxru.phoenix.ui;

import ru.curoviyxru.phoenix.Theming;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import ru.curoviyxru.j2vk.api.objects.attachments.Photo;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.contents.ImageViewer;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class ImageItem extends PaneItem implements ImageProvider {
    
    public Image cached;
    public int iw, ih;
    public boolean local;
    public String url;
    public boolean doNotCircle;
    public byte align = Graphics.LEFT;
    int tries;
    Photo photo;
    
    public int tries() {
        return tries;
    }
    
    public void tried() {
        if (tries > 5) return;
        ++tries;
    }
    
    public ImageItem(Photo photo, String url, boolean local, int pw, int ph) {
        cAction = Localization.get("action.reveal");
        //setFocusable(false);
        this.photo = photo;
        this.iw = pw;
        this.ih = ph;
        this.local = local;
        this.url = Midlet.instance.config.doNotLoadImages ? null : url;
        skipSelection(Midlet.instance.config.doNotLoadImages);
        AppCanvas.instance.queue(this);
    }
    
    public void actionPerformed() {
        if (photo != null)
            AppCanvas.instance.goTo(ImageViewer.instance.setPhoto(photo, null, this).parent(content));
    }

    public ImageItem setAlign(int a) {
        this.align = (byte) a;
        return this;
    }

    public void updateHeight() {
        if (url == null) {
            height = 0;
        } else {
            if (cached != null) {
            ih = Math.min(ih, cached.getHeight());
            iw = Math.min(iw, cached.getWidth());
        }
            height = ih + AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace;
        }
    }

    public void paint(Graphics g, int pY, int ppX) {
        updateHeight();
        
        if (url == null) return;
        
        pY += AppCanvas.instance.perLineSpace;

        int pX = (align == Graphics.LEFT ? 0 : align == Graphics.RIGHT ? width - iw : (width - iw) / 2);

        if (cached != null) {
            g.drawRegion(cached, (cached.getWidth() - iw) / 2, (cached.getHeight() - ih) / 2, iw, ih, 0, x + ppX + pX, y + pY, Graphics.TOP | Graphics.LEFT);
        } else {
            AppCanvas.instance.queue(this);
            g.setColor(Theming.now.nonLoadedContentColor);
            g.fillRect(x + ppX + pX, y + pY, iw, ih);
        }
        if (!doNotCircle && AppCanvas.tranImageStandard != null) {
            g.drawImage(AppCanvas.tranImageStandard, x + ppX + pX, y + pY, Graphics.LEFT | Graphics.TOP);
            
            g.drawRegion(AppCanvas.tranImageStandard, 
                    0, 0, AppCanvas.tranSize, AppCanvas.tranSize, Sprite.TRANS_ROT90, 
                    x + ppX + pX + iw - AppCanvas.tranSize, y + pY, 0);
            g.drawRegion(AppCanvas.tranImageStandard, 
                    0, 0, AppCanvas.tranSize, AppCanvas.tranSize, Sprite.TRANS_ROT180, 
                    x + ppX + pX + iw - AppCanvas.tranSize, y + pY + ih - AppCanvas.tranSize, 0);
            g.drawRegion(AppCanvas.tranImageStandard, 
                    0, 0, AppCanvas.tranSize, AppCanvas.tranSize, Sprite.TRANS_ROT270, 
                    x + ppX + pX, y + pY + ih - AppCanvas.tranSize, 0);
        }

        if (pressed && focusable) {
            g.setColor(Theming.now.focusedBorderColor);
            g.drawRect(x + ppX + pX, y + pY, iw, ih);
        }
    }

    public ImageItem doNotCircle(boolean b) {
        this.doNotCircle = b;
        return this;
    }
    
    public int size() {
        return 1;
    }

    public boolean local(int i) {
        return local;
    }

    public String get(int i) {
        return cached == null ? url : null;
    }

    public void set(int i, Image image) {
        if (image == null) return;
        cached = RenderUtil.resizeImage(image, Math.min(ih, iw * image.getHeight() / Math.max(1, image.getWidth())));
        ih = cached.getHeight();
        iw = cached.getWidth();
    }
    
    public void errored(Throwable ex) {
        AppCanvas.instance.dropError("Image item: " + ex != null ? ex.getMessage() : "Unknown error");
    }
}
