package com.hyphenate.easeui.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.model.EaseAtMessageHelper;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import com.hyphenate.easeui.widget.EaseConversationList.EaseConversationListHelper;
import com.socks.library.KLog;
import com.stratagile.pnrouter.R;
import com.stratagile.pnrouter.application.AppConfig;
import com.stratagile.pnrouter.constant.ConstantValue;
import com.stratagile.pnrouter.constant.UserDataManger;
import com.stratagile.pnrouter.db.GroupEntity;
import com.stratagile.pnrouter.db.GroupEntityDao;
import com.stratagile.pnrouter.db.UserEntity;
import com.stratagile.pnrouter.db.UserEntityDao;
import com.stratagile.pnrouter.entity.UnReadEMMessage;
import com.stratagile.pnrouter.utils.Base58;
import com.stratagile.pnrouter.utils.DateUtil;
import com.stratagile.pnrouter.utils.RxEncodeTool;
import com.stratagile.pnrouter.utils.SpUtil;
import com.stratagile.pnrouter.view.ImageButtonWithText;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.jar.Attributes;


/**
 * conversation list adapter
 */
public class EaseConversationNewAdapter extends ArrayAdapter<UnReadEMMessage> {
    private static final String TAG = "EaseConversationNewAdapter";
    private List<UnReadEMMessage> conversationEmmessageList;
    private List<UnReadEMMessage> copyConversationEmmessageList;
    private ConversationFilter conversationFilter;
    private boolean notiyfyByFilter;

    protected int primaryColor;
    protected int secondaryColor;
    protected int timeColor;
    protected int primarySize;
    protected int secondarySize;
    protected float timeSize;

    public EaseConversationNewAdapter(Context context, int resource,
                                      List<UnReadEMMessage> objects) {
        super(context, resource, objects);
        conversationEmmessageList = objects;
        copyConversationEmmessageList = new ArrayList<UnReadEMMessage>();
        copyConversationEmmessageList.addAll(objects);
    }

    @Override
    public int getCount() {
        return conversationEmmessageList.size();
    }

    @Override
    public UnReadEMMessage getItem(int arg0) {
        if (arg0 < conversationEmmessageList.size()) {
            return conversationEmmessageList.get(arg0);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.ease_row_chat_history, parent, false);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        if (holder == null) {
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.subject);
            holder.userRouter = (TextView) convertView.findViewById(R.id.userRouter);
            holder.unreadLabel = (TextView) convertView.findViewById(R.id.unread_msg_number);
            holder.message = (TextView) convertView.findViewById(R.id.message);
            holder.draft = (TextView) convertView.findViewById(R.id.draft);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.avatar = (ImageButtonWithText) convertView.findViewById(R.id.avatar);
            holder.msgState = convertView.findViewById(R.id.msg_state);
            holder.list_itease_layout = (RelativeLayout) convertView.findViewById(R.id.list_itease_layout);
            holder.motioned = (TextView) convertView.findViewById(R.id.mentioned);
            convertView.setTag(holder);
        }
//        holder.list_itease_layout.setBackgroundResource(R.drawable.ease_mm_listitem);

