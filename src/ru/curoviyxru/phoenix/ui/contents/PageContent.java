package ru.curoviyxru.phoenix.ui.contents;

import ru.curoviyxru.j2vk.PageStorage;
import ru.curoviyxru.j2vk.TextUtil;
import ru.curoviyxru.j2vk.VKConstants;
import ru.curoviyxru.j2vk.api.objects.VKObject;
import ru.curoviyxru.j2vk.api.objects.user.Group;
import ru.curoviyxru.j2vk.api.objects.user.Page;
import ru.curoviyxru.j2vk.api.objects.user.User;
import ru.curoviyxru.j2vk.api.requests.friends.FriendsAdd;
import ru.curoviyxru.j2vk.api.requests.friends.FriendsDelete;
import ru.curoviyxru.j2vk.api.requests.groups.GroupsJoin;
import ru.curoviyxru.j2vk.api.requests.groups.GroupsLeave;
import ru.curoviyxru.j2vk.api.requests.messages.MessagesGetConversationsById;
import ru.curoviyxru.j2vk.api.requests.wall.WallGet;
import ru.curoviyxru.j2vk.api.responses.friends.FriendsAddResponse;
import ru.curoviyxru.j2vk.api.responses.friends.FriendsDeleteResponse;
import ru.curoviyxru.j2vk.api.responses.groups.GroupsJoinResponse;
import ru.curoviyxru.j2vk.api.responses.groups.GroupsLeaveResponse;
import ru.curoviyxru.j2vk.api.responses.messages.MessagesGetConversationsByIdResponse;
import ru.curoviyxru.j2vk.api.responses.wall.WallGetResponse;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.ui.AppCanvas;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.ConversationItem;
import ru.curoviyxru.phoenix.ui.Footer;
import ru.curoviyxru.phoenix.ui.Label;
import ru.curoviyxru.phoenix.ui.ListItem;
import ru.curoviyxru.phoenix.ui.PaneItem;
import ru.curoviyxru.phoenix.ui.PopupButton;

/**
 *
 * @author curoviyxru, Roman Lahin
 */
public class PageContent extends ScrollContent implements PostContent {

    Page page;
    int deleteFrom = 0;

    public PageContent(long pid) {
        super(Localization.get("title.pageInformation"), false);
        noNext = true;
        PageStorage.load(pid, VKConstants.full_user_fields);
        page = PageStorage.get(pid);
        rightSoft.add(new PopupButton(Localization.get("action.showAvatar")) {
            public void actionPerformed() {
                AppCanvas.instance.closePopup();
                AppCanvas.instance.goTo(ImageViewer.instance.setPhoto(page.isGroup ? page.asGroup().crop_photo : page.asUser().crop_photo, null, null).parent(PageContent.this));
            }
        }.setIcon("new/account.rle"));
        setTitle("@" + page.getNickname());
        load();
    }
    
    public Content removeAll() {
        for (int i = deleteFrom; i < size();) {
            removeAt(i);
        }
        System.gc();
        return this;
    }

    public void addElement(PaneItem i) {
        add(i);
        deleteFrom++;
    }

    public void process() {
        AppCanvas.instance.setProgress(true);
        boolean empty = next == null;
        if (empty) {
            next = new Integer(-1);
        }

        next = new Integer(((Integer) next).intValue() + 1);

        final WallGetResponse rr = (WallGetResponse) new WallGet(page.getId(), 20, ((Integer) next).intValue() * 20).execute();
        if (rr != null && rr.hasItems()) {
            if (rr.items.length < 20) {
                noNext = true;
            }

            for (int i1 = 0; i1 < rr.items.length; i1++) {
                NewsfeedContent.addPost(this, rr.items[i1]);
            }
        } else {
            noNext = true;
            if (rr == null) {
                AppCanvas.instance.dropError(Localization.get("general.loadError"));
            }
        }

        AppCanvas.instance.setProgress(false);
    }

    //todo posts
    public void load() {
        loading = true;
        AppCanvas.instance.setProgress(true);

        if (page != null && page.isGroup) {
            loadGroupInfo();
        } else {
            loadUserInfo();
        }

        loading = false;
        AppCanvas.instance.setProgress(false);
        refresh();
    }

