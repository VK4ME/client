package ru.curoviyxru.phoenix.ui;

import java.util.Vector;
import ru.curoviyxru.phoenix.ui.contents.ContentController;
import ru.curoviyxru.phoenix.ui.contents.PageContent;
import ru.curoviyxru.phoenix.Theming;
import javax.microedition.lcdui.Graphics;
import ru.curoviyxru.j2vk.api.objects.attachments.Post;
import ru.curoviyxru.j2vk.api.requests.likes.LikesAdd;
import ru.curoviyxru.j2vk.api.requests.likes.LikesDelete;
import ru.curoviyxru.j2vk.api.requests.wall.WallRepost;
import ru.curoviyxru.j2vk.api.responses.likes.LikesAddResponse;
import ru.curoviyxru.j2vk.api.responses.likes.LikesDeleteResponse;
import ru.curoviyxru.j2vk.api.responses.wall.WallRepostResponse;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.PaneItem;
import ru.curoviyxru.phoenix.ui.PopupButton;
import ru.curoviyxru.phoenix.ui.PopupMenu;
import ru.curoviyxru.phoenix.ui.RenderUtil;
import ru.curoviyxru.phoenix.ui.contents.CommentContent;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class PostFooter extends PaneItem implements PaneItem.ItemThatUsesLeftAndRight, PaneItem.ItemThatUsesFullWidth {

    Post post;
    boolean liked, reposted;
    int selection = 0;
    String likeStr, repStr, viewStr, comStr;
    public static int[] images = new int[9];

    public PostFooter(Content content, Post post) {
        this.content = content;
        this.post = post;
        height = AppCanvas.instance.normalFont.getHeight() * 2;
        loadPostInfo();
    }

    public void loadPostInfo() {
        if (post == null) {
            return;
        }
        liked = post.likes == null ? false : post.likes.user_likes;
        likeStr = PageContent.count(post.likes == null ? 0 : post.likes.count);
        reposted = post.reposts == null ? false : post.reposts.user_reposted;
        viewStr = PageContent.count(post.views_count);
        repStr = PageContent.count(post.reposts == null ? 0 : post.reposts.count);
        comStr = PageContent.count(post.comments == null ? 0 : post.comments.count);
    }

    public PaneItem keyPressed(int key, int pX) {
        if (selection == -1) {
            selection = 0;
        } else if (selection == -2) {
            return this;
        }

        switch (key) {
            case AppCanvas.LEFT:
                if (selection < 1) {
                    break;
                }
                selection--;
                break;
            case AppCanvas.RIGHT:
                if (selection > 1) {
                    break;
                }
                selection++;
                break;
        }

        return this;
    }

    public PaneItem keyRepeated(int key, int pX) {
        keyPressed(key, pX);
        return this;
    }

    public PaneItem pointerPressed(int x, int y, int pY) {
        if (selection == -2) {
            return this;
        }
        selection = -1;
        super.pointerPressed(x, y, pY);

        if (pressed) {
            int posX = this.x;
            int oldX = this.x;

            //if (likeable || likeStr.length() > 0) {
                posX += AppCanvas.instance.normalFont.getHeight() / 2;
                posX += AppCanvas.instance.normalFont.getHeight() / 2 + AppCanvas.instance.normalFont.getHeight();
                posX += (likeStr.length() > 0 ? AppCanvas.instance.normalFont.getHeight() / 2 : 0) + AppCanvas.instance.normalFont.stringWidth(likeStr);
            //}

            if (x > oldX && x < posX) {
                selection = 0;
            }

            oldX = posX;

            //if (commentable || comStr.length() > 0) {
                posX += AppCanvas.instance.normalFont.getHeight() / 2;
                posX += AppCanvas.instance.normalFont.getHeight() / 2 + AppCanvas.instance.normalFont.getHeight();
                posX += (comStr.length() > 0 ? AppCanvas.instance.normalFont.getHeight() / 2 : 0) + AppCanvas.instance.normalFont.stringWidth(comStr);
            //}

            if (x > oldX && x < posX) {
                selection = 1;
            }

            oldX = posX;

            //if (repostable || repStr.length() > 0) {
                posX += AppCanvas.instance.normalFont.getHeight() / 2;
                posX += AppCanvas.instance.normalFont.getHeight() / 2 + AppCanvas.instance.normalFont.getHeight();
                posX += (repStr.length() > 0 ? AppCanvas.instance.normalFont.getHeight() / 2 : 0) + AppCanvas.instance.normalFont.stringWidth(repStr);
            //}

            if (x > oldX && x < posX) {
                selection = 2;
            }
        }

        return this;
    }

    public void actionPerformed() {
        actionPerformed(true);
    }

    public PaneItem keyReleased(int key, int scrollY) {
        if ((key == AppCanvas.FIRE || key == AppCanvas.ENTER) && pressed && focusable) {
            actionPerformed(false);
        }
        return this;
    }

    public void actionPerformed(boolean touch) {
        switch (selection) {
            case 0:
                if (liked) {
                    liked = false;
                    LikesDeleteResponse l = (LikesDeleteResponse) new LikesDelete(post).execute();

                    liked = l == null || l.likes == -1;
                    if (l != null) {
                        likeStr = PageContent.count(l.likes);
                    }
                } else {
                    liked = true;
                    LikesAddResponse l = (LikesAddResponse) new LikesAdd(post).execute();

                    liked = l != null && l.likes != -1;
                    if (l != null) {
                        likeStr = PageContent.count(l.likes);
                    }
                }
                break;
            case 1:
                if (!(AppCanvas.instance.content instanceof CommentContent))
                    ContentController.showComments(PostFooter.this.content, post, null);
                break;
            case 2:
                PopupMenu rMenu = new PopupMenu();
                
                rMenu.add(new PopupButton(Localization.get("action.repostToWall")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        if (!reposted) {
                            reposted = true;
                            WallRepostResponse l = (WallRepostResponse) new WallRepost().setItem(post).execute();

                            reposted = l != null && l.success;
                            if (l != null) {
                                repStr = PageContent.count(l.reposts_count);
                                likeStr = PageContent.count(l.likes_count);
                            }
                        }
                    }
                }.setIcon("new/account.rle"));
                rMenu.add(new PopupButton(Localization.get("action.repostToChat")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        Vector v = new Vector();
                        v.addElement(post);
                        ContentController.showConversations(PostFooter.this.content, v, 3);
                    }
                }.setIcon("new/share.rle"));
                
                AppCanvas.instance.showPopup(rMenu);
                break;
        }
        if (touch) {
            selection = 0;
        }
    }

    public void paint(Graphics g, int pY, int pX) {
        if (post == null) {
            return;
        }

        int posX = x + pX;
        int posY = y + pY + AppCanvas.instance.normalFont.getHeight() / 2;
        int selheight = AppCanvas.instance.normalFont.getHeight() + AppCanvas.instance.normalFont.getHeight() - 1;

		boolean likeFocused = pressed && selection == 0;
		int likeColor, likeColor_, likeTextColor;
		if(!liked) {
			likeColor = likeFocused ? Theming.now.focusedIconColor : Theming.now.iconColor;
			likeColor_ = likeFocused ? Theming.now.focusedIconColor_ : Theming.now.iconColor_;
			likeTextColor = likeFocused ? Theming.now.focusedCaptionColor : Theming.now.captionColor;
		} else {
			likeColor = likeFocused ? Theming.now.focusedLikeColor : Theming.now.likeColor;
			likeColor_ = likeFocused ? Theming.now.focusedLikeColor_ : Theming.now.likeColor_;
			likeTextColor = likeFocused ? Theming.now.focusedLikeTextColor : Theming.now.likeTextColor;
		}
		
        //selection = 0;
        //if (likeable || likeStr.length() > 0) {
            if (likeFocused) {
                RenderUtil.fillRect(g, posX, y + pY, AppCanvas.instance.normalFont.getHeight() / 2 * 2 + AppCanvas.instance.normalFont.getHeight() + (likeStr.length() > 0 ? AppCanvas.instance.normalFont.getHeight() / 2 : 0) + AppCanvas.instance.normalFont.stringWidth(likeStr), selheight, Theming.now.focusedBackgroundColor, Theming.now.focusedBackgroundColor_);
            }
            posX += AppCanvas.instance.normalFont.getHeight() / 2;

            PostFooter.images[(pressed && selection == 0 ? 1 : liked ? 2 : 0)] = 
                    Theming.now.drawImage(
                    g, 
                    posX, 
                    posY, 
                    PostFooter.images[(pressed && selection == 0 ? 1 : liked ? 2 : 0)], 
                    "new/heart.rle",
                    likeColor, 
                    likeColor_, 
                    AppCanvas.instance.normalFont.getHeight(), 
                    Graphics.TOP | Graphics.LEFT);
            
            g.setColor(likeTextColor);
            posX += AppCanvas.instance.normalFont.getHeight() / 2 + AppCanvas.instance.normalFont.getHeight();
            if (liked) {
                g.setFont(AppCanvas.instance.boldFont);
                g.drawString(likeStr, posX, posY, Graphics.TOP | Graphics.LEFT);
            } else {
                g.setFont(AppCanvas.instance.normalFont);
                g.drawString(likeStr, posX, posY, Graphics.TOP | Graphics.LEFT);
            }
            posX += (likeStr.length() > 0 ? AppCanvas.instance.normalFont.getHeight() / 2 : 0) + AppCanvas.instance.normalFont.stringWidth(likeStr);
        //}

        //selection = 1;
        //if (commentable || comStr.length() > 0) {
            if (pressed && selection == 1) {
                RenderUtil.fillRect(g, posX, y + pY, AppCanvas.instance.normalFont.getHeight() / 2 * 2 + AppCanvas.instance.normalFont.getHeight() + (comStr.length() > 0 ? AppCanvas.instance.normalFont.getHeight() / 2 : 0) + AppCanvas.instance.normalFont.stringWidth(comStr), selheight, Theming.now.focusedBackgroundColor, Theming.now.focusedBackgroundColor_);
            }
            posX += AppCanvas.instance.normalFont.getHeight() / 2;

            PostFooter.images[(pressed && selection == 1 ? 4 : 3)] = 
                    Theming.now.drawImage(
                    g, 
                    posX, 
                    posY, 
                    PostFooter.images[(pressed && selection == 1 ? 4 : 3)], 
                    "new/comment.rle",
                    pressed && selection == 1 ? Theming.now.focusedIconColor : Theming.now.iconColor, 
                    pressed && selection == 1 ? Theming.now.focusedIconColor_ : Theming.now.iconColor_, 
                    AppCanvas.instance.normalFont.getHeight(), 
                    Graphics.TOP | Graphics.LEFT);
            g.setColor(pressed && selection == 1 ? Theming.now.focusedIconColor : Theming.now.captionColor);
            posX += AppCanvas.instance.normalFont.getHeight() / 2 + AppCanvas.instance.normalFont.getHeight();
            g.setFont(AppCanvas.instance.normalFont);
            g.drawString(comStr, posX, posY, Graphics.TOP | Graphics.LEFT);
            posX += (comStr.length() > 0 ? AppCanvas.instance.normalFont.getHeight() / 2 : 0) + AppCanvas.instance.normalFont.stringWidth(comStr);
        //}

        //selection = 2;
        //if (repostable || repStr.length() > 0) {
            if (pressed && selection == 2) {
                RenderUtil.fillRect(g, posX, y + pY, AppCanvas.instance.normalFont.getHeight() / 2 * 2 + AppCanvas.instance.normalFont.getHeight() + (repStr.length() > 0 ? AppCanvas.instance.normalFont.getHeight() / 2 : 0) + AppCanvas.instance.normalFont.stringWidth(repStr), selheight, Theming.now.focusedBackgroundColor, Theming.now.focusedBackgroundColor_);
            }
            posX += AppCanvas.instance.normalFont.getHeight() / 2;

           
            PostFooter.images[(pressed && selection == 2 ? 6 : reposted ? 7 : 5)] = 
                    Theming.now.drawImage(
                    g, 
                    posX, 
                    posY, 
                    PostFooter.images[(pressed && selection == 2 ? 6 : reposted ? 7 : 5)], 
                    "new/share.rle",
                    pressed && selection == 2 ? Theming.now.focusedIconColor : Theming.now.iconColor, 
                    pressed && selection == 2 ? Theming.now.focusedIconColor_ : Theming.now.iconColor_, 
                    AppCanvas.instance.normalFont.getHeight(), 
                    Graphics.TOP | Graphics.LEFT);
            
            g.setColor(pressed && selection == 2 ? Theming.now.focusedIconColor : Theming.now.captionColor);
            posX += AppCanvas.instance.normalFont.getHeight() / 2 + AppCanvas.instance.normalFont.getHeight();
            if (reposted) {
                g.setFont(AppCanvas.instance.boldFont);
                g.drawString(repStr, posX, posY, Graphics.TOP | Graphics.LEFT);
            } else {
                g.setFont(AppCanvas.instance.normalFont);
                g.drawString(repStr, posX, posY, Graphics.TOP | Graphics.LEFT);
            }
            posX += (repStr.length() > 0 ? AppCanvas.instance.normalFont.getHeight() / 2 : 0) + AppCanvas.instance.normalFont.stringWidth(repStr);
        //}

        if (posX > x + pX + width - AppCanvas.instance.normalFont.getHeight() / 2 || viewStr.length() < 1) {
            return;
        }
        g.setColor(Theming.now.captionColor);
        g.setFont(AppCanvas.instance.normalFont);
        g.drawString(viewStr, x + pX + width - AppCanvas.instance.normalFont.getHeight() / 2, posY, Graphics.TOP | Graphics.RIGHT);
        PostFooter.images[8] = 
                Theming.now.drawImage(g,
                x + pX + width - AppCanvas.instance.normalFont.getHeight() / 2 * 2 - AppCanvas.instance.normalFont.stringWidth(viewStr), 
                posY, 
                PostFooter.images[8],
                "new/eye.rle",
                Theming.now.iconColor,
                Theming.now.iconColor_,
                AppCanvas.instance.normalEmojiFont.height,
                Graphics.RIGHT | Graphics.TOP);
    }

    public void updateHeight() {
    }
}
