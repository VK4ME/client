package ru.curoviyxru.phoenix.ui.im;

import java.util.Vector;
import ru.curoviyxru.phoenix.Theming;
import javax.microedition.io.Connector;
import javax.microedition.lcdui.Graphics;
import ru.curoviyxru.j2vk.LongPoll;
import ru.curoviyxru.j2vk.ProgressProvider;
import ru.curoviyxru.j2vk.TextUtil;
import ru.curoviyxru.j2vk.Uploader;
import ru.curoviyxru.j2vk.api.objects.Attachment;
import ru.curoviyxru.j2vk.api.objects.ImItem;
import ru.curoviyxru.j2vk.api.requests.messages.MessagesEdit;
import ru.curoviyxru.j2vk.api.requests.messages.MessagesSend;
import ru.curoviyxru.j2vk.api.requests.messages.MessagesSetActivity;
import ru.curoviyxru.j2vk.api.requests.wall.WallCreateComment;
import ru.curoviyxru.j2vk.api.requests.wall.WallEditComment;
import ru.curoviyxru.j2vk.api.responses.messages.MessagesEditResponse;
import ru.curoviyxru.j2vk.api.responses.messages.MessagesSendResponse;
import ru.curoviyxru.j2vk.api.responses.wall.WallCreateCommentResponse;
import ru.curoviyxru.j2vk.api.responses.wall.WallEditCommentResponse;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.kernel.FocusedProgressProvider;
import ru.curoviyxru.phoenix.kernel.ProgressKernel;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.Field;
import ru.curoviyxru.phoenix.ui.FilePicker;
import ru.curoviyxru.phoenix.ui.KeyboardUtil;
import ru.curoviyxru.phoenix.ui.PaneItem;
import ru.curoviyxru.phoenix.ui.PopupButton;
import ru.curoviyxru.phoenix.ui.PopupMenu;
import ru.curoviyxru.phoenix.ui.ProgressBar;
import ru.curoviyxru.phoenix.ui.RenderUtil;
import ru.curoviyxru.phoenix.ui.SuperString;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class ImField extends Field implements PaneItem.ItemThatUsesLeftAndRight, ProgressProvider {

    public boolean opened;
    public SuperString[] textLines;
    static int paperclipIcon, paperclipIconSelected, deleteIcon, deleteIconSelected, sendIcon, sendIconSelected;
    int sel = -1;
    public static int heightNormal = AppCanvas.instance.normalEmojiFont.height * 5 / 2,
            fieldHeight = AppCanvas.instance.normalEmojiFont.height * 2,
            buttonWidth = heightNormal, iconSize = heightNormal - AppCanvas.instance.normalEmojiFont.height * 5 / 4;
    long typingLong;
    boolean typing;
    PopupMenu attachmentMenu;
    public Object attachment;
    public int attachmentType; //1 - edit, 2 - reply, 3 - fwds, 4 - att
    
    ProgressBar bar;

    public ImField(ImContent ccc) {
        super();
        setCaption(Localization.get("action.writeMessageField"));
        opened = Midlet.instance.config.showCmtFieldByOpen && ccc.canWrite();
        height = 0;
        content = (Content) ccc;
    }

    public PaneItem keyPressed(int key, int o) {
        if (key == AppCanvas.LEFT) {
            sel = 0;
        } else if (key == AppCanvas.RIGHT) {
            sel = 2;
        } else {
//            if (key == AppCanvas.FIRE) {
//                key = AppCanvas.ENTER;
//            }
            KeyboardUtil.keyPressed(key, this);
            if (key != AppCanvas.FIRE && key != AppCanvas.ENTER) {
                typing = true;
            }
        }

        return this;
    }
    
    public void updateTyping() {
        if (((ImContent) content).isComments()) {
            return;
        }

        if (typing && !Midlet.instance.config.DNT && System.currentTimeMillis() - typingLong > 4000) {
            typing = false;
            typingLong = System.currentTimeMillis();
            new MessagesSetActivity(((ImContent) content).id(), MessagesSetActivity.TYPING).execute();
        }
    }

    public PaneItem keyRepeated(int key, int o) {
        if (key != AppCanvas.LEFT && key != AppCanvas.RIGHT) {
//            if (key == AppCanvas.FIRE) {
//                key = AppCanvas.ENTER;
//            }
            KeyboardUtil.keyRepeated(key, this);
            if (key != AppCanvas.FIRE && key != AppCanvas.ENTER) {
                typing = true;
            }
        }

        return this;
    }

    public PaneItem keyReleased(int key, int o) {
        if (key == AppCanvas.LEFT) {
            openAttachmentsList();
            sel = -1;
        } else if (key == AppCanvas.RIGHT) {
            sendMessage();
            sel = -1;
        }

        return this;
    }

    public PaneItem pointerPressed(int x, int y, int p) {
        sel = selectedButton(x, y);

        return this;
    }

    public PaneItem pointerDragged(int x, int y, int p) {
        return this;
    }

    public PaneItem pointerReleased(int x, int y, int p) {
        if (sel != -1 && sel == selectedButton(x, y)) {
            if (sel == 0) {
                openAttachmentsList();
            } else if (sel == 1) {
                super.actionPerformed();
            } else {
                sendMessage();
            }
        }

        sel = -1;
        return this;
    }

    public int selectedButton(int x, int y) {
        if (opened && y < this.y + heightNormal && y >= this.y) {
            if (x >= this.x && x < this.x + buttonWidth) {
                return 0;
            } else if (y >= this.y + (heightNormal - fieldHeight) / 2
                    && y < this.y + heightNormal - (heightNormal - fieldHeight) / 2
                    && x <= this.x - buttonWidth + width && x >= this.x + buttonWidth) {
                return 1;
            } else if (x <= this.x + width && x > this.x - buttonWidth + width) {
                return 2;
            }
        }

        return -1;
    }

    public void setText(String text) {
        this.text = text == null ? null : new SuperString(text);
        textLines = null;
    }

    public void setText(SuperString text) {
        this.text = text;
        textLines = null;
    }
    
    public void paint(Graphics g, int pY, int pX) {
        if (!((ImContent) content).canWrite()) {
            return;
        }
        updateHeight();

        if (!opened) {
            return;
        }

        g.setColor(Theming.now.msgFieldBackgroundColor);
        g.fillRect(x + pX, y + pY, width, heightNormal);

        //Left button
        RenderUtil.fillRect(g, x + pX, y + pY, buttonWidth, heightNormal, sel == 0 ? Theming.now.focusedBackgroundColor : Theming.now.msgFieldBackgroundColor, sel == 0 ? Theming.now.focusedBackgroundColor_ : Theming.now.msgFieldBackgroundColor);

        //Right button
        RenderUtil.fillRect(g, x + pX + width - buttonWidth, y + pY, buttonWidth, heightNormal, sel == 2 ? Theming.now.focusedBackgroundColor : Theming.now.msgFieldBackgroundColor, sel == 2 ? Theming.now.focusedBackgroundColor_ : Theming.now.msgFieldBackgroundColor);

        g.setColor(Theming.now.itemSeparatorColor);
        g.fillRect(x + pX, y + pY, width, 1);

        boolean haveAttachment = attachment != null && attachmentType != 0;

        int id1 = Theming.now.drawImage(g, x + pX + buttonWidth / 2, y + pY + heightNormal / 2,
                haveAttachment ? sel == 0 ? paperclipIconSelected : paperclipIcon : sel == 0 ? deleteIconSelected : deleteIcon, haveAttachment ? "new/close-circle-outline.rle" : "new/paperclip.rle",
                sel == 0 ? Theming.now.focusedIconColor : Theming.now.iconColor,
                sel == 0 ? Theming.now.focusedIconColor : Theming.now.iconColor,
                iconSize,
                Graphics.VCENTER | Graphics.HCENTER);
        if (haveAttachment) {
            if (sel == 0) {
                paperclipIconSelected = id1;
            } else {
                paperclipIcon = id1;
            }
        } else {
            if (sel == 0) {
                deleteIconSelected = id1;
            } else {
                deleteIcon = id1;
            }
        }

        int id = Theming.now.drawImage(g, x + pX + width - buttonWidth / 2, y + pY + heightNormal - heightNormal / 2,
                sel == 2 ? sendIconSelected : sendIcon, "new/send.rle",
                sel == 2 ? Theming.now.focusedIconColor : Theming.now.iconColor,
                sel == 2 ? Theming.now.focusedIconColor_ : Theming.now.iconColor_,
                iconSize,
                Graphics.VCENTER | Graphics.HCENTER);

        if (sel == 2) {
            sendIconSelected = id;
        } else {
            sendIcon = id;
        }

        int fieldWidth = width - buttonWidth * 2 - AppCanvas.instance.perLineSpace * 2;

        if (width > 0) {
            if (text != null && text.length != 0 && textLines == null) {
                textLines = AppCanvas.instance.normalEmojiFont.multiline(text, fieldWidth);
            }
            if (caption != null && renderCaption == null) {
                renderCaption = AppCanvas.instance.normalEmojiFont.limit(caption, fieldWidth, true);
            }
        }

        int fnHeight = AppCanvas.instance.normalEmojiFont.height;

        if (textLines != null) {
            g.setColor(Theming.now.textColor);

            int yy = y + pY + heightNormal / 2 - fnHeight / 2;
            if (textLines.length >= 2) {
                yy -= (fnHeight + AppCanvas.instance.perLineSpace) / 2;
            }

            for (int i = Math.max(0, textLines.length - 2); i < textLines.length; i++) {

                AppCanvas.instance.normalEmojiFont.drawString(g, textLines[i],
                        x + pX + (width - fieldWidth) / 2,
                        yy, Graphics.TOP | Graphics.LEFT);

                yy += AppCanvas.instance.normalEmojiFont.height;
            }

        } else if (renderCaption != null) {
            g.setColor(Theming.now.captionColor);

            AppCanvas.instance.normalEmojiFont.drawString(g, renderCaption,
                    x + pX + (width - fieldWidth) / 2,
                    y + pY + heightNormal / 2 - fnHeight / 2, Graphics.TOP | Graphics.LEFT);
        }
        
        if (bar != null) {
            bar.width = width;
            bar.paint(g, y + pY + height - bar.height, x + pX);
        }
    }

    private void openAttachmentsList() {
        if (!((ImContent) content).canWrite()) {
            return;
        }

        boolean haveAttachment = attachment != null && attachmentType != 0;

        if (haveAttachment) {
            resetAttachment();
            ProgressKernel.deleteProvider(getName());
            bar = null;
        } else {
            if (attachmentMenu == null) {
                attachmentMenu = new PopupMenu();
                attachmentMenu.add(new PopupButton(Localization.get("action.addPhoto")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        AppCanvas.instance.goTo(new FilePicker(false, ImField.this.content) {
                            public void filePicked(final String path) {
                                attachmentType = 4;
                                attachment = System.currentTimeMillis() + "";
                                AppCanvas.instance.backTo(ImField.this.content);
                                final FocusedProgressProvider pp = new FocusedProgressProvider(ImField.this);
                                ProgressKernel.addProvider(pp);
                                new Thread() {
                                    public void run() {
                                        try {
                                            javax.microedition.io.file.FileConnection conn = (javax.microedition.io.file.FileConnection) Connector.open(path, Connector.READ);
                                            Attachment ph = Uploader.uploadMessagePhoto(((ImContent) ImField.this.content).id(), conn.getName(), conn.fileSize(), conn.openInputStream(), pp);
                                            if (ph == null) {
                                                resetAttachment();
                                            } else {
                                                attachment = ph;
                                            }
                                            conn.close();
                                        } catch (Exception e) {
                                            AppCanvas.instance.dropError(e);
                                        }
                                    }
                                }.start();
                            }
                        });
                    }
                }.setIcon("new/image-plus.rle"));
                attachmentMenu.add(new PopupButton(Localization.get("action.addDocument")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        AppCanvas.instance.goTo(new FilePicker(false, ImField.this.content) {
                            public void filePicked(final String path) {
                                attachmentType = 4;
                                attachment = System.currentTimeMillis() + "";
                                AppCanvas.instance.backTo(ImField.this.content);
                                final FocusedProgressProvider pp = new FocusedProgressProvider(ImField.this);
                                ProgressKernel.addProvider(pp);
                                new Thread() {
                                    public void run() {
                                        try {
                                            javax.microedition.io.file.FileConnection conn = (javax.microedition.io.file.FileConnection) Connector.open(path, Connector.READ);
                                            Attachment ph = Uploader.uploadMessageDocument(((ImContent) ImField.this.content).id(), conn.getName(), conn.fileSize(), conn.openInputStream(), pp);
                                            if (ph == null) {
                                                resetAttachment();
                                            } else {
                                                attachment = ph;
                                            }
                                            conn.close();
                                        } catch (Exception e) {
                                            AppCanvas.instance.dropError(e);
                                        }
                                    }
                                }.start();
                            }
                        });
                    }
                }.setIcon("new/file-plus.rle"));
            }
            AppCanvas.instance.showPopup(attachmentMenu);
        }
    }

    public void setOpened(boolean b) {
        this.opened = b;
        if (!((ImContent) content).canWrite()) {
            this.opened = false;
        }

        AppCanvas.instance.showScroll();
        if (content.totalHeight > content.contentHeight) {
            content.toScrollY += Math.min(content.totalHeight - content.contentHeight, heightNormal) * (opened ? 1 : -1);
        }
    }
    
    private void sendMessage() {
        if (((ImContent) content).isComments()) {
            sendCommentInternal();
            return;
        }
        
        sendMessageInternal();
    }

    private void sendMessageInternal() {
        if (!((ImContent) content).canWrite()) {
            return;
        }
        if (!opened) {
            return;
        }

        if (attachmentType != 1) {
            if (TextUtil.isNullOrEmpty(getText()) && (attachmentType == 0 || attachment == null || attachment instanceof String)) {
                return;
            }
            MessagesSend request = new MessagesSend(((ImContent) content).id(), getText());
            if (attachmentType == 2) {
                request.setReply(((ImItem) attachment).id());
            } else if (attachmentType == 3) {
                request.setForwardedMessages(((Vector) attachment));
            } else if (attachmentType == 4) {
                request.setAttachment(((Attachment) attachment).toString());
            }
            MessagesSendResponse rr = (MessagesSendResponse) request.execute();
            LongPoll.skipWait = true;
            if (rr == null || !rr.isSuccessful()) {
                AppCanvas.instance.dropError(Localization.get("element.sendError"));
            } else {
                if (Midlet.instance.config.hideAfterSending) setOpened(false);
                KeyboardUtil.messageVibrate();
                resetAttachment();
                setText("");
            }
        } else if (!TextUtil.isNullOrEmpty(getText())) {
            String buf = null;
            if (((ImItem) attachment).hasAttachments()) {
                buf = "";
                for (int i = 0; i < ((ImItem) attachment).attachments().length; ++i) {
                    if (((ImItem) attachment).attachments()[i] != null) {
                        buf += ((ImItem) attachment).attachments()[i].toString();
                        if (i != ((ImItem) attachment).attachments().length - 1) {
                            buf += ",";
                        }
                    }
                }
            }
            MessagesEditResponse rr = (MessagesEditResponse) new MessagesEdit(((ImContent) content).id(), ((ImItem) attachment).id(), getText()).setAttachment(buf).execute();
            LongPoll.skipWait = true;
            if (rr == null || !rr.hasEdited(((ImItem) attachment).id())) {
                AppCanvas.instance.dropError(Localization.get("element.editError"));
            } else {
                if (Midlet.instance.config.hideAfterSending) setOpened(false);
                KeyboardUtil.messageVibrate();
                resetAttachment();
                setText("");
            }
        }
    }
    
    private void sendCommentInternal() {
        if (!((ImContent) content).canWrite()) {
            return;
        }
        if (!opened) {
            return;
        }

        if (attachmentType != 1) {
            if (TextUtil.isNullOrEmpty(getText()) && (attachmentType == 0 || attachment == null || attachment instanceof String)) {
                return;
            }
            WallCreateComment request = new WallCreateComment(((ImContent) content).ownerId(), (int) ((ImContent) content).id(), getText());
            if (attachmentType == 2) {
                request.setReply(((ImItem) attachment).id());
            } else if (attachmentType == 4) {
                request.setAttachment(((Attachment) attachment).toString());
            }
            WallCreateCommentResponse rr = (WallCreateCommentResponse) request.execute();
			if (!((ImContent) content).isComments()) LongPoll.skipWait = true;
            if (rr == null || !rr.isSuccessful()) {
                AppCanvas.instance.dropError(Localization.get("element.sendError"));
            } else {
                if (Midlet.instance.config.hideAfterSending) setOpened(false);
                ((ImContent) content).gotNew(rr.commentId);
                KeyboardUtil.messageVibrate();
                resetAttachment();
                setText("");
            }
        } else if (!TextUtil.isNullOrEmpty(getText())) {
            String buf = null;
            if (((ImItem) attachment).hasAttachments()) {
                buf = "";
                for (int i = 0; i < ((ImItem) attachment).attachments().length; ++i) {
                    if (((ImItem) attachment).attachments()[i] != null) {
                        buf += ((ImItem) attachment).attachments()[i].toString();
                        if (i != ((ImItem) attachment).attachments().length - 1) {
                            buf += ",";
                        }
                    }
                }
            }
            WallEditCommentResponse rr = (WallEditCommentResponse) new WallEditComment(((ImContent) content).ownerId(), ((ImItem) attachment).id(), getText()).setAttachment(buf).execute();
            if (!((ImContent) content).isComments()) LongPoll.skipWait = true;
            if (rr == null || !rr.hasEdited(((ImItem) attachment).id())) {
                AppCanvas.instance.dropError(Localization.get("element.editError"));
            } else {
                if (Midlet.instance.config.hideAfterSending) setOpened(false);
                ((ImContent) content).gotEdit(((ImItem) attachment).id());
                KeyboardUtil.messageVibrate();
                resetAttachment();
                setText("");
            }
        }
    }

    public void updateHeight() {
        if (!((ImContent) content).canWrite()) {
            height = 0;
        }
        height = opened ? heightNormal : 0;
    }

    public void edit(ImItem m) {
        if (!((ImContent) content).canWrite()) {
            return;
        }
        if (m != null && !m.canEdDel()) {
            return;
        }
        resetAttachment();
        setText("");
        if (m == null) {
            return;
        }

        attachmentType = 1;
        attachment = m;
        setCaption(Localization.get("action.editMessageField"));
        setText(m.text());
        setOpened(true);
    }
    
    public void addForward(Vector a) {
        if (!((ImContent) content).canWrite()) {
            return;
        }
        if (attachmentType != 3) {
            resetAttachment();
        }
        if (a == null) {
            return;
        }

        if (attachment == null) {
            attachment = new Vector();
            attachmentType = 3;
        }

        ((Vector) attachment).ensureCapacity(((Vector) attachment).size() + a.size());
        for (int i = 0; i < a.size(); ++i) {
            if (!((Vector) attachment).contains(a.elementAt(i))) ((Vector) attachment).addElement(a.elementAt(i));
        }
    }
    
    public void addForward(ImItem m) {
        if (!((ImContent) content).canWrite()) {
            return;
        }
        if (attachmentType != 3) {
            resetAttachment();
        }
        if (m == null) {
            return;
        }

        if (attachment == null) {
            attachment = new Vector();
            attachmentType = 3;
        }
        
        if (!((Vector) attachment).contains(m)) ((Vector) attachment).addElement(m);
    }
    
    public void reply(ImItem m) {
        if (!((ImContent) content).canWrite()) {
            return;
        }
        resetAttachment();
        if (m == null) {
            return;
        }

        attachmentType = 2;
        attachment = m;
        setOpened(true);
    }

    public void setOpened() {
        setOpened(!opened);
    }

    public void resetAttachment() {
        attachmentType = 0;
        attachment = null;
        setCaption(Localization.get("action.writeMessageField"));
    }

    public void setProgress(long i) {
        if (bar == null) {
            bar = (ProgressBar) new ProgressBar().setFocusable(true);
        }
        if (bar != null) {
            bar.setProgress(i);
        }
        content.renderIfNeeded();
    }

    public void failed(String s) {
        bar = null;
        resetAttachment();
        content.renderIfNeeded();
    }

    public void successful() {
        bar = null;
        content.renderIfNeeded();
    }

    public String getName() {
        return attachment == null ? null : (String) attachment;
    }
}