    private void loadGroupInfo() {
        final Group groupPage = page.asGroup();

        if (!VKObject.isEmpty(groupPage.deactivated)) {
            if (groupPage.deactivated.equals(User.DELETED)) {
                addElement(new Label(Localization.get("element.pageDeleted")));
            } else {
                addElement(new Label(Localization.get("element.pageBanned")));
            }
        } else {
            addElement(ContentController.getUserButton(groupPage, false, true, true, true, this).setDescription(Localization.get("element.community")));
            if (!TextUtil.isNullOrEmpty(groupPage.status)) {
                addElement(new Label(groupPage.status).setFocusable(false).skipSelection(true));
            }

            if (groupPage.blacklisted) {
                addElement(new Label(Localization.get("element.groupBlacklisted")));
            } else {
                if (!groupPage.is_member && groupPage.is_closed != Group.OPENED) {
                    if (groupPage.is_closed == Group.CLOSED) {
                        addElement(new Label(Localization.get("element.groupClosed")));
                    } else {
                        addElement(new Label(Localization.get("element.groupPrivate")));
                    }
                }

                if (groupPage.is_member || groupPage.is_closed != Group.PRIVATE) {
                    String friendButtonName = groupPage.is_member ? Localization.get("action.leaveGroup") : groupPage.is_closed == Group.OPENED ? Localization.get("action.joinGroup") : groupPage.invited_by != 0 ? Localization.get("action.acceptInvitation") : Localization.get("action.sendRequest");

                    addElement(new ListItem(friendButtonName) {
                        public void actionPerformed() {
                            if (groupPage.is_member) {
                                GroupsLeaveResponse resp = (GroupsLeaveResponse) new GroupsLeave(groupPage.getOriginalId()).execute();
                                if (resp != null) {
                                    if (resp.response) {
                                        setCaption(groupPage.is_closed == Group.OPENED ? Localization.get("action.joinGroup") : Localization.get("action.sendRequest"));
                                        groupPage.is_member = false;
                                        groupPage.invited_by = 0;
                                        PageStorage.put(groupPage);
                                        page = groupPage;
                                    }
                                } else {
                                    AppCanvas.instance.dropError(Localization.get("element.groupLeaveError"));
                                }
                            } else {
                                GroupsJoinResponse resp1 = (GroupsJoinResponse) new GroupsJoin(groupPage.getOriginalId()).execute();
                                if (resp1 != null) {
                                    if (resp1.response) {
                                        setCaption(Localization.get("action.leaveGroup"));
                                        groupPage.is_member = true;
                                        PageStorage.put(groupPage);
                                        page = groupPage;
                                    }
                                } else {
                                    AppCanvas.instance.dropError(Localization.get("element.groupJoinError"));
                                }
                            }
                        }
                    }.setFont(true));
                }

                if (groupPage.is_member || groupPage.is_closed == Group.OPENED) {
                    if (groupPage.can_message) {
                        addElement(new ListItem(Localization.get("action.writeMessage")) {
                            public void actionPerformed() {
                                MessagesGetConversationsByIdResponse byId = (MessagesGetConversationsByIdResponse) new MessagesGetConversationsById().addPeerId(page.getId()).execute();
                                if (byId == null || !byId.hasConversation()) {
                                    return;
                                }
                                ConversationItem.showConversation(PageContent.this, page.getId());
                            }
                        }.setFont(true));
                    }

                    addElement(new ListItem(Localization.get("action.additionalInfo")) {
                        public void actionPerformed() {
                            AppCanvas.instance.goTo(new PageInfoContent(page).parent(PageContent.this));
                        }
                    }.setIcon("new/information.rle"));
                    
                    if (groupPage.albums_count > 0) {
                        addElement(new ListItem(Localization.get("title.albums"), ListItem.UNREAD) {
                            public void actionPerformed() {
                                ContentController.showAlbums(page.getId(), PageContent.this);
                            }
                        }.setIcon("new/image.rle").setTimestamp(count(groupPage.albums_count)).ignoreUnreadBackground(true));
                    }
                    
                    if (groupPage.audios_count > 0) {
                        addElement(new ListItem(Localization.get("elements.audios"), ListItem.UNREAD) {
                            public void actionPerformed() {
                                ContentController.showTracks(PageContent.this, page.getId());
                            }
                        }.setIcon("new/music-note.rle").setTimestamp(count(groupPage.audios_count)).ignoreUnreadBackground(true));
                    }
                    /*
                     if (groupPage.members_count > 0) {
                     Label l = new Label(groupPage.members_count + " " + getFollowersAddition(groupPage.members_count)) {
                     public void actionPerformed() {
                     new MembersWindow(PageWindow.this, groupPage.getId()).showThis();
                     }
                     };
                     ((PlainContent) l.getContent()).setIcon(Icons.iconsHash, Icons.convMultiple);
                     l.setFocusable(true);
                     pane.addItem(l);
                     }
                     if (groupPage.audios_count > 0) {
                     Label l = new Label(groupPage.audios_count + " " + getAudiosAddition(groupPage.audios_count)) {
                     public void actionPerformed() {
                     new TracksWindow(PageWindow.this, groupPage.getId()).showThis();
                     }
                     };
                     ((PlainContent) l.getContent()).setIcon(Icons.attHash, Icons.attAudio);
                     l.setFocusable(true);
                     pane.addItem(l);
                     }

                     if (groupPage.is_member || groupPage.is_closed == Group.OPENED) {
                     canLoadContent = true;
                     }
                     }
                     */

                    addElement(new Footer());
                    noNext = false;
                }
            }
        }
    }

