package ru.curoviyxru.phoenix.ui;

import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.midlet.Midlet;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class FilePicker extends Content {

    boolean chooseFolder;
    Vector selected;
    public static String SEPARATOR = null;
    ListItem upItem, thisItem;
    PopupMenu popup;
    static String currentPath;

    static {
        SEPARATOR = System.getProperty("file.separator");
        if (SEPARATOR == null) {
            SEPARATOR = "/";
        }
    }

    //TODO  (in multiple mode it end selecting, in single just selects), multiple select, slim style
    public FilePicker(boolean chooseFolder, Content parent) {
        super(Localization.get("title.filePicker"));

        parent(parent);
        this.chooseFolder = chooseFolder;

        init();
    }

    public String getRightSoft() {
        if (AppCanvas.instance.touchHud) {
            return null;
        }

        return super.getRightSoft();
    }

    public void reloadContent() {
        if (currentPath == null || currentPath.equals("file://")) {
            loadRoots();
        } else {
            loadFiles(currentPath);
        }
    }

    private void init() {
        setTitle(Localization.get(chooseFolder ? "title.chooseFolder" : "title.chooseFile"));

        popup = new PopupMenu(Localization.get("general.actions"));

        popup.add(new PopupButton(Localization.get(chooseFolder ? "fm.chooseThisFolder" : "fm.chooseThisFile")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
                if (selectedY == 0) {
                    AppCanvas.instance.dropError(Localization.get("fm.pleaseSelectFile"));
                    return;
                }
                if (selectedY == 1 && chooseFolder) {
                    selectFile(currentPath);
                    return;
                }
                ListItem item = (ListItem) getSelected();
                if (isFolder(item) && !chooseFolder) {
                    AppCanvas.instance.dropError(Localization.get("fm.pleaseSelectFile"));
                    return;
                }
                if (!isFolder(item) && chooseFolder) {
                    AppCanvas.instance.dropError(Localization.get("fm.pleaseSelectFile"));
                    return;
                }
                selectFile(currentPath + item.caption + (isFolder(item) ? SEPARATOR : ""));
            }
        }.setIcon("new/paperclip.rle"));

        popup.add(new PopupButton(Localization.get("fm.createFolder")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
                makeFolderPopup();
            }
        }.setIcon("new/folder.rle"));

        popup.add(new PopupButton(Localization.get("fm.rename")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
                if (selectedY == 0) {
                    AppCanvas.instance.dropError(Localization.get("fm.pleaseSelectFile"));
                    return;
                }
                if (selectedY == 1 && chooseFolder) {
                    AppCanvas.instance.dropError(Localization.get("fm.pleaseSelectFile"));
                    return;
                }
                renamePopup();
            }
        }.setIcon("new/pencil.rle"));

        popup.add(new PopupButton(Localization.get("fm.delete")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
                if (selectedY == 0) {
                    AppCanvas.instance.dropError(Localization.get("fm.pleaseSelectFile"));
                    return;
                }
                if (selectedY == 1 && chooseFolder) {
                    AppCanvas.instance.dropError(Localization.get("fm.pleaseSelectFile"));
                    return;
                }
                removeFilePopup();
            }
        }.setIcon("new/delete.rle"));

        popup.add(new PopupButton(Localization.get("action.cancelChoosing")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
                goBack();
            }
        }.setIcon("new/close.rle"));

        reloadContent();
    }

    private void makeFolderPopup() {
        PopupMenu mkdir = new PopupMenu();

        mkdir.add(new Label(Localization.get("fm.enterFolderName")).setFont(true).skipSelection(true));

        final Field textfield = new Field(title != null ? title.toString() : null, Localization.get("fm.newFolder")); //TODO: remove title?
        mkdir.add(textfield);

        mkdir.add(new PopupButton(Localization.get("action.ok")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
                makeFolder(currentPath + textfield.getText() + SEPARATOR);
            }
        });

        mkdir.add(new PopupButton(Localization.get("action.cancel")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
            }
        });

        AppCanvas.instance.showPopup(mkdir);
        mkdir.selectedY = 1;
    }

    private void makeFolder(String folder) {
        try {
            javax.microedition.io.file.FileConnection conn = null;

            try {
                conn = (javax.microedition.io.file.FileConnection) Connector.open(folder);
                if (!conn.exists()) {
                    conn.mkdir();
                } else {
                    throw new Exception(Localization.get("fm.folderExists"));
                }
                conn.close();
                AppCanvas.instance.closePopup();

                loadFiles(currentPath);
                selectFileInList(folder.substring(currentPath.length(),
                        folder.length() - SEPARATOR.length()));

            } catch (Exception e) {
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (Exception ex) {
                }

                AppCanvas.instance.dropError(e);
            }
        } catch (Throwable e) {
            AppCanvas.instance.dropError(e);
        }
    }

    private void renamePopup() {
        PopupMenu rename = new PopupMenu();

        final ListItem item = (ListItem) getSelected();
        String title = Localization.get("fm.enterNewName", item.caption != null ? item.caption.toString() : null);

        rename.add(new Label(title).setFont(true).skipSelection(true));

        final Field textfield = new Field(title, item.caption != null ? item.caption.toString() : null);
        rename.add(textfield);

        rename.add(new PopupButton(Localization.get("action.ok")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
                renameFile(textfield.getText(),
                        currentPath + item.caption + (isFolder(item) ? SEPARATOR : ""));
            }
        });

        rename.add(new PopupButton(Localization.get("action.cancel")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
            }
        });

        AppCanvas.instance.showPopup(rename);
        rename.selectedY = 1;
    }

    private void renameFile(String newName, String oldPath) {
        try {
            javax.microedition.io.file.FileConnection conn = null;

            try {
                conn = (javax.microedition.io.file.FileConnection) Connector.open(oldPath);
                conn.rename(newName);
                conn.close();
                AppCanvas.instance.closePopup();

                loadFiles(currentPath);
                selectFileInList(newName);

            } catch (Exception e) {
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (Exception ex) {
                }

                AppCanvas.instance.dropError(e);
            }
        } catch (Throwable e) {
            AppCanvas.instance.dropError(e);
        }
    }

    private void removeFilePopup() {
        PopupMenu remove = new PopupMenu();

        final ListItem item = (ListItem) getSelected();
        String title = Localization.get("fm.deleteConfirm", item.caption != null ? item.caption.toString() : null);

        remove.add(new Label(title).setFont(true).skipSelection(true));

        remove.add(new PopupButton(Localization.get("action.ok")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
                deleteFile(currentPath + item.caption + (isFolder(item) ? SEPARATOR : ""));
            }
        });

        remove.add(new PopupButton(Localization.get("action.cancel")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
            }
        });

        AppCanvas.instance.showPopup(remove);
        remove.selectedY = 2;
    }

    private void deleteFile(String path) {
        try {
            //System.out.println(path);
            javax.microedition.io.file.FileConnection conn = null;

            try {
                conn = (javax.microedition.io.file.FileConnection) Connector.open(path);
                conn.delete();
                conn.close();
                AppCanvas.instance.closePopup();

                int oldSelectedY = selectedY;
                loadFiles(currentPath);

                selectedY = oldSelectedY;
                if (selectedY >= size()) {
                    selectedY = size() - 1;
                }

                updateHeights(getWidth(), selectedY + 1);
                scrollTo(selectedY);
                scrollY = toScrollY;
            } catch (Exception e) {
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (Exception ex) {
                }

                AppCanvas.instance.dropError(e);
            }
        } catch (Throwable e) {
            AppCanvas.instance.dropError(e);
        }
    }

    public void showPopup() {
        popup.selectedY = 0;
        popup.scrollY = popup.toScrollY = 0;
        AppCanvas.instance.showPopup(popup);
    }

    public void keyPressed(int p) {
        if (p == AppCanvas.LEFT) {
            if (upItem != null) {
                upItem.actionPerformed();
            } else {
                goBack();
            }
        } else if (p == AppCanvas.BACKSPACE) {
            if (selectedY == 0) {
                AppCanvas.instance.dropError(Localization.get("fm.pleaseSelectFile"));
            } else {
                removeFilePopup();
            }
        } else {
            super.keyPressed(p == AppCanvas.RIGHT ? AppCanvas.FIRE : p);
        }
    }

    public void filePicked(String path) {
        goBack();
    }

    public static boolean checkAPIAvailiability() {
        return System.getProperty("microedition.io.file.FileConnection.version") != null;
    }

    public static boolean checkPermission() {
        return Midlet.instance.checkPermission("javax.microedition.io.Connector.file.read") != 0;
    }

    public static String getName(String path) {
        if (path == null) {
            return null;
        }
        int i = path.lastIndexOf(SEPARATOR.charAt(0));
        if (i == -1) {
            return path;
        }
        return path.substring(i + 1);
    }

    private void loadRoots() {
        setTitle(Localization.get(chooseFolder ? "title.chooseFolder" : "title.chooseFile"));
        rightSoft = null;
        currentPath = "file://";
        removeAll();

        upItem = null;
        thisItem = null;
        this.selectedY = 0;
        this.scrollY = 0;
        this.toScrollY = 0;

        Enumeration roots = null;
        try {
            roots = javax.microedition.io.file.FileSystemRegistry.listRoots();
        } catch (Throwable e) {
            AppCanvas.instance.dropError(e);
        }
        String currentRoot;
        while (roots != null && roots.hasMoreElements()) {
            currentRoot = (String) roots.nextElement();
            if (currentRoot != null) {
                if (currentRoot.endsWith("/") && !SEPARATOR.equals("/")) {
                    SEPARATOR = "/";
                }
                if (currentRoot.endsWith("/") || currentRoot.endsWith("\\")) {
                    currentRoot = currentRoot.substring(0, currentRoot.length() - 1);
                }
                final String cur = currentRoot;
                ListItem rootItem = (ListItem) new ListItem(cur, ListItem.GOTO) {
                    public void actionPerformed() {
                        loadFiles(cur);
                    }
                }.setDescription(AppCanvas.instance.touchHud ? Localization.get("general.root") : null).setIcon("new/sd.rle");
                this.add(rootItem);
            }
        }
    }

    private void loadFiles(String currentRoot) {
        final String path = (currentRoot.startsWith("file://") ? "" : currentRoot.startsWith(SEPARATOR) ? "file://" : "file://" + SEPARATOR) + currentRoot + (!currentRoot.endsWith(SEPARATOR) ? SEPARATOR : "");
        final String parent = path.substring(0, path.length() - 1).substring(0, path.substring(0, path.length() - 1).lastIndexOf(SEPARATOR.charAt(0)));
        final boolean isParentRoot = parent.equals("file://");

        setTitle(path.substring(7));
        rightSoft = popup;
        currentPath = path;
        removeAll();
        this.selectedY = 0;
        this.scrollY = 0;
        this.toScrollY = 0;

        upItem = (ListItem) new ListItem(".." + SEPARATOR, ListItem.GOTO) {
            public void actionPerformed() {
                if (isParentRoot) {
                    loadRoots();
                } else {
                    loadFiles(parent);
                }

                selectFileInList(path.substring(
                        parent.length() + SEPARATOR.length(),
                        path.length() - SEPARATOR.length()));
            }
        }.setDescription(AppCanvas.instance.touchHud ? Localization.get("action.goUpwards") : null).setIcon("new/chevron-up.rle");
        this.add(upItem);

        try {
            int folderCount = 1;
            javax.microedition.io.file.FileConnection rootConnection = (javax.microedition.io.file.FileConnection) Connector.open(path, Connector.READ);
            if (rootConnection.isDirectory()) {
                Enumeration rootFiles = rootConnection.list();
                String currentFile;
                while (rootFiles.hasMoreElements()) {
                    currentFile = (String) rootFiles.nextElement();
                    if (currentFile != null) {
                        final String curr = currentFile;
                        final boolean isFolder = curr.endsWith(SEPARATOR);

                        String displayName = curr;
                        if (isFolder) {
                            displayName = curr.substring(0, curr.length() - SEPARATOR.length());
                        }

                        ListItem fileItem = (ListItem) new ListItem(displayName,
                                isFolder ? ListItem.GOTO : ListItem.TEXT) {
                            public void actionPerformed() {
                                if (isFolder) {
                                    loadFiles(path + curr);
                                } else {
                                    selectFile(path + curr + (isFolder && !curr.endsWith(SEPARATOR) ? SEPARATOR : ""));
                                }
                            }

                            public void iconPressPerformed() {
                                showPopup();
                            }
                        }.setDescription(getDescription(curr)).setIcon(getIcon(curr));
                        if (isFolder) {
                            this.insert(fileItem, folderCount);
                            folderCount++;
                        } else {
                            this.add(fileItem);
                        }
                    }
                }
            }
            rootConnection.close();
        } catch (Throwable expected) {
        }

        if (chooseFolder) {
            thisItem = (ListItem) new ListItem(Localization.get("fm.chooseThisFolder")) {
                public void actionPerformed() {
                    selectFile(path);
                }
            }.setDescription(AppCanvas.instance.touchHud ? path.substring(parent.length() + 1) : null).setIcon("new/folder-open.rle");
            this.insert(thisItem, 1);
        } else {
            thisItem = null;
        }
    }

    public void selectFileInList(String name) {
        int id = findElement(name);

        if (id != -1) {
            updateHeights(getWidth(), id + 1);
            scrollTo(id);
            scrollY = toScrollY;
        }
    }

    public int findElement(String name) {
        for (int i = 0; i < size(); i++) {
            Object obj = at(i);
            if (obj instanceof ListItem && ((ListItem) obj).caption.equals(name)) {
                return i;
            }
        }

        return -1;
    }

    public static boolean isImage(String name) {
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".bmp");
    }

    public static String getDescription(String name) {
        if (!AppCanvas.instance.touchHud) {
            return null;
        }

        name = getIcon(name);

        if (name.equals("new/file.rle")) {
            return Localization.get("file.file");
        } else if (name.equals("new/music-note.rle")) {
            return Localization.get("file.audio");
        } else if (name.equals("new/image.rle")) {
            return Localization.get("file.photo");
        } else if (name.equals("new/filmstrip.rle")) {
            return Localization.get("file.video");
        } else if (name.equals("new/folder.rle")) {
            return Localization.get("file.folder");
        }

        return null;
    }

    public static String getIcon(String name) {
        if (name == null) {
            return "new/file.rle";
        }

        name = name.toLowerCase();

        if (name.endsWith(SEPARATOR)) {
            return "new/folder.rle";
        } else if (name.endsWith(".mp3") || name.endsWith(".aac") || name.endsWith(".ogg") || name.endsWith(".wav")) {
            return "new/music-note.rle";
        } else if (isImage(name)) {
            return "new/image.rle";
        } else if (name.endsWith(".3gp") || name.endsWith(".avi") || name.endsWith(".mkv") || name.endsWith(".mp4")) {
            return "new/filmstrip.rle";
        }

        return "new/file.rle";
    }

    public static boolean isFolder(ListItem item) {
        return item.iconObj != null && item.iconObj.equals(getIcon(SEPARATOR));
    }

    private void selectFile(String path) {
        filePicked(path);
    }
}
