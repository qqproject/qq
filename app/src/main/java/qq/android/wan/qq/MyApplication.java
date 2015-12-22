package qq.android.wan.qq;

import android.app.Application;

import com.easemob.chat.EMChat;

/**
 * Created by Administrator on 2015/12/18.
 */
public class MyApplication extends Application {
    public static String user="4";
    //public static DemoHXSDKHelper hxSDKHelper = new DemoHXSDKHelper();

    @Override
    public void onCreate() {
        super.onCreate();
        //hxSDKHelper.onInit(this);
       EMChat.getInstance().init(this);
    }
}