    private void loadUserInfo() {
        final User userPage = page.asUser();

        if (!VKObject.isEmpty(userPage.deactivated)) {
            if (userPage.deactivated.equals(User.DELETED)) {
                addElement(new Label(Localization.get("element.pageDeleted")));
            } else {
                addElement(new Label(Localization.get("element.pageBanned")));
            }
        } else {
            addElement(ContentController.getUserButton(userPage, false, true, true, true, this));
            if (!TextUtil.isNullOrEmpty(userPage.status)) {
                addElement(new Label(userPage.status).setFocusable(false).skipSelection(true));
            }

            if (userPage.blacklisted) {
                addElement(new Label(Localization.get("element.userBlacklisted")));
            } else {
                String friendButtonName;
                switch (userPage.friend_status) {
                    case User.IS_FRIEND:
                        friendButtonName = Localization.get("action.removeFromFriends");
                        break;
                    case User.FRIEND_INVITE:
                        friendButtonName = Localization.get("action.acceptFriendsRequest");
                        break;
                    case User.FRIEND_SENT:
                        friendButtonName = Localization.get("action.cancelFriendsRequest");
                        break;
                    default:
                        friendButtonName = Localization.get("action.addToFriends");
                        break;
                }
                if (userPage.getId() != VKConstants.account.getId()) {
                    addElement(new ListItem(friendButtonName) {
                        public void actionPerformed() {
                            switch (userPage.friend_status) {
                                case User.FRIEND_SENT:
                                case User.IS_FRIEND:
                                    FriendsDeleteResponse resp = (FriendsDeleteResponse) new FriendsDelete(userPage.getId()).execute();
                                    if (resp != null) {
                                        switch (resp.response) {
                                            case FriendsDeleteResponse.FRIEND_DELETED:
                                                setCaption(Localization.get("action.acceptFriendsRequest"));
                                                userPage.friend_status = User.FRIEND_INVITE;
                                                PageStorage.put(userPage);
                                                page = userPage;
                                                break;
                                            default:
                                                setCaption(Localization.get("action.addToFriends"));
                                                userPage.friend_status = User.NOT_FRIEND;
                                                PageStorage.put(userPage);
                                                page = userPage;
                                                break;
                                        }
                                    } else {
                                        AppCanvas.instance.dropError(Localization.get("element.friendRemoveError"));
                                    }
                                    break;
                                case User.FRIEND_INVITE:
                                default:
                                    FriendsAddResponse resp1 = (FriendsAddResponse) new FriendsAdd(userPage.getId()).execute();
                                    if (resp1 != null) {
                                        switch (resp1.response) {
                                            case FriendsAddResponse.SENT:
                                            case FriendsAddResponse.REPEATED:
                                                setCaption(Localization.get("action.cancelFriendsRequest"));
                                                userPage.friend_status = User.FRIEND_SENT;
                                                PageStorage.put(userPage);
                                                page = userPage;
                                                break;
                                            case FriendsAddResponse.ACCEPTED:
                                                setCaption(Localization.get("action.removeFromFriends"));
                                                userPage.friend_status = User.IS_FRIEND;
                                                PageStorage.put(userPage);
                                                page = userPage;
                                                break;
                                        }
                                    } else {
                                        AppCanvas.instance.dropError(Localization.get("element.friendSendError"));
                                    }
                                    break;
                            }
                        }
                    }.setFont(true));
                }

                if (userPage.can_write_private_message && userPage.getId() != VKConstants.account.getId()) {
                    addElement(new ListItem(Localization.get("action.writeMessage")) {
                        public void actionPerformed() {
                            MessagesGetConversationsByIdResponse byId = (MessagesGetConversationsByIdResponse) new MessagesGetConversationsById().addPeerId(page.getId()).execute();
                            if (byId == null || !byId.hasConversation()) {
                                return;
                            }
                            ConversationItem.showConversation(PageContent.this, page.getId());
                        }
                    }.setFont(true));
                }

                //addElement(new Footer());

                addElement(new ListItem(Localization.get("action.additionalInfo")) {
                    public void actionPerformed() {
                        AppCanvas.instance.goTo(new PageInfoContent(page).parent(PageContent.this));
                    }
                }.setIcon("new/information.rle"));
                
                if (userPage.friends_count > 0) {
                    addElement(new ListItem(Localization.get("elements.friends"), ListItem.UNREAD) {
                        public void actionPerformed() {
                            ContentController.showFriends(PageContent.this, page.getId());
                        }
                    }.setIcon("new/account.rle").setTimestamp((userPage.online_friends > 0 ? count(userPage.online_friends) + " / " : "") + count(userPage.friends_count)).ignoreUnreadBackground(true));
                }
                if (userPage.groups_count > 0) {
                    addElement(new ListItem(Localization.get("elements.groups"), ListItem.UNREAD) {
                        public void actionPerformed() {
                            ContentController.showGroups(PageContent.this, page.getId());
                        }
                    }.setIcon("new/account-supervisor.rle").setTimestamp(count(userPage.groups_count)).ignoreUnreadBackground(true));
                }
                
                if (userPage.albums_count > 0) {
                    addElement(new ListItem(Localization.get("title.albums"), ListItem.UNREAD) {
                        public void actionPerformed() {
                            ContentController.showAlbums(page.getId(), PageContent.this);
                        }
                    }.setIcon("new/image.rle").setTimestamp(count(userPage.albums_count)).ignoreUnreadBackground(true));
                }
                    
                if (userPage.audios_count > 0) {
                    addElement(new ListItem(Localization.get("elements.audios"), ListItem.UNREAD) {
                        public void actionPerformed() {
                            ContentController.showTracks(PageContent.this, page.getId());
                        }
                    }.setIcon("new/music-note.rle").setTimestamp(count(userPage.audios_count)).ignoreUnreadBackground(true));
                }
                
                /* if (userPage.followers_count > 0) {
                 Label l = new Label(userPage.followers_count + " " + getFollowersAddition(userPage.followers_count)) {
                 public void actionPerformed() {
                 new FollowersWindow(PageWindow.this, userPage.getId()).showThis();
                 }
                 };
                 ((PlainContent) l.getContent()).setIcon(Icons.iconsHash, Icons.convMultiple);
                 l.setFocusable(true);
                 pane.addItem(l);
                 } */
                /* if (userPage.can_see_audio && userPage.audios_count > 0) {
                 add(new ListItem("Музыка", ListItem.TEXT) {
                 public void actionPerformed() {
                 //ContentController.showTracks();
                 }
                 }.setIcons(AppCanvas.loadImage("new/music-note.rle"), AppCanvas.loadImage("new/music-note.rle")));
                 } */

                //if (!(userPage.is_closed && !userPage.can_access_closed)) {
                //    canLoadContent = true;
                //}

                addElement(new Footer());
                noNext = false;
            }
        }
    }

