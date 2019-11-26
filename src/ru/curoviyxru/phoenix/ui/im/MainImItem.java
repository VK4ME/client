package ru.curoviyxru.phoenix.ui.im;

import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import ru.curoviyxru.j2vk.LongPoll;
import ru.curoviyxru.j2vk.PageStorage;
import ru.curoviyxru.j2vk.TextUtil;
import ru.curoviyxru.j2vk.VKConstants;
import ru.curoviyxru.j2vk.api.objects.Attachment;
import ru.curoviyxru.j2vk.api.objects.ImItem;
import ru.curoviyxru.j2vk.api.objects.Message;
import ru.curoviyxru.j2vk.api.objects.VKObject;
import ru.curoviyxru.j2vk.api.objects.attachments.Gift;
import ru.curoviyxru.j2vk.api.objects.attachments.ImageAttachment;
import ru.curoviyxru.j2vk.api.objects.attachments.Sticker;
import ru.curoviyxru.j2vk.api.objects.user.Page;
import ru.curoviyxru.j2vk.api.requests.messages.MessagesDelete;
import ru.curoviyxru.j2vk.api.requests.messages.MessagesPin;
import ru.curoviyxru.j2vk.api.requests.messages.MessagesUnpin;
import ru.curoviyxru.j2vk.api.requests.wall.WallDeleteComment;
import ru.curoviyxru.j2vk.api.responses.messages.MessagesDeleteResponse;
import ru.curoviyxru.j2vk.api.responses.messages.MessagesPinResponse;
import ru.curoviyxru.j2vk.api.responses.messages.MessagesUnpinResponse;
import ru.curoviyxru.j2vk.api.responses.wall.WallDeleteCommentResponse;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.contents.ContentController;
import ru.curoviyxru.phoenix.ui.FontWithEmoji;
import ru.curoviyxru.phoenix.ui.PaneItem;
import ru.curoviyxru.phoenix.ui.PopupButton;
import ru.curoviyxru.phoenix.ui.PopupMenu;
import ru.curoviyxru.phoenix.ui.RenderUtil;
import ru.curoviyxru.phoenix.Theming;
import ru.curoviyxru.phoenix.ui.AttachmentView;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.ConversationItem;
import ru.curoviyxru.phoenix.ui.ImageProvider;
import ru.curoviyxru.phoenix.ui.Label;
import ru.curoviyxru.phoenix.ui.ListItem;
import ru.curoviyxru.phoenix.ui.SuperString;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class MainImItem extends PaneItem implements ImHolder, ImageProvider {

    static final Object locker = new Object();
    ImItem m;
    public boolean showSender;
    boolean isChat;
    SuperString peerName;
    SuperString[] v;
    int maxTextWidth;
    int textHeight;
    int replyPadding;
    int namePadding;
    int attHeight;
    int fwdsHeight;
    SuperString replyPeer, replyText;
    SuperString[] fwdPeers, fwdTexts;
    AttachmentView[] attachments;
    PopupMenu popup;
    boolean onlySticker;
    String timeString;
    int timePadding, peerWidth, timeWidth;
    boolean forceUpdate;
    Image[] photos;
    String[] photosUrl;
    String avatarUrl;
    boolean localAvatar;
    int avatarS;
    Integer[] photosH;
    Integer[] photosW;
    String cornerIcon;
    int tries;
    boolean isMsgContent;

    public int tries() {
        return tries;
    }

    public void tried() {
        if (tries > 5) {
            return;
        }
        ++tries;
    }

    public int size() {
        return 1 + (photosUrl != null ? photosUrl.length : 0);
    }

    public boolean local(int i) {
        return i == 0 && localAvatar;
    }

    public String get(int i) {
        if (i == 0 && avatarUrl != null && content != null && content.screenCache.get(avatarUrl) == null) {
            return avatarUrl;
        } else if (i > 0 && i < size() && photos != null && photos[i - 1] == null) {
            return photosUrl[i - 1];
        }
        return null;
    }

    public void set(int i, Image image) {
        if (image == null) {
            return;
        }
        if (i == 0 && avatarUrl != null && content.screenCache.get(avatarUrl) == null) {
            if (content != null) {
                content.screenCache.put(avatarUrl, RenderUtil.resizeImage(image, ListItem.smallHeightAvatar));
            }
        } else if (i > 0 && i < size() && photos != null && photos[i - 1] == null) {
            photos[i - 1] = RenderUtil.circlify(AppCanvas.tranSize, RenderUtil.resizeImage(image, photosH[i - 1].intValue()), RenderUtil.ALL);
        }
    }

    public MainImItem(ImContent c, ImItem message) {
        this.m = message;
        this.content = (Content) c;
        isMsgContent = content != null;
        isChat = content != null && c.isChat();
        showSender = true;
    }

    public void showAttachmentsList() {
        if (!m.hasAttachments()) {
            return;
        }
        Content c = new Content(Localization.get("title.attachmentsList")).parent(content);
        for (int i = 0; i < m.attachments().length; ++i) {
            Attachment at = m.attachments()[i];
            if (at == null) {
                continue;
            }
            c.add(new AttachmentView(at));
        }
        AppCanvas.instance.closePopup();
        AppCanvas.instance.goTo(c);
    }

    public void showAdditionalMessages() {
        if (!m.hasForwardedMessages() && !m.hasReplyMessage()) {
            return;
        }
        Content c = new Content(Localization.get("title.attachmentsList")).parent(content);
        if (m.hasReplyMessage()) {
            c.add(new Label(Localization.get("attachments.reply")).setFont(true));
            c.add(new MainImItem(null, m.replyMessage()));
        }
        if (m.hasForwardedMessages()) {
            c.add(new Label(Localization.get("attachments.fwds", "")).setFont(true));
            for (int i = 0; i < m.forwardedMessages().length; i++) {
                if (m.forwardedMessages()[i] != null) {
                    c.add(new MainImItem(null, m.forwardedMessages()[i]));
                }
            }
        }
        AppCanvas.instance.closePopup();
        AppCanvas.instance.goTo(c);
    }

    public void addMessageParts() {
        final ImContent contentT = isMsgContent ? (ImContent) content : null;
        final Page userPage = PageStorage.get(m.fromId());
        
        if (contentT == null || contentT.isComments()) {
            return;
        }
        
        final PopupMenu replyPopup = new PopupMenu();
        if (contentT.canWrite()) {
            replyPopup.add(new PopupButton(Localization.get("action.replyThisChat")) {
                public void actionPerformed() {
                    contentT.field().reply(m);
                    AppCanvas.instance.closePopup();
                }
            });
        }

        if (userPage != null && !userPage.isGroup && userPage.asUser().can_write_private_message && m.fromId() != m.ownerId()) {
            replyPopup.add(new PopupButton(Localization.get("action.replyPrivately")) {
                public void actionPerformed() {
                    ImContent msgC = ConversationItem.showConversation(MainImItem.this.content, m.fromId());
                    msgC.field().addForward(m);
                    AppCanvas.instance.closePopup();
                }
            });
        }
        if (replyPopup.size() > 0) {
            popup.add(new PopupButton(Localization.get("action.reply"), replyPopup).setIcon("new/reply.rle"));
        }

        final PopupMenu forwardPopup = new PopupMenu();
        if (contentT.canWrite()) {
            forwardPopup.add(new PopupButton(Localization.get("action.fwdThisChat")) {
                public void actionPerformed() {
                    contentT.field().addForward(m);
                    contentT.field().setOpened(true);
                    AppCanvas.instance.closePopup();
                }
            });
        }
        forwardPopup.add(new PopupButton(Localization.get("action.fwdAnotherChat")) {
            public void actionPerformed() {
                Vector v = new Vector(1, 1);
                v.addElement(m); //TODO multiselect
                AppCanvas.instance.closePopup();
                ContentController.showConversations(MainImItem.this.content, v, 1);
            }
        });
        if (forwardPopup.size() > 0) {
            popup.add(new PopupButton(Localization.get("action.fwdMessage"), forwardPopup).setIcon("new/share.rle"));
        }

        final PopupMenu deletePopup = new PopupMenu();
        if ((contentT.canWrite()
                && m.canEdDel()
                && m.ownerId() != VKConstants.account.getId()
                && m.fromId() == VKConstants.account.getId())
                || contentT.isAdmin()) {
            deletePopup.add(new PopupButton(Localization.get("action.deleteForEveryone")) {
                public void actionPerformed() {
                    MessagesDeleteResponse rr = (MessagesDeleteResponse) new MessagesDelete(m.id()).setDeleteForAll(true).execute();
                    LongPoll.skipWait = true;
                    AppCanvas.instance.closePopup();
                    if (rr == null || !rr.hasDeleted(m.id())) {
                        AppCanvas.instance.dropError(Localization.get("element.deleteError"));
                    }
                }
            });
        }
        deletePopup.add(new PopupButton(Localization.get("action.deleteForMyself")) {
            public void actionPerformed() {
                MessagesDeleteResponse rr = (MessagesDeleteResponse) new MessagesDelete(m.id()).setDeleteForAll(false).execute();
                LongPoll.skipWait = true;
                AppCanvas.instance.closePopup();
                if (rr == null || !rr.hasDeleted(m.id())) {
                    AppCanvas.instance.dropError(Localization.get("element.deleteError"));
                }
            }
        });
        if (contentT.canWrite() && deletePopup.size() > 0) {
            popup.add(new PopupButton(Localization.get("action.deleteMessage"), deletePopup).setIcon("new/delete.rle"));
        }
    }
    
    public void addCommentParts() {
        final ImContent contentT = isMsgContent ? (ImContent) content : null;
        
        if (contentT == null || !contentT.isComments()) {
            return;
        }

        if (contentT.canWrite()) {
            popup.add(new PopupButton(Localization.get("action.reply")) { //action.replyThisChat
                public void actionPerformed() {
                    contentT.field().reply(m);
                    AppCanvas.instance.closePopup();
                }
            }.setIcon("new/reply.rle"));
        }

        popup.add(new PopupButton(Localization.get("action.fwdMessage")) { //action.fwdAnotherChat
            public void actionPerformed() {
                Vector v = new Vector(1, 1);
                v.addElement(m); //TODO multiselect
                AppCanvas.instance.closePopup();
                ContentController.showConversations(MainImItem.this.content, v, 2);
            }
        }.setIcon("new/share.rle"));

        if ((contentT.canWrite()
                && m.canEdDel()
                && m.fromId() == VKConstants.account.getId())
                || contentT.isAdmin()) {
            popup.add(new PopupButton(Localization.get("action.deleteMessage")) { //action.deleteForEveryone
                public void actionPerformed() {
                    WallDeleteCommentResponse rr = (WallDeleteCommentResponse) new WallDeleteComment().setComment(m.ownerId(), m.id()).execute();
                    AppCanvas.instance.closePopup();
                    if (rr == null || !rr.hasDeleted(m.id())) {
                        AppCanvas.instance.dropError(Localization.get("element.deleteError"));
                    } else {
                        contentT.gotDelete(m.id());
                    }
                }
            }.setIcon("new/delete.rle"));
        }
    }

    public void buildPopup() {
        synchronized (locker) {
            if (m == null || content == null || popup != null) {
                return;
            }
            popup = new PopupMenu(Localization.get("general.actions"));

            final ImContent contentT = isMsgContent ? (ImContent) content : null;
            final Page userPage = PageStorage.get(m.fromId());
            
            addMessageParts();
            
            addCommentParts();

            if (contentT != null && contentT.canWrite() && m.canEdDel() && m.fromId() == VKConstants.account.getId()) {
                popup.add(new PopupButton(Localization.get("action.editMessage")) {
                    public void actionPerformed() {
                        contentT.field().edit(m);
                        contentT.field().setOpened(true);
                        AppCanvas.instance.closePopup();
                    }
                }.setIcon("new/pencil.rle"));
            }
            
            if (m.hasAttachments()) {
                popup.add(new PopupButton(Localization.get("action.showAttachmentsList")) {
                    public void actionPerformed() {
                        showAttachmentsList();
                    }
                }.setIcon("new/paperclip.rle"));
            }        
            
            if (m.hasForwardedMessages() || m.hasReplyMessage()) {
                popup.add(new PopupButton(Localization.get("action.additionalMessages")) {
                    public void actionPerformed() {
                        showAdditionalMessages();
                    }
                }.setIcon("new/message.rle"));
            }  
            
            if (contentT != null && contentT.isComments()) {
                popup.add(new PopupButton(Localization.get("action.showReplies")) {
                    public void actionPerformed() {
                        contentT.setThread(m);
                        AppCanvas.instance.closePopup();
                    }
                }.setIcon("new/chevron-down.rle"));
            }
            
            if (!TextUtil.isNullOrEmpty(m.text())) {
                popup.add(new PopupButton(Localization.get("action.showTextInEditor")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        AppCanvas.instance.showTextBox(Localization.get("title.textEditor"), m.text());
                    }
                }.setIcon("new/content-copy.rle"));
            }

            if (contentT != null && !contentT.isComments() && !m.out()) {
                popup.add(new PopupButton(Localization.get("action.markAsReaded")) {
                    public void actionPerformed() {
                        contentT.readTo(m);
                        AppCanvas.instance.closePopup();
                    }
                }.setIcon("new/eye.rle"));
            }
            
            popup.add(new PopupButton(Localization.get("action.goToProfile")) {
                public void actionPerformed() {
                    ContentController.showProfile(MainImItem.this.content, userPage);
                    AppCanvas.instance.closePopup();
                }
            }.setIcon("new/account.rle"));
        
            addPin();
        
            addUnpin();
        }
    }
    
    public void addPin() {
        final ImContent contentT = isMsgContent ? (ImContent) content : null;
        
        if (contentT == null || contentT.isComments()) {
            return;
        }
        
        if (contentT.isChat() && contentT.isAdmin() && contentT.pinned() != m.id()) {
            popup.add(new PopupButton(Localization.get("action.pin")) {
                public void actionPerformed() {
                    MessagesPinResponse rr = (MessagesPinResponse) new MessagesPin(m.ownerId(), m.id()).execute();
                    LongPoll.skipWait = true;
                    if (rr == null || !rr.hasMessage()) {
                        AppCanvas.instance.dropError(Localization.get("element.pinError"));
                    } else {
                        MainImItem.this.popup.remove(this);

                        contentT.setPinned(m);
                        addUnpin();
                    }
                    AppCanvas.instance.closePopup();
                }
            }.setIcon("new/pin.rle"));
        }
    }

    public void addUnpin() {
        final ImContent contentT = isMsgContent ? (ImContent) content : null;
        
        if (contentT == null || contentT.isComments()) {
            return;
        }
        
        if (contentT.isChat() && contentT.isAdmin() && contentT.pinned() == m.id()) {
            popup.add(new PopupButton(Localization.get("action.unpin")) {
                public void actionPerformed() {
                    MessagesUnpinResponse rr = (MessagesUnpinResponse) new MessagesUnpin(m.ownerId()).execute();
                    LongPoll.skipWait = true;
                    if (rr == null || !rr.isSuccessful()) {
                        AppCanvas.instance.dropError(Localization.get("element.unpinError"));
                    } else {
                        MainImItem.this.popup.remove(this);

                        contentT.setPinned(null);
                        addPin();
                    }
                    AppCanvas.instance.closePopup();
                }
            }.setIcon("new/pin-off.rle"));
        }
    }

    public void resetCache() {
        v = null;
        avatarUrl = null;
        peerName = null;
        timeString = null;
        replyPeer = null;
        replyText = null;
        fwdPeers = null;
        fwdTexts = null;
        maxTextWidth = 0;
        forceUpdate = true;
    }

    public void updateHeight() {
        if (m == null) {
            return;
        }
        boolean recalc = forceUpdate;
        boolean callQueue = false;

        if (width > 0 && m != null && m.hasText() && v == null) {
            SuperString mtext = new SuperString(m.text());
            v = AppCanvas.instance.normalEmojiFont.multiline(mtext, width - AppCanvas.instance.perLineSpace * 2);
            recalc = true;
        }

        if (Midlet.instance.config.gui_messagesAvatars && !Midlet.instance.config.doNotLoadImages && !(Midlet.instance.config.gui_messageDiffSides && m.out()) && width > 0 && showSender && isChat && avatarUrl == null) {
            Page p = PageStorage.get(m.fromId());
            String temp = ListItem.smallHeight > 50 ? p.getPhoto_100() : p.getPhoto_50();
            boolean localUrl = false;
            if (temp == null) {
                localUrl = true;
                if (p.isGroup) {
                    if (!VKObject.isEmpty(p.asGroup().deactivated)) {
                        temp = (ListItem.smallHeight > 50 ? "new/deactivated_100.png" : "new/deactivated_50.png");
                    } else {
                        temp = (ListItem.smallHeight > 50 ? "new/community_100.png" : "new/community_50.png");
                    }
                } else {
                    if (!VKObject.isEmpty(p.asUser().deactivated)) {
                        temp = (ListItem.smallHeight > 50 ? "new/deactivated_100.png" : "new/deactivated_50.png");
                    } else {
                        temp = (ListItem.smallHeight > 50 ? "new/camera_100.png" : "new/camera_50.png");
                    }
                }
            }

            cornerIcon = ListItem.getCornerPath(ContentController.getStatusIcon(p));
            avatarUrl = temp;
            localAvatar = localUrl;
            callQueue = true;
            recalc = true;
        }

        if (width > 0 && showSender && isChat && peerName == null) {
            peerName = AppCanvas.instance.boldEmojiFont.limit(new SuperString(PageStorage.get(m.fromId()).getMessageTitle()), width - (avatarUrl != null ? ListItem.smallHeightAvatar + AppCanvas.instance.perLineSpace : 0), true);
            peerWidth = AppCanvas.instance.boldEmojiFont.stringWidth(peerName) + AppCanvas.instance.perLineSpace;
            recalc = true;
        }

        if (width > 0 && showSender && timeString == null) {
            timeString = PaneItem.limit(VKObject.timeToString(m.getLastTime()), width - AppCanvas.instance.perLineSpace * 2
                    - (peerName != null ? peerWidth : 0)
                    - (avatarUrl != null ? ListItem.smallHeightAvatar + AppCanvas.instance.perLineSpace : 0), true, AppCanvas.instance.normalFont);
            timePadding = (AppCanvas.instance.boldEmojiFont.height - AppCanvas.instance.normalFont.getHeight()) / 2;
            timeWidth = AppCanvas.instance.normalFont.stringWidth(timeString);
            recalc = true;
        }

        if (!showSender) {
            timeString = null;
        }

        if (width > 0 && m.hasReplyMessage()) {
            if (replyPeer == null) {
                if (m.replyMessage().isDeleted()) {
                    replyPeer = new SuperString(Message.LOCALE_EMPTY);
                } else {
                    replyPeer = AppCanvas.instance.boldEmojiFont.limit(new SuperString(PageStorage.get(m.replyMessage().fromId()).getMessageTitle()), width, true);
                }
                recalc = true;
            }

            if (replyText == null) {
                replyText = AppCanvas.instance.normalEmojiFont.limit(new SuperString(m.replyMessage().toString(false, false, false, true, true)), width - AppCanvas.instance.perLineSpace * 4, true);
                recalc = true;
            }
        }

        if (width > 0 && m.hasForwardedMessages() && (fwdPeers == null || fwdTexts == null)) {
            Vector tFwdPeers = new Vector();
            Vector tFwdTexts = new Vector();

            for (int i = 0; i < m.forwardedMessages().length; ++i) {
                ImItem mm = m.forwardedMessages()[i];
                if (mm == null) {
                    continue;
                }

                SuperString replyPeer = AppCanvas.instance.boldEmojiFont.limit(new SuperString(PageStorage.get(mm.fromId()).getMessageTitle()), width - AppCanvas.instance.perLineSpace * 4, true);
                SuperString replyText = AppCanvas.instance.normalEmojiFont.limit(new SuperString(mm.toString(false, false, false, true, true)), width - AppCanvas.instance.perLineSpace * 4, true);

                tFwdPeers.addElement(replyPeer);
                tFwdTexts.addElement(replyText);
            }

            fwdPeers = new SuperString[tFwdPeers.size()];
            fwdTexts = new SuperString[tFwdTexts.size()];
            tFwdPeers.copyInto(fwdPeers);
            tFwdTexts.copyInto(fwdTexts);
            recalc = true;
        }
        
        int tattHeight = -1;
        if (width > 0 && m.hasAttachments() && (photos == null || attachments == null)) {
            tattHeight = 0;
            Vector tPhotosUrl = new Vector();
            Vector tPhotosH = new Vector();
            Vector tPhotosW = new Vector();
            Vector tAttachments = new Vector();

            boolean hasSticker = false;

            for (int i = 0; i < m.attachments().length; ++i) {
                Attachment a = m.attachments()[i];
                if (a == null) {
                    continue;
                }

                hasSticker = hasSticker || a instanceof Sticker || a instanceof Gift;

                if (a instanceof ImageAttachment && !Midlet.instance.config.doNotLoadImages) {
                    int rW = Math.min(AppCanvas.instance.getWidth() / 2, AppCanvas.instance.getHeight() / 2);

                    String fUrl = ((ImageAttachment) a).getURL(rW);
                    if (fUrl == null) {
                        continue;
                    }
                    int fW = ((ImageAttachment) a).getWidth(rW);
                    int fH = ((ImageAttachment) a).getHeight(rW);
                    fH = Math.min(rW, fW) * fH / fW;
                    fW = Math.min(rW, fW);

                    tPhotosUrl.addElement(fUrl);
                    tPhotosH.addElement(new Integer(fH));
                    tPhotosW.addElement(new Integer(fW));

                    maxTextWidth = Math.max(maxTextWidth, fW);
                    tattHeight += fH + AppCanvas.instance.perLineSpace;
                    callQueue = true;
                } else {
                    AttachmentView atView = new AttachmentView(a);
                    atView.updateHeight();
                    tAttachments.addElement(atView);
                    maxTextWidth = Math.max(maxTextWidth, width - AppCanvas.instance.perLineSpace * 2);
                    tattHeight += atView.height + AppCanvas.instance.perLineSpace;
                }
            }

            photos = new Image[tPhotosUrl.size()];
            photosUrl = new String[tPhotosUrl.size()];
            tPhotosUrl.copyInto(photosUrl);
            photosH = new Integer[tPhotosH.size()];
            tPhotosH.copyInto(photosH);
            photosW = new Integer[tPhotosW.size()];
            tPhotosW.copyInto(photosW);
            attachments = new AttachmentView[tAttachments.size()];
            tAttachments.copyInto(attachments);
            onlySticker = hasSticker;
            recalc = true;
        }

        if (recalc) {
            if (v != null) {
                for (int i = 0; i < v.length; i++) {
                    maxTextWidth = Math.max(maxTextWidth, AppCanvas.instance.normalEmojiFont.stringWidth(v[i]));
                }
            }
            maxTextWidth = Math.max(maxTextWidth, AppCanvas.instance.boldEmojiFont.stringWidth(replyPeer) + AppCanvas.instance.perLineSpace * 2);
            maxTextWidth = Math.max(maxTextWidth, AppCanvas.instance.normalEmojiFont.stringWidth(replyText) + AppCanvas.instance.perLineSpace * 2);
            if (fwdPeers != null && fwdTexts != null) {
                for (int i = 0; i < fwdPeers.length; ++i) {
                    maxTextWidth = Math.max(maxTextWidth, AppCanvas.instance.boldEmojiFont.stringWidth(fwdPeers[i]) + AppCanvas.instance.perLineSpace * 2);
                    maxTextWidth = Math.max(maxTextWidth, AppCanvas.instance.normalEmojiFont.stringWidth(fwdTexts[i]) + AppCanvas.instance.perLineSpace * 2);
                }
            }
            if (photosW != null) {
                for (int i = 0; i < photosW.length; ++i) {
                    maxTextWidth = Math.max(maxTextWidth, photosW[i].intValue());
                }
            }
            if (attachments != null && attachments.length > 0) {
                maxTextWidth = Math.max(maxTextWidth, width - AppCanvas.instance.perLineSpace * 2);
            }

            textHeight = v == null ? 0 : (v.length * AppCanvas.instance.normalEmojiFont.height) + AppCanvas.instance.perLineSpace;
            namePadding = showSender ? (isChat && Midlet.instance.config.gui_messagesAvatars && !Midlet.instance.config.doNotLoadImages && !(Midlet.instance.config.gui_messageDiffSides && m.out()) ? ListItem.smallHeightAvatar : AppCanvas.instance.boldEmojiFont.height) + AppCanvas.instance.perLineSpace : 0; //math max height avatar or font
            replyPadding = m.replyMessage() != null ? AppCanvas.instance.boldEmojiFont.height + AppCanvas.instance.normalEmojiFont.height + AppCanvas.instance.perLineSpace : 0;

            if (tattHeight == -1) {
                tattHeight = 0;
                if (photosH != null) {
                    for (int i = 0; i < photosH.length; ++i) {
                        tattHeight += photosH[i].intValue() + AppCanvas.instance.perLineSpace;
                    }
                }
                if (attachments != null) {
                    for (int i = 0; i < attachments.length; ++i) {
                        AttachmentView ii = attachments[i];
                        if (ii == null) {
                            continue;
                        }
                        ii.updateHeight();
                        tattHeight += ii.height + AppCanvas.instance.perLineSpace;
                    }
                }
            }
            attHeight = tattHeight;
            fwdsHeight = fwdTexts == null || fwdPeers == null ? 0 : fwdTexts.length * (AppCanvas.instance.boldEmojiFont.height + AppCanvas.instance.normalEmojiFont.height + AppCanvas.instance.perLineSpace);

            height = namePadding + Math.max(AppCanvas.tranSizeStatic * 2, AppCanvas.instance.perLineSpace + replyPadding + textHeight + fwdsHeight + attHeight) + AppCanvas.instance.perLineSpace * 2; // + AppCanvas.instance.perLineSpace * 2 + AppCanvas.instance.normalEmojiFont.getHeight();
            forceUpdate = false;
        }

        if (callQueue) {
            AppCanvas.instance.queue(this);
        }

        if (popup == null) {
            buildPopup();
        }
    }

    public void paint(Graphics g, int pY, int pX) {
        updateHeight();
        int origpy = pY;
        pY += AppCanvas.instance.perLineSpace;
        boolean selected = focusable && pressed;
        boolean queue = false;
        boolean unread = isMsgContent && !((ImContent) content).isComments() && ((!m.out() && ((ImContent) content).readedTo() < m.id()) || (m.out() && ((ImContent) content).outReadedTo() < m.id()));

        if (unread) {
            RenderUtil.fillRect(g, x + pX - (AppCanvas.instance.perLineSpace * 2), y + origpy, width + (AppCanvas.instance.perLineSpace * 4), height, Theming.now.unreadBackgroundColor, Theming.now.unreadBackgroundColor_);
        }

        if (showSender) {
            int nameXP = (Midlet.instance.config.gui_messageDiffSides && m.out() ? width - peerWidth - timeWidth - (avatarUrl != null ? ListItem.smallHeightAvatar : 0) : 0);
            pX += nameXP;

            if (Midlet.instance.config.gui_messageDiffSides && m.out()) {
                if (timeString != null) {
                    g.setColor(Theming.now.captionColor);
                    g.setFont(AppCanvas.instance.normalFont);
                    g.drawString(timeString, x + pX + (peerName != null ? peerWidth : 0), y + pY + timePadding, Graphics.TOP | Graphics.LEFT);
                }
            } else {
                int avaPX = 0, avaPY = 0;
                if (isChat && Midlet.instance.config.gui_messagesAvatars && !Midlet.instance.config.doNotLoadImages) {
                    avaPX = ListItem.smallHeightAvatar + AppCanvas.instance.perLineSpace;
                    avaPY = (ListItem.smallHeightAvatar - AppCanvas.instance.boldEmojiFont.height) / 2;
                    if (avatarUrl != null) {
                        Image o = content != null ? (Image) content.screenCache.get(avatarUrl) : null;
                        if (o != null) {
                            g.drawImage(o, x + pX, y + pY, Graphics.TOP | Graphics.LEFT);
                        }
                    } else {
                        queue = true;
                        g.setColor(Theming.now.nonLoadedContentColor);
                        g.fillRect(x + pX, y + pY, ListItem.smallHeightAvatar, ListItem.smallHeightAvatar);
                    }
                    if (ListItem.smallCircleImage != null) {
                        g.drawImage(ListItem.smallCircleImage, x + pX, y + pY, Graphics.TOP | Graphics.LEFT);
                    }
                    if (cornerIcon != null) {
                        if (ListItem.cornerBgImage != null) {
                            g.drawImage(ListItem.cornerBgImage, x + pX + ListItem.smallHeightAvatar - ListItem.cornerIconSize / 2, y + pY + ListItem.smallHeightAvatar - ListItem.cornerIconSize / 2, Graphics.VCENTER | Graphics.HCENTER);
                        }
                        RenderUtil.renderListItemIcon(g, x + pX + ListItem.smallHeightAvatar - ListItem.cornerIconSize / 2, y + pY + ListItem.smallHeightAvatar - ListItem.cornerIconSize / 2, cornerIcon,
                                1,
                                Theming.now.onlineIconColor,
                                Theming.now.onlineIconColor, ListItem.cornerIconSize, Graphics.VCENTER | Graphics.HCENTER);
                    }

                }
                pX += avaPX;
                pY += avaPY;

                if (peerName != null) {
                    g.setColor(Theming.now.textColor);
                    AppCanvas.instance.boldEmojiFont.drawString(g, peerName, x + pX, y + pY, Graphics.TOP | Graphics.LEFT);
                }

                if (timeString != null) {
                    g.setColor(Theming.now.captionColor);
                    g.setFont(AppCanvas.instance.normalFont);
                    g.drawString(timeString, x + pX + (peerName != null ? peerWidth : 0), y + pY + timePadding, Graphics.TOP | Graphics.LEFT);
                }
                pX -= avaPX;
                pY -= avaPY;
            }
            pX -= nameXP;
            pY += namePadding;
        }

        int cloudH = height - AppCanvas.instance.perLineSpace * 2 - namePadding;
        int cloudW = Math.max(AppCanvas.instance.perLineSpace + maxTextWidth + AppCanvas.instance.perLineSpace, AppCanvas.tranSizeStatic * 2); //max width of content
        int cPadd = (Midlet.instance.config.gui_messageDiffSides && m.out() ? width - cloudW : 0);
        if (Midlet.instance.config.gui_showClouds && (replyPadding != 0 || !onlySticker)) {
            RenderUtil.fillRect(g, x + pX + cPadd, y + pY, cloudW, cloudH, m.out() ? Theming.now.userMessageColor : Theming.now.somebodyMessageColor, m.out() ? Theming.now.userMessageColor_ : Theming.now.somebodyMessageColor_);
        }
        if ((Midlet.instance.config.gui_showClouds && Midlet.instance.config.gui_cloudBorder && (replyPadding != 0 || !onlySticker)) || selected) {
            g.setColor(selected ? Theming.now.focusedBorderColor : m.out() ? Theming.now.userMessageBorderColor : Theming.now.somebodyMessageBorderColor);
            g.drawRect(x + pX + cPadd, y + pY, cloudW - 1, cloudH - 1);
        }

        Image tranImage;
        if (unread) {
            tranImage = AppCanvas.tranImageUnread;
        } else {
            tranImage = AppCanvas.tranImageStandard;
        }
        if (tranImage != null && ((Midlet.instance.config.gui_showClouds && (replyPadding != 0 || !onlySticker)) || selected)) {
            if (Midlet.instance.config.gui_messageDiffSides && m.out()) {
                g.drawImage(tranImage, x + pX + cPadd, y + pY, Graphics.LEFT | Graphics.TOP);
            } else {
                g.drawRegion(tranImage,
                        0, 0, AppCanvas.tranSize, AppCanvas.tranSize, Sprite.TRANS_ROT180,
                        x + pX + cPadd + cloudW - AppCanvas.tranSize, y + pY + cloudH - AppCanvas.tranSize, Graphics.LEFT | Graphics.TOP);
            }
            g.drawRegion(tranImage,
                    0, 0, AppCanvas.tranSize, AppCanvas.tranSize, Sprite.TRANS_ROT90,
                    x + pX + cPadd + cloudW - AppCanvas.tranSize, y + pY, Graphics.LEFT | Graphics.TOP);
            g.drawRegion(tranImage,
                    0, 0, AppCanvas.tranSize, AppCanvas.tranSize, Sprite.TRANS_ROT270,
                    x + pX + cPadd, y + pY + cloudH - AppCanvas.tranSize, Graphics.LEFT | Graphics.TOP);
        }

        if ((Midlet.instance.config.gui_showClouds && Midlet.instance.config.gui_cloudBorder && (replyPadding != 0 || !onlySticker)) || selected) {
            Image selectedBorderImage;
            if (selected) {
                selectedBorderImage = AppCanvas.selBorderImage;
            } else if (m != null && m.out()) {
                selectedBorderImage = AppCanvas.outBorderImage;
            } else {
                selectedBorderImage = AppCanvas.borderImage;
            }

            if (selectedBorderImage != null) {
                if (Midlet.instance.config.gui_messageDiffSides && m.out()) {
                    g.drawImage(selectedBorderImage, x + pX + cPadd, y + pY, Graphics.LEFT | Graphics.TOP);
                } else {
                    g.drawRegion(selectedBorderImage,
                            0, 0, AppCanvas.tranSize, AppCanvas.tranSize, Sprite.TRANS_ROT180,
                            x + pX + cPadd + cloudW - AppCanvas.tranSize, y + pY + cloudH - AppCanvas.tranSize, Graphics.LEFT | Graphics.TOP);
                }
                g.drawRegion(selectedBorderImage,
                        0, 0, AppCanvas.tranSize, AppCanvas.tranSize, Sprite.TRANS_ROT90,
                        x + pX + cPadd + cloudW - AppCanvas.tranSize, y + pY, Graphics.LEFT | Graphics.TOP);
                g.drawRegion(selectedBorderImage,
                        0, 0, AppCanvas.tranSize, AppCanvas.tranSize, Sprite.TRANS_ROT270,
                        x + pX + cPadd, y + pY + cloudH - AppCanvas.tranSize, Graphics.LEFT | Graphics.TOP);
            }
        }

        pY += AppCanvas.instance.perLineSpace;
        pX += AppCanvas.instance.perLineSpace;

        if (replyPadding != 0) {
            g.setColor(m.out() ? Theming.now.userMessageQuoteColor : Theming.now.somebodyMessageQuoteColor);
            g.fillRect(x + pX + cPadd, y + pY, AppCanvas.instance.perLineSpace, replyPadding - AppCanvas.instance.perLineSpace);

            pX += AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace;

            g.setColor(Theming.now.textColor);
            if (replyPeer != null) {
                AppCanvas.instance.boldEmojiFont.drawString(g, replyPeer, x + pX + cPadd, y + pY, Graphics.TOP | Graphics.LEFT);
            }

            pY += AppCanvas.instance.boldEmojiFont.height;

            if (replyText != null) {
                AppCanvas.instance.normalEmojiFont.drawString(g, replyText, x + pX + cPadd, y + pY, Graphics.TOP | Graphics.LEFT);
            }

            pY += AppCanvas.instance.normalEmojiFont.height + AppCanvas.instance.perLineSpace;

            pX -= AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace;
        }

        if (v != null) {
            g.setColor(Theming.now.textColor);
            int ci = calcI(origpy);
            FontWithEmoji fn = AppCanvas.instance.normalEmojiFont;
            pY += Math.min(ci, v.length) * fn.height;
            int cm = calcMax(origpy);
            for (int i = ci; i < cm; i++) {
                fn.drawString(g, v[i], x + pX + cPadd, y + pY, Graphics.TOP | Graphics.LEFT);
                pY += fn.height;
            }
            pY += AppCanvas.instance.perLineSpace;
        }

        g.setColor(m.out() ? Theming.now.userMessageQuoteColor : Theming.now.somebodyMessageQuoteColor);
        g.fillRect(x + pX + cPadd, y + pY, AppCanvas.instance.perLineSpace, fwdsHeight - AppCanvas.instance.perLineSpace);

        pX += AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace;

        if (fwdTexts != null && fwdPeers != null) {
            for (int i = 0; i < fwdTexts.length; ++i) {
                SuperString replyPeer = fwdPeers[i];
                SuperString replyText = fwdTexts[i];

                g.setColor(Theming.now.textColor);
                if (replyPeer != null) {
                    AppCanvas.instance.boldEmojiFont.drawString(g, replyPeer, x + pX + cPadd, y + pY, Graphics.TOP | Graphics.LEFT);
                }

                pY += AppCanvas.instance.boldEmojiFont.height;

                if (replyText != null) {
                    AppCanvas.instance.normalEmojiFont.drawString(g, replyText, x + pX + cPadd, y + pY, Graphics.TOP | Graphics.LEFT);
                }

                pY += AppCanvas.instance.normalEmojiFont.height + AppCanvas.instance.perLineSpace;
            }
        }

        pX -= AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace;

        if (photos != null) {
            for (int i = 0; i < photos.length; ++i) {
                Image ii = photos[i];
                if (ii == null) {
                    queue = true;
                    g.setColor(Theming.now.nonLoadedContentColor);
                    g.fillRect(x + pX + cPadd, y + pY, photosW[i].intValue(), photosH[i].intValue());
                } else {
                    //instead of pX - (cloudW - ii.getWidth()) / 2 - for center
                    g.drawImage(ii, x + pX + cPadd, y + pY, Graphics.TOP | Graphics.LEFT);
                }

                pY += photosH[i].intValue() + AppCanvas.instance.perLineSpace;
            }
        }

        if (attachments != null) {
            for (int i = 0; i < attachments.length; ++i) {
                AttachmentView aV = attachments[i];
                if (aV == null) {
                    continue;
                }
                aV.content = content;
                aV.width = maxTextWidth;
                aV.paint(g, y + pY, x + pX + cPadd);
                pY += aV.height + AppCanvas.instance.perLineSpace;
            }
        }

        //pX -= AppCanvas.instance.perLineSpace;
        //pY += AppCanvas.instance.perLineSpace;
        //g.setColor(Theming.now.captionColor);
        //if (timeString != null) AppCanvas.instance.normalEmojiFont.drawString(g, timeString, x + pX + (Midlet.instance.config.gui_messageDiffSides && m.out ? width : 0), y + pY, Graphics.TOP | (Midlet.instance.config.gui_messageDiffSides && m.out ? Graphics.RIGHT : Graphics.LEFT), true);
        //pY += AppCanvas.instance.normalEmojiFont.getHeight() + AppCanvas.instance.perLineSpace;

        if (queue) {
            AppCanvas.instance.queue(this);
        }
    }

    private int calcI(int pY) {
        int pos = this.y + pY + namePadding + replyPadding + AppCanvas.instance.perLineSpace - (!(content instanceof PopupMenu) ? AppCanvas.instance.contentY : AppCanvas.instance.popupY()) + AppCanvas.instance.perLineSpace;
        int i = 0;
        if (pos < 0) {
            i += -pos / AppCanvas.instance.normalEmojiFont.height;
        }
        return i;
    }

    private int calcMax(int pY) {
        int pos = this.y + pY + namePadding + replyPadding + AppCanvas.instance.perLineSpace - (!(content instanceof PopupMenu) ? AppCanvas.instance.contentY : AppCanvas.instance.popupY()) + AppCanvas.instance.perLineSpace + textHeight - (!(content instanceof PopupMenu) ? AppCanvas.instance.contentHeight : AppCanvas.instance.popupHeight());
        int i = v.length;
        if (pos > 0) {
            i += -pos / AppCanvas.instance.normalEmojiFont.height;
        }
        return i;
    }

    public void actionPerformed() {
        if (popup == null) {
            return;
        }
        AppCanvas.instance.showPopup(popup);
    }

    public void updateShowName(MainImItem prevItem) {
        showSender = (prevItem == null || prevItem.m == null || m == null || prevItem.m.fromId() != m.fromId() || Math.abs(prevItem.m.getLastTime() - m.getLastTime()) > 300);
        forceUpdate = true;
    }

    public void errored(Throwable ex) {
        AppCanvas.instance.dropError("IM view: " + ex != null ? ex.getMessage() : "Unknown error");
    }
    
    public int id() {
        return m.id();
    }
    
    public boolean out() {
        return m.out();
    }
}
