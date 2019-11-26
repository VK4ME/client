package ru.curoviyxru.phoenix.ui.contents;

import javax.microedition.lcdui.Graphics;
import org.json.me.JSONArray;
import ru.curoviyxru.j2vk.LongPoll;
import ru.curoviyxru.j2vk.PageStorage;
import ru.curoviyxru.j2vk.VKConstants;
import ru.curoviyxru.j2vk.api.objects.Conversation;
import ru.curoviyxru.j2vk.api.objects.ImItem;
import ru.curoviyxru.j2vk.api.objects.Message;
import ru.curoviyxru.j2vk.api.requests.messages.MessagesGetById;
import ru.curoviyxru.j2vk.api.requests.messages.MessagesGetHistory;
import ru.curoviyxru.j2vk.api.requests.messages.MessagesMarkAsRead;
import ru.curoviyxru.j2vk.api.requests.messages.MessagesSetActivity;
import ru.curoviyxru.j2vk.api.responses.messages.MessagesGetByIdResponse;
import ru.curoviyxru.j2vk.api.responses.messages.MessagesGetHistoryResponse;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.ConversationItem;
import ru.curoviyxru.phoenix.ui.KeyboardUtil;
import ru.curoviyxru.phoenix.ui.ListItem;
import ru.curoviyxru.phoenix.ui.PaneItem;
import ru.curoviyxru.phoenix.ui.PopupButton;
import ru.curoviyxru.phoenix.ui.PopupMenu;
import ru.curoviyxru.phoenix.ui.im.ImContent;
import ru.curoviyxru.phoenix.ui.im.ImField;
import ru.curoviyxru.phoenix.ui.im.ImHolder;
import ru.curoviyxru.phoenix.ui.im.MainImItem;
import ru.curoviyxru.phoenix.ui.im.ServiceImItem;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class MessageContent extends TwoScrollContent implements ImContent {
    
    public static MessageContent MESSAGE_CONTENT = null;

    public static void updateOnline(long peer_id) {
        Content content = MessageContent.getMessageContent();
        if (content instanceof MessageContent) {
            MessageContent current = (MessageContent) content;

            if (current.c.getId() != peer_id) {
                return;
            }
            PageStorage.load(peer_id);
            synchronized (current.lock) {
                current.setCornerIcon(ListItem.getCornerPath(ContentController.getStatusIcon(PageStorage.get(peer_id))));
                current.renderIfNeeded();
            }
        }
    }

    public static Content getMessageContent() {
        if (MESSAGE_CONTENT != null) {
            return MESSAGE_CONTENT;
        }
        
        Content content = AppCanvas.instance.content;
        if (content instanceof MessageContent) {
            MESSAGE_CONTENT = (MessageContent) content;
            return content;
        }
        
        while (content != null) {
            content = content.parent;
            if (content instanceof MessageContent) {
                MESSAGE_CONTENT = (MessageContent) content;
                return content;
            }
        }
        
        return null;
    }

    int typing;
    String oldCornerIcon;
    boolean forceDown = false;
    public int readTo, readedTo, outReadedTo;
    int min_id = -1, max_id = -1;
    public final ImField msgField;
    int pressedY;
    long typingLong;

    public void updateTyping() {
        if (msgField != null) {
            msgField.updateTyping();
        }
    }

    public void itemRendered(PaneItem item, Graphics g, int pY, int pX) {
        if (item instanceof ImHolder && !((ImHolder) item).out() && ((ImHolder) item).id() > readTo) {
            readTo = ((ImHolder) item).id();
        }
    }

    public static void gotRead(int code, long peer_id, int mid) {
        Content content = MessageContent.getMessageContent();
        if (content instanceof MessageContent) {
            MessageContent current = (MessageContent) content;

            if (current.c.getId() != peer_id) {
                return;
            }
            synchronized (current.lock) {
                if (code == 7) {
                    current.outReadedTo = Math.max(mid, current.outReadedTo);
                } else {
                    current.readedTo = Math.max(mid, current.readedTo);
                }
                current.renderIfNeeded();
            }
        }
    }

    public void loadNextForce() {
        if (loading) {
            return;
        }
        loading = true;

        new Thread() {
            public void run() {
                try {
                    synchronized (lock) {
                        int old = size();
                        boolean scrollTo = totalHeight - toScrollY - contentHeight <= AppCanvas.instance.normalEmojiFont.height * 2;
                        setTyping(0);
                        updateLabel();
                        processNext();
                        
                        if (scrollTo && old != size()) {
                            scrollTo(size() - 1);
                        }
                        
                        loading = false;
                    }
                } catch (Exception e) {
                    loading = false;
                    AppCanvas.instance.dropError(e);
                } catch (OutOfMemoryError error) {
                    loading = false;
                    imOut();
                }

                AppCanvas.instance.setProgress(false);
            }
        }.start();
    }
    
    public static void gotNew(int msg_id, long peer_id) {
        Content content = MessageContent.getMessageContent();
        if (content instanceof MessageContent) {
            MessageContent current = (MessageContent) content;

            if (current.c.getId() != peer_id) {
                return;
            }

            synchronized (current.lock) {
                if (!current.noNext || current.max_id >= msg_id) {
                    return;
                }

                //TODO update photo, pinned message, chat label
                
                current.loadNextForce();
            }
        }
    }

    public static void gotEdit(int msg_id, long peer_id) {
        Content content = MessageContent.getMessageContent();
        if (content instanceof MessageContent) {
            MessageContent current = (MessageContent) content;

            if (current.c.getId() != peer_id) {
                return;
            }

            synchronized (current.lock) {
                MessagesGetByIdResponse response = (MessagesGetByIdResponse) new MessagesGetById().addMessageId(msg_id).execute();
                if (response != null && response.hasMessage()) {
                    //boolean scrollTo = current.totalHeight - content.toScrollY - current.contentHeight <= AppCanvas.instance.normalEmojiFont.getHeight() * 2;
                    //boolean scrollTo = current.selectedY == old-1;
                    Message msg = response.getMessage();
                    int indexes = current.indexOf(msg.id);

                    if (indexes != -1) {
                        current.removeAt(indexes);
                        current.insert(current.getMessageItem(msg), indexes);
                        //Think about scrolling.
                        ///if (scrollTo) {
                        //    renderFromTo();
                        //    //current.scrollTo(current.size() - 1);
                        ////}
                        //toScrollY += totalHeight - oldHeight;
                        current.renderIfNeeded();
                    }
                }
            }
        }
    }

    public static void gotDelete(int msg_id, long peer_id) {
        Content content = MessageContent.getMessageContent();
        if (content instanceof MessageContent) {
            MessageContent current = (MessageContent) content;

            if (current.c.getId() != peer_id) {
                return;
            }

            synchronized (current.lock) {
                int index = current.indexOf(msg_id);
                if (index != -1) {
                    current.removeAt(index);
                    current.renderIfNeeded();
                }
            }
        }
    }

    public void paint(Graphics g, int pX) {
        msgField.width = AppCanvas.instance.getWidth();
        msgField.x = 0;
        msgField.updateHeight();
        contentHeight = AppCanvas.instance.contentOriginalHeight - msgField.height;
        msgField.y = AppCanvas.instance.contentY + contentHeight;
        if (g == null) {
            return;
        }
        super.paint(g, contentHeight, pX);
        msgField.paint(g, 0, pX);
    }

    public void pointerPressed(int x, int y) {
        pressedY = y;

        if (pressedY >= AppCanvas.instance.contentY + contentHeight) {
            msgField.pointerPressed(x, y, 0);
        } else {
            super.pointerPressed(x, y);
        }
    }

    public void pointerReleased(int x, int y) {
        if (pressedY >= AppCanvas.instance.contentY + contentHeight) {
            msgField.pointerReleased(x, y, 0);
        } else {
            super.pointerReleased(x, y);
        }
    }

    public void pointerDragged(int x, int y) {
        if (pressedY >= AppCanvas.instance.contentY + contentHeight) {
            msgField.pointerDragged(x, y, 0);
        } else {
            super.pointerDragged(x, y);
        }
    }

    public void keyRepeated(int x) {
        if ((KeyboardUtil.isAccepted(x) || (x == AppCanvas.ENTER || x == AppCanvas.FIRE
                || x == AppCanvas.LEFT || x == AppCanvas.RIGHT))
                && !AppCanvas.instance.popupOpened()
                && c.canWrite() && (msgField.opened || (!Midlet.instance.config.usePQSofts && x != AppCanvas.FIRE && x != AppCanvas.KEY_NUM2 && x != AppCanvas.KEY_NUM8))) {
            if (!msgField.opened) {
                msgField.setOpened(true);
            }
            msgField.keyRepeated(x, scrollY);
        } else {
            super.keyRepeated(x);
        }
    }

    public void keyReleased(int x) {
        if ((KeyboardUtil.isAccepted(x) || (x == AppCanvas.ENTER || x == AppCanvas.FIRE
                || x == AppCanvas.LEFT || x == AppCanvas.RIGHT))
                && !AppCanvas.instance.popupOpened()
                && c.canWrite() && (msgField.opened || (!Midlet.instance.config.usePQSofts && x != AppCanvas.FIRE && x != AppCanvas.KEY_NUM2 && x != AppCanvas.KEY_NUM8))) {
            if (!msgField.opened) {
                msgField.setOpened(true);
            }
            msgField.keyReleased(x, scrollY);
        } else {
            super.keyReleased(x);
        }
    }

    public void keyPressed(int x) {
        if ((KeyboardUtil.isAccepted(x) || (x == AppCanvas.ENTER || x == AppCanvas.FIRE
                || x == AppCanvas.LEFT || x == AppCanvas.RIGHT))
                && !AppCanvas.instance.popupOpened()
                && c.canWrite() && (msgField.opened || (!Midlet.instance.config.usePQSofts && x != AppCanvas.FIRE && x != AppCanvas.KEY_NUM2 && x != AppCanvas.KEY_NUM8))) {
            if (!msgField.opened) {
                msgField.setOpened(true);
            }
            msgField.keyPressed(x, scrollY);
        } else {
            super.keyPressed(x);
        }
    }

    public void rightSoft() {
        if (!Midlet.instance.config.oldMessagesSoft) {
            super.rightSoft();
            return;
        }
        if (//!AppCanvas.instance.touchHud && 
                c.canWrite()) {
            msgField.setOpened(); //TODO: global rightsoft menu
        }
    }

    public String getRightSoft() {
        if (!Midlet.instance.config.oldMessagesSoft) {
            return super.getRightSoft();
        }
        return //AppCanvas.instance.touchHud ? null : 
                !c.canWrite() ? null : msgField.opened ? Localization.get("action.hide") : Localization.get("action.reveal"); //TODO: global rightsoft menu
    }

    //update view state
    public MessageContent(final Conversation c) {
        super(c);
        readTo = readedTo = c.inRead;
        outReadedTo = c.outRead;
        msgField = new ImField(this);

        rightSoft = new PopupMenu(Localization.get("general.actions"));
        if (c.canWrite()) {
            rightSoft.add(new PopupButton(Localization.get("action.fieldVisibility")) {
                public void actionPerformed() {
                    msgField.setOpened();
                    AppCanvas.instance.closePopup();
                }
            }.setIcon("new/pencil.rle"));
        }
        rightSoft.add(new PopupButton(Localization.get("action.scrollToBottom")) {
            public void actionPerformed() {
                synchronized (lock) {
                    min_id = max_id = -1;
                    forceDown = true;
                    noNext = noPrev = loadedFirst = false;
                    removeAll();
                    loadFirst();
                }
                AppCanvas.instance.closePopup();
            }
        }.setIcon("new/chevron-down.rle"));
        if (c.isPage()) {
            PageStorage.load(c.getId());
            setCornerIcon(ListItem.getCornerPath(ContentController.getStatusIcon(PageStorage.get(c.getId()))));
            
            rightSoft.add(new PopupButton(Localization.get("action.goToProfile")) {
                public void actionPerformed() {
                    ContentController.showProfile(MessageContent.this, PageStorage.get(c.getId()));
                    AppCanvas.instance.closePopup();
                }
            }.setIcon("new/account.rle"));
        }
        updateLabel();
        loadFirst();
        paint(null, 0);
    }

    public void processFirst() {
        if (forceDown || Midlet.instance.config.showMessagesFromEnd) {
            processNext(true);
        } else {
            processPrev();
        }
        scrollTo(size() - 1);
        processNext();
    }

    public void processPrev() {
        AppCanvas.instance.setProgress(true);
		
        int oldSize = size();
		
        MessagesGetHistory r = new MessagesGetHistory(c.getId(), 5, min_id == -1 ? 0 : 1).setStartMessageId(min_id);
        final MessagesGetHistoryResponse rr = (MessagesGetHistoryResponse) r.execute();
        AppCanvas.instance.setProgress(false);

        if (rr != null && rr.hasItems()) {
            if (rr.items.length < 5) {
                noPrev = true;
            }
            int ii = 0;
            for (int i = rr.items.length - 1; i >= 0; --i) {
                Message item = rr.items[i];
                if (item == null) {
                    continue;
                }
                if (min_id == -1 || min_id > item.id) {
                    min_id = item.id;
                }
                if (max_id == -1 || max_id < item.id) {
                    max_id = item.id;
                }

                insert(getMessageItem(item), ii++);
            }
        } else {
            noPrev = true;
            if (rr == null) {
                AppCanvas.instance.dropError(Localization.get("general.loadError"));
            }
        }

        //TODO: Nonext if maxid == last and remove processNext() in processFirst() (?) 
		
        selectedY += size() - oldSize;
        updateHeightsAdd(getWidth(), size() - oldSize);
		
    }

    public void processNext() {
        processNext(false);
    }

    public void processNext(boolean first) {
        AppCanvas.instance.setProgress(true);
        int ts = size();

        MessagesGetHistory r = new MessagesGetHistory(c.getId(), 5, first ? 0 : -5);
        if (!first) {
            r.setStartMessageId(max_id);
        }
        final MessagesGetHistoryResponse rr = (MessagesGetHistoryResponse) r.execute();
        AppCanvas.instance.setProgress(false);

        if (rr != null && rr.hasItems()) {
            if (rr.items.length < 5) {
                noNext = true;
            }
            for (int i = rr.items.length - 1; i >= 0; i--) {
                Message item = rr.items[i];
                if (item == null) {
                    continue;
                }
                if (min_id == -1 || min_id > item.id) {
                    min_id = item.id;
                }
                if (max_id == -1 || max_id < item.id) {
                    max_id = item.id;
                }

                add(getMessageItem(item));
            }
        } else {
            noNext = true;
            if (rr == null) {
                AppCanvas.instance.dropError(Localization.get("general.loadError"));
            }
        }

        updateHeightsFromEnd(getWidth(), size() - ts);
    }

    private int indexOf(int id) {
        for (int i = size() - 1; i >= 0; i--) {
            PaneItem item = at(i);
            if (item instanceof ImHolder && ((ImHolder) item).id() == id) {
                return i;
            }
        }

        return -1;
    }

    public static void updateTypingUsers(JSONArray optJSONArray, long peer_id, int ts) {
        Content content = MessageContent.getMessageContent();
        if (content instanceof MessageContent) {
            MessageContent current = (MessageContent) content;

            if (current.c.getId() != peer_id) {
                return;
            }

            synchronized (current.lock) {
                current.setTyping(5);
                current.updateLabel();
            }
        }
    }

    public static void updateTypingUser(long peer_id, long uid) {
        Content content = MessageContent.getMessageContent();
        if (content instanceof MessageContent) {
            MessageContent current = (MessageContent) content;

            if (current.c.getId() != peer_id) {
                return;
            }
            synchronized (current.lock) {
                current.setTyping(5);
                current.updateLabel();
            }
        }
    }

    public void updateLabel() {
        if (readTo > readedTo && !Midlet.instance.config.DNR) {
            readedTo = Math.max(readTo, readedTo);
            new MessagesMarkAsRead(c.getId(), readTo).execute();
        }

        String prefix = "";
        ConversationItem ci = ConversationList.instance.get(c.getId(), false);
        if (ci != null && ci.rightState > 0) {
            prefix = "(+" + ci.rightState + ") ";
        }

        setTitle(prefix + (c.getId() == VKConstants.account.getId() ? Localization.get("element.favorites") : c.getTitle()));

        if (AppCanvas.lastField instanceof ImField && !Midlet.instance.config.DNT && System.currentTimeMillis() - typingLong > 4000) {
            typingLong = System.currentTimeMillis();
            new MessagesSetActivity(c.getId(), MessagesSetActivity.TYPING).execute();
        }

        renderIfNeeded();
    }

    public void setTyping(int i) {
        synchronized (lock) {
            typing = Math.max(i, 0);
            if (oldCornerIcon == null) {
                oldCornerIcon = cornerIcon;
            }
            if (typing == 0) {
                if (cornerIcon != null && cornerIcon.equals(ListItem.getCornerPath(7)))
                    setCornerIcon(oldCornerIcon);
                oldCornerIcon = null;
            } else {
                setCornerIcon(ListItem.getCornerPath(7));
            }
        }
        //renderIfNeeded();
    }

    public void updateTypingState() {
        if (typing > 0) {
            setTyping(typing - 1);
        }
    }

    public Content insert(PaneItem item, int i) {
        super.insert(item, i);

        updateMessageSender(i);
        updateMessageSender(i + 1);

        return this;
    }

    public Content remove(PaneItem i) {
        return removeAt(indexOf(i));
    }

    public Content removeAt(int i) {
        super.removeAt(i);

        updateMessageSender(i + 1);

        return this;
    }

    public Content add(PaneItem item) {
        super.add(item);

        updateMessageSender(size() - 1);

        return this;
    }

    private void updateMessageSender(int at) {
        PaneItem i = at >= 0 && at < size() ? at(at) : null;
        PaneItem i2 = at - 1 >= 0 && at - 1 < size() ? at(at - 1) : null;
        if (i instanceof MainImItem) {
            ((MainImItem) i).updateShowName(i2 instanceof MainImItem ? (MainImItem) i2 : null);
        }
    }

    private PaneItem getMessageItem(Message m) {
        if (m.hasChatAction()) {
            return new ServiceImItem(this, m);
        }

        return new MainImItem(this, m);
    }
    
    public void readTo(ImItem m) {
        synchronized (lock) {
            if (m.id() > readedTo) {
                readedTo = m.id();
                new MessagesMarkAsRead(c.getId(), m.id()).execute();
                LongPoll.skipWait = true;
            }
        }
    }

    public boolean isChat() {
        return c.isChat();
    }

    public boolean canWrite() {
        return c.canWrite();
    }

    public ImField field() {
        return msgField;
    }

    public void gotDelete(int id) {
        
    }

    public boolean isAdmin() {
        return c.hasChatSettings() && ((Conversation) c).chatSettings.isAdmin(VKConstants.account.getId());
    }

    public void setThread(ImItem m) {
        
    }

    public void gotEdit(int id) {
        
    }

    public long ownerId() {
        return 0;
    }

    public long id() {
        return c.getId();
    }

    public void gotNew(int commentId) {
        
    }

    public boolean isComments() {
        return false;
    }

    public int readedTo() {
        return readedTo;
    }

    public int outReadedTo() {
        return outReadedTo;
    }

    public int pinned() {
        return c.hasChatSettings() && ((Conversation) c).chatSettings.hasPinnedMessage() ? ((Conversation) c).chatSettings.pinnedMessage.id : 0;
    }

    public void setPinned(ImItem m) {
        if (c.hasChatSettings()) {
            ((Conversation) c).chatSettings.pinnedMessage = (Message) m;
        }
    }
}
