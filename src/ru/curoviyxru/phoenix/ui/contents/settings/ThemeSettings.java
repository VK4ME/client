package ru.curoviyxru.phoenix.ui.contents.settings;

import javax.microedition.io.Connector;
import ru.curoviyxru.j2vk.HTTPClient;
import ru.curoviyxru.j2vk.TextUtil;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.RmsController;
import ru.curoviyxru.phoenix.Theme;
import ru.curoviyxru.phoenix.Theming;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.FilePicker;
import ru.curoviyxru.phoenix.ui.Footer;
import ru.curoviyxru.phoenix.ui.Label;
import ru.curoviyxru.phoenix.ui.ListItem;
import ru.curoviyxru.phoenix.ui.PopupButton;
import ru.curoviyxru.phoenix.ui.PopupMenu;

/**
 *
 * @author curoviyxru
 */
public class ThemeSettings extends Content {

    public ThemeSettings() {
        super(Localization.get("title.themeSettings"));

        add(new Label(Localization.get("element.integratedThemes")).setFont(true).skipSelection(true));
        Theming.ThemeEntry[] a = Theming.avaliable();
        for (int i = 0; i < a.length; i++) {
            add(getThemeItem(a[i], false).separatorAfter(i != a.length - 1));
        }
        add(new Footer());

        add(new Label(Localization.get("element.installedThemes")).setFont(true).skipSelection(true));
        a = Midlet.instance.config.themes;

        for (int i = 0; i < a.length; i++) {
            add(getThemeItem(a[i], true).separatorAfter(true));
        }

        add(new ListItem(Localization.get("action.installFromFile")) {
            public void actionPerformed() {
                FilePicker fp = new FilePicker(false, ThemeSettings.this) {
                    public void filePicked(final String path) {
                        try {
                            AppCanvas.instance.setProgress(true);
                            if (!path.endsWith(".theme")) {
                                throw new IllegalArgumentException(Localization.get("error.invalidThemeFile"));
                            }

                            javax.microedition.io.file.FileConnection conn = (javax.microedition.io.file.FileConnection) Connector.open(path, Connector.READ);
                            byte[] bytes = HTTPClient.readStream(conn.openInputStream());
                            conn.close();
                            Theme th = new Theme(bytes);

                            if (!TextUtil.isNullOrEmpty(th.entry.name)) {
                                Theming.ThemeEntry[] compareArray = Theming.avaliable();
                                for (int i = 0; i < compareArray.length; i++) {
                                    if (compareArray[i] != null && compareArray[i].name != null && compareArray[i].name.equals(th.entry.name)) {
                                        throw new IllegalArgumentException(Localization.get("error.themeInstalledAlready"));
                                    }
                                }
                                compareArray = Midlet.instance.config.themes;
                                for (int i = 0; i < compareArray.length; i++) {
                                    if (compareArray[i] != null && compareArray[i].name != null && compareArray[i].name.equals(th.entry.name)) {
                                        throw new IllegalArgumentException(Localization.get("error.themeInstalledAlready"));
                                    }
                                }

                                RmsController.saveStore(th.entry.name + "_theme", bytes);

                                compareArray = new Theming.ThemeEntry[Midlet.instance.config.themes.length + 1];
                                compareArray[compareArray.length - 1] = th.entry;
                                System.arraycopy(Midlet.instance.config.themes, 0, compareArray, 0, Midlet.instance.config.themes.length);
                                Midlet.instance.config.themes = compareArray;
                                Midlet.instance.config.saveInstalled();

                                ThemeSettings.this.insert(getThemeItem(th.entry, true).separatorAfter(true), ThemeSettings.this.size() - 1);
                                AppCanvas.instance.backTo(ThemeSettings.this);
                            } else {
                                throw new IllegalArgumentException(Localization.get("error.invalidThemeFile"));
                            }
                        } catch (Throwable e) {
                            AppCanvas.instance.dropError(e);
                        }
                        AppCanvas.instance.setProgress(false);
                    }
                };
                AppCanvas.instance.goTo(fp);
            }
        }.setFont(true).setIcon("new/file-plus.rle"));

        rightSoft = new PopupMenu(Localization.get("general.actions"));
        rightSoft.add(new PopupButton(Localization.get("action.delete")) {
            public void actionPerformed() {
                Theming.ThemeEntry[] compareArray = Midlet.instance.config.themes;
                int index = selectedY - 3 - Theming.avaliable().length;
                String deleteName = compareArray[index].name;

                ThemeSettings.this.removeAt(3 + Theming.avaliable().length + index);
                AppCanvas.instance.closePopup();
                if (Midlet.instance.config.themeName.equals(deleteName)) {
                    Theming.loadSelected(Theming.avaliable()[0].name, false);
                    Midlet.instance.config.themeName = Theming.avaliable()[0].name;
                    Midlet.instance.config.themeFromRMS = false;
                    ((ListItem) ThemeSettings.this.at(1)).setState(true);
                }
                compareArray = new Theming.ThemeEntry[Midlet.instance.config.themes.length - 1];
                System.arraycopy(Midlet.instance.config.themes, 0, compareArray, 0, index);
                System.arraycopy(Midlet.instance.config.themes, index + 1, compareArray, index, Midlet.instance.config.themes.length - index - 1);
                RmsController.removeStore(deleteName + "_theme");
                Midlet.instance.config.themes = compareArray;
                Midlet.instance.config.saveInstalled();
            }
        }.setIcon("new/delete.rle"));

        AppCanvas.instance.goTo(this);
    }

