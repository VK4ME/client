package ru.curoviyxru.playvk;

import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.InputConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VolumeControl;
import ru.curoviyxru.j2vk.HTTPClient;
import ru.curoviyxru.j2vk.ProgressProvider;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class PlayerWrapper implements PlayerListener {

    Player p;
    int volume = 100;
    VolumeControl vc;
    PlayerContent pc;
    int pauseTime = -1;
    private static String mp3Type;
    private static Boolean hasHTTPS;

    public PlayerWrapper(PlayerContent pc) {
        this.pc = pc;
    }

    public void setVolume(int i) {
        volume = Math.max(0, Math.min(100, i));
        if (vc == null) {
            return;
        }
        vc.setLevel(volume);
    }

    public void playerUpdate(Player player, String e, Object o) {
        pc.update();
        if (e.equals(PlayerListener.END_OF_MEDIA)) {
            pc.endOfMedia();
        }
    }

    public void stop() {
        if (p == null) {
            return;
        }
        pauseTime = -1;
        try {
            p.close();
            p = null;
        } catch (Exception e) {
        }

        System.gc();
    }

    public void play() {
        if (p == null) {
            return;
        }

        try {
            if (pauseTime != -1) {
                p.setMediaTime(pauseTime);
            }
            setVolume(volume);
            p.start();
            if (pauseTime != -1) {
                p.setMediaTime(pauseTime);
            }
            pauseTime = -1;
        } catch (MediaException ex) {
            stop();
        }
    }

    public void pause() {
        if (p == null) {
            return;
        }

        try {
            pauseTime = getTime();
            p.stop();
        } catch (MediaException ex) {
            stop();
        }
    }

    public int setTime(int secs) {
        if (p == null) {
            return -1;
        }

        try {
            if (pauseTime != -1) {
                pauseTime = secs;
            }
            long l = p.setMediaTime(secs * 1000000);
            return l == -1 ? -1 : (int) (l / 1000000);
        } catch (Exception e) {
            return -1;
        }
    }

    public int getTime() {
        if (p == null) {
            return -1;
        }

        try {
            if (pauseTime != -1) {
                return pauseTime;
            }
            long l = p.getMediaTime();
            return l == -1 ? -1 : (int) (l / 1000000);
        } catch (Exception e) {
            stop();
            return -1;
        }
    }

    public int getDuration() {
        if (p == null) {
            return -1;
        }

        try {
            long l = p.getDuration();
            return l == -1 ? -1 : (int) (l / 1000000);
        } catch (Exception e) {
            stop();
            return -1;
        }
    }

    public void set(InputConnection s) {
        stop();

        try {
            p = Manager.createPlayer(s.openInputStream(), getMP3Type());
            p.addPlayerListener(this);
            p.realize();
            try {
                vc = (VolumeControl) p.getControl("VolumeControl");
            } catch (Exception e) {
                vc = null;
            }
            setVolume(volume);
            //p.prefetch();
        } catch (Exception e) {
            stop();
        }
    }

    public boolean isStopped() {
        return p == null;
    }

    public static String getMP3Type() {
        if (mp3Type != null) {
            return mp3Type;
        }

        try {
            String[] str = javax.microedition.media.Manager.getSupportedContentTypes(null);

            for (int i = 0; i < str.length; i++) {
                if (str[i] != null && str[i].toLowerCase().equals("audio/mp3")) {
                    return mp3Type = "audio/mp3";
                }
            }
        } catch (Exception e) {
        }

        return mp3Type = "audio/mpeg";
    }

    public static boolean hasHTTPS() {
        if (hasHTTPS != null) {
            return hasHTTPS.booleanValue();
        }

        try {
            String[] protocols = javax.microedition.media.Manager.getSupportedProtocols(null);

            for (int i = 0; i < protocols.length; i++) {
                if (protocols[i] != null && protocols[i].toLowerCase().indexOf("https") != -1) {
                    return (hasHTTPS = Boolean.TRUE).booleanValue();
                }
            }
        } catch (Exception e) {
        }

        return (hasHTTPS = Boolean.FALSE).booleanValue();
    }

    public boolean isPlaying() {
        try {
            return p != null && p.getState() == Player.STARTED;
        } catch (Exception e) {
        }

        return false;
    }

    public static boolean folderExists(String path) {
        try {
            FileConnection rootConnection = (FileConnection) Connector.open(path, Connector.READ_WRITE);
            boolean i = rootConnection.exists() && rootConnection.isDirectory() && rootConnection.canWrite() && rootConnection.canRead();
            rootConnection.close();
            return i;
        } catch (Exception e) {
        }
        return false;
    }

    public interface PWListener {

        public void update();

        public void endOfMedia();
    }

    public static abstract class DProgressProvider implements ProgressProvider {

        String failed;
        boolean successful;
    }

    public static long downloadFile(String url, FileConnection s, ProgressProvider pp, int check) throws Exception {
        try {
            long l = _downloadFile(url, s, pp, check);
            if (pp != null) {
                if (l != 0) {
                    pp.successful();
                } else {
                    pp.failed("File corrupted.");
                }
            }
            return l;
        } catch (Exception e) {
            if (pp != null) {
                pp.failed(e.toString());
            }
            throw e;
        }
    }

    public static long _downloadFile(String url, FileConnection conn, final ProgressProvider pp, int check) throws Exception {
        if (!conn.canWrite()) {
            throw new Exception("Can't write");
        }
        if (conn.exists()) {
            conn.delete();
        }
        conn.create();
        OutputStream os = conn.openOutputStream();

        DProgressProvider pp1 = new DProgressProvider() {
            public void setProgress(long i) {
                if (pp != null) {
                    pp.setProgress(i);
                }
            }

            public void failed(String s) {
                failed = s;
                successful = false;
            }

            public void successful() {
                failed = null;
                successful = true;
            }

            public String getName() {
                if (pp == null) {
                    return null;
                }
                return pp.getName();
            }
        };

        long avaliable = HTTPClient.downloadFile(url, os, pp1, check);

        if (pp1.failed != null) {
            throw new Exception(pp1.failed);
        }

        if (conn.fileSize() != avaliable) {
            conn.delete();
            if (check != -1 && check < 5) {
                _downloadFile(url, conn, pp, check + 1);
            } else {
                throw new Exception("File corrupted.");
            }
        } else if (pp != null) {
            pp.setProgress(100);
        }

        return avaliable;
    }
}
