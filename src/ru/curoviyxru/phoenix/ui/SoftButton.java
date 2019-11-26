package ru.curoviyxru.phoenix.ui;

import javax.microedition.lcdui.Command;

/**
 *
 * @author curoviyxru
 */
public class SoftButton {
    Command lcduiCommand;
    String title;
    int limitedWidth;
    String limitedTitle;
    boolean left;
    
    public SoftButton(String title) {
        this(title, false);
    }
    
    public SoftButton(String title, boolean left) {
        this.left = left;
        setTitle(title);
    }
    
    public void setTitle(String title) {
        this.title = title == null ? null : title.intern();
        lcduiCommand = title == null ? null : new Command(title, left ? Command.BACK : Command.SCREEN, 1);
        limitedTitle = null;
        limitedWidth = 0;
    }
    
    public void trigger() {
        
    }
}
