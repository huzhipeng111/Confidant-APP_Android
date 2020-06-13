package com.stratagile.pnrouter.data.web

import com.alibaba.fastjson.JSONObject
import com.socks.library.KLog
import com.stratagile.pnrouter.application.AppConfig
import com.stratagile.pnrouter.constant.ConstantValue
import com.stratagile.pnrouter.data.api.DotLog
import com.stratagile.pnrouter.entity.*
import com.stratagile.pnrouter.utils.GsonUtil
import com.stratagile.pnrouter.utils.LogUtil
import com.stratagile.pnrouter.utils.SpUtil
import com.stratagile.pnrouter.utils.baseDataToJson
import com.stratagile.tox.toxcore.ToxCoreJni
import java.io.IOException


class PNRouterServiceMessageReceiver constructor(private val urls: SignalServiceConfiguration, private
val credentialsProvider: CredentialsProvider, private
                                                 val userAgent: String, private
                                                 val connectivityListener: ConnectivityListener) : SignalServiceMessagePipe.MessagePipeCallback {
    init {
        KLog.i("PNRouterServiceMessageReceiver")
    }
    override fun onMessage(baseData: BaseData, text: String?) {
//        KLog.i(baseData.baseDataToJson())
//        KLog.i(baseData.params.toString())
        var gson = GsonUtil.getIntGson()
        var paramsStr = (JSONObject.parseObject(baseData.baseDataToJson())).get("params").toString()
        DotLog.receiveLog(paramsStr)
        var action = JSONObject.parseObject(paramsStr).getString("Action")
        if (ConstantValue.loginOut) {
            if (action.toString().contains("Recovery") || action.toString().contains("Register") || action.toString().contains("Login") || action.toString().contains("LogOut") || action.toString().contains("RouterLogin") || action.toString().contains("ResetRouterKey") || action.toString().contains("ResetUserIdcode") || action.toString().contains("ResetRouterName") || action.toString().contains("OnlineStatusPush")) {
                when (action) {
                    "Recovery" -> {
                        val jRecoveryRsp = gson.fromJson(text, JRecoveryRsp::class.java)
                        KLog.i(jRecoveryRsp)
                        recoveryBackListener?.recoveryBack(jRecoveryRsp)
                        loginBackListener?.recoveryBack(jRecoveryRsp)
                        adminRecoveryCallBack?.recoveryBack(jRecoveryRsp)
                        mainInfoBack?.recoveryBack(jRecoveryRsp)
                        chatCallBack?.recoveryBack(jRecoveryRsp)
                        groupchatCallBack?.recoveryBack(jRecoveryRsp)
                    }
                    "Register" -> {
                        val JRegisterRsp = gson.fromJson(text, JRegisterRsp::class.java)
                        KLog.i(JRegisterRsp)
                        registerListener?.registerBack(JRegisterRsp)
                        loginBackListener?.registerBack(JRegisterRsp)
                        adminRecoveryCallBack?.registerBack(JRegisterRsp)
                        mainInfoBack?.registerBack(JRegisterRsp)
                        chatCallBack?.registerBack(JRegisterRsp)
                        groupchatCallBack?.registerBack(JRegisterRsp)
                    }
                    "Login" -> {
                        KLog.i("没有初始化。。登录之后" + loginBackListener)
                        val loginRsp = gson.fromJson(text, JLoginRsp::class.java)
                        LogUtil.addLog("Login" + loginBackListener, "PNRouterServiceMessageReceiver")
                        KLog.i(loginRsp)
                        loginBackListener?.loginBack(loginRsp)
                        registerListener?.loginBack(loginRsp)
                        adminRecoveryCallBack?.loginBack(loginRsp)
                        selcectCircleCallBack?.loginBack(loginRsp)
                        mainInfoBack?.loginBack(loginRsp)
                        chatCallBack?.loginBack(loginRsp)
                        groupchatCallBack?.loginBack(loginRsp)
                    }
                    "LogOut" -> {
                        val JLogOutRsp = gson.fromJson(text, JLogOutRsp::class.java)
                        logOutBack?.logOutBack(JLogOutRsp)
                        selcectCircleCallBack?.logOutBack(JLogOutRsp)
                    }
                    //admin登陆
                    "RouterLogin" -> {
                        val JAdminLoginRsp = gson.fromJson(text, JAdminLoginRsp::class.java)
                        adminLoginCallBack?.login(JAdminLoginRsp)

                    }
                    //admin修改密码
                    "ResetRouterKey" -> {
                        val JAdminUpdataPasswordRsp = gson.fromJson(text, JAdminUpdataPasswordRsp::class.java)
                        adminUpdataPassWordCallBack?.updataPassWord(JAdminUpdataPasswordRsp)

                    }
                    //admin修改code
                    "ResetUserIdcode" -> {
                        val JAdminUpdataCodeRsp = gson.fromJson(text, JAdminUpdataCodeRsp::class.java)
                        adminUpdataCodeCallBack?.updataCode(JAdminUpdataCodeRsp)

                    }
                    //56.	设备管理员修改设备昵称
                    "ResetRouterName" -> {
                        val jResetRouterNameRsp = gson.fromJson(text, JResetRouterNameRsp::class.java)
                        resetRouterNameCallBack?.ResetRouterName(jResetRouterNameRsp)

                    }
                    //60.	用户在线状态通知_V4
                    "OnlineStatusPush" -> {
                        val jOnlineStatusPushRsp = gson.fromJson(text, JOnlineStatusPushRsp::class.java)
                        mainInfoBack?.OnlineStatusPush(jOnlineStatusPushRsp)
                        var userId = SpUtil.getString(AppConfig.instance, ConstantValue.userId, "")
                        var msgData = OnlineStatusPushRsp(0, "", userId!!)
                        if (ConstantValue.isWebsocketConnected) {
                            AppConfig.instance.getPNRouterServiceMessageSender().send(BaseData(4, msgData, jOnlineStatusPushRsp.msgid))
                        } else if (ConstantValue.isToxConnected) {
                            var baseData = BaseData(4, msgData, jOnlineStatusPushRsp.msgid)
                            var baseDataJson = baseData.baseDataToJson().replace("\\", "")
                            if (ConstantValue.isAntox) {
                                //var friendKey: FriendKey = FriendKey(ConstantValue.currentRouterId.substring(0, 64))
                                //MessageHelper.sendMessageFromKotlin(AppConfig.instance, friendKey, baseDataJson, ToxMessageType.NORMAL)
                            } else {
                                ToxCoreJni.getInstance().senToxMessage(baseDataJson, ConstantValue.currentRouterId.substring(0, 64))
                            }
                        }
                    }
                }
            }

        } else {
            when (action) {
                "Recovery" -> {
                    val jRecoveryRsp = gson.fromJson(text, JRecoveryRsp::class.java)
                    KLog.i(jRecoveryRsp)
                    recoveryBackListener?.recoveryBack(jRecoveryRsp)
                    loginBackListener?.recoveryBack(jRecoveryRsp)
                    adminRecoveryCallBack?.recoveryBack(jRecoveryRsp)
                    mainInfoBack?.recoveryBack(jRecoveryRsp)
                    chatCallBack?.recoveryBack(jRecoveryRsp)
                    groupchatCallBack?.recoveryBack(jRecoveryRsp)
                }
                "Register" -> {
                    val JRegisterRsp = gson.fromJson(text, JRegisterRsp::class.java)
                    KLog.i(JRegisterRsp)
                    registerListener?.registerBack(JRegisterRsp)
                    loginBackListener?.registerBack(JRegisterRsp)
                    adminRecoveryCallBack?.registerBack(JRegisterRsp)
                    mainInfoBack?.registerBack(JRegisterRsp)
                    chatCallBack?.registerBack(JRegisterRsp)
                    groupchatCallBack?.registerBack(JRegisterRsp)
                }
                "Login" -> {
                    KLog.i("没有初始化。。登录之后" + loginBackListener + "##" + AppConfig.instance.name)
                    val loginRsp = gson.fromJson(text, JLoginRsp::class.java)
                    LogUtil.addLog("Login" + loginBackListener, "PNRouterServiceMessageReceiver")
                    KLog.i(loginRsp)
                    loginBackListener?.loginBack(loginRsp)
                    registerListener?.loginBack(loginRsp)
                    adminRecoveryCallBack?.loginBack(loginRsp)
                    selcectCircleCallBack?.loginBack(loginRsp)
                    mainInfoBack?.loginBack(loginRsp)
                    chatCallBack?.loginBack(loginRsp)
                    groupchatCallBack?.loginBack(loginRsp)
                }
                "AddFriendReq" -> {
                    val addFreindRsp = gson.fromJson(text, JAddFreindRsp::class.java)
                    KLog.i(addFreindRsp.toString())
//                addfrendCallBack?.addFriendBack(addFreindRsp)
                    userControlleCallBack?.addFriendBack(addFreindRsp)
                }
                "UserInfoUpdate" -> {
                    val jUserInfoUpdateRsp = gson.fromJson(text, JUserInfoUpdateRsp::class.java)
                    KLog.i(jUserInfoUpdateRsp.toString())
                    uerInfoUpdateCallBack?.UserInfoUpdateCallBack(jUserInfoUpdateRsp)
                }
                "UserInfoPush" -> {
                    val jUserInfoPushRsp = gson.fromJson(text, JUserInfoPushRsp::class.java)
                    KLog.i(jUserInfoPushRsp.toString())
                    mainInfoBack?.userInfoPushRsp(jUserInfoPushRsp)
                }
                "LogOut" -> {
                    val JLogOutRsp = gson.fromJson(text, JLogOutRsp::class.java)
                    logOutBack?.logOutBack(JLogOutRsp)
                    selcectCircleCallBack?.logOutBack(JLogOutRsp)
                }

                //对方要加我为好友，服务器给我推送的好友请求
                "AddFriendPush" -> {
                    val addFreindPusRsp = gson.fromJson(text, JAddFriendPushRsp::class.java)
                    KLog.i(addFreindPusRsp.toString())
                    //mainInfoBack?.addFriendPushRsp(addFreindPusRsp)
                    userControlleCallBack?.addFriendPushRsp(addFreindPusRsp)
                }
                //添加好友，对方处理的结果的推送
                "AddFriendDeal" -> {
                    val addFriendDealRsp = gson.fromJson(text, JAddFriendDealRsp::class.java)
                    addFriendDealCallBack?.addFriendDealRsp(addFriendDealRsp)
                    userControlleCallBack?.addFriendDealRsp(addFriendDealRsp)
                }
                //添加好友的返回
                "AddFriendReply" -> {
                    val jAddFriendReplyRsp = gson.fromJson(text, JAddFriendReplyRsp::class.java)
                    mainInfoBack?.addFriendReplyRsp(jAddFriendReplyRsp)
                    userControlleCallBack?.addFriendReplyRsp(jAddFriendReplyRsp)
                }
                //删除对方，服务器返回是否操作成功
                "DelFriendCmd" -> {
                    val jDelFriendCmdRsp = gson.fromJson(text, JDelFriendCmdRsp::class.java)
                    userControlleCallBack!!.delFriendCmdRsp(jDelFriendCmdRsp)
                }
                //删除对方，服务器返回是否操作成功
                "ChangeRemarks" -> {
                    val jChangeRemarksRsp = gson.fromJson(text, JChangeRemarksRsp::class.java)
                    userControlleCallBack!!.changeRemarksRsp(jChangeRemarksRsp)
                }
                //对方删除我，服务器给我推送消息
                "DelFriendPush" -> {
                    val jDelFriendPushRsp = gson.fromJson(text, JDelFriendPushRsp::class.java)
                    mainInfoBack?.delFriendPushRsp(jDelFriendPushRsp)
                    //userControlleCallBack?.delFriendPushRsp(jDelFriendPushRsp)
                }
                //拉取好友列表
                "PullFriend" -> {
                    val jPullFriendRsp = gson.fromJson(text, JPullFriendRsp::class.java)
                    forwardFriendAndGroupBack?.firendList(jPullFriendRsp = jPullFriendRsp)
                    if (forwardFriendAndGroupBack == null) {
                        pullFriendCallBack?.firendList(jPullFriendRsp)
                    }
                    //userControlleCallBack?.firendList(jPullFriendRsp)
                }
                //拉取用户列表
                "PullUserList" -> {
                    val jPullUserRsp = gson.fromJson(text, JPullUserRsp::class.java)
                    pullUserCallBack?.userList(jPullUserRsp)
                    //userControlleCallBack?.firendList(jPullFriendRsp)
                }
                //创建用户
                "CreateNormalUser" -> {
                    val JCreateNormalUserRsp = gson.fromJson(text, JCreateNormalUserRsp::class.java)
                    createUserCallBack?.createUser(JCreateNormalUserRsp)
                    //userControlleCallBack?.firendList(jPullFriendRsp)
                }
                //发送消息服务器给的返回，代表消息服务器已经收到
                "SendMsg" -> {
                    val JSendMsgRsp = gson.fromJson(text, JSendMsgRsp::class.java)
                    if (ConstantValue.isWebsocketConnected) {
                        try {
                            var toSendMessage = AppConfig.instance.getPNRouterServiceMessageSender().toSendChatMessage
                            for (item in toSendMessage) {
                                if (item.msgid == JSendMsgRsp.msgid) {
                                    toSendMessage.remove(item)
                                    var messageEntityList = AppConfig.instance.mDaoMaster!!.newSession().messageEntityDao.loadAll()
                                    if (messageEntityList != null) {
                                        messageEntityList.forEach {
                                            if (it.msgId.equals(JSendMsgRsp.msgid.toString())) {
                                                AppConfig.instance.mDaoMaster!!.newSession().messageEntityDao.delete(it)
                                                KLog.i("私聊消息数据删除")
                                            }
                                        }
                                    }
                                    break
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    chatCallBack?.sendMsgRsp(JSendMsgRsp)
                    convsationCallBack?.sendMsgRsp(JSendMsgRsp)
                }
                "QueryFriend" -> {
                    val jQueryFriendRsp = gson.fromJson(text, JQueryFriendRsp::class.java)

                    chatCallBack?.QueryFriendRep(jQueryFriendRsp)
                }
                //发送消息对方已读
                "ReadMsgPush" -> {
                    val JReadMsgPushRsp = gson.fromJson(text, JReadMsgPushRsp::class.java)
                    chatCallBack?.readMsgPushRsp(JReadMsgPushRsp)
                    mainInfoBack?.readMsgPushRsp(JReadMsgPushRsp)
                }
                //发送文件_Tox消息回馈
                "SendFile" -> {
                    val jSendToxFileRsp = gson.fromJson(text, JSendToxFileRsp::class.java)
                    chatCallBack?.sendToxFileRsp(jSendToxFileRsp)
                }
                //服务器推送过来的别人的消息
                "PushMsg" -> {
                    val JPushMsgRsp = gson.fromJson(text, JPushMsgRsp::class.java)
                    chatCallBack?.pushMsgRsp(JPushMsgRsp)
                    convsationCallBack?.pushMsgRsp(JPushMsgRsp)
                    if (mainInfoBack == null) {
                        AppConfig.instance.tempPushMsgList.add(JPushMsgRsp)
                    }
                    mainInfoBack?.pushMsgRsp(JPushMsgRsp)
                }
                //拉取某个好友的消息,一次十条
                "PullMsg" -> {
                    val JPullMsgRsp = gson.fromJson(text, JPullMsgRsp::class.java)
                    KLog.i("insertMessage:PNRouterServiceMessageReceiver" + chatCallBack)
                    //KLog.i("insertMessage:PNRouterServiceMessageReceiver"+convsationCallBack)
                    chatCallBack?.pullMsgRsp(JPullMsgRsp)
                    convsationCallBack?.pullMsgRsp(JPullMsgRsp)
                }
                "DelMsg" -> {
                    val JDelMsgRsp = gson.fromJson(text, JDelMsgRsp::class.java)
                    chatCallBack?.delMsgRsp(JDelMsgRsp)
                    convsationCallBack?.delMsgRsp(JDelMsgRsp)
                }
                "PushDelMsg" -> {
                    val JDelMsgPushRsp = gson.fromJson(text, JDelMsgPushRsp::class.java)
                    chatCallBack?.pushDelMsgRsp(JDelMsgPushRsp)
                    convsationCallBack?.pushDelMsgRsp(JDelMsgPushRsp)
                    mainInfoBack?.pushDelMsgRsp(JDelMsgPushRsp)
                }
                "PushFile" -> {
                    val JPushFileMsgRsp = gson.fromJson(text, JPushFileMsgRsp::class.java)
                    chatCallBack?.pushFileMsgRsp(JPushFileMsgRsp)
                    mainInfoBack?.pushFileMsgRsp(JPushFileMsgRsp)
                }
                "PullFile" -> {
                    val jToxPullFileRsp = gson.fromJson(text, JToxPullFileRsp::class.java)
                    chatCallBack?.pullFileMsgRsp(jToxPullFileRsp)
                    groupchatCallBack?.pullGroupFileMsgRsp(jToxPullFileRsp);
                    fileManageBack?.pullFileMsgRsp(jToxPullFileRsp)
                    if (fileManageBack == null) {
                        fileMainManageBack?.pullFileMsgRsp(jToxPullFileRsp)
                    }
                }
                //admin登陆
                "RouterLogin" -> {
                    val JAdminLoginRsp = gson.fromJson(text, JAdminLoginRsp::class.java)
                    adminLoginCallBack?.login(JAdminLoginRsp)

                }
                //admin修改密码
                "ResetRouterKey" -> {
                    val JAdminUpdataPasswordRsp = gson.fromJson(text, JAdminUpdataPasswordRsp::class.java)
                    adminUpdataPassWordCallBack?.updataPassWord(JAdminUpdataPasswordRsp)

                }
                //admin修改code
                "ResetUserIdcode" -> {
                    val JAdminUpdataCodeRsp = gson.fromJson(text, JAdminUpdataCodeRsp::class.java)
                    adminUpdataCodeCallBack?.updataCode(JAdminUpdataCodeRsp)

                }
                //56.	设备管理员修改设备昵称
                "ResetRouterName" -> {
                    val jResetRouterNameRsp = gson.fromJson(text, JResetRouterNameRsp::class.java)
                    resetRouterNameCallBack?.ResetRouterName(jResetRouterNameRsp)

                }
                //请求上传文件
                "UploadFileReq" -> {
                    val jUploadFileRsp = gson.fromJson(text, JUploadFileRsp::class.java)
                    fileTaskBack?.UploadFileRsp(jUploadFileRsp)

                }
                //拉取文件列表返回
                "PullFileList" -> {
                    val jPullFileListRsp = gson.fromJson(text, JPullFileListRsp::class.java)
                    fileManageBack?.pullFileListRsp(jPullFileListRsp)
                    fileChooseBack?.pullFileListRsp(jPullFileListRsp)
                    if (fileManageBack == null && fileChooseBack == null) {
                        fileMainManageBack?.pullFileListRsp(jPullFileListRsp)
                    }
                }
                //删除文件
                "DelFile" -> {
                    val jDelFileRsp = gson.fromJson(text, JDelFileRsp::class.java)
                    fileManageBack?.deleFileRsp(jDelFileRsp)
                    if (fileManageBack == null) {
                        fileMainManageBack?.deleFileRsp(jDelFileRsp)
                    }
                }
                //50.	设备磁盘统计信息
                "GetDiskTotalInfo" -> {
                    val jGetDiskTotalInfoRsp = gson.fromJson(text, JGetDiskTotalInfoRsp::class.java)
                    getDiskTotalInfoBack?.getDiskTotalInfoReq(jGetDiskTotalInfoRsp)
                }
                //51.	设备磁盘详细信息
                "GetDiskDetailInfo" -> {
                    val jGetDiskDetailInfoRsp = gson.fromJson(text, JGetDiskDetailInfoRsp::class.java)
                    getDiskDetailInfoBack?.getDiskDetailInfoReq(jGetDiskDetailInfoRsp)
                }
                //52.	设备磁盘模式配置
                "FormatDisk" -> {
                    val jFormatDiskRsp = gson.fromJson(text, JFormatDiskRsp::class.java)
                    formatDiskBack?.formatDiskReq(jFormatDiskRsp)
                    getDiskTotalInfoBack?.formatDiskReq(jFormatDiskRsp)
                }
                //被踢
                "PushLogout" -> {
                    val jPushLogoutRsp = gson.fromJson(text, JPushLogoutRsp::class.java)
                    mainInfoBack?.pushLogoutRsp(jPushLogoutRsp)
                }
                //60.	用户在线状态通知_V4
                "OnlineStatusPush" -> {
                    val jOnlineStatusPushRsp = gson.fromJson(text, JOnlineStatusPushRsp::class.java)
                    mainInfoBack?.OnlineStatusPush(jOnlineStatusPushRsp)
                    mainInfoBack?.OnlineStatusPush(jOnlineStatusPushRsp)
                    var userId = SpUtil.getString(AppConfig.instance, ConstantValue.userId, "")
                    var msgData = OnlineStatusPushRsp(0, "", userId!!)
                    if (ConstantValue.isWebsocketConnected) {
                        AppConfig.instance.getPNRouterServiceMessageSender().send(BaseData(4, msgData, jOnlineStatusPushRsp.msgid))
                    } else if (ConstantValue.isToxConnected) {
                        var baseData = BaseData(4, msgData, jOnlineStatusPushRsp.msgid)
                        var baseDataJson = baseData.baseDataToJson().replace("\\", "")
                        if (ConstantValue.isAntox) {
                            //var friendKey: FriendKey = FriendKey(ConstantValue.currentRouterId.substring(0, 64))
                            //MessageHelper.sendMessageFromKotlin(AppConfig.instance, friendKey, baseDataJson, ToxMessageType.NORMAL)
                        } else {
                            ToxCoreJni.getInstance().senToxMessage(baseDataJson, ConstantValue.currentRouterId.substring(0, 64))
                        }
                    }
                }
                //77.	文件重命名
                "FileRename" -> {
                    val JFileRenameRsp = gson.fromJson(text, JFileRenameRsp::class.java)
                    fileMainManageBack?.fileRenameReq(JFileRenameRsp)
                }
                //78.	文件转发
                "FileForward" -> {
                    val JFileForwardRsp = gson.fromJson(text, JFileForwardRsp::class.java)
                    fileForwardBack?.fileForwardReq(JFileForwardRsp)
                    chatCallBack?.fileForwardReq(JFileForwardRsp)
                    groupchatCallBack?.fileForwardReq(JFileForwardRsp)
                }
                //79.	用户上传头像
                "UploadAvatar" -> {
                    val JUploadAvatarRsp = gson.fromJson(text, JUploadAvatarRsp::class.java)
                    if (uploadAvatarBack != null) {
                        uploadAvatarBack?.uploadAvatarReq(JUploadAvatarRsp)
                    } else {

                        mainInfoBack?.uploadAvatarReq(JUploadAvatarRsp)
                    }

                }
                //80.更新好友用户头像
                "UpdateAvatar" -> {
                    val JUpdateAvatarRsp = gson.fromJson(text, JUpdateAvatarRsp::class.java)
                    //updateAvatarBackBack?.updateAvatarReq(JUpdateAvatarRsp)
                    userControlleCallBack?.updateAvatarReq(JUpdateAvatarRsp)
                    //chatCallBack?.updateAvatarReq(JUpdateAvatarRsp)
                }
                //61.	用户创建群组会话
                "CreateGroup" -> {
                    val JCreateGroupRsp = gson.fromJson(text, JCreateGroupRsp::class.java)
                    groupBack?.createGroup(JCreateGroupRsp)
                }
                //67.	拉取群列表
                "GroupListPull" -> {
                    val JGroupListPullRsp = gson.fromJson(text, JGroupListPullRsp::class.java)
                    groupListPullBack?.groupListPull(JGroupListPullRsp)
                    forwardFriendAndGroupBack?.groupListPull(jGroupListPullRsp = JGroupListPullRsp)
                    if (groupListPullBack == null && forwardFriendAndGroupBack == null) {
                        mainInfoBack?.groupListPull(JGroupListPullRsp)
                    }
                }
                //71.	群组会话中发文本消息
                "GroupSendMsg" -> {
                    val JGroupSendMsgRsp = gson.fromJson(text, JGroupSendMsgRsp::class.java)
                    if (ConstantValue.isWebsocketConnected) {
                        try {
                            var toSendMessage = AppConfig.instance.getPNRouterServiceMessageSender().toSendChatMessage
                            for (item in toSendMessage) {
                                if (item.msgid == JGroupSendMsgRsp.msgid) {
                                    toSendMessage.remove(item)
                                    var messageEntityList = AppConfig.instance.mDaoMaster!!.newSession().messageEntityDao.loadAll()
                                    if (messageEntityList != null) {
                                        messageEntityList.forEach {
                                            if (it.msgId.equals(JGroupSendMsgRsp.msgid.toString())) {
                                                AppConfig.instance.mDaoMaster!!.newSession().messageEntityDao.delete(it)
                                                KLog.i("群聊消息数据删除")
                                            }
                                        }
                                    }
                                    break
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    groupchatCallBack?.sendGroupMsgRsp(JGroupSendMsgRsp)
                    convsationCallBack?.sendGroupMsgRsp(JGroupSendMsgRsp)
                }
                //服务器推送过来的别人的群消息
                "GroupMsgPush" -> {
                    val JGroupMsgPushRsp = gson.fromJson(text, JGroupMsgPushRsp::class.java)
                    groupchatCallBack?.pushGroupMsgRsp(JGroupMsgPushRsp)
                    //convsationCallBack?.pushGroupMsgRsp(JGroupMsgPushRsp)
                    if (mainInfoBack == null) {
                        AppConfig.instance.tempPushGroupMsgList.add(JGroupMsgPushRsp)
                    }
                    mainInfoBack?.pushGroupMsgRsp(JGroupMsgPushRsp)
                }
                //拉取群聊消息
                "GroupMsgPull" -> {
                    val JGroupMsgPullRsp = gson.fromJson(text, JGroupMsgPullRsp::class.java)
                    groupchatCallBack?.pullGroupMsgRsp(JGroupMsgPullRsp)
                    convsationCallBack?.pullGroupMsgRsp(JGroupMsgPullRsp)
                }
                "GroupDelMsg" -> {
                    val JGroupDelMsgRsp = gson.fromJson(text, JGroupDelMsgRsp::class.java)
                    groupchatCallBack?.delGroupMsgRsp(JGroupDelMsgRsp)
                }
                "GroupUserPull" -> {
                    val jGroupUserPullRsp = gson.fromJson(text, JGroupUserPullRsp::class.java)
                    groupDetailBack?.groupUserPull(jGroupUserPullRsp)
                    groupMemberback?.groupUserPull(jGroupUserPullRsp)
                }
                "GroupConfig" -> {
                    val jGroupConfigRsp = gson.fromJson(text, JGroupConfigRsp::class.java)
                    groupDetailBack?.groupConfig(jGroupConfigRsp)
                }
                "GroupQuit" -> {
                    val jGroupQuitRsp = gson.fromJson(text, JGroupQuitRsp::class.java)
                    groupDetailBack?.quitGroup(jGroupQuitRsp)
                }
                "InviteGroup" -> {
                    val jGroupInviteDealRsp = gson.fromJson(text, JGroupInviteDealRsp::class.java)
                    groupDetailBack?.groupInvite(jGroupInviteDealRsp)
                    groupMemberback?.groupInvite(jGroupInviteDealRsp)
                }
                "GroupVerifyPush" -> {
                    val jGroupVerifyPushRsp = gson.fromJson(text, JGroupVerifyPushRsp::class.java)
                    userControlleCallBack?.groupVerifyPush(jGroupVerifyPushRsp)
                }
                "GroupVerify" -> {
                    val jGroupVerifyRsp = gson.fromJson(text, JGroupVerifyRsp::class.java)
                    groupMemberOpreateBack?.groupMemberOpreate(jGroupVerifyRsp)
                }
                "GroupSysPush" -> {
                    val JGroupSysPushRsp = gson.fromJson(text, JGroupSysPushRsp::class.java)
                    groupchatCallBack?.droupSysPushRsp(JGroupSysPushRsp)
                    mainInfoBack?.droupSysPushRsp(JGroupSysPushRsp)
                }
                //发送文件_群消息回馈
                "GroupSendFileDone" -> {
                    val JGroupSendFileDoneRsp = gson.fromJson(text, JGroupSendFileDoneRsp::class.java)
                    groupchatCallBack?.sendGroupToxFileRsp(JGroupSendFileDoneRsp)
                }
                //83.	拉取临时账户信息
                "PullTmpAccount" -> {
                    val jPullTmpAccountRsp = gson.fromJson(text, JPullTmpAccountRsp::class.java)
                    createUserCallBack?.pullTmpAccount(jPullTmpAccountRsp)
                    if(createUserCallBack == null)
                    {
                        resetRouterNameCallBack?.pullTmpAccount(jPullTmpAccountRsp)
                    }
                    pullTmpAccountBack?.pullTmpAccount(jPullTmpAccountRsp)
                    //userControlleCallBack?.firendList(jPullFriendRsp)
                }
                "DelUser" -> {
                    val JRemoveMemberRsp = gson.fromJson(text, JRemoveMemberRsp::class.java)
                    removeMemberCallBack?.removeMember(JRemoveMemberRsp)
                }
                "EnableQlcNode" -> {
                    val jEnableQlcNodeRsp = gson.fromJson(text, JEnableQlcNodeRsp::class.java)
                    qlcNodeCallBack?.enableQlcNodeRsp(jEnableQlcNodeRsp)
                }
                "CheckQlcNode" -> {
                    val jCheckQlcNodeRsp = gson.fromJson(text, JCheckQlcNodeRsp::class.java)
                    qlcNodeCallBack?.checkQlcNodeRsp(jCheckQlcNodeRsp)
                }
                "SaveEmailConf" -> {
                    val jSaveEmailConfRsp = gson.fromJson(text, JSaveEmailConfRsp::class.java)
                    saveEmailConfCallback?.saveEmailConf(jSaveEmailConfRsp)
                    saveEmailConfChooseCallback?.saveEmailConf(jSaveEmailConfRsp)
                }
                "CheckmailUkey" -> {
                    val jCheckmailUkeyRsp = gson.fromJson(text, JCheckmailUkeyRsp::class.java)
                    checkmailUkeyCallback?.checkmailUkey(jCheckmailUkeyRsp)
                }
                "BakupEmail" -> {
                    val JBakupEmailRsp = gson.fromJson(text, JBakupEmailRsp::class.java)
                    bakupEmailCallback?.BakupEmailBack(JBakupEmailRsp)
                }
                "PullMailList" -> {
                    val JPullMailListRsp = gson.fromJson(text, JPullMailListRsp::class.java)
                    pullMailListCallback?.PullMailListBack(JPullMailListRsp)
                }
                "DelEmailConf" -> {
                    val JDelEmailConfRsp = gson.fromJson(text, JDelEmailConfRsp::class.java)
                    delEmailConfCallback?.DelEmailConfBack(JDelEmailConfRsp)
                }
                "DelEmail" -> {
                    val JDelEmailRsp = gson.fromJson(text, JDelEmailRsp::class.java)
                    dlEmailCallback?.DelEmailBack(JDelEmailRsp)
                }
                "BakMailsNum" -> {
                    val JBakMailsNumRsp = gson.fromJson(text, JBakMailsNumRsp::class.java)
                    bakMailsNumCallback?.BakMailsNumBack(JBakMailsNumRsp)
                }
                "BakMailsCheck" -> {
                    val JBakMailsCheckRsp = gson.fromJson(text, JBakMailsCheckRsp::class.java)
                    bakMailsCheckCallback?.BakMailsCheckBack(JBakMailsCheckRsp)
                }
                "MailSendNotice" -> {
                    val JMailSendNoticeRsp = gson.fromJson(text, JMailSendNoticeRsp::class.java)
                    mailSendNoticeCallback?.MailSendNoticeBack(JMailSendNoticeRsp)
                }
                "SysMsgPush" -> {
                    val JSysMsgPushRsp = gson.fromJson(text, JSysMsgPushRsp::class.java)
                    mainInfoBack?.sysMsgPushRsp(JSysMsgPushRsp)
                }
                "AddFriendsAuto" -> {
                    val JAddFriendsAutoRsp = gson.fromJson(text, JAddFriendsAutoRsp::class.java)
                    checkmailUkeyCallback?.addFriendsAuto(JAddFriendsAutoRsp)
                }
                "FileAction" -> {
                    val jFileActionRsp = gson.fromJson(text, JFileActionRsp::class.java)
                    if(nodeFilesListPullCallback == null)
                    {
                        nodeFileCallback?.fileAction(jFileActionRsp)
                    }
                    nodeSelectFileCallback?.fileAction(jFileActionRsp)
                    nodeFilesListPullCallback?.fileAction(jFileActionRsp)
                }
                "FilePathsPull" -> {
                    val jFilePathsPulRsp = gson.fromJson(text, JFilePathsPulRsp::class.java)
                    nodeFileCallback?.filePathsPull(jFilePathsPulRsp)
                    nodeSelectFileCallback?.filePathsPull(jFilePathsPulRsp)
                }
                "FilesListPull" -> {
                    val jFilesListPullRsp = gson.fromJson(text, JFilesListPullRsp::class.java)
                    nodeFilesListPullCallback?.filesListPull(jFilesListPullRsp)
                }
                "BakFile" -> {
                    val jBakFileRsp = gson.fromJson(text, JBakFileRsp::class.java)
                    if(bakAddrUserNumCallback == null)
                    {
                        mainInfoBack?.bakFileBack(jBakFileRsp)
                    }

                    bakAddrUserNumCallback?.bakFileBack(jBakFileRsp)


                }
                "BakAddrBookInfo" -> {
                    val jBakAddrUserNumRsp = gson.fromJson(text, JBakAddrUserNumRsp::class.java)
                    bakAddrUserNumCallback?.bakAddrUserNum(jBakAddrUserNumRsp)
                    bakAddrUserNumOutCallback?.bakAddrUserNum(jBakAddrUserNumRsp)
                }
                "BakContent" -> {
                    val JBakContentRsp = gson.fromJson(text, JBakContentRsp::class.java)
                    bakContentCallback?.BakContentBack(JBakContentRsp)
                }
                "PullBakContent" -> {
                    val JPullBakContentRsp = gson.fromJson(text, JPullBakContentRsp::class.java)
                    pullBakContentCallback?.pullBakContentBack(JPullBakContentRsp)
                    pullSecondBakContentCallback?.pullBakContentBack(JPullBakContentRsp)
                }
                "GetBakContentStat" -> {
                    val jGetBakContentStatRsp = gson.fromJson(text, JGetBakContentStatRsp::class.java)
                    getBakContentStatCallback?.getBakContentStatCallback(jGetBakContentStatRsp)
                    bakAddrUserNumOutCallback?.getBakContentStatCallback(jGetBakContentStatRsp)
                }
                "DelBakContent" -> {
                    val JDelBakContentRsp = gson.fromJson(text, JDelBakContentRsp::class.java)
                    pullBakContentCallback?.delBakContentBack(JDelBakContentRsp)
                    pullSecondBakContentCallback?.delBakContentBack(JDelBakContentRsp)
                }
                "GetWalletAccount" -> {
                    var jGetWalletAccountReq = gson.fromJson(text, JGetWalletAccountRsp::class.java)
                    walletAccountCallback?.getWalletAccountBack(jGetWalletAccountReq)
                }
                "SetWalletAccount" -> {
                    var jSetWalletAccountRsp = gson.fromJson(text, JSetWalletAccountRsp::class.java)
                    walletAccountCallback?.setWalletAccountBack(jSetWalletAccountRsp)
                }
            }
        }

        messageListner?.onMessage(baseData)
    }


//    private val socket: PushServiceSocket

    var pipe: SignalServiceMessagePipe? = null
    var messageListner: MessageReceivedCallback? = null
    var recoveryBackListener: RecoveryMessageCallback? = null
    var registerListener: RegisterMessageCallback? = null
    var loginBackListener: LoginMessageCallback? = null
    var addfrendCallBack: AddfrendCallBack? = null
    var uerInfoUpdateCallBack: UserInfoUpdateCallBack? = null
    var logOutBack: LogOutCallBack? = null
    var mainInfoBack: MainInfoBack? = null
    var addFriendDealCallBack: AddFriendDealCallBack? = null
    var chatCallBack: ChatCallBack? = null
    var groupchatCallBack: GroupChatCallBack? = null

    var convsationCallBack: CoversationCallBack? = null

    var pullFriendCallBack: PullFriendCallBack? = null

    var pullUserCallBack: PullUserCallBack? = null

    var createUserCallBack: CreateUserCallBack? = null

    var pullTmpAccountBack: PullTmpAccountBack? = null

    var adminLoginCallBack: AdminLoginCallBack? = null

    var resetRouterNameCallBack: ResetRouterNameCallBack? = null

    var qlcNodeCallBack: QlcNodeCallBack? = null

    var adminUpdataPassWordCallBack: AdminUpdataPassWordCallBack? = null

    var adminUpdataCodeCallBack: AdminUpdataCodeCallBack? = null

    var adminRecoveryCallBack: AdminRecoveryCallBack? = null

    var userControlleCallBack: UserControlleCallBack? = null

    var fileTaskBack: FileTaskBack? = null

    var bigImageBack : BigImageBack? = null

    var fileManageBack: FileManageBack? = null
    var fileMainManageBack: FileMainManageBack? = null
    var fileChooseBack: FileChooseBack? = null

    var getDiskTotalInfoBack: GetDiskTotalInfoBack? = null

    var getDiskDetailInfoBack: GetDiskDetailInfoBack? = null

    var formatDiskBack: FormatDiskBack? = null

    var fileForwardBack: FileForwardBack? = null
    var uploadAvatarBack: UploadAvatarBack? = null
    var updateAvatarBackBack: UpdateAvatarBack? = null
    var groupBack: GroupBack? = null
    var groupListPullBack: GroupListPullBack? = null
    var groupDetailBack: GroupDetailBack? = null
    var groupMemberback: GroupMemberback? = null
    var groupMemberOpreateBack: GroupMemberOpreateBack? = null

    var selcectCircleCallBack: SelcectCircleCallBack? = null

    var forwardFriendAndGroupBack : ForwardFriendAndGroupBack? = null

    var removeMemberCallBack:RemoveMemberCallBack ? = null

    var saveEmailConfCallback: SaveEmailConfCallback? = null

    var saveEmailConfChooseCallback: SaveEmailConfCallback? = null

    var checkmailUkeyCallback: CheckmailUkeyCallback? = null

    var bakupEmailCallback:BakupEmailCallback? = null

    var pullMailListCallback:PullMailListCallback? = null

    var delEmailConfCallback:DelEmailConfCallback? = null

    var dlEmailCallback:DelEmailCallback? = null

    var bakMailsNumCallback:BakMailsNumCallback?= null

    var bakMailsCheckCallback: BakMailsCheckCallback? = null

    var mailSendNoticeCallback:MailSendNoticeCallback? = null;

    var nodeFileCallback:NodeFileCallback? = null;

    var nodeSelectFileCallback:NodeFileCallback? = null;

    var nodeFilesListPullCallback:NodeFilesListPullCallback? = null;

    var bakAddrUserNumCallback:BakAddrUserNumCallback? = null;

    var bakAddrUserNumOutCallback:BakAddrUserNumOutCallback? = null;

    var bakContentCallback:BakContentCallback? = null;

    var getBakContentStatCallback:GetBakContentStatCallback? = null
    var pullBakContentCallback:PullBakContentCallback? = null;
    var pullSecondBakContentCallback:PullBakContentCallback? = null

    var walletAccountCallback : WalletAccountCallback? = null

    /**
     * Construct a PNRouterServiceMessageReceiver.
     *
     * @param urls The URL of the Signal Service.
     * @param user The Signal Service username (eg. phone number).
     * @param password The Signal Service user password.
     * @param signalingKey The 52 byte signaling key assigned to this user at registration.
     */
    constructor(urls: SignalServiceConfiguration,
                user: String, password: String,
                signalingKey: String, userAgent: String,
                listener: ConnectivityListener) : this(urls, StaticCredentialsProvider(user, password, signalingKey), userAgent, listener) {
    }

    init {
        //        this.socket = PushServiceSocket(urls, credentialsProvider, userAgent)
        KLog.i("没有初始化。。PNRouterServiceMessageReceiver")
        createMessagePipe()
//        pipe!!.read(object : SignalServiceMessagePipe.NullMessagePipeCallback() {
//            override fun onMessage(envelope: BaseData<*>) {
//                Log.i("receiver", envelope.baseDataToJson())
//            }
//        })
    }

//    @Throws(IOException::class)
//    fun retrieveProfile(address: SignalServiceAddress): SignalServiceProfile {
//        return socket.retrieveProfile(address)
//    }
//
//    @Throws(IOException::class)
//    fun retrieveProfileAvatar(path: String, destination: File, profileKey: ByteArray, maxSizeBytes: Int): InputStream {
//        socket.retrieveProfileAvatar(path, destination, maxSizeBytes)
//        return ProfileCipherInputStream(FileInputStream(destination), profileKey)
//    }

    /**
     * Retrieves a SignalServiceAttachment.
     *
     * @param pointer The [SignalServiceAttachmentPointer]
     * received in a [SignalServiceDataMessage].
     * @param destination The download destination for this attachment.
     * @param listener An optional listener (may be null) to receive callbacks on download progress.
     *
     * @return An InputStream that streams the plaintext attachment contents.
     * @throws IOException
     * @throws InvalidMessageException
     */
//    @Throws(IOException::class, InvalidMessageException::class)
//    @JvmOverloads
//    fun retrieveAttachment(pointer: SignalServiceAttachmentPointer, destination: File, maxSizeBytes: Int, listener: ProgressListener? = null): InputStream {
//        if (!pointer.getDigest().isPresent()) throw InvalidMessageException("No attachment digest!")
//
//        socket.retrieveAttachment(pointer.getRelay().orNull(), pointer.getId(), destination, maxSizeBytes, listener)
//        return AttachmentCipherInputStream.createFor(destination, pointer.getSize().or(0), pointer.getKey(), pointer.getDigest().get())
//    }

    /**
     * Creates a pipe for receiving SignalService messages.
     *
     * Callers must call [SignalServiceMessagePipe.shutdown] when finished with the pipe.
     *
     * @return A SignalServiceMessagePipe for receiving Signal Service messages.
     */
    fun createMessagePipe(): SignalServiceMessagePipe {
        /*KLog.i("没有初始化。。createMessagePipe" + pipe)
        KLog.i("没有初始化。。PNRouterServiceMessageReceiver" + this)
        KLog.i("没有初始化。。PNRouterServiceMessageReceiver loginBackListener" + loginBackListener)
        KLog.i("超时调试：1" + pipe)*/
        if (pipe == null) {
            KLog.i("超时调试：webSocketConnection createMessagePipe")
            val webSocket = WebSocketConnection(urls.signalServiceUrls[0].url, urls.signalServiceUrls[0].trustStore, credentialsProvider, userAgent, connectivityListener)
            KLog.i("超时调试：webSocket createMessagePipe" + webSocket)
            pipe = SignalServiceMessagePipe(webSocket, credentialsProvider)
            KLog.i("超时调试：pipe createMessagePipe" + pipe)
            pipe!!.messagePipeCallback = this
            return pipe!!
        } else {
            return pipe!!
        }
    }

    fun shutdown() {
        pipe!!.shutdown()
    }

    fun close() {
        KLog.i("pipe close。。。")
        pipe!!.close()
    }

    fun reConnect() {
        KLog.i("pipe reConnect。。。")
        pipe!!.reConenct()
    }

    fun getTrustStore(): TrustStore {
        return urls.signalServiceUrls[0].trustStore
    }

    /**
     * 作为对外暴露的接口，聊天消息统一用这个接口对外输出消息
     */
    interface MessageReceivedCallback {
        fun onMessage(baseData: BaseData)
    }

    class NullMessageReceivedCallback : MessageReceivedCallback {
        override fun onMessage(envelope: BaseData) {}
    }

    interface RecoveryMessageCallback {
        fun recoveryBack(recoveryRsp: JRecoveryRsp)
    }

    interface RegisterMessageCallback {
        fun registerBack(registerRsp: JRegisterRsp)
        fun loginBack(loginRsp: JLoginRsp)
    }

    interface LoginMessageCallback {
        fun registerBack(registerRsp: JRegisterRsp)
        fun loginBack(loginRsp: JLoginRsp)
        fun recoveryBack(recoveryRsp: JRecoveryRsp)
    }

    interface AddfrendCallBack {
        fun addFriendBack(addFriendRsp: JAddFreindRsp)
    }

    interface UserInfoUpdateCallBack {
        fun UserInfoUpdateCallBack(jUserInfoUpdateRsp: JUserInfoUpdateRsp)
    }

    interface LogOutCallBack {
        fun logOutBack(jLogOutRsp: JLogOutRsp)
    }

    interface MainInfoBack {
        fun registerBack(registerRsp: JRegisterRsp)
        fun loginBack(loginRsp: JLoginRsp)
        fun recoveryBack(recoveryRsp: JRecoveryRsp)

        fun addFriendPushRsp(jAddFriendPushRsp: JAddFriendPushRsp)
        fun addFriendReplyRsp(jAddFriendReplyRsp: JAddFriendReplyRsp)
        fun delFriendPushRsp(jDelFriendPushRsp: JDelFriendPushRsp)
        fun firendList(jPullFriendRsp: JPullFriendRsp)
        fun pushMsgRsp(pushMsgRsp: JPushMsgRsp)
        fun pushGroupMsgRsp(pushMsgRsp: JGroupMsgPushRsp)
        fun pushDelMsgRsp(delMsgPushRsp: JDelMsgPushRsp)
        fun pushFileMsgRsp(jPushFileMsgRsp: JPushFileMsgRsp)
        fun userInfoPushRsp(jUserInfoPushRsp: JUserInfoPushRsp)
        fun OnlineStatusPush(jOnlineStatusPushRsp: JOnlineStatusPushRsp)
        fun readMsgPushRsp(jReadMsgPushRsp: JReadMsgPushRsp)
        fun pushLogoutRsp(jPushLogoutRsp: JPushLogoutRsp)
        fun uploadAvatarReq(jUploadAvatarRsp: JUploadAvatarRsp)
        fun droupSysPushRsp(jGroupSysPushRsp: JGroupSysPushRsp)
        fun groupListPull(jGroupListPullRsp: JGroupListPullRsp)
        fun sysMsgPushRsp(jSysMsgPushRsp:JSysMsgPushRsp)
        fun bakFileBack(jBakFileRsp: JBakFileRsp)
    }
    interface BigImageBack {
        fun registerBack(registerRsp: JRegisterRsp)
        fun loginBack(loginRsp: JLoginRsp)
        fun recoveryBack(recoveryRsp: JRecoveryRsp)
    }
    interface FileTaskBack {
        fun UploadFileRsp(jUploadFileRsp: JUploadFileRsp)
    }

    interface UserControlleCallBack {
        fun addFriendPushRsp(jAddFriendPushRsp: JAddFriendPushRsp)
        fun addFriendReplyRsp(jAddFriendReplyRsp: JAddFriendReplyRsp)
        fun delFriendPushRsp(jDelFriendPushRsp: JDelFriendPushRsp)
        fun firendList(jPullFriendRsp: JPullFriendRsp)
        fun addFriendBack(addFriendRsp: JAddFreindRsp)
        fun addFriendDealRsp(jAddFriendDealRsp: JAddFriendDealRsp)
        fun delFriendCmdRsp(jDelFriendCmdRsp: JDelFriendCmdRsp)
        fun changeRemarksRsp(jChangeRemarksRsp: JChangeRemarksRsp)
        fun updateAvatarReq(jUpdateAvatarRsp: JUpdateAvatarRsp)
        fun groupVerifyPush(jGroupVerifyPushRsp: JGroupVerifyPushRsp)
//        fun groupVerify()
    }

    interface AddFriendDealCallBack {
        fun addFriendDealRsp(jAddFriendDealRsp: JAddFriendDealRsp)
    }

    interface DelFriendCallBack {
        fun delFriendCmdRsp(jDelFriendCmdRsp: JDelFriendCmdRsp)
    }

    interface PullFriendCallBack {
        fun firendList(jPullFriendRsp: JPullFriendRsp)
    }

    /**
     * 转发页面获取好友和群组的接口
     */
    interface ForwardFriendAndGroupBack {
        fun firendList(jPullFriendRsp: JPullFriendRsp)
        fun groupListPull(jGroupListPullRsp: JGroupListPullRsp)
    }

    interface PullUserCallBack {
        fun userList(jPullUserRsp: JPullUserRsp)
    }

    interface CreateUserCallBack {
        fun createUser(jCreateNormalUserRsp: JCreateNormalUserRsp)
        fun pullTmpAccount(jPullTmpAccountRsp: JPullTmpAccountRsp)
    }
    interface PullTmpAccountBack {
        fun pullTmpAccount(jPullTmpAccountRsp: JPullTmpAccountRsp)
    }
    interface AdminLoginCallBack {
        fun login(jAdminLoginRsp: JAdminLoginRsp)
    }

    interface ResetRouterNameCallBack {
        fun ResetRouterName(jResetRouterNameRsp: JResetRouterNameRsp)
        fun pullTmpAccount(jPullTmpAccountRsp: JPullTmpAccountRsp)
    }
    interface QlcNodeCallBack {
        fun enableQlcNodeRsp(jEnableQlcNodeRsp: JEnableQlcNodeRsp)
        fun checkQlcNodeRsp(jCheckQlcNodeRsp: JCheckQlcNodeRsp)
    }
    interface AdminUpdataCodeCallBack {
        fun updataCode(jAdminUpdataCodeRsp: JAdminUpdataCodeRsp)
    }

    interface AdminRecoveryCallBack {
        fun registerBack(registerRsp: JRegisterRsp)
        fun recoveryBack(recoveryRsp: JRecoveryRsp)
        fun loginBack(loginRsp: JLoginRsp)

    }

    interface AdminUpdataPassWordCallBack {
        fun updataPassWord(jAdminUpdataPasswordRsp: JAdminUpdataPasswordRsp)
    }

    interface ChatCallBack {
        fun sendMsg(FromId: String, ToId: String, FriendPublicKey: String, Msg: String);
        fun sendMsgV3(FromIndex: String, ToIndex: String, FriendPublicKey: String, Msg: String,AssocId:String): String;
        fun sendMsgRsp(sendMsgRsp: JSendMsgRsp)
        fun pushMsgRsp(pushMsgRsp: JPushMsgRsp)
        fun pullMsgRsp(pushMsgRsp: JPullMsgRsp)
        fun delMsgRsp(delMsgRsp: JDelMsgRsp)
        fun pushDelMsgRsp(delMsgPushRsp: JDelMsgPushRsp)
        fun pushFileMsgRsp(jPushFileMsgRsp: JPushFileMsgRsp)
        fun readMsgPushRsp(jReadMsgPushRsp: JReadMsgPushRsp)
        fun sendToxFileRsp(jSendToxFileRsp: JSendToxFileRsp)
        fun pullFileMsgRsp(jJToxPullFileRsp: JToxPullFileRsp)
        fun userInfoPushRsp(jUserInfoPushRsp: JUserInfoPushRsp)
        fun queryFriend(FriendId: String)
        fun QueryFriendRep(jQueryFriendRsp: JQueryFriendRsp)
        fun updateAvatarReq(jUpdateAvatarRsp: JUpdateAvatarRsp)
        fun fileForwardReq(jFileForwardRsp: JFileForwardRsp)
        fun registerBack(registerRsp: JRegisterRsp)
        fun loginBack(loginRsp: JLoginRsp)
        fun recoveryBack(recoveryRsp: JRecoveryRsp)
    }

    interface GroupChatCallBack {
        fun sendGroupMsg(userId: String, gId: String, point: String, Msg: String, userKey: String,AssocId:String): String;
        fun sendGroupMsgRsp(jGroupSendMsgRsp: JGroupSendMsgRsp)
        fun pushGroupMsgRsp(pushMsgRsp: JGroupMsgPushRsp)
        fun pullGroupMsgRsp(pushMsgRsp: JGroupMsgPullRsp)
        fun delGroupMsgRsp(delMsgRsp: JGroupDelMsgRsp)
        fun droupSysPushRsp(jGroupSysPushRsp: JGroupSysPushRsp)
        fun pushDelGroupMsgRsp(delMsgPushRsp: JDelMsgPushRsp)
        /* fun pushGroupFileMsgRsp(jPushFileMsgRsp: JPushFileMsgRsp)*/
        fun readMsgPushRsp(jReadMsgPushRsp: JReadMsgPushRsp)

        fun sendGroupToxFileRsp(jSendToxFileRsp: JGroupSendFileDoneRsp)
        fun pullGroupFileMsgRsp(jJToxPullFileRsp: JToxPullFileRsp)
        fun userInfoGroupPushRsp(jUserInfoPushRsp: JUserInfoPushRsp)
        fun fileForwardReq(jFileForwardRsp: JFileForwardRsp)
        fun registerBack(registerRsp: JRegisterRsp)
        fun loginBack(loginRsp: JLoginRsp)
        fun recoveryBack(recoveryRsp: JRecoveryRsp)
    }

    interface CoversationCallBack {
        fun sendMsgRsp(sendMsgRsp: JSendMsgRsp)
        fun sendGroupMsgRsp(jGroupSendMsgRsp: JGroupSendMsgRsp)
        fun pushMsgRsp(pushMsgRsp: JPushMsgRsp)
        fun pushGroupMsgRsp(pushMsgRsp: JGroupMsgPushRsp)
        fun pullMsgRsp(pushMsgRsp: JPullMsgRsp)
        fun pullGroupMsgRsp(pushMsgRsp: JGroupMsgPullRsp)
        fun delMsgRsp(delMsgRsp: JDelMsgRsp)
        fun pushDelMsgRsp(delMsgPushRsp: JDelMsgPushRsp)
    }

    interface GlobalBack {
        fun pushMsgRsp(pushMsgRsp: JPushMsgRsp)
    }

    interface FileManageBack {
        fun pullFileListRsp(pullFileListRsp: JPullFileListRsp)
        fun deleFileRsp(jDelFileRsp: JDelFileRsp)
        fun pullFileMsgRsp(jJToxPullFileRsp: JToxPullFileRsp)
    }

    //主页面文件对文件的操作回调
    interface FileMainManageBack {
        fun pullFileListRsp(pullFileListRsp: JPullFileListRsp)
        fun deleFileRsp(jDelFileRsp: JDelFileRsp)
        fun pullFileMsgRsp(jJToxPullFileRsp: JToxPullFileRsp)
        fun fileRenameReq(jFileRenameRsp: JFileRenameRsp)
    }

    interface FileChooseBack {
        fun pullFileListRsp(pullFileListRsp: JPullFileListRsp)
    }

    interface GetDiskTotalInfoBack {
        fun getDiskTotalInfoReq(JGetDiskTotalInfoRsp: JGetDiskTotalInfoRsp)
        fun formatDiskReq(jFormatDiskRsp: JFormatDiskRsp)
    }

    interface GetDiskDetailInfoBack {
        fun getDiskDetailInfoReq(JGetDiskDetailInfoRsp: JGetDiskDetailInfoRsp)
    }

    interface FormatDiskBack {
        fun formatDiskReq(jFormatDiskRsp: JFormatDiskRsp)
    }

    interface FileForwardBack {
        fun fileForwardReq(jFileForwardRsp: JFileForwardRsp)
    }

    interface UploadAvatarBack {
        fun uploadAvatarReq(jUploadAvatarRsp: JUploadAvatarRsp)
    }

    interface UpdateAvatarBack {
        fun updateAvatarReq(jUpdateAvatarRsp: JUpdateAvatarRsp)
    }

    interface GroupBack {
        fun createGroup(jCreateGroupRsp: JCreateGroupRsp)
    }

    interface GroupListPullBack {
        fun groupListPull(jGroupListPullRsp: JGroupListPullRsp)
    }

    interface GroupDetailBack {
        fun groupUserPull(jGroupUserPullRsp: JGroupUserPullRsp)
        fun groupConfig(jGroupConfigRsp: JGroupConfigRsp)
        fun quitGroup(jGroupQuitRsp: JGroupQuitRsp)
        fun groupInvite(jGroupInviteDealRsp: JGroupInviteDealRsp)
    }

    interface GroupMemberback {
        fun groupUserPull(jGroupUserPullRsp: JGroupUserPullRsp)
        fun groupInvite(jGroupInviteDealRsp: JGroupInviteDealRsp)
    }

    interface GroupMemberOpreateBack {
        fun groupMemberOpreate(jGroupVerifyRsp: JGroupVerifyRsp)
    }

    interface SelcectCircleCallBack {
        fun logOutBack(jLogOutRsp: JLogOutRsp)
        fun loginBack(loginRsp: JLoginRsp)
    }
    interface RemoveMemberCallBack {
        fun removeMember(jRemoveMemberRsp: JRemoveMemberRsp)
    }
    interface SaveEmailConfCallback {
        fun saveEmailConf(jSaveEmailConfRsp: JSaveEmailConfRsp)
    }
    interface CheckmailUkeyCallback {
        fun checkmailUkey(jCheckmailUkeyRsp: JCheckmailUkeyRsp)
        fun addFriendsAuto(jAddFriendsAutoRsp: JAddFriendsAutoRsp)
    }
    interface BakupEmailCallback {
        fun BakupEmailBack(jBakupEmailRsp: JBakupEmailRsp)
    }
    interface PullMailListCallback {
        fun PullMailListBack(JPullMailListRsp: JPullMailListRsp)
    }
    interface DelEmailConfCallback {
        fun DelEmailConfBack(JDelEmailConfRsp: JDelEmailConfRsp)
    }
    interface DelEmailCallback {
        fun DelEmailBack(JDelEmailRsp: JDelEmailRsp)
    }
    interface BakMailsNumCallback {
        fun BakMailsNumBack(JBakMailsNumRsp: JBakMailsNumRsp)
    }
    interface BakMailsCheckCallback {
        fun BakMailsCheckBack(JBakMailsCheckRsp: JBakMailsCheckRsp)
    }
    interface MailSendNoticeCallback {
        fun MailSendNoticeBack(JMailSendNoticeRsp: JMailSendNoticeRsp)
    }
    interface NodeFileCallback {
        fun fileAction(jFileActionRsp: JFileActionRsp)
        fun filePathsPull(jFilePathsPulRsp: JFilePathsPulRsp)
    }
    interface NodeFilesListPullCallback {
        fun filesListPull(jFilesListPullRsp: JFilesListPullRsp)
        fun fileAction(jFileActionRsp: JFileActionRsp)
    }
    interface BakAddrUserNumCallback {
        fun bakAddrUserNum(jBakAddrUserNumRsp: JBakAddrUserNumRsp)
        fun bakFileBack(jBakFileRsp: JBakFileRsp)
    }
    interface BakAddrUserNumOutCallback {
        fun bakAddrUserNum(jBakAddrUserNumRsp: JBakAddrUserNumRsp)
        fun getBakContentStatCallback(jGetBakContentStatRsp: JGetBakContentStatRsp)
    }
    interface BakContentCallback {
        fun BakContentBack(jBakContentRsp: JBakContentRsp)
    }
    interface GetBakContentStatCallback {
        fun getBakContentStatCallback(jGetBakContentStatRsp: JGetBakContentStatRsp)
    }
    interface PullBakContentCallback {
        fun pullBakContentBack(jPullBakContentRsp: JPullBakContentRsp)
        fun delBakContentBack(jDelBakContentRsp: JDelBakContentRsp)
    }

    interface WalletAccountCallback {
        fun getWalletAccountBack(jGetWalletAccountRsp: JGetWalletAccountRsp)
        fun setWalletAccountBack(jSetWalletAccountRsp: JSetWalletAccountRsp)
    }
}


