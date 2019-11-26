package ru.curoviyxru.phoenix.ui.contents.settings;

import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.Theming;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.Label;
import ru.curoviyxru.phoenix.ui.ListItem;
import ru.curoviyxru.phoenix.ui.PopupButton;
import ru.curoviyxru.phoenix.ui.PopupMenu;
import ru.curoviyxru.phoenix.ui.contents.SettingsContent;

/**
 *
 * @author curoviyxru
 */
public class LocalizationSettings extends Content {

    public LocalizationSettings() {
        super(Localization.get("title.localizationSettings"));

        add(new Label(Localization.get("element.intergratedLocales")).setFont(true).skipSelection(true));
        Localization.LocaleEntry[] a = Localization.avaliable();
        for (int i = 0; i < a.length; i++) {
            final Localization.LocaleEntry item = a[i];
            add(new ListItem(item.name, ListItem.CHECK) {
                public void actionPerformed() {
                    if (item.version == null || !item.version.equals(Midlet.instance.version)) {
                        PopupMenu popup = new PopupMenu();
                        popup.add(new Label(Localization.get("element.languageOutdated")).setColor(Theming.now.captionColor).skipSelection(true));

                        popup.add(new PopupButton(Localization.get("action.ok")) {
                            public void actionPerformed() {
                                Midlet.instance.config.localeCode = item.code;

                                AppCanvas.instance.closePopup();
                            }
                        });

                        popup.add(new PopupButton(Localization.get("action.cancel")) {
                            public void actionPerformed() {
                                AppCanvas.instance.closePopup();
                            }
                        });

                        AppCanvas.instance.showPopup(popup);
                        popup.selectedY = 1;
                    } else {
                        Midlet.instance.config.localeCode = item.code;
                    }
                }
            }.setDescription(Localization.get("general.for", item.version))//.setTooltipRight(false, false)
                    .setState(Midlet.instance.config.localeCode != null && Midlet.instance.config.localeCode.equals(item.code))
                    .separatorAfter(i != a.length - 1));
        }

        //TODO: installed locales

        AppCanvas.instance.goTo(this);
    }
}