package ru.curoviyxru.phoenix.ui.contents;

import ru.curoviyxru.j2vk.api.requests.notifications.NotificationsGet;
import ru.curoviyxru.j2vk.api.responses.notifications.NotificationsGetResponse;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.ListItem;

/**
 *
 * @author curoviyxru
 */
public class NotificationsContent extends Content {
    public NotificationsContent() {
        super(Localization.get("tltle.notifications"));
        
        //TODO: hidden types of notifications
        NotificationsGetResponse response = (NotificationsGetResponse) new NotificationsGet().setCount(5).execute();
        if (response.hasItems()) for (int i = 0; i < response.items.length; ++i) {
            add(new ListItem("test " + i, ListItem.GOTO));
        }
    }
}
