package qq.android.wan.qq;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;

/**
 * Created by Administrator on 2015/12/10.
 */
public class LogoActivity extends Activity {
    private LinearLayout layout_animation;
    private View view;
    private ImageView img_logo;
    private Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_logo);
        view = getLayoutInflater().inflate(R.layout.layout_logo, null);
        initUI();
        Animation animation1 = AnimationUtils.loadAnimation(LogoActivity.this, R.anim.animation_login);
        layout_animation.startAnimation(animation1);
        Animation animation2 = AnimationUtils.loadAnimation(LogoActivity.this, R.anim.animation_login_logo);
        img_logo.startAnimation(animation2);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogoActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        EMChatManager.getInstance().login(MyApplication.user, MyApplication.user, new EMCallBack() {//回调
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        EMGroupManager.getInstance().loadAllGroups();
                        EMChatManager.getInstance().loadAllConversations();
                        Log.e("test", "登陆聊天服务器成功！");
                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
                Log.e("test", "登陆聊天服务器失败！");
            }
        });
    }

    private void initUI() {
        layout_animation = (LinearLayout) findViewById(R.id.layout_animation);
        img_logo = (ImageView) findViewById(R.id.img_logo);
        btn_login=(Button)findViewById(R.id.btn_login);
    }
}