        // get conversation
        UnReadEMMessage conversation = getItem(position);
        // get username or group id
        String conversationId = conversation.getEmMessage().conversationId();
        UnReadEMMessage lastMessage = conversation;
        if (lastMessage == null) {
            return convertView;
        }
        EMMessage eMMessage = conversation.getEmMessage();
        String chatType = eMMessage.getChatType().toString();
        String usernameSouce = "";
        UserEntity friendUser = null;
        GroupEntity groupEntity = null;
        if (chatType.equals("Chat")) {

            List<UserEntity> localFriendList = null;
            if (UserDataManger.myUserData != null && !lastMessage.getEmMessage().getTo().equals(UserDataManger.myUserData.getUserId())) {
                localFriendList = AppConfig.instance.getMDaoMaster().newSession().getUserEntityDao().queryBuilder().where(UserEntityDao.Properties.UserId.eq(lastMessage.getEmMessage().getTo())).list();
                KLog.i("对话：拿to");
                if (localFriendList.size() > 0)
                    friendUser = localFriendList.get(0);
            } else {
                KLog.i("对话：拿from");
                localFriendList = AppConfig.instance.getMDaoMaster().newSession().getUserEntityDao().queryBuilder().where(UserEntityDao.Properties.UserId.eq(lastMessage.getEmMessage().getFrom())).list();
                if (localFriendList.size() > 0)
                    friendUser = localFriendList.get(0);
            }
            if (friendUser == null) {
                return convertView;
            }
            String username = friendUser.getNickName();
            if (friendUser.getRemarks() != null && !friendUser.getRemarks().equals("")) {
                username = friendUser.getRemarks();
            }
            usernameSouce = new String(RxEncodeTool.base64Decode(username));
        } else if ("GroupChat".equals(chatType)){
            List<GroupEntity> localGroupList = null;
            localGroupList = AppConfig.instance.getMDaoMaster().newSession().getGroupEntityDao().loadAll();
            localGroupList = AppConfig.instance.getMDaoMaster().newSession().getGroupEntityDao().queryBuilder().where(GroupEntityDao.Properties.GId.eq(lastMessage.getEmMessage().getTo())).list();
            if (localGroupList.size() > 0)
                groupEntity = localGroupList.get(0);
            List<UserEntity> localFriendList = null;
            localFriendList = AppConfig.instance.getMDaoMaster().newSession().getUserEntityDao().queryBuilder().where(UserEntityDao.Properties.UserId.eq(lastMessage.getEmMessage().getFrom())).list();
            if (localFriendList.size() > 0)
                friendUser = localFriendList.get(0);
        } else if ("ChatRoom".equals(chatType)) {

        }

        if (conversation.getEmMessage().getChatType() == EMMessage.ChatType.GroupChat) {
            String groupId = conversation.getEmMessage().conversationId();
            if (EaseAtMessageHelper.get().hasAtMeMsg(groupId)) {
                holder.motioned.setVisibility(View.VISIBLE);
            } else {
                holder.motioned.setVisibility(View.GONE);
            }
            // group message, show group avatar
//            holder.avatar.setImageResource(R.drawable.ease_group_icon);
            EMGroup group = EMClient.getInstance().groupManager().getGroup(conversationId);
            if (groupEntity != null) {
                if (groupEntity.getRemark() ==null  || "".equals(groupEntity.getRemark())) {
                    String groupnameSouce = new String(RxEncodeTool.base64Decode(groupEntity.getGName()));
                    holder.name.setText(groupnameSouce);
                } else {
                    String groupnameSouce = new String(RxEncodeTool.base64Decode(groupEntity.getRemark()));
                    holder.name.setText(groupnameSouce);
                }
            }
            holder.userRouter.setText("");
            holder.avatar.setGroupHeadImage();
        } else if (conversation.getEmMessage().getChatType() == EMMessage.ChatType.ChatRoom) {
            holder.motioned.setVisibility(View.GONE);
            holder.userRouter.setVisibility(View.GONE);
            holder.name.setText(R.string.campaign_update);
        } else {
//            EaseUserUtils.setUserAvatar(getContext(), conversationId, holder.avatar);
            //EaseUserUtils.setUserNick(username, holder.name);
            holder.avatar.setText(usernameSouce);
            String fileBase58Name = Base58.encode(RxEncodeTool.base64Decode(friendUser.getSignPublicKey())) + ".jpg";
            holder.avatar.setImageFile(fileBase58Name);
            holder.name.setText(usernameSouce);
            KLog.i("对话：" + usernameSouce);
            if (friendUser.getRouterAlias() == null || "".equals(friendUser.getRouterAlias())) {
                if (!"".equals(new String(RxEncodeTool.base64Decode(friendUser.getRouteName())))) {
                    holder.userRouter.setText("- " + new String(RxEncodeTool.base64Decode(friendUser.getRouteName())));
                } else {
                    holder.userRouter.setText("");
                }
            } else {
                holder.userRouter.setText("- " + friendUser.getRouterAlias());
            }
            holder.motioned.setVisibility(View.GONE);
        }

