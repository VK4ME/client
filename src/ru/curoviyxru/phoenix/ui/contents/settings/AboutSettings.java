package ru.curoviyxru.phoenix.ui.contents.settings;

import javax.microedition.lcdui.Graphics;
import ru.curoviyxru.j2vk.PageStorage;
import ru.curoviyxru.phoenix.Config;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.Footer;
import ru.curoviyxru.phoenix.ui.ImageItem;
import ru.curoviyxru.phoenix.ui.Label;
import ru.curoviyxru.phoenix.ui.ListItem;
import ru.curoviyxru.phoenix.ui.contents.ContentController;
import ru.curoviyxru.phoenix.ui.contents.SettingsContent;

/**
 *
 * @author curoviyxru
 */
public class AboutSettings extends Content {

    public AboutSettings(final boolean logged) {
        super(Localization.get("title.aboutSettings"));

        int size = Math.min(AppCanvas.instance.getWidth() / 2, AppCanvas.instance.getHeight() / 2);
        add(new ImageItem(null, "new/phmini.png", true, size, size).setAlign(Graphics.HCENTER).doNotCircle(true));

        add(new Label("VK4ME").setFont(true).setAlign(Graphics.HCENTER));
        add(new Label(Localization.get("general.buildNumber", Midlet.instance.version + "") + "\ncuroviyxru, 2022").setAlign(Graphics.HCENTER));
        add(new Footer());

        if (SettingsContent.hasUpdate) {
            add(new ListItem(Localization.get("settings.downloadUpdate"), ListItem.GOTO) {
                public void actionPerformed() {
                    Midlet.goLink("http://vk4me.crx.moe/next/latest" + (Config.hasEmojis ? "-emoji" : "") + ".jad");
                }
            }.setDescription(Localization.get("settings.updateToVersion", SettingsContent.newVersion + "")).setIcon("new/cloud-download.rle"));
        }
        add(new ListItem("vk4me.crx.moe", ListItem.GOTO) {
            public void actionPerformed() {
                Midlet.goLink("http://vk4me.crx.moe/");
            }
        }.setDescription(Localization.get("settings.applicationsWebsite")).setIcon("new/web.rle"));
        add(new ListItem("vk.com/vk4me_app", logged ? ListItem.GOTO : ListItem.TEXT) {
            public void actionPerformed() {
                if (logged) {
                    ContentController.showProfile(AboutSettings.this, PageStorage.get(-185833676));
                }
            }
        }.setDescription(Localization.get("settings.vkGroup")).setIcon("new/vk.rle"));

        add(new Footer());
        add(new Label(Localization.get("settings.specialThanks")).setAlign(Graphics.HCENTER).setFont(true));
        add(new Label("rmn20\nuninterestingrunt\nBodyZ\nKyrtovich\nshinovon\nmostwantedcheater\nJSON.org authors\n" + Localization.get("settings.thanksTesters") + "\n" + Localization.get("settings.thanksYou")).setAlign(Graphics.HCENTER));
        add(new Label("vk4me. vk4you. vk4everyone.").setAlign(Graphics.HCENTER).setFont(true));
        AppCanvas.instance.goTo(this);
    }
}