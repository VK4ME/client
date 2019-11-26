package ru.curoviyxru.phoenix;

import java.io.OutputStream;
import javax.microedition.io.Connector;
import ru.curoviyxru.j2vk.HTTPClient;
import ru.curoviyxru.j2vk.ProgressProvider;
import ru.curoviyxru.phoenix.ui.AppCanvas;

/**
 *
 * @author curoviyxru
 */
public class DownloadUtils {

    private static String mp3Type;
    private static Boolean hasHTTPS;

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

    public static boolean folderExists(String path) {
        try {
            javax.microedition.io.file.FileConnection rootConnection = (javax.microedition.io.file.FileConnection) Connector.open(path, Connector.READ_WRITE);
            boolean i = rootConnection.exists() && rootConnection.isDirectory() && rootConnection.canWrite() && rootConnection.canRead();
            rootConnection.close();
            return i;
        } catch (Exception e) {
        }
        return false;
    }

    public static abstract class DProgressProvider implements ProgressProvider {
        String failed;
        boolean successful;
    }

    public static long downloadFile(String url, Object s, ProgressProvider pp, int check) throws Exception {
        AppCanvas.instance.setProgress(true);
        try {
            long l = _downloadFile(url, s, pp, check);
            if (pp != null) {
                if (l != 0) {
                    pp.successful();
                } else {
                    pp.failed("Download failed.");
                }
            }
            AppCanvas.instance.setProgress(false);
            return l;
        } catch (Exception e) {
            if (pp != null) {
                pp.failed(e.toString());
            }
            AppCanvas.instance.setProgress(false);
            throw e;
        }
    }

    public static long _downloadFile(String url, Object conn, final ProgressProvider pp, int check) throws Exception {
//        if (conn.exists()) {
//            conn.delete();
//        }
        javax.microedition.io.file.FileConnection connection = (javax.microedition.io.file.FileConnection) conn;
        if (!connection.exists()) connection.create();
        OutputStream os = connection.openOutputStream();

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

//        if (conn.fileSize() != avaliable) {
//            conn.delete();
//            if (check != -1 && check < 5) {
//                _downloadFile(url, conn, pp, check + 1);
//            } else {
//                throw new Exception("File corrupted.");
//            }
//        } else 
            if (pp != null) {
            pp.setProgress(100);
        }

        return Math.max(connection.fileSize(), avaliable);
    }
}
