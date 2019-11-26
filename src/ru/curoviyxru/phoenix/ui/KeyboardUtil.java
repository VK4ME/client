package ru.curoviyxru.phoenix.ui;

import javax.microedition.lcdui.Display;
import ru.curoviyxru.phoenix.Logger;
import ru.curoviyxru.phoenix.midlet.Midlet;

/**
 *
 * @author curoviyxru
 */
public class KeyboardUtil {

    public static void keyPressed(int key, Field field) {
        Logger.l("[KEY]: " + key);

        if (!isAccepted(key)) {
            return;
        }

        if (field == null) {
            return;
        }
        char c = (char) key;
        if (key == AppCanvas.BACKSPACE) {
            field.setText(field.text == null || field.text.length == 0 ? null : field.text.deleteLast());
        } else if (Character.isDigit(c) || c == '*' || c == '#') {
            digitPressed(field, c, false);
        } else {
            if (key == AppCanvas.SPACE) {
                field.setText((field.text == null ? "" : field.text.toString()) + ' ');
            } else if (key == AppCanvas.ENTER) {
                field.setText((field.text == null ? "" : field.text.toString()) + '\n');
            } else if (key == AppCanvas.FIRE) {
                field.actionPerformed();
                tryVibrate();
            } else {
                if (Midlet.instance.config.replaceEmailAtYo && c == '@')
                    c = 'ё';
                String testText = (field.text == null ? "" : field.text.toString());
                if (!(field instanceof PasswordField) && Midlet.instance.config.upperFirstChar && (field.text == null || field.text.length == 0 || testText.trim().endsWith("."))) {
                    if (Character.isUpperCase(c))
                        c = Character.toLowerCase(c);
                    else c = Character.toUpperCase(c);
                }
                field.setText(testText + c);
            }
        }
        if (field instanceof PasswordField) {
            ((PasswordField) field).showLastChar = 3;
            ((PasswordField) field).hideText = null;
        }

        tryVibrate();
    }

    public static void keyRepeated(int key, Field field) {
        if (!isAccepted(key)) {
            return;
        }

        if (field == null) {
            return;
        }

        char c = (char) key;
        if (Character.isDigit(c) || c == '*' || c == '#') {
            digitPressed(field, c, true);
            tryVibrate();
        } else if (key == AppCanvas.ENTER) {
            //field.actionPerformed();
            //tryVibrate();
        } else {
            keyPressed(key, field);
        }
    }

    //Offset ot fields
    public static boolean isAccepted(int keyCode) {
        switch (keyCode) {
            case AppCanvas.BACKSPACE:
            case AppCanvas.SPACE:
            case AppCanvas.ENTER:
            case AppCanvas.FIRE:
                return true;
        }

        return keyCode >= 32 || Character.isDigit((char) keyCode) || Character.isLowerCase((char) keyCode) || Character.isUpperCase((char) keyCode);
    }

    public static void tryVibrate() {
        if (Midlet.instance.config.feed_keyVibro) {
            Display.getDisplay(Midlet.instance).vibrate(Midlet.instance.config.keyVibroTime);
        }
    }

    public static void messageVibrate() {
        if (Midlet.instance.config.vibroOnSend) {
            Display.getDisplay(Midlet.instance).vibrate(Midlet.instance.config.sendVibroTime);
        }
    }

    static int last = -1, caret = -1, lang = 0, timeoutTicks = 0;
    static char[][] ENG12 = {
        {' ', '*', '#', '+', '0'},
        {'.', ',', '?', '!', '@', '\'', '-', '_', '(', ')', ':', ';', '/', '1'}, {'a', 'b', 'c', '2'}, {'d', 'e', 'f', '3'},
        {'g', 'h', 'i', '4'}, {'j', 'k', 'l', '5'}, {'m', 'n', 'o', '6'},
        {'p', 'q', 'r', 's', '7'}, {'t', 'u', 'v', '8'}, {'w', 'x', 'y', 'z', '9'},
        {' ', '*', '#'}
    };
    static char[][] RUS12 = {
        {' ', '*', '#', '+', '0'},
        {'.', ',', '?', '!', '@', '\'', '-', '_', '(', ')', ':', ';', '/', '1'}, {'а', 'б', 'в', 'г', '2'}, {'д', 'е', 'ё', 'ж', 'з', '3'},
        {'и', 'й', 'к', 'л', '4'}, {'м', 'н', 'о', 'п', '5'}, {'р', 'с', 'т', 'у', '6'},
        {'ф', 'х', 'ц', 'ч', '7'}, {'ш', 'щ', 'ъ', 'ы', '8'}, {'ь', 'э', 'ю', 'я', '9'},
    };
    
    private static void digitPressed(Field field, int i, boolean repeated) {
        if (field == null) return;
        if (!Midlet.instance.config.useKeypadInput) {
            field.setText((field.text == null ? "" : field.text.toString()) + ((char) i));
            return;
        }
        
        if (i == '#') i = 10;
        else if (i == '*') i = 11;
        else i -= 0x30;
        
        if (i == 11) {
            lang = (lang + 1) % 4;
            last = 11;
            caret = -1;
            timeoutTicks = 6;
        } else if (i == 10) {
            last = 10;
            caret = -1;
            timeoutTicks = 6;
            field.setText(field.text == null || field.text.length == 0 ? null : field.text.deleteLast());
        } else if (last != i || caret == -1) {
            last = i;
            caret = 0;
            if (repeated)
                caret = ((lang / 2) == 0 ? ENG12[last].length : RUS12[last].length) - 1;
            timeoutTicks = 6;
            char ch = ((lang / 2) == 0 ? ENG12[last][caret] : RUS12[last][caret]);
            if (lang % 2 == 1) ch = Character.toUpperCase(ch);
            field.setText((field.text == null ? "" : field.text.toString()) + ch);
        } else {
            timeoutTicks = 6;
            caret = (caret + 1) % ((lang / 2) == 0 ? ENG12[last].length : RUS12[last].length);
            if (repeated)
                caret = ((lang / 2) == 0 ? ENG12[last].length : RUS12[last].length) - 1;
            char ch = ((lang / 2) == 0 ? ENG12[last][caret] : RUS12[last][caret]);
            if (lang % 2 == 1) ch = Character.toUpperCase(ch);
            field.setText(field.text == null || field.text.length == 0 ? null : field.text.deleteLast());
            field.setText((field.text == null ? "" : field.text.toString()) + ch);
        }
    }
}
