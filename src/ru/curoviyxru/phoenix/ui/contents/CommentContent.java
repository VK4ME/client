package ru.curoviyxru.phoenix.ui.contents;

import javax.microedition.lcdui.Graphics;
import ru.curoviyxru.j2vk.api.objects.ImItem;
import ru.curoviyxru.j2vk.api.objects.attachments.Comment;
import ru.curoviyxru.j2vk.api.objects.attachments.Post;
import ru.curoviyxru.j2vk.api.requests.wall.WallGetComment;
import ru.curoviyxru.j2vk.api.requests.wall.WallGetComments;
import ru.curoviyxru.j2vk.api.responses.wall.WallGetCommentResponse;
import ru.curoviyxru.j2vk.api.responses.wall.WallGetCommentsResponse;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.KeyboardUtil;
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
 * @author curoviyxru
 */
public class CommentContent extends ScrollContent implements PostContent, ImContent {

    public final ImField msgField;
    boolean forceDown = false;
    int pressedY;
    long typingLong;
    public Post c;
    int deleteFrom = 0;
    public Comment thread;
    boolean showReplies = Midlet.instance.config.showReplies;

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

    public Content removeAll() {
        for (int i = deleteFrom; i < size();) {
            removeAt(i);
        }
        System.gc();
        return this;
    }

