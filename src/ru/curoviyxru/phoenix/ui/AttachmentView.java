package ru.curoviyxru.phoenix.ui;

import javax.microedition.io.Connector;
import javax.microedition.lcdui.Graphics;
import midletintegration.MIDletIntegration;
import midletintegration.MIDletNotFoundException;
import midletintegration.ProtocolNotSupportedException;
import midletintegration.Util;
import ru.curoviyxru.j2vk.PageStorage;
import ru.curoviyxru.j2vk.ProgressProvider;
import ru.curoviyxru.j2vk.api.objects.Attachment;
import ru.curoviyxru.j2vk.api.objects.PhotoAlbum;
import ru.curoviyxru.j2vk.api.objects.VKObject;
import ru.curoviyxru.j2vk.api.objects.attachments.Audio;
import ru.curoviyxru.j2vk.api.objects.attachments.AudioMessage;
import ru.curoviyxru.j2vk.api.objects.attachments.AudioPlaylist;
import ru.curoviyxru.j2vk.api.objects.attachments.Comment;
import ru.curoviyxru.j2vk.api.objects.attachments.Document;
import ru.curoviyxru.j2vk.api.objects.attachments.Graffiti;
import ru.curoviyxru.j2vk.api.objects.attachments.ImageAttachment;
import ru.curoviyxru.j2vk.api.objects.attachments.Link;
import ru.curoviyxru.j2vk.api.objects.attachments.Photo;
import ru.curoviyxru.j2vk.api.objects.attachments.Post;
import ru.curoviyxru.j2vk.api.objects.attachments.Sticker;
import ru.curoviyxru.j2vk.api.objects.attachments.Video;
import ru.curoviyxru.j2vk.api.requests.audio.AudioAdd;
import ru.curoviyxru.j2vk.api.requests.execute.ExecuteGetPlaylist;
import ru.curoviyxru.j2vk.api.requests.wall.WallGetById;
import ru.curoviyxru.j2vk.api.responses.audio.AudioAddResponse;
import ru.curoviyxru.j2vk.api.responses.execute.ExecuteGetPlaylistResponse;
import ru.curoviyxru.j2vk.api.responses.wall.WallGetByIdResponse;
import ru.curoviyxru.phoenix.AudioCache;
import ru.curoviyxru.phoenix.DownloadUtils;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.Theming;
import ru.curoviyxru.phoenix.kernel.FocusedProgressProvider;
import ru.curoviyxru.phoenix.kernel.ProgressKernel;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.contents.ContentController;
import ru.curoviyxru.phoenix.ui.contents.ImageViewer;
import ru.curoviyxru.playvk.PlayerContent;
import ru.curoviyxru.playvk.Playlist;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class AttachmentView extends ListItem implements ProgressProvider {

    ProgressBar bar;
    Attachment linked;
    int[] wfH;
    int[] wfY;
    int wfP;
    int lW, lH;

    public AttachmentView(Attachment a) {
        super(a.toString(), UNREAD);

        noCircling = true;
        ignoreUnreadBackground = true;
        useFullTimestamp = true;

        setAttachment(a);
    }

    public void setAttachment(Attachment a) {
        this.linked = a;
        if (a == null) {
            return;
        }
        setFont(true);
        setStyle(a); //todo slim style
        initPP();
    }

    private void initPP() {
        FocusedProgressProvider pp = ProgressKernel.getProvider(getName());
        if (pp == null) {
            return;
        }
        pp.setProvider(this);
    }

    private void setStyle(Attachment a) {
        //setTooltipRight(true, false);
        if (a instanceof PhotoAlbum) {
            PhotoAlbum pa = (PhotoAlbum) a;
            String descs = pa.description;
            if (VKObject.isEmpty(descs))
                descs = "";
            setDescription(descs);
            setPhotoAlbum((PhotoAlbum) a);
            setCaption(pa.title);
        } else if (a instanceof Photo) {
            String descs = ((Photo) a).text;
            if (VKObject.isEmpty(descs))
                descs = "";
            setDescription(descs);
            //setIcon("new/image.rle");
            setPhoto((Photo) a);
            setCaption(Localization.get("attachment.photo") + " #" + ((Photo) a).id);
        } else if (a instanceof Sticker) {
            setDescription(a.toString());
            setIcon("new/sticker-emoji.rle");
            setCaption(Localization.get("attachment.sticker"));
        } else if (a instanceof Audio) {
            //TODO playing status?
            Audio au = (Audio) a;

            setDescription(au.artist);
            setAudio(au);
            setCaption(au.title);
            setTimestamp(VKObject.trackTimeToString(au.duration));
        } else if (a instanceof Graffiti) {
            setDescription(a.toString());
            setIcon("new/spray.rle");
            setCaption(Localization.get("attachment.graffiti"));
        } else if (a instanceof AudioMessage) {
            AudioMessage am = (AudioMessage) a;

            setDescription("");
            setTimestamp(VKObject.trackTimeToString(am.duration));
            setIcon("new/voicemail.rle");
            setCaption("");
        } else if (a instanceof Document) {
            Document d = (Document) a;

            setDescription(d.title);
            setTimestamp(Document.getSizeString(d.size));
            setIcon("new/file.rle");
            setCaption(Localization.get("attachment.document"));
        } else if (a instanceof AudioPlaylist) {
            AudioPlaylist ap = (AudioPlaylist) a;

			setAudioPlaylist(ap);
        } else if (a instanceof Video) {
            Video v = (Video) a;

            setDescription(VKObject.isEmpty(v.description) ? Localization.get("attachment.video") : v.description);
            setTimestamp(VKObject.trackTimeToString(v.duration));
            setIcon("new/filmstrip.rle");
            setCaption(v.title);
        } else if (a instanceof Link) {
            Link l = (Link) a;

            setDescription(l.url);
            setIcon("new/link.rle");
            setCaption(Localization.get(l.title));
        } else if (a instanceof Post) {
            Post p = (Post) a;

            setDescription(p.hasText() ? p.text : Localization.get("attachment.wall"));
            setTimestamp(VKObject.dateToString(p.date));
            setIcon("new/newspaper-variant.rle");
            setCaption(PageStorage.get(p.owner_id).getMessageTitle());
        } else if (a instanceof Comment) {
            Comment c = (Comment) a;

            setDescription(c.hasText() ? c.text : Localization.get("attachment.wall_reply"));
            setTimestamp(VKObject.dateToString(c.date));
            setIcon("new/message.rle");
            setCaption(PageStorage.get(c.fromId).getMessageTitle());
        } else {
            setDescription(a.toString());
            setIcon("new/paperclip.rle");
            setCaption(Localization.get("attachment.general"));
        }
    }

    public void actionPerformed() {
        if (linked instanceof Post) {
            ContentController.showComments(content, (Post) linked, null);
        } else if (linked instanceof Comment) {
            Comment c = (Comment) linked;

            WallGetByIdResponse rr = (WallGetByIdResponse) new WallGetById(c.ownerId, c.postId).execute();
            if (rr.post != null) {
                ContentController.showComments(content, rr.post, null);
            }
        } else if (linked instanceof AudioMessage) {
            if (ProgressKernel.hasProvider(getName())) {
                return;
            }

            final AudioMessage d = (AudioMessage) linked;
            downloadAudioMessage(d);
        } else if (linked instanceof Document) {
            if (ProgressKernel.hasProvider(getName())) {
                return;
            }

            final Document d = (Document) linked;
            downloadDocument(d);
        } else if (linked instanceof PhotoAlbum) {
            ContentController.showPhotos(((PhotoAlbum) linked).owner_id, ((PhotoAlbum) linked).id, content);
        } else if (linked instanceof Photo) {
            AppCanvas.instance.goTo(ImageViewer.instance.setPhoto((Photo) linked, this, null).parent(content));
        } else if (linked instanceof ImageAttachment) {
            if (ProgressKernel.hasProvider(getName())) {
                return;
            }

            final ImageAttachment d = (ImageAttachment) linked;
            final String iURL = d.getURL(Integer.MAX_VALUE);
            downloadImageAttachment(d, iURL);
        } else if (linked instanceof AudioPlaylist) {
            AudioPlaylist pl = (AudioPlaylist) linked;
            Content c = new Content(pl.title).parent(this.content);
			
            ExecuteGetPlaylistResponse eresponse = (ExecuteGetPlaylistResponse) new ExecuteGetPlaylist(pl.owner_id, pl.id, pl.access_key).execute();
            if (eresponse != null && eresponse.hasItems()) {
                for (int i = 0; i < eresponse.items.length; i++) {
                    if (eresponse.items[i] != null) {
                        c.add(new AttachmentView(eresponse.items[i]));
                    }
                }
            }
			
            AppCanvas.instance.goTo(c);
        } else if (linked instanceof Audio) {
            if (ProgressKernel.hasProvider(getName())) {
                return;
            }

            final Audio d = (Audio) linked;
            downloadAudio(d);
        } else if (linked instanceof Video) {
            if (ProgressKernel.hasProvider(getName())) {
                return;
            }

            PopupMenu menu = new PopupMenu();

            final Video v = (Video) linked;
            if (v.isExternal()) {
                if (v.player != null && v.player.indexOf("youtube.com") != -1) {
                    menu.add(new PopupButton(Localization.get("action.openJTube")) {
                        public void actionPerformed() {
                            AppCanvas.instance.closePopup();
                            try {
                                if (MIDletIntegration.startApp(Midlet.instance, "JTube", "nnproject", "0xAFCE0816", 1260, "url=" + Util.encodeURL(v.player))) {
                                    Midlet.instance.exit();
                                }
                            } catch (MIDletNotFoundException e) {
                                AppCanvas.instance.dropError(Localization.get("error.jtubeError"));
                                e.printStackTrace();
                            } catch (ProtocolNotSupportedException e) {
                                AppCanvas.instance.dropError("Launching protocol is not supported!");
                                e.printStackTrace();
                            } catch (Exception e) {
                                AppCanvas.instance.dropError(e);
                                e.printStackTrace();
                            }
                        }
                    });
                    menu.add(new PopupButton(Localization.get("action.webPlayer")) {
                        public void actionPerformed() {
                            AppCanvas.instance.closePopup();
                            Midlet.goLink(v.player);
                        }
                    });
                }
            } else {
                if (v.mp4_144 != null) {
                    menu.add(new PopupButton("144p") {
                        public void actionPerformed() {
                            AppCanvas.instance.closePopup();
                            downloadVideo(v, v.mp4_144, "144p");
                        }
                    });
                }
                if (v.mp4_240 != null) {
                    menu.add(new PopupButton("240p") {
                        public void actionPerformed() {
                            AppCanvas.instance.closePopup();
                            downloadVideo(v, v.mp4_240, "240p");
                        }
                    });
                }
                if (v.mp4_360 != null) {
                    menu.add(new PopupButton("360p") {
                        public void actionPerformed() {
                            AppCanvas.instance.closePopup();
                            downloadVideo(v, v.mp4_360, "360p");
                        }
                    });
                }
                if (v.mp4_480 != null) {
                    menu.add(new PopupButton("480p") {
                        public void actionPerformed() {
                            AppCanvas.instance.closePopup();
                            downloadVideo(v, v.mp4_480, "480p");
                        }
                    });
                }
                if (v.mp4_720 != null) {
                    menu.add(new PopupButton("720p") {
                        public void actionPerformed() {
                            AppCanvas.instance.closePopup();
                            downloadVideo(v, v.mp4_720, "720p");
                        }
                    });
                }
                if (v.mp4_1080 != null) {
                    menu.add(new PopupButton("1080p") {
                        public void actionPerformed() {
                            AppCanvas.instance.closePopup();
                            downloadVideo(v, v.mp4_1080, "1080p");
                        }
                    });
                }
                if (v.player != null) {
                    menu.add(new PopupButton(Localization.get("action.webPlayer")) {
                        public void actionPerformed() {
                            AppCanvas.instance.closePopup();
                            Midlet.goLink(v.player);
                        }
                    });
                }
            }

            if (menu.size() == 0) {
                AppCanvas.instance.dropError(Localization.get("error.linksNotFound"));
            } else {
                AppCanvas.instance.showPopup(menu);
            }
        } else if (linked instanceof Link) {
            Midlet.goLink(((Link) linked).url);
        }
    }

    public void downloadVideo(final Video v, final String url, final String selQ) {
        if (v == null || url == null || selQ == null) {
            return;
        }
        switch (Midlet.instance.config.downloadMode) {
            case 0:
                PopupMenu ask = new PopupMenu();
                ask.add(new PopupButton(Localization.get("settings.downloadFile")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        downloadVideo(false, v, url, selQ);
                    }
                }.setIcon("new/cloud-download.rle"));
                ask.add(new PopupButton(Localization.get("settings.openBrowser")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        downloadVideo(true, v, url, selQ);
                    }
                }.setIcon("new/web.rle"));
                AppCanvas.instance.showPopup(ask);
                break;
            case 1:
            case 2:
                downloadVideo(Midlet.instance.config.downloadMode == 2, v, url, selQ);
                break;
        }
    }

    public void downloadAudio(final Audio d) {
        switch (Midlet.instance.config.downloadMode) {
            case 0:
                PopupMenu ask = new PopupMenu();
                ask.add(new PopupButton(Localization.get("settings.addToLibrary")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        addAudio(d);
                    }
                }.setIcon("new/content-copy.rle"));
                ask.add(new PopupButton(Localization.get("action.playAudio")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        PlayerContent.play(AttachmentView.this.content, new Playlist((Audio) linked), 0);
                    }
                }.setIcon("new/play.rle"));
                ask.add(new PopupButton(Localization.get("settings.downloadFile")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        downloadAudio(false, d);
                    }
                }.setIcon("new/cloud-download.rle"));
                ask.add(new PopupButton(Localization.get("settings.openBrowser")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        downloadAudio(true, d);
                    }
                }.setIcon("new/web.rle"));
                AppCanvas.instance.showPopup(ask);
                break;
            case 1:
            case 2:
                downloadAudio(Midlet.instance.config.downloadMode == 2, d);
                break;
        }
    }

    public void addAudio(final Audio d) {
        AudioAddResponse add = (AudioAddResponse) new AudioAdd(d.owner_id, d.id).execute();
        if (!add.isSuccessful()) {
            AppCanvas.instance.dropError(Localization.get("element.addTrackError"));
        }
    }
    
    public void downloadImageAttachment(final ImageAttachment d, final String iURL) {
        if (d == null || iURL == null) {
            return;
        }
        switch (Midlet.instance.config.downloadMode) {
            case 0:
                PopupMenu ask = new PopupMenu();
                ask.add(new PopupButton(Localization.get("settings.downloadFile")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        downloadImageAttachment(false, d, iURL);
                    }
                }.setIcon("new/cloud-download.rle"));
                ask.add(new PopupButton(Localization.get("settings.openBrowser")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        downloadImageAttachment(true, d, iURL);
                    }
                }.setIcon("new/web.rle"));
                AppCanvas.instance.showPopup(ask);
                break;
            case 1:
            case 2:
                downloadImageAttachment(Midlet.instance.config.downloadMode == 2, d, iURL);
                break;
        }
    }

    public void downloadDocument(final Document d) {
        switch (Midlet.instance.config.downloadMode) {
            case 0:
                PopupMenu ask = new PopupMenu();
                ask.add(new PopupButton(Localization.get("settings.downloadFile")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        downloadDocument(false, d);
                    }
                }.setIcon("new/cloud-download.rle"));
                ask.add(new PopupButton(Localization.get("settings.openBrowser")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        downloadDocument(true, d);
                    }
                }.setIcon("new/web.rle"));
                AppCanvas.instance.showPopup(ask);
                break;
            case 1:
            case 2:
                downloadDocument(Midlet.instance.config.downloadMode == 2, d);
                break;
        }
    }

    public void downloadAudioMessage(final AudioMessage d) {
        switch (Midlet.instance.config.downloadMode) {
            case 0:
                PopupMenu ask = new PopupMenu();
                ask.add(new PopupButton(Localization.get("settings.downloadFile")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        downloadAudioMessage(false, d);
                    }
                }.setIcon("new/cloud-download.rle"));
                ask.add(new PopupButton(Localization.get("settings.openBrowser")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        downloadAudioMessage(true, d);
                    }
                }.setIcon("new/web.rle"));
                AppCanvas.instance.showPopup(ask);
                break;
            case 1:
            case 2:
                downloadAudioMessage(Midlet.instance.config.downloadMode == 2, d);
                break;
        }
    }

    public void downloadVideo(final boolean browser, final Video v, final String url, final String selQ) {
        if (browser) {
            Midlet.goLink(url);
            return;
        }

        AppCanvas.instance.goTo(new FilePicker(true, content) {
            public void filePicked(final String path) {
                final FocusedProgressProvider pp = new FocusedProgressProvider(AttachmentView.this);
                ProgressKernel.addProvider(pp);
                try {
                    final javax.microedition.io.file.FileConnection conn = (javax.microedition.io.file.FileConnection) Connector.open(path + v.toString() + "_" + selQ + ".mp4", Connector.READ_WRITE); //TODO check existance
                    if (conn.exists()) {
                        PopupMenu remove = new PopupMenu();

                        String title = Localization.get("fm.replaceConfirm", v.toString() + ".mp4");

                        remove.add(new Label(title).setFont(true).skipSelection(true));

                        remove.add(new PopupButton(Localization.get("action.ok")) {
                            public void actionPerformed() {
                                AppCanvas.instance.closePopup();
                                try {
                                    goBack();
                                    new Thread() {
                                        public void run() {
                                            try {
                                                DownloadUtils.downloadFile(url, conn, pp, 0);
                                                conn.close();
                                            } catch (Exception e) {
                                            }
                                        }
                                    }.start();
                                } catch (Exception e) {
                                    AppCanvas.instance.dropError(e);
                                }
                            }
                        });

                        remove.add(new PopupButton(Localization.get("action.cancel")) {
                            public void actionPerformed() {
                                AppCanvas.instance.closePopup();
                            }
                        });

                        AppCanvas.instance.showPopup(remove);
                    } else {
                        goBack();
                        new Thread() {
                            public void run() {
                                try {
                                    DownloadUtils.downloadFile(url, conn, pp, 0);
                                    conn.close();
                                } catch (Exception e) {
                                }
                            }
                        }.start();
                    }
                } catch (Exception e) {
                    AppCanvas.instance.dropError(e);
                }
            }
        });
    }

    public void downloadAudio(final boolean browser, final Audio d) {
        if (browser) {
            Midlet.goLink(d.url);
            return;
        }

        AppCanvas.instance.goTo(new FilePicker(true, content) {
            public void filePicked(final String path) {
                final FocusedProgressProvider pp = new FocusedProgressProvider(AttachmentView.this);
                ProgressKernel.addProvider(pp);
                try {
                    String filename = AudioCache.get(d);
                    final javax.microedition.io.file.FileConnection conn = (javax.microedition.io.file.FileConnection) Connector.open(path + filename, Connector.READ_WRITE); //TODO check existance
                    if (conn.exists()) {
                        PopupMenu remove = new PopupMenu();

                        String title = Localization.get("fm.replaceConfirm", filename);

                        remove.add(new Label(title).setFont(true).skipSelection(true));

                        remove.add(new PopupButton(Localization.get("action.ok")) {
                            public void actionPerformed() {
                                AppCanvas.instance.closePopup();
                                try {
                                    goBack();
                                    new Thread() {
                                        public void run() {
                                            try {
                                                DownloadUtils.downloadFile(d.url, conn, pp, 0);
                                                conn.close();
                                            } catch (Exception e) {
                                            }
                                        }
                                    }.start();
                                } catch (Exception e) {
                                    AppCanvas.instance.dropError(e);
                                }
                            }
                        });

                        remove.add(new PopupButton(Localization.get("action.cancel")) {
                            public void actionPerformed() {
                                AppCanvas.instance.closePopup();
                            }
                        });

                        AppCanvas.instance.showPopup(remove);
                    } else {
                        goBack();
                        new Thread() {
                            public void run() {
                                try {
                                    DownloadUtils.downloadFile(d.url, conn, pp, 0);
                                    conn.close();
                                } catch (Exception e) {
                                }
                            }
                        }.start();
                    }
                } catch (Exception e) {
                    AppCanvas.instance.dropError(e);
                }
            }
        });
    }

    public void downloadImageAttachment(final boolean browser, final ImageAttachment d, final String iURL) {
        if (browser) {
            Midlet.goLink(iURL);
            return;
        }

        AppCanvas.instance.goTo(new FilePicker(true, content) {
            public void filePicked(final String path) {
                final FocusedProgressProvider pp = new FocusedProgressProvider(AttachmentView.this);
                ProgressKernel.addProvider(pp);
                try {
                    String filename = linked.toString() + (linked instanceof Photo ? ".jpeg" : ".png");
                    final javax.microedition.io.file.FileConnection conn = (javax.microedition.io.file.FileConnection) Connector.open(path + filename, Connector.READ_WRITE); //TODO check existance
                    if (conn.exists()) {
                        PopupMenu remove = new PopupMenu();

                        String title = Localization.get("fm.replaceConfirm", filename);

                        remove.add(new Label(title).setFont(true).skipSelection(true));

                        remove.add(new PopupButton(Localization.get("action.ok")) {
                            public void actionPerformed() {
                                AppCanvas.instance.closePopup();
                                try {
                                    goBack();
                                    new Thread() {
                                        public void run() {
                                            try {
                                                DownloadUtils.downloadFile(iURL, conn, pp, 0);
                                                conn.close();
                                            } catch (Exception e) {
                                            }
                                        }
                                    }.start();
                                } catch (Exception e) {
                                    AppCanvas.instance.dropError(e);
                                }
                            }
                        });

                        remove.add(new PopupButton(Localization.get("action.cancel")) {
                            public void actionPerformed() {
                                AppCanvas.instance.closePopup();
                            }
                        });

                        AppCanvas.instance.showPopup(remove);
                    } else {
                        goBack();
                        new Thread() {
                            public void run() {
                                try {
                                    DownloadUtils.downloadFile(iURL, conn, pp, 0);
                                    conn.close();
                                } catch (Exception e) {
                                }
                            }
                        }.start();
                    }
                } catch (Exception e) {
                    AppCanvas.instance.dropError(e);
                }
            }
        });
    }

    public void downloadDocument(final boolean browser, final Document d) {
        if (browser) {
            Midlet.goLink(d.url);
            return;
        }

        AppCanvas.instance.goTo(new FilePicker(true, content) {
            public void filePicked(final String path) {
                final FocusedProgressProvider pp = new FocusedProgressProvider(AttachmentView.this);
                ProgressKernel.addProvider(pp);
                try {
                    final javax.microedition.io.file.FileConnection conn = (javax.microedition.io.file.FileConnection) Connector.open(path + d.title, Connector.READ_WRITE); //TODO check existance
                    if (conn.exists()) {
                        PopupMenu remove = new PopupMenu();

                        String title = Localization.get("fm.replaceConfirm", d.title);

                        remove.add(new Label(title).setFont(true).skipSelection(true));

                        remove.add(new PopupButton(Localization.get("action.ok")) {
                            public void actionPerformed() {
                                AppCanvas.instance.closePopup();
                                try {
                                    goBack();
                                    new Thread() {
                                        public void run() {
                                            try {
                                                DownloadUtils.downloadFile(d.url, conn, pp, 0);
                                                conn.close();
                                            } catch (Exception e) {
                                            }
                                        }
                                    }.start();
                                } catch (Exception e) {
                                    AppCanvas.instance.dropError(e);
                                }
                            }
                        });

                        remove.add(new PopupButton(Localization.get("action.cancel")) {
                            public void actionPerformed() {
                                AppCanvas.instance.closePopup();
                            }
                        });

                        AppCanvas.instance.showPopup(remove);
                    } else {
                        goBack();
                        new Thread() {
                            public void run() {
                                try {
                                    DownloadUtils.downloadFile(d.url, conn, pp, 0);
                                    conn.close();
                                } catch (Exception e) {
                                }
                            }
                        }.start();
                    }
                } catch (Exception e) {
                    AppCanvas.instance.dropError(e);
                }
            }
        });
    }

    public void downloadAudioMessage(final boolean browser, final AudioMessage d) {
        if (browser) {
            Midlet.goLink(d.link_mp3);
            return;
        }

        AppCanvas.instance.goTo(new FilePicker(true, content) {
            public void filePicked(final String path) {
                final FocusedProgressProvider pp = new FocusedProgressProvider(AttachmentView.this);
                ProgressKernel.addProvider(pp);
                try {
                    final javax.microedition.io.file.FileConnection conn = (javax.microedition.io.file.FileConnection) Connector.open(path + d.toString() + ".mp3", Connector.READ_WRITE);
                    if (conn.exists()) {
                        PopupMenu remove = new PopupMenu();

                        String title = Localization.get("fm.replaceConfirm", d.toString() + ".mp3");

                        remove.add(new Label(title).setFont(true).skipSelection(true));

                        remove.add(new PopupButton(Localization.get("action.ok")) {
                            public void actionPerformed() {
                                AppCanvas.instance.closePopup();
                                try {
                                    goBack();
                                    new Thread() {
                                        public void run() {
                                            try {
                                                DownloadUtils.downloadFile(d.link_mp3, conn, pp, 0);
                                                conn.close();
                                            } catch (Exception e) {
                                            }
                                        }
                                    }.start();
                                } catch (Exception e) {
                                    AppCanvas.instance.dropError(e);
                                }
                            }
                        });

                        remove.add(new PopupButton(Localization.get("action.cancel")) {
                            public void actionPerformed() {
                                AppCanvas.instance.closePopup();
                            }
                        });

                        AppCanvas.instance.showPopup(remove);
                    } else {
                        goBack();
                        new Thread() {
                            public void run() {
                                try {
                                    DownloadUtils.downloadFile(d.link_mp3, conn, pp, 0);
                                    conn.close();
                                } catch (Exception e) {
                                }
                            }
                        }.start();
                    }
                } catch (Exception e) {
                    AppCanvas.instance.dropError(e);
                }
            }
        });
    }

    public void paint(Graphics g, int pY, int pX) {
        super.paint(g, pY, pX);

        if (bar != null) {
            bar.pressed = pressed;
            bar.width = width - 2;
            bar.paint(g, y + pY + height - bar.height, x + pX + 1);
        }

        if (linked instanceof AudioMessage && width > 0 && lW != width && lH != height) {
            lW = width;
            lH = height;

            int useWidth = width - startX - rightCaptionX;
            int[] raw = ((AudioMessage) linked).waveform;
            int dpls = AppCanvas.instance.perLineSpace + AppCanvas.instance.perLineSpace / 2;
            int size = Math.max(1, Math.min(raw.length, (useWidth / Math.max(1, dpls)) - 1));
            int skip = raw.length / size;
            wfH = new int[size];
            wfY = new int[size];
            wfP = useWidth / size;
            int ii = 0;
            int minValue = Integer.MAX_VALUE;
            int maxValue = Integer.MIN_VALUE;

            for (int i = 0; i < raw.length; i += skip) {
                minValue = Math.min(minValue, raw[i]);
                maxValue = Math.max(maxValue, raw[i]);
            }

            int ratio = (height - dpls) / Math.max(1, maxValue - minValue);
            for (int i = 0; i < raw.length; i += skip) {
                wfH[ii] = dpls + (raw[i] - minValue) * ratio;
                wfY[ii] = (height - wfH[ii]) / 2;
                ++ii;
                if (ii >= size) {
                    break;
                }
            }
        }

        if (wfH != null && wfY != null) {
            pX += AppCanvas.instance.perLineSpace * 3 / 2;
            for (int i = 0; i < wfH.length; ++i) {
                RenderUtil.fillRect(g, x + pX + startX, y + pY + wfY[i], AppCanvas.instance.perLineSpace, wfH[i],
                        focusable && pressed ? Theming.now.focusedIconColor : Theming.now.iconColor,
                        focusable && pressed ? Theming.now.focusedIconColor_ : Theming.now.iconColor_);
                pX += wfP;
            }
        }
    }

    public void setProgress(long i) {
        if (bar == null) {
            bar = (ProgressBar) new ProgressBar().setFocusable(true);
        }
        if (bar != null) {
            bar.setProgress(i);
        }
        if (content != null) {
            content.renderIfNeeded();
        }
    }

    public void failed(String s) {
        bar = null;
        if (content != null) {
            content.renderIfNeeded();
        }
        setDescription(Localization.get("general.error") + ": Attachment view: " + s);
        AppCanvas.instance.dropError(s);
    }

    public void successful() {
        bar = null;
        if (content != null) {
            content.renderIfNeeded();
        }
        setDescription(Localization.get("general.downloadFinished"));
    }

    public String getName() {
        return linked == null ? null : linked.toString();
    }
}
