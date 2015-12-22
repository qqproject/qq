package qq.android.wan.qq;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/12/12.
 */
public class GroupBean {
    private String name;//分组的名字
    private ArrayList<FriendBean> friends;//该组里面的好友
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<FriendBean> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<FriendBean> friends) {
        this.friends = friends;
    }


}
