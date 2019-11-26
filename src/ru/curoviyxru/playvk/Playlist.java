package ru.curoviyxru.playvk;

import java.util.Vector;
import ru.curoviyxru.j2vk.VKConstants;
import ru.curoviyxru.j2vk.api.objects.attachments.Audio;
import ru.curoviyxru.j2vk.api.objects.attachments.AudioPlaylist;
import ru.curoviyxru.j2vk.api.objects.user.Page;

/**
 *
 * @author curoviyxru
 */
public class Playlist {

    String access_key;
    long owner_id;
    int id;
    byte type;
    Vector audios = new Vector();
    public static final byte PLAYLIST = 1, ONE_TRACK = 2, USER_TRACKS = 0;

    public Playlist(AudioPlaylist playlist) {
        owner_id = playlist.owner_id;
        id = playlist.id;
        access_key = playlist.access_key;
        type = PLAYLIST;
    }

    public Playlist(Audio a) {
        owner_id = a.owner_id;
        id = a.id;
        audios.addElement(a);
        type = ONE_TRACK;
    }

    public Playlist(long owner_id) {
        this.owner_id = owner_id;
        id = 0;
        type = USER_TRACKS;
    }

    public Playlist(Page p) {
        this(p.getId());
    }

    public Playlist() {
        this(VKConstants.account.getId());
    }

    public boolean equals(Playlist p) {
        return p != null && p.owner_id == owner_id && p.id == p.id && p.type == type;
    }

    public boolean equals(Object o) {
        return o instanceof Playlist && equals((Playlist) o);
    }
}