    public void addPostItem(PaneItem i) {
        add(i);
        deleteFrom++;
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
        if ((KeyboardUtil.isAccepted(x) || (x == AppCanvas.ENTER || x == AppCanvas.FIRE || x == AppCanvas.LEFT || x == AppCanvas.RIGHT))
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
        if (!Midlet.instance.config.oldCommentsSoft) {
            super.rightSoft();
            return;
        }
        if (//!AppCanvas.instance.touchHud && 
                c.canWrite()) {
            msgField.setOpened(); //TODO: global rightsoft menu
        }
    }
    
    public String getRightSoft() {
        if (!Midlet.instance.config.oldCommentsSoft) {
            return super.getRightSoft();
        }
        return //AppCanvas.instance.touchHud ? null : 
                !c.canWrite() ? null : msgField.opened ? Localization.get("action.hide") : Localization.get("action.reveal"); //TODO: global rightsoft menu
    }
    
    //update view state
    public CommentContent(Post c, Comment comment) {
        super(Localization.get("title.comments"), true);
        this.c = c;
        this.thread = comment;
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
        rightSoft.add(new PopupButton(Localization.get("action.refresh")) {
            public void actionPerformed() {
                refresh();
                AppCanvas.instance.closePopup();
            }
        }.setIcon("new/refresh.rle"));
        rightSoft.add(new PopupButton(Localization.get("action.upperComments")) {
            public void actionPerformed() {
                synchronized (lock) {
                    next = null;
                    thread = null;
                    noNext = false;
                    removeAll();
                    loadNext();
                }
                AppCanvas.instance.closePopup();
            }
        }.setIcon("new/chevron-up.rle"));
        rightSoft.add(new PopupButton(Localization.get("action.switchOrder")) {
            public void actionPerformed() {
                synchronized (lock) {
                    next = null;
                    forceDown = !forceDown;
                    noNext = false;
                    removeAll();
                    loadNext();
                }
                AppCanvas.instance.closePopup();
            }
        }.setIcon("new/chevron-down.rle"));
        rightSoft.add(new PopupButton(Localization.get("action.switchReplies")) {
            public void actionPerformed() {
                synchronized (lock) {
                    next = null;
                    showReplies = !showReplies;
                    noNext = false;
                    removeAll();
                    loadNext();
                }
                AppCanvas.instance.closePopup();
            }
        }.setIcon("new/message.rle"));

        NewsfeedContent.addPost(this, c);
        loadNext();
        paint(null, 0);
    }

    public void process() {
        AppCanvas.instance.setProgress(true);
        int ts = size();

        WallGetComments r = new WallGetComments(5, next == null ? 0 : 1).setPost((Post) c).setSort(forceDown ? "desc" : "asc");
        if (thread != null) {
            r.setCommentId(thread.id);
        }
        if (next != null) {
            r.setStartCommentId(((Integer) next).intValue());
        } else {
            next = new Integer(forceDown ? Integer.MAX_VALUE : Integer.MIN_VALUE);
        }
        final WallGetCommentsResponse rr = (WallGetCommentsResponse) r.execute();
        AppCanvas.instance.setProgress(false);

        if (rr != null && rr.hasItems()) {
            if (rr.items.length < 5) {
                noNext = true;
            }
            for (int i = 0; i < rr.items.length; ++i) {
                Comment item = rr.items[i];
                if (item == null) {
                    continue;
                }

                if ((!forceDown && ((Integer) next).intValue() < item.id) || (forceDown && ((Integer) next).intValue() > item.id)) {
                    next = new Integer(item.id);
                }

                add(getMessageItem(item));

                if (showReplies && item.hasThread() && item.thread.hasItems()) {
                    for (int j = 0; j < item.thread.items.length; ++j) {
                        Comment replyItem = item.thread.items[j];
                        if (replyItem == null) {
                            continue;
                        }

                        replyItem.replyMessage = item;
                        add(getMessageItem(replyItem));
                    }
                }
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
//        PaneItem i = at >= 0 && at < size() ? at(at) : null;
//        PaneItem i2 = at - 1 >= 0 && at - 1 < size() ? at(at - 1) : null;
//        if (i instanceof MainCommentItem) {
//            ((MainCommentItem) i).updateShowName(i2 instanceof MainCommentItem ? (MainCommentItem) i2 : null);
//        }
    }

    private PaneItem getMessageItem(Comment m) {
        if (m.isDeleted()) {
            return new ServiceImItem(this, m);
        }

        return new MainImItem(this, m);
    }

    public void gotDelete(int id) {
        Content content = AppCanvas.instance.content;
        if (content instanceof CommentContent) {
            CommentContent current = (CommentContent) content;

            synchronized (current.lock) {
                int index = current.indexOf(id);
                if (index != -1) {
                    current.removeAt(index);
                    current.renderIfNeeded();
                }
            }
        }
    }

    public void gotEdit(int id) {
        Content content = AppCanvas.instance.content;
        if (content instanceof CommentContent) {
            CommentContent current = (CommentContent) content;

            synchronized (current.lock) {
                WallGetCommentResponse response = (WallGetCommentResponse) new WallGetComment(current.c.owner_id, id).execute();
                if (response != null && response.hasComment()) {
                    //boolean scrollTo = current.totalHeight - content.toScrollY - current.contentHeight <= AppCanvas.instance.normalEmojiFont.getHeight() * 2;
                    //boolean scrollTo = current.selectedY == old-1;
                    Comment msg = response.getComment();
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

    public void gotNew(int id) {
        Content content = AppCanvas.instance.content;
        if (content instanceof CommentContent) {
            CommentContent current = (CommentContent) content;

            synchronized (current.lock) {
                WallGetCommentResponse response = (WallGetCommentResponse) new WallGetComment(current.c.owner_id, id).execute();
                if (response != null && response.hasComment()) {
                    //int old = current.size();
                    boolean scrollTo = current.totalHeight - content.toScrollY - current.contentHeight <= AppCanvas.instance.normalEmojiFont.height * 2;
                    //boolean scrollTo = current.selectedY == old-1;
                    Comment msg = response.getComment();
                    current.add(current.getMessageItem(msg));
                    current.updateHeightsFromEnd(current.getWidth(), 1); //current.size() - old??

                    if (scrollTo && msg != null) {
                        current.scrollTo(current.size() - 1);
                    }
                }
            }
        }
    }

    public boolean isChat() {
        return c.isChat();
    }

    public boolean canWrite() {
        return c.canWrite();
    }

    public boolean isAdmin() {
        return c.hasChatSettings() && c.comments.can_close;
    }

    public void setThread(ImItem m) {
        synchronized (lock) {
            next = null;
            thread = (Comment) m;
            noNext = false;
            removeAll();
            loadNext();
        }
    }

    public long ownerId() {
        return c.owner_id;
    }

    public long id() {
        return c.post_id;
    }
    
    public ImField field() {
        return msgField;
    }

    public boolean isComments() {
        return true;
    }

    public void readTo(ImItem m) {
        
    }

    public int readedTo() {
        return 0;
    }

    public int outReadedTo() {
        return 0;
    }

    public int pinned() {
        return 0;
    }

    public void setPinned(ImItem m) {
        
    }
}
