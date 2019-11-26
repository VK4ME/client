package ru.curoviyxru.phoenix.ui;

import ru.curoviyxru.phoenix.ui.contents.ConversationList;
import ru.curoviyxru.phoenix.ui.contents.ContentController;
import ru.curoviyxru.phoenix.ui.contents.MessageContent;
import ru.curoviyxru.phoenix.Theming;
import ru.curoviyxru.j2vk.TextUtil;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import ru.curoviyxru.j2vk.HTTPClient;
import ru.curoviyxru.j2vk.LongPoll;
import ru.curoviyxru.j2vk.VKConstants;
import ru.curoviyxru.phoenix.DownloadUtils;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.Logger;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.contents.CommentContent;
import ru.curoviyxru.phoenix.ui.im.ImField;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class AppCanvas extends Canvas {

    public boolean lpFetching = false;
    //TODO: custom double buffer
    //TODO: allow use lW/lH instead of getWidth/getHeight
    public static int MAX_TEXT_LENGTH = 3072;
    public static final int KEY_CODE_LEFT_MENU = 1000001;
    public static final int KEY_CODE_RIGHT_MENU = 1000002;
    public static final int KEY_CODE_BACK_BUTTON = 1000003;
    public static AppCanvas instance;
    public static TextBox textBox;
    public static Field lastField;
    public static Command cancelBox, okBox;
    public static boolean horizontalMode = false;
    boolean showSpinner;
    Image renderImage;
    Graphics renderGraphics;
    Sprite renderSprite;
    public Font normalFont;
    public Font boldFont;
    public FontWithEmoji normalEmojiFont;
    public FontWithEmoji boldEmojiFont;
    int rotateMode;
    Runtime runtime;
    public Content content;
    double fingerDist;
    long lastClock;
    PopupMenu popupNow;
    public boolean gotNewMessage;
    public int pX, pY, lX, lY, fY, fX;
    public boolean pressed;
    boolean sizeChanged;
    boolean hasTouch, showSelection;
    int bottomNavHeight, bottomNavY, bottomNavIconHeight;
    int topNavHeight, topNavIconHeight;
    boolean leftSP, rightSP;
    public int contentY, contentHeight, scrollX, scrollWidth, contentOriginalHeight;
    int bottomNavNormalTextY, bottomNavBoldTextY, topNavBoldTextY;
    public boolean touchHud, showHeader, showFooter;
    int backIcon, menuIcon;
    public String clock;
    public int lH, lW;
    boolean slAnimDir;
    AnimationInterp slAnim;
    Content animC;
    Image cache1, cache2;
    int spinnerIcon;
    int spinnerFrame;
    AnimationInterp scrollOpaq;
    byte lastShow;
    boolean splash = true;
    boolean justReinited;
    boolean started;
    boolean showDock, slimHeader;
    int splashLogo;
    public int perLineSpace;
    int popupWidth;
    Thread thr;
    Vector itemsToLoad;
    private int cachedDimmColor;
    private Image cachedDimm;
    private boolean fingerOnNavBar, fingerOnOtherBar;
    public SoftButton leftSoft, rightSoft = new SoftButton(null) {
        public void trigger() {
            if (AppCanvas.instance.content != null) {
                AppCanvas.instance.content.rightSoft();
            }
        }
    };
    public boolean reverseSoftButtons;
    int dock1, dock1H, dock2, dock2H, dock3, dock3H, dock4, dock4H, dock5, dock5H;
    Content drawerBack;
    int drawerIcon;
    public int sleepTime;
    public static Image tranImageUnread, tranImageStandard, borderImage, outBorderImage, selBorderImage;
    public static int tranSizeStatic, tranSize;

    public AppCanvas() {
        instance = this;

        setTitle("VK4ME");

        lW = getWidth();
        lH = getHeight();

        runtime = Runtime.getRuntime();
        hasTouch = hasPointerEvents();
        showSelection = !hasTouch;

        normalEmojiFont = new FontWithEmoji(normalFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
        boldEmojiFont = new FontWithEmoji(boldFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL));
        perLineSpace = normalFont.charWidth(' ');

        try {
            TextBox textBox = new TextBox("", "", 3072, 0);
            if (textBox.getMaxSize() < MAX_TEXT_LENGTH) {
                MAX_TEXT_LENGTH = textBox.getMaxSize();
            }
        } catch (Throwable e) {
            MAX_TEXT_LENGTH = 3072;
        }

        itemsToLoad = new Vector();

        System.gc();
    }

    private void stopSplash() {
        //splashLogo = 0; //TODO remove splash logo from cache
        splash = false;
        render();
    }

    protected void paint(Graphics g) {
        try {
            long paintStart = System.currentTimeMillis();
            //#ifndef CACHING
            if (((renderGraphics == null || lW != getWidth() || lH != getHeight())) || sizeChanged) {
                //#else
//#         if (((renderImage == null || renderImage.getHeight() != getHeight() || renderImage.getWidth() != getWidth())) || sizeChanged) {
                //#endif
                renderGraphics = g;
                init(false);
                //#ifndef CACHING
                if (!splash) {
                    renderContent();
                }
                //#endif
            }

            //#ifndef CACHING
            renderGraphics = g;
            //#endif

            renderSplash();
            if (splash) {
                return;
            }

            if (Theming.interp.started) {
                Theming.updateTheme();
            }

            boolean hasPopup = popupNow != null && popupNow.active && popupNow.open;

            if (!hasPopup || popupNow.renderShadow) {
                renderBackground();
                renderContent();
                renderForeground();

                if (touchHud) {
                    renderTouchHeader();
                    renderDock();
                } else {
                    renderNavbar();
                    renderHeader();
                }

                if (hasPopup) {
                    renderDimm();
                }
            }

            if (hasPopup) {
                popupNow.paint(renderGraphics, 0);
            }

            //#ifdef CACHING
//#                  renderSprite.paint(g);
            //#endif

            if (Midlet.instance.config.debug_drawFPS) {
                drawFPS(g, paintStart);
            }
            if (Midlet.instance.config.debug_lp) {
                drawLPState(g);
            }

            //System.gc();
        } catch (Throwable ex) {
            if (ex instanceof OutOfMemoryError) {
                init(true);
            }
            dropError(ex);
            //Midlet.panic(ex);
        }
    }

    private void drawLPState(Graphics g) {
        g.setColor(lpFetching ? 0x00FF00 : LongPoll.slowMode ? 0xFFFF00 : 0xFF0000);
        g.fillRect(lW - 20, 0, 20, 20);
    }

    private void drawFPS(Graphics g, long paintStart) {
        int paintTime = (int) (System.currentTimeMillis() - paintStart);
        if (paintTime == 0) {
            paintTime = 1;
        }
        int fps = 1000 / paintTime;

        int charHeight = boldFont.getHeight();

        String pts = "Paint time: " + paintTime;
        String fpss = "FPS: " + fps;
        Runtime r = Runtime.getRuntime();
        String heap = (r.totalMemory() - r.freeMemory()) / 1024 + " / " + r.totalMemory() / 1024 + " KiB";

        g.setColor(justReinited ? 0x800000 : 0);
        justReinited = false;
        g.fillRect(0, 0, 8 + Math.max(boldFont.stringWidth(pts), Math.max(boldFont.stringWidth(fpss), boldFont.stringWidth(heap))), charHeight * 3 + 8 + 4);

        int color = 0xff0000;
        if (fps >= 40) {
            color = 0x00ffdd;
        } else if (fps >= 30) {
            color = 0x00ff00;
        } else if (fps >= 20) {
            color = 0xffff00;
        } else if (fps >= 15) {
            color = 0xff8000;
        }

        g.setColor(color);
        g.setFont(boldFont);
        g.drawString(pts, 4, 4, 0);
        g.drawString(fpss, 4, 4 + charHeight + 2, 0);
        g.drawString(heap, 4, 4 + charHeight + 2 + charHeight + 2, 0);
    }

    private void renderTouchHeader() {
        RenderUtil.fillRect(renderGraphics, 0, 0, lW, topNavHeight, Theming.now.mainColor, Theming.now.mainColor_);

        int leftX = (Midlet.instance.config.shiftTitleRight ? 22 : 0);
        int rightX = perLineSpace + perLineSpace;

        if (showSpinner || leftSoft != null) {
            if (leftSP) {
                RenderUtil.fillRect(renderGraphics, leftX, 0, topNavHeight, topNavHeight, Theming.now.focusedMainColor, Theming.now.focusedMainColor_);
            }

            if (showSpinner) {
                int rot;
                switch (spinnerFrame) {
                    case 1:
                        rot = Sprite.TRANS_ROT90;
                        break;
                    case 2:
                        rot = Sprite.TRANS_ROT180;
                        break;
                    case 3:
                        rot = Sprite.TRANS_ROT270;
                        break;
                    default:
                        rot = Sprite.TRANS_NONE;
                        break;
                }

                spinnerIcon = Theming.now.drawImage(renderGraphics,
                        leftX + topNavHeight / 2, topNavHeight / 2,
                        spinnerIcon, "new/loading.rle",
                        Theming.now.onMainIconColor, Theming.now.onMainIconColor_, topNavIconHeight,
                        rot, Graphics.HCENTER | Graphics.VCENTER);
            } else if (!(Midlet.instance.config.gui_useDrawer && ContentController.menu != null)) {
                backIcon = Theming.now.drawImage(renderGraphics,
                        leftX + topNavHeight / 2, topNavHeight / 2,
                        backIcon, "new/arrow-left.rle", Theming.now.onMainIconColor, Theming.now.onMainIconColor_, topNavIconHeight,
                        Graphics.HCENTER | Graphics.VCENTER);
            } else {
                drawerIcon = Theming.now.drawImage(renderGraphics,
                        leftX + topNavHeight / 2, topNavHeight / 2,
                        drawerIcon, "new/menu.rle", Theming.now.onMainIconColor, Theming.now.onMainIconColor_, topNavIconHeight,
                        Graphics.HCENTER | Graphics.VCENTER);
            }
            leftX += topNavHeight;
        }
        leftX += perLineSpace + perLineSpace;

        if (KeyboardUtil.last != -1 && KeyboardUtil.caret != -1) {
            renderGraphics.setColor(Theming.now.onMainTextColor);
            renderGraphics.setFont(boldFont);
            char[] array = ((KeyboardUtil.lang / 2) == 0 ? KeyboardUtil.ENG12[KeyboardUtil.last] : KeyboardUtil.RUS12[KeyboardUtil.last]);
            for (int i = array.length - 1; i >= 0; --i) {
                if (KeyboardUtil.lang % 2 == 1) {
                    array[i] = Character.toUpperCase(array[i]);
                }
                int chw = boldFont.charWidth(array[i]);
                if (KeyboardUtil.caret == i) {
                    RenderUtil.fillRect(renderGraphics, lW - rightX - chw, topNavBoldTextY, chw, boldFont.getHeight(), Theming.now.focusedMainColor, Theming.now.focusedMainColor_);
                }
                renderGraphics.drawChar(array[i], lW - rightX, topNavBoldTextY, Graphics.RIGHT | Graphics.TOP);
                rightX += chw;
            }
            rightX += perLineSpace + perLineSpace;
        } else if (KeyboardUtil.last == 11 || KeyboardUtil.last == 10) {
            renderGraphics.setColor(Theming.now.onMainTextColor);
            renderGraphics.setFont(boldFont);
            String lang = "Bksp";
            if (KeyboardUtil.last == 11) {
                switch (KeyboardUtil.lang) {
                    case 0:
                        lang = "eng";
                        break;
                    case 1:
                        lang = "ENG";
                        break;
                    case 2:
                        lang = "рус";
                        break;
                    case 3:
                        lang = "РУС";
                        break;
                }
            }
            renderGraphics.drawString(lang, lW - rightX, topNavBoldTextY, Graphics.RIGHT | Graphics.TOP);
            rightX += perLineSpace + perLineSpace + boldFont.stringWidth(lang);
        } else if (content != null && content.getRightSoft() != null) {
            rightX = topNavHeight + perLineSpace + perLineSpace;
            if (rightSP) {
                RenderUtil.fillRect(renderGraphics, lW - topNavHeight, 0, topNavHeight, topNavHeight, Theming.now.focusedMainColor, Theming.now.focusedMainColor_);
            }

            menuIcon = Theming.now.drawImage(renderGraphics,
                    lW - topNavHeight / 2, topNavHeight / 2,
                    menuIcon, "new/dots-vertical.rle", Theming.now.onMainIconColor, Theming.now.onMainIconColor_, topNavIconHeight,
                    Graphics.HCENTER | Graphics.VCENTER);
        }

        if (content.cornerIcon != null) {
            RenderUtil.renderListItemIcon(renderGraphics, leftX, topNavBoldTextY, content.cornerIcon,
                    1,
                    Theming.now.onlineIconColor,
                    Theming.now.onlineIconColor, ListItem.cornerIconSize, Graphics.LEFT | Graphics.TOP);
            leftX += ListItem.cornerIconSize + perLineSpace;
        }

        int finalL = lW - leftX - rightX;

        if (content != null && content.title != null && (content.renderTitle == null || content.lW != finalL)) {
            content.renderTitle = boldEmojiFont.limit(content.title, finalL, true);
            content.lW = finalL;
        }

        if (content != null && content.renderTitle != null) {
            renderGraphics.setColor(Theming.now.onMainTextColor);
            boldEmojiFont.drawString(renderGraphics, content.renderTitle,
                    leftX, topNavBoldTextY,
                    Graphics.TOP | Graphics.LEFT);
        }
    }

    private void renderHeader() {
        if (!showHeader) {
            return;
        }
        RenderUtil.fillRect(renderGraphics, 0, 0, lW, topNavHeight, Theming.now.mainColor, Theming.now.mainColor_);

        int rightX = perLineSpace + perLineSpace;
        if (KeyboardUtil.last != -1 && KeyboardUtil.caret != -1) {
            renderGraphics.setColor(Theming.now.onMainTextColor);
            renderGraphics.setFont(boldFont);
            char[] array = ((KeyboardUtil.lang / 2) == 0 ? KeyboardUtil.ENG12[KeyboardUtil.last] : KeyboardUtil.RUS12[KeyboardUtil.last]);
            for (int i = array.length - 1; i >= 0; --i) {
                if (KeyboardUtil.lang % 2 == 1) {
                    array[i] = Character.toUpperCase(array[i]);
                }
                int chw = boldFont.charWidth(array[i]);
                if (KeyboardUtil.caret == i) {
                    RenderUtil.fillRect(renderGraphics, lW - rightX - chw, topNavBoldTextY, chw, boldFont.getHeight(), Theming.now.focusedMainColor, Theming.now.focusedMainColor_);
                }
                renderGraphics.drawChar(array[i], lW - rightX, topNavBoldTextY, Graphics.RIGHT | Graphics.TOP);
                rightX += chw;
            }
            rightX += perLineSpace + perLineSpace;
        } else if (KeyboardUtil.last == 11 || KeyboardUtil.last == 10) {
            renderGraphics.setColor(Theming.now.onMainTextColor);
            renderGraphics.setFont(boldFont);
            String lang = "Bksp";
            if (KeyboardUtil.last == 11) {
                switch (KeyboardUtil.lang) {
                    case 0:
                        lang = "eng";
                        break;
                    case 1:
                        lang = "ENG";
                        break;
                    case 2:
                        lang = "рус";
                        break;
                    case 3:
                        lang = "РУС";
                        break;
                }
            }
            renderGraphics.drawString(lang, lW - rightX, topNavBoldTextY, Graphics.RIGHT | Graphics.TOP);
            rightX += perLineSpace + perLineSpace + boldFont.stringWidth(lang);
        } else if (showSpinner) {
            int rot;
            switch (spinnerFrame) {
                case 1:
                    rot = Sprite.TRANS_ROT90;
                    break;
                case 2:
                    rot = Sprite.TRANS_ROT180;
                    break;
                case 3:
                    rot = Sprite.TRANS_ROT270;
                    break;
                default:
                    rot = Sprite.TRANS_NONE;
                    break;
            }

            spinnerIcon = Theming.now.drawImage(renderGraphics,
                    lW - rightX, topNavHeight / 2,
                    spinnerIcon, "new/loading.rle",
                    Theming.now.onMainIconColor, Theming.now.onMainIconColor_, topNavIconHeight,
                    rot, Graphics.RIGHT | Graphics.VCENTER);
            rightX = perLineSpace + perLineSpace + topNavHeight + perLineSpace + perLineSpace;
        }

        int finalL = lW - perLineSpace - perLineSpace - rightX - (Midlet.instance.config.shiftTitleRight ? 22 : 0);

        if (content.cornerIcon != null) {
            RenderUtil.renderListItemIcon(renderGraphics, perLineSpace + perLineSpace + (Midlet.instance.config.shiftTitleRight ? 22 : 0), topNavBoldTextY, content.cornerIcon,
                    1,
                    Theming.now.onlineIconColor,
                    Theming.now.onlineIconColor, ListItem.cornerIconSize, Graphics.LEFT | Graphics.TOP);
            finalL -= ListItem.cornerIconSize + perLineSpace;
        }

        if (content != null && content.title != null && (content.renderTitle == null || content.lW != finalL)) {
            content.renderTitle = boldEmojiFont.limit(content.title, finalL, true);
            content.lW = finalL;
        }

        if (content != null && content.renderTitle != null) {
            renderGraphics.setColor(Theming.now.onMainTextColor);
            boldEmojiFont.drawString(renderGraphics, content.renderTitle, perLineSpace + perLineSpace + (Midlet.instance.config.shiftTitleRight ? 22 : 0) + (content.cornerIcon != null ? ListItem.cornerIconSize + perLineSpace : 0), topNavBoldTextY, Graphics.TOP | Graphics.LEFT);
        }
    }

    private void renderDock() {
        if (!showDock) {
            return;
        }

        RenderUtil.fillRect(renderGraphics, 0, bottomNavY, lW, bottomNavHeight, Theming.now.mainColor, Theming.now.mainColor_);
    }

    private void renderNavbar() {
        String rAction = isFieldSelected()
                ? (content.getSelectedField() instanceof ImField ? content.getRightSoft() : Localization.get("action.erase"))
                : (content != null ? content.getRightSoft() : null);

        boolean equal = (rightSoft.title == null && rAction == null) || (rightSoft.title != null && rAction != null && rAction.intern() == rightSoft.title);

        if (!equal) {
            if (rightSoft.lcduiCommand != null) {
                removeCommand(rightSoft.lcduiCommand);
            }
            rightSoft.setTitle(rAction);
            if (rightSoft.lcduiCommand != null && !this.showFooter) {
                addCommand(rightSoft.lcduiCommand);
            }
        }

        if (!this.showFooter) {
            return;
        }

        SoftButton rightSoft = this.rightSoft.title == null ? null : this.rightSoft;
        SoftButton leftSoft = reverseSoftButtons ? rightSoft : this.leftSoft;
        rightSoft = reverseSoftButtons ? this.leftSoft : rightSoft;

        RenderUtil.fillRect(renderGraphics, 0, bottomNavY, lW, bottomNavHeight, Theming.now.mainColor, Theming.now.mainColor_);

        int availableW = Math.max(0, lW / 2 - (clock == null ? 0 : boldFont.stringWidth(clock)) / 2 - perLineSpace * 2);

        //Compare strings by pointer to make comparsion faster
        if (leftSoft != null) {
            if (leftSoft.limitedTitle == null || leftSoft.limitedWidth != availableW) {
                leftSoft.limitedWidth = availableW;
                leftSoft.limitedTitle = PaneItem.limit(leftSoft.title, leftSoft.limitedWidth, true, normalFont);
            }

            int buttonW = Math.min(availableW, normalFont.stringWidth(leftSoft.limitedTitle) + perLineSpace * 4);

            if (leftSP) {
                RenderUtil.fillRect(renderGraphics,
                        0, bottomNavY,
                        buttonW, bottomNavHeight,
                        Theming.now.focusedMainColor, Theming.now.focusedMainColor_);
            }

            renderGraphics.setColor(Theming.now.onMainTextColor);
            renderGraphics.setFont(normalFont);
            renderGraphics.drawString(leftSoft.limitedTitle,
                    buttonW / 2, bottomNavY + bottomNavNormalTextY,
                    Graphics.TOP | Graphics.HCENTER);
        }

        if (rightSoft != null) {
            if (rightSoft.limitedTitle == null || rightSoft.limitedWidth != availableW) {
                rightSoft.limitedWidth = availableW;
                rightSoft.limitedTitle = PaneItem.limit(rightSoft.title, rightSoft.limitedWidth, true, normalFont);
            }

            int buttonW = Math.min(availableW, normalFont.stringWidth(rightSoft.limitedTitle) + perLineSpace * 4);

            if (rightSP) {
                RenderUtil.fillRect(renderGraphics,
                        getWidth() - buttonW, bottomNavY,
                        buttonW, bottomNavHeight,
                        Theming.now.focusedMainColor, Theming.now.focusedMainColor_);
            }

            renderGraphics.setColor(Theming.now.onMainTextColor);
            renderGraphics.setFont(normalFont);
            renderGraphics.drawString(rightSoft.limitedTitle,
                    getWidth() - buttonW / 2,
                    bottomNavY + bottomNavNormalTextY,
                    Graphics.TOP | Graphics.HCENTER);
        }

        if (clock != null) {
            renderGraphics.setColor(Theming.now.onMainTextColor);
            renderGraphics.setFont(boldFont);
            renderGraphics.drawString(
                    clock, getWidth() / 2, bottomNavY + bottomNavBoldTextY,
                    Graphics.TOP | Graphics.HCENTER);
        }
    }

    public void init(boolean resetGraphics) {
        //Reallocate memory
        if (resetGraphics) {
            itemsToLoad.removeAllElements();
            RenderUtil.clear();
            RenderUtil.init();
            Theming.now.clearCache();
            if (content != null) {
                content.screenCache.clear();
            }
        }
        justReinited = true;
        popupWidth = getWidth() * 2 / 3;

        if (resetGraphics || Midlet.instance.config.gui_disableDimm) {
            cachedDimm = null;
        }

        lW = getWidth();
        lH = getHeight();
        themeRecache();

        popupNow = null;
        renderImage = null;
        if (resetGraphics) {
            renderGraphics = null;
        }
        //bar = null;
        cache1 = null;
        cache2 = null;
        renderSprite = null;

        //Run garbage collector
        System.gc();

        //Base values
        //#ifdef CACHING
//#              renderImage = Image.createImage(getWidth(), getHeight());
//#              renderGraphics = renderImage.getGraphics();
//#              renderSprite = new Sprite(renderImage);
        //#endif

        //bar = new ProgressBar();
        //bar.width = getWidth();
        sizeChanged = false;
        reverseSoftButtons = Midlet.instance.config.gui_reverseSofts;
        touchHud = Midlet.instance.config.gui_touchHud;
        showHeader = Midlet.instance.config.gui_showHeader || touchHud || Midlet.instance.config.gui_fullscreen;
        showFooter = Midlet.instance.config.gui_showFooter || touchHud || Midlet.instance.config.gui_fullscreen;
        showDock = Midlet.instance.config.gui_showDock && touchHud;
        slimHeader = Midlet.instance.config.gui_slimHeader && touchHud;

        if (!(Midlet.instance.config.gui_useDrawer && touchHud)) {
            drawerBack = null;
            if (ContentController.menu != null) {
                ContentController.menu.parent(null);
            }
        }

        setCommandListener(showFooter ? null : new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                switch (c.getCommandType()) {
                    case Command.BACK:
                        if (!reverseSoftButtons) {
                            keyPressed(KEY_CODE_LEFT_MENU);
                            keyReleased(KEY_CODE_LEFT_MENU);
                        } else {
                            keyPressed(KEY_CODE_RIGHT_MENU);
                            keyReleased(KEY_CODE_RIGHT_MENU);
                        }
                        break;
                    default:
                        if (reverseSoftButtons) {
                            keyPressed(KEY_CODE_LEFT_MENU);
                            keyReleased(KEY_CODE_LEFT_MENU);
                        } else {
                            keyPressed(KEY_CODE_RIGHT_MENU);
                            keyReleased(KEY_CODE_RIGHT_MENU);
                        }
                        break;
                }
            }
        });

        if (showFooter) {
            if (rightSoft.lcduiCommand != null) {
                removeCommand(rightSoft.lcduiCommand);
            }
            if (leftSoft != null && leftSoft.lcduiCommand != null) {
                removeCommand(leftSoft.lcduiCommand);
            }
        } else {
            if (rightSoft.lcduiCommand != null) {
                addCommand(rightSoft.lcduiCommand);
            }
            if (leftSoft != null && leftSoft.lcduiCommand != null) {
                addCommand(leftSoft.lcduiCommand);
            }
        }

        bottomNavHeight = touchHud ? normalEmojiFont.height * 5 / 2 : normalEmojiFont.height * 3 / 2;
        topNavHeight = touchHud && !slimHeader ? normalEmojiFont.height * 5 / 2 : normalEmojiFont.height * 3 / 2;
        contentY = showHeader ? topNavHeight : 0;
        topNavIconHeight = topNavHeight - (touchHud && !slimHeader ? normalEmojiFont.height * 5 / 4 : normalEmojiFont.height / 4);

        bottomNavY = getHeight() - ((touchHud && showDock) || (!touchHud && showFooter) ? bottomNavHeight : 0);

        contentOriginalHeight = contentHeight = lH - contentY;

        if ((touchHud && showDock) || (!touchHud && showFooter)) {
            contentOriginalHeight -= bottomNavHeight; //minus navbar height
        }
        scrollWidth = normalEmojiFont.height / 4;
        scrollX = getWidth() - scrollWidth;
        bottomNavNormalTextY = (bottomNavHeight// - bar.height
                - normalEmojiFont.height) / 2;
        bottomNavBoldTextY = (bottomNavHeight// - bar.height
                - boldEmojiFont.height) / 2;
        topNavBoldTextY = (topNavHeight// - bar.height
                - boldEmojiFont.height) / 2;
        slAnimDir = false;
        slAnim = new AnimationInterp(AnimationInterp.INTERP_INVSQR); //Смена контейнеров экрана
        completeCAnim();
        animC = null;
        scrollOpaq = new AnimationInterp(AnimationInterp.INTERP_INVSQR);
        //TODO if (content != null) content.clearItemCache();
        if (content != null) {
            content.contentHeight = contentOriginalHeight;
            setTitle(content.title == null ? "VK4ME" : content.title.toString());
        }
        fingerDist = (sqr(getWidth()) + sqr(getHeight())) / (160 * 160 + 128 * 128);

        //#ifdef CACHING