    public static String getFollowersString(int num) {
        int preLastDigit = num % 100 / 10;
        if (preLastDigit == 1) {
            return Localization.get("element.followersCount", num + "");
        }

        switch (num % 10) {
            case 1:
                return Localization.get("element.followersCountOne", num + "");
            case 2:
            case 3:
            case 4:
                return Localization.get("element.followersCountSome", num + "");
            default:
                return Localization.get("element.followersCount", num + "");
        }
    }

    public static String count(int i) {
        return count(i, false);
    }

    public static String count(int count, boolean showZero) {
        return count == 0 ? showZero ? "0" : "" : count < 10000 ? count + "" : count < 1000000 ? count / 1000 + "K" : count / 1000000 + "M";
    }

    public static String getTimeActionString(int date) {
        long dates = (System.currentTimeMillis() / 1000 / 86400) - (date / 86400);
        return dates > 1
                ? Localization.get("general.dateAt", VKObject.dateToString(date), VKObject.onlyTimeToString(date))
                : Localization.get(dates == 1 ? "general.yesterdayAt" : "general.todayAt", VKObject.onlyTimeToString(date));
    }

    public static String getLastSeenString(boolean male, int date) {
        return Localization.get("element.lastSeen" + (male ? "Male" : "Female"), getTimeActionString(date));
    }

    public void addPostItem(PaneItem i) {
        add(i);
    }
}
