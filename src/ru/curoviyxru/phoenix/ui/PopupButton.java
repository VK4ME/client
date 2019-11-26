package ru.curoviyxru.phoenix.ui;

/**
 *
 * @author Roman Lahin
 */
public class PopupButton extends ListItem {

    public PopupMenu popup;

    public PopupButton(String title, PopupMenu popup) {
        super(title, popup != null && popup.active ? ListItem.GOTO : ListItem.TEXT);
        this.popup = popup;
    }

    public PopupButton(String caption) {
        this(caption, null);
    }

    public PopupButton() {
        this(null, null);
    }

    public void actionPerformed() {
        if (popup != null && popup.active) {
            AppCanvas.instance.showPopup(popup); //todo make relative position
        }
    }
}
