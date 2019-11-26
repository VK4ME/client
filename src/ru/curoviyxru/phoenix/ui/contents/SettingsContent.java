package ru.curoviyxru.phoenix.ui.contents;

import javax.microedition.lcdui.Graphics;
import ru.curoviyxru.j2vk.HTTPClient;
import ru.curoviyxru.j2vk.api.objects.VKObject;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.Footer;
import ru.curoviyxru.phoenix.ui.Label;
import ru.curoviyxru.phoenix.ui.ListItem;
import ru.curoviyxru.phoenix.ui.Slider;
import ru.curoviyxru.phoenix.Theming;
import ru.curoviyxru.phoenix.ui.contents.settings.AboutSettings;
import ru.curoviyxru.phoenix.ui.contents.settings.BehaviorSettings;
import ru.curoviyxru.phoenix.ui.contents.settings.InterfaceSettings;
import ru.curoviyxru.phoenix.ui.contents.settings.LocalizationSettings;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class SettingsContent extends Content {

    public static boolean hasUpdate = checkUpdate();
    public static String newVersion;

    private static boolean checkUpdate() {
        try {
            final String ver = HTTPClient.getURLString("http://vk4me.crx.moe/next/latest");
            if (!VKObject.isEmpty(ver) && ver.indexOf("Not Found") == -1 && ver.indexOf("Moved") == -1 && ver.compareTo(Midlet.instance.version) != 0) {
                newVersion = ver;
                return true;
            }
        } catch (Exception e) { }
        
        newVersion = null;
        return false;
    }

    public SettingsContent(final boolean logged) {
        super(Localization.get("title.settings"));

        add(new ListItem(Localization.get("element.interface"), ListItem.GOTO) {
            public void actionPerformed() {
                new InterfaceSettings().parent(SettingsContent.this);
            }
        }.setIcon("new/image.rle").separatorAfter(true));
        add(new ListItem(Localization.get("element.behavior"), ListItem.GOTO) {
            public void actionPerformed() {
                new BehaviorSettings(logged).parent(SettingsContent.this);
            }
        }.setIcon("new/widgets.rle").separatorAfter(true));
        add(new ListItem(Localization.get("element.localization"), ListItem.GOTO) {
            public void actionPerformed() {
                new LocalizationSettings().parent(SettingsContent.this);
            }
        }.setIcon("new/translate.rle"));

        add(new Footer());

        ListItem ab;
        add(ab = (ListItem) new ListItem(Localization.get("element.aboutApp"), ListItem.UNREAD) {
            public void actionPerformed() {
                new AboutSettings(logged).parent(SettingsContent.this);
            }
        }.setIcon("new/information.rle").ignoreUnreadBackground(true).separatorAfter(true));
        if (hasUpdate) {
            ab.setTimestamp(Localization.get("event.updateAvaliable"));
        }
        if (logged) {
            add(new ListItem(Localization.get("action.logOut")) {
                public void actionPerformed() {
                    ContentController.logout();
                }
            }.setFont(true).setIcon("new/exit-to-app.rle").separatorAfter(true));
        }
        
        try {
            if (Class.forName("com.nokia.mid.ui.DeviceControl") != null) {
                final Label lpS = (Label) new Label(Localization.get("settings.brightness", Midlet.instance.config.brightness == 0 ? Localization.get("element.system") : (Midlet.instance.config.brightness) + "")).skipSelection(true);
                add(lpS);
                final Slider lpSS = (Slider) new Slider(Midlet.instance.config.brightness) {
                    public void valueChanged(long o, long n) {
                        lpS.setText(Localization.get("settings.brightness", (Midlet.instance.config.brightness = (int) (n)) == 0 ? Localization.get("element.system") : ((Midlet.instance.config.brightness = (int) (n)) + "")));
                        Midlet.instance.config.post();
                    }
                }.setMaxProgress(100);
                add(lpSS);
            } else {
                Midlet.instance.config.brightness = 0;
                Midlet.instance.config.post();
            }
        } catch (Throwable e) {
            Midlet.instance.config.brightness = 0;
            Midlet.instance.config.post();
        }

        add(new Label(Localization.get("element.settingsRestartDisclaimer")).setAlign(Graphics.HCENTER).setColor(Theming.now.captionColor)
                .skipSelection(false));

        AppCanvas.instance.goTo(this);
    }
}
