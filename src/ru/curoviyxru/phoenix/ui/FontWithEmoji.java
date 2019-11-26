package ru.curoviyxru.phoenix.ui;

import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import ru.curoviyxru.phoenix.Config;
import ru.curoviyxru.phoenix.Logger;
import ru.curoviyxru.phoenix.Theming;
import ru.curoviyxru.phoenix.midlet.Midlet;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class FontWithEmoji {

    public Font nativeFont;
    public static Thread thr;
    static boolean disallowLoad = !Config.hasEmojis;
    static Hashtable emojis = new Hashtable();
    static Vector toLoad = new Vector();
    public int height;

    public FontWithEmoji(Font font) {
        this.nativeFont = font;
        this.height = Math.max(16, nativeFont.getHeight());
        if (thr == null) {
            thr = new Thread() {
                public void run() {
                    loadEmojis();
                }
            };
            thr.setPriority(Thread.MIN_PRIORITY);
            thr.start();
        }
    }
    
    public void drawString(Graphics g, SuperString str, int x, int y, int origpos) {
        if (str == null || g == null) {
            return;
        }
        
        if ((origpos & Graphics.RIGHT) == Graphics.RIGHT) {
            x -= stringWidth(str);
        }
        if ((origpos & Graphics.HCENTER) == Graphics.HCENTER) {
            x -= stringWidth(str) / 2;
        }
        if ((origpos & Graphics.BOTTOM) == Graphics.BOTTOM) {
            y -= height;
        }
        if ((origpos & Graphics.VCENTER) == Graphics.VCENTER) {
            y -= height / 2;
        }
        
        g.setFont(nativeFont);
        if (!str.hasEmoji) {
            if (str.entities.length > 0) g.drawString(str.entities[0], x, y + (height - nativeFont.getHeight()) / 2, Graphics.LEFT | Graphics.TOP);
            return;
        }
        
        int oldColor = g.getColor();

        int pad = 0;
        for (int i = 0; i < str.types.length; i++) {
            if (str.types[i] == 1) {
                pad += 1; //original: 0
                if (!disallowLoad) {
                    if (!emojis.containsKey(str.entities[i]) && !toLoad.contains(str.entities[i])) {
                        toLoad.addElement(str.entities[i]);
                        g.setColor(Theming.now.nonLoadedContentColor);
                        g.fillRect(x + pad, y + (height - 16) / 2, height, height);
                    } else {
                        Object image = emojis.get(str.entities[i]);
                        if (image instanceof Image) {
                            if (Midlet.instance.config.debug_drawEmojiRed) {
                                g.setColor(0xff0000);
                                g.fillRect(x + pad, y + (height - 16) / 2, ((Image) image).getWidth(), ((Image) image).getHeight());
                            }
                            g.drawImage((Image) image, x + pad, y + (height - 16) / 2, Graphics.LEFT | Graphics.TOP);
                        } else {
                            g.setColor(Theming.now.nonLoadedContentColor);
                            g.fillRect(x + pad, y + (height - 16) / 2, height, height);
                        }
                    }
                } else {
                    g.setColor(Theming.now.nonLoadedContentColor);
                    g.fillRect(x + pad, y + (height - 16) / 2, height, height);
                }
                pad += 17; //original: 16
            } else {
                g.setColor(oldColor);
                g.drawString(str.entities[i], x + pad, y + (height - nativeFont.getHeight()) / 2, Graphics.LEFT | Graphics.TOP);
                pad += nativeFont.stringWidth(str.entities[i]);
            }
        }
        g.setColor(oldColor);
    }

    public void loadEmojis() {
        while (!disallowLoad) {
            try {
                if (toLoad.isEmpty() || AppCanvas.instance.content == null || AppCanvas.instance.slAnim.started) {
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e) {
                    }
                    continue;
                }

                for (int i = 0; i < toLoad.size(); i++) {
                    try {
                        String id = (String) toLoad.elementAt(i);
                        Logger.l("Trying to get emoji " + id);
                        Image get = AppCanvas.loadLocal("emoji/" + id + ".png");
                        if (get == null){
                            Logger.l("Failed to get emoji " + id);
                            //get = AppCanvas.loadImageURL((Midlet.instance.config.network_mode != 1 ? VKConstants.apiUrl + "_/vk.com/emoji/e/" : "https://vk.com/emoji/e/") + id + ".png");
                        }
                        
                        emojis.put(id, get != null ? (Object) get : "null");

                        toLoad.removeElementAt(i);
                        i--;
                        AppCanvas.instance.render();
                    } catch (OutOfMemoryError e) {
                        throw e;
                    } catch (Exception ex) {
                        //AppCanvas.instance.dropError(ex);
                    }
                }
            } catch (OutOfMemoryError e) {
                toLoad.removeAllElements();
                emojis.clear();
                disallowLoad = true;
                System.gc();
                continue;
            }
        }
    }

    public int stringWidth(SuperString str) {
        if (str == null) {
            return 0;
        }

        int width = 0;

        for (int i = 0; i < str.types.length; ++i) {
            width += str.types[i] == 1 ? 18 : nativeFont.stringWidth(str.entities[i]); //original: 16
        }

        return width;
    }
    
    public SuperString[] multiline(SuperString str, int width) {
        if (str == null || str.length == 0) {
            return new SuperString[0];
        }
        Vector v = new Vector();
        
        StringBuffer realBuffer = new StringBuffer();
        StringBuffer textBuffer = new StringBuffer();
        int emojiWidth = 0;
        for (int i = 0; i < str.types.length; ++i) {
            if (str.types[i] == 1) {
                if (emojiWidth + nativeFont.stringWidth(textBuffer.toString()) + 16 > width) {
                    emojiWidth = 0;
                    v.addElement(new SuperString(realBuffer.toString()));
                    textBuffer.setLength(0);
                    realBuffer.setLength(0);
                }
                
                emojiWidth += 18; //original: 16
                realBuffer.append(SuperString.hexToString(str.entities[i]));
            } else {
                String item = str.entities[i];
                for (int j = 0; j < item.length(); ++j) {
                    if (item.charAt(j) == '\n') {
                        emojiWidth = 0;
                        v.addElement(new SuperString(realBuffer.toString()));
                        textBuffer.setLength(0);
                        realBuffer.setLength(0);
                        continue;
                    }
                    
                    realBuffer.append(item.charAt(j));
                    textBuffer.append(item.charAt(j));
                    
                    if (nativeFont.stringWidth(textBuffer.toString()) + emojiWidth > width) {
                        if (realBuffer.toString().lastIndexOf(' ') != -1) {
                            String stt = realBuffer.toString();
                            realBuffer.setLength(0);
                            textBuffer.setLength(0);
                            emojiWidth = 0;
                            int ind = stt.lastIndexOf(' ');
                            v.addElement(new SuperString(stt.substring(0, ind)));
                            textBuffer.append(stt.substring(ind + 1, stt.length()));
                            realBuffer.append(stt.substring(ind + 1, stt.length()));
                        } else {
                            realBuffer.setLength(realBuffer.length() - 1);
                            v.addElement(new SuperString(realBuffer.toString()));
                            realBuffer.setLength(0);
                            textBuffer.setLength(0);
                            emojiWidth = 0;
                            realBuffer.append(item.charAt(j));
                            textBuffer.append(item.charAt(j));
                        }
                    }
                }
            }
        }
        
        if (realBuffer.length() > 0) {
            v.addElement(new SuperString(realBuffer.toString()));
        }
        
        SuperString[] strs = new SuperString[v.size()];
        v.copyInto(strs);
        return strs;
    } 

    public SuperString limit(SuperString str, int width, boolean withDots) {
        if (str == null || str.length == 0) {
            return str;
        }
        
        StringBuffer realBuffer = new StringBuffer();
        StringBuffer textBuffer = new StringBuffer();
        int emojiWidth = 0;
        for (int i = 0; i < str.types.length; ++i) {
            if (str.types[i] == 1) {
                if (emojiWidth + nativeFont.stringWidth(textBuffer.toString().trim() + (withDots ? "..." : "")) + 16 > width) {
                    return new SuperString(realBuffer.toString().trim() + (withDots ? "..." : ""));
                }
                
                emojiWidth += 18; //original: 16
                realBuffer.append(SuperString.hexToString(str.entities[i]));
            } else {
                String item = str.entities[i];
                for (int j = 0; j < item.length(); ++j) {
                    if (item.charAt(j) == '\n') {
                        realBuffer.append(' ');
                        textBuffer.append(' ');
                    } else {
                        realBuffer.append(item.charAt(j));
                        textBuffer.append(item.charAt(j));
                    }
                    
                    if (nativeFont.stringWidth(textBuffer.toString().trim() + (withDots ? "..." : "")) + emojiWidth > width) {
                        realBuffer.setLength(realBuffer.length() - 1);
                        return new SuperString(realBuffer.toString().trim() + (withDots ? "..." : ""));
                    }
                }
            }
        }
        
        return new SuperString(realBuffer.toString());
    }
    
    public SuperString hide(SuperString str, boolean showLast) {
        if (str == null) return null;
        
        int last = str.types.length - 1;
        SuperString str1 = new SuperString(null);
        str1.types = new byte[str.types.length];
        str1.entities = new String[str.entities.length];
        str1.hasEmoji = showLast && str.types.length != 0 ? str.types[last] == 1 : false;
        str1.length = str.length;
        
        for (int i = 0; i < str.types.length - (showLast ? 1 : 0); ++i) {
            str1.types[last] = 0;
            if (str.types[i] == 1) {
                str1.entities[i] = "*";
            } else {
                str1.entities[i] = PaneItem.hide(str.entities[i], false);
            }
        }
        
        if (showLast && str.types.length != 0) {
            if (str.types[last] == 1) {
                str1.types[last] = 1;
                str1.entities[last] = str.entities[last];
            } else {
                str1.types[last] = 0;
                str1.entities[last] = PaneItem.hide(str.entities[last], true);
            }
        }
        
        return str1;
    }
}
