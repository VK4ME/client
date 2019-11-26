package ru.curoviyxru.playvk;

import javax.microedition.io.Connector;
import javax.microedition.io.InputConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import ru.curoviyxru.j2vk.ProgressProvider;
import ru.curoviyxru.j2vk.api.objects.VKObject;
import ru.curoviyxru.j2vk.api.objects.attachments.Audio;
import ru.curoviyxru.j2vk.api.requests.audio.AudioGet;
import ru.curoviyxru.j2vk.api.requests.execute.ExecuteGetPlaylist;
import ru.curoviyxru.j2vk.api.responses.audio.AudioGetResponse;
import ru.curoviyxru.j2vk.api.responses.execute.ExecuteGetPlaylistResponse;
import ru.curoviyxru.j2vk.HTTPClient;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.Theming;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.PopupMenu;
import ru.curoviyxru.phoenix.ui.ProgressBar;
import ru.curoviyxru.phoenix.ui.RenderUtil;
import ru.curoviyxru.phoenix.ui.Slider;
import ru.curoviyxru.phoenix.ui.SuperString;
import ru.curoviyxru.phoenix.ui.contents.ContentController;
import ru.curoviyxru.playvk.PlayerWrapper.PWListener;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class PlayerContent extends Content implements PWListener {

    Playlist pl;
    int index;
    InputConnection conn;
    PopupMenu popup;
    PlayerWrapper p = new PlayerWrapper(this);
    private static PlayerContent instance;
    Slider slider = new Slider() {
        public void valueChanged(long o, long n) {
            setTime(n);
        }
    };
    public int hideTime = 0;
    Slider vSlider = (Slider) new Slider() {
        public void valueChanged(long o, long n) {
            volume = (int) n;
            p.setVolume(volume * 10);
        }
    }.setMaxProgress(10);
    int volume = 10;
    ProgressBar cacheBar = new ProgressBar();
    int loadIcon, playIcon, pauseIcon, nextIcon, menuIcon, prevIcon, repeIcon, onceIcon, repeSelIcon;
    Image albumImage;
    SuperString renderTitle, renderArtist;
    String renderDuration;
    int lastHeightPlayer, lastWidthPlayer, albumWidth, albumHeight;
    int perElementSpace;
    public boolean landscape, drawMenuAndRepeatApart, loading, caching;
    int playX, playY,
            prevX, prevY,
            nextX, nextY,
            repeX, repeY,
            menuX, menuY,
            slideX, slideY;
    int repeatMode = 0;
    boolean sliderPressed;
    public static final int NORMAL = 0, REPEAT_ALL = 1, REPEAT_ONCE = 2; //, SHUFFLE = 3;

    public void paint(Graphics g, int pX, int pY, int renderWidth, int renderHeight, int fullHeight, boolean drawBG) {
        super.paint(g, pX, pY, renderWidth, renderHeight, fullHeight, drawBG);
        initUI(g, pX, pY, renderWidth, renderHeight, fullHeight);
        paintUI(g, pX, pY, renderWidth, renderHeight, fullHeight);
    }

    private void initUI(Graphics g, int pX, int pY, int renderWidth, int renderHeight, int fullHeight) {
        if (lastHeightPlayer == renderHeight && lastWidthPlayer == renderWidth) {
            return;
        }
        lastHeightPlayer = renderHeight;
        lastWidthPlayer = renderWidth;

        landscape = renderWidth > renderHeight;
        int minimalElementsHeight = buttonsHeight() + labelHeight() + timeSliderHeight();
        drawMenuAndRepeatApart = false;

        if (!landscape) {
            albumWidth = renderWidth;
            albumHeight = renderHeight - minimalElementsHeight;
            if (albumHeight > renderWidth) {
                albumHeight = renderWidth;
            }

            perElementSpace = (renderHeight - albumHeight - minimalElementsHeight) / 3;
            //Да, у нас 2 пробела должно быть, но делим мы на 3, чтобы был отступ между последим элементом и концом экрана
        } else {
            albumWidth = renderWidth - buttonsWidth(); //Кнопки типо самый широкий элемент
            if (albumWidth < (renderHeight * 8 / 10)) {
                drawMenuAndRepeatApart = true;
                albumWidth = renderWidth - buttonsWidth();
                minimalElementsHeight += miscButtonsHeight();
            }
            if (albumWidth > renderHeight) {
                albumWidth = renderHeight;
            }
            albumHeight = renderHeight;

            perElementSpace = (renderHeight - minimalElementsHeight) / (drawMenuAndRepeatApart ? 5 : 4);
            //Здесь нам ещё и в начале место нужно
        }

        albumImage = null;
        renderTitle = null;
        renderArtist = null;
        renderDuration = null;

        /* playImage = RenderUtil.resizeImage(AppCanvas.loadImage("player_play.png"), buttonsHeight());
        loadImage = RenderUtil.resizeImage(AppCanvas.loadImage("player_load.png"), buttonsHeight());
        pauseImage = RenderUtil.resizeImage(AppCanvas.loadImage("player_paus.png"), buttonsHeight());
        prevImage = RenderUtil.resizeImage(AppCanvas.loadImage("player_prev.png"), prevNextHeight());
        nextImage = RenderUtil.resizeImage(AppCanvas.loadImage("player_next.png"), prevNextHeight());
        repeImage = RenderUtil.resizeImage(AppCanvas.loadImage("player_repe.png"), miscButtonsHeight());
        repeSelImage = RenderUtil.resizeImage(AppCanvas.loadImage("player_repe_sel.png"), miscButtonsHeight());
        onceImage = RenderUtil.resizeImage(AppCanvas.loadImage("player_once.png"), miscButtonsHeight());
        menuImage = RenderUtil.resizeImage(AppCanvas.loadImage("player_dots.png"), miscButtonsHeight()); */
    }

    public void pointerReleased(int x, int y) {
        int bHhalf = buttonsHeight() / 2;
        int nHhalf = prevNextHeight() / 2;
        int mHhalf = miscButtonsHeight() / 2;
        if (!sliderPressed) {
            if (x > playX - bHhalf && x < playX + bHhalf && y > playY - bHhalf && y < playY + bHhalf) {
                playPause();
            } else if (x > prevX - nHhalf && x < prevX + nHhalf && y > prevY - nHhalf && y < prevY + nHhalf) {
                prevButton();
            } else if (x > nextX - nHhalf && x < nextX + nHhalf && y > nextY - nHhalf && y < nextY + nHhalf) {
                nextButton();
            } else if (x > repeX - mHhalf && x < repeX + mHhalf && y > repeY - mHhalf && y < repeY + mHhalf) {
                repeatSwitch();
            } else if (x > menuX - mHhalf && x < menuX + mHhalf && y > menuY - mHhalf && y < menuY + mHhalf) {
                //menu();
            }
        } else //if (x > slider.x && x < slider.x + slider.width && y > slider.y && y < slider.y + slider.height) 
        {
            slider.pointerReleased(x, y, AppCanvas.instance.contentY);
            sliderPressed = slider.pressed;
        }
    }

    public void pointerPressed(int x, int y) {
        if (!isStopped() && x > slider.x && x < slider.x + slider.width && y > slider.y && y < slider.y + slider.height) {
            slider.pointerPressed(x, y, AppCanvas.instance.contentY);
            sliderPressed = slider.pressed;
        }
    }

    public void pointerDragged(int x, int y) {
        if (sliderPressed && x > slider.x && x < slider.x + slider.width && y > slider.y && y < slider.y + slider.height) {
            slider.pointerDragged(x, y, AppCanvas.instance.contentY);
            sliderPressed = slider.pressed;
        }
    }

    public void keyPressed(int k) {
        switch (k) {
            case AppCanvas.KEY_NUM2:
            case AppCanvas.UP:
                volUp();
                break;
            case AppCanvas.KEY_NUM8:
            case AppCanvas.DOWN:
                volDown();
                break;
            case AppCanvas.KEY_NUM4:
            case AppCanvas.LEFT:
                prevButton();
                break;
            case AppCanvas.KEY_NUM5:
            case AppCanvas.RIGHT:
                nextButton();
                break;
            case AppCanvas.KEY_NUM6:
            case AppCanvas.FIRE:
            case AppCanvas.ENTER:
                playPause();
                break;
        }
    }

    public void keyRepeated(int k) {
        switch (k) {
            case AppCanvas.KEY_NUM2:
            case AppCanvas.UP:
                volUp();
                break;
            case AppCanvas.KEY_NUM5:
            case AppCanvas.DOWN:
                volDown();
                break;
        }
    }

    public void volUp() {
        volume = Math.max(0, Math.min(10, volume + 1));
        p.setVolume(volume * 10);
        vSlider.progress = volume;
        hideTime = 5;
        renderIfNeeded();
    }

    public void volDown() {
        volume = Math.max(0, Math.min(10, volume - 1));
        p.setVolume(volume * 10);
        vSlider.progress = volume;
        hideTime = 5;
        renderIfNeeded();
    }

    private void paintUI(Graphics g, int pX, int pY, int renderWidth, int renderHeight, int fullHeight) {
        int addY = 0;

        paintAlbum(g, pX, pY + addY, renderWidth, renderHeight, fullHeight);
        paintBottomBar(g, pX, pY + renderHeight - cacheBar.height, renderWidth, renderHeight, fullHeight);
        addY += albumHeight;

        //По идее мы теперь можем спокойно менять порядок элементов и нам за это ничего не будет

        if (landscape) {
            addY = perElementSpace;
            pX += albumWidth;
            renderWidth -= albumWidth;
        } else { //Рисуем слайдер сразу после обложки если в портретном
            paintTimeSlider(g, pX, pY + addY, renderWidth, renderHeight, fullHeight);
            addY += timeSliderHeight() + perElementSpace;
        }

        paintLabel(g, pX, pY + addY, renderWidth, renderHeight, fullHeight);
        addY += labelHeight() + perElementSpace;

        if (landscape) { //Рисуем слайдер после информации, если в пейзажном
            paintTimeSlider(g, pX, pY + addY, renderWidth, renderHeight, fullHeight);
            addY += timeSliderHeight() + perElementSpace;
        }

        paintButtons(g, pX, pY + addY, renderWidth, renderHeight, fullHeight);
        addY += buttonsHeight() + perElementSpace;

        if (drawMenuAndRepeatApart) { //Рисуем эти кнопки отдельно, если нужно
            paintMiscButtons(g, pX, pY + addY, renderWidth, renderHeight, fullHeight);
            addY += miscButtonsHeight() + perElementSpace;
        }
    }

    private void paintAlbum(Graphics g, int pX, int pY, final int renderWidth, final int renderHeight, int fullHeight) {
        if (albumImage == null) {
            albumImage = RenderUtil.resizeImage(AppCanvas.loadLocal("new/thumb.png"),
                    landscape ? albumWidth : albumHeight);
            //Пусть нота влезает в preview

            new Thread() {
                public void run() {
                    Audio a = getAudio();
                    if (a != null) {
                        try {
                            albumImage = RenderUtil.resizeImage(AppCanvas.loadImageURL(a.getPhoto_600()),
                                    landscape ? renderHeight : renderWidth);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    if (albumImage == null) {
                        albumImage = RenderUtil.resizeImage(AppCanvas.loadLocal("new/thumb.png"),
                                landscape ? albumWidth : albumHeight);
                        //Пусть нота влезает в preview
                    }
                    renderIfNeeded();
                }
            }.start();
        }

        if (albumImage != null) {
            int minx = Math.min(albumImage.getWidth(), albumWidth);
            int miny = Math.min(albumImage.getHeight(), albumHeight);

            g.drawRegion(albumImage,
                    (albumImage.getWidth() - minx) / 2, (albumImage.getHeight() - miny) / 2,
                    minx, miny, 0,
                    pX + albumWidth / 2, pY + albumHeight / 2,
                    Graphics.HCENTER | Graphics.VCENTER);
        }
    }

    private void paintTimeSlider(Graphics g, int pX, int paintY, int renderWidth, int renderHeight, int fullHeight) {
        int pY = paintY - (landscape ? 0 : (slider.height - slider.pheight) / 2);

        if (slider.maxProgress == 0) {
            Audio a = getAudio();
            if (a != null) {
                slider.maxProgress = a.duration;
            }

            if (slider.maxProgress == 0) {
                slider.maxProgress = 1;
            }
        }

        if (!sliderPressed) {
            slider.progress = Math.min(slider.maxProgress, Math.max(0, p.getTime()));
        }

        slider.width = renderWidth;
        slider.x = pX;
        slider.y = pY;
        slider.paint(g, 0, 0);

        pY += AppCanvas.instance.perLineSpace;
        pY += slider.height;

        g.setColor(Theming.now.captionColor);
        g.setFont(AppCanvas.instance.normalFont);
        g.drawString(getNowString(), pX + AppCanvas.instance.normalEmojiFont.height, pY, Graphics.LEFT | Graphics.TOP);

        if (renderDuration == null) {
            Audio a = getAudio();
            if (a != null) {
                renderDuration = VKObject.trackTimeToString(a.duration);
            }

            if (renderDuration == null) {
                renderDuration = "0:00";
            }
        }

        if (renderDuration != null) {
            g.drawString(renderDuration,
                    pX + renderWidth - AppCanvas.instance.normalEmojiFont.height - AppCanvas.instance.normalFont.stringWidth("0:00"),
                    pY, Graphics.LEFT | Graphics.TOP);
        }
    }

    private final int timeSliderHeight() {
        if (landscape) {
            return slider.height + AppCanvas.instance.perLineSpace + AppCanvas.instance.normalEmojiFont.height;
        }

        return slider.height - (slider.height - slider.pheight) / 2 + AppCanvas.instance.perLineSpace;
    }

    public int getDuration() {
        Audio a = getAudio();
        return a == null ? 1 : a.duration;
    }

    //TODO running label, excplicit sign
    private void paintLabel(Graphics g, int pX, int paintY, int renderWidth, int renderHeight, int fullHeight) {
        int pY = paintY;
        int limit = renderWidth
                - (landscape ? 0 : AppCanvas.instance.normalFont.stringWidth(renderDuration == null ? "0:00" : renderDuration))
                - (landscape ? 0 : AppCanvas.instance.normalFont.stringWidth(renderDuration))
                - (landscape ? AppCanvas.instance.normalEmojiFont.height : AppCanvas.instance.normalEmojiFont.height * 4);

        if (renderTitle == null) {
            Audio a = getAudio();
            if (a != null) {
                renderTitle = AppCanvas.instance.normalEmojiFont.limit(new SuperString(a.title), limit, true);
            }

            if (renderTitle == null) {
                renderTitle = AppCanvas.instance.boldEmojiFont.limit(new SuperString(Localization.get("general.noTrack")), limit, true);
            }
        }

        limit = renderWidth - (landscape ? AppCanvas.instance.normalEmojiFont.height : AppCanvas.instance.normalEmojiFont.height * 2);
        //Это чтобы края исполнителя были вместе с краями надписи длительности и текущего времени

        if (renderArtist == null) {
            Audio a = getAudio();
            if (a != null) {
                renderArtist = AppCanvas.instance.boldEmojiFont.limit(new SuperString(a.artist), limit, true);
            }

            if (renderArtist == null) {
                renderArtist = new SuperString("");
            }
        }

        g.setColor(Theming.now.textColor);
        AppCanvas.instance.boldEmojiFont.drawString(g, renderTitle, pX + renderWidth / 2, pY, Graphics.HCENTER | Graphics.TOP);

        pY += AppCanvas.instance.perLineSpace;
        pY += AppCanvas.instance.boldEmojiFont.height;

        AppCanvas.instance.normalEmojiFont.drawString(g, renderArtist, pX + renderWidth / 2, pY, Graphics.HCENTER | Graphics.TOP);
    }

    private final int labelHeight() {
        return AppCanvas.instance.perLineSpace
                + AppCanvas.instance.boldEmojiFont.height
                + AppCanvas.instance.normalEmojiFont.height;
    }

    private void paintButtons(Graphics g, int pX, int paintY, int renderWidth, int renderHeight, int fullHeight) {
        int pY = paintY + buttonsHeight() / 2;
        playY = prevY = nextY = pY;

        playX = pX + renderWidth / 2;
        if (loading) {
            loadIcon = Theming.now.drawImage(g, playX, playY, loadIcon, "new/player_load.rle", Theming.now.iconColor, Theming.now.iconColor_, buttonsHeight(), 0, Graphics.VCENTER | Graphics.HCENTER);
        } else if (p.isPlaying()) {
            pauseIcon = Theming.now.drawImage(g, playX, playY, pauseIcon, "new/player_pause.rle", Theming.now.iconColor, Theming.now.iconColor_, buttonsHeight(), 0, Graphics.VCENTER | Graphics.HCENTER);
        } else {
            playIcon = Theming.now.drawImage(g, playX, playY, playIcon, "new/player_play.rle", Theming.now.iconColor, Theming.now.iconColor_, buttonsHeight(), 0, Graphics.VCENTER | Graphics.HCENTER);
        }

        prevX = pX + renderWidth / 2 - AppCanvas.instance.normalEmojiFont.height * 3;
        prevIcon = Theming.now.drawImage(g, prevX, prevY, prevIcon, "new/player_prev.rle", Theming.now.iconColor, Theming.now.iconColor_, prevNextHeight(), 0, Graphics.VCENTER | Graphics.HCENTER);
        nextX = pX + renderWidth / 2 + AppCanvas.instance.normalEmojiFont.height * 3;
        nextIcon = Theming.now.drawImage(g, nextX, nextY, nextIcon, "new/player_next.rle", Theming.now.iconColor, Theming.now.iconColor_, prevNextHeight(), 0, Graphics.VCENTER | Graphics.HCENTER);

        if (!drawMenuAndRepeatApart) {
            paintMiscButtons(g, pX, paintY, renderWidth, renderHeight, fullHeight);
        }
    }

    private void paintMiscButtons(Graphics g, int pX, int paintY, int renderWidth, int renderHeight, int fullHeight) {
        int pY = paintY + (drawMenuAndRepeatApart ? miscButtonsHeight() : buttonsHeight()) / 2;
        repeY = menuY = pY;

        int offset = renderWidth / 4 + AppCanvas.instance.normalEmojiFont.height * 2;
        if (drawMenuAndRepeatApart) {
            offset = AppCanvas.instance.normalEmojiFont.height * 3;
        }
        //Если надо рисуем иконки у краёв экрана

        repeX = pX + renderWidth / 2 - offset;
        switch (repeatMode) {
            case NORMAL:
                repeIcon = Theming.now.drawImage(g, repeX, repeY, repeIcon, "new/player_repe.rle", Theming.now.iconColor, Theming.now.iconColor_, miscButtonsHeight(), 0, Graphics.VCENTER | Graphics.HCENTER);
                break;
            case REPEAT_ALL:
                repeSelIcon = Theming.now.drawImage(g, repeX, repeY, repeSelIcon, "new/player_repe.rle", Theming.now.activeIconColor, Theming.now.activeIconColor_, miscButtonsHeight(), 0, Graphics.VCENTER | Graphics.HCENTER);
                break;
            case REPEAT_ONCE:
                onceIcon = Theming.now.drawImage(g, repeX, repeY, onceIcon, "new/player_once.rle", Theming.now.activeIconColor, Theming.now.activeIconColor_, miscButtonsHeight(), 0, Graphics.VCENTER | Graphics.HCENTER);
                break;
        }
        menuX = pX + renderWidth / 2 + offset;
        //menuIcon = Theming.now.drawImage(g, menuX, menuY, menuIcon, "new/player_dots.rle", Theming.now.iconColor, Theming.now.iconColor_, miscButtonsHeight(), 0, Graphics.VCENTER | Graphics.HCENTER);
    }

    private final int buttonsHeight() {
        return 3 * AppCanvas.instance.normalEmojiFont.height;
    }

    private final int buttonsWidth() {
        if (drawMenuAndRepeatApart) {
            return AppCanvas.instance.normalEmojiFont.height * (3 * 2 + 2 / 2 + 1);
        }
        //Отступ перемотки на 2 + половина ширины кнопки + отступ

        return AppCanvas.instance.normalEmojiFont.height * (6 * 2 + 2 / 2 + 1);
        //Отступ самой левой кнопки на 2 + половина ширины кнопки + отступ
    }

    private int prevNextHeight() {
        return buttonsHeight() * 2 / 3;
    }

    private final int miscButtonsHeight() {
        return AppCanvas.instance.normalEmojiFont.height * 3 / 2;
    }

    public static void play(Content parent, Playlist pl) {
        play(parent, pl, -1);
    }

    public static void play(Content parent, Playlist pl, int a) {
        if (instance != null) {
            instance.playThere(pl, a);
        }
        ContentController.showPlayer(parent);
    }

    public void playThere(Playlist pl, int a) {
        if (pl == null) {
            stop();
            return;
        }

        this.pl = pl;
        index = a;
        if (index == -1) {
            index = 0;
        }
        play(true);
    }

    public void play(final boolean force) {
        if (pl == null) {
            return;
        }
        final Audio a = getAudio();
        if (a == null) {
            stop();
            return;
        }
        new Thread() {
            public void run() {
                if (p.isStopped() || force) {
                    set(a);
                }
                renderIfNeeded();
                p.play();
            }
        }.start();
    }

    public void play() {
        play(false);
    }

    public void pause() {
        if (pl == null || loading) {
            return;
        }
        p.pause();
        renderIfNeeded();
    }

    public void nextButton() {
        if (pl == null || loading) {
            return;
        }
        index++;
        Audio a = getAudio();
        if (a == null) {
            stop();
        } else {
            play(true);
        }
        renderIfNeeded();
    }

    public void prevButton() {
        if (pl == null || loading) {
            return;
        }

        if (p.getTime() < 3) {
            index--;
            Audio a = getAudio();
            if (a == null) {
                stop();
            } else {
                play(true);
            }
        } else {
            setTime(0);
        }


        renderIfNeeded();
    }

    public void setTime(long l) {
        if (pl == null || loading) {
            return;
        }
        p.setTime((int) l);
    }

    public static void remove() {
        if (instance != null) {
            instance.stop();
        }
        instance = null;
    }

    public PlayerContent() {
        super(Localization.get("title.audioplayer"));
        instance = this;
    }

    public static PlayerContent get() {
        return instance == null ? new PlayerContent() : instance;
    }

    public void stop() {
        pl = null;
        index = -1;
        p.stop();
        set(null);
        renderIfNeeded();
    }

    public boolean isStopped() {
        return p.isStopped();
    }

    private Audio getAudio() {
        if (pl == null || index < 0) {
            return null;
        }
        if (index >= pl.audios.size()) {
            switch (pl.type) {
                case Playlist.USER_TRACKS:
                    AudioGetResponse aresponse = (AudioGetResponse) new AudioGet().setCount(index + 1 - pl.audios.size()).setOffset(pl.audios.size()).setOwnerId(pl.owner_id).execute();
                    if (aresponse != null && aresponse.hasItems()) {
                        for (int i = 0; i < aresponse.items.length; i++) {
                            pl.audios.addElement(aresponse.items[i]);
                        }
                    }
                    break;
                case Playlist.PLAYLIST:
                    ExecuteGetPlaylistResponse eresponse = (ExecuteGetPlaylistResponse) new ExecuteGetPlaylist(pl.owner_id, pl.id, pl.access_key).execute();
                    pl.audios.removeAllElements();
                    if (eresponse != null && eresponse.hasItems()) {
                        for (int i = 0; i < eresponse.items.length; i++) {
                            pl.audios.addElement(eresponse.items[i]);
                        }
                    }
                    break;
            }
        }

        if (index >= pl.audios.size()) {
            return null;
        }

        return (Audio) pl.audios.elementAt(index);
    }

    private void set(final Audio a) {
        closeConn();
        renderTitle = null;
        renderArtist = null;
        albumImage = null;
        renderDuration = null;
        popup = null;
        loading = false;
        slider.setMaxProgress(0);
        slider.setProgress(0);
        caching = false;
        cacheBar.setProgress(0);
        if (a != null) {
            loading = true;

            renderIfNeeded();

            setupTrack(a);

            loading = false;
        }
    }

    private String getNowString() {
        if (p.isStopped()) {
            return "0:00";
        } else {
            return VKObject.trackTimeToString(Math.max(0, p.getTime()));
        }
    }

    private void playPause() {
        if (p.isStopped()) {
            return;
        }
        if (p.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    public boolean isPlaying() {
        return p.isPlaying();
    }

    private void repeatSwitch() {
        repeatMode = (repeatMode + 1) % 3;
    }

    //private void menu() {
    //    AppCanvas.instance.showPopup(getPopup());
    //}

    //private PopupMenu getPopup() {
    //    if (popup != null) {
    //        return popup;
    //    }
    //
    //    popup = new PopupMenu(Localization.get("general.actions"));
    //
    //    return popup;
    //}

    //public void rightSoft() {
    //    if (AppCanvas.instance.popupOpened()) {
    //       AppCanvas.instance.closePopup();
    //        return;
    //     }
    //    menu();
    //}

    //public String getRightSoft() {
    //    return getPopup().title.toString();
    //}

    public void update(boolean fromCanvas) {
        if (hideTime > 0) {
            hideTime--;
        }
        if (p.isPlaying() && !sliderPressed && Math.abs(getDuration() - p.getTime()) < 2) {
            next();
        }
        renderIfNeeded();
    }

    public void update() {
        update(false);
    }

    public void endOfMedia() {
        next();
    }

    private void next() {
        if (pl == null || loading) {
            return;
        }
        if (repeatMode == NORMAL || repeatMode == REPEAT_ALL) {
            index++;
        }
        Audio a = getAudio();
        if (a == null) {
            if (repeatMode == REPEAT_ALL) {
                index = 0;
            } else {
                stop();
            }
        } else {
            play(true);
        }
    }

    private void paintBottomBar(Graphics g, int pX, int i, int renderWidth, int renderHeight, int fullHeight) {
        if (caching) {
            cacheBar.width = renderWidth;
            cacheBar.x = pX;
            cacheBar.y = i;
            cacheBar.paint(g, 0, 0);
        }
    }

    private void setupTrack(final Audio a) {
        if (a == null) {
            return;
        }

        if (Midlet.instance.config.use_caching) {
            if (AudioCache.folderExists()) {
                if (AudioCache.has(a)) {
                    try {
                        conn = (FileConnection) Connector.open(AudioCache.get(a), Connector.READ);
                        p.set(conn);
                    } catch (Exception ex) {
                        AppCanvas.instance.dropError(ex);
                        closeConn();
                        if (!Midlet.instance.config.cache_only) {
                            try {
                                conn = (InputConnection) HTTPClient.openHttpConnection(a.url, true);
                                p.set(conn);
                            } catch (Exception e) {}
                        } else {
                            stop();
                        }
                    }
                } else {
                    if (Midlet.instance.config.cache_only) {
                        caching = true;

                        try {
                            conn = (InputConnection) Connector.open(AudioCache.get(a), Connector.READ_WRITE);
                            PlayerWrapper.downloadFile(a.url, (FileConnection) conn, new ProgressProvider() {
                                public void setProgress(long i) {
                                    Audio ad = getAudio();
                                    if (ad != null && ad.toString().equals(a.toString())) {
                                        cacheBar.setProgress(i);
                                        renderIfNeeded();
                                    }
                                }

                                public void failed(String s) {
                                }

                                public void successful() {
                                }

                                public String getName() {
                                    return a.toString();
                                }
                            }, 0);

                            p.set(conn);
                        } catch (Exception ex) {
                            AppCanvas.instance.dropError(ex);
                            closeConn();
                            if (!Midlet.instance.config.cache_only) {
                                try {
                                    conn = (InputConnection) HTTPClient.openHttpConnection(a.url, true);
                                    p.set(conn);
                                } catch (Exception e) {}
                            } else {
                                stop();
                            }
                        }
                        caching = false;
                    } else {
                        try {
                            conn = (InputConnection) HTTPClient.openHttpConnection(a.url, true);
                            p.set(conn);
                        } catch (Exception e) {}
                        new Thread() {
                            public void run() {
                                caching = true;

                                try {
                                    FileConnection conn = (FileConnection) Connector.open(AudioCache.get(a), Connector.READ_WRITE);
                                    PlayerWrapper.downloadFile(a.url, (FileConnection) conn, new ProgressProvider() {
                                        public void setProgress(long i) {
                                            Audio ad = getAudio();
                                            if (ad != null && ad.toString().equals(a.toString())) {
                                                cacheBar.setProgress(i);
                                                renderIfNeeded();
                                            }
                                        }

                                        public void failed(String s) {
                                        }

                                        public void successful() {
                                        }

                                        public String getName() {
                                            return a.toString();
                                        }
                                    }, 0);
                                    conn.close();
                                } catch (Exception ex) {
                                    AppCanvas.instance.dropError(ex);
                                }

                                caching = false;
                            }
                        }.start();
                    }
                }
            } else {
                Midlet.instance.config.use_caching = Midlet.instance.config.use_caching = false;
                Midlet.instance.config.caching_folder = Midlet.instance.config.caching_folder = null;
                AppCanvas.instance.dropError(Localization.get("settings.cachingFolderNotExists"));
                try {
                    conn = (InputConnection) HTTPClient.openHttpConnection(a.url, true);
                    p.set(conn);
                } catch (Exception e) {}
            }
        } else {
            try {
                conn = (InputConnection) HTTPClient.openHttpConnection(a.url, true);
                p.set(conn);
            } catch (Exception e) {}
        }
    }

    private void closeConn() {
        if (conn == null) {
            return;
        }

        try {
            conn.close();
        } catch (Exception e) {
        }

        conn = null;
    }
}
