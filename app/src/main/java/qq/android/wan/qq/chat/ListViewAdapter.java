package qq.android.wan.qq.chat;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.FileMessageBody;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.exceptions.EaseMobException;

import qq.android.wan.qq.ChatActivity;
import qq.android.wan.qq.MyApplication;
import qq.android.wan.qq.R;

public class ListViewAdapter extends BaseAdapter {
    private List<EMMessage> datas;
    private LayoutInflater inflater;
    private Context context;
    private Activity activity;
    private Map<String, Timer> timers = new Hashtable<String, Timer>();
    public static final String IMAGE_DIR = "chat/image/";

    public ListViewAdapter(Context context, List<EMMessage> datas) {
        super();
        this.context = context;
        this.datas = datas;
        this.activity = (Activity) context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        EMMessage message = datas.get(position);
       // if (convertView == null) {

            if (message.direct != EMMessage.Direct.RECEIVE) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.chat_right, null);
                viewHolder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
                viewHolder.img_state_error = (ImageView) convertView.findViewById(R.id.img_state_error);
                viewHolder.img_head = (ImageView) convertView.findViewById(R.id.img_head);
                viewHolder.img_pic = (ImageView) convertView.findViewById(R.id.img_pic);
                viewHolder.pb=(ProgressBar)convertView.findViewById(R.id.pb);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.chat_left, null);
                viewHolder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
                viewHolder.img_state_error = (ImageView) convertView.findViewById(R.id.img_state_error);
                viewHolder.img_head = (ImageView) convertView.findViewById(R.id.img_head);
                viewHolder.img_pic = (ImageView) convertView.findViewById(R.id.img_pic);
                viewHolder.pb=(ProgressBar)convertView.findViewById(R.id.pb);
                convertView.setTag(viewHolder);
            }

        /*}else{
            viewHolder= (ViewHolder) convertView.getTag();
        }
*/
        // 设置内容
        if (message.getType() == EMMessage.Type.TXT) {
            TextMessageBody txtBody = (TextMessageBody) message.getBody();
            Spannable span = SmileUtils.getSmiledText(context, txtBody.getMessage());
            // 设置内容
            viewHolder.tv_content.setText(span, TextView.BufferType.SPANNABLE);
        } else if (message.getType() == EMMessage.Type.IMAGE) {
            //接收或者发送的是图片

            viewHolder.tv_content.setVisibility(View.GONE);
            viewHolder.img_pic.setVisibility(View.VISIBLE);
            handleImageMessage(message, viewHolder, position, convertView);
        }else if (message.getType() == EMMessage.Type.VOICE) {
            //接收或者发送的是录制的语音

            viewHolder.tv_content.setVisibility(View.GONE);
            viewHolder.img_pic.setVisibility(View.VISIBLE);
            handleVoiceMessage(message, viewHolder, position, convertView);
        }


        return convertView;
    }

    class ViewHolder {
        //img_pic   可以是发送的图片，也可以是发送的语音
        ImageView img_pic, img_progress, img_state_error, img_head;
        TextView tv_content;
        private ProgressBar pb;
    }
    private void handleVoiceMessage(final EMMessage message, final ViewHolder viewHolder, final int position, View convertView) {
        VoiceMessageBody voiceBody = (VoiceMessageBody) message.getBody();
        int len = voiceBody.getLength();
        if(len>0){
           // holder.tv.setText(voiceBody.getLength() + "\"");
            //holder.tv.setVisibility(View.VISIBLE);
        }else{
           // holder.tv.setVisibility(View.INVISIBLE);
        }
        viewHolder.img_pic.setOnClickListener(new VoicePlayClickListener(message, viewHolder.img_pic, this, activity, message.getFrom()));
       /* viewHolder.img_pic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                activity.startActivityForResult(
                        (new Intent(activity, ContextMenu.class)).putExtra("position", position).putExtra("type",
                                EMMessage.Type.VOICE.ordinal()), ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                return true;
            }
        });*/
        if (((ChatActivity)activity).playMsgId != null
                && ((ChatActivity)activity).playMsgId.equals(message
                .getMsgId())&&VoicePlayClickListener.isPlaying) {
            AnimationDrawable voiceAnimation;
            if (message.direct == EMMessage.Direct.RECEIVE) {
                viewHolder.img_pic.setImageResource(R.anim.voice_from_icon);
            } else {
                viewHolder.img_pic.setImageResource(R.anim.voice_to_icon);
            }
            voiceAnimation = (AnimationDrawable)  viewHolder.img_pic.getDrawable();
            voiceAnimation.start();
        } else {
            if (message.direct == EMMessage.Direct.RECEIVE) {
                viewHolder.img_pic.setImageResource(R.drawable.chatfrom_voice_playing);
            } else {
                viewHolder.img_pic.setImageResource(R.drawable.chatto_voice_playing);
            }
        }


        if (message.direct == EMMessage.Direct.RECEIVE) {
            if (message.status == EMMessage.Status.INPROGRESS) {
                //语音未下载
               viewHolder.pb.setVisibility(View.VISIBLE);
                ((FileMessageBody) message.getBody()).setDownloadCallback(new EMCallBack() {

                    @Override
                    public void onSuccess() {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                viewHolder.pb.setVisibility(View.GONE);
                                notifyDataSetChanged();
                            }
                        });

                    }

                    @Override
                    public void onProgress(int progress, String status) {
                    }

                    @Override
                    public void onError(int code, String message) {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                viewHolder.pb.setVisibility(View.GONE);
                            }
                        });

                    }
                });

            } else {
                viewHolder.pb.setVisibility(View.GONE);

            }
            return;
        }

        // until here, deal with send voice msg
        switch (message.status) {
            case SUCCESS:
                viewHolder.pb.setVisibility(View.GONE);
                viewHolder.img_state_error.setVisibility(View.GONE);
                break;
            case FAIL:
                viewHolder.pb.setVisibility(View.GONE);
                viewHolder.img_state_error.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                viewHolder.pb.setVisibility(View.VISIBLE);
                viewHolder.img_state_error.setVisibility(View.GONE);
                break;
            default:
                sendVoiceMessage(message, viewHolder);
        }
    }
    private void handleImageMessage(final EMMessage message, final ViewHolder viewHolder, final int position, View convertView) {
        // 接收对方的消息
        if (message.direct == EMMessage.Direct.RECEIVE) {
            viewHolder.img_pic.setImageResource(R.drawable.ziji);
            if (message.status == EMMessage.Status.INPROGRESS) {
                //图片没下载下来,我们就下载
                showDownloadImageProgress(message, viewHolder);
            } else {
                // 对方的图片消息已经下载到本地了
                ImageMessageBody imgBody = (ImageMessageBody) message.getBody();

                //图片已经缓存在本地
                if (imgBody.getLocalUrl() != null) {
                    String remotePath = imgBody.getRemoteUrl();//图片的远程路径
                    Log.e("test", "图片的远程路径是:" + remotePath);
                    String filePath = ImageUtils.getImagePath(remotePath);
                    Log.e("test", "图片的filePath=:" + filePath);
                    String thumbRemoteUrl = imgBody.getThumbnailUrl();
                    if (TextUtils.isEmpty(thumbRemoteUrl) && !TextUtils.isEmpty(remotePath)) {
                        thumbRemoteUrl = remotePath;
                    }
                    String thumbnailPath = ImageUtils.getThumbnailImagePath(thumbRemoteUrl);
                    //把图片展示listview中
                    showImageView(thumbnailPath, viewHolder.img_pic, filePath, imgBody.getRemoteUrl(), message);
                }
            }
            return;
        }

        //该图片是你自己发送的
        ImageMessageBody imgBody = (ImageMessageBody) message.getBody();
        String filePath = imgBody.getLocalUrl();
        if (filePath != null && new File(filePath).exists()) {
            showImageView(ImageUtils.getThumbnailImagePath(filePath), viewHolder.img_pic, filePath, null, message);
        } else {
            showImageView(ImageUtils.getThumbnailImagePath(filePath), viewHolder.img_pic, filePath, IMAGE_DIR, message);
        }

        switch (message.status) {
            case SUCCESS:
               viewHolder.pb.setVisibility(View.GONE); //隐藏进度条
                viewHolder.img_state_error.setVisibility(View.GONE);
                break;
            case FAIL:
                viewHolder.pb.setVisibility(View.GONE);
                viewHolder.img_pic.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                viewHolder.img_state_error.setVisibility(View.GONE);
               viewHolder.pb.setVisibility(View.VISIBLE);
              //  viewHolder.tv.setVisibility(View.VISIBLE);
                if (timers.containsKey(message.getMsgId()))
                    return;
                // set a timer
                final Timer timer = new Timer();
                timers.put(message.getMsgId(), timer);
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                viewHolder.pb.setVisibility(View.VISIBLE);
                               // viewHolder.tv.setVisibility(View.VISIBLE);
                               // viewHolder.tv.setText(message.progress + "%");
                                if (message.status == EMMessage.Status.SUCCESS) {
                                   viewHolder.pb.setVisibility(View.GONE);
                                   // holder.tv.setVisibility(View.GONE);
                                    timer.cancel();
                                } else if (message.status == EMMessage.Status.FAIL) {
                                   viewHolder.pb.setVisibility(View.GONE);
                                  //  holder.tv.setVisibility(View.GONE);
                                viewHolder.img_state_error.setVisibility(View.VISIBLE);
                                    Toast.makeText(activity,"发送失败", Toast.LENGTH_SHORT).show();
                                    timer.cancel();
                                }

                            }
                        });

                    }
                }, 0, 500);
                break;
            default:
                sendPictureMessage(message, viewHolder);
        }
    }

    private void sendVoiceMessage(EMMessage message, final ViewHolder viewHolder) {
        try {
            EMChatManager.getInstance().sendMessage(message, new EMCallBack() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(int i, String s) {
                    //发送失败
                    viewHolder.img_state_error.setVisibility(View.GONE);
                }

                @Override
                public void onProgress(int i, String s) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPictureMessage(final EMMessage message, final ViewHolder viewHolder) {
        try {
            String to = message.getTo();

            // before send, update ui
           viewHolder.img_state_error.setVisibility(View.GONE);
            viewHolder.pb.setVisibility(View.VISIBLE);
            //holder.tv.setVisibility(View.VISIBLE);
           // holder.tv.setText("0%");

            final long start = System.currentTimeMillis();
            // if (chatType == ChatActivity.CHATTYPE_SINGLE) {
            EMChatManager.getInstance().sendMessage(message, new EMCallBack() {

                @Override
                public void onSuccess() {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            // 发送成功
                            viewHolder.pb.setVisibility(View.GONE);
                           // holder.tv.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                public void onError(int code, String error) {

                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            //发送失败
                            viewHolder.img_state_error.setVisibility(View.VISIBLE);
                            viewHolder.pb.setVisibility(View.GONE);
                            //holder.tv.setVisibility(View.GONE);
                            Toast.makeText(activity, "发送失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onProgress(final int progress, String status) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            //holder.tv.setText(progress + "%");
                        }
                    });
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 下载网络上的图片呢
     *
     * @param message 包含图片的消息
     * @param viewHolder 显示的控件
     */
    private void showDownloadImageProgress(final EMMessage message, final ViewHolder viewHolder) {
        // final ImageMessageBody msgbody = (ImageMessageBody)
        // message.getBody();
        final FileMessageBody msgbody = (FileMessageBody) message.getBody();
        if (viewHolder.pb != null)
            viewHolder.pb.setVisibility(View.VISIBLE);
        /*if (viewHolder.tv != null)
            viewHolder.tv.setVisibility(View.VISIBLE);*/

        msgbody.setDownloadCallback(new EMCallBack() {

            @Override
            public void onSuccess() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // message.setBackReceive(false);
                        if (message.getType() == EMMessage.Type.IMAGE) {
                            viewHolder.pb.setVisibility(View.GONE);
                            // holder.tv.setVisibility(View.GONE);
                        }
                        notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(int code, String message) {

            }

            @Override
            public void onProgress(final int progress, String status) {
                if (message.getType() == EMMessage.Type.IMAGE) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // holder.tv.setText(progress + "%");

                        }
                    });
                }

            }

        });
    }

    /**
     * 把图片加载到listview中(通过异步加载)
     *
     * @param thumbernailPath   缩略图的路径
     * @param img_pic           listview中item显示图片的控件
     * @param localFullSizePath 大图片在本地的路径
     * @param remoteDir         该图片在服务器中的路径
     * @param message           要处理的图片消息
     * @return
     */
    private boolean showImageView(final String thumbernailPath, final ImageView img_pic, final String localFullSizePath, String remoteDir,
                                  final EMMessage message) {
        final String remote = remoteDir;
        // 第一次加载缩略图
        Bitmap bitmap = ImageCache.getInstance().get(thumbernailPath);
        //如果缩略图存在
        if (bitmap != null) {
            // thumbnail image is already loaded, reuse the drawable
            img_pic.setImageBitmap(bitmap);
            img_pic.setClickable(true);
            img_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, ShowBigImage.class);
                    File file = new File(localFullSizePath);
                    if (file.exists()) {
                        Uri uri = Uri.fromFile(file);
                        intent.putExtra("uri", uri);
                    } else {
                        ImageMessageBody body = (ImageMessageBody) message.getBody();
                        intent.putExtra("secret", body.getSecret());
                        intent.putExtra("remotepath", remote);
                    }
                    if (message != null && message.direct == EMMessage.Direct.RECEIVE && !message.isAcked
                            && message.getChatType() != EMMessage.ChatType.GroupChat && message.getChatType() != EMMessage.ChatType.ChatRoom) {
                        try {
                            EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                            message.isAcked = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    activity.startActivity(intent);
                }
            });
            return true;
        } else {
            new LoadImageTask().execute(thumbernailPath, localFullSizePath, remote, message.getChatType(), img_pic, activity, message);
            return true;
        }

    }


}
