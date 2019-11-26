package ru.curoviyxru.phoenix.ui;

import ru.curoviyxru.phoenix.Theming;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class ProgressBar extends PaneItem {

    public long progress;
    public long maxProgress;

    public void checkVal() {
        if (progress < 0) {
            progress = 0;
        } else if (progress > maxProgress) {
            progress = maxProgress;
        }
    }

    public ProgressBar(int p) {
        this(AppCanvas.instance.normalFont.getHeight() / 4, p);
    }

    public ProgressBar(int height, int p) {
        focusable = false;
        this.height = height + AppCanvas.instance.perLineSpace * 2;
        setMaxProgress(100);
        this.progress = p;
        //checkVal();
    }

    public ProgressBar setProgress(long p) {
        this.progress = p;
        //checkVal();
        return this;
    }

    public ProgressBar setMaxProgress(long p) {
        this.maxProgress = p;
        //checkVal();
        return this;
    }

    public ProgressBar() {
        this(0);
    }

    public void paint(Graphics g, int pY, int pX) {
        //checkVal();
        pY += AppCanvas.instance.perLineSpace * 2;

        int p = (int) (width * Math.min(progress, maxProgress) / maxProgress);
        RenderUtil.fillRect(g, x + pX + p, y + pY, width - p, height - AppCanvas.instance.perLineSpace * 2, Theming.now.sliderColor, Theming.now.sliderColor_);
        
        RenderUtil.fillRect(g, x + pX, y + pY, p, height - AppCanvas.instance.perLineSpace * 2, Theming.now.sliderLoadedColor, Theming.now.sliderLoadedColor_);
    }

    public void updateHeight() {
    }
}
