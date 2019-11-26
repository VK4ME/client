package ru.curoviyxru.phoenix.ui;

import ru.curoviyxru.phoenix.ui.contents.ConversationList;
import ru.curoviyxru.phoenix.ui.contents.ContentController;
import ru.curoviyxru.phoenix.ui.contents.MessageContent;
import ru.curoviyxru.j2vk.PageStorage;
import ru.curoviyxru.j2vk.VKConstants;
import ru.curoviyxru.j2vk.api.objects.Conversation;
import ru.curoviyxru.j2vk.api.objects.Message;
import ru.curoviyxru.j2vk.api.objects.user.Page;
import ru.curoviyxru.j2vk.api.requests.messages.MessagesGetConversationsById;
import ru.curoviyxru.j2vk.api.responses.messages.MessagesGetConversationsByIdResponse;
import ru.curoviyxru.phoenix.Localization;
/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class ConversationItem extends ListItem {

    //private boolean pushEnabled = true;
    public Message msg;
    public Conversation conv;
    //private int online;
    public int typing;
    public String oldCornerIcon;
    public SuperString oldDescription;
    final Object sync = new Object();
    
    public ConversationItem setConversation(Conversation c) {
        if (c == null) {
            return this;
        }

        this.conv = c;
        if (c.unreadCount == 0 && ConversationList.instance != null) {
            ConversationList.instance.unread.removeElement(new Long(c.getId()));
            ContentController.updateMessagesItem();
        }
        if (c.isPage())
            PageStorage.load(c.getId());
        Page p = c.isPage() ? PageStorage.get(c.getId()) : null;
        //pushEnabled = c.hasPushSettings() ? !c.pushSettings.no_sound : true;

        /* if (icon == null) {
         setDescription("");
         setThumb(false);
         if (ConversationList.instance != null && ConversationList.instance.needsLoad != null) {
         ConversationList.instance.needsLoad.addElement(this);
         } //TODO Avatar loading
         } */
        setConver(c);
        if (c.getId() != VKConstants.account.getId()) {
            setCornerIcon(ContentController.getStatusIcon(p));
            setCaption(c.getTitle());
        } else {
            setCornerIcon(0);
            setCaption(Localization.get("element.favorites"));
        }
        setTimestamp(msg != null ? msg.getLastTime() : 0);
        setState((short) Math.min(9999, conv.unreadCount));
		if(c.hasPushSettings() && conv.unreadCount > 0)
			setMode(c.pushSettings.no_sound ? UNREAD_MUTED : UNREAD);
        
        //if(p != null && !p.isGroup) setCaptionColor(ContentController.getUserColor(p.getId()));
        
        return this;
    }

    public ConversationItem setMessage(Conversation cc, Message m) {
        if (cc == null || m == null) {
            return this;
        }
        
        msg = m;
        setTyping(0);
        setTimestamp(msg != null ? msg.getLastTime() : 0);
        setDescription(m.toString(true, cc.isPage(), true, true, true));
		
		boolean outUnread = m.out && m.id > cc.outRead;
        setMode(outUnread ? UNREAD_OUT : UNREAD);

        return this;
    }

    public ConversationItem(Content p, Conversation c, Message m) {
        super(null, ListItem.UNREAD);
        setFont(true);
        content = p;
        setMessage(c, m);
        setConversation(c);
    }

    public void actionPerformed() {
        ConversationList c = (ConversationList) content;
        MessageContent mc = getConversationContent(c.forward != null && c.forwardType != 0 && content.parent != null ? content.parent : content, conv.getId());
        if (c.forward != null && c.forwardType != 0) {
            switch (c.forwardType) {
                case 1:
                    mc.msgField.addForward(c.forward);
                    break;
                case 2:
                case 3:
                    mc.msgField.attachment = c.forward.firstElement();
                    mc.msgField.attachmentType = 4;
                    break;
            }
            c.forward(null, 0);
        }
        AppCanvas.instance.goTo(mc);
    }
    
    public static MessageContent getConversationContent(Content parent, final long id) {
        MessagesGetConversationsByIdResponse resp = (MessagesGetConversationsByIdResponse) new MessagesGetConversationsById().addPeerId(id).execute();
        if (resp == null || !resp.hasConversation()) {
            return null;
        }
		
        return (MessageContent) new MessageContent(resp.getConversation()).parent(parent);
    }
    
    public static MessageContent showConversation(Content parent, long id) {
        MessageContent msgC = getConversationContent(parent, id);
        AppCanvas.instance.goTo(msgC);
        return msgC;
    }

    public void setTyping(int i) {
        synchronized (sync) {
            typing = Math.max(i, 0);
            if (oldCornerIcon == null) {
                oldCornerIcon = cornerIcon;
                oldDescription = desc;
            }
            if (typing == 0) {
                if (cornerIcon != null && cornerIcon.equals(getCornerPath(7)))
                    cornerIcon = oldCornerIcon;
                oldCornerIcon = null;
                setDescription(oldDescription);
                oldDescription = null;
            } else {
                cornerIcon = getCornerPath(7);
                if (typing == 5) setDescription(Localization.get(!conv.isPage() ? "title.manyUsersTyping" : "title.userTyping", "").trim());
            }
        }
    }
    
    public void updateTyping() {
        if (typing > 0)
            setTyping(typing - 1);
    }
}