    public String getRightSoft() {
        if (selectedY == size() - 1 || selectedY < 3 + Theming.avaliable().length) {
            return null;
        }
        return super.getRightSoft();
    }

    public void rightSoft() {
        if (selectedY == size() - 1 || selectedY < 3 + Theming.avaliable().length) {
            return;
        }
        super.rightSoft();
    }

    public ListItem getThemeItem(final Theming.ThemeEntry item, final boolean fromRMS) {
        return (ListItem) new ListItem(item.name, ListItem.CHECK) {
            public void actionPerformed() {
                if (item.version == null || !item.version.equals(Midlet.instance.version)) {
                    PopupMenu popup = new PopupMenu();
                    popup.add(new Label(Localization.get("element.themeOutdated")).setColor(Theming.now.captionColor).skipSelection(true));

                    popup.add(new PopupButton(Localization.get("action.ok")) {
                        public void actionPerformed() {
                            Theming.loadSelected(Midlet.instance.config.themeName = item.name, Midlet.instance.config.themeFromRMS = fromRMS);
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
                    Theming.loadSelected(Midlet.instance.config.themeName = item.name, Midlet.instance.config.themeFromRMS = fromRMS);
                }
            }

            public void iconPressPerformed() {
                if (!fromRMS) {
                    return;
                }
                PopupMenu popup = new PopupMenu();
                popup.add(new PopupButton(Localization.get("action.delete")) {
                    public void actionPerformed() {
                        Theming.ThemeEntry[] compareArray = Midlet.instance.config.themes;
                        int index = -1;
                        for (int i = 0; i < compareArray.length; i++) {
                            if (compareArray[i] != null && compareArray[i].name != null && compareArray[i].name.equals(item.name)) {
                                index = i;
                                break;
                            }
                        }
                        if (index == -1) {
                            return;
                        }

                        ThemeSettings.this.removeAt(3 + Theming.avaliable().length + index);
                        AppCanvas.instance.closePopup();
                        if (Midlet.instance.config.themeName.equals(item.name)) {
                            Theming.loadSelected(Theming.avaliable()[0].name, false);
                            Midlet.instance.config.themeName = Theming.avaliable()[0].name;
                            Midlet.instance.config.themeFromRMS = false;
                            ((ListItem) ThemeSettings.this.at(1)).setState(true);
                        }
                        compareArray = new Theming.ThemeEntry[Midlet.instance.config.themes.length - 1];
                        System.arraycopy(Midlet.instance.config.themes, 0, compareArray, 0, index);
                        System.arraycopy(Midlet.instance.config.themes, index + 1, compareArray, index, Midlet.instance.config.themes.length - index - 1);
                        RmsController.removeStore(item.name + "_theme");
                        Midlet.instance.config.themes = compareArray;
                        Midlet.instance.config.saveInstalled();
                    }
                }.setIcon("new/delete.rle"));
                AppCanvas.instance.showPopup(popup);
            }
        }.setDescription(Localization.get("general.for", item.version))//.setTooltipRight(false, false)
                .setState(Midlet.instance.config.themeName != null && Midlet.instance.config.themeName.equals(item.name))
                .setIcon(fromRMS ? "new/file.rle" : "new/palette.rle");
    }
}