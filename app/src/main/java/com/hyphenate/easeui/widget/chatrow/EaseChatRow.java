package com.hyphenate.easeui.widget.chatrow;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.Direct;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.easeui.model.styles.EaseMessageListItemStyle;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseChatMessageList;
import com.hyphenate.easeui.widget.EaseChatMessageList.MessageListItemClickListener;
import com.hyphenate.util.DateUtils;
import com.socks.library.KLog;
import com.stratagile.pnrouter.R;
import com.stratagile.pnrouter.application.AppConfig;
import com.stratagile.pnrouter.constant.ConstantValue;
import com.stratagile.pnrouter.db.UserEntity;
import com.stratagile.pnrouter.db.UserEntityDao;
import com.stratagile.pnrouter.utils.Base58;
import com.stratagile.pnrouter.utils.DateUtil;
import com.stratagile.pnrouter.utils.RxEncodeTool;
import com.stratagile.pnrouter.utils.SpUtil;
import com.stratagile.pnrouter.view.ImageButtonWithText;

import java.util.List;

public abstract class EaseChatRow extends LinearLayout {
    public interface EaseChatRowActionCallback {
        void onResendClick(EMMessage message);

        void onBubbleClick(EMMessage message);

        void onBubbleLongClick(EMMessage message,View view);

        void onDetachedFromWindow();
    }

    protected static final String TAG = EaseChatRow.class.getSimpleName();

    protected LayoutInflater inflater;
    protected Context context;
    protected BaseAdapter adapter;
    protected EMMessage message;
    protected int position;

    protected TextView timestamp;
    protected ImageButtonWithText userAvatarView;
    protected View bubbleLayout;
    protected TextView usernickView;

    protected TextView percentageView;
    protected ProgressBar progressBar;
    protected ImageView statusView;
    protected Activity activity;

    protected TextView ackedView;
    protected TextView deliveredView;

    protected ImageView sendStatusView;
    private View marginView;

    protected MessageListItemClickListener itemClickListener;
    protected EaseMessageListItemStyle itemStyle;

    protected EaseChatRowActionCallback itemActionCallback;
    private int count;

    public EaseChatRow(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context);
        this.context = context;
        this.message = message;
        this.position = position;
        this.adapter = adapter;
        this.activity = (Activity) context;
        inflater = LayoutInflater.from(context);

        initView();
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        KLog.i("touch...");
//        AppConfig.instance.getPoint().x = (int) event.getX();
//        AppConfig.instance.getPoint().y = (int) event.getY();
//        return super.onTouchEvent(event);
//    }

    @Override
    protected void onDetachedFromWindow() {
        itemActionCallback.onDetachedFromWindow();
        super.onDetachedFromWindow();
    }

