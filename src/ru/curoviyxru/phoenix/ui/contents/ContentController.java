package ru.curoviyxru.phoenix.ui.contents;

import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import ru.curoviyxru.j2vk.HTTPClient;
import ru.curoviyxru.j2vk.PageStorage;
import ru.curoviyxru.j2vk.TextUtil;
import ru.curoviyxru.j2vk.VKConstants;
import ru.curoviyxru.j2vk.api.objects.attachments.Comment;
import ru.curoviyxru.j2vk.api.objects.attachments.Post;
import ru.curoviyxru.j2vk.api.objects.VKObject;
import ru.curoviyxru.j2vk.api.objects.attachments.Audio;
import ru.curoviyxru.j2vk.api.objects.user.Group;
import ru.curoviyxru.j2vk.api.objects.user.Page;
import ru.curoviyxru.j2vk.api.objects.user.User;
import ru.curoviyxru.j2vk.api.requests.audio.AudioGet;
import ru.curoviyxru.j2vk.api.requests.friends.FriendsGet;
import ru.curoviyxru.j2vk.api.requests.groups.GroupsGet;
import ru.curoviyxru.j2vk.api.requests.users.UsersGet;
import ru.curoviyxru.j2vk.api.responses.audio.AudioGetResponse;
import ru.curoviyxru.j2vk.api.responses.friends.FriendsGetResponse;
import ru.curoviyxru.j2vk.api.responses.groups.GroupsGetResponse;
import ru.curoviyxru.j2vk.auth.Authorization;
import ru.curoviyxru.j2vk.auth.Authorization.AuthSignals;
import ru.curoviyxru.j2vk.auth.AuthorizationResponse;
import ru.curoviyxru.j2vk.LongPoll;
import ru.curoviyxru.j2vk.api.objects.PhotoAlbum;
import ru.curoviyxru.j2vk.api.objects.attachments.Document;
import ru.curoviyxru.j2vk.api.objects.attachments.Photo;
import ru.curoviyxru.j2vk.api.requests.docs.DocsGet;
import ru.curoviyxru.j2vk.api.requests.friends.FriendsGetRequests;
import ru.curoviyxru.j2vk.api.requests.photos.PhotosGet;
import ru.curoviyxru.j2vk.api.requests.photos.PhotosGetAlbums;
import ru.curoviyxru.j2vk.api.responses.auth.AuthResponse;
import ru.curoviyxru.j2vk.api.responses.docs.DocsGetResponse;
import ru.curoviyxru.j2vk.api.responses.friends.FriendsGetRequestsResponse;
import ru.curoviyxru.j2vk.api.responses.photos.PhotosGetAlbumsResponse;
import ru.curoviyxru.j2vk.api.responses.photos.PhotosGetResponse;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.Logger;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.AttachmentView;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.Field;
import ru.curoviyxru.phoenix.ui.ImageItem;
import ru.curoviyxru.phoenix.ui.Label;
import ru.curoviyxru.phoenix.ui.ListItem;
import ru.curoviyxru.phoenix.ui.PasswordField;
import ru.curoviyxru.phoenix.ui.PopupButton;
import ru.curoviyxru.phoenix.ui.PopupMenu;
import ru.curoviyxru.playvk.PlayerContent;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class ContentController {

    public static Content menu;

    public static void auth(final boolean send, final String captcha, final String sid, final String login, final String password, final boolean rememberMe, final String tfaCode) {
        Midlet.instance.config.httpPost();
        AppCanvas.instance.setProgress(true);
        new Thread() {
            public void run() {
                try {
                    AuthorizationResponse resp = Authorization.authorize(login, password, captcha, sid, tfaCode, send);
                    switch (resp.getSignal()) {
                        case AuthSignals.SUCCESSFUL:
                            if (rememberMe) {
                                Midlet.instance.config.rms_access_token = VKConstants.account.getToken();
                                Midlet.instance.config.saveToken();
                            }
                            loggedIn();
                            break;
                        case AuthSignals.INVALID_PASSWORD:
                            AppCanvas.instance.dropError(Localization.get("auth.invalidPair"));
                            break;
                        case AuthSignals.SPECIFIC_ERROR:
                            AuthResponse aResp = resp.getResponse();
                            String errorDesc = aResp.error_description;
                            if (errorDesc == null) errorDesc = aResp.error_msg;
                            String error = aResp.error;
                            if (error == null) error = aResp.error_code+"";
                            AppCanvas.instance.dropError(error + ": " + errorDesc);
                            break;
                        case AuthSignals.UNKNOWN_ERROR:
                            AppCanvas.instance.dropMessage(Localization.get("auth.unknownError"), resp.getObject() == null ? "No JSON response." : resp.getObject().toString());
                            break;
                        case AuthSignals.NEEDS_CAPTCHA:
                            showCaptchaWindow(resp.getResponse().captcha_img, resp.getResponse().captcha_sid, login, password, rememberMe, tfaCode);
                            break;
                        case AuthSignals.NEEDS_VERIFICATION:
                        case AuthSignals.WRONG_OTP:
                        case AuthSignals.WRONG_OTP_FORMAT:
                            showTFAWindow(login, password, rememberMe);
                            break;
                    }
                } catch (Exception e) {
                    Logger.l(e);
                    AppCanvas.instance.dropError(TextUtil.easyReplace(TextUtil.easyReplace(TextUtil.easyReplace(TextUtil.easyReplace(e.toString(), login, "*censored*"), password, "*censored*"), HTTPClient.urlEncode(login), "*censored*"), HTTPClient.urlEncode(password), "*censored*"));
                }
                AppCanvas.instance.setProgress(false);
            }
        }.start();
    }

    public static void showCaptchaWindow(String img, final String sid, final String login, final String password, final boolean rememberMe, final String tfaCode) {
        PopupMenu captchaContent = new PopupMenu();
        captchaContent.setTitle(Localization.get("title.CAPTCHA"));
        final Field CF = new Field(Localization.get("element.CAPTCHACode")); //.setConstrains(TextField.SENSITIVE | TextField.NON_PREDICTIVE);
        captchaContent.add(new ImageItem(null, img, false, 130, 50).setAlign(Graphics.HCENTER).doNotCircle(true));
        captchaContent.add(CF);
        captchaContent.add(new PopupButton(Localization.get("action.confirm")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
                auth(true, CF.getText(), sid, login, password, rememberMe, tfaCode);
            }
        });
        captchaContent.add(new PopupButton(Localization.get("action.cancel")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
            }
        });
        AppCanvas.instance.showPopup(captchaContent);
    }

    public static void showTFAWindow(final String login, final String password, final boolean rememberMe) {
        PopupMenu tfaContent = new PopupMenu();
        tfaContent.setTitle(Localization.get("title.2FA"));
        final Field TF = new Field(Localization.get("element.2FACode"));
        tfaContent.add(TF); //.setConstrains(TextField.NUMERIC | TextField.SENSITIVE | TextField.NON_PREDICTIVE));
        tfaContent.add(new PopupButton(Localization.get("action.confirm")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
                auth(true, null, null, login, password, rememberMe, TF.getText());
            }
        });
