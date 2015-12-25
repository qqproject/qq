package qq.android.wan.qq;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.util.PathUtil;
import com.easemob.util.VoiceRecorder;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import qq.android.wan.qq.chat.ListViewAdapter;
import qq.android.wan.qq.chat.SmileUtils;
import qq.android.wan.qq.chat.VoicePlayClickListener;

/**
 * Created by Administrator on 2015/12/13.
 */
public class ChatActivity extends Activity implements View.OnClickListener, EMEventListener {
    public static final int REQUEST_CODE_CAMERA = 18;
    public static final int REQUEST_CODE_LOCAL = 19;
    private EditText et_content;
    private Button btn_send, btn_more;
    private ImageView img_voice, img_expression, img_mic, img_camera,img_picture;
    private String str_content;
    private TextView tv_pass_say, tv_back, tv_nick;
    private LinearLayout layout_more, layout_expression;
    private List<EMMessage> datas;
    private ListViewAdapter adapter;
    private ListView lv_chat_record;
    private EMConversation conversation;        //聊天对象
    private String toChatUser = "3";        //在和谁聊天
    private ViewPager vp_expression;    //聊天表情
    private Drawable[] micImages;//录制语音的动画资源
    public String playMsgId;        //播放语音的信息的ID
    private RelativeLayout layout_mic;
    private TextView tv_mic_hint;
    private File cameraFile;//拍照的临时图片
    private VoiceRecorder voiceRecorder;
    private Handler micImageHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            // 切换msg切换图片
            Log.e("test","第几张:"+msg.what);
            img_mic.setImageDrawable(micImages[msg.what]);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_chat);
        initUI();
        initVoice();
        initListener();
        btn_send.setOnClickListener(this);
        conversation = EMChatManager.getInstance().getConversationByType(toChatUser, EMConversation.EMConversationType.Chat);
        datas = conversation.getAllMessages();
        adapter = new ListViewAdapter(ChatActivity.this, datas);
        lv_chat_record.setAdapter(adapter);
        lv_chat_record.setSelection(lv_chat_record.getCount() - 1);
        initExpression();

        wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "demo");

    }
    private void initUI() {
        tv_nick = (TextView) findViewById(R.id.tv_nick);
        tv_back = (TextView) findViewById(R.id.tv_back);
        et_content = (EditText) findViewById(R.id.et_content);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_more = (Button) findViewById(R.id.btn_more);
        img_voice = (ImageView) findViewById(R.id.img_voice);
        tv_pass_say = (TextView) findViewById(R.id.tv_pass_say);
        layout_more = (LinearLayout) findViewById(R.id.layout_more);
        lv_chat_record = (ListView) findViewById(R.id.lv_chat_record);
        vp_expression = (ViewPager) findViewById(R.id.vp_expression);
        img_expression = (ImageView) findViewById(R.id.img_expression);
        tv_mic_hint = (TextView) findViewById(R.id.tv_mic_hint);
        img_mic = (ImageView) findViewById(R.id.img_mic);
        layout_mic = (RelativeLayout) findViewById(R.id.layout_mic);
        img_camera = (ImageView) findViewById(R.id.img_camera);
        img_picture=(ImageView)findViewById(R.id.img_picture);
        tv_nick.setText("我是" + MyApplication.user + "   在和" + toChatUser + "聊天");

    }

    /**
     * 初始化语音资源(录制语音)
     */
    private void initVoice() {
// 动画资源文件,用于录制语音时
        micImages = new Drawable[]{
                getResources().getDrawable(R.drawable.record_animate_01),
                getResources().getDrawable(R.drawable.record_animate_02),
                getResources().getDrawable(R.drawable.record_animate_03),
                getResources().getDrawable(R.drawable.record_animate_04),
                getResources().getDrawable(R.drawable.record_animate_05),
                getResources().getDrawable(R.drawable.record_animate_06),
                getResources().getDrawable(R.drawable.record_animate_07),
                getResources().getDrawable(R.drawable.record_animate_08),
                getResources().getDrawable(R.drawable.record_animate_09),
                getResources().getDrawable(R.drawable.record_animate_10),
                getResources().getDrawable(R.drawable.record_animate_11),
                getResources().getDrawable(R.drawable.record_animate_12),
                getResources().getDrawable(R.drawable.record_animate_13),
                getResources().getDrawable(R.drawable.record_animate_14)};
        voiceRecorder = new VoiceRecorder(micImageHandler);
        tv_pass_say.setOnTouchListener(new PressToSpeakListen());

    }

    /**
     * 初始化聊天的表情包  每个页面显示20个表情和个删除的表情，共21个   正好是3行7列
     */
    private void initExpression() {
        ArrayList<String> expressionNames = (ArrayList<String>) getExpressionName(30);
        int a = 0;//viewpager的页面的个数
        if (expressionNames.size() % 21 == 0) {
            a = expressionNames.size() / 21;
        } else {
            a = expressionNames.size() / 21 + 1;
        }
        ArrayList<View> gridViews = new ArrayList<View>();
        for (int i = 0; i < a; i++) {
            View view = getLayoutInflater().inflate(R.layout.item_chat_expression_viewpager, null);
            GridView gridView = (GridView) view.findViewById(R.id.gv_expression);
            int start = i * 20;
            int end = i * 20 + 20;
            if (i == a - 1) {
                //说明是最后一个页面
                end = expressionNames.size();
            }
            List<String> tmp = expressionNames.subList(start, end);//第i个viewpager页面的表情
            List<String> tmp1 = new ArrayList<String>();
            tmp1.addAll(tmp);
            tmp1.add("ee_delete");
            //不能给tmp.add(String)  会报异常
            final ExpressionGridViewAdapter gv_adapter = new ExpressionGridViewAdapter(tmp1);
            gridView.setAdapter(gv_adapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String ee_name = (String) gv_adapter.getItem(position);
                    if (ee_name.equals("ee_delete")) {
                        // 删除文字或者表情
                        clickDeleteExpression();
                    } else {
                        SmileUtils tmp = new SmileUtils();
                        Field field = null;
                        try {
                            field = SmileUtils.class.getField(ee_name);
                            et_content.append(SmileUtils.getSmiledText(
                                    ChatActivity.this, (String) field.get(null)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            gridViews.add(view);
        }

        vp_expression.setAdapter(new ExpressionViewPagerAdapter(gridViews));
    }

    /**
     * 点击了表情里的删除那个表情
     */
    private void clickDeleteExpression() {
        // 删除文字或者表情
        if (!TextUtils.isEmpty(et_content.getText())) {
            int selectionStart = et_content.getSelectionStart();// 获取光标的位置
            if (selectionStart > 0) {
                String body = et_content.getText().toString();
                String tempStr = body.substring(0, selectionStart);
                int i = tempStr.lastIndexOf("[");// 获取最后一个表情的位置
                if (i != -1) {
                    CharSequence cs = tempStr.substring(i, selectionStart);
                    if (SmileUtils.containsKey(cs.toString()))
                        et_content.getEditableText().delete(i, selectionStart);
                    else
                        et_content.getEditableText().delete(selectionStart - 1, selectionStart);
                } else {
                    et_content.getEditableText().delete(selectionStart - 1, selectionStart);
                }
            }
        }
    }

    /**
     * 把所有表情的名字放在一个集合里
     *
     * @param getSum 总共表情的个数(不算删除的那个表情)
     * @return
     */
    public List<String> getExpressionName(int getSum) {
        List<String> reslist = new ArrayList<String>();
        for (int x = 1; x <= getSum; x++) {
            String filename = "ee_" + x;
            reslist.add(filename);
        }
        return reslist;

    }

    private void initListener() {
        tv_back.setOnClickListener(this);
        img_voice.setOnClickListener(this);
        btn_more.setOnClickListener(this);
        btn_send.setOnClickListener(this);
        img_expression.setOnClickListener(this);
        img_camera.setOnClickListener(this);
        img_picture.setOnClickListener(this);
        et_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                str_content = et_content.getText().toString();
                if (!str_content.equals("") && btn_send.getVisibility() == View.VISIBLE) {
                    return;
                }
                if (!str_content.equals("")) {
                    btn_more.setVisibility(View.GONE);
                    btn_send.setVisibility(View.VISIBLE);
                } else {
                    btn_more.setVisibility(View.VISIBLE);
                    btn_send.setVisibility(View.GONE);
                }
            }
        });
        et_content.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hintBottom();
                }
            }
        });
        et_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hintBottom();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back://返回
                finish();
                break;
            case R.id.img_voice://语音
                clickVoice();
                break;
            case R.id.btn_more://点击了更多，展开下面的bottom
                clickMore();
                break;
            case R.id.btn_send://发送文本
                sendText();
                break;
            case R.id.img_expression://点击了编辑框旁边的表情开关
                clickExpression();
                break;
            case R.id.img_camera:
                clickFromCamera();
                break;
            case R.id.img_picture:
                selectPicFromLocal();
                break;
        }
    }
    /**
     * 从图库获取图片
     */
    public void selectPicFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_LOCAL);
    }
    private void clickFromCamera() {

        if (isExitsSdcard()) {
            Toast.makeText(getApplicationContext(), "拍照需要sd卡支持", Toast.LENGTH_SHORT).show();
            return;
        }

        cameraFile = new File(PathUtil.getInstance().getImagePath(),
                MyApplication.user + System.currentTimeMillis() + ".jpg");
        cameraFile.getParentFile().mkdirs();
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
                        MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
                REQUEST_CODE_CAMERA);

    }

    private void clickMore() {
        if (btn_more.isSelected()) {
            btn_more.setSelected(false);
            layout_more.setVisibility(View.GONE);
        } else {
            btn_more.setSelected(true);
            hideKeyboard();
            layout_more.setVisibility(View.VISIBLE);
            if (et_content.getVisibility() == View.GONE) {
                tv_pass_say.setVisibility(View.GONE);
                img_voice.setSelected(false);
                et_content.setVisibility(View.VISIBLE);
            }
            if (vp_expression.getVisibility() == View.VISIBLE) {
                vp_expression.setVisibility(View.GONE);
                img_expression.setSelected(false);
            }
        }
    }

    private void clickVoice() {
        if (img_voice.isSelected()) {
            img_voice.setSelected(false);
            et_content.setVisibility(View.VISIBLE);
            tv_pass_say.setVisibility(View.GONE);

        } else {
            img_voice.setSelected(true);
            et_content.setVisibility(View.GONE);
            tv_pass_say.setVisibility(View.VISIBLE);
            hideKeyboard();
            hintBottom();
        }
    }

    /**
     * 点击了文本输入框旁边的表情，打开表情或者关闭表情框
     */
    private void clickExpression() {
        if (vp_expression.getVisibility() == View.GONE) {
            //打开表情框 1)隐藏软键盘 2)隐藏更多 3)设置表情按钮为选中状态 4)让标签库显示
            hideKeyboard();
            if (layout_more.getVisibility() == View.VISIBLE) {
                btn_more.setSelected(false);
                layout_more.setVisibility(View.GONE);
            }
            if (et_content.getVisibility() == View.GONE) {
                img_voice.setSelected(false);
                et_content.setVisibility(View.VISIBLE);
                tv_pass_say.setVisibility(View.GONE);
            }
            img_expression.setSelected(true);
            vp_expression.setVisibility(View.VISIBLE);

        } else {
            //点击了关闭表情框
            img_expression.setSelected(false);
            vp_expression.setVisibility(View.GONE);
        }
    }

    /**
     * 隐藏软键盘
     */
    private void hideKeyboard() {
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void hintBottom() {
        if (layout_more.getVisibility() == View.VISIBLE) {
            btn_more.setSelected(false);
            layout_more.setVisibility(View.GONE);
        }
        if (vp_expression.getVisibility() == View.VISIBLE) {
            img_expression.setSelected(false);
            vp_expression.setVisibility(View.GONE);
        }
    }

    private void sendText() {
        if (str_content.length() > 0) {
            EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
            // 如果是群聊，设置chattype,默认是单聊
            TextMessageBody txtBody = new TextMessageBody(str_content);
            // 设置消息body
            message.addBody(txtBody);
            // 设置要发给谁,用户username或者群聊groupid
            message.setReceipt(toChatUser);
            // 把messgage加到conversation中
            conversation.addMessage(message);
            try {
                EMChatManager.getInstance().sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 通知adapter有消息变动，adapter会根据加入的这条message显示消息和调用sdk的发送方法
            adapter.notifyDataSetChanged();
            lv_chat_record.setSelection(lv_chat_record.getCount() - 1);
            et_content.setText("");
            str_content = "";
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        EMChatManager.getInstance().registerEventListener(this,
                new EMNotifierEvent.Event[]{EMNotifierEvent.Event.EventNewMessage,
                        EMNotifierEvent.Event.EventOfflineMessage, EMNotifierEvent.Event.EventDeliveryAck,
                        EMNotifierEvent.Event.EventReadAck});
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EMChatManager.getInstance().unregisterEventListener(this);
    }

    @Override
    public void onEvent(EMNotifierEvent emNotifierEvent) {
        EMMessage message = (EMMessage) emNotifierEvent.getData();


        switch (emNotifierEvent.getEvent()) {
            case EventNewMessage: {
                // 获取到message,收到了新消息

                String username = null;
                // 群组消息
                if (message.getChatType() == EMMessage.ChatType.GroupChat || message.getChatType() == EMMessage.ChatType.ChatRoom) {
                    username = message.getTo();
                } else {
                    // 单聊消息
                    username = message.getFrom();
                    if (!username.equals(toChatUser)) {
                        // 收到了新的消息，我们刷新title的未读数目,发送消息的人不是当前聊天的

                    }
                }
                // 如果是当前会话的消息，刷新聊天页面
                if (username.equals(toChatUser)) {
                    conversation.addMessage(message);
                    //datas.add(message);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            lv_chat_record.setSelection(lv_chat_record.getCount() - 1);
                        }
                    });

                } else {
                    /*// 如果消息不是和当前聊天ID的消息
                    conversation.addMessage(message);
                    //datas.add(message);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            lv_chat_record.setSelection(lv_chat_record.getCount() - 1);
                        }
                    });*/
                }
                break;
            }
            case EventDeliveryAck: {
                // 获取到message，已送达
                adapter.notifyDataSetChanged();
                break;
            }
            case EventReadAck: {
                // 获取到message
                adapter.notifyDataSetChanged();
                break;
            }
            case EventOfflineMessage: {
                adapter.notifyDataSetChanged();
                break;
            }
            default:
                break;
        }

    }

    class ExpressionViewPagerAdapter extends PagerAdapter {
        private ArrayList<View> gridViews;

        public ExpressionViewPagerAdapter(ArrayList<View> gridViews) {
            this.gridViews = gridViews;
        }

        @Override
        public int getCount() {
            return gridViews.size();
        }

        // 来判断显示的是否是同一张图片，这里我们将两个参数相比较返回即可
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ((ViewPager) container).addView(gridViews.get(position));
            return gridViews.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(gridViews.get(position));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAMERA) { // 发送照片
            if (cameraFile != null && cameraFile.exists())
                sendPicture(cameraFile.getAbsolutePath());
        }else if (requestCode == REQUEST_CODE_LOCAL) { // 发送本地图片
            if (data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    sendPicByUri(selectedImage);
                }
            }
        }
    }
    private void sendPicByUri(Uri selectedImage) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        String st8 = "不能找到图片";
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            cursor = null;

            if (picturePath == null || picturePath.equals("null")) {
                Toast toast = Toast.makeText(this, st8, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            sendPicture(picturePath);
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                Toast toast = Toast.makeText(this, st8, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;

            }
            sendPicture(file.getAbsolutePath());
        }

    }

    private void sendPicture(final String filePath) {
        String to = toChatUser;
        // create and add image message in view
        final EMMessage message = EMMessage
                .createSendMessage(EMMessage.Type.IMAGE);
        message.setReceipt(to);
        ImageMessageBody body = new ImageMessageBody(new File(filePath));
        // 默认超过100k的图片会压缩后发给对方，可以设置成发送原图
        // body.setSendOriginalImage(true);
        message.addBody(body);

        conversation.addMessage(message);
        adapter.notifyDataSetChanged();
        lv_chat_record.setSelection(lv_chat_record.getCount() - 1);
        setResult(RESULT_OK);
    }

    class ExpressionGridViewAdapter extends BaseAdapter {
        private List<String> expressionNames;

        public ExpressionGridViewAdapter(List<String> expressionNames) {
            this.expressionNames = expressionNames;
        }

        @Override
        public int getCount() {
            return expressionNames.size();
        }

        @Override
        public Object getItem(int position) {
            return expressionNames.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new ImageView(ChatActivity.this);
                convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            int resId = getResources().getIdentifier(expressionNames.get(position), "drawable", getPackageName());
            ((ImageView) convertView).setImageResource(resId);
            return convertView;
        }
    }

    /**
     * 检测Sdcard是否存在
     *
     * @return
     */
    public static boolean isExitsSdcard() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            return false;
        else
            return false;
    }

    /**
     * 发送语音
     *
     * @param filePath
     * @param fileName
     * @param length
     * @param isResend
     */
    private void sendVoice(String filePath, String fileName, String length,
                           boolean isResend) {
        if (!(new File(filePath).exists())) {
            return;
        }
        try {
            final EMMessage message = EMMessage
                    .createSendMessage(EMMessage.Type.VOICE);

            message.setReceipt(toChatUser);
            int len = Integer.parseInt(length);
            VoiceMessageBody body = new VoiceMessageBody(new File(filePath),
                    len);
            message.addBody(body);

            conversation.addMessage(message);
            adapter.notifyDataSetChanged();
            lv_chat_record.setSelection(lv_chat_record.getCount() - 1);
            setResult(RESULT_OK);
            // send file
            // sendVoiceSub(filePath, fileName, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PowerManager.WakeLock wakeLock;

    /**
     * 按住说话listener
     */
    class PressToSpeakListen implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (isExitsSdcard()) {
                        Toast.makeText(ChatActivity.this, "发送语音需要内存卡支持", Toast.LENGTH_SHORT)
                                .show();
                        return false;
                    }
                    try {
                        v.setPressed(true);
                        wakeLock.acquire();
                        if (VoicePlayClickListener.isPlaying)
                            VoicePlayClickListener.currentPlayListener.stopPlayVoice();
                        layout_mic.setVisibility(View.VISIBLE);
                        tv_mic_hint.setText("手指上滑，取消发送");
                        tv_mic_hint.setBackgroundColor(Color.TRANSPARENT);
                        voiceRecorder.startRecording(null, toChatUser, getApplicationContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                        v.setPressed(false);
                        if (wakeLock.isHeld())
                            wakeLock.release();
                        if (voiceRecorder != null)
                            voiceRecorder.discardRecording();
                        layout_mic.setVisibility(View.INVISIBLE);
                        return false;
                    }

                    return true;
                case MotionEvent.ACTION_MOVE: {
                    if (event.getY() < 0) {
                        tv_mic_hint
                                .setText("松开手指，取消发送");
                        tv_mic_hint.setBackgroundColor(Color.parseColor("#55FF0000"));
                    } else {
                        tv_mic_hint.setText("手指上滑，取消发送");
                        tv_mic_hint.setBackgroundColor(Color.TRANSPARENT);
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                    v.setPressed(false);
                    layout_mic.setVisibility(View.INVISIBLE);
                    if (wakeLock.isHeld())
                        wakeLock.release();
                    if (event.getY() < 0) {
                        voiceRecorder.discardRecording();
                    } else {
                        String st1 = "无录音权限";
                        String st2 = "录音时间太短";
                        String st3 = "发送失败，请检测服务器是否连接";
                        try {
                            Log.e("test","length11111");
                            Log.e("test","路径:"+voiceRecorder.getVoiceFilePath());
                            int length = voiceRecorder.stopRecoding();
                            Log.e("test","length="+length);
                            if (length > 0) {
                                sendVoice(voiceRecorder.getVoiceFilePath(),voiceRecorder.getVoiceFileName(toChatUser),
                                        Integer.toString(length), false);
                            } else if (length == EMError.INVALID_FILE) {
                                Toast.makeText(getApplicationContext(), st1,Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), st2,Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ChatActivity.this, st3, Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                default:
                    Toast.makeText(ChatActivity.this,"取消了",Toast.LENGTH_SHORT).show();
                    layout_mic.setVisibility(View.INVISIBLE);
                    if (voiceRecorder != null)
                        voiceRecorder.discardRecording();
                    return false;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wakeLock.isHeld())
            wakeLock.release();
        if (VoicePlayClickListener.isPlaying
                && VoicePlayClickListener.currentPlayListener != null) {
            // 停止语音播放
            VoicePlayClickListener.currentPlayListener.stopPlayVoice();
        }

        try {
            // 停止录音
            if (voiceRecorder.isRecording()) {
                voiceRecorder.discardRecording();
                layout_mic.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
        }
    }
}
