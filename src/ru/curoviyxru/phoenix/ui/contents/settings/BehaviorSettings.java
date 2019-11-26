package ru.curoviyxru.phoenix.ui.contents.settings;

import javax.microedition.lcdui.Graphics;
import ru.curoviyxru.j2vk.VKConstants;
import ru.curoviyxru.phoenix.DownloadUtils;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.Logger;
import ru.curoviyxru.phoenix.Theming;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.Field;
import ru.curoviyxru.phoenix.ui.FilePicker;
import ru.curoviyxru.phoenix.ui.Footer;
import ru.curoviyxru.phoenix.ui.Label;
import ru.curoviyxru.phoenix.ui.ListItem;
import ru.curoviyxru.phoenix.ui.PopupButton;
import ru.curoviyxru.phoenix.ui.PopupMenu;
import ru.curoviyxru.phoenix.ui.Slider;
import ru.curoviyxru.phoenix.ui.contents.SettingsContent;
import ru.curoviyxru.playvk.PlayerWrapper;

/**
 *
 * @author curoviyxru
 */
public class BehaviorSettings extends Content {

    ListItem nm, dm;

    public void showCustomSleepPopup(final Label l, final Slider s, final boolean sleep) {
        PopupMenu rename = new PopupMenu();

        rename.add(new Label(Localization.get("element.setCustomValue")).setFont(true).skipSelection(true));

        final Field textfield = new Field(Localization.get("element.timeInSecs"));
        rename.add(textfield);

        rename.add(new PopupButton(Localization.get("action.ok")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
                String text = textfield.getText();
                try {
                    s.setProgress(Math.max(Integer.parseInt(text != null ? text : "0") - (sleep ? 10 : 1), 0));
                } catch (Throwable e) {
                    s.setProgress(0);
                }
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

    public BehaviorSettings(final boolean logged) {
        super(Localization.get("title.behaviorSettings"));

        add(new Label(Localization.get("settings.general")).setFont(true).skipSelection(true));
        add(new ListItem(Localization.get("settings.increaseTextLimit"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.increaseTextLimit = this.marked();
            }
        }.setState(Midlet.instance.config.increaseTextLimit));
        add(new ListItem(Localization.get("settings.useKeypadInput"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.useKeypadInput = this.marked();
            }
        }.setState(Midlet.instance.config.useKeypadInput));
        add(new ListItem(Localization.get("settings.pqSofts"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.usePQSofts = this.marked();
            }
        }.setState(Midlet.instance.config.usePQSofts));
        add(new ListItem(Localization.get("settings.upperFirstChar"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.upperFirstChar = this.marked();
            }
        }.setDescription(Localization.get("settings.qwertyOnly")).setState(Midlet.instance.config.upperFirstChar));
        add(new ListItem(Localization.get("settings.replaceEmailAtYo"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.replaceEmailAtYo = this.marked();
            }
        }.setDescription(Localization.get("settings.qwertyOnly")).setState(Midlet.instance.config.replaceEmailAtYo));
        add(new ListItem(Localization.get("settings.showReplies"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.showReplies = this.marked();
            }
        }.setState(Midlet.instance.config.showReplies));
        add(new ListItem(Localization.get("settings.translitFiles"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.translitFiles = this.marked();
            }
        }.setState(Midlet.instance.config.translitFiles));
        add(new ListItem(Localization.get("settings.doNotLoadImages"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.doNotLoadImages = this.marked();
            }
        }.setState(Midlet.instance.config.doNotLoadImages));
        add(dm = new ListItem(Localization.get("settings.downloadMode"), ListItem.GOTO) {
            public void actionPerformed() {
                new DownloadModeSettings().parent(BehaviorSettings.this);
            }
        });
        dm.setDescription(Localization.get("settings.currentMode", Localization.get(Midlet.instance.config.downloadMode == 1 ? "settings.downloadFile" : Midlet.instance.config.downloadMode == 2 ? "settings.openBrowser" : "settings.askEverytime")));
        add(new Footer());

        add(new Label(Localization.get("settings.messaging")).setFont(true).skipSelection(true));
        add(new ListItem(Localization.get("settings.showMessagesFromEnd"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.showMessagesFromEnd = this.marked();
            }
        }.setState(Midlet.instance.config.showMessagesFromEnd));
        add(new ListItem(Localization.get("settings.DNR"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.DNR = this.marked();
            }
        }.setState(Midlet.instance.config.DNR));
        add(new ListItem(Localization.get("settings.DNT"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.DNT = this.marked();
            }
        }.setState(Midlet.instance.config.DNT));
        add(new ListItem(Localization.get("settings.typingInDialogs"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.typingInDialogs = this.marked();
            }
        }.setState(Midlet.instance.config.typingInDialogs));
        add(new Footer());

        add(new Label(Localization.get("settings.networkSettings")).setFont(true).skipSelection(true));
        add(nm = new ListItem(Localization.get("settings.networkMode"), ListItem.GOTO) {
            public void actionPerformed() {
                new NetworkModeSettings().parent(BehaviorSettings.this);
            }
        });
        nm.setDescription(Localization.get("settings.currentMode", Localization.get(Midlet.instance.config.network_mode == 1 ? "settings.https" : Midlet.instance.config.network_mode == 2 ? "settings.custom" : Midlet.instance.config.network_mode == 3 ? "settings.openvkHttp" : Midlet.instance.config.network_mode == 4 ? "settings.openvkHttps" : "settings.http")));

        add(new ListItem(Localization.get("settings.useFlushUpload"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.useFlushUpload = this.marked();
                Midlet.instance.config.post();
            }
        }.setDescription(Localization.get("settings.partWarning")).setState(Midlet.instance.config.useFlushUpload));
        add(new Footer());

        add(new Label(Localization.get("settings.feedback")).setFont(true).skipSelection(true));
        add(new ListItem(Localization.get("settings.keyboardVibration"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.feed_keyVibro = this.marked();
            }
        }.setState(Midlet.instance.config.feed_keyVibro));
        add(new ListItem(Localization.get("settings.notificationVibration"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.feed_notVibro = this.marked();
            }
        }.setState(Midlet.instance.config.feed_notVibro));
        add(new ListItem(Localization.get("settings.notificationSound"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.feed_notSound = this.marked();
            }
        }.setState(Midlet.instance.config.feed_notSound));
        add(new ListItem(Localization.get("settings.vibroOnSend"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.vibroOnSend = this.marked();
            }
        }.setState(Midlet.instance.config.vibroOnSend));
        add(new ListItem(Localization.get("settings.sendVibroTime")) {
            public void actionPerformed() {
                PopupMenu rename = new PopupMenu();

                rename.add(new Label(Localization.get("element.setCustomValue")).setFont(true).skipSelection(true));

                final Field textfield = new Field(Localization.get("element.timeInMSecs"));
                textfield.setText(Midlet.instance.config.sendVibroTime + "");
                rename.add(textfield);

                rename.add(new PopupButton(Localization.get("action.ok")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        String text = textfield.getText();
                        try {
                            Midlet.instance.config.sendVibroTime = Math.max(Integer.parseInt(text != null ? text : "10"), 10);
                        } catch (Throwable e) {
                            Midlet.instance.config.sendVibroTime = 10;
                        }
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
        });
        add(new ListItem(Localization.get("settings.keyVibroTime")) {
            public void actionPerformed() {
                PopupMenu rename = new PopupMenu();

                rename.add(new Label(Localization.get("element.setCustomValue")).setFont(true).skipSelection(true));

                final Field textfield = new Field(Localization.get("element.timeInMSecs"));
                textfield.setText(Midlet.instance.config.keyVibroTime + "");
                rename.add(textfield);

                rename.add(new PopupButton(Localization.get("action.ok")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        String text = textfield.getText();
                        try {
                            Midlet.instance.config.keyVibroTime = Math.max(Integer.parseInt(text != null ? text : "10"), 10);
                        } catch (Throwable e) {
                            Midlet.instance.config.keyVibroTime = 10;
                        }
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
        });
        add(new ListItem(Localization.get("settings.notVibroTime")) {
            public void actionPerformed() {
                PopupMenu rename = new PopupMenu();

                rename.add(new Label(Localization.get("element.setCustomValue")).setFont(true).skipSelection(true));

                final Field textfield = new Field(Localization.get("element.timeInMSecs"));
                textfield.setText(Midlet.instance.config.notVibroTime + "");
                rename.add(textfield);

                rename.add(new PopupButton(Localization.get("action.ok")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        String text = textfield.getText();
                        try {
                            Midlet.instance.config.notVibroTime = Math.max(Integer.parseInt(text != null ? text : "10"), 10);
                        } catch (Throwable e) {
                            Midlet.instance.config.notVibroTime = 10;
                        }
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
        });
        add(new Footer());

        add(new Label(Localization.get("settings.caching")).setFont(true).skipSelection(true));
        if (Midlet.instance.config.use_caching) {
            try {
                if (!PlayerWrapper.folderExists(Midlet.instance.config.caching_folder)) {
                    Midlet.instance.config.use_caching = false;
                    AppCanvas.instance.dropError(Localization.get("settings.cachingFolderNotExists"));
                }
            } catch (Throwable e) {
            }
        }
        add(new ListItem(Localization.get("settings.cacheAudio"), ListItem.CHECK) {
            public void stateUpdated() {
                boolean folderExists = false;
                try {
                    folderExists = PlayerWrapper.folderExists(Midlet.instance.config.caching_folder);
                } catch (Throwable e) {
                }
                if (!folderExists) {
                    AppCanvas.instance.dropError(Localization.get("settings.cachingFolderNotExists"));
                }
                setState(Midlet.instance.config.use_caching = this.marked() && folderExists);
            }
        }.setState(Midlet.instance.config.use_caching));
        add(new ListItem(Localization.get("settings.cacheOnly"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.cache_only = this.marked();
            }
        }.setState(Midlet.instance.config.cache_only));
        ListItem item1 = new ListItem(Localization.get("settings.cachingFolder"), ListItem.GOTO) {
            public void actionPerformed() {
                AppCanvas.instance.goTo(new FilePicker(true, BehaviorSettings.this) {
                    public void filePicked(String path) {
                        AppCanvas.instance.backTo(BehaviorSettings.this);
                        Midlet.instance.config.caching_folder = path;
                        try {
                            setDescription(PlayerWrapper.folderExists(Midlet.instance.config.caching_folder) ? Localization.get("settings.cachingFolderSelected") : null);
                        } catch (Throwable e) {
                        }
                    }
                });
            }
        };
        add(item1);
        try {
            item1.setDescription(PlayerWrapper.folderExists(Midlet.instance.config.caching_folder) ? Localization.get("settings.cachingFolderSelected") : null);
        } catch (Throwable e) {
        }
        add(new Footer());

        add(new Label(Localization.get("settings.longpoll")).setFont(true).skipSelection(true));
        final Label lpS = (Label) new Label(Localization.get("settings.longpollSpeed", (Midlet.instance.config.lpSpeed) + " " + Localization.get("general.secs"))).skipSelection(true);
        add(lpS);
        final Slider lpSS = (Slider) new Slider(Midlet.instance.config.lpSpeed - 1) {
            public void valueChanged(long o, long n) {
                lpS.setText(Localization.get("settings.longpollSpeed", (Midlet.instance.config.lpSpeed = (int) (n + 1)) + " " + Localization.get("general.secs")));
                Midlet.instance.config.post();
            }
        }.setMaxProgress(19);
        add(lpSS);
        add(new ListItem(Localization.get("element.setCustomValue")) {
            public void actionPerformed() {
                showCustomSleepPopup(lpS, lpSS, false);
            }
        });
        final Label lpsS = (Label) new Label(Localization.get("settings.longpollSleepSpeed", (Midlet.instance.config.lpsSpeed) + " " + Localization.get("general.secs"))).skipSelection(true);
        add(lpsS);
        final Slider lpsSS = (Slider) new Slider(Midlet.instance.config.lpsSpeed - 10) {
            public void valueChanged(long o, long n) {
                lpsS.setText(Localization.get("settings.longpollSleepSpeed", (Midlet.instance.config.lpsSpeed = (int) (n + 10)) + " " + Localization.get("general.secs")));
                Midlet.instance.config.post();
            }
        }.setMaxProgress(50);
        add(lpsSS);
        add(new ListItem(Localization.get("element.setCustomValue")) {
            public void actionPerformed() {
                showCustomSleepPopup(lpsS, lpsSS, true);
            }
        });
        add(new ListItem(Localization.get("settings.sleepWithTimeout"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.sleepWithTimeout = this.marked();
            }
        }.setState(Midlet.instance.config.sleepWithTimeout));
        final Label lll = (Label) new Label(Localization.get("settings.sleepTimeout", Midlet.instance.config.sleepTimeout + "")).skipSelection(true);
        add(lll);
        add(new ListItem(Localization.get("element.setCustomValue", Midlet.instance.config.sleepTimeout + "")) {
            public void actionPerformed() {
                PopupMenu rename = new PopupMenu();

                rename.add(new Label(Localization.get("element.setCustomValue")).setFont(true).skipSelection(true));

                final Field textfield = new Field(Localization.get("element.timeInSecs"));
                rename.add(textfield);

                rename.add(new PopupButton(Localization.get("action.ok")) {
                    public void actionPerformed() {
                        AppCanvas.instance.closePopup();
                        String text = textfield.getText();
                        try {
                            Midlet.instance.config.sleepTimeout = Math.max(Integer.parseInt(text != null ? text : "10"), 10);
                        } catch (Throwable e) {
                            Midlet.instance.config.sleepTimeout = 10;
                        }
                        lll.setText(Localization.get("settings.sleepTimeout", Midlet.instance.config.sleepTimeout + ""));
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
        });

        add(new Footer());
        add(new Label(Localization.get("settings.debugging")).setFont(true).skipSelection(true));

        add(new ListItem(Localization.get("settings.setOffline"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.setOffline = this.marked();
            }
        }.setState(Midlet.instance.config.setOffline));
        add(new ListItem(Localization.get("settings.loggerEnabled"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.loggerEnabled = this.marked();
                Midlet.instance.config.post();
                if (Midlet.instance.config.loggerEnabled) {
                    AppCanvas.instance.dropMessage(Localization.get("general.caution"), Localization.get("settings.loggingSensitive"));
                } else {
                    Logger.clear();
                }
            }
        }.setState(Midlet.instance.config.loggerEnabled));
        add(new ListItem(Localization.get("settings.flushLog")) {
            public void actionPerformed() {
                if (!Midlet.instance.config.loggerEnabled) {
                    AppCanvas.instance.dropError(Localization.get("settings.logDisabled"));
                } else {
                    AppCanvas.instance.goTo(new FilePicker(true, BehaviorSettings.this) {
                        public void filePicked(String b) {
                            Logger.flushToFile(b);
                            AppCanvas.instance.backTo(BehaviorSettings.this);
                        }
                    });
                }
            }
        });

        add(new ListItem(Localization.get("settings.drawLPState"), ListItem.CHECK) {
            public void stateUpdated() {
                Midlet.instance.config.debug_lp = this.marked();
            }
        }.setState(Midlet.instance.config.debug_lp));

        if (logged) {
            add(new ListItem(Localization.get("settings.showToken")) {
                public void actionPerformed() {
                    PopupMenu p = new PopupMenu();
                    p.add(new Label(Localization.get("element.doNotGiveIt")).setFont(true));
                    p.add(new Label(VKConstants.account.getToken()));
                    p.add(new PopupButton(Localization.get("action.close")) {
                        public void actionPerformed() {
                            AppCanvas.instance.closePopup();
                        }
                    });
                    p.selectedY = 1;
                    AppCanvas.instance.showPopup(p);
                }
            });
        }

        AppCanvas.instance.goTo(this);
    }

    public class NetworkModeSettings extends Content {

        private NetworkModeSettings() {
            super(Localization.get("title.networkMode"));

            add(new ListItem(Localization.get("settings.httpByVK4MEMode"), ListItem.CHECK) {
                public void actionPerformed() {
                    Midlet.instance.config.network_mode = 5;
                    AppCanvas.instance.backTo(BehaviorSettings.this);
                    nm.setDescription(Localization.get("settings.currentMode", Localization.get(Midlet.instance.config.network_mode == 1 ? "settings.https" : Midlet.instance.config.network_mode == 2 ? "settings.custom" : Midlet.instance.config.network_mode == 3 ? "settings.openvkHttp" : Midlet.instance.config.network_mode == 4 ? "settings.openvkHttps" : "settings.http")));

                    AppCanvas.instance.dropMessage(Localization.get("general.caution"), Localization.get("element.proxyDisclaimer"), new Runnable() {
                        public void run() {
                            if (VKConstants.account == null) {
                                Midlet.instance.config.httpPost();
                            } else {
                                AppCanvas.instance.dropError(Localization.get("error.proxyRestartRequired"));
                            }
                        }
                    });
                }
            }.setState(Midlet.instance.config.network_mode == 5).separatorAfter(true));

            add(new ListItem(Localization.get("settings.httpsByVKMode"), ListItem.CHECK) {
                public void actionPerformed() {
                    Midlet.instance.config.network_mode = 1;
                    AppCanvas.instance.backTo(BehaviorSettings.this);
                    nm.setDescription(Localization.get("settings.currentMode", Localization.get(Midlet.instance.config.network_mode == 1 ? "settings.https" : Midlet.instance.config.network_mode == 2 ? "settings.custom" : Midlet.instance.config.network_mode == 3 ? "settings.openvkHttp" : Midlet.instance.config.network_mode == 4 ? "settings.openvkHttps" : "settings.http")));

                    if (VKConstants.account == null) {
                        Midlet.instance.config.httpPost();
                    } else {
                        AppCanvas.instance.dropError(Localization.get("error.proxyRestartRequired"));
                    }
                }
            }.setState(Midlet.instance.config.network_mode == 1).separatorAfter(true));

            add(new ListItem(Localization.get("settings.httpByOpenvk"), ListItem.CHECK) {
                public void actionPerformed() {
                    Midlet.instance.config.network_mode = 3;
                    AppCanvas.instance.backTo(BehaviorSettings.this);
                    nm.setDescription(Localization.get("settings.currentMode", Localization.get(Midlet.instance.config.network_mode == 1 ? "settings.https" : Midlet.instance.config.network_mode == 2 ? "settings.custom" : Midlet.instance.config.network_mode == 3 ? "settings.openvkHttp" : Midlet.instance.config.network_mode == 4 ? "settings.openvkHttps" : "settings.http")));

                    AppCanvas.instance.dropMessage(Localization.get("general.caution"), Localization.get("element.openvkDisclaimer"), new Runnable() {
                        public void run() {
                            if (VKConstants.account == null) {
                                Midlet.instance.config.httpPost();
                            } else {
                                AppCanvas.instance.dropError(Localization.get("error.proxyRestartRequired"));
                            }
                        }
                    });
                }
            }.setState(Midlet.instance.config.network_mode == 3).separatorAfter(true));
            add(new ListItem(Localization.get("settings.httpsByOpenvk"), ListItem.CHECK) {
                public void actionPerformed() {
                    Midlet.instance.config.network_mode = 4;
                    AppCanvas.instance.backTo(BehaviorSettings.this);
                    nm.setDescription(Localization.get("settings.currentMode", Localization.get(Midlet.instance.config.network_mode == 1 ? "settings.https" : Midlet.instance.config.network_mode == 2 ? "settings.custom" : Midlet.instance.config.network_mode == 3 ? "settings.openvkHttp" : Midlet.instance.config.network_mode == 4 ? "settings.openvkHttps" : "settings.http")));

                    AppCanvas.instance.dropMessage(Localization.get("general.caution"), Localization.get("element.openvkDisclaimer"), new Runnable() {
                        public void run() {
                            if (VKConstants.account == null) {
                                Midlet.instance.config.httpPost();
                            } else {
                                AppCanvas.instance.dropError(Localization.get("error.proxyRestartRequired"));
                            }
                        }
                    });
                }
            }.setState(Midlet.instance.config.network_mode == 4).separatorAfter(true));

            add(new ListItem(Localization.get("settings.httpsByCustomMode"), ListItem.CHECK) {
                public void actionPerformed() {
                    Midlet.instance.config.network_mode = 2;
                    AppCanvas.instance.backTo(BehaviorSettings.this);
                    nm.setDescription(Localization.get("settings.currentMode", Localization.get(Midlet.instance.config.network_mode == 1 ? "settings.https" : Midlet.instance.config.network_mode == 2 ? "settings.custom" : Midlet.instance.config.network_mode == 3 ? "settings.openvkHttp" : Midlet.instance.config.network_mode == 4 ? "settings.openvkHttps" : "settings.http")));

                    AppCanvas.instance.dropMessage(Localization.get("general.caution"), Localization.get("element.proxyDisclaimer"), new Runnable() {
                        public void run() {
                            if (VKConstants.account == null) {
                                Midlet.instance.config.httpPost();
                            } else {
                                AppCanvas.instance.dropError(Localization.get("error.proxyRestartRequired"));
                            }
                        }
                    });
                }
            }.setState(Midlet.instance.config.network_mode == 2).separatorAfter(true));

            add(new ListItem(Localization.get("settings.httpByProxy"), ListItem.CHECK) {
                public void actionPerformed() {
                    Midlet.instance.config.network_mode = 6;
                    AppCanvas.instance.backTo(BehaviorSettings.this);
                    nm.setDescription(Localization.get("settings.currentMode", Localization.get(Midlet.instance.config.network_mode == 1 ? "settings.https" : Midlet.instance.config.network_mode == 2 ? "settings.custom" : Midlet.instance.config.network_mode == 3 ? "settings.openvkHttp" : Midlet.instance.config.network_mode == 4 ? "settings.openvkHttps" : "settings.http")));

                    AppCanvas.instance.dropMessage(Localization.get("general.caution"), Localization.get("element.proxyDisclaimer"), new Runnable() {
                        public void run() {
                            if (VKConstants.account == null) {
                                Midlet.instance.config.httpPost();
                            } else {
                                AppCanvas.instance.dropError(Localization.get("error.proxyRestartRequired"));
                            }
                        }
                    });
                }
            }.setState(Midlet.instance.config.network_mode == 6).separatorAfter(true));

            add(new ListItem(Localization.get("settings.httpByProxyAndAPI"), ListItem.CHECK) {
                public void actionPerformed() {
                    Midlet.instance.config.network_mode = 7;
                    AppCanvas.instance.backTo(BehaviorSettings.this);
                    nm.setDescription(Localization.get("settings.currentMode", Localization.get(Midlet.instance.config.network_mode == 1 ? "settings.https" : Midlet.instance.config.network_mode == 2 ? "settings.custom" : Midlet.instance.config.network_mode == 3 ? "settings.openvkHttp" : Midlet.instance.config.network_mode == 4 ? "settings.openvkHttps" : "settings.http")));

                    AppCanvas.instance.dropMessage(Localization.get("general.caution"), Localization.get("element.proxyDisclaimer"), new Runnable() {
                        public void run() {
                            if (VKConstants.account == null) {
                                Midlet.instance.config.httpPost();
                            } else {
                                AppCanvas.instance.dropError(Localization.get("error.proxyRestartRequired"));
                            }
                        }
                    });
                }
            }.setState(Midlet.instance.config.network_mode == 7).separatorAfter(true));

            final Field fAPI, fAUTH, fPROXY;
            add(fPROXY = new Field(Localization.get("settings.customPROXY"), Midlet.instance.config.customPROXY));
            add(fAPI = new Field(Localization.get("settings.customAPI"), Midlet.instance.config.customAPI));
            add(fAUTH = new Field(Localization.get("settings.customAUTH"), Midlet.instance.config.customAUTH));
            add(new ListItem(Localization.get("settings.saveAPIs")) {
                public void actionPerformed() {
                    Midlet.instance.config.customPROXY = fPROXY.getText();
                    Midlet.instance.config.customAPI = fAPI.getText();
                    Midlet.instance.config.customAUTH = fAUTH.getText();
                    if (VKConstants.account == null) {
                        Midlet.instance.config.httpPost();
                    } else {
                        AppCanvas.instance.dropError(Localization.get("error.proxyRestartRequired"));
                    }
                    Midlet.instance.config.save();
                }
            }.separatorAfter(true));
            add(new Label(Localization.get("element.proxyDisclaimer")).setAlign(Graphics.HCENTER).setColor(Theming.now.captionColor)
                    .skipSelection(false));
            add(new Label(Localization.get("element.openvkDisclaimer")).setAlign(Graphics.HCENTER).setColor(Theming.now.captionColor)
                    .skipSelection(false));
            try {
                boolean hasHTTPS = DownloadUtils.hasHTTPS();
                if (!hasHTTPS) {
                    throw new IllegalStateException("No HTTPS.");
                }
            } catch (Throwable e) {
                add(new Label(Localization.get("settings.httpsNotSupportedDisclaimer")).setAlign(Graphics.HCENTER)
                        .setColor(Theming.now.captionColor).skipSelection(false));
            }

            AppCanvas.instance.goTo(this);
        }
    }

    public class DownloadModeSettings extends Content {

        private DownloadModeSettings() {
            super(Localization.get("title.downloadMode"));

            add(new ListItem(Localization.get("settings.askEverytime"), ListItem.CHECK) {
                public void actionPerformed() {
                    Midlet.instance.config.downloadMode = 0;
                    AppCanvas.instance.backTo(BehaviorSettings.this);
                    dm.setDescription(Localization.get("settings.currentMode", Localization.get(Midlet.instance.config.downloadMode == 1 ? "settings.downloadFile" : Midlet.instance.config.downloadMode == 2 ? "settings.openBrowser" : "settings.askEverytime")));
                }
            }.setState(Midlet.instance.config.downloadMode == 0).separatorAfter(true));
            add(new ListItem(Localization.get("settings.downloadFile"), ListItem.CHECK) {
                public void actionPerformed() {
                    Midlet.instance.config.downloadMode = 1;
                    AppCanvas.instance.backTo(BehaviorSettings.this);
                    dm.setDescription(Localization.get("settings.currentMode", Localization.get(Midlet.instance.config.downloadMode == 1 ? "settings.downloadFile" : Midlet.instance.config.downloadMode == 2 ? "settings.openBrowser" : "settings.askEverytime")));
                }
            }.setState(Midlet.instance.config.downloadMode == 1).separatorAfter(true));
            add(new ListItem(Localization.get("settings.openBrowser"), ListItem.CHECK) {
                public void actionPerformed() {
                    Midlet.instance.config.downloadMode = 2;
                    AppCanvas.instance.backTo(BehaviorSettings.this);
                    dm.setDescription(Localization.get("settings.currentMode", Localization.get(Midlet.instance.config.downloadMode == 1 ? "settings.downloadFile" : Midlet.instance.config.downloadMode == 2 ? "settings.openBrowser" : "settings.askEverytime")));
                }
            }.setState(Midlet.instance.config.downloadMode == 2).separatorAfter(true));

            AppCanvas.instance.goTo(this);
        }
    }
}