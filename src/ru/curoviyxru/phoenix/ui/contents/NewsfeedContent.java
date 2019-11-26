package ru.curoviyxru.phoenix.ui.contents;

import javax.microedition.lcdui.Graphics;
import ru.curoviyxru.j2vk.PageStorage;
import ru.curoviyxru.j2vk.api.objects.Attachment;
import ru.curoviyxru.j2vk.api.objects.attachments.Post;
import ru.curoviyxru.j2vk.api.objects.attachments.ImageAttachment;
import ru.curoviyxru.j2vk.api.objects.attachments.Photo;
import ru.curoviyxru.j2vk.api.requests.newsfeed.NewsfeedGet;
import ru.curoviyxru.j2vk.api.responses.newsfeed.NewsfeedGetResponse;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.AttachmentView;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.Footer;
import ru.curoviyxru.phoenix.ui.ImageItem;
import ru.curoviyxru.phoenix.ui.Label;
import ru.curoviyxru.phoenix.ui.ListItem;
import ru.curoviyxru.phoenix.ui.PaneItem;
import ru.curoviyxru.phoenix.ui.PostFooter;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class NewsfeedContent extends ScrollContent implements PostContent {

    public NewsfeedContent() {
        super(Localization.get("title.newsfeed"), true);
    }

    public void process() {
        AppCanvas.instance.setProgress(true);

        final NewsfeedGetResponse rr = (NewsfeedGetResponse) new NewsfeedGet().setCount(5).setStartFrom(next == null ? null : next.toString()).execute();
        if (rr != null && rr.hasItems()) {
            if (rr.items.length < 5 || rr.next_from == null || (next != null && next.equals(rr.next_from))) {
                noNext = true;
                AppCanvas.instance.setProgress(false);
                if (rr.next_from != null && (next != null && next.equals(rr.next_from))) return;
            }
            next = rr.next_from;

            for (int i1 = 0; i1 < rr.items.length; i1++) {
                NewsfeedContent.addPost(this, rr.items[i1]);
            }

//            new Thread() {
//                public void run() {
//                    for (int i = 0; i < v.size(); i++) {
//                        try {
//                            ((ListItem) v.elementAt(i)).setPage(((Page) ((ListItem) v.elementAt(i)).linked).getId());
//                            AppCanvas.instance.render();
//
//                            //System.gc();
//                        } catch (Exception e) {
//                        }
//                    }
//                }
//            }.start();
        } else {
            noNext = true;
            if (rr == null) {
                AppCanvas.instance.dropError(Localization.get("general.loadError"));
            }
        }

        AppCanvas.instance.setProgress(false);
    }

    //TODO advanced elements like messages
    public static void addPost(PostContent target, Post post) {
        if (post == null) {
            return;
        }

        target.addPostItem((ListItem) ContentController.getUserButton(
                PageStorage.get(post.owner_id), false, true, true, true, (Content) target)
                .setDescription(PageContent.getTimeActionString(post.date)));
        if (post.from_id != post.owner_id && post.from_id != 0) target.addPostItem((ListItem) ContentController.getUserButton(
                PageStorage.get(post.from_id), true, true, false, true, (Content) target)
                .setDescription(PageContent.getTimeActionString(post.date)));

        if (post.isAd()) {
            target.addPostItem(new Label(Localization.get("element.advertisement")).setFont(true));
        }

        if (post.hasText()) { //TODO short text / expand text
            target.addPostItem(new Label(post.text));
        }
        if (post.hasAttachments()) {
            for (int i = 0; i < post.attachments.length; i++) {
                Attachment a = post.attachments[i];
                if (a == null) {
                    continue;
                }

                if (a instanceof ImageAttachment) {
                    int rS = Math.min(AppCanvas.instance.lW, AppCanvas.instance.lH) - (AppCanvas.instance.perLineSpace * 4);
                    int fW = ((ImageAttachment) a).getWidth(rS);
                    target.addPostItem(new ImageItem(a instanceof Photo ? (Photo) a : null, ((ImageAttachment) a).getURL(rS), false, Math.min(rS, fW), Math.min(rS, fW) * ((ImageAttachment) a).getHeight(rS) / fW).setAlign(Graphics.HCENTER));
                } else {
                    target.addPostItem(new AttachmentView(a));
                }
            }
        }

        Post forFooter = post;
        if (post.hasCopyHistory()) {
            post = post.copy_history[0];
            if (post != null) {
                target.addPostItem(new AttachmentView(post));
            }
        }
        post = forFooter;
        if (post.hasSigner() && post.signer_id != post.owner_id && post.signer_id != post.from_id) {
            target.addPostItem((ListItem) ContentController.getUserButton(PageStorage.get(post.signer_id), true, true, false, true, (Content) target));
        }

        target.addPostItem(new PostFooter((Content) target, post));
        target.addPostItem(new Footer());
    }

    public void addPostItem(PaneItem i) {
        add(i);
    }
}
