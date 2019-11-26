package ru.curoviyxru.phoenix.ui.contents;

import ru.curoviyxru.j2vk.api.objects.Conversation;
import ru.curoviyxru.j2vk.api.objects.IHistoryConversation;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.Content;

/**
 *
 * @author curoviyxru
 */
public class TwoScrollContent extends Content {

    public int min_id = Integer.MAX_VALUE;
    public final Object lock = new Object();
    public boolean noNext = false, noPrev = false, loadedFirst = false, loading = false;
    public IHistoryConversation c;

    public TwoScrollContent(IHistoryConversation c) {
        super(Localization.get("title.historyContent"));
        this.c = c;
    }

    public void loadFirst() {
        if (loadedFirst || loading) {
            return;
        }

        loading = true;
        new Thread() {
            public void run() {
                try {
                    synchronized (lock) {
                        processFirst();
                        loadedFirst = true;
                        loading = false;
                        onScrollUpdate();
                    }
                } catch (Exception e) {
                    AppCanvas.instance.dropError(e);
                } catch (OutOfMemoryError error) {
                    imOut();
                }

                AppCanvas.instance.setProgress(false);
            }
        }.start();
    }

    public Content imOut() {
        noNext = true;
        return super.imOut();
    }

    public TwoScrollContent(String s, Conversation c) {
        this(c);
        setTitle(s);
    }

    public void onScrollUpdate() {
        if (!noPrev && toScrollY < AppCanvas.instance.perLineSpace) {
            loadPrev();
        } else if (!noNext && totalHeight - toScrollY - contentHeight < AppCanvas.instance.perLineSpace) {
            loadNext();
        }
    }

    public void processNext() {
    }

    public void processFirst() {
    }

    public void processPrev() {
    }

    public void loadPrev() {
        if (loading || noPrev) {
            return;
        }

        loading = true;
        new Thread() {
            public void run() {
                try {
                    synchronized (lock) {
                        while (!noPrev && toScrollY < AppCanvas.instance.perLineSpace)
                            processPrev();
                        loading = false;
                    }
                } catch (Exception e) {
                    loading = false;
                    AppCanvas.instance.dropError(e);
                } catch (OutOfMemoryError error) {
                    loading = false;
                    imOut();
                }

                AppCanvas.instance.setProgress(false);
            }
        }.start();
    }

    public void loadNext() {
        if (loading || noNext) {
            return;
        }
        loading = true;

        new Thread() {
            public void run() {
                try {
                    synchronized (lock) {
                        while (!noNext && totalHeight - toScrollY - contentHeight < AppCanvas.instance.perLineSpace)
                            processNext();
                        loading = false;
                    }
                } catch (Exception e) {
                    loading = false;
                    AppCanvas.instance.dropError(e);
                } catch (OutOfMemoryError error) {
                    loading = false;
                    imOut();
                }

                AppCanvas.instance.setProgress(false);
            }
        }.start();
    }
}
