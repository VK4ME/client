package ru.curoviyxru.phoenix.ui;

import javax.microedition.lcdui.Image;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public interface ImageProvider {
    int size();
    boolean local(int i);
    String get(int i);
    void set(int i, Image image);
    int tries();
    void tried();
    void errored(Throwable ex);
}
