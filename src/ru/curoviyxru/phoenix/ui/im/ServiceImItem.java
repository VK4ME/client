package ru.curoviyxru.phoenix.ui.im;

import javax.microedition.lcdui.Graphics;
import ru.curoviyxru.j2vk.api.objects.ImItem;
import ru.curoviyxru.phoenix.ui.Label;
import ru.curoviyxru.phoenix.Theming;

/**
 *
 * @author curoviyxru
 */
public class ServiceImItem extends Label implements ImHolder {
    
    private ImItem m;
    
    public ServiceImItem(ImContent c, ImItem message) {
        super(message.toString(true, !c.isComments() && !c.isChat(), true, !message.hasReplyMessage(), !message.hasForwardedMessages()));
        this.m = message;
        setAlign(c.isComments() ? Graphics.LEFT : Graphics.HCENTER);
        setColor(Theming.now.actionMessageColor);
        setFocusable(false);
    }

    public int id() {
        return m.id();
    }

    public boolean out() {
        return m.out();
    }
}