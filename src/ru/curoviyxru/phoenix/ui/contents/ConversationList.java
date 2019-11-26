package ru.curoviyxru.phoenix.ui.contents;

import java.util.Vector;
import javax.microedition.lcdui.Display;
import org.json.me.JSONArray;
import ru.curoviyxru.j2vk.api.objects.Conversation;
import ru.curoviyxru.j2vk.api.objects.Message;
import ru.curoviyxru.j2vk.api.requests.messages.MessagesGetConversations;
import ru.curoviyxru.j2vk.api.requests.messages.MessagesGetConversationsById;
import ru.curoviyxru.j2vk.api.requests.messages.MessagesGetHistory;
import ru.curoviyxru.j2vk.api.responses.messages.MessagesGetConversationsByIdResponse;
import ru.curoviyxru.j2vk.api.responses.messages.MessagesGetConversationsResponse;
import ru.curoviyxru.j2vk.api.responses.messages.MessagesGetConversationsResponse.ConversationResponseItem;
import ru.curoviyxru.j2vk.api.responses.messages.MessagesGetHistoryResponse;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.ListItem;
import ru.curoviyxru.phoenix.ui.PaneItem;
import ru.curoviyxru.phoenix.ui.ConversationItem;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
//todo slim style setting
public class ConversationList extends ScrollContent {

    public static ConversationList instance; //TODO remove this caching
    public Vector forward, unread;
    public int forwardType;

    public static void gotNew(boolean out, long peer_id) {
        if (instance == null) {
            return;
        }

        synchronized (instance.lock) {
            ConversationItem i = instance.get(peer_id, true);
            if (i == null) {
                return;
            }

            if (!out) {
                if (i.mode != ListItem.UNREAD_MUTED) {
                    if (Midlet.instance.config.feed_notVibro) {
                        Display.getDisplay(Midlet.instance).vibrate(Midlet.instance.config.notVibroTime);
                    }
                    if (Midlet.instance.config.feed_notSound) {
                        AppCanvas.playIncomingSound();
                    }
                }

                Long l = new Long(peer_id);
                if (!instance.unread.contains(l)) {
                    instance.unread.addElement(l);
                }
                ContentController.updateMessagesItem();
            }

            update(i);
            up(i);
        }

        instance.renderIfNeeded();
    }

    private static void update(ConversationItem i) {
        MessagesGetConversationsByIdResponse resp = (MessagesGetConversationsByIdResponse) new MessagesGetConversationsById(i.conv.getId()).execute();
        Conversation cc = resp != null ? resp.getConversation() : null;
        MessagesGetHistoryResponse respM = (MessagesGetHistoryResponse) new MessagesGetHistory(i.conv.getId(), 1).execute();
        if (respM != null && respM.hasItems()) {
            i.setMessage(cc != null ? cc : i.conv, respM.items[0]);
        }
        if (cc != null) {
            i.setConversation(cc);
        }
    }

    private static void up(ConversationItem i) {
        instance.remove(i);
        instance.insert(i, 0);
        //instance.updateHeights(instance.getWidth(), 1);
    }

    public static void gotRead(long peer_id) {
        justUpdateConv(peer_id);
    }

    public static void updateOnline(long peer_id) {
        justUpdateConv(peer_id);
    }

    public static void gotDelete(long peer_id) {
        justUpdateConv(peer_id);
    }

    public static void gotEdit(long peer_id) {
        justUpdateConv(peer_id);
    }

    private static void justUpdateConv(long peer_id) {
        if (instance == null) {
            return;
        }

        synchronized (instance.lock) {
            ConversationItem i = instance.get(peer_id, false);
            if (i == null) {
                return;
            }

            update(i);
        }

        instance.renderIfNeeded();
    }

    public ConversationList() {
        super(Localization.get("title.messaging"), false);
        unread = new Vector();
        instance = this;
        refresh();
    }

    public void refresh() {
        synchronized (instance.lock) {
            unread.removeAllElements();
            MessagesGetConversationsResponse respM = (MessagesGetConversationsResponse) new MessagesGetConversations().setCount(0).execute();
            if (respM != null) {
                if (respM.unreadCount != 0) {
                    MessagesGetConversationsResponse resp = (MessagesGetConversationsResponse) new MessagesGetConversations().setCount(respM.unreadCount).setFilter("unread").execute();
                    if (resp != null && resp.hasItems()) {
                        for (int i = 0; i < resp.items.length; i++) {
                            ConversationResponseItem it = resp.items[i];
                            if (it != null && it.hasConversation()) {
                                Long p = new Long(it.conversation.getId());
                                if (!unread.contains(p)) {
                                    unread.addElement(p);
                                }
                            }
                        }
                    }
                }
            }
            ContentController.updateMessagesItem();
        }
        super.refresh();
    }

    public void forward(Vector forward, int type) {
        this.forward = forward;
        this.forwardType = type;
        updateTitle();
        parent(ContentController.menu);
    }

    public void updateTitle() {
        String counter = PageContent.count(Math.min(9999, unread.size()));
        setTitle(forward != null && forwardType != 0 ? Localization.get("title.forwardTo") : unread.size() > 0 ? Localization.get("title.messagingWithCount", counter) : Localization.get("title.messaging"));
    }

    public ConversationItem get(long id, boolean create) {
        for (int i = 0; i < size(); i++) {
            PaneItem item = (PaneItem) at(i);
            if (item instanceof ConversationItem) {
                ConversationItem ci = (ConversationItem) item;
                if (ci.conv.getId() == id) {
                    return ci;
                }
            }
        }

        return create ? create(id) : null;
    }

    public void process() {
        AppCanvas i = AppCanvas.instance;

        i.setProgress(true);
        boolean empty = next == null;
        if (empty) {
            next = new Integer(-5);
        }

        next = new Integer(((Integer) next).intValue() + 5);
        final MessagesGetConversations req = new MessagesGetConversations().setCount(5).setOffset(((Integer) next).intValue());
        if (!empty) {
            req.setStartMessageId(((Integer) addon).intValue());
        } else {
            addon = new Integer(Integer.MIN_VALUE);
        }
        final MessagesGetConversationsResponse rr = (MessagesGetConversationsResponse) req.execute();
        if (rr != null && rr.unreadCount > 0) {
            //AppCanvas.newMessage(true);
        }
        if (rr != null && rr.hasItems()) {
            if (rr.items.length < 5) {
                noNext = true;
            }

            for (int i1 = 0; i1 < rr.items.length; i1++) {
                ConversationResponseItem item = rr.items[i1];
                final Conversation c = item.conversation;
                Message m = item.lastMessage;
                if (c == null || m == null) {
                    continue;
                }
                if (empty && ((Integer) addon).intValue() < m.id) {
                    addon = new Integer(m.id);
                }

                add(new ConversationItem(this, c, m));
            }
        } else {
            noNext = true;
            if (rr == null) {
                AppCanvas.instance.dropError(Localization.get("general.loadError"));
            }
        }

        updateHeightsFromEnd(getWidth(), 5);

        i.setProgress(false);
    }

    private ConversationItem create(long id) {
        MessagesGetConversationsByIdResponse resp = (MessagesGetConversationsByIdResponse) new MessagesGetConversationsById(id).execute();
        if (resp != null && !resp.hasConversation()) {
            return null;
        }
        MessagesGetHistoryResponse respM = (MessagesGetHistoryResponse) new MessagesGetHistory(id, 1).execute();
        ConversationItem item = new ConversationItem(this, resp.getConversation(), respM != null && respM.hasItems() ? respM.items[0] : null);
        insert(item, 0);
        //updateHeights(getWidth(), 1);
        return item;
    }

    public void updateTypingUser(long peer_id, long uid) {
        if (!Midlet.instance.config.typingInDialogs) {
            return;
        }

        ConversationItem i = get(peer_id, false);
        if (i == null) {
            return;
        }

        i.setTyping(5);
        renderIfNeeded();
    }

    public void updateTypingUsers(JSONArray optJSONArray, long peer_id, int ts) {
        if (!Midlet.instance.config.typingInDialogs) {
            return;
        }

        ConversationItem i = get(peer_id, false);
        if (i == null) {
            return;
        }

        i.setTyping(5);
        renderIfNeeded();
    }

    public void updateTyping() {
        if (!Midlet.instance.config.typingInDialogs) {
            return;
        }

        for (int i = 0; i < size(); ++i) {
            ConversationItem item = (ConversationItem) at(i);
            if (item == null) {
                continue;
            }

            item.updateTyping();
        }
        renderIfNeeded();
    }
}