        String userId = SpUtil.INSTANCE.getString(AppConfig.instance, ConstantValue.INSTANCE.getUserId(), "");
        String userSn = SpUtil.INSTANCE.getString(AppConfig.instance, ConstantValue.INSTANCE.getUserSnSp(), "");
        if (lastMessage.getUnReadCount() != 0) {
            holder.unreadLabel.setText(lastMessage.getUnReadCount() == 0 ? "" : lastMessage.getUnReadCount() + "");
            holder.unreadLabel.setVisibility(View.VISIBLE);
        } else {
            holder.unreadLabel.setVisibility(View.INVISIBLE);
        }
        String time = lastMessage.getEmMessage().getMsgTime() + "";
        try
        {
            if (time.length() == 10) {
                holder.time.setText(DateUtil.getTimestampString(new Date(lastMessage.getEmMessage().getMsgTime() * 1000), getContext()));
            } else if(time.length() == 16){
                long timeTemp = Long.valueOf(lastMessage.getEmMessage().getMsgTime() /1000);
                holder.time.setText(DateUtil.getTimestampString(new Date(timeTemp), getContext()));
            }else{
                holder.time.setText(DateUtil.getTimestampString(new Date(lastMessage.getEmMessage().getMsgTime()), getContext()));
            }
            if (lastMessage.getEmMessage().isUnread()) {
                // show the content of latest message
                if (lastMessage.getEmMessage().direct() == EMMessage.Direct.SEND && lastMessage.getEmMessage().status() == EMMessage.Status.FAIL) {
                    holder.msgState.setVisibility(View.VISIBLE);
                } else {
                    holder.msgState.setVisibility(View.GONE);
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        holder.message.setTextColor(secondaryColor);
        String content = null;
        if (cvsListHelper != null) {
            content = cvsListHelper.onSetItemSecondaryText(lastMessage);
        }
        if (friendUser != null) {
            String username = friendUser.getNickName();
            if (friendUser.getRemarks() != null && !friendUser.getRemarks().equals("")) {
                username = friendUser.getRemarks();
            }
            if (friendUser.getUserId().equals(userId)) {
                String name = SpUtil.INSTANCE.getString(AppConfig.instance, ConstantValue.INSTANCE.getUsername(), "");
                usernameSouce = name;
            } else {
                usernameSouce = new String(RxEncodeTool.base64Decode(username));
            }

        } else {
            if (lastMessage.getEmMessage().getFrom().equals(userId)) {
                String name = SpUtil.INSTANCE.getString(AppConfig.instance, ConstantValue.INSTANCE.getUsername(), "");
                usernameSouce = name;
            }
        }

        holder.message.setText(EaseSmileUtils.getSmiledText(getContext(), EaseCommonUtils.getMessageDigest(lastMessage.getEmMessage(), (this.getContext()))), TextView.BufferType.SPANNABLE);
        if (content != null && !"".equals(content)) {
            //设置草稿
            holder.message.setText(content);
            holder.draft.setText("[Draft] ");
            if (chatType.equals("Chat")) {
                holder.message.setText(EaseSmileUtils.getSmiledText(getContext(), content));
            } else {
                holder.message.setText(EaseSmileUtils.getSmiledText(getContext(), holder.message.getText().toString()));
            }
        } else {
            //没有草稿
            if (chatType.equals("Chat")) {
                holder.message.setText(EaseSmileUtils.getSmiledText(getContext(), holder.message.getText().toString()));
            } else if (chatType.equals("GroupChat")){
                String messageContent =  holder.message.getText().toString();
                String name = SpUtil.INSTANCE.getString(AppConfig.instance, ConstantValue.INSTANCE.getUsername(), "");

                String cotainAt = SpUtil.INSTANCE.getString(AppConfig.instance, ConstantValue.INSTANCE.getMessageAT()+ userSn + "_" + lastMessage.getEmMessage().getTo(), "0");
                if(cotainAt.equals("1"))
                {

                    Spanned htmlStr =  Html.fromHtml("<font color='#F7625F'>"+getContext().getResources().getString(R.string.You_were_mentioned)+" </font>" + usernameSouce + ": " + holder.message.getText().toString());
                    holder.message.setText(EaseSmileUtils.getSmiledText(getContext(), htmlStr));
                }else{
                    holder.message.setText(EaseSmileUtils.getSmiledText(getContext(), usernameSouce + ": " + holder.message.getText().toString()));
                }

            } else if ("ChatRoom".equals(chatType)){
                holder.avatar.setImageResource(R.mipmap.icon_avatar_active, false);
                holder.message.setText(EaseSmileUtils.getSmiledText(getContext(), holder.message.getText().toString()));
            }
            holder.draft.setText("");
        }
        holder.message.setTextColor(getContext().getResources().getColor(R.color.list_itease_secondary_color));
        //set property
//        holder.name.setTextColor(primaryColor);
//        holder.time.setTextColor(timeColor);
//        if (primarySize != 0)
//            holder.name.setTextSize(TypedValue.COMPLEX_UNIT_PX, primarySize);
//        if (secondarySize != 0)
//            holder.message.setTextSize(TypedValue.COMPLEX_UNIT_PX, secondarySize);
//        if (timeSize != 0)
//            holder.time.setTextSize(TypedValue.COMPLEX_UNIT_PX, timeSize);

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (!notiyfyByFilter) {
            copyConversationEmmessageList.clear();
            copyConversationEmmessageList.addAll(conversationEmmessageList);
            notiyfyByFilter = false;
        }
    }

    @Override
    public Filter getFilter() {
        if (conversationFilter == null) {
            conversationFilter = new ConversationFilter(conversationEmmessageList);
        } else {
            conversationFilter.updata(conversationEmmessageList);
        }
        return conversationFilter;
    }


    public void setPrimaryColor(int primaryColor) {
        this.primaryColor = primaryColor;
    }

    public void setSecondaryColor(int secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public void setTimeColor(int timeColor) {
        this.timeColor = timeColor;
    }

    public void setPrimarySize(int primarySize) {
        this.primarySize = primarySize;
    }

    public void setSecondarySize(int secondarySize) {
        this.secondarySize = secondarySize;
    }

    public void setTimeSize(float timeSize) {
        this.timeSize = timeSize;
    }


    private class ConversationFilter extends Filter {
        List<UnReadEMMessage> mOriginalValues = null;

        public ConversationFilter(List<UnReadEMMessage> mList) {
            mOriginalValues = mList;
        }

        public void updata(List<UnReadEMMessage> mList) {
            mOriginalValues = mList;
        }

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                mOriginalValues = new ArrayList<UnReadEMMessage>();
            }
            if (prefix == null || prefix.length() == 0) {
                results.values = copyConversationEmmessageList;
                results.count = copyConversationEmmessageList.size();
            } else {
                if (copyConversationEmmessageList.size() > mOriginalValues.size()) {
                    mOriginalValues = copyConversationEmmessageList;
                }
                String prefixString = prefix.toString();
                final int count = mOriginalValues.size();
                final ArrayList<UnReadEMMessage> newValues = new ArrayList<UnReadEMMessage>();

                for (int i = 0; i < count; i++) {
                    final UnReadEMMessage value = mOriginalValues.get(i);
                    UnReadEMMessage lastMessage = value;
                    UserEntity friendUser = null;
                    GroupEntity groupEntity = null;
                    List<UserEntity> localFriendList = null;
                    List<GroupEntity> localGroupList = null;
                    if (lastMessage.getEmMessage().getTo().contains("group")) {
                        //群聊
                        localGroupList = AppConfig.instance.getMDaoMaster().newSession().getGroupEntityDao().queryBuilder().where(GroupEntityDao.Properties.GId.eq(lastMessage.getEmMessage().getTo())).list();
                        if (localGroupList.size() > 0) {
                            groupEntity = localGroupList.get(0);
                            String username = new String(RxEncodeTool.base64Decode(groupEntity.getGName()));
                            if (groupEntity.getRemark() != null && !groupEntity.getRemark().equals("")) {
                                username = new String(RxEncodeTool.base64Decode(groupEntity.getRemark()));
                            }
                            if (username.toLowerCase().contains(prefixString.toLowerCase())) {
                                newValues.add(value);
                            } else {
                                final String[] words = username.split(" ");
                                final int wordCount = words.length;

                                // Start at index 0, in case valueText starts with space(s)
                                for (String word : words) {
                                    if (word.toLowerCase().contains(prefixString.toLowerCase())) {
                                        newValues.add(value);
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        //单聊
                        if (!lastMessage.getEmMessage().getTo().equals(UserDataManger.myUserData.getUserId())) {
                            localFriendList = AppConfig.instance.getMDaoMaster().newSession().getUserEntityDao().queryBuilder().where(UserEntityDao.Properties.UserId.eq(lastMessage.getEmMessage().getTo())).list();
                            if (localFriendList.size() > 0)
                                friendUser = localFriendList.get(0);
                        } else {
                            localFriendList = AppConfig.instance.getMDaoMaster().newSession().getUserEntityDao().queryBuilder().where(UserEntityDao.Properties.UserId.eq(lastMessage.getEmMessage().getFrom())).list();
                            if (localFriendList.size() > 0)
                                friendUser = localFriendList.get(0);
                        }
                        String username = new String(RxEncodeTool.base64Decode(friendUser.getNickName()));
                        if (friendUser.getRemarks() != null && !friendUser.getRemarks().equals("")) {
                            username = new String(RxEncodeTool.base64Decode(friendUser.getRemarks()));
                        }
                        // First match against the whole ,non-splitted value
                        if (username.toLowerCase().contains(prefixString.toLowerCase())) {
                            newValues.add(value);
                        } else {
                            final String[] words = username.split(" ");
                            final int wordCount = words.length;

                            // Start at index 0, in case valueText starts with space(s)
                            for (String word : words) {
                                if (word.toLowerCase().contains(prefixString.toLowerCase())) {
                                    newValues.add(value);
                                    break;
                                }
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            conversationEmmessageList.clear();
            if (results.values != null) {
                conversationEmmessageList.addAll((List<UnReadEMMessage>) results.values);
            }
            if (results.count > 0) {
                notiyfyByFilter = true;
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    private EaseConversationListHelper cvsListHelper;

    public void setCvsListHelper(EaseConversationListHelper cvsListHelper) {
        this.cvsListHelper = cvsListHelper;
    }

    private static class ViewHolder {
        /**
         * who you chat with
         */
        TextView name;
        /**
         * unread message count
         */
        TextView unreadLabel;
        /**
         * content of last message
         */
        TextView message;
        /**
         * time of last message
         */
        TextView time;
        /**
         * avatar
         */
        ImageButtonWithText avatar;
        /**
         * status of last message
         */
        View msgState;
        /**
         * layout
         */
        RelativeLayout list_itease_layout;
        TextView motioned;
        TextView userRouter;

        TextView draft;
    }

    private boolean is24Time(Context context) {
        ContentResolver cv = context.getContentResolver();
        String strTimeFormat = android.provider.Settings.System.getString(cv, android.provider.Settings.System.TIME_12_24);
        if (strTimeFormat.equals("24")) {
            return true;
        }
        return false;
    }
}

