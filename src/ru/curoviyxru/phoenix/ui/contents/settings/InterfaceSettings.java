package ru.curoviyxru.phoenix.ui.contents.settings;

import ru.curoviyxru.j2vk.api.objects.Message;
import ru.curoviyxru.j2vk.api.objects.user.User;
import ru.curoviyxru.j2vk.api.requests.users.UsersGet;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.Footer;
import ru.curoviyxru.phoenix.ui.Label;
import ru.curoviyxru.phoenix.ui.ListItem;
import ru.curoviyxru.phoenix.ui.Slider;
import ru.curoviyxru.phoenix.ui.im.MainImItem;

/**
 *
 * @author curoviyxru
 */
public class InterfaceSettings extends Content {

    public InterfaceSettings() {
        super(Localization.get("title.interfaceSettings"));

        add(new ListItem(Localization.get("element.theming"), ListItem.GOTO) {
            public void actionPerformed() {
                new ThemeSettings().parent(InterfaceSettings.this);
            }
        }.setIcon("new/palette.rle"));

        final ListItem headerLI = new ListItem(Localization.get("settings.showHeader"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.gui_showHeader = this.marked() || Midlet.instance.config.gui_fullscreen || Midlet.instance.config.gui_touchHud;
                this.rightState = Midlet.instance.config.gui_showHeader ? (short) 1 : 0;
                AppCanvas.instance.init(false);
                if (Midlet.instance.config.gui_fullscreen || Midlet.instance.config.gui_touchHud) {
                    AppCanvas.instance.dropError(Localization.get("error.disableFullscreenTouch"));
                }
            }
        }.setState(Midlet.instance.config.gui_showHeader || Midlet.instance.config.gui_fullscreen || Midlet.instance.config.gui_touchHud);
        final ListItem footerLI = new ListItem(Localization.get("settings.showFooter"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.gui_showFooter = this.marked() || Midlet.instance.config.gui_fullscreen || Midlet.instance.config.gui_touchHud;
                this.rightState = Midlet.instance.config.gui_showFooter ? (short) 1 : 0;
                AppCanvas.instance.init(false);
                if (Midlet.instance.config.gui_fullscreen || Midlet.instance.config.gui_touchHud) {
                    AppCanvas.instance.dropError(Localization.get("error.disableFullscreenTouch"));
                }
            }
        }.setState(Midlet.instance.config.gui_showFooter || Midlet.instance.config.gui_fullscreen || Midlet.instance.config.gui_touchHud);

        add(new Label(Localization.get("settings.screenSettings")).setFont(true).skipSelection(true));
        add(new ListItem(Localization.get("settings.reverseSofts"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.gui_reverseSofts = this.marked();
                AppCanvas.instance.init(false);
            }
        }.setDescription(Localization.get("element.onlyForNonTouchUI")).setState(Midlet.instance.config.gui_reverseSofts));
        add(new ListItem(Localization.get("settings.fullscreenMode"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.gui_fullscreen = this.marked();
                footerLI.rightState = (Midlet.instance.config.gui_showFooter || Midlet.instance.config.gui_fullscreen || Midlet.instance.config.gui_touchHud) ? (short) 1 : 0;
                headerLI.rightState = (Midlet.instance.config.gui_showHeader || Midlet.instance.config.gui_fullscreen || Midlet.instance.config.gui_touchHud) ? (short) 1 : 0;
                AppCanvas.instance.init(false);
                AppCanvas.instance.showGraphics();
            }
        }.setState(Midlet.instance.config.gui_fullscreen));

        add(new ListItem(Localization.get("settings.longTouchScreenHudMode"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.gui_touchHud = this.marked();
                footerLI.rightState = (Midlet.instance.config.gui_showFooter || Midlet.instance.config.gui_fullscreen || Midlet.instance.config.gui_touchHud) ? (short) 1 : 0;
                headerLI.rightState = (Midlet.instance.config.gui_showHeader || Midlet.instance.config.gui_fullscreen || Midlet.instance.config.gui_touchHud) ? (short) 1 : 0;
                AppCanvas.instance.init(false);
            }
        }.setState(Midlet.instance.config.gui_touchHud));
//            add(new ListItem(Localization.get("settings.showDock"), ListItem.CHECK) {
//                public void stateUpdated() {
//                    Midlet.instance.config.gui_showDock = this.marked();
//                    if (Midlet.instance.config.gui_touchHud) AppCanvas.instance.init(false);
//                }
//            }.setDescription(Localization.get("element.touchOnly")).setState(Midlet.instance.config.gui_showDock));
        add(new ListItem(Localization.get("settings.slimHeader"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.gui_slimHeader = this.marked();
                if (Midlet.instance.config.gui_touchHud) {
                    AppCanvas.instance.init(false);
                }
            }
        }.setDescription(Localization.get("element.touchOnly")).setState(Midlet.instance.config.gui_slimHeader));
        add(new ListItem(Localization.get("settings.useDrawer"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.gui_useDrawer = this.marked();
                if (Midlet.instance.config.gui_touchHud) {
                    AppCanvas.instance.init(false);
                }
            }
        }.setDescription(Localization.get("element.touchOnly")).setState(Midlet.instance.config.gui_useDrawer));

        add(new ListItem(Localization.get("settings.shiftTitleRight"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.shiftTitleRight = this.marked();
            }
        }.setState(Midlet.instance.config.shiftTitleRight));
        add(new ListItem(Localization.get("settings.oldMessagesSoft"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.oldMessagesSoft = this.marked();
            }
        }.setState(Midlet.instance.config.oldMessagesSoft));
        add(new ListItem(Localization.get("settings.oldCommentsSoft"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.oldCommentsSoft = this.marked();
            }
        }.setState(Midlet.instance.config.oldCommentsSoft));

        add(new ListItem(Localization.get("settings.showMsgFieldByOpen"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.showMsgFieldByOpen = this.marked();
            }
        }.setState(Midlet.instance.config.showMsgFieldByOpen));
        add(new ListItem(Localization.get("settings.showCmtFieldByOpen"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.showCmtFieldByOpen = this.marked();
            }
        }.setState(Midlet.instance.config.showCmtFieldByOpen));

        add(headerLI);
        add(footerLI);

        add(new Footer());
        add(new Label(Localization.get("settings.graphics")).setFont(true).skipSelection(true));
        add(new ListItem(Localization.get("settings.animations"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.gui_animations = this.marked();
            }
        }.setState(Midlet.instance.config.gui_animations));
        add(new ListItem(Localization.get("settings.gradients"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.gui_drawGradients = this.marked();
            }
        }.setState(Midlet.instance.config.gui_drawGradients));
        add(new ListItem(Localization.get("settings.smoothScroll"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.gui_smoothScroll = this.marked();
            }
        }.setState(Midlet.instance.config.gui_smoothScroll));
        add(new ListItem(Localization.get("settings.renderDimm"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.gui_disableDimm = !this.marked();
            }
        }.setState(!Midlet.instance.config.gui_disableDimm));

        add(new Footer());
        add(new Label(Localization.get("settings.general")).setFont(true).skipSelection(true));

        final User croshUser = new User();
        croshUser.first_name = Localization.get("element.testItemFName");
        croshUser.last_name = Localization.get("element.testItemLName");
        final ListItem nameItem = new ListItem(Midlet.instance.config.gui_reverseNames ? croshUser.getShortR(UsersGet.NOM) : croshUser.getShortO(UsersGet.NOM), ListItem.UNREAD).setFont(true).setDescription(Localization.get("element.testItemDesc")).setLocalURL("new/crosh.png").setCornerIcon(1).setState((short) 13).setTimestamp(1627145577);
        add(nameItem);

        Message msg = new Message();
        msg.text = Localization.get("element.testItemDesc");
        MainImItem mmi = new MainImItem(null, msg) {
            public void buildPopup() {
            }

            public void actionPerformed() {
            }
        };
        mmi.showSender = false;
        Message msg1 = new Message();
        msg1.out = true;
        msg1.text = Localization.get("element.testMessageText");
        MainImItem mmi1 = new MainImItem(null, msg1) {
            public void buildPopup() {
            }

            public void actionPerformed() {
            }
        };
        mmi1.showSender = false;
        add(mmi);
        add(mmi1);

        final Label circleLabel;
        add(circleLabel = (Label) new Label(Localization.get("settings.cornerRadius", Midlet.instance.config.gui_avatarCircleType + "%"))
                .skipSelection(true));

        add(new Slider(Midlet.instance.config.gui_avatarCircleType / 5) {
            public void valueChanged(long o, long n) {
                Midlet.instance.config.gui_avatarCircleType = (int) n * 5;
                circleLabel.setText(Localization.get("settings.cornerRadius", Midlet.instance.config.gui_avatarCircleType + "%"));
                AppCanvas.instance.themeRecache();
            }
        }.setMaxProgress(20));

        final Label avacircleLabel;
        add(avacircleLabel = (Label) new Label(Localization.get("settings.avatarsCircling", Midlet.instance.config.gui_photosCircleType + "%"))
                .skipSelection(true));

        add(new Slider(Midlet.instance.config.gui_photosCircleType / 5) {
            public void valueChanged(long o, long n) {
                Midlet.instance.config.gui_photosCircleType = (int) n * 5;
                avacircleLabel.setText(Localization.get("settings.avatarsCircling", Midlet.instance.config.gui_photosCircleType + "%"));
                AppCanvas.instance.themeRecache();
            }
        }.setMaxProgress(20));

        add(new ListItem(Localization.get("settings.reverseProfilesNames"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.gui_reverseNames = this.marked();
                Midlet.instance.config.post();
                nameItem.setCaption(Midlet.instance.config.gui_reverseNames ? croshUser.getShortR(UsersGet.NOM) : croshUser.getShortO(UsersGet.NOM));
            }
        }.setState(Midlet.instance.config.gui_reverseNames));

        add(new Footer());
        add(new Label(Localization.get("settings.dialogsInterface")).setFont(true).skipSelection(true));
        add(new ListItem(Localization.get("settings.hideFieldAfterSending"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.hideAfterSending = this.marked();
            }
        }.setState(Midlet.instance.config.hideAfterSending));
        add(new ListItem(Localization.get("settings.drawMessagesBorder"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.gui_cloudBorder = this.marked();
            }
        }.setState(Midlet.instance.config.gui_cloudBorder));
        add(new ListItem(Localization.get("settings.showMessagesAvatars"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.gui_messagesAvatars = this.marked();
            }
        }.setState(Midlet.instance.config.gui_messagesAvatars));
        add(new ListItem(Localization.get("settings.drawMessagesClouds"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.gui_showClouds = this.marked();
            }
        }.setState(Midlet.instance.config.gui_showClouds));
        add(new ListItem(Localization.get("settings.messsageDiffSides"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.gui_messageDiffSides = this.marked();
            }
        }.setState(Midlet.instance.config.gui_messageDiffSides));

        add(new Footer());
        add(new Label(Localization.get("settings.debugging")).setFont(true).skipSelection(true));
        add(new ListItem(Localization.get("settings.drawFPS"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.debug_drawFPS = this.marked();
            }
        }.setState(Midlet.instance.config.debug_drawFPS));
        add(new ListItem(Localization.get("settings.drawEmojiRed"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.debug_drawEmojiRed = this.marked();
            }
        }.setState(Midlet.instance.config.debug_drawEmojiRed));
        add(new ListItem(Localization.get("settings.drawImagesFrames"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.debug_drawImagesFrames = this.marked();
            }
        }.setState(Midlet.instance.config.debug_drawImagesFrames));

        AppCanvas.instance.goTo(this);
    }
}
