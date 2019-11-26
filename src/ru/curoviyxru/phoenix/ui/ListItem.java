package ru.curoviyxru.phoenix.ui;

import ru.curoviyxru.phoenix.Theming;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import ru.curoviyxru.j2vk.PageStorage;
import ru.curoviyxru.j2vk.VKConstants;
import ru.curoviyxru.j2vk.api.objects.Conversation;
import ru.curoviyxru.j2vk.api.objects.PhotoAlbum;
import ru.curoviyxru.j2vk.api.objects.VKObject;
import ru.curoviyxru.j2vk.api.objects.attachments.Audio;
import ru.curoviyxru.j2vk.api.objects.attachments.AudioPlaylist;
import ru.curoviyxru.j2vk.api.objects.attachments.Photo;
import ru.curoviyxru.j2vk.api.objects.user.Page;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.midlet.Midlet;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class ListItem extends PaneItem implements PaneItem.ItemThatUsesFullWidth, ImageProvider {

    public SuperString caption, renderCaption;
    int renderCaptionWidth;
    public boolean music;
    public boolean boldFont;
    public int iconMode = -1;
    public byte mode;
    public String cornerIcon;
    public final static byte TEXT = 0, CHECK = 1, GOTO = 2,
            UNREAD = 3, UNREAD_MUTED = 4, UNREAD_OUT = 5, MUSIC = 6;
    public short rightState;
    public String timestamp;
    public SuperString desc, renderDesc;
    String iconObj;
    public static Image smallCircleImage = null, smallCircleImageSelected = null, smallCircleImageUnreaded = null,
            fullCircleImage = null, fullCircleImageSelected = null, fullCircleImageUnreaded = null;
    public static Image cornerBgImage = null;
    public static Image unreadCorner = null, unreadCornerSelected = null, unreadCornerUnreaded = null;
    public static int smallHeight = AppCanvas.instance.normalEmojiFont.height * 2,
            fullHeight = AppCanvas.instance.normalEmojiFont.height * 13 / 4;
    public static int smallHeightAvatar = smallHeight - AppCanvas.instance.normalEmojiFont.height / 2,
            fullHeightAvatar = fullHeight - AppCanvas.instance.normalEmojiFont.height;
    public static int cornerIconSize =
            AppCanvas.instance.normalEmojiFont.height * 3 / 4;
    public static int cornerBgSize =
            cornerIconSize + Math.max(2, cornerIconSize / 3 / 2) * 2;
    public static int unreadCornerSize = AppCanvas.instance.boldFont.getHeight() + AppCanvas.instance.perLineSpace;
    public boolean noCircling;
    public boolean ignoreUnreadBackground;
    public boolean useFullTimestamp;
    public int startX, rightCaptionX, rightDescX;
    public int tries;
    
    public int tries() {
        return tries;
    }
    
    public void tried() {
        if (tries > 5) return;
        ++tries;
    }
    public int size() {
        return 1;
    }

    public boolean local(int i) {
        return iconMode == 2;
    }

    public String get(int i) {
        return iconObj != null && iconMode != 0 && ((desc == null && content != null && content.screenCache.get("s/" + iconObj) == null) || (desc != null && content != null && content.screenCache.get("f/" + iconObj) == null)) ? iconObj : null;
    }

    public void set(int i, Image image) {
        if (image == null || iconObj == null || content == null) {
            return;
        }
        if (desc == null) { // && content.screenCache.get("s/"+iconObj) == null) {
            content.screenCache.put("s/" + iconObj, RenderUtil.resizeImage(image, Math.min(smallHeightAvatar * image.getHeight() / Math.max(1, image.getWidth()), smallHeightAvatar * image.getWidth() / Math.max(1, image.getHeight()))));
        } else { // if (desc != null && content.screenCache.get("f/"+iconObj) == null) {
            content.screenCache.put("f/" + iconObj, RenderUtil.resizeImage(image, Math.min(fullHeightAvatar * image.getHeight() / Math.max(1, image.getWidth()), fullHeightAvatar * image.getWidth() / Math.max(1, image.getHeight()))));
        }
    }

    public ListItem setTimestamp(String t) {
        this.timestamp = t;
        return this;
    }
    
    public ListItem useFullTimestamp(boolean b) {
        useFullTimestamp = b;
        return this;
    }

    public ListItem setTimestamp(int timestamp) {
        this.timestamp = VKObject.timeToString(timestamp);
        return this;
    }

    public ListItem ignoreUnreadBackground(boolean ignore) {
        ignoreUnreadBackground = ignore;
        return this;
    }

    public ListItem setCornerIcon(int icon) {
        this.cornerIcon = getCornerPath(icon);
        return this;
    }

    public ListItem clearIcon(String icon) {
        if (iconObj != null && icon != null && !iconObj.equals(icon) && content != null) {
            content.screenCache.remove("s/" + iconObj);
            content.screenCache.remove("f/" + iconObj);
        }
        iconObj = null;
        iconMode = -1;
        
        return this;
    }

    public ListItem setIcon(String icon) {
        clearIcon(icon);
        if (icon == null) return this;
        iconObj = icon;
        iconMode = 0;

        return this;
    }

    public ListItem setURL(String a) {
        clearIcon(a);
        if (Midlet.instance.config.doNotLoadImages) {
            return this;
        }
        iconObj = a;
        iconMode = 1;
        //AppCanvas.instance.queue(this);

        return this;
    }

    public ListItem setLocalURL(String a) {
        clearIcon(a);
        if (Midlet.instance.config.doNotLoadImages) {
            return this;
        }
        iconObj = a;
        iconMode = 2;
        //AppCanvas.instance.queue(this);

        return this;
    }

    public ListItem setAudio(Audio a) {
        //Uncomment for Kate:
        //return setIcon("new/music-note.rle");
        
        //Comment this code for Kate.
        updateHeight();
        if (Midlet.instance.config.doNotLoadImages) {
            setIcon("new/music-note.rle");
            return this;
        }
		
        String temp = a == null ? null : (height > 68 ? a.getPhoto_135() : a.getPhoto_68());
        if (temp == null) {
            setLocalURL(height > 68 ? "new/thumb_135.png" : "new/thumb_68.png");
        } else {
            setURL(temp);
        }

        return this;
    }
	
	   //todo blocked in region support
    public ListItem setAudioPlaylist(AudioPlaylist ap) {
        setCaption(ap.title);
        if (ap.bottom() != null) {
            setDescription(ap.bottom());
        } else {
            setDescription(Localization.get("title.playlist"));
        }

        updateHeight();

        String temp = ap.photo == null ? null : (height > 68 ? ap.photo.photo_135 : ap.photo.photo_68);
        if (Midlet.instance.config.doNotLoadImages || temp == null) {
            setIcon("new/playlist-music.rle");
        } else {
            setURL(temp);
        }

        return this;
    }

    public ListItem setPhoto(Photo p) {
        updateHeight();
		
        String temp = (Midlet.instance.config.doNotLoadImages || p == null) ? null : p.getURL(height);
        if (temp == null) {
            setIcon("new/image.rle");
        } else {
            setURL(temp);
        }

        return this;
    }

    public ListItem setPhotoAlbum(PhotoAlbum p) {
        updateHeight();
        if (Midlet.instance.config.doNotLoadImages) {
            clearIcon(iconObj);
            return this;
        }
		
        /*String temp = p == null ? null : p.thumb_src;
        if (temp == null) {*/
            setIcon("new/folder.rle");
        /*} else {
            setURL(temp);
        }*/

        return this;
    }

    public ListItem setPage(Page p) {
        if (p == null) {
            return this;
        }
        updateHeight();
        if (Midlet.instance.config.doNotLoadImages) {
            clearIcon(iconObj);
            return this;
        }
        String temp = height > 50 ? p.getPhoto_100() : p.getPhoto_50();
        if (temp == null) {
            if (p.isGroup) {
                if (!VKObject.isEmpty(p.asGroup().deactivated)) {
                    setLocalURL(height > 50 ? "new/deactivated_100.png" : "new/deactivated_50.png");
                } else {
                    setLocalURL(height > 50 ? "new/community_100.png" : "new/community_50.png");
                }
            } else {
                if (!VKObject.isEmpty(p.asUser().deactivated)) {
                    setLocalURL(height > 50 ? "new/deactivated_100.png" : "new/deactivated_50.png");
                } else {
                    setLocalURL(height > 50 ? "new/camera_100.png" : "new/camera_50.png");
                }
            }
        } else {
            setURL(temp);
        }

        return this;
    }

    public ListItem setConver(Conversation c) {
        if (c == null) {
            return this;
        }
        updateHeight();
        if (Midlet.instance.config.doNotLoadImages) {
            clearIcon(iconObj);
            return this;
        }
        
        if (c.getId() == VKConstants.account.getId()) {
            setLocalURL(height > 50 ? "new/im_favorites_100.png" : "new/im_favorites_50.png");
            return this;
        }
        
        String temp = height > 50 ? c.getPhoto_100() : c.getPhoto_50();
        if (temp == null) {
            if (c.isPage()) {
                setPage(PageStorage.get(c.getId()));
            } else {
                setLocalURL(height > 50 ? "new/im_multichat_100.png" : "new/im_multichat_50.png");
            }
        } else {
            setURL(temp);
        }

        return this;
    }

    public void actionPerformed() {
        if (mode == CHECK) {
            rightState = (short) (rightState == 0 ? 1 : 0);
            updateCA();
            stateUpdated();
        }
    }

    public void stateUpdated() {
    }

    public void updateCA() {
        this.cAction = Localization.get(mode == TEXT ? cAction : mode == CHECK ? rightState != 0 ? "action.uncheck" : "action.check" : "action.goTo");
    }

    public ListItem setCaption(String s) {
        caption = s == null ? null : new SuperString(s);
        renderCaption = null;
        renderCaptionWidth = 0;
        return this;
    }

    public ListItem setFont(boolean bold) {
        this.boldFont = bold;
        renderCaption = null;
        renderCaptionWidth = 0;

        return this;
    }

    public ListItem setDescription(String s) {
        return setDescription(s == null ? null : new SuperString(s));
    }
    
    public ListItem setDescription(SuperString s) {
        desc = s;
        updateHeight();
        renderDesc = null;

        renderCaption = null;
        renderCaptionWidth = 0;

        return this;
    }

    public ListItem setMode(byte mode) {
        this.mode = mode;

        return this;
    }

    public ListItem(String caption, byte mode) {
        this.mode = mode;
        updateHeight();
        setCaption(caption);
        updateCA();
    }

    public ListItem(String caption) {
        this(caption, TEXT);
    }

    public void updateHeight() {
        height = desc != null ? fullHeight : smallHeight;
    }

    public void paint(Graphics gr, int pY, int pX) {
        updateHeight();
        
        if ((focusable && pressed)
                || (((mode == UNREAD || mode == UNREAD_MUTED) && !ignoreUnreadBackground) && rightState != 0)) {
            RenderUtil.fillRect(gr, x + pX, y + pY, width, height, focusable && pressed ? Theming.now.focusedBackgroundColor : Theming.now.unreadBackgroundColor, focusable && pressed ? Theming.now.focusedBackgroundColor_ : Theming.now.unreadBackgroundColor_);
        }

        //TODO: icon mode 3 and 4
        switch (iconMode) {
            default:
                startX = AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace;
                break;
            case 0:
                startX = (desc != null ? fullHeightAvatar : smallHeightAvatar) + AppCanvas.instance.perLineSpace * 3;
                RenderUtil.renderListItemIcon(gr, x + AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace + pX, y + pY + height / 2, iconObj,
                        focusable && pressed ? 1 : 2,
                        focusable && pressed ? Theming.now.focusedIconColor : Theming.now.iconColor,
                        focusable && pressed ? Theming.now.focusedIconColor_ : Theming.now.iconColor_,
                        desc != null ? fullHeightAvatar : smallHeightAvatar, Graphics.VCENTER | Graphics.LEFT);
                if (Midlet.instance.config.debug_drawImagesFrames) {
                    gr.setColor(0xFF0000);
                    gr.drawRect(x + AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace + pX, y + pY + height / 2 - (desc != null ? fullHeightAvatar : smallHeightAvatar) / 2, (desc != null ? fullHeightAvatar : smallHeightAvatar) - 1, (desc != null ? fullHeightAvatar : smallHeightAvatar) - 1);
                }
                break;
            case 1:
            case 2:
                //TODO thread load
                startX = (desc != null ? fullHeightAvatar : smallHeightAvatar) + AppCanvas.instance.perLineSpace * 3;
                if (desc != null) {
                    //smallCache = null;
                    Image fullCache = content != null ? (Image) content.screenCache.get("f/"+iconObj) : null;
                    if (fullCache == null) {
                        AppCanvas.instance.queue(this);
                        gr.setColor(Theming.now.nonLoadedContentColor);
                        gr.fillRect(x + AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace + pX, y + pY + height / 2 - fullHeightAvatar / 2, fullHeightAvatar, fullHeightAvatar);
                    } else {
                        gr.drawImage(fullCache, 
								x + AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace + pX + fullHeightAvatar / 2, 
								y + pY + height / 2, 
								Graphics.VCENTER | Graphics.HCENTER
								);
                    }

                    if (!noCircling) {
                        Image tmpImg;
                        if (focusable && pressed) {
                            tmpImg = fullCircleImageSelected;
                        } else if ((mode == UNREAD || mode == UNREAD_MUTED) && rightState != 0) {
                            tmpImg = fullCircleImageUnreaded;
                        } else {
                            tmpImg = fullCircleImage;
                        }

                        if (tmpImg != null) {
                            gr.drawImage(tmpImg,
                                    x + AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace + pX,
                                    y + pY + height / 2,
                                    Graphics.VCENTER | Graphics.LEFT);
                        }
                    }
                } else {
                    //fullCache = null;
                    Image smallCache = content != null ? (Image) content.screenCache.get("s/"+iconObj) : null;
                    if (smallCache == null) {
                        AppCanvas.instance.queue(this);
                        gr.setColor(Theming.now.nonLoadedContentColor);
                        gr.fillRect(x + AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace + pX, y + pY + height / 2 - smallHeightAvatar / 2, smallHeightAvatar, smallHeightAvatar);
                    } else {
                        gr.drawImage(smallCache, 
								x + AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace + pX + smallHeightAvatar / 2, 
								y + pY + height / 2, 
								Graphics.VCENTER | Graphics.HCENTER
								);
                    }

                    if (!noCircling) {
                        Image tmpImg;
                        if (focusable && pressed) {
                            tmpImg = smallCircleImageSelected;
                        } else if ((mode == UNREAD || mode == UNREAD_MUTED) && rightState != 0) {
                            tmpImg = smallCircleImageUnreaded;
                        } else {
                            tmpImg = smallCircleImage;
                        }

                        if (tmpImg != null) {
                            gr.drawImage(tmpImg,
                                    x + AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace + pX,
                                    y + pY + height / 2,
                                    Graphics.VCENTER | Graphics.LEFT);
                        }
                    }
                }
                if (Midlet.instance.config.debug_drawImagesFrames) {
                    gr.setColor(0xFF0000);
                    gr.drawRect(x + AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace + pX, y + pY + height / 2 - (desc != null ? fullHeightAvatar : smallHeightAvatar) / 2, (desc != null ? fullHeightAvatar : smallHeightAvatar) - 1, (desc != null ? fullHeightAvatar : smallHeightAvatar) - 1);
                }
                break;
        }

        //render cornerIcon at caption or corner of left icon
        rightCaptionX = AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace;
        rightDescX = AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace;

        String s = rightState + "";
        int sw = AppCanvas.instance.boldFont.stringWidth(s);
        int bgw = Math.max(sw + AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace, unreadCornerSize);
        int tsw = timestamp != null ? AppCanvas.instance.normalFont.stringWidth(timestamp) : 0;
        switch (mode) {
            case CHECK:
            case GOTO:
                rightCaptionX = rightDescX = AppCanvas.instance.perLineSpace * 3 + smallHeightAvatar;
                break;
            case UNREAD:
            case UNREAD_MUTED:
            case UNREAD_OUT:
                if (timestamp != null || rightState != 0) {
                    rightCaptionX = Math.max(bgw, tsw) + AppCanvas.instance.perLineSpace * 3;
                }
                if (rightState != 0 || useFullTimestamp) {
                    rightDescX = rightCaptionX;
                }
                break;
        }

        if (caption != null && renderCaption == null) {
            renderCaption = (boldFont ? AppCanvas.instance.boldEmojiFont : AppCanvas.instance.normalEmojiFont).limit(caption, width - startX - rightCaptionX, true);
            renderCaptionWidth = (boldFont ? AppCanvas.instance.boldEmojiFont : AppCanvas.instance.normalEmojiFont).stringWidth(renderCaption);
            //hasEmoji = FontWithEmoji.hasEmoji(renderCaption) || FontWithEmoji.hasEmoji(renderDesc);
        }
        
        int rcy = y + pY + AppCanvas.instance.normalEmojiFont.height / 2;
        
        if (cornerIcon != null) {
            int cx, cy;
            int ll;
            if (iconObj == null || iconMode == -1) {
                cx = x + pX + startX + renderCaptionWidth + AppCanvas.instance.perLineSpace;
                cy = rcy;
                ll = Graphics.TOP | Graphics.LEFT;

                RenderUtil.renderListItemIcon(gr, cx, cy, cornerIcon,
                        1,
                        Theming.now.onlineIconColor,
                        Theming.now.onlineIconColor, cornerIconSize, ll);
            } else {
                cx = x + AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace + pX + (desc != null ? fullHeightAvatar : smallHeightAvatar) - cornerIconSize / 2;
                cy = y + pY + height / 2 + (desc != null ? fullHeightAvatar : smallHeightAvatar) / 2 - cornerIconSize / 2;
                ll = Graphics.VCENTER | Graphics.HCENTER;
                
                if (cornerBgImage != null) {
                    gr.drawImage(cornerBgImage, cx, cy, ll);
                }
                RenderUtil.renderListItemIcon(gr, cx, cy, cornerIcon,
                        1,
                        Theming.now.onlineIconColor,
                        Theming.now.onlineIconColor, cornerIconSize, ll);
            }
            
        }

        if (desc != null && renderDesc == null) {
            renderDesc = AppCanvas.instance.normalEmojiFont.limit(desc, width - startX - rightDescX, true);
        }

        if (renderCaption != null) {
            gr.setColor(focusable && pressed ? Theming.now.focusedTextColor : Theming.now.textColor);
            (boldFont ? AppCanvas.instance.boldEmojiFont : AppCanvas.instance.normalEmojiFont).drawString(gr, renderCaption, x + pX + startX, rcy, Graphics.TOP | Graphics.LEFT);
        }

        int dcy = y + pY + AppCanvas.instance.normalEmojiFont.height * 7 / 4;
        if (renderDesc != null) {
            if (!(focusable && pressed) && mode == UNREAD_OUT) {
                RenderUtil.fillRect(gr,
                        x + pX + startX, dcy,
                        width - startX - rightDescX, AppCanvas.instance.normalEmojiFont.height,
                        Theming.now.unreadBackgroundColor, Theming.now.unreadBackgroundColor_);
            }

            gr.setColor(focusable && pressed ? Theming.now.focusedCaptionColor : Theming.now.captionColor);
            AppCanvas.instance.normalEmojiFont.drawString(gr, renderDesc,
                    x + pX + startX, dcy,
                    Graphics.TOP | Graphics.LEFT);
        }

        int cx = x + width + pX - smallHeight / 2 - AppCanvas.instance.perLineSpace - AppCanvas.instance.perLineSpace;
        int cy = y + pY + height / 2;

        switch (mode) {
            case CHECK:
                RenderUtil.renderListItemIcon(gr, cx, cy, rightState != 0 ? "new/checkbox-selected.rle" : "new/checkbox-none.rle",
                        focusable ? (pressed ? 1 : rightState != 0 ? 2 : 3) : 4,
                        focusable ? (pressed ? Theming.now.focusedCheckboxColor : rightState != 0 ? Theming.now.selectedCheckboxColor : Theming.now.checkboxColor) : Theming.now.checkboxColor,
                        focusable ? (pressed ? Theming.now.focusedCheckboxColor_ : rightState != 0 ? Theming.now.selectedCheckboxColor_ : Theming.now.checkboxColor_) : Theming.now.checkboxColor_,
                        smallHeightAvatar, Graphics.VCENTER | Graphics.HCENTER);
                break;
            case GOTO:
                RenderUtil.renderListItemIcon(gr, cx, cy, "new/chevron-right.rle",
                        focusable && pressed ? 1 : 2,
                        focusable && pressed ? Theming.now.focusedGotoColor : Theming.now.gotoColor,
                        focusable && pressed ? Theming.now.focusedGotoColor_ : Theming.now.gotoColor_,
                        smallHeightAvatar, Graphics.VCENTER | Graphics.HCENTER);
                break;
            case UNREAD:
            case UNREAD_MUTED:
            case UNREAD_OUT:
                cx += smallHeight / 2;

                if (timestamp != null) {
                    if (rightState == 0 && useFullTimestamp) rcy = cy - AppCanvas.instance.boldFont.getHeight() / 2;
                    gr.setFont(AppCanvas.instance.normalFont);
                    gr.setColor(focusable && pressed ? Theming.now.focusedCaptionColor : Theming.now.captionColor);
                    gr.drawString(timestamp, cx, rcy, Graphics.TOP | Graphics.RIGHT);
                }

                if (rightState != 0) {
                    if (desc != null && timestamp != null) {
                        cy = dcy;
                    } else {
                        cy -= AppCanvas.instance.boldFont.getHeight() / 2;
                    }

                    int color, color_;
                    if (mode == UNREAD) {
                        color = focusable && pressed
                                ? Theming.now.focusedUnreadCloudBackgroundColor
                                : Theming.now.unreadCloudBackgroundColor;
                        color_ = focusable && pressed
                                ? Theming.now.focusedUnreadCloudBackgroundColor_
                                : Theming.now.unreadCloudBackgroundColor_;
                    } else {
                        color = focusable && pressed
                                ? Theming.now.focusedMutedUnreadCloudBackgroundColor
                                : Theming.now.mutedUnreadCloudBackgroundColor;
                        color_ = focusable && pressed
                                ? Theming.now.focusedMutedUnreadCloudBackgroundColor_
                                : Theming.now.mutedUnreadCloudBackgroundColor_;
                    }

                    RenderUtil.fillRect(gr, cx - bgw, cy - AppCanvas.instance.perLineSpace / 2, bgw, unreadCornerSize, color, color_);
                    int halfCornerSize = unreadCornerSize / 2;

                    if (focusable && pressed) {
                        if (unreadCornerSelected != null) {
                            gr.drawRegion(unreadCornerSelected, 0, 0, halfCornerSize, unreadCornerSize, 0, cx - bgw, cy - AppCanvas.instance.perLineSpace / 2, Graphics.TOP | Graphics.LEFT);
                            gr.drawRegion(unreadCornerSelected, unreadCornerSize - halfCornerSize, 0, halfCornerSize, unreadCornerSize, 0, cx - halfCornerSize, cy - AppCanvas.instance.perLineSpace / 2, Graphics.TOP | Graphics.LEFT);
                        }
                    } else if (rightState != 0) {
                        if (unreadCornerUnreaded != null) {
                            gr.drawRegion(unreadCornerUnreaded, 0, 0, halfCornerSize, unreadCornerSize, 0, cx - bgw, cy - AppCanvas.instance.perLineSpace / 2, Graphics.TOP | Graphics.LEFT);
                            gr.drawRegion(unreadCornerUnreaded, unreadCornerSize - halfCornerSize, 0, halfCornerSize, unreadCornerSize, 0, cx - halfCornerSize, cy - AppCanvas.instance.perLineSpace / 2, Graphics.TOP | Graphics.LEFT);
                        }
                    } else {
                        if (unreadCorner != null) {
                            gr.drawRegion(unreadCorner, 0, 0, halfCornerSize, unreadCornerSize, 0, cx - bgw, cy - AppCanvas.instance.perLineSpace / 2, Graphics.TOP | Graphics.LEFT);
                            gr.drawRegion(unreadCorner, unreadCornerSize - halfCornerSize, 0, halfCornerSize, unreadCornerSize, 0, cx - halfCornerSize, cy - AppCanvas.instance.perLineSpace / 2, Graphics.TOP | Graphics.LEFT);
                        }
                    }

                    gr.setColor(focusable && pressed ? Theming.now.focusedUnreadCloudForegroundColor : Theming.now.unreadCloudForegroundColor);
                    gr.setFont(AppCanvas.instance.boldFont);
                    gr.drawString(s, cx - (bgw - sw) / 2, cy, Graphics.TOP | Graphics.RIGHT);
                }

                break;
            case MUSIC:
                //TODO: play/pause icon sync
                break;
        }
    }

    public static String getCornerPath(int i) {
        //pc, mobile, android, apple, windows, vkme, pencil
        switch (i) {
            case 0:
                return null;
            case 2:
                return "new/online-mobile.rle";
            case 3:
                return "new/online-android.rle";
            case 4:
                return "new/online-apple.rle";
            case 5:
                return "new/online-win.rle";
            case 6:
                return "new/online-vkme.rle";
            case 7:
                return "new/pencil.rle";
            default:
                return "new/online-pc.rle";
        }
    }

    public PaneItem pointerReleased(int x, int y, int scrollY) {
        if (pressed && focusable && iconMode != -1) {
            if (x < this.x + (desc != null ? fullHeightAvatar : smallHeightAvatar) + AppCanvas.instance.perLineSpace
                    && x > this.x + AppCanvas.instance.perLineSpace
                    && y < this.y + height + AppCanvas.instance.contentY - scrollY
                    && y > this.y + AppCanvas.instance.contentY - scrollY) {
                iconPressPerformed();
                pressed = false;
                return this;
            }
        }

        return super.pointerReleased(x, y, scrollY);
    }

    public void iconPressPerformed() {
        actionPerformed();
    }

    public void resetCache() {
        renderCaption = null;
        renderCaptionWidth = 0;
        renderDesc = null;
    }

    public ListItem setState(boolean b) {
        rightState = (short) (b ? 1 : 0);
        return this;
    }

    public ListItem setState(short i) {
        if ((rightState != 0) != (i != 0)) {
            renderCaption = null;
            renderCaptionWidth = 0;
            renderDesc = null;
        }
        rightState = i;

        return this;
    }

    public boolean marked() {
        return rightState != 0;
    }
    
    public void errored(Throwable ex) {
        AppCanvas.instance.dropError("List item: " + ex != null ? ex.getMessage() : "Unknown error");
    }
}
