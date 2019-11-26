package ru.curoviyxru.phoenix;

import org.json.me.JSONArray;
import ru.curoviyxru.j2vk.ILongPollCallback;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.contents.ConversationList;
import ru.curoviyxru.phoenix.ui.contents.MessageContent;

/**
 *
 * @author curoviyxru
 */
public class LongPollClient implements ILongPollCallback {

    public void update(JSONArray update) {
        if (update != null) {
            Logger.l("[UPD] "+update.toString());
            int code = update.optInt(0);

            switch (code) {
                case 1:
                case 2:
                    int flags = update.optInt(2);
                    if ((flags & 131072) == 131072 || (flags & 128) == 128) {
                        ConversationList.gotDelete(update.optLong(3));
                        MessageContent.gotDelete(update.optInt(1), update.optLong(3));
                    }
                    break;
                case 4:
                    //нewMessage, vibrate, incomingSound не нужно при out = false
                    //vibrate incomingSound не нужны при вырубленных уведомах
                    ConversationList.gotNew((update.optInt(2) & 2) == 2, update.optLong(3));
                    MessageContent.gotNew(update.optInt(1), update.optLong(3));
                    break;
                case 5:
                    ConversationList.gotEdit(update.optLong(3));
                    MessageContent.gotEdit(update.optInt(1), update.optLong(3));
                    break;
                case 6:
                case 7:
                    ConversationList.gotRead(update.optLong(1));
                    MessageContent.gotRead(code, update.optLong(1), update.optInt(2));
                    break;
                case 8:
                case 9:
                    MessageContent.updateOnline(-update.optLong(1));
                    ConversationList.updateOnline(-update.optLong(1));
                    break;
                case 61:
                    MessageContent.updateTypingUser(update.optLong(1), update.optLong(1));
                    ConversationList.instance.updateTypingUser(update.optLong(1), update.optLong(1));
                    break;
                case 62:
                    MessageContent.updateTypingUser(update.optLong(2), update.optLong(1));
                    ConversationList.instance.updateTypingUser(update.optLong(2), update.optLong(1));
                    break;
                case 63:
                    MessageContent.updateTypingUsers(update.optJSONArray(1), update.optLong(2), update.optInt(4));
                    ConversationList.instance.updateTypingUsers(update.optJSONArray(1), update.optLong(2), update.optInt(4));
                    break;
                case 114:
                    //Updater.updateIsPushesEnabled(update.optJSONObject(1));
                    break;
            }
        }
    }

    public void fetching() {
        AppCanvas.instance.lpFetching = true;
        AppCanvas.instance.render();
    }

    public void sleeping() {
        AppCanvas.instance.lpFetching = false;
        AppCanvas.instance.render();
    }
}
