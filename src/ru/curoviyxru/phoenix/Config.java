package ru.curoviyxru.phoenix;

import org.json.me.JSONArray;
import org.json.me.JSONObject;
import ru.curoviyxru.j2vk.LongPoll;
import ru.curoviyxru.j2vk.VKConstants;
import ru.curoviyxru.j2vk.api.objects.Account;
import ru.curoviyxru.j2vk.api.objects.attachments.Comment;
import ru.curoviyxru.j2vk.api.objects.user.User;
import ru.curoviyxru.j2vk.api.objects.Message;
import ru.curoviyxru.j2vk.api.objects.VKObject;
import ru.curoviyxru.j2vk.platform.Charset;
import ru.curoviyxru.j2vk.platform.HTTPMEClient;
import ru.curoviyxru.phoenix.Localization.LocaleEntry;
import ru.curoviyxru.phoenix.Theming.ThemeEntry;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.contents.ContentController;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class Config {

    public static boolean hasEmojis;

    public void tryAuth() {
        httpPost();
        String to = rms_access_token;
        if (to == null) {
            ContentController.logout();
            return;
        }
        try {
            VKConstants.account = new Account(to);
            ContentController.loggedIn();
        } catch (Exception e) {
            Logger.l(e);
            VKConstants.account = null;
            ContentController.logError();
        }
    }
    
    public boolean gui_cloudBorder,
            gui_reverseNames,
            gui_disableDimm,
            gui_animations,
            gui_smoothScroll,
            gui_showClouds,
            debug_drawFPS,
            debug_drawEmojiRed,
            gui_fullscreen,
            gui_messagesAvatars,
            gui_touchHud,
            feed_keyVibro,
            feed_notVibro,
            feed_notSound,
            debug_drawImagesFrames,
            gui_messageDiffSides,
            useFlushUpload,
            gui_reverseSofts,
            translitFiles,
            gui_drawGradients,
            gui_showHeader,
            gui_showFooter,
            showMessagesFromEnd,
            doNotLoadImages,
            gui_showDock,
            usePQSofts,
            use_caching,
            cache_only,
            gui_slimHeader,
            gui_useDrawer,
            showReplies;
    public int gui_avatarCircleType,
            network_mode,
            lpSpeed,
            lpsSpeed,
            gui_photosCircleType,
            downloadMode;
    public String rms_access_token, customAPI, customAUTH, customPROXY, caching_folder;
    public ThemeEntry[] themes;
    public LocaleEntry[] locales;
    public String localeCode, themeName;
    public boolean localeFromRMS, themeFromRMS;
    public boolean DNR, DNT;
    public boolean loggerEnabled;
    public boolean increaseTextLimit, oldMessagesSoft, oldCommentsSoft;
    public int brightness;
    public boolean showMsgFieldByOpen, showCmtFieldByOpen;
    public boolean shiftTitleRight;
    public boolean useKeypadInput;
    public boolean debug_lp;
    public boolean sleepWithTimeout;
    public boolean hideAfterSending;
    public boolean typingInDialogs;
    public boolean upperFirstChar;
    public boolean replaceEmailAtYo;
    public int sleepTimeout;
    public boolean setOffline;
    public boolean vibroOnSend;
    public int sendVibroTime, keyVibroTime, notVibroTime;
    
    public Config() {
        load();
    }

    public void reset() {
        //TODO: error popup
        gui_cloudBorder = true;
        gui_reverseNames = false;
        gui_avatarCircleType = 100;
        network_mode = 5;
        gui_disableDimm = false;
        debug_drawFPS = false;
        debug_drawEmojiRed = false;
        gui_animations = true;
        gui_smoothScroll = true;
        feed_keyVibro = true;
        feed_notVibro = true;
        feed_notSound = true;
        gui_fullscreen = true;
        gui_touchHud = AppCanvas.instance != null ? AppCanvas.instance.hasPointerEvents() : true;
        themeName = Theming.getStandard();
        localeCode = Localization.getSystem();
        themeFromRMS = false;
        localeFromRMS = false;
        debug_drawImagesFrames = false;
        lpSpeed = 3;
        lpsSpeed = 20;
        gui_showClouds = true;
        gui_messageDiffSides = true;
        gui_messagesAvatars = true;
        useFlushUpload = false;
        gui_photosCircleType = 100;
        gui_reverseSofts = true;
        translitFiles = true;
        gui_drawGradients = true;
        gui_showHeader = true;
        gui_showFooter = true;
        showMessagesFromEnd = false;
        downloadMode = 0;
        DNR = false;
        DNT = false;
        doNotLoadImages = false;
        customAPI = "";
        customAUTH = "";
        customPROXY = "";
        gui_showDock = false; //true
        gui_slimHeader = false;
        gui_useDrawer = false;
        usePQSofts = false;
        showReplies = false;
        loggerEnabled = false;
        oldMessagesSoft = false;
        oldCommentsSoft = false;
        increaseTextLimit = false;
        brightness = 0;
        showCmtFieldByOpen = true;
        showMsgFieldByOpen = true;
        shiftTitleRight = false;
        useKeypadInput = true;
        sleepWithTimeout = true;
        debug_lp = false;
        hideAfterSending = true;
        typingInDialogs = true;
        replaceEmailAtYo = false;
        upperFirstChar = false;
        sleepTimeout = 90;
        setOffline = true;
        vibroOnSend = true;
        sendVibroTime = 300;
        keyVibroTime = 100;
        notVibroTime = 500;
        use_caching = false;
        cache_only = false;
        caching_folder = null;
        
        httpPost();
        post();
        innerPost();
    }
    
    private void innerPost() {
        Localization.loadSelected(localeCode);
        
        Message.LOCALE_YOU = Localization.get("j2vk.you");
        Message.LOCALE_CREATED_IT = Localization.get("j2vk.created_it");
        Message.LOCALE_CREATED_M = Localization.get("j2vk.created_m");
        Message.LOCALE_CREATED_F = Localization.get("j2vk.created_f");
        Message.LOCALE_JOINED_IT = Localization.get("j2vk.joined_it");
        Message.LOCALE_JOINED_M = Localization.get("j2vk.joined_m");
        Message.LOCALE_JOINED_F = Localization.get("j2vk.joined_f");
        Message.LOCALE_INVITED_IT = Localization.get("j2vk.invited_it");
        Message.LOCALE_INVITED_M = Localization.get("j2vk.invited_m");
        Message.LOCALE_INVITED_F = Localization.get("j2vk.invited_f");
        Message.LOCALE_TO_CHAT = Localization.get("j2vk.to_chat");
        Message.LOCALE_TO_CHAT_VIA_LINK = Localization.get("j2vk.to_chat_via_link");
        Message.LOCALE_FROM_CHAT = Localization.get("j2vk.from_chat");
        Message.LOCALE_KICKED_IT = Localization.get("j2vk.kicked_it");
        Message.LOCALE_KICKED_M = Localization.get("j2vk.kicked_m");
        Message.LOCALE_KICKED_F = Localization.get("j2vk.kicked_f");
        Message.LOCALE_LEFT_IT = Localization.get("j2vk.left_it");
        Message.LOCALE_LEFT_M = Localization.get("j2vk.left_m");
        Message.LOCALE_LEFT_F = Localization.get("j2vk.left_f");
        Message.LOCALE_EMPTY = Localization.get("j2vk.empty");
        Message.LOCALE_ATTACH = Localization.get("j2vk.attach");
        Message.LOCALE_ATTACHS = Localization.get("j2vk.attachs");
        Message.LOCALE_FORWARD = Localization.get("j2vk.forward");
        Message.LOCALE_FORWARDS = Localization.get("j2vk.forwards");
        Message.LOCALE_REPLY = Localization.get("j2vk.reply");
        Message.LOCALE_CHAT_TITLE_TO = Localization.get("j2vk.chat_title_to");
        Message.LOCALE_CHAT_PHOTO = Localization.get("j2vk.chat_photo");
        Message.LOCALE_MESSAGE = Localization.get("j2vk.message");
        Message.LOCALE_UPDATED_IT = Localization.get("j2vk.updated_it");
        Message.LOCALE_UPDATED_M = Localization.get("j2vk.updated_m");
        Message.LOCALE_UPDATED_F = Localization.get("j2vk.updated_f");
        Message.LOCALE_REMOVED_IT = Localization.get("j2vk.removed_it");
        Message.LOCALE_REMOVED_M = Localization.get("j2vk.removed_m");
        Message.LOCALE_REMOVED_F = Localization.get("j2vk.removed_f");
        Message.LOCALE_PINNED_IT = Localization.get("j2vk.pinned_it");
        Message.LOCALE_PINNED_M = Localization.get("j2vk.pinned_m");
        Message.LOCALE_PINNED_F = Localization.get("j2vk.pinned_f");
        Message.LOCALE_UNPINNED_IT = Localization.get("j2vk.unpinned_it");
        Message.LOCALE_UNPINNED_M = Localization.get("j2vk.unpinned_m");
        Message.LOCALE_UNPINNED_F = Localization.get("j2vk.unpinned_f");
        Comment.COMMENT_DELETED = Localization.get("j2vk.comment_deleted");
        VKObject.MONTH_NAMES[1] = Localization.get("j2vk.month1");
        VKObject.MONTH_NAMES[2] = Localization.get("j2vk.month2");
        VKObject.MONTH_NAMES[3] = Localization.get("j2vk.month3");
        VKObject.MONTH_NAMES[4] = Localization.get("j2vk.month4");
        VKObject.MONTH_NAMES[5] = Localization.get("j2vk.month5");
        VKObject.MONTH_NAMES[6] = Localization.get("j2vk.month6");
        VKObject.MONTH_NAMES[7] = Localization.get("j2vk.month7");
        VKObject.MONTH_NAMES[8] = Localization.get("j2vk.month8");
        VKObject.MONTH_NAMES[9] = Localization.get("j2vk.month9");
        VKObject.MONTH_NAMES[10] = Localization.get("j2vk.month10");
        VKObject.MONTH_NAMES[11] = Localization.get("j2vk.month11");
        VKObject.MONTH_NAMES[12] = Localization.get("j2vk.month12");
        
        Theming.loadSelected(themeName, themeFromRMS);
    }

    public void loadInstalled() {
        try {
            JSONObject object = new JSONObject(new String(RmsController.openStore("insjson3"), Charset.current));
            JSONArray arr = object.optJSONArray("themes");
            if (arr != null) {
                themes = new ThemeEntry[arr.length()];
                for (int i = 0; i < themes.length; ++i) {
                    themes[i] = new ThemeEntry();
                    JSONObject obj = arr.optJSONObject(i);
                    if (obj == null) {
                        continue;
                    }
                    themes[i].name = obj.optString("name");
                    themes[i].version = obj.optString("version");
                }
            }
            arr = object.optJSONArray("locales");
            if (arr != null) {
                locales = new LocaleEntry[arr.length()];
                for (int i = 0; i < locales.length; ++i) {
                    locales[i] = new LocaleEntry();
                    JSONObject obj = arr.optJSONObject(i);
                    if (obj == null) {
                        continue;
                    }
                    locales[i].name = obj.optString("name");
                    locales[i].version = obj.optString("version");
                    locales[i].code = obj.optString("code");
                }
            }
        } catch (Throwable e) {
            themes = new ThemeEntry[0];
            locales = new LocaleEntry[0];
            //e.printStackTrace();
        }
    }

    public void saveInstalled() {
        try {
            JSONObject object = new JSONObject();
            JSONArray arr = new JSONArray();
            if (themes != null) for (int i = 0; i < themes.length; ++i) {
                if (themes[i] == null) themes[i] = new ThemeEntry();
                JSONObject obj = new JSONObject();
                obj.put("name", themes[i].name);
                obj.put("version", themes[i].version);
                arr.put(obj);
            }
            object.put("themes", arr);
            arr = new JSONArray();
            if (locales != null) for (int i = 0; i < locales.length; ++i) {
                if (locales[i] == null) locales[i] = new LocaleEntry();
                JSONObject obj = new JSONObject();
                obj.put("name", locales[i].name);
                obj.put("version", locales[i].version);
                obj.put("code", locales[i].code);
                arr.put(obj);
            }
            object.put("locales", arr);
            
            RmsController.saveStore("insjson3", object.toString().getBytes(Charset.current));
        } catch (Throwable e) {
            Logger.l(e);
        }
    }

    public void saveToken() {
        try {
            if (rms_access_token != null) {
                RmsController.saveStore("tokenavk3", rms_access_token.getBytes(Charset.current));
            } else {
                RmsController.removeStore("tokenavk3");
            }
        } catch (Throwable e) {
            Logger.l(e);
        }
    }

    public void loadToken() {
        try {
            rms_access_token = new String(RmsController.openStore("tokenavk3"), Charset.current);
        } catch (Throwable e) {
            rms_access_token = null;
            //e.printStackTrace();
        }
    }

    public void load() {
        loadToken();
        loadInstalled();
        try {
            JSONObject object = new JSONObject(new String(RmsController.openStore("cfgjson3"), Charset.current));

            gui_cloudBorder = object.optBoolean("gui_cloudBorder", true);
            gui_reverseNames = object.optBoolean("gui_reverseNames", false);
            gui_avatarCircleType = object.optInt("gui_avatarCircleType", 100);
            network_mode = object.optInt("network_mode", 5);
            if (network_mode == 0) network_mode = 5;
            gui_disableDimm = object.optBoolean("gui_disableDimm", false);
            debug_drawFPS = object.optBoolean("debug_drawFPS", false);
            debug_drawEmojiRed = object.optBoolean("debug_drawEmojiRed", false);
            gui_animations = object.optBoolean("gui_animations", true);
            gui_smoothScroll = object.optBoolean("gui_smoothScroll", true);
            feed_keyVibro = object.optBoolean("feed_keyVibro", true);
            feed_notVibro = object.optBoolean("feed_notVibro", true);
            feed_notSound = object.optBoolean("feed_notSound", true);
            gui_fullscreen = object.optBoolean("gui_fullscreen", true);
            gui_touchHud = object.optBoolean("gui_touchHud", AppCanvas.instance != null ? AppCanvas.instance.hasPointerEvents() : true);
            themeName = object.optString("themeName", Theming.getStandard());
            localeCode = object.optString("localeCode", Localization.getSystem());
            themeFromRMS = object.optBoolean("themeFromRMS", false);
            localeFromRMS = object.optBoolean("localeFromRMS", false);
            debug_drawImagesFrames = object.optBoolean("debug_drawImagesFrames", false);
            lpSpeed = object.optInt("lpSpeed", 3);
            lpsSpeed = object.optInt("lpsSpeed", 20);
            gui_showClouds = object.optBoolean("gui_showClouds", true);
            gui_messageDiffSides = object.optBoolean("gui_messageDiffSides", true);
            gui_messagesAvatars = object.optBoolean("gui_messagesAvatars", true);
            useFlushUpload = object.optBoolean("useFlushUpload", false);
            gui_photosCircleType = object.optInt("gui_photosCircleType", 100);
            gui_reverseSofts = object.optBoolean("gui_reverseSofts", true);
            translitFiles = object.optBoolean("translitFiles", true);
            gui_drawGradients = object.optBoolean("gui_drawGradients", true);
            gui_showHeader = object.optBoolean("gui_showHeader", true);
            gui_showFooter = object.optBoolean("gui_showFooter", true);
            showMessagesFromEnd = object.optBoolean("showMessagesFromEnd", false);
            downloadMode = object.optInt("downloadMode", 0);
            DNR = object.optBoolean("DNR", false);
            DNT = object.optBoolean("DNT", false);
            doNotLoadImages = object.optBoolean("doNotLoadImages", false);
            customAPI = object.optString("customAPI", "");
            customAUTH = object.optString("customAUTH", "");
            customPROXY = object.optString("customPROXY", "");
            //gui_showDock = object.optBoolean("gui_showDock", true);
            gui_showDock = false;
            gui_slimHeader = object.optBoolean("gui_slimHeader", false);
            gui_useDrawer = object.optBoolean("gui_useDrawer", false);
            usePQSofts = object.optBoolean("usePQSofts", false);
            showReplies = object.optBoolean("showReplies", false);
            loggerEnabled = object.optBoolean("loggerEnabled", false);
            oldMessagesSoft = object.optBoolean("oldMessagesSoft", false);
            oldCommentsSoft = object.optBoolean("oldCommentsSoft", false);
            increaseTextLimit = object.optBoolean("increaseTextLimit", false);
            brightness = object.optInt("brightness", 0);
            showCmtFieldByOpen = object.optBoolean("showCmtFieldByOpen", true);
            showMsgFieldByOpen = object.optBoolean("showMsgFieldByOpen", true);
            shiftTitleRight = object.optBoolean("shiftTitleRight", false);
            useKeypadInput = object.optBoolean("useKeypadInput", true);
            sleepWithTimeout = object.optBoolean("sleepWithTimeout", true);
            debug_lp = object.optBoolean("debug_lp", false);
            hideAfterSending = object.optBoolean("hideAfterSending", true);
            typingInDialogs = object.optBoolean("typingInDialogs", true);
            replaceEmailAtYo = object.optBoolean("replaceEmailAtYo", false);
            upperFirstChar = object.optBoolean("upperFirstChar", false);
            sleepTimeout = object.optInt("sleepTimeout", 90);
            setOffline = object.optBoolean("setOffline", true);
            vibroOnSend = object.optBoolean("vibroOnSend", true);
            sendVibroTime = object.optInt("sendVibroTime", 300);
            keyVibroTime = object.optInt("keyVibroTime", 100);
            notVibroTime = object.optInt("notVibroTime", 500);
            use_caching = object.optBoolean("use_caching", false);
            cache_only = object.optBoolean("cache_only", false);
            caching_folder = object.optString("caching_folder", null);
            
            httpPost();
            post();
            innerPost();
        } catch (Throwable e) {
            //e.printStackTrace();
            reset();
        }
    }

    public void httpPost() {
        switch (network_mode) {
            case 2:
                VKConstants.apiUrl = customAPI;
                VKConstants.oauthUrl = customAUTH;
                VKConstants.proxyUrl = null;
                break;
            case 0:
                VKConstants.apiUrl = "http://vk-api-proxy.xtrafrancyz.net/";
                VKConstants.oauthUrl = "http://vk-oauth-proxy.xtrafrancyz.net/";
                VKConstants.proxyUrl = null;
                break;
            case 3:
                VKConstants.apiUrl = "http://openvk.xyz/";
                VKConstants.oauthUrl = "http://openvk.xyz/";
                VKConstants.proxyUrl = null;
                break;
            case 4:
                VKConstants.apiUrl = "https://openvk.uk/";
                VKConstants.oauthUrl = "https://openvk.uk/";
                VKConstants.proxyUrl = null;
                break;
            case 5:
                VKConstants.apiUrl = "https://api.vk.com/";
                VKConstants.oauthUrl = "https://oauth.vk.com/";
                VKConstants.proxyUrl = "http://vk4me.crx.moe/proxy.php";
                break;
            case 6:
                VKConstants.apiUrl = "https://api.vk.com/";
                VKConstants.oauthUrl = "https://oauth.vk.com/";
                VKConstants.proxyUrl = customPROXY;
                break;
            case 7:
                VKConstants.apiUrl = customAPI;
                VKConstants.oauthUrl = customAUTH;
                VKConstants.proxyUrl = customPROXY;
                break;
            //case 1:
            default:
                VKConstants.apiUrl = "https://api.vk.com/";
                VKConstants.oauthUrl = "https://oauth.vk.com/";
                VKConstants.proxyUrl = null;
                break;
        }
    }
    
    public void post() {
        HTTPMEClient.useFlush = useFlushUpload;
        Logger.enabled = loggerEnabled;

        LongPoll.limit = lpSpeed;
        LongPoll.sleptLimit = lpsSpeed;

        User.reverseShortName = gui_reverseNames;
        if (brightness != 0) {
            try {
                com.nokia.mid.ui.DeviceControl.setLights(0, brightness);
            } catch (Throwable e) {
                brightness = 0;
            }
        }
    }

    public void save() {
        saveToken();
        saveInstalled();
        try {
            JSONObject object = new JSONObject();

            object.put("gui_cloudBorder", gui_cloudBorder);
            object.put("gui_reverseNames", gui_reverseNames);
            object.put("gui_avatarCircleType", gui_avatarCircleType);
            object.put("network_mode", network_mode);
            object.put("gui_disableDimm", gui_disableDimm);
            object.put("debug_drawFPS", debug_drawFPS);
            object.put("debug_drawEmojiRed", debug_drawEmojiRed);
            object.put("gui_animations", gui_animations);
            object.put("gui_smoothScroll", gui_smoothScroll);
            object.put("feed_keyVibro", feed_keyVibro);
            object.put("feed_notVibro", feed_notVibro);
            object.put("feed_notSound", feed_notSound);
            object.put("gui_fullscreen", gui_fullscreen);
            object.put("gui_touchHud", gui_touchHud);
            object.put("themeName", themeName);
            object.put("localeCode", localeCode);
            object.put("themeFromRMS", themeFromRMS);
            object.put("localeFromRMS", localeFromRMS);
            object.put("debug_drawImagesFrames", debug_drawImagesFrames);
            object.put("lpSpeed", lpSpeed);
            object.put("lpsSpeed", lpsSpeed);
            object.put("gui_showClouds", gui_showClouds);
            object.put("gui_messageDiffSides", gui_messageDiffSides);
            object.put("gui_messagesAvatars", gui_messagesAvatars);
            object.put("useFlushUpload", useFlushUpload);
            object.put("gui_photosCircleType", gui_photosCircleType);
            object.put("gui_reverseSofts", gui_reverseSofts);
            object.put("translitFiles", translitFiles);
            object.put("gui_drawGradients", gui_drawGradients);
            object.put("gui_showHeader", gui_showHeader);
            object.put("gui_showFooter", gui_showFooter);
            object.put("showMessagesFromEnd", showMessagesFromEnd);
            object.put("downloadMode", downloadMode);
            object.put("DNR", DNR);
            object.put("DNT", DNT);
            object.put("doNotLoadImages", doNotLoadImages);
            object.put("customAPI", customAPI);
            object.put("customAUTH", customAUTH);
            object.put("customPROXY", customPROXY);
            object.put("gui_showDock", gui_showDock);
            object.put("gui_slimHeader", gui_slimHeader);
            object.put("gui_useDrawer", gui_useDrawer);
            object.put("usePQSofts", usePQSofts);
            object.put("showReplies", showReplies);
            object.put("loggerEnabled", loggerEnabled);
            object.put("oldMessagesSoft", oldMessagesSoft);
            object.put("oldCommentsSoft", oldCommentsSoft);
            object.put("increaseTextLimit", increaseTextLimit);
            object.put("brightness", brightness);
            object.put("showCmtFieldByOpen", showCmtFieldByOpen);
            object.put("showMsgFieldByOpen", showMsgFieldByOpen);
            object.put("shiftTitleRight", shiftTitleRight);
            object.put("useKeypadInput", useKeypadInput);
            object.put("sleepWithTimeout", sleepWithTimeout);
            object.put("debug_lp", debug_lp);
            object.put("hideAfterSending", hideAfterSending);
            object.put("typingInDialogs", typingInDialogs);
            object.put("replaceEmailAtYo", replaceEmailAtYo);
            object.put("upperFirstChar", upperFirstChar);
            object.put("sleepTimeout", sleepTimeout);
            object.put("setOffline", setOffline);
            object.put("vibroOnSend", vibroOnSend);
            object.put("sendVibroTime", sendVibroTime);
            object.put("keyVibroTime", keyVibroTime);
            object.put("notVibroTime", notVibroTime);
            object.put("use_caching", use_caching);
            object.put("cache_only", cache_only);
            object.put("caching_folder", caching_folder);
            
            RmsController.saveStore("cfgjson3", object.toString().getBytes(Charset.current));
        } catch (Exception e) {
            Logger.l(e);
        }
    }
}