//        tfaContent.add(new PopupButton(Localization.get("action.sendSMS")) {
//            public void actionPerformed() {
//                AppCanvas.instance.closePopup();
//                auth(true, null, null, login, password, rememberMe, TF.getText());
//            }
//        });
        tfaContent.add(new PopupButton(Localization.get("action.cancel")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
            }
        });
        AppCanvas.instance.showPopup(tfaContent);
    }

    public static void showAuthWindow() {
        menu = null;

        final Content authContent = new Content(Localization.get("title.auth"));
        int size = Math.min(AppCanvas.instance.getWidth() / 2, AppCanvas.instance.getHeight() / 2);
        authContent.add(new ImageItem(null, "new/phmini.png", true, size, size).setAlign(Graphics.HCENTER).doNotCircle(true));
        final Field LF = new Field(Localization.get("element.login"));
        final PasswordField PF = new PasswordField(Localization.get("element.password"));
        final ListItem RM = (ListItem) new ListItem(Localization.get("element.rememberPassword"), ListItem.CHECK);
        authContent.add(LF); //.setConstrains(TextField.SENSITIVE | TextField.NON_PREDICTIVE));
        authContent.add(PF);
        authContent.add(RM);
        authContent.add(new ListItem(Localization.get("action.logIn")) {
            public void actionPerformed() {
                switch (Midlet.instance.config.network_mode) {
                    case 3:
                    case 4:
                        AppCanvas.instance.dropMessage(Localization.get("general.caution"), Localization.get("element.openvkDisclaimer"), new Runnable() {
                            public void run() {
                                auth(true, null, null, LF.getText(), PF.getText(), RM.marked(), null);
                            }
                        });
                        break;
                    case 1:
                        auth(true, null, null, LF.getText(), PF.getText(), RM.marked(), null);
                        break;
                    default:
                        AppCanvas.instance.dropMessage(Localization.get("general.caution"), Localization.get("element.proxyDisclaimer"), new Runnable() {
                            public void run() {
                                auth(true, null, null, LF.getText(), PF.getText(), RM.marked(), null);
                            }
                        });
                        break;
                }
                
            }
        }.setFont(true).setIcon("new/exit-to-app.rle"));
        final ListItem li;
        authContent.add(li = (ListItem) new ListItem(Localization.get("element.settings")) {
            public void actionPerformed() {
                ContentController.showSettings(authContent, false);
            }
        }.setFont(true).setIcon("new/cog.rle"));
        authContent.add(new ListItem(Localization.get("action.quit")) {
            public void actionPerformed() {
                Midlet.instance.exit();
            }
        }.setFont(true).setIcon("new/close.rle"));
        AppCanvas.instance.goTo(authContent);
    }

    public static Content getMenu() {
        if (menu != null) {
            return menu;
        }

        menu = new Content(Localization.get("title.home"));
        menu.add(new ListItem("", ListItem.GOTO) {
            public void actionPerformed() {
                showProfile(menu, PageStorage.get(0));
            }
        }.setDescription("").setFont(true));
        menu.add(new ListItem(Localization.get("element.newsfeed"), ListItem.TEXT) {
            public void actionPerformed() {
                showNewsfeed(menu);
            }
        }.setIcon("new/newspaper-variant.rle"));
        menu.add(new ListItem(Localization.get("element.messages"), ListItem.UNREAD) {
            public void actionPerformed() {
                showConversations(menu, null, 0);
            }
        }.setIcon("new/email.rle").ignoreUnreadBackground(true));
//        menu.add(new ListItem(Localization.get("element.notifications"), ListItem.UNREAD) {
//            public void actionPerformed() {
//                showNotifications(menu);
//            }
//        }.setIcon("new/bell.rle").ignoreUnreadBackground(true));
        menu.add(new ListItem(Localization.get("element.myFriends"), ListItem.UNREAD) {
            public void actionPerformed() {
                showFriends(menu, VKConstants.account.getId());
            }
        }.setIcon("new/account.rle").ignoreUnreadBackground(true));
        menu.add(new ListItem(Localization.get("element.myGroups"), ListItem.UNREAD) {
            public void actionPerformed() {
                showGroups(menu, VKConstants.account.getId());
            }
        }.setIcon("new/account-supervisor.rle").ignoreUnreadBackground(true));
        menu.add(new ListItem(Localization.get("title.albums"), ListItem.UNREAD) {
            public void actionPerformed() {
                showAlbums(VKConstants.account.getId(), menu);
            }
        }.setIcon("new/image.rle").ignoreUnreadBackground(true));
        menu.add(new ListItem(Localization.get("element.myMusic"), ListItem.UNREAD) {
            public void actionPerformed() {
                showTracks(menu);
            }
        }.setIcon("new/music-note.rle").ignoreUnreadBackground(true));
        menu.add(new ListItem(Localization.get("title.docs"), ListItem.UNREAD) {
            public void actionPerformed() {
                showDocs(menu);
            }
        }.setIcon("new/file.rle").ignoreUnreadBackground(true));
        menu.add(new ListItem(Localization.get("element.settings"), ListItem.UNREAD) {
            public void actionPerformed() {
                ContentController.showSettings(menu, true);
            }
        }.setIcon("new/cog.rle").ignoreUnreadBackground(true));
        menu.add(new ListItem(Localization.get("action.quit"), ListItem.TEXT) {
            public void actionPerformed() {
                Midlet.instance.exit();
            }
        }.setIcon("new/close.rle").setFont(true));

        int[] dateArray = TextUtil.createDateArray(TextUtil.getCurrentTimeGMT());
        if (dateArray[3] == 9 && dateArray[4] == 1) {
            menu.add(new ListItem(Localization.get("event.happyBirthday"), ListItem.GOTO) {
                public void actionPerformed() {
                    showProfile(menu, PageStorage.get(145557030));
                }
            }.setDescription(Localization.get("event.happyBirthdayDevCongratulation")).setIcon("new/gift.rle"));
        }

        new Thread() {
            public void run() {
                updateMenu(menu);
            }
        }.start();

        return menu;
    }

    private static void updateMenu(Content content) {
        PageStorage.load(0);
        User u = (User) PageStorage.get(0);

        ((ListItem) content.at(0)).setCaption(u.getName()).setDescription(u.hasStatus() ? u.status : Localization.get("element.yourProfile")).setPage(u);
        if (ConversationList.instance != null) {
            ConversationList.instance.updateTitle();
            ((ListItem) content.at(2)).setTimestamp(PageContent.count(Math.min(9999, ConversationList.instance.unread.size())));
        }
        ((ListItem) content.at(3)).setTimestamp((u.online_friends > 0 ? PageContent.count(u.online_friends) + " / " : "") + PageContent.count(u.friends_count));
        ((ListItem) content.at(4)).setTimestamp(PageContent.count(u.groups_count));
        ((ListItem) content.at(5)).setTimestamp(PageContent.count(u.albums_count));
        ((ListItem) content.at(6)).setTimestamp(PageContent.count(u.audios_count));
        ((ListItem) content.at(8)).setTimestamp(SettingsContent.hasUpdate ? Localization.get("event.updateAvaliable") : null);

        if (u.bdate != null) {
            int[] dateArray = TextUtil.createDateArray(TextUtil.getCurrentTimeGMT());
            int[] birthArray = TextUtil.parseDateString(u.bdate);
            if (birthArray[3] == dateArray[3] && birthArray[4] == dateArray[4]) {
                content.add(new ListItem(Localization.get("event.happyBirthday"), ListItem.TEXT).setDescription(Localization.get("event.happyBirthdayCongratulation")).setIcon("new/gift.rle"));
            }
        }

        AppCanvas.instance.render();
    }

    public static void showConversations(Content parent, Vector forward, int type) {
        AppCanvas.instance.gotNewMessage = false;

        if (ConversationList.instance == null) {
            ConversationList.instance = (ConversationList) new ConversationList();
            return;
        }
        ConversationList.instance.forward(forward, type);
        AppCanvas.instance.goTo(ConversationList.instance.parent(parent));
    }

    public static void showTracks(Content parent) {
        showTracks(parent, VKObject.account.getId());
    }

    public static void showTracks(final Content parent, final long id) {
        AppCanvas.instance.goTo(new ScrollContent(VKConstants.account.getId() == id ? Localization.get("title.myMusic") : Localization.get("title.usersMusic", (PageStorage.get(id) != null ? PageStorage.get(id).getMessageTitle(UsersGet.GEN) : Localization.get("general.unknownUser"))), true) {
            public void process() {
                AppCanvas.instance.setProgress(true);
                boolean empty = next == null;
                if (empty) {
                    next = new Integer(-1);
                }

                next = new Integer(((Integer) next).intValue() + 1);

                int offset = ((Integer) next).intValue() * 5;
                final AudioGetResponse rr = (AudioGetResponse) new AudioGet().setOwnerId(id).setCount(5).setOffset(offset).execute();
                if (rr != null && rr.hasItems()) {
                    if (rr.items.length < 5) {
                        noNext = true;
                    }

                    for (int i1 = 0; i1 < rr.items.length; i1++) {
                        final Audio u = rr.items[i1];
                        //todo slim style setting
                        add(getTrackItem(u, i1 + offset));
                    }
                } else {
                    noNext = true;
                    if (rr == null) {
                        AppCanvas.instance.dropError(Localization.get("general.loadError"));
                    }
                }

                AppCanvas.instance.setProgress(false);
            }
        }.parent(parent));
    }

    public static void showProfile(Content parent, Page p) {
        if (p == null) {
            return;
        }

        AppCanvas.instance.goTo(new PageContent(p.getId()).parent(parent));
    }
    
    public static void showPhotos(final long owner, final int album, Content parent) {
        final ScrollContent sContent = (ScrollContent) new ScrollContent(Localization.get("title.photos"), true) {
            public void process() {
                AppCanvas.instance.setProgress(true);
                boolean empty = next == null;
                if (empty) {
                    next = new Integer(-1);
                }

                next = new Integer(((Integer) next).intValue() + 1);

                final PhotosGetResponse rr = (PhotosGetResponse) new PhotosGet(owner, album, ((Integer) next).intValue() * 5, 5).execute();
                if (rr != null && rr.hasItems()) {
                    if (rr.items.length < 5) {
                        noNext = true;
                    }

                    for (int i1 = 0; i1 < rr.items.length; i1++) {
                        final Photo d = rr.items[i1];
                        add(new AttachmentView(d));
                    }
                } else {
                    noNext = true;
                    if (rr == null) {
                        AppCanvas.instance.dropError(Localization.get("general.loadError"));
                    }
                }

                AppCanvas.instance.setProgress(false);
            }
        }.parent(parent);
//        sContent.rightSoft.add(new PopupButton(Localization.get("action.showRequests")) {
//            public void actionPerformed() {
//                AppCanvas.instance.closePopup();
//            }
//        }.setIcon("new/refresh.rle"));
        AppCanvas.instance.goTo(sContent);
    }
    
    public static void showAlbums(final long owner, Content parent) {
        final ScrollContent sContent = (ScrollContent) new ScrollContent(Localization.get("title.albums"), true) {
            public void process() {
                AppCanvas.instance.setProgress(true);
                boolean empty = next == null;
                if (empty) {
                    next = new Integer(-1);
                }

                next = new Integer(((Integer) next).intValue() + 1);

                final PhotosGetAlbumsResponse rr = (PhotosGetAlbumsResponse) new PhotosGetAlbums(owner, ((Integer) next).intValue() * 5, 5).execute();
                if (rr != null && rr.hasItems()) {
                    if (rr.items.length < 5) {
                        noNext = true;
                    }

                    for (int i1 = 0; i1 < rr.items.length; i1++) {
                        final PhotoAlbum d = rr.items[i1];
                        add(new AttachmentView(d));
                    }
                } else {
                    noNext = true;
                    if (rr == null) {
                        AppCanvas.instance.dropError(Localization.get("general.loadError"));
                    }
                }

                AppCanvas.instance.setProgress(false);
            }
        }.parent(parent);
//        sContent.rightSoft.add(new PopupButton(Localization.get("action.showRequests")) {
//            public void actionPerformed() {
//                AppCanvas.instance.closePopup();
//            }
//        }.setIcon("new/refresh.rle"));
        AppCanvas.instance.goTo(sContent);
    }
    
    public static void showDocs(Content parent) {
        final ScrollContent sContent = (ScrollContent) new ScrollContent(Localization.get("title.docs"), true) {
            public void process() {
                AppCanvas.instance.setProgress(true);
                boolean empty = next == null;
                if (empty) {
                    next = new Integer(-1);
                }

                next = new Integer(((Integer) next).intValue() + 1);

                final DocsGetResponse rr = (DocsGetResponse) new DocsGet(5, ((Integer) next).intValue() * 5).execute();
                if (rr != null && rr.hasDocs()) {
                    if (rr.docs.length < 5) {
                        noNext = true;
                    }

                    for (int i1 = 0; i1 < rr.docs.length; i1++) {
                        final Document d = rr.docs[i1];
                        add(new AttachmentView(d));
                    }
                } else {
                    noNext = true;
                    if (rr == null) {
                        AppCanvas.instance.dropError(Localization.get("general.loadError"));
                    }
                }

                AppCanvas.instance.setProgress(false);
            }
        }.parent(parent);
//        sContent.rightSoft.add(new PopupButton(Localization.get("action.showRequests")) {
//            public void actionPerformed() {
//                AppCanvas.instance.closePopup();
//            }
//        }.setIcon("new/refresh.rle"));
        AppCanvas.instance.goTo(sContent);
    }

    public static void showFriendsRequests(Content parent, final boolean out) {
        final ScrollContent sContent = (ScrollContent) new ScrollContent(Localization.get("title.friendRequests"), true) {
            public void process() {
                AppCanvas.instance.setProgress(true);
                boolean empty = next == null;
                if (empty) {
                    next = new Integer(-1);
                }

                next = new Integer(((Integer) next).intValue() + 1);

                final FriendsGetRequestsResponse rr = (FriendsGetRequestsResponse) new FriendsGetRequests(5, ((Integer) next).intValue() * 5, out).execute();
                if (rr != null && rr.hasUsers()) {
                    if (rr.users.length < 5) {
                        noNext = true;
                    }

                    for (int i1 = 0; i1 < rr.users.length; i1++) {
                        final User u = rr.users[i1];
                        //todo slim style setting
                        add(getUserButton(u, false, false, true, false, this));
                    }
                } else {
                    noNext = true;
                    if (rr == null) {
                        AppCanvas.instance.dropError(Localization.get("general.loadError"));
                    }
                }

                AppCanvas.instance.setProgress(false);
            }
        }.parent(parent);
//        sContent.rightSoft.add(new PopupButton(Localization.get("action.showRequests")) {
//            public void actionPerformed() {
//                AppCanvas.instance.closePopup();
//            }
//        }.setIcon("new/refresh.rle"));
        AppCanvas.instance.goTo(sContent);
    }
    
    public static void showFriends(Content parent, final long id) {
        final ScrollContent sContent = (ScrollContent) new ScrollContent(VKConstants.account.getId() == id ? Localization.get("title.myFriends") : Localization.get("title.usersFriends", (PageStorage.get(id) != null ? PageStorage.get(id).getMessageTitle(UsersGet.GEN) : Localization.get("general.unknownUser"))), true) {
            public void process() {
                AppCanvas.instance.setProgress(true);
                boolean empty = next == null;
                if (empty) {
                    next = new Integer(-1);
                }

                next = new Integer(((Integer) next).intValue() + 1);

                final FriendsGetResponse rr = (FriendsGetResponse) new FriendsGet(id, 5, ((Integer) next).intValue() * 5).setOrder(FriendsGet.HINTS).execute();
                if (rr != null && rr.hasUsers()) {
                    if (rr.users.length < 5) {
                        noNext = true;
                    }

                    for (int i1 = 0; i1 < rr.users.length; i1++) {
                        final User u = rr.users[i1];
                        //todo slim style setting
                        add(getUserButton(u, false, false, true, false, this));
                    }
                } else {
                    noNext = true;
                    if (rr == null) {
                        AppCanvas.instance.dropError(Localization.get("general.loadError"));
                    }
                }

                AppCanvas.instance.setProgress(false);
            }
        }.parent(parent);
        sContent.rightSoft.add(new PopupButton(Localization.get("action.showRequestsIn")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
                showFriendsRequests(sContent, false);
            }
        }.setIcon("new/reply.rle"));
        sContent.rightSoft.add(new PopupButton(Localization.get("action.showRequestsOut")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
                showFriendsRequests(sContent, true);
            }
        }.setIcon("new/account-arrow-right.rle"));
        
        AppCanvas.instance.goTo(sContent);
    }

    public static void showGroups(Content parent, final long id) {
        AppCanvas.instance.goTo(new ScrollContent(VKConstants.account.getId() == id ? Localization.get("title.myGroups") : Localization.get("title.usersGroups", (PageStorage.get(id) != null ? PageStorage.get(id).getMessageTitle(UsersGet.GEN) : Localization.get("general.unknownUser"))), true) {
            public void process() {
                AppCanvas.instance.setProgress(true);
                boolean empty = next == null;
                if (empty) {
                    next = new Integer(-1);
                }

                next = new Integer(((Integer) next).intValue() + 1);
                
                System.out.println(((Integer) next).intValue() + " ");

                final GroupsGetResponse rr = (GroupsGetResponse) new GroupsGet(id, 5, ((Integer) next).intValue() * 5).setFilter("groups,publics,events").execute();
                if (rr != null && rr.hasGroups()) {
                    if (rr.groups.length < 5) {
                        noNext = true;
                    }

                    for (int i1 = 0; i1 < rr.groups.length; i1++) {
                        final Group u = rr.groups[i1];
                        //todo slim style setting
                        add(getUserButton(u, false, false, true, false, this));
                    }
                } else {
                    noNext = true;
                    if (rr == null) {
                        AppCanvas.instance.dropError(Localization.get("general.loadError"));
                    }
                }

                AppCanvas.instance.setProgress(false);
            }
        }.parent(parent));
    }

    private static ListItem getTrackItem(final Audio audio, final int i) {
        return (ListItem) new AttachmentView(audio);
    }

    public static void showPlayerOrTracks(Content parent) {
        if (!PlayerContent.get().isStopped()) {
            showPlayer(parent);
        } else {
            showTracks(parent);
        }
    }

    //todo slim style
    public static ListItem getUserButton(final Page u, boolean short1, boolean bold, boolean desc, boolean dontNeedStatus, final Content backContent) {
        return (ListItem) new ListItem(short1 ? u.getMessageTitle() : u.getName()) {
            public void actionPerformed() {
                showProfile(backContent == null ? menu : backContent, u);
            }
        }
                .setFont(bold)
                .setDescription(!desc ? null
                : u.isGroup
                ? u.hasStatus() && !dontNeedStatus ? u.asGroup().status : PageContent.getFollowersString(u.asGroup().members_count)
                : u.hasStatus() && !dontNeedStatus ? u.asUser().status : u.asUser().online == 1 ? Localization.get("element.online") : PageContent.getLastSeenString(u.asUser().sex != User.FEMALE, u.asUser().last_seen_time))
                .setPage(u)
                .setCornerIcon(getStatusIcon(u));
    }

    //TODO: merge with Page.getStatusChar
    public static int getStatusIcon(Page p) {
        if (p == null || p.isGroup) {
            return 0;
        }

        User page = (User) p;

        if (page.online == 1) {
            if (page.online_app != 0) {
                switch (page.online_app) {
                    case 2274003:
                    case 2685278:
                        return 3;
                    case 3697615:
                        return 5;
                    case 3140623:
                        return 4;
                    case 6146827:
                        return 2;
                    default:
                        return 1;
                }
            } else if (page.online_mobile == 1) {
                return 2;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    public static void loggedIn() {
        if (VKConstants.account == null) {
            logout();
        } else {
            PlayerContent.get();
            ConversationList.instance = new ConversationList();
            LongPoll.start();
            ContentController.showMenu();
        }
    }

    public static void logout() {
        LongPoll.stop(true);
        VKConstants.account = null;
        Midlet.instance.config.rms_access_token = null;
        Midlet.instance.config.saveToken();
        ConversationList.instance = null;
        PlayerContent.remove();
        Midlet.instance.config.post();
        ContentController.showAuthWindow();
        AppCanvas.instance.clearItemsToLoad();
    }

    static void showNewsfeed(Content parent) {
        AppCanvas.instance.goTo(new NewsfeedContent().parent(parent));
    }

    static void showNotifications(Content parent) {
        AppCanvas.instance.goTo(new NotificationsContent().parent(parent)); //TODO: add caching?
    }

    public static void showComments(Content parent, final Post post, final Comment comment) {
        AppCanvas.instance.goTo(new CommentContent(post, comment).parent(parent));
    }

    private static void showSettings(Content parent, boolean s) {
        new SettingsContent(s).parent(parent);
    }

    public static void backMenu() {
        AppCanvas.instance.backTo(getMenu());
    }

    public static void showMenu() {
        AppCanvas.instance.goTo(getMenu());
    }

    public static void logError() {
        LongPoll.stop(true);
        VKConstants.account = null;
        ConversationList.instance = null;
        PlayerContent.remove();

        showErrorWindow();
    }

    //TODO: rewrite to popups
    private static void showErrorWindow() {
        final Content content1 = new Content(Localization.get("title.authError"));
        //ImageItem ii = new ImageItem(RenderUtil.resizeImage(RImageCache.loadImage("new/big-error.png"), Math.min(AppCanvas.instance.getWidth() / 2, AppCanvas.instance.getHeight() / 2)));
        //ii.align = Graphics.HCENTER;
        //content.add(ii);
        content1.add(new Label(Localization.get("element.authError")));
        content1.add(new ListItem(Localization.get("action.tryAgain")) {
            public void actionPerformed() {
                new Thread() {
                    public void run() {
                        AppCanvas.instance.setProgress(true);
                        Midlet.instance.config.tryAuth();
                        AppCanvas.instance.setProgress(false);
                    }
                }.start();
            }
        }.setIcon("new/refresh.rle").setFont(true));
        content1.add(new ListItem(Localization.get("action.resetSession")) {
            public void actionPerformed() {
                ContentController.logout();
            }
        }.setIcon("new/exit-to-app.rle").setFont(true));
        content1.add(new ListItem(Localization.get("element.settings")) {
            public void actionPerformed() {
                ContentController.showSettings(content1, false);
            }
        }.setIcon("new/cog.rle").setFont(true));
        content1.add(new ListItem(Localization.get("action.close")) {
            public void actionPerformed() {
                Midlet.instance.exit();
            }
        }.setIcon("new/close.rle").setFont(true));

        AppCanvas.instance.goTo(content1);
    }

    public static void updateMessagesItem() {
        if (ConversationList.instance == null) {
            return;
        }
        ConversationList.instance.updateTitle();
        if (menu != null && menu.size() >= 3) {
            //item.setIcons(ConversationList.instance.unread.size() > 0 ? "4dN.png" : "4d.png", null); TODO
            ((ListItem) menu.at(2)).setTimestamp(PageContent.count(Math.min(9999, ConversationList.instance.unread.size())));
        }
    }
    
    public static void showPlayer(Content parent) {
        AppCanvas.instance.goTo(PlayerContent.get().parent(parent));
    }
}