//#              renderContent();
        //#endif

        if (!started) {
            new Timer().schedule(new TimerTask() {
                public void run() {
                    try {
                        if (!Midlet.instance.config.sleepWithTimeout) {
                            sleepTime = 0;
                        } else {
                            if (sleepTime > Midlet.instance.config.sleepTimeout) {
                                //!LongPoll.slowMode = guard from repeating
                                if (!LongPoll.slowMode && Midlet.instance.config.setOffline && VKConstants.account != null) {
                                    LongPoll.slowMode = true;
                                    VKConstants.account.setOffline();
                                }
                                LongPoll.slowMode = true;
                                if (Midlet.instance.config.debug_lp) {
                                    render();
                                }
                            } else if (lastField == null) {
                                ++sleepTime;
                            }
                        }
                    } catch (Exception e) {
                    }
                    try {
                        if (lastShow != -1) {
                            if (lastShow == 0) {
                                if (Midlet.instance.config.gui_animations) {
                                    scrollOpaq.endValue = 0;
                                    scrollOpaq.startContinue(100);
                                } else {
                                    scrollOpaq.value = 0;
                                }

                                render();
                            }
                            lastShow--;
                        }
                        if (lastClock != System.currentTimeMillis() / 60000) {
                            lastClock = System.currentTimeMillis() / 60000;
                            clock = TextUtil.getTimeString(System.currentTimeMillis() / 1000, false);
                            render();
                        }
                    } catch (Throwable e) {
                        Logger.l(e);
                    }
                    try {
                        if (ConversationList.instance != null) {
                            ConversationList.instance.updateTyping();
                        }
                    } catch (Exception e) {
                        Logger.l(e);
                    }
                    try {
                        Content msgContent = MessageContent.getMessageContent();
                        if (msgContent instanceof MessageContent) {
                            ((MessageContent) msgContent).updateTypingState();
                            ((MessageContent) msgContent).updateLabel();
                            ((MessageContent) msgContent).updateTyping();
                        }
                        //if (content instanceof PlayerContent && (((PlayerContent) content).isPlaying() || ((PlayerContent) content).caching || ((PlayerContent) content).hideTime > 0)) {
                        //    ((PlayerContent) content).update(true);
                        //}
                    } catch (Throwable e) {
                        Logger.l(e);
                    }
                    try {
                        Field field = popupOpened() ? popupNow.getSelectedField() : content != null ? content.getSelectedField() : null;
                        if (field instanceof PasswordField) {
                            PasswordField pf = (PasswordField) field;
                            int ov = pf.showLastChar;
                            pf.showLastChar();
                            if (ov > 0 && pf.showLastChar == 0) {
                                render();
                            }
                        }
                    } catch (Throwable e) {
                        Logger.l(e);
                    }
                }
            }, 0, 1000);
            new Timer().schedule(new TimerTask() {
                public void run() {
                    try {
                        spinnerFrame = (spinnerFrame + 1) % 4;
                        if (showSpinner) {
                            render();
                        }
                        if (KeyboardUtil.timeoutTicks > 0) {
                            KeyboardUtil.timeoutTicks--;
                            if (KeyboardUtil.timeoutTicks == 0) {
                                KeyboardUtil.caret = KeyboardUtil.last = -1;
                            }
                            render();
                        }
                    } catch (Throwable e) {
                        Logger.l(e);
                    }
                }
            }, 0, 200);
            new Thread() {
                public void run() {
                    Midlet.instance.config.tryAuth();
                    stopSplash();
                }
            }.start();
            started = true;
        }
    }

    protected void pointerPressed(int x, int y) {
//        if (showSpinner) {
//            return;
//        }
        LongPoll.slowMode = false;
        sleepTime = 0;

        if (!hasTouch) {
            hasTouch = true;
            init(false);
        }

        //super.pointerPressed(x, y);
        showSelection = false;
        pressed = true;
        fX = x;
        fY = y;
        lX = x;
        lY = y;
        pX = x;
        pY = y;

        if (popupOpened()) {
            popupNow.pointerPressed(x, y);
            render();
            return;
        }

        if ((y >= bottomNavY && !touchHud && showFooter) || (y < topNavHeight && touchHud)) {
            int buttonW = touchHud ? topNavHeight : getWidth() / 2;
            fingerOnNavBar = true;

            if (x < buttonW) {
                leftSP = true;
            } else if (x >= getWidth() - buttonW) {
                rightSP = true;
            }

        } else if (y < contentY) {
            fingerOnOtherBar = true;
        } else {
            fingerOnNavBar = false;
            fingerOnOtherBar = false;

            if (content != null) {
                content.pointerPressed(x, y);
            }
        }

        render();
    }

    protected void pointerReleased(int x, int y) {
//        if (showSpinner) {
//            return;
//        }
        LongPoll.slowMode = false;
        sleepTime = 0;

        if (!hasTouch) {
            hasTouch = true;
            init(false);
        }

        //super.pointerReleased(x, y);
        pressed = false;
        lX = pX;
        lY = pY;
        pX = x;
        pY = y;

        if (popupOpened()) {
            popupNow.pointerReleased(x, y);
            render();
            return;
        }

        int buttonW = touchHud ? topNavHeight : getWidth() / 2;
        boolean onNavOrHeader = (y >= bottomNavY && !touchHud && showFooter) || (y < topNavHeight && touchHud);

        if (leftSP) {
            leftSP = false;

            if (onNavOrHeader && x < buttonW && leftSoft != null) {
                if (touchHud || !reverseSoftButtons) {
                    if (leftSoft != null) {
                        leftSoft.trigger();
                    }
                } else {
                    if (isFieldSelected() && !touchHud) {
                        KeyboardUtil.keyPressed(BACKSPACE, content.getSelectedField());
                    } else {
                        if (popupNow != null) {
                            popupNow.open = false;
                        }
                        if (rightSoft != null) {
                            rightSoft.trigger();
                        }
                    }
                }
            }
        } else if (rightSP) {
            rightSP = false;

            if (onNavOrHeader && x >= getWidth() - buttonW) {
                if (reverseSoftButtons && !touchHud) {
                    if (leftSoft != null) {
                        leftSoft.trigger();
                    }
                } else {
                    if (isFieldSelected() && !touchHud) {
                        KeyboardUtil.keyPressed(BACKSPACE, content.getSelectedField());
                    } else {
                        if (popupNow != null) {
                            popupNow.open = false;
                        }
                        if (rightSoft != null) {
                            rightSoft.trigger();
                        }
                    }
                }
            }
        } else if (!fingerOnNavBar && !fingerOnOtherBar) {
            if (content != null) {
                content.pointerReleased(x, y);
            }
        }

        render();
    }

    protected void pointerDragged(int x, int y) {
//        if (showSpinner) {
//            return;
//        }
        LongPoll.slowMode = false;
        sleepTime = 0;

        if (!hasTouch) {
            hasTouch = true;
            init(false);
        }

        //super.pointerDragged(x, y);
        pressed = true;
        lX = pX;
        lY = pY;
        pX = x;
        pY = y;

        if (popupOpened()) {
            showScroll();
            popupNow.pointerDragged(x, y);
            render();
            return;
        }

        if (content != null && !fingerOnNavBar && !fingerOnOtherBar) {
            showScroll();
            content.pointerDragged(x, y);
        }


        render();
    }

    private void renderBackground() {
        renderGraphics.setColor(Theming.now.backgroundColor);
        renderGraphics.fillRect(0, 0, getWidth(), getHeight());
    }

    private void renderForeground() {
        boolean reRender = false;

        if (content != null && contentHeight < content.totalHeight) {
            if (scrollOpaq.started) {
                reRender = true;
                if (scrollOpaq.update()) {
                    if (scrollOpaq.value <= 0) {
                        lastShow = -1;
                    } else if (scrollOpaq.value >= 6) {
                        lastShow = 1;
                    }
                }

            }

            if (scrollOpaq.value != 0) {
                Image scrollImage = RenderUtil.renderScroll(Theming.now.scrollColor,
                        Math.max(lW, lH), scrollOpaq.value);
                if (scrollImage != null) {
                    renderGraphics.drawRegion(
                            scrollImage, 0, 0,
                            scrollWidth,
                            contentHeight * contentHeight / content.totalHeight,
                            0,
                            scrollX,
                            contentY + content.scrollY * contentHeight / content.totalHeight,
                            Graphics.LEFT | Graphics.TOP);
                }
            }
        }

        if (reRender) {
            render();
        }
    }

    private void renderContent() {
        if (content != null) {
            //#ifndef DIRECT_SLIDE
            if (slAnim.started) {
                if (cache1 == null) {
                    cache1 = Image.createImage(getWidth(), contentOriginalHeight);
                    int contentYold = contentY;
                    contentY = 0;
                    content.paint(cache1.getGraphics(), 0);
                    contentY = contentYold;
                }

                renderGraphics.drawImage(cache1, slAnim.value, contentY, Graphics.TOP | Graphics.LEFT);
            } else {
                content.paint(renderGraphics, slAnim.value);
            }
            //#else
//#              content.paint(renderGraphics, slAnim.getValue());
            //#endif
        }
        if (animC != null) {
            //#ifndef DIRECT_SLIDE

            int animContentX = slAnim.value + (slAnimDir ? getWidth() : -getWidth());

            if (slAnim.started) {
                if (cache2 == null) {
                    cache2 = Image.createImage(getWidth(), contentOriginalHeight);
                    int contentYold = contentY;
                    contentY = 0;
                    animC.paint(cache2.getGraphics(), 0);
                    contentY = contentYold;
                }

                renderGraphics.drawImage(cache2, animContentX, contentY, Graphics.TOP | Graphics.LEFT);
            } else {
                animC.paint(renderGraphics, animContentX);
            }
            //#else
//#              animC.paint(renderGraphics,animContentX);
            //#endif
        }

        boolean needRepaint = slAnim.started;

        if (slAnim.started) {
            if (slAnim.update()) {
                completeCAnim();
            }
        }

        if (needRepaint) {
            render();
        }
    }

    public void goTo(Content c, boolean back) {
        completeCAnim();

        if (c == null || (content != null && content.equals(c))) {
            return;
        }

        if (content instanceof ConversationList) {
            ((ConversationList) content).forward(null, 0);
        }

        drawerBack = null;

        if (content == null) {
            content = c;
            MessageContent.MESSAGE_CONTENT = null;
            content.opened();
            setTitle(content.title == null ? "VK4ME" : content.title.toString());
            completeCAnim();
            return;
        }

        if (!(content instanceof ConversationList) && (ContentController.menu == null || !ContentController.menu.equals(content))) {
            content.screenCache.clear();
        }
        System.gc();

        if (Midlet.instance.config.gui_animations) {
            animC = c;
            slAnimDir = !back;
            slAnim.endValue = ((back ? 1 : -1) * getWidth());
            slAnim.start(200);
        } else {
            content = c;
            MessageContent.MESSAGE_CONTENT = null;
            content.opened();
            setTitle(content.title == null ? "VK4ME" : content.title.toString());
        }


        if (content instanceof FilePicker) {
            ((FilePicker) content).reloadContent();
        }
        render();
    }

    public void completeCAnim() {
        if (!Midlet.instance.config.gui_animations) {
            return;
        }

        if (animC != null) {
            content = animC;
            content.opened();
            setTitle(content.title == null ? "VK4ME" : content.title.toString());
        }

        slAnim.endValue = 0;
        slAnim.end();
        slAnimDir = false;
        animC = null;
        cache1 = null;
        cache2 = null;

        render();
    }

    public void inverseCAnimDirection() {
        if (!slAnim.started) {
            return;
        }

        slAnimDir ^= true;
        slAnim.endValue = (getWidth() * (slAnimDir ? -1 : 1));
    }

    public void backTo(Content c) {
        goTo(c, true);
    }

    public void goTo(Content c) {
        goTo(c, false);
    }

    public void showScroll() {
        if (Midlet.instance.config.gui_animations) {
            scrollOpaq.endValue = 6;
            scrollOpaq.startContinue(100);
        } else {
            scrollOpaq.value = 6;
            lastShow = 1;
        }
    }

    public void showField(Field aThis) {
        if (aThis == null) {
            return;
        }
        lastField = aThis;

        textBox = new TextBox(aThis.caption == null ? "VK4ME" : aThis.caption.toString(), aThis.text == null ? "" : aThis.text.toString(), (Midlet.instance.config.increaseTextLimit ? MAX_TEXT_LENGTH : 500), TextField.ANY);

        textBox.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (lastField != null && textBox != null) {
                    lastField.setText(textBox.getString());
                }
                AppCanvas.instance.showGraphics();
            }
        });

        textBox.addCommand(new Command(Localization.get("action.ok"), Command.OK, 1));
        textBox.addCommand(new Command(Localization.get("action.close"), Command.BACK, 1));

        Display.getDisplay(Midlet.instance).setCurrent(textBox);
    }

    public void showGraphics() {
        if (popupNow != null) {
            popupNow.renderShadow = popupNow.open;
        }
        setFullScreenMode(Midlet.instance.config == null ? true : Midlet.instance.config.gui_fullscreen);
        Display.getDisplay(Midlet.instance).setCurrent(this);
        render();

        lastField = null;
        textBox = null;
        //System.gc();
    }

    protected void keyPressed(int key) {
//        if (showSpinner) {
//            return;
//        }
        LongPoll.slowMode = false;
        sleepTime = 0;

        key = getExtGameAct(key);
        showSelection = showSelection || key == AppCanvas.DOWN || key == AppCanvas.UP;

        if (popupOpened()) {
            if (popupNow.isFieldSelected() && KeyboardUtil.isAccepted(key)) {
                KeyboardUtil.keyPressed(key, popupNow.getSelectedField());
            } else {
                popupNow.keyPressed(key);
            }
            render();
            return;
        }

        //TODO switch-case
        if (key == -11) {
            if (content != null && content.parent != null) {
                backTo(content.parent);
            }
        } else if (key == KEY_CODE_LEFT_MENU) {
            leftSP = true;

            if (!(!touchHud && reverseSoftButtons)) {
                if (leftSoft != null) {
                    leftSoft.trigger();
                }
            } else {
                if (isFieldSelected()) {
                    KeyboardUtil.keyPressed(BACKSPACE, content.getSelectedField());
                } else {
                    if (popupNow != null) {
                        popupNow.open = false;
                    }
                    if (rightSoft != null) {
                        rightSoft.trigger();
                    }
                }
            }
        } else if (key == KEY_CODE_RIGHT_MENU) {
            rightSP = true;

            if (!touchHud && reverseSoftButtons) {
                if (leftSoft != null) {
                    leftSoft.trigger();
                }
            } else {
                if (isFieldSelected()) {
                    KeyboardUtil.keyPressed(BACKSPACE, content.getSelectedField());
                } else {
                    if (popupNow != null) {
                        popupNow.open = false;
                    }
                    if (rightSoft != null) {
                        rightSoft.trigger();
                    }
                }
            }
        } else if (key == LEFT) {
            if (content != null) {
                content.keyPressed(key);
            }
        } else if (key == RIGHT) {
            if (content != null) {
                content.keyPressed(key);
            }
        } else if (key == UP || (key == KEY_NUM2 && !isFieldSelected())) {
            if (content != null) {
                content.keyPressed(key);
            }
        } else if (key == DOWN || (key == KEY_NUM8 && !isFieldSelected())) {
            if (content != null) {
                content.keyPressed(key);
            }
        } else if (isFieldSelected() && KeyboardUtil.isAccepted(key)) {
            KeyboardUtil.keyPressed(key, content.getSelectedField());
        } else if (key == BACKSPACE && isPQSofts()) {
            if (leftSoft != null) {
                leftSoft.trigger();
            }
        } else if (content != null) {
            content.keyPressed(key);
        }

        render();
    }

    protected void keyRepeated(int key) {
//        if (showSpinner) {
//            return;
//        }
        LongPoll.slowMode = false;
        sleepTime = 0;

        key = getExtGameAct(key);

        if (popupOpened()) {
            if (popupNow.isFieldSelected() && KeyboardUtil.isAccepted(key)) {
                KeyboardUtil.keyRepeated(key, popupNow.getSelectedField());
            } else {
                popupNow.keyRepeated(key);
            }
            render();
            return;
        }

        //TODO switch-case
        if (key == -11) {
            if (VKConstants.account != null) {
                ContentController.showMenu();
            }
        } else if (key == LEFT) {
            if (content != null) {
                content.keyRepeated(key);
            }
        } else if (key == RIGHT) {
            if (content != null) {
                content.keyRepeated(key);
            }
        } else if (key == UP) {
            if (content != null) {
                content.keyRepeated(key);
            }
        } else if (key == DOWN) {
            if (content != null) {
                content.keyRepeated(key);
            }
        } else if (key == KEY_CODE_RIGHT_MENU) {
            rightSP = true;

            if (!(!touchHud && reverseSoftButtons)) {
                if (isFieldSelected()) {
                    KeyboardUtil.keyRepeated(AppCanvas.BACKSPACE, content.getSelectedField());
                }
            } else {
                if (VKConstants.account != null) {
                    ContentController.showMenu();
                }
            }
        } else if (key == KEY_CODE_LEFT_MENU) {
            leftSP = true;

            if (!touchHud && reverseSoftButtons) {
                if (isFieldSelected()) {
                    KeyboardUtil.keyRepeated(AppCanvas.BACKSPACE, content.getSelectedField());
                }
            } else {
                if (VKConstants.account != null) {
                    ContentController.showMenu();
                }
            }
        } else if (isFieldSelected() && KeyboardUtil.isAccepted(key)) {
            KeyboardUtil.keyRepeated(key, content.getSelectedField());
        } else if (key == BACKSPACE && isPQSofts()) {
            if (VKConstants.account != null) {
                ContentController.showMenu();
            }
        } else if (content != null) {
            content.keyRepeated(key);
        }

        render();
    }

    protected void keyReleased(int key) {
//        if (showSpinner) {
//            return;
//        }
        LongPoll.slowMode = false;
        sleepTime = 0;

        key = getExtGameAct(key);

        //TODO switch-case
        if (key == KEY_CODE_LEFT_MENU) {
            leftSP = false;
        } else if (key == KEY_CODE_RIGHT_MENU) {
            rightSP = false;
        }

        if (popupOpened()) {
            if (!popupNow.isFieldSelected()) {
                popupNow.keyReleased(key);
            }
            render();
            return;
        }

        if (key == -11) {
        } else if (key == KEY_CODE_LEFT_MENU) {
            leftSP = false;
        } else if (key == KEY_CODE_RIGHT_MENU) {
            rightSP = false;
        } else if (key == FIRE) {
            if (content != null) {
                content.keyReleased(key);
            }
        } else if (content != null && (!isFieldSelected() || !KeyboardUtil.isAccepted(key))) {
            content.keyReleased(key);
        }

        render();
    }
    public static final int BACKSPACE = 1000010, ENTER = 1000011, SHIFT = -50, SPACE = 32;

    public int getExtGameAct(int keyCode) {
        if (keyCode == -11) {
            return -11;
        }
        try {
            String strCode = getKeyName(keyCode).toLowerCase();
            if ("soft1".equals(strCode) || "soft 1".equals(strCode) || "soft_1".equals(strCode) || "softkey 1".equals(strCode) || "sk2(left)".equals(strCode)
                    || strCode.startsWith("left soft")) {
                return KEY_CODE_LEFT_MENU;
            }
            if ("soft2".equals(strCode) || "soft 2".equals(strCode) || "soft_2".equals(strCode) || "softkey 4".equals(strCode) || "sk1(right)".equals(strCode)
                    || strCode.startsWith("right soft")) {
                return KEY_CODE_RIGHT_MENU;
            }
            if ("on/off".equals(strCode) || "back".equals(strCode)) {
                return KEY_CODE_BACK_BUTTON;
            }
            if ("trackball".equals(strCode) || "select".equals(strCode)) {
                return FIRE;
            }
            if ("enter".equals(strCode)) {
                return ENTER;
            }
            if ("backspace".equals(strCode) || "clear".equals(strCode)) {
                return BACKSPACE;
            }
        } catch (Throwable excepted) {
        }
        if (isPQSofts()) {
            if (keyCode == 113 || keyCode == 81) {
                return KEY_CODE_LEFT_MENU;
            } else if (keyCode == 112 || keyCode == 80) {
                return KEY_CODE_RIGHT_MENU;
            }
            if (keyCode == 42) {
                return KEY_CODE_LEFT_MENU;
            } else if (keyCode == 35) {
                return KEY_CODE_RIGHT_MENU;
            }
        }
        switch (keyCode) {
            case -6:
            case -21:
            case 21:
            case -202:
            case 57345:
                return KEY_CODE_LEFT_MENU;
            case -7:
            case -22:
            case 22:
            case -203:
            case 57346:
                return KEY_CODE_RIGHT_MENU;
            case -8:
                return BACKSPACE;
            default:
                try {
                    int gameAct = getGameAction(keyCode);
                    switch (gameAct) {
                        case Canvas.UP:
                        case Canvas.DOWN:
                        case Canvas.LEFT:
                        case Canvas.RIGHT:
                            return KeyboardUtil.isAccepted(keyCode) ? keyCode : gameAct;
                        case Canvas.FIRE:
                            return KeyboardUtil.isAccepted(keyCode) ? keyCode : gameAct;
                        default:
                            return keyCode;
                    }
                } catch (Throwable excepted) {
                    return keyCode;
                }
        }
    }

    public void render() {
        //!LongPoll.slowMode && 
        if (!Midlet.instance.paused && isShown()) {
            repaint();
        }
    }

    private boolean isFieldSelected() {
        return ((content != null && content.isFieldSelected())
                || (popupOpened() && popupNow.isFieldSelected()));
    }

    private boolean isPQSofts() {
        return Midlet.instance.config.usePQSofts && !isFieldSelected() && (!(content instanceof MessageContent) || !((MessageContent) content).msgField.opened) && (!(content instanceof CommentContent) || !((CommentContent) content).msgField.opened);
    }

    public static Image loadImageURL(String s) throws Exception {
        if (s == null) {
            return null;
        }
        HttpConnection conn = null;
        Image imgg = null;
        conn = (HttpConnection) HTTPClient.openHttpConnection(s, true);
        InputStream is = conn.openInputStream();
        imgg = Image.createImage(is);

        is.close();
        conn.close();

        return imgg;
    }

    public static Image loadLocalUnsafe(String url) throws Exception {
        if (url == null) {
            return null;
        }
        return Image.createImage("/res/" + url);
    }

    public static Image loadLocal(String url) {
        try {
            return loadLocalUnsafe(url);
        } catch (Exception e) {
        }

        return null;
    }

    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    public void setProgress(boolean p) {
        showSpinner = p;
        render();
    }

    public void showPopup(PopupMenu popup, int x, int y, int anchor) {
        if (popup == null) {
            return;
        }
        closePopup();
        popup.open = false;
        popupNow = popup;
        popupNow.actionPerformed(x, y, anchor);
        render();
    }

    public void showPopup(PopupMenu popup) {
        showPopup(popup, lW / 2, lH / 2, Graphics.HCENTER | Graphics.VCENTER);
    }

    public void showRightsoftPopup(PopupMenu popup) {
        if (Midlet.instance.config.gui_touchHud) {
            showPopup(popup,
                    lW / 2, lH / 2,
                    Graphics.HCENTER | Graphics.VCENTER);
        } else {
            if (reverseSoftButtons && showFooter) {
                showPopup(popup,
                        0, bottomNavY,
                        Graphics.LEFT | Graphics.BOTTOM);
            } else {
                showPopup(popup,
                        lW, bottomNavY,
                        Graphics.RIGHT | Graphics.BOTTOM);
            }
        }
    }

    public void dropMessage(String header, String error) {
        dropMessage(header, error, null);
    }

    public void dropMessage(String header, String error, final Runnable run) {
        if (error != null && VKConstants.account != null) {
            error = TextUtil.easyReplace(error, VKConstants.account.getToken(), "*censored*");
        }

        if (header == null) {
            header = Localization.get("general.error");
        }

        Logger.l(header + ": " + error);

        PopupMenu err = new PopupMenu();

        err.add(new Label(header).setFont(true).skipSelection(true));
        err.add(new Label(error).skipSelection(true));

        err.add(new PopupButton(Localization.get("action.close")) {
            public void actionPerformed() {
                closePopup();
                if (run != null) {
                    run.run();
                }
            }
        }.setFont(true));

        showPopup(err);
        err.selectedY = 2;
    }

    public void dropError(String error) {
        dropMessage(null, error);
    }

    public void dropError(Throwable ex) {
        if (ex == null) {
            return;
        }
        Logger.l(ex);
        dropError(ex.toString() + ": " + ex.getMessage());
    }

    public void closePopup() {
        if (popupNow != null) {
            popupNow.open = false;
        }
        popupNow = null;
    }

    public int popupY() {
        return popupNow != null ? popupNow.y : 0;
    }

    public int popupHeight() {
        return popupNow != null ? popupNow.getHeight() : 0;
    }

    private void renderDimm() {
        if (Midlet.instance.config.gui_disableDimm) {
            return;
        }

        //TODO: fill with nokia ui
        if (cachedDimm == null) {
            cacheDimm();
        }

        for (int y = 0; y < lH; y += 10) {
            renderGraphics.drawImage(cachedDimm, 0, y, 0);
        }
    }
    static Player p;

    public static void playIncomingSound() {
        try {
            if (p == null) {
                p = Manager.createPlayer(Runtime.getRuntime().getClass().getResourceAsStream("/bb2.mp3"), DownloadUtils.getMP3Type());
                p.realize();
                p.prefetch();
            }
            p.start();
        } catch (Throwable e) {
            Logger.l(e);
        }
    }

    private void renderSplash() {
        if (!splash) {
            return;
        }

        RenderUtil.fillRect(renderGraphics, 0, 0, getWidth(), getHeight(), Theming.now.mainColor, Theming.now.mainColor_);

        splashLogo = Theming.now.drawImage(renderGraphics,
                getWidth() / 2, getHeight() / 2, splashLogo, "new/logo-outline.rle",
                Theming.now.onMainIconColor,
                Theming.now.onMainIconColor_,
                Math.min(getWidth() / 3, getHeight() / 3),
                Graphics.VCENTER | Graphics.HCENTER);
    }

    public static double sqr(double e) {
        return e * e;
    }

    public static long round(double e) {
        double floor = Math.floor(e);
        if (e - floor < 0.5d) {
            return (long) floor;
        } else {
            return (long) Math.ceil(e);
        }
    }

    public boolean popupOpened() {
        return popupNow != null && popupNow.active && popupNow.open;
    }

    public void themeRecache() {
        RenderUtil.scrollBuffers = null;

        if (!Midlet.instance.config.gui_disableDimm
                && (cachedDimm == null
                || cachedDimmColor != Theming.now.dimmColor
                || cachedDimm.getWidth() != lW)) {
            cacheDimm();
        }

        ListItem.fullCircleImage = RenderUtil.circlify(
                ListItem.fullHeightAvatar,
                ListItem.fullHeightAvatar,
                ListItem.fullHeightAvatar * Midlet.instance.config.gui_photosCircleType / 200,
                Theming.now.backgroundColor,
                Theming.now.backgroundColor,
                RenderUtil.ALL,
                (ListItem.fullHeight - ListItem.fullHeightAvatar) / 2,
                ListItem.fullHeight,
                false);
        ListItem.fullCircleImageSelected = RenderUtil.circlify(
                ListItem.fullHeightAvatar,
                ListItem.fullHeightAvatar,
                ListItem.fullHeightAvatar * Midlet.instance.config.gui_photosCircleType / 200,
                Theming.now.focusedBackgroundColor,
                Theming.now.focusedBackgroundColor_,
                RenderUtil.ALL,
                (ListItem.fullHeight - ListItem.fullHeightAvatar) / 2,
                ListItem.fullHeight,
                false);
        ListItem.fullCircleImageUnreaded = RenderUtil.circlify(
                ListItem.fullHeightAvatar,
                ListItem.fullHeightAvatar,
                ListItem.fullHeightAvatar * Midlet.instance.config.gui_photosCircleType / 200,
                Theming.now.unreadBackgroundColor,
                Theming.now.unreadBackgroundColor_,
                RenderUtil.ALL,
                (ListItem.fullHeight - ListItem.fullHeightAvatar) / 2,
                ListItem.fullHeight,
                false);
        ListItem.smallCircleImage = RenderUtil.circlify(
                ListItem.smallHeightAvatar,
                ListItem.smallHeightAvatar,
                ListItem.smallHeightAvatar * Midlet.instance.config.gui_photosCircleType / 200,
                Theming.now.backgroundColor,
                Theming.now.backgroundColor,
                RenderUtil.ALL,
                (ListItem.smallHeight - ListItem.smallHeightAvatar) / 2,
                ListItem.smallHeight,
                false);
        ListItem.smallCircleImageSelected = RenderUtil.circlify(
                ListItem.smallHeightAvatar,
                ListItem.smallHeightAvatar,
                ListItem.smallHeightAvatar * Midlet.instance.config.gui_photosCircleType / 200,
                Theming.now.focusedBackgroundColor,
                Theming.now.focusedBackgroundColor_,
                RenderUtil.ALL,
                (ListItem.smallHeight - ListItem.smallHeightAvatar) / 2,
                ListItem.smallHeight,
                false);
        ListItem.smallCircleImageUnreaded = RenderUtil.circlify(
                ListItem.smallHeightAvatar,
                ListItem.smallHeightAvatar,
                ListItem.smallHeightAvatar * Midlet.instance.config.gui_photosCircleType / 200,
                Theming.now.unreadBackgroundColor,
                Theming.now.unreadBackgroundColor_,
                RenderUtil.ALL,
                (ListItem.smallHeight - ListItem.smallHeightAvatar) / 2,
                ListItem.smallHeight,
                false);

        AppCanvas.tranSizeStatic = (AppCanvas.instance.normalEmojiFont.height + AppCanvas.instance.perLineSpace) / 2;
        AppCanvas.tranSize = AppCanvas.tranSizeStatic * Midlet.instance.config.gui_avatarCircleType / 100;
        AppCanvas.tranImageUnread = RenderUtil.circlify(
                AppCanvas.tranSizeStatic,
                AppCanvas.tranSizeStatic,
                AppCanvas.tranSize,
                Theming.now.unreadBackgroundColor,
                Theming.now.unreadBackgroundColor,
                RenderUtil.TL, false);
        AppCanvas.tranImageStandard = RenderUtil.circlify(
                AppCanvas.tranSizeStatic,
                AppCanvas.tranSizeStatic,
                AppCanvas.tranSize,
                Theming.now.backgroundColor,
                Theming.now.backgroundColor,
                RenderUtil.TL, false);
        AppCanvas.outBorderImage = RenderUtil.genBorderOutline(AppCanvas.tranSize, Theming.now.userMessageBorderColor);
        AppCanvas.borderImage = RenderUtil.genBorderOutline(AppCanvas.tranSize, Theming.now.somebodyMessageBorderColor);
        AppCanvas.selBorderImage = RenderUtil.genBorderOutline(AppCanvas.tranSize, Theming.now.focusedBorderColor);

        ListItem.cornerBgImage = RenderUtil.circlify(
                ListItem.cornerBgSize,
                ListItem.cornerBgSize,
                ListItem.cornerBgSize / 2,
                Theming.now.onlineBackgroundColor,
                Theming.now.onlineBackgroundColor,
                RenderUtil.ALL, true);

        ListItem.unreadCorner = RenderUtil.circlify(
                ListItem.unreadCornerSize,
                ListItem.unreadCornerSize,
                ListItem.unreadCornerSize * Midlet.instance.config.gui_avatarCircleType / 200,
                Theming.now.backgroundColor,
                Theming.now.backgroundColor,
                RenderUtil.ALL,
                normalEmojiFont.height * 7 / 4 - perLineSpace / 2,
                ListItem.fullHeight,
                false);
        ListItem.unreadCornerSelected = RenderUtil.circlify(
                ListItem.unreadCornerSize,
                ListItem.unreadCornerSize,
                ListItem.unreadCornerSize * Midlet.instance.config.gui_avatarCircleType / 200,
                Theming.now.focusedBackgroundColor,
                Theming.now.focusedBackgroundColor_,
                RenderUtil.ALL,
                normalEmojiFont.height * 7 / 4 - perLineSpace / 2,
                ListItem.fullHeight,
                false);
        ListItem.unreadCornerUnreaded = RenderUtil.circlify(
                ListItem.unreadCornerSize,
                ListItem.unreadCornerSize,
                ListItem.unreadCornerSize * Midlet.instance.config.gui_avatarCircleType / 200,
                Theming.now.unreadBackgroundColor,
                Theming.now.unreadBackgroundColor_,
                RenderUtil.ALL,
                normalEmojiFont.height * 7 / 4 - perLineSpace / 2,
                ListItem.fullHeight,
                false);

        PopupMenu.circleSize = boldFont.getHeight() * Midlet.instance.config.gui_avatarCircleType / 200;
        PopupMenu.circleImage = RenderUtil.circlify(
                PopupMenu.circleSize,
                PopupMenu.circleSize,
                PopupMenu.circleSize,
                Theming.now.popupColor,
                Theming.now.popupColor,
                RenderUtil.TL,
                true);

        Slider.buttonHeight = Math.max(2, normalFont.getHeight() & (~1));
        Slider.button = RenderUtil.circlify(
                Slider.buttonHeight,
                Slider.buttonHeight,
                Slider.buttonHeight / 2,
                Theming.now.sliderButtonColor,
                Theming.now.sliderButtonColor_,
                RenderUtil.ALL,
                true);
        Slider.buttonBorder = RenderUtil.genBorderOutline(
                Slider.buttonHeight / 2,
                Theming.now.sliderButtonBorderColor);
        Slider.focusedButton = RenderUtil.circlify(
                Slider.buttonHeight,
                Slider.buttonHeight,
                Slider.buttonHeight / 2,
                Theming.now.focusedSliderButtonColor,
                Theming.now.focusedSliderButtonColor_,
                RenderUtil.ALL,
                true);
        Slider.focusedButtonBorder = RenderUtil.genBorderOutline(
                Slider.buttonHeight / 2,
                Theming.now.focusedSliderButtonBorderColor);

        Field.circleSize = (normalFont.getHeight() + perLineSpace + perLineSpace) * Midlet.instance.config.gui_avatarCircleType / 200;
        Field.borderImage = RenderUtil.genBorderOutline(Field.circleSize, Theming.now.borderColor);
        Field.borderImageSelected = RenderUtil.genBorderOutline(Field.circleSize, Theming.now.focusedBorderColor);
    }

    private void cacheDimm() {
        int[] dimm = new int[lW * 10];

        for (int i = 0; i < dimm.length; i++) {
            dimm[i] = (0x99000000 | Theming.now.dimmColor);
        }

        cachedDimm = Image.createRGBImage(dimm, lW, 10, true);
    }

    public void queue(ImageProvider provider) {
        queue(provider, -1);
    }

    public void queue(ImageProvider provider, int i) {
        if (Midlet.instance.config.doNotLoadImages || provider == null) {
            return;
        }

        if (i < 0 || i >= itemsToLoad.size()) {
            i = -1;
        }

        if (!itemsToLoad.contains(provider) && provider.tries() < 5) {
            if (i == -1) {
                itemsToLoad.addElement(provider);
            } else {
                itemsToLoad.insertElementAt(provider, i);
            }
        }
        if (thr == null || !thr.isAlive()) {
            thr = new Thread() {
                public void run() {
                    while (!itemsToLoad.isEmpty()) {
                        ImageProvider item = (ImageProvider) itemsToLoad.firstElement();
                        if (item == null) {
                            continue;
                        }

                        //TODO: skip it if current content aren't equals?
                        for (int i = 0; i < item.size(); ++i) {
                            try {
                                Image loaded = item.local(i) ? loadLocal(item.get(i)) : loadImageURL(item.get(i));
                                if (loaded == null) {
                                    continue;
                                }
                                try {
                                    item.set(i, loaded);
                                } catch (Exception e) {
                                }
                            } catch (Throwable t) {
                                item.errored(t);
                            }
                        }

                        item.tried();
                        itemsToLoad.removeElement(item);
                        //TODO: render only if current content equals? or render at cycle end only?
                        AppCanvas.instance.render();
                    }
                    System.gc();
                    interrupt();
                }
            };
            thr.start();
        }
    }

    public void setLeftSoft(SoftButton soft) {
        if (leftSoft != null && leftSoft.lcduiCommand != null) {
            removeCommand(leftSoft.lcduiCommand);
        }
        leftSoft = soft;
        if (leftSoft != null && leftSoft.lcduiCommand != null && !this.showFooter) {
            addCommand(leftSoft.lcduiCommand);
        }
        render();
    }

    public void showTextBox(String header, String text) {
        if (TextUtil.isNullOrEmpty(text)) {
            return;
        }

        textBox = new TextBox(header == null ? "VK4ME" : header, text, (Midlet.instance.config.increaseTextLimit ? MAX_TEXT_LENGTH : 500), TextField.ANY);

        textBox.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                AppCanvas.instance.showGraphics();
            }
        });

        textBox.addCommand(new Command(Localization.get("action.ok"), Command.OK, 1));

        Display.getDisplay(Midlet.instance).setCurrent(textBox);
    }

    public void clearItemsToLoad() {
        itemsToLoad.removeAllElements();
    }
}