    public void updateView(final EMMessage msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onViewUpdate(msg);
            }
        });
    }

    private void initView() {
        onInflateView();
        timestamp = (TextView) findViewById(R.id.timestamp);
        userAvatarView = (ImageButtonWithText) findViewById(R.id.iv_userhead);
        bubbleLayout = findViewById(R.id.bubble);
        usernickView = (TextView) findViewById(R.id.tv_userid);
        marginView = findViewById(R.id.marginView);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        statusView = (ImageView) findViewById(R.id.msg_status);
        ackedView = (TextView) findViewById(R.id.tv_ack);
        sendStatusView = (ImageView) findViewById(R.id.msg_sendstatus);
        deliveredView = (TextView) findViewById(R.id.tv_delivered);
        onFindViewById();
    }

    /**
     * set property according message and postion
     *
     * @param message
     * @param position
     */
    public void setUpView(EMMessage message, int position,
            EaseChatMessageList.MessageListItemClickListener itemClickListener,
                          EaseChatRowActionCallback itemActionCallback,
                          EaseMessageListItemStyle itemStyle, int count) {
        this.message = message;
        this.position = position;
        this.itemClickListener = itemClickListener;
        this.itemActionCallback = itemActionCallback;
        this.itemStyle = itemStyle;
        this.count = count;
        setUpBaseView();
        onSetUpView();
        setClickListener();
    }

    private void setUpBaseView() {
    	// set nickname, avatar and background of bubble
        if (position == count - 1) {
            marginView.setVisibility(View.VISIBLE);
        } else {
            marginView.setVisibility(View.GONE);
        }
        if (timestamp != null) {
            if (position == 0) {
                timestamp.setText(DateUtil.getTimestampString(message.getMsgTime(), context));
                timestamp.setVisibility(View.VISIBLE);
            } else {
            	// show time stamp if interval with last message is > 30 seconds
                EMMessage prevMessage = (EMMessage) adapter.getItem(position - 1);
                if (prevMessage != null && DateUtils.isCloseEnough(message.getMsgTime(), prevMessage.getMsgTime()) && (count - position) % 10 != 0) {
                    timestamp.setVisibility(View.GONE);
                } else {
                    timestamp.setText(DateUtil.getTimestampString(message.getMsgTime(), context));
                    timestamp.setVisibility(View.VISIBLE);
                }
            }
        }
        if(userAvatarView != null) {
            //set nickname and avatar
            if (message.direct() == Direct.SEND) {
//                EaseUserUtils.setUserAvatar(context, EMClient.getInstance().getCurrentUser(), userAvatarView);
                //设置自己的头像
//                userAvatarView.setText(SpUtil.INSTANCE.getString(AppConfig.instance, ConstantValue.INSTANCE.getUsername(), ""));
                String fileBase58Name = Base58.encode( RxEncodeTool.base64Decode(ConstantValue.INSTANCE.getLibsodiumpublicSignKey()))+".jpg";
                userAvatarView.setImageFileInChat(fileBase58Name, SpUtil.INSTANCE.getString(AppConfig.instance, ConstantValue.INSTANCE.getUsername(), ""));
            } else {
                List<UserEntity> user = AppConfig.instance.getMDaoMaster().newSession().getUserEntityDao().queryBuilder().where(UserEntityDao.Properties.UserId.eq(message.getFrom())).list();
                if (user.size() != 0) {
                    String usernameSouce = new String(RxEncodeTool.base64Decode(user.get(0).getNickName()));
                    if(user.get(0).getRemarks() != null && !user.get(0).getRemarks().equals(""))
                    {
                        usernameSouce = new  String(RxEncodeTool.base64Decode(user.get(0).getRemarks()));
                    }
                    usernickView.setText(usernameSouce);
                    userAvatarView.setText(usernameSouce);
                    EaseUserUtils.setUserAvatar(usernameSouce, userAvatarView,user.get(0).getSignPublicKey());
                } else {
                    usernickView.setText("UNKNOW");
                    userAvatarView.setText("UNKNOW");


                }
            }
        }
        if (EMClient.getInstance().getOptions().getRequireDeliveryAck()) {
            if(deliveredView != null){
                if (message.isDelivered()) {
                    deliveredView.setVisibility(View.VISIBLE);
                } else {
                    deliveredView.setVisibility(View.INVISIBLE);
                }
            }
        }
        if (EMClient.getInstance().getOptions().getRequireAck()) {
            if (ackedView != null) {
                if (message.isAcked()) {
                    if (deliveredView != null) {
                        deliveredView.setVisibility(View.INVISIBLE);
                    }
                    ackedView.setVisibility(View.INVISIBLE);
                } else {
                    ackedView.setVisibility(View.INVISIBLE);
                }
            }
            if (sendStatusView != null) {

                if(message.isDelivered())
                {
                    Animation rotateAnimation  = new RotateAnimation(-3590, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimation.setFillAfter(true);
                    rotateAnimation.setDuration(18000);
                    rotateAnimation.setRepeatCount(-1);
                    rotateAnimation.setInterpolator(new LinearInterpolator());
                    sendStatusView.startAnimation(rotateAnimation);

                    sendStatusView.setImageResource(R.mipmap.ic_in_sending);
                    sendStatusView.setVisibility(View.VISIBLE);

                }
                if (message.isAcked()) {
                    sendStatusView.setImageResource(R.mipmap.ic_unread);
                    sendStatusView.setVisibility(View.VISIBLE);
                    sendStatusView.setAnimation(null);
                }
                if(message.isUnread() == false)
                {
                    if(message.getChatType().equals(EMMessage.ChatType.GroupChat))
                    {
                        sendStatusView.setImageResource(R.mipmap.ic_unread);
                    }else{
                        sendStatusView.setImageResource(R.mipmap.already_read);
                    }

                    sendStatusView.setVisibility(View.VISIBLE);
                    sendStatusView.setAnimation(null);
                }
            }
        }
        if (itemStyle != null) {
            if (userAvatarView != null) {
                if (itemStyle.isShowAvatar()) {
                    userAvatarView.setVisibility(View.VISIBLE);
//                    EaseAvatarOptions avatarOptions = EaseUI.getInstance().getAvatarOptions();
//                    if(avatarOptions != null && userAvatarView instanceof EaseImageView){
//                        EaseImageView avatarView = ((EaseImageView)userAvatarView);
//                        if(avatarOptions.getAvatarShape() != 0)
//                            avatarView.setShapeType(avatarOptions.getAvatarShape());
//                        if(avatarOptions.getAvatarBorderWidth() != 0)
//                            avatarView.setBorderWidth(avatarOptions.getAvatarBorderWidth());
//                        if(avatarOptions.getAvatarBorderColor() != 0)
//                            avatarView.setBorderColor(avatarOptions.getAvatarBorderColor());
//                        if(avatarOptions.getAvatarRadius() != 0)
//                            avatarView.setRadius(avatarOptions.getAvatarRadius());
//                    }
                } else {
                    userAvatarView.setVisibility(View.GONE);
                }
            }
            if (usernickView != null) {
                if (itemStyle.isShowUserNick())
                    usernickView.setVisibility(View.VISIBLE);
                else
                    usernickView.setVisibility(View.GONE);
            }
            if (bubbleLayout != null) {
                if (message.direct() == Direct.SEND) {
                    if (itemStyle.getMyBubbleBg() != null) {
                        bubbleLayout.setBackgroundDrawable(((EaseMessageAdapter) adapter).getMyBubbleBg());
                    }
                } else if (message.direct() == Direct.RECEIVE) {
                    if (itemStyle.getOtherBubbleBg() != null) {
                        bubbleLayout.setBackgroundDrawable(((EaseMessageAdapter) adapter).getOtherBubbleBg());
                    }
                }
            }
        }

    }

    private void setClickListener() {
        if(bubbleLayout != null){
            bubbleLayout.setOnClickListener(new OnClickListener() {
    
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null && itemClickListener.onBubbleClick(message)){
                        return;
                    }
                    if (itemActionCallback != null) {
                        itemActionCallback.onBubbleClick(message);
                    }
                }
            });

            bubbleLayout.setOnLongClickListener(new OnLongClickListener() {
    
                @Override
                public boolean onLongClick(View v) {
                    if (itemActionCallback != null) {
                        itemActionCallback.onBubbleLongClick(message,v);
                    }
                    return true;
                }
            });
        }

        if (statusView != null) {
            statusView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (itemClickListener != null && itemClickListener.onResendClick(message)){
                        return;
                    }
                    if (itemActionCallback != null) {
                        itemActionCallback.onResendClick(message);
                    }
                }
            });
        }

        if(userAvatarView != null){
            userAvatarView.setOnClickListener(new OnClickListener() {
    
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        if (message.direct() == Direct.SEND) {
                            itemClickListener.onUserAvatarClick(message.getFrom());
                        } else {
                            itemClickListener.onUserAvatarClick(message.getFrom());
                        }
                    }
                }
            });
            userAvatarView.setOnLongClickListener(new OnLongClickListener() {
                
                @Override
                public boolean onLongClick(View v) {
                    if(itemClickListener != null){
                        if (message.direct() == Direct.SEND) {
                            itemClickListener.onUserAvatarLongClick(message.getFrom());
                        } else {
                            itemClickListener.onUserAvatarLongClick(message.getFrom());
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    protected abstract void onInflateView();

    /**
     * find view by id
     */
    protected abstract void onFindViewById();

    /**
     * refresh view when message status change
     */
    protected abstract void onViewUpdate(EMMessage msg);

    /**
     * setup view
     * 
     */
    protected abstract void onSetUpView();
}
