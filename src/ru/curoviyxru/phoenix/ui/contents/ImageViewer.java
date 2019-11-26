package ru.curoviyxru.phoenix.ui.contents;

import javax.microedition.io.Connector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import ru.curoviyxru.j2vk.ProgressProvider;
import ru.curoviyxru.j2vk.api.objects.attachments.ImageAttachment;
import ru.curoviyxru.j2vk.api.objects.attachments.Photo;
import ru.curoviyxru.j2vk.api.objects.attachments.Photo.Size;
import ru.curoviyxru.phoenix.DownloadUtils;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.Theming;
import ru.curoviyxru.phoenix.kernel.FocusedProgressProvider;
import ru.curoviyxru.phoenix.kernel.ProgressKernel;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.AttachmentView;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.FilePicker;
import ru.curoviyxru.phoenix.ui.ImageItem;
import ru.curoviyxru.phoenix.ui.ImageProvider;
import ru.curoviyxru.phoenix.ui.Label;
import ru.curoviyxru.phoenix.ui.PopupButton;
import ru.curoviyxru.phoenix.ui.PopupMenu;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class ImageViewer extends Content implements ImageProvider, ProgressProvider {
    
    public static ImageViewer instance = new ImageViewer();
    
    AttachmentView aView;
    Image image;
    Photo photo;
    int tries;
    private String size, origSize;
    private int x, y;
    private int sH, sW;
    private ImageItem ii;
	
	private float kineticScrollX = 0, kineticScrollY = 0;
	private float kineticStoreX = 0, kineticStoreY = 0;
	private PopupButton resSwitchButton = null;
    
    public ImageViewer() {
        super(Localization.get("title.imageViewer"));
        
        rightSoft = new PopupMenu(Localization.get("general.actions")) {
			public void actionPerformed(int x, int y, int anchor) {
				if (!open) {
					if(contains(resSwitchButton)) remove(resSwitchButton);
					
					if(canSwitchResolution()) insert(resSwitchButton, 0);
					
					updateHeights(getWidth(), size()); //recalculate container height
				}
				
				super.actionPerformed(x, y, anchor);
			}
		};
				
        resSwitchButton = (PopupButton) new PopupButton(Localization.get("action.switchResolution")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
                switchResolution();
            }
        }.setIcon("new/refresh.rle");
		
        rightSoft.add(new PopupButton(Localization.get("action.downloadImage")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
                if (photo != null) downloadImageAttachment(photo, photo.getURL(Integer.MAX_VALUE));
            }
        }.setIcon("new/cloud-download.rle"));
    }
    
    public ImageViewer setPhoto(Photo p, AttachmentView a, ImageItem ii) {
        this.photo = p;
        this.ii = ii;
        this.aView = a;
        this.image = null;
        this.tries = 0;
        this.size = null;
		
		Size s = (p != null) ? p.getSize(Integer.MAX_VALUE, true) : null;
		this.origSize = (s != null) ? s.type : null;
		
        this.x = this.y = 0;
        this.sH = this.sW = 0;
        this.lastWidth = this.lastHeight = 0;
		
		this.kineticScrollX = 0; this.kineticScrollY = 0;
        
        return this;
    }
    
    public void paint(Graphics g, int pX, int pY, int renderWidth, int renderHeight, int fullHeight, boolean drawBG) {
		lastWidth = renderWidth; 
		lastHeight = renderHeight;
		
        if (drawBG) {
            g.setColor(Theming.now.backgroundColor);
            g.fillRect(pX, pY, renderWidth, renderHeight);
        }
        
        if (photo != null && image == null) {
            if (size == null) {
                Size s = photo.getSize(renderWidth, true);
				
                if (s != null) {
                    size = s.type;
                    sH = s.height;
                    sW = s.width;
                    centralizeImage();
					
					kineticScrollX = kineticScrollY = 0;
                }
            }
            AppCanvas.instance.queue(this, 0); //priority queue
        }
		
		boolean needAnim = false;
		
		if (kineticScrollX != 0) {
            kineticScrollX *= (AppCanvas.instance.pressed ? 0.5f : 0.96f);

            if (kineticScrollX > 1 || kineticScrollX < -1) {
                x += kineticScrollX;
				boundOffsetX();
            } else {
                kineticScrollX = 0;
            }

            needAnim = true;
        }
		
		if (kineticScrollY != 0) {
            kineticScrollY *= (AppCanvas.instance.pressed ? 0.5f : 0.96f);

            if (kineticScrollY > 1 || kineticScrollY < -1) {
                y += kineticScrollY;
				boundOffsetY();
            } else {
                kineticScrollY = 0;
            }

            needAnim = true;
        }
        
        if (image != null) {
            g.drawImage(image, pX + x, pY + y, Graphics.LEFT | Graphics.TOP);
        } else if (sW > 0 && sH > 0) {
            g.setColor(Theming.now.nonLoadedContentColor);
            g.fillRect(pX + x, pY + y, sW, sH);
        }
		
		if(needAnim) AppCanvas.instance.render();
    }
	
	private void boundOffsetX() {
		if (sW > lastWidth) {
			x = Math.max(Math.min(x, 0), lastWidth - sW);
		} else x = (lastWidth - sW) / 2;
	}
	
	private void boundOffsetY() {
		if (sH > lastHeight) {
			y = Math.max(Math.min(y, 0), lastHeight - sH);
		} else y = (lastHeight - sH) / 2;
	}
	
	private void centralizeImage() {
		x = (lastWidth - sW) / 2;
        y = (lastHeight - sH) / 2;
	}

    public void pointerPressed(int x, int y) {
        kineticStoreX = kineticStoreY = 0;
    }

    public void pointerReleased(int x, int y) {
        kineticScrollX += kineticStoreX;
        kineticScrollY += kineticStoreY;
		
        kineticStoreX = kineticStoreY = 0;
    }

    public void pointerDragged(int x, int y) {
		this.kineticStoreX = AppCanvas.instance.pX - AppCanvas.instance.lX; 
		this.kineticStoreY = AppCanvas.instance.pY - AppCanvas.instance.lY; 
		
        this.x += kineticStoreX;
        this.y += kineticStoreY;
		
		boundOffsetX();
		boundOffsetY();
    }

    public void keyRepeated(int k) {
        //avoid it?
        switch (k) {
            case AppCanvas.LEFT:
                x += AppCanvas.instance.lW / 10;
				boundOffsetX();
                break;
            case AppCanvas.RIGHT:
                x -= AppCanvas.instance.lW / 10;
				boundOffsetX();
                break;
            case AppCanvas.UP:
                y += AppCanvas.instance.lH / 10;
				boundOffsetY();
                break;
            case AppCanvas.DOWN:
                y -= AppCanvas.instance.lH / 10;
				boundOffsetY();
                break;
            //case AppCanvas.FIRE:
            //case AppCanvas.ENTER:
            //    switchResolution();
            //    break;
        }
    }

    public void keyReleased(int k) {
        
    }

    public void keyPressed(int k) {
        //do continous navigation
        switch (k) {
            case AppCanvas.LEFT:
                x += AppCanvas.instance.lW / 10;
				boundOffsetX();
                break;
            case AppCanvas.RIGHT:
                x -= AppCanvas.instance.lW / 10;
				boundOffsetX();
                break;
            case AppCanvas.UP:
                y += AppCanvas.instance.lH / 10;
				boundOffsetY();
                break;
            case AppCanvas.DOWN:
                y -= AppCanvas.instance.lH / 10;
				boundOffsetY();
                break;
            case AppCanvas.FIRE:
            case AppCanvas.ENTER:
                if (canSwitchResolution()) switchResolution();
                break;
        }
    }
	
	private boolean canSwitchResolution() {
		//Zoom in
		if (origSize != null && !origSize.equals(size)) return true;
		
		//Zoom out
		if (photo != null) {
			Size s = photo.getSize(lastWidth, true);
			
			if (s != null && origSize != null && origSize.equals(size) && !origSize.equals(s.type)) return true;
		}
		
		return false;
	}
    
    private void switchResolution() {
        if (photo != null) {
            Size s = photo.getSize(size == null || (size.equals(origSize)) ? lastWidth : Integer.MAX_VALUE, true);

            if (s != null) {
				if (s.type.equals(size)) return;
				
                size = s.type;
                image = null;
                tries = 0;

				
				if (sW != 0 && sH != 0) {
					x = (x - lastWidth / 2) * s.width / sW + lastWidth / 2;
					y = (y - lastHeight / 2) * s.height / sH + lastHeight / 2;
				} else centralizeImage();
				
				kineticScrollX = kineticScrollY = 0;
				
                sW = s.width;
                sH = s.height;
				
				boundOffsetX();
				boundOffsetY();
            }
        }
    }

    public boolean local(int i) {
        return false;
    }

    public String get(int i) {
        return photo != null && size != null ? photo.getURL(size) : null;
    }

    public void set(int i, Image image) {
        if (image == null) return;
        this.image = image;
        this.sW = image.getWidth();
        this.sH = image.getHeight();
    }

    public int tries() {
        return tries;
    }

    public void tried() {
        if (tries > 5) return;
        tries++;
    }
    
    public int size() {
        return 1;
    }
    
    public void errored(Throwable ex) {
        AppCanvas.instance.dropError("Image viewer: " + ex != null ? ex.getMessage() : "Unknown error");
    }
    
    public void downloadImageAttachment(final ImageAttachment d, final String iURL) {
        if (d == null || iURL == null) return;
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
    
    public void downloadImageAttachment(final boolean browser, final ImageAttachment d, final String iURL) {
        final Photo linked = photo;
                
        if (browser) {
            Midlet.goLink(iURL);
            return;
        }

        AppCanvas.instance.goTo(new FilePicker(true, ImageViewer.this) {
            public void filePicked(final String path) {
                final FocusedProgressProvider pp = new FocusedProgressProvider(ImageViewer.this);
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

    public void setProgress(long i) {
        if (aView != null) aView.setProgress(i);
        //ImageItem
    }

    public void failed(String s) {
        if (aView != null) aView.failed(s);
        else AppCanvas.instance.dropError("Image viewer:" + s);
        //ImageItem
    }

    public void successful() {
        if (aView != null) aView.successful();
        //ImageItem
    }

    public String getName() {
        return photo == null ? null : photo.toString();
    }
}
