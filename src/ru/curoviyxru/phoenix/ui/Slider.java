package ru.curoviyxru.phoenix.ui;

import ru.curoviyxru.phoenix.Theming;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class Slider extends ProgressBar implements PaneItem.ItemThatUsesLeftAndRight {

	public static int buttonHeight;
	public static Image button, buttonBorder,
			focusedButton, focusedButtonBorder;
	
    public int pheight;
    public int repeats;

    public void valueChanged(long o, long n) {
    }

    public Slider(int p) {
        focusable = true;
        height = AppCanvas.instance.normalFont.getHeight();
        pheight = height / 4;
        height += AppCanvas.instance.perLineSpace * 2;
        setMaxProgress(100);
        progress = p;
        //checkVal();
    }

    public Slider() {
        this(0);
    }

    public PaneItem pointerDragged(int x, int y, int pY) {
        if (pressed) {
            //long old = progress;
            progress = AppCanvas.round((double) 
					(x - this.x - buttonHeight / 2) * maxProgress / (width - 1 - buttonHeight));
            checkVal();
            //valueChanged(old, progress);
        }

        return this;
    }

    public ProgressBar setProgress(long p) {
        long old = progress;
        //super.setProgress(p);
        this.progress = p;
        valueChanged(old, progress);
        return this;
    }

    public PaneItem pointerReleased(int x, int y, int pY) {
        if (pressed) {
            long old = progress;
            progress = AppCanvas.round((double) 
					(x - this.x - buttonHeight / 2) * maxProgress / (width - 1 - buttonHeight));
            checkVal();
			valueChanged(old, progress);
        }
		
        super.pointerReleased(x, y, pY);
        //checkVal();
        return this;
    }

    public PaneItem keyPressed(int k, int pY) {
        repeats = 0;
        switch (k) {
            case AppCanvas.LEFT:
                long old = progress;
                progress -= Math.max(1, maxProgress / 20);
                checkVal();
                valueChanged(old, progress);
                break;
            case AppCanvas.RIGHT:
                long old1 = progress;
                progress += Math.max(1, maxProgress / 20);
                checkVal();
                valueChanged(old1, progress);
                break;
        }

        return this;
    }

    public PaneItem keyRepeated(int k, int pY) {
        repeats++;
        switch (k) {
            case AppCanvas.LEFT:
                progress -= Math.max(1, maxProgress * repeats * 3 / 40);
                checkVal();
                break;
            case AppCanvas.RIGHT:
                progress += Math.max(1, maxProgress * repeats * 3 / 40);
                checkVal();
                break;
        }

        return this;
    }

    public PaneItem keyReleased(int k, int pY) {
        //checkVal();
		if(repeats > 0) valueChanged(progress, progress);
        repeats = 0;
        return this;
    }

    public PaneItem pointerPressed(int x, int y, int pY) {
        super.pointerPressed(x, y, pY);
        return this;
    }

    public void paint(Graphics g, int pY, int pX) {
        //checkVal();
        pY += AppCanvas.instance.perLineSpace;

        int p = (int) ((width - buttonHeight) * Math.min(progress, maxProgress) / maxProgress) + buttonHeight / 2;
		int progressY = y + pY + (height - AppCanvas.instance.perLineSpace * 2 - pheight) / 2;
		
        RenderUtil.fillRect(g, 
				x + pX + p, progressY, 
				width - p, pheight, 
				Theming.now.sliderColor, Theming.now.sliderColor_);
		
        RenderUtil.fillRect(g, 
				x + pX, progressY, 
				p, pheight, 
				Theming.now.sliderLoadedColor, Theming.now.sliderLoadedColor_);
        
		int buttonX = x + pX + p - buttonHeight / 2;
		buttonX = Math.min(Math.max(buttonX, x + pX), x + pX + width - buttonHeight);
		int buttonY = progressY + pheight / 2 - buttonHeight / 2;
		
        if(button != null && buttonBorder != null && 
				focusedButton != null && focusedButtonBorder != null) {
			boolean focused = pressed && focusable;
			
			g.drawImage(focused ? focusedButton : button, buttonX, buttonY, 0);
			
			Image border = focused ? focusedButtonBorder : buttonBorder;
			int borderSize = border.getHeight();
			
			g.drawImage(border, buttonX, buttonY, 0);
			g.drawRegion(border, 
					0, 0, borderSize, borderSize, 
					Sprite.TRANS_ROT90, 
					buttonX + borderSize, buttonY, 0);
			g.drawRegion(border, 
					0, 0, borderSize, borderSize, 
					Sprite.TRANS_ROT180, 
					buttonX + borderSize, buttonY + borderSize, 0);
			g.drawRegion(border, 
					0, 0, borderSize, borderSize, 
					Sprite.TRANS_ROT270, 
					buttonX, buttonY + borderSize, 0);
		}
    }
}
