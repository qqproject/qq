package qq.android.wan.qq;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import qq.android.wan.qq.view.CircleImageView;

/**
 * Created by Administrator on 2015/12/11.
 */
public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private ImageView img_tab1, img_tab2, img_tab3;
    private CircleImageView img_head;
    private TextView tv_title;
    private TextView tv_right;
    private ImageView img_right;
    private LinearLayout layout_search, layout_fragment;
    private int select_index = -1;           //table选中的索引
    private MessageFragment messageFragment;
    private ContactFragment contactFragment;
    private DynamicFragment dynamicFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private Fragment[] fragments;
    private DrawerLayout layout_drawer;
    private View main_view;
    private PopupWindow popupWindow;
    private TextView tv_sys, tv_jhy, tv_cjtlz, tv_fsddn, tv_mdmkc, tv_sq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        main_view = getLayoutInflater().inflate(R.layout.layout_main, null);
        initUI();
        initListener();
        fragmentManager = getSupportFragmentManager();

        initDate();
        select(0);
    }

    private void initUI() {
        layout_drawer = (DrawerLayout) findViewById(R.id.layout_drawer);
        this.img_tab3 = (ImageView) findViewById(R.id.img_tab3);
        this.img_tab2 = (ImageView) findViewById(R.id.img_tab2);
        this.img_tab1 = (ImageView) findViewById(R.id.img_tab1);
        this.layout_search = (LinearLayout) findViewById(R.id.layout_search);
        this.img_right = (ImageView) findViewById(R.id.img_right);
        this.tv_right = (TextView) findViewById(R.id.tv_right);
        this.tv_title = (TextView) findViewById(R.id.tv_title);
        this.img_head = (CircleImageView) findViewById(R.id.img_head);
        this.layout_fragment = (LinearLayout) findViewById(R.id.layout_fragment);
    }

    private void initListener() {
        img_tab1.setOnClickListener(this);
        img_tab2.setOnClickListener(this);
        img_tab3.setOnClickListener(this);
        img_right.setOnClickListener(this);
        img_head.setOnClickListener(this);
        layout_drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
            /**
             * 当抽屉被滑动的时候调用此方法
             * arg1 表示 滑动的幅度（0-1）
             */
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                Log.e("test", "drawerView=" + drawerView + "  slideOffset=" + slideOffset + "  抽屉出来了多少:" + drawerView.getWidth() * slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            /**
             * 当抽屉滑动状态改变的时候被调用
             * 状态值是STATE_IDLE（闲置--0）, STATE_DRAGGING（拖拽的--1）, STATE_SETTLING（固定--2）中之一。
             * 抽屉打开的时候，点击抽屉，drawer的状态就会变成STATE_DRAGGING，然后变成STATE_IDLE
             */
            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    private void initDate() {
        messageFragment = new MessageFragment();
        contactFragment = new ContactFragment();
        dynamicFragment = new DynamicFragment();
        fragments = new Fragment[]{messageFragment, contactFragment, dynamicFragment};
    }

    @Override
    public void onClick(View v) {


        switch (v.getId()) {
            case R.id.img_head:
                layout_drawer.openDrawer(Gravity.LEFT);
                break;
            case R.id.img_tab1:
                select(0);
                break;
            case R.id.img_tab2:
                select(1);
                break;
            case R.id.img_tab3:
                select(2);
                break;
            case R.id.img_right:
                Log.e("test", "点击了更多");
                if (popupWindow == null) {
                    initPopWindow();
                }
                if(popupWindow.isShowing()){
                    return;
                }
                popupWindow.showAsDropDown(img_right);
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 0.5f; //0.0-1.0
                getWindow().setAttributes(lp);
                break;
            case R.id.tv_sys:
                popupWindow.dismiss();
                Toast.makeText(MainActivity.this,"点击了扫一扫",Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_jhy:
                popupWindow.dismiss();
                Toast.makeText(MainActivity.this,"点击了加好友",Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_cjtlz:
                popupWindow.dismiss();
                Toast.makeText(MainActivity.this,"点击了创建讨论组",Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_fsddn:
                popupWindow.dismiss();
                Toast.makeText(MainActivity.this,"点击了发送到电脑",Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_mdmkc:
                popupWindow.dismiss();
                Toast.makeText(MainActivity.this,"点击了面对面快传",Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_sq:
                Toast.makeText(MainActivity.this,"点击了收钱",Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
                break;
        }
    }

    private void initPopWindow() {
        popupWindow = new PopupWindow(MainActivity.this);
        View pop_view = getLayoutInflater().inflate(R.layout.item_tab1_more, null);

        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setContentView(pop_view);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1.0f; //0.0-1.0
                getWindow().setAttributes(lp);
            }
        });

        tv_sys = (TextView) pop_view.findViewById(R.id.tv_sys);
        tv_jhy = (TextView) pop_view.findViewById(R.id.tv_jhy);
        tv_cjtlz = (TextView) pop_view.findViewById(R.id.tv_cjtlz);
        tv_fsddn = (TextView) pop_view.findViewById(R.id.tv_fsddn);
        tv_mdmkc = (TextView) pop_view.findViewById(R.id.tv_mdmkc);
        tv_sq = (TextView) pop_view.findViewById(R.id.tv_sq);

        tv_sys.setOnClickListener(this);
        tv_jhy.setOnClickListener(this);
        tv_cjtlz.setOnClickListener(this);
        tv_fsddn.setOnClickListener(this);
        tv_mdmkc.setOnClickListener(this);
        tv_sq.setOnClickListener(this);


    }

    private void select(int index) {
        if (select_index == 1 && index == 1) {
            Log.e("test", "expandableListView=" + fragments[1].getView());
            ExpandableListView expandableListView = (ExpandableListView) fragments[1].getView().findViewById(R.id.elv_lists);
            for (int i = 0; i < expandableListView.getAdapter().getCount(); i++)
                expandableListView.collapseGroup(i);
            return;
        }
        if (select_index == index) {
            return;
        }

        select_index = index;
        img_tab1.setSelected(false);
        img_tab2.setSelected(false);
        img_tab3.setSelected(false);
        if (index == 0) {
            img_tab1.setSelected(true);

            tv_title.setText("消息");
            tv_right.setVisibility(View.GONE);
            img_right.setVisibility(View.VISIBLE);
        } else if (index == 1) {
            img_tab2.setSelected(true);
            tv_right.setText("添加");
            tv_title.setText("联系人");
            tv_right.setVisibility(View.VISIBLE);
            img_right.setVisibility(View.GONE);
        } else {
            img_tab3.setSelected(true);
            tv_right.setText("更多");
            tv_title.setText("动态");
            tv_right.setVisibility(View.VISIBLE);
            img_right.setVisibility(View.GONE);
        }

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.layout_fragment, fragments[index]);
        fragmentTransaction.commit();

    }
}
