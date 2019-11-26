package ru.curoviyxru.phoenix.midlet;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;
import ru.curoviyxru.j2vk.HTTPClient;
import ru.curoviyxru.j2vk.IVKClient;
import ru.curoviyxru.j2vk.LongPoll;
import ru.curoviyxru.j2vk.VKConstants;
import ru.curoviyxru.j2vk.platform.HTTPMEClient;
import ru.curoviyxru.j2vk.platform.StaticTokener;
import ru.curoviyxru.phoenix.Config;
import ru.curoviyxru.phoenix.Logger;
import ru.curoviyxru.phoenix.LongPollClient;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.contents.ContentController;

/**
 * @author curoviyxru
 */
public class Midlet extends MIDlet implements IVKClient {

    public static Midlet instance;

    public static void panic(Throwable ex) {
        Form form = new Form("Application panic");
        form.append(ex.toString());
        Display.getDisplay(instance).setCurrent(form);
    }
    
    public String version;
    public boolean started;
    public boolean paused;
    public Config config;

    protected void startApp() {
        instance = this;
        LongPoll.slowMode = paused = false;
        if (AppCanvas.instance != null) {
            AppCanvas.instance.sleepTime = 0;
        }
        
        if (started) {
            return;
        }

        try {
            version = getAppProperty("MIDlet-Version");
        } catch (Exception e) {
            version = "3.0.0";
        }
        
        try {
            Config.hasEmojis = getAppProperty("VK4ME-Has-Emojis").toLowerCase().equals("true");
        } catch (Exception e) {
            Config.hasEmojis = false;
        }

        try {
            HTTPClient.client = new HTTPMEClient();
            HTTPClient.tokener = new StaticTokener();
            VKConstants.logger = new Logger();
            VKConstants.client = this;
            LongPoll.callback = new LongPollClient();

            new AppCanvas();
            config = new Config();
            
            AppCanvas.instance.showGraphics();
            started = true;
        } catch (Exception e) {
            Logger.l(e);
        }
    }

    protected void pauseApp() {
        paused = true;
        if (config.setOffline && VKConstants.account != null)
            VKConstants.account.setOffline();
        if (!config.sleepWithTimeout)
            LongPoll.slowMode = true;
    }

    protected void destroyApp(boolean unconditional) {
        if (config != null) {
            if (config.setOffline && VKConstants.account != null)
                VKConstants.account.setOffline();
            config.save();
        }
    }

    public void exit() {
        destroyApp(false);
        notifyDestroyed();
    }

    public void saveConfig() {
        if (config != null && VKConstants.account != null) {
            config.rms_access_token = VKConstants.account.getToken();
            config.saveToken();
        }
    }

    public void readConfig() {
        //Nothing is here.
    }

    public void logout() {
        ContentController.logout();
    }

    public static void goLink(String h) {
        try {
            if (h != null) instance.platformRequest(h);
        } catch (Exception e) {
        }
    }
}
