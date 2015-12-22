package qq.android.wan.qq;

import android.app.ExpandableListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/12/11.
 */
public class ContactFragment extends Fragment implements View.OnClickListener {

    private android.widget.ExpandableListView elv_lists;
    private ArrayList<GroupBean> groups;
    private View head;
    private MyAdater adater;
    private View viewParent;
    private LinearLayout layout_new_friend, layout_heart, layout_group, layout_gzh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initUI(inflater);
        initListener();
        //初始化模拟数据
        groups = new ArrayList<GroupBean>();
        for (int i = 0; i < 10; i++) {
            GroupBean tmp1 = new GroupBean();
            ArrayList<FriendBean> tmp2 = new ArrayList<FriendBean>();
            tmp1.setName("这是第" + i + "组");
            for (int j = 0; j < 15; j++) {
                FriendBean tmp3 = new FriendBean();
                tmp3.setNick("我是该组里第" + j + "个好友");
                tmp3.setNum("" + i + j + "");
                tmp2.add(tmp3);
            }
            tmp1.setFriends(tmp2);
            groups.add(tmp1);
        }
        adater = new MyAdater();
        elv_lists.setAdapter(adater);

        return viewParent;

    }

    private void initUI(LayoutInflater inflater) {
        viewParent = inflater.inflate(R.layout.fragment_tab2, null);
        elv_lists = (ExpandableListView) viewParent.findViewById(R.id.elv_lists);
        head = getActivity().getLayoutInflater().inflate(R.layout.tab2_head, null);
        layout_new_friend = (LinearLayout) head.findViewById(R.id.layout_new_friend);
        layout_heart = (LinearLayout) head.findViewById(R.id.layout_heart);
        layout_group = (LinearLayout) head.findViewById(R.id.layout_group);
        layout_gzh = (LinearLayout) head.findViewById(R.id.layout_gzh);
        elv_lists.addHeaderView(head);
    }

    private void initListener() {
        layout_new_friend.setOnClickListener(this);
        layout_heart.setOnClickListener(this);
        layout_group.setOnClickListener(this);
        layout_gzh.setOnClickListener(this);
        elv_lists.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (parent.isGroupExpanded(groupPosition)) {
                    parent.collapseGroup(groupPosition);
                } else {
                    //第二个参数false表示展开时是否触发默认滚动动画
                    parent.expandGroup(groupPosition, false);
                }
                //telling the listView we have handled the group click, and don't want the default actions.
                return true;
            }
        });
        elv_lists.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final PopupWindow popupWindow = new PopupWindow(getActivity());
                Log.e("test","点击了第几个:"+position+"  view="+view+"   id="+id);
                popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
                popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                popupWindow.setFocusable(true);
                popupWindow.setOutsideTouchable(true);
                View tmp = getActivity().getLayoutInflater().inflate(R.layout.pop_group_manager, null);
                popupWindow.setContentView(tmp);
                int location[] = new int[2];
                tmp.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                view.getLocationInWindow(location);
                popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, (location[0] + view.getWidth() / 2) - tmp.getMeasuredWidth() / 2, location[1] - tmp.getMeasuredHeight());
                // popupWindow.showAtLocation(view, Gravity.NO_GRAVITY,0,0-popupWindow.getHeight());
                tmp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(), "点击了分组管理", Toast.LENGTH_SHORT).show();
                        popupWindow.dismiss();
                    }
                });
                return true;
            }
        });
        elv_lists.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("name", groups.get(groupPosition).getFriends().get(childPosition).getNick());
                getActivity().startActivity(intent);
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_new_friend:
                Toast.makeText(getContext(), "点击了新朋友", Toast.LENGTH_SHORT).show();
                break;
            case R.id.layout_heart:
                Toast.makeText(getContext(), "点击了特别关心", Toast.LENGTH_SHORT).show();
                break;
            case R.id.layout_group:
                Toast.makeText(getContext(), "点击了群组", Toast.LENGTH_SHORT).show();
                break;
            case R.id.layout_gzh:
                Toast.makeText(getContext(), "点击了公众号", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    class MyAdater extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return groups.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return groups.get(groupPosition).getFriends().size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groups.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return groups.get(groupPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            convertView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.item_group, null);
            TextView tv_nick = (TextView) convertView.findViewById(R.id.tv_group_name);
            tv_nick.setText(groups.get(groupPosition).getName());
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_contact, null);
            TextView tv_nick = (TextView) convertView.findViewById(R.id.tv_nick);
            tv_nick.setText(groups.get(groupPosition).getFriends().get(childPosition).getNick());
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
