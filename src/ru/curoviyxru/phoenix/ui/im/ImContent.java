package ru.curoviyxru.phoenix.ui.im;

import ru.curoviyxru.j2vk.api.objects.ImItem;

/**
 *
 * @author curoviyxru
 */
public interface ImContent {
    boolean isChat();

    public boolean canWrite();
    
    public ImField field();

    public void gotDelete(int id);

    public boolean isAdmin();

    public void setThread(ImItem m);

    public void gotEdit(int id);

    public long ownerId();
    
    public long id();

    public void gotNew(int commentId);
    
    boolean isComments();

    public void readTo(ImItem m);

    public int readedTo();

    public int outReadedTo();
    
    public int pinned();
    
    public void setPinned(ImItem m);
}
