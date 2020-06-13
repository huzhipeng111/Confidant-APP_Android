package com.stratagile.pnrouter.ui.activity.main

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.gson.reflect.TypeToken
import com.pawegio.kandroid.runOnUiThread
import com.pawegio.kandroid.toast
import com.smailnet.eamil.Callback.GetCountCallback
import com.smailnet.eamil.Callback.GetGmailReceiveCallback
import com.smailnet.eamil.Callback.GetReceiveCallback1
import com.smailnet.eamil.Callback.MarkCallback
import com.smailnet.eamil.EmailCount
import com.smailnet.eamil.EmailMessage
import com.smailnet.eamil.EmailReceiveClient
import com.smailnet.eamil.MailAttachment
import com.smailnet.eamil.Utils.AESCipher
import com.smailnet.islands.Islands
import com.socks.library.KLog
import com.stratagile.pnrouter.R
import com.stratagile.pnrouter.application.AppConfig
import com.stratagile.pnrouter.base.BaseFragment
import com.stratagile.pnrouter.constant.ConstantValue
import com.stratagile.pnrouter.data.web.PNRouterServiceMessageReceiver
import com.stratagile.pnrouter.db.*
import com.stratagile.pnrouter.entity.*
import com.stratagile.pnrouter.entity.events.*
import com.stratagile.pnrouter.gmail.GmailQuickstart
import com.stratagile.pnrouter.ui.activity.email.EmailInfoActivity
import com.stratagile.pnrouter.ui.activity.email.EmailSendActivity
import com.stratagile.pnrouter.ui.activity.main.component.DaggerEmailMessageComponent
import com.stratagile.pnrouter.ui.activity.main.contract.EmailMessageContract
import com.stratagile.pnrouter.ui.activity.main.module.EmailMessageModule
import com.stratagile.pnrouter.ui.activity.main.presenter.EmailMessagePresenter
import com.stratagile.pnrouter.ui.adapter.conversation.EmaiMessageAdapter
import com.stratagile.pnrouter.utils.*
import com.stratagile.pnrouter.view.CommonDialog
import kotlinx.android.synthetic.main.email_search_bar.*
import kotlinx.android.synthetic.main.fragment_mail_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.greendao.database.Database
import java.nio.ByteBuffer
import java.util.*
import javax.inject.Inject

/**
 * @author zl
 * @Package com.stratagile.pnrouter.ui.activity.main
 * @Description: $description
 * @date 2019/07/11 16:19:12
 */

class EmailMessageFragment : BaseFragment(), EmailMessageContract.View, PNRouterServiceMessageReceiver.PullMailListCallback {
    override fun PullMailListBack(JPullMailListRsp: JPullMailListRsp) {

        runOnUiThread {
            closeProgressDialog()

            if (nodeUpandDown == "up") {
                refreshLayout.finishRefresh()
                refreshLayout.resetNoMoreData()
            } else if (nodeUpandDown == "down") {
                refreshLayout.finishLoadMore()
            }

        }
        if (JPullMailListRsp.params.retCode == 0) {
            var emailMessageEntityList = mutableListOf<EmailMessageEntity>()
            var dataList = JPullMailListRsp.params.payload
            if (dataList.size != 0) {
                lastPayload = JPullMailListRsp.params.payload.last()
            }
            for (item in dataList) {
                var userKey = item.userkey
                var aesKey = LibsodiumUtil.DecryptShareKey(userKey, ConstantValue.libsodiumpublicMiKey!!, ConstantValue.libsodiumprivateMiKey!!);
                var mailInfoStr = item.mailInfo
                var miContentSoucreBase = RxEncodeTool.base64Decode(mailInfoStr)
                val miContent = AESCipher.aesDecryptBytes(miContentSoucreBase, aesKey.toByteArray())
                var sourceContent = ""
                try {
                    sourceContent = String(miContent)
                    var gson = GsonUtil.getIntGson()
                    var mainInfo = gson.fromJson(sourceContent, EmailInfo::class.java)
                    var toUserJosnStr = mainInfo.toUserJosn
                    var toStr = ""
                    if (toUserJosnStr != null) {
                        var toUserJosn = gson.fromJson<ArrayList<EmailContact>>(toUserJosnStr, object : TypeToken<ArrayList<EmailContact>>() {

                        }.type)
                        //283619512 <283619512@qq.com>,emaildev <emaildev@qlink.mobi>

                        for (user in toUserJosn) {
                            toStr += user.userName + " " + user.userAddress + ","
                        }
                        if (toStr != "") {
                            toStr = toStr.substring(0, toStr.length - 1)
                        }
                    }

                    var ccUserJosnStr = mainInfo.ccUserJosn
                    var ccStr = ""
                    if (ccUserJosnStr != null) {

                        var ccUserJosn = gson.fromJson<ArrayList<EmailContact>>(ccUserJosnStr, object : TypeToken<ArrayList<EmailContact>>() {

                        }.type)
                        //283619512 <283619512@qq.com>,emaildev <emaildev@qlink.mobi>

                        for (user in ccUserJosn) {
                            ccStr += user.userName + " " + user.userAddress + ","
                        }
                        if (ccStr != "") {
                            ccStr = ccStr.substring(0, ccStr.length - 1)
                        }
                    }

                    var bccStr = ""
                    var bccUserJosnStr = mainInfo.bccUserJosn
                    if (bccUserJosnStr != null) {
                        var bccUserJosn = gson.fromJson<ArrayList<EmailContact>>(bccUserJosnStr, object : TypeToken<ArrayList<EmailContact>>() {

                        }.type)
                        //283619512 <283619512@qq.com>,emaildev <emaildev@qlink.mobi>

                        for (user in bccUserJosn) {
                            bccStr += user.userName + " " + user.userAddress + ","
                        }
                        if (bccStr != "") {
                            bccStr = bccStr.substring(0, bccStr.length - 1)
                        }
                    }

                    var eamilMessage = EmailMessageEntity()
                    eamilMessage.account_ = AppConfig.instance.emailConfig().account
                    eamilMessage.msgId = item.id.toString()
                    eamilMessage.menu_ = ConstantValue.chooseEmailMenuName
                    eamilMessage.from_ = mainInfo.fromName + " " + mainInfo.fromEmailBox
                    eamilMessage.to_ = toStr
                    eamilMessage.cc = ccStr
                    eamilMessage.bcc = bccStr
                    eamilMessage.setIsContainerAttachment(if (mainInfo.attchCount > 0) {
                        true
                    } else {
                        false
                    })
                    eamilMessage.setIsSeen(true)
                    eamilMessage.setIsStar(false)
                    eamilMessage.setIsReplySign(false)
                    eamilMessage.setAttachmentCount(mainInfo.attchCount)
                    eamilMessage.subject_ = mainInfo.subTitle
                    eamilMessage.content = ""
                    eamilMessage.contentText = mainInfo.content
                    eamilMessage.originalText = ""
                    eamilMessage.aesKey = aesKey
                    eamilMessage.emailAttachPath = item.emailPath
                    eamilMessage.date_ = DateUtil.getDateToString((mainInfo.revDate * 1000).toLong(), "yyyy-MM-dd HH:mm:ss");
                    emailMessageEntityList.add(eamilMessage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            runOnUiThread {
                emaiMessageChooseAdapter!!.addData(emailMessageEntityList);
                emaiMessageChooseAdapter!!.setNewData(emaiMessageChooseAdapter!!.data)
            }
        } else {

        }
    }


    @Inject
    lateinit internal var mPresenter: EmailMessagePresenter
    var emaiMessageChooseAdapter: EmaiMessageAdapter? = null
    var name = "name"
    var menu = "INBOX"
    var isChangeMenu = false
    var from = ""
    var nodeStartId = 0;
    var nodeUpandDown = "up";
    var lastPayload: JPullMailListRsp.ParamsBean.PayloadBean? = null
    var emailConfigEntityChooseList = mutableListOf<EmailConfigEntity>()
    var emailConfigEntityChoose: EmailConfigEntity? = null
    var deleteEmailMeaasgeData: EmailMessageEntity? = null
    var positionDeleteIndex = 0;
    var initSize = 50;
    var isRefreshing =false
    var isLoadingMore = false

    var currentInfoId = "-1"
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun changEmailMenu(changEmailMenu: ChangEmailMenu) {
        name = changEmailMenu.name
        menu = changEmailMenu.menu
        ConstantValue.chooseEmailMenuServer = menu
        if (menu == "star") {
            if (refreshLayout != null) {
                refreshLayout.isEnabled = false
            }

        } else {
            if (refreshLayout != null) {
                refreshLayout.isEnabled = true
            }
        }
        /*var localMessageList = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account.eq(AppConfig.instance.emailConfig().account),EmailMessageEntityDao.Properties.Menu.eq(menu)).orderDesc(EmailMessageEntityDao.Properties.TimeStamp).list()
        if(localMessageList == null || localMessageList.size ==0)
        {
            showProgressDialog()
            pullMoreMessageList(0)
        }else{
            runOnUiThread {
                emaiMessageChooseAdapter!!.setNewData(localMessageList);
            }
        }
        recyclerView.scrollToPosition(0)*/
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_mail_list, null);
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        AppConfig.instance.messageReceiver!!.pullMailListCallback = this
        emailConfigEntityChooseList = AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.queryBuilder().where(EmailConfigEntityDao.Properties.IsChoose.eq(true)).list()
        if (emailConfigEntityChooseList.size > 0) {
            emailConfigEntityChoose = emailConfigEntityChooseList.get(0)
        }
        from = arguments!!.getString("from", "")
        var account = AppConfig.instance.emailConfig().account
        var emailMessageEntityList = mutableListOf<EmailMessageEntity>()
        var emailMessageEntityList50 = mutableListOf<EmailMessageEntity>()
        if (account != null) {
//            emailMessageEntityList = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(account), EmailMessageEntityDao.Properties.Menu_.eq(menu)).orderDesc(EmailMessageEntityDao.Properties.SortId).list()
            emailMessageEntityList = EmailUtils.loadLocalEmail(account, menu)
            KLog.i("邮件的数量为：" + emailMessageEntityList.size)
            if (emailMessageEntityList.size > initSize) {
                for (index in 0 until initSize) {
                    if (emailMessageEntityList.get(index).content == null) {
                        AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.delete(emailMessageEntityList.get(index))
                    } else {
                        emailMessageEntityList50.add(index, emailMessageEntityList.get(index))
                    }
                }
            } else {
                var iterator = emailMessageEntityList.iterator()
                while (iterator.hasNext()) {
                    var message = iterator.next()
                    if (message.content == null) {
                        AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.delete(message)
                        iterator.remove()
                    }
                }
                emailMessageEntityList50 = emailMessageEntityList;
            }
        }
        emaiMessageChooseAdapter = EmaiMessageAdapter(emailMessageEntityList50)
        emaiMessageChooseAdapter!!.setEmptyView(R.layout.layout_email_empty, recyclerView)
        emaiMessageChooseAdapter!!.setOnItemLongClickListener { adapter, view, position ->
            if (name == "Drafts") {
                deleteEmailMeaasgeData = emaiMessageChooseAdapter!!.getItem(position)
                positionDeleteIndex = position
                val commonDialog = CommonDialog(activity)
                val view1 = activity!!.layoutInflater.inflate(R.layout.dialog_conversation_layout, null, false)
                commonDialog.setView(view1)
                commonDialog.show()
                val tvDelete = view1.findViewById<TextView>(R.id.tvDelete)
                tvDelete.setOnClickListener {
                    showProgressDialog(AppConfig.instance.resources.getString(R.string.waiting))
                    deleteAndMoveEmailSend(ConstantValue.currentEmailConfigEntity!!.deleteMenu, 2)
                    commonDialog.cancel()
                }
            }

            /*   val floatMenu = FloatMenu(activity)
              floatMenu.inflate(R.menu.popup_menu_voice)
               var left = view.left
               var top = view.top
               var point = Point(left,top)
               floatMenu.show(point,0,0)*/
            true
        }
        recyclerView.adapter = emaiMessageChooseAdapter
        recyclerView.scrollToPosition(0)
        emaiMessageChooseAdapter!!.setOnItemClickListener { adapter, view, position ->
            var emailMeaasgeData = emaiMessageChooseAdapter!!.getItem(position)
            if (name == "Drafts") {
                var intent = Intent(activity!!, EmailSendActivity::class.java)
                AppConfig.instance.emailSendoMessageEntity = emailMeaasgeData
//                intent.putExtra("emailMeaasgeInfoData", emailMeaasgeData)
                intent.putExtra("foward", 3)
                intent.putExtra("flag", 1)
                intent.putExtra("menu", menu)
                intent.putExtra("attach", 1)
                intent.putExtra("positionIndex", position)
                startActivity(intent)
            } else {
                if (emailMeaasgeData!!.content == null) {
                    currentInfoId = emailMeaasgeData.msgId
                }
                AppConfig.instance.emailInfoMessageEntity = emailMeaasgeData
                var intent = Intent(activity!!, EmailInfoActivity::class.java)
//                intent.putExtra("emailMeaasgeData", emailMeaasgeData)
                intent.putExtra("menu", menu)
                intent.putExtra("positionIndex", position)
                startActivity(intent)
            }
            if (name != "Nodebackedup") {

                if (emailMeaasgeData!!.content == null) {
                    emailMeaasgeData!!.setIsSeen(true)
                } else {
                    emailMeaasgeData!!.setIsSeen(true)
                    AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.update(emailMeaasgeData)
                }
                emaiMessageChooseAdapter!!.notifyItemChanged(position)
            }

        }
        KLog.i("设置adapter完成。。。")
        /* refreshLayout.setOnRefreshListener {
             pullMoreMessageList()
             if (refreshLayout != null)
                 refreshLayout.isRefreshing = false
         }*/
        if (from != null && from != "") {
            if (refreshLayout != null) {
                refreshLayout.isEnabled = false
            }

        } else {
            if (refreshLayout != null) {
                refreshLayout.isEnabled = true
            }

            refreshLayout.setEnableAutoLoadMore(false)//开启自动加载功能（非必须）
            refreshLayout.setOnRefreshListener { refreshLayout ->
                if (isRefreshing) {
                    refreshLayout.finishRefresh()
                    return@setOnRefreshListener
                }
                var account = AppConfig.instance.emailConfig().account
                if (account != null && account != "") {
                    if (menu == "node") {
                        nodeUpandDown = "up";
                        if (lastPayload == null) {
                            nodeStartId = 0;
                        } else {
                            nodeStartId = lastPayload!!.id
                        }
                        var type = AppConfig.instance.emailConfig().emailType.toInt()
                        var accountBase64 = String(RxEncodeTool.base64Encode(AppConfig.instance.emailConfig().account))
                        var pullMailList = PullMailList(type, accountBase64, nodeStartId, 20)
                        AppConfig.instance.getPNRouterServiceMessageSender().send(BaseData(6, pullMailList))
                    } else {
//                        var localMessageList = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(AppConfig.instance.emailConfig().account), EmailMessageEntityDao.Properties.Menu_.eq(menu)).orderDesc(EmailMessageEntityDao.Properties.SortId).list()
                        var localMessageList = EmailUtils.loadLocalEmail(account, menu)
                        if (localMessageList.size == 0) {
                            if (ConstantValue.currentEmailConfigEntity!!.userId == null || ConstantValue.currentEmailConfigEntity!!.userId == "") {
                                pullMoreMessageList(0, true)
                            } else {
                                pullMoreGmailMessageList("", true)
                            }
                        } else {
                            if (ConstantValue.currentEmailConfigEntity!!.userId == null || ConstantValue.currentEmailConfigEntity!!.userId == "") {
                                pullNewMessageList(0L)
                            } else {
                                pullNewGmailMessageList(0L)
                            }
                        }
                    }

                } else {
                    refreshLayout.finishRefresh()
                    refreshLayout.resetNoMoreData()
                }


            }
            refreshLayout.setOnLoadMoreListener { refreshLayout ->
                if (isLoadingMore) {
                    refreshLayout.finishLoadMore()
                    return@setOnLoadMoreListener
                }
                var account = AppConfig.instance.emailConfig().account
                if (account != null && account != "") {
                    if (menu == "node") {
                        refreshLayout.finishLoadMore()
                        /*  nodeUpandDown = "down";
                          if(lastPayload == null)
                          {pupu
                              nodeStartId = 0;
                          }else{
                              nodeStartId = lastPayload!!.id
                          }
                          var type = AppConfig.instance.emailConfig().emailType.toInt()
                          var accountBase64 = String(RxEncodeTool.base64Encode(AppConfig.instance.emailConfig().account))
                          var pullMailList = PullMailList(type ,accountBase64,nodeStartId, 20)
                          AppConfig.instance.getPNRouterServiceMessageSender().send(BaseData(6,pullMailList))*/
                    } else {
//                        var localMessageList = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(AppConfig.instance.emailConfig().account), EmailMessageEntityDao.Properties.Menu_.eq(menu)).orderDesc(EmailMessageEntityDao.Properties.SortId).list()
                        var localMessageList = EmailUtils.loadLocalEmail(AppConfig.instance.emailConfig().account, menu)
                        if (ConstantValue.currentEmailConfigEntity!!.userId == null || ConstantValue.currentEmailConfigEntity!!.userId == "") {
                            pullMoreMessageList(if (localMessageList != null) {
                                localMessageList.size
                            } else {
                                0
                            })
                        } else {
                            var pageToken = ""
                            var emailConfigEntityChooseList = AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.queryBuilder().where(EmailConfigEntityDao.Properties.IsChoose.eq(true)).list()
                            if (emailConfigEntityChooseList.size > 0) {
                                var emailConfigEntityChoose = emailConfigEntityChooseList.get(0)
                                if (emailConfigEntityChoose.pageToken != null) {
                                    pageToken = emailConfigEntityChoose.pageToken
                                }

                            }
                            pullMoreGmailMessageList(pageToken)
                        }
                    }

                } else {
                    refreshLayout.finishLoadMore()
                }
                /* refreshLayout.layout.postDelayed({

                     *//*if (mAdapter.getItemCount() > 30) {
                    Toast.makeText(AppConfig.instance, "数据全部加载完毕", Toast.LENGTH_SHORT).show()
                    refreshLayout.finishLoadMoreWithNoMoreData()//将不会再次触发加载更多事件
                } else {
                    pullMoreMessageList()
                    refreshLayout.finishLoadMore()
                }*//*

            }, 2000)*/


            }
        }


        //触发自动刷新
        //refreshLayout.autoRefresh()
        EventBus.getDefault().register(this)
        initQuerData()
        if (from != null && from != "") {
            shouUI(true)
        }
        KLog.i("initdata完成。。")
    }

    fun deleteAndMoveEmailSend(menuTo: String, flag: Int) {
        /*tipDialog.show()*/
        val emailReceiveClient = EmailReceiveClient(AppConfig.instance.emailConfig())
        emailReceiveClient
                .imapMarkEmail(activity, object : MarkCallback {
                    override fun gainSuccess(result: Boolean) {
                        //tipDialog.dismiss()
                        closeProgressDialog()
                        if (result) {
                            deleteEmail()
                        } else {
                            Toast.makeText(activity, getString(R.string.fail), Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun gainFailure(errorMsg: String) {
                        //tipDialog.dismiss()
                        closeProgressDialog()
                        Toast.makeText(activity, getString(R.string.fail), Toast.LENGTH_SHORT).show()
                    }
                }, menu, deleteEmailMeaasgeData!!.msgId, flag, true, menuTo)
    }

    fun deleteEmail() {
        AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.delete(deleteEmailMeaasgeData)
        EventBus.getDefault().post(ChangEmailMessage(positionDeleteIndex, 1))
        if (emailConfigEntityChoose != null) {
            when (menu) {
                ConstantValue.currentEmailConfigEntity!!.inboxMenu -> {
                    emailConfigEntityChoose!!.totalCount -= 1

                }
                ConstantValue.currentEmailConfigEntity!!.drafMenu -> {
                    emailConfigEntityChoose!!.drafTotalCount -= 1
                }
                ConstantValue.currentEmailConfigEntity!!.sendMenu -> {
                    emailConfigEntityChoose!!.sendTotalCount -= 1
                }
                ConstantValue.currentEmailConfigEntity!!.garbageMenu -> {
                    emailConfigEntityChoose!!.garbageCount -= 1
                }
                ConstantValue.currentEmailConfigEntity!!.deleteMenu -> {
                    emailConfigEntityChoose!!.deleteTotalCount -= 1
                }
            }
            AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.update(emailConfigEntityChoose)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun changEmailMessage(changEmailMessage: ChangEmailMessage) {
        if (changEmailMessage.type == 0) {
            var emailMeaasgeData = emaiMessageChooseAdapter!!.getItem(changEmailMessage.positon)
            emailMeaasgeData!!.setIsSeen(false)
            emaiMessageChooseAdapter!!.notifyItemChanged(changEmailMessage.positon)
        } else {
            emaiMessageChooseAdapter!!.remove(changEmailMessage.positon)
            emaiMessageChooseAdapter!!.notifyDataSetChanged()
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun changEmailStar(changEmailStar: ChangEmailStar) {
        var emailMeaasgeData = emaiMessageChooseAdapter!!.getItem(changEmailStar.positon)
        if (name == "Starred") {
            if (changEmailStar.type == 0) {
                emaiMessageChooseAdapter!!.remove(changEmailStar.positon)
                emaiMessageChooseAdapter!!.notifyDataSetChanged()
            }
        } else {
            if (changEmailStar.type == 0) {
                emailMeaasgeData!!.setIsStar(false)
            } else {
                emailMeaasgeData!!.setIsStar(true)
            }

            emaiMessageChooseAdapter!!.notifyItemChanged(changEmailStar.positon)
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSendEmailSuccess(sendEmailSuccess: SendEmailSuccess) {
        if (name == "Sent") {
            var emailConfigEntityChooseList = AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.queryBuilder().where(EmailConfigEntityDao.Properties.IsChoose.eq(true)).list()
            if (emailConfigEntityChooseList.size > 0) {
                var emailConfigEntityChoose = emailConfigEntityChooseList.get(0)
                if (emailConfigEntityChoose.sendMenuRefresh) {
//                    var localMessageList = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(AppConfig.instance.emailConfig().account), EmailMessageEntityDao.Properties.Menu_.eq(menu)).orderDesc(EmailMessageEntityDao.Properties.SortId).list()
                    var localMessageList = EmailUtils.loadLocalEmail(AppConfig.instance.emailConfig().account, menu)
                    if (localMessageList == null || localMessageList.size == 0) {
                        showProgressDialog()
                        if (ConstantValue.currentEmailConfigEntity!!.userId == null || ConstantValue.currentEmailConfigEntity!!.userId == "") {
                            pullMoreMessageList(0)
                        } else {
                            pullMoreGmailMessageList("")
                        }
                    } else {
                        showProgressDialog()
                        if (ConstantValue.currentEmailConfigEntity!!.userId == null || ConstantValue.currentEmailConfigEntity!!.userId == "") {
                            pullNewMessageList(0L)
                        } else {
                            pullNewGmailMessageList(0L)
                        }
                    }
                } else {
//                    var localMessageList = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(AppConfig.instance.emailConfig().account), EmailMessageEntityDao.Properties.Menu_.eq(menu)).orderDesc(EmailMessageEntityDao.Properties.SortId).list()
                    var localMessageList = EmailUtils.loadLocalEmail(AppConfig.instance.emailConfig().account, menu)
                    if (localMessageList == null || localMessageList.size == 0) {
                        showProgressDialog()
                        if (ConstantValue.currentEmailConfigEntity!!.userId == null || ConstantValue.currentEmailConfigEntity!!.userId == "") {
                            pullMoreMessageList(0)
                        } else {
                            pullMoreGmailMessageList("")
                        }
                    } else {
                        runOnUiThread {
                            emaiMessageChooseAdapter!!.setNewData(localMessageList);
                        }
                    }
                }
            }
        } else if (name == "Drafts") {
            var emailMeaasgeData = emaiMessageChooseAdapter!!.getItem(sendEmailSuccess.positon)
            AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.delete(emailMeaasgeData)
            emaiMessageChooseAdapter!!.remove(sendEmailSuccess.positon)
            emaiMessageChooseAdapter!!.notifyDataSetChanged()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDrawerOpened(onDrawerOpened: OnDrawerOpened) {
        var localMessageList = mutableListOf<EmailMessageEntity>()
        runOnUiThread {
            emaiMessageChooseAdapter!!.setNewData(localMessageList);
        }
        getMailUnReadCount()
        if (menu.equals("star")) {
//            var localMessageList = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(AppConfig.instance.emailConfig().account), EmailMessageEntityDao.Properties.IsStar.eq(true)).orderDesc(EmailMessageEntityDao.Properties.SortId).list()
            var localMessageList = EmailUtils.loadLocalEmail(AppConfig.instance.emailConfig().account, menu)
            runOnUiThread {
                emaiMessageChooseAdapter!!.setNewData(localMessageList);
            }
            return;
        } else if (menu.equals("node")) {
            showProgressDialog()
            /*if(lastPayload == null)
            {
                nodeStartId = 0;
            }else{
                nodeStartId = lastPayload!!.id
            }*/
            nodeUpandDown = ""
            nodeStartId = 0;
            var type = AppConfig.instance.emailConfig().emailType.toInt()
            var accountBase64 = String(RxEncodeTool.base64Encode(AppConfig.instance.emailConfig().account))
            var pullMailList = PullMailList(type, accountBase64, nodeStartId, 20)
            AppConfig.instance.getPNRouterServiceMessageSender().send(BaseData(6, pullMailList))
            return;
        } else if (menu.equals("star") || menu.equals("")) {
            return;
        }
        if (AppConfig.instance.emailConfig().account != null && !AppConfig.instance.emailConfig().account.equals("")) {
            if (name == "Sent") {
                var emailConfigEntityChooseList = AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.queryBuilder().where(EmailConfigEntityDao.Properties.IsChoose.eq(true)).list()
                if (emailConfigEntityChooseList.size > 0) {
                    var emailConfigEntityChoose = emailConfigEntityChooseList.get(0)
                    if (emailConfigEntityChoose.sendMenuRefresh) {
//                        var localMessageList = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(AppConfig.instance.emailConfig().account), EmailMessageEntityDao.Properties.Menu_.eq(menu)).orderDesc(EmailMessageEntityDao.Properties.SortId).list()
                        var localMessageList = EmailUtils.loadLocalEmail(AppConfig.instance.emailConfig().account, menu)
                        if (localMessageList == null || localMessageList.size == 0) {
                            showProgressDialog()
                            if (ConstantValue.currentEmailConfigEntity!!.userId == null || ConstantValue.currentEmailConfigEntity!!.userId == "") {
                                pullMoreMessageList(0)
                            } else {
                                pullMoreGmailMessageList("")
                            }
                        } else {
                            showProgressDialog()
                            if (ConstantValue.currentEmailConfigEntity!!.userId == null || ConstantValue.currentEmailConfigEntity!!.userId == "") {
                                pullNewMessageList(0L)
                            } else {
                                pullNewGmailMessageList(0L)
                            }
                        }
                    } else {
//                        var localMessageList = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(AppConfig.instance.emailConfig().account), EmailMessageEntityDao.Properties.Menu_.eq(menu)).orderDesc(EmailMessageEntityDao.Properties.SortId).list()
                        var localMessageList = EmailUtils.loadLocalEmail(AppConfig.instance.emailConfig().account, menu)
                        if (localMessageList == null || localMessageList.size == 0) {
                            showProgressDialog()
                            if (ConstantValue.currentEmailConfigEntity!!.userId == null || ConstantValue.currentEmailConfigEntity!!.userId == "") {
                                pullMoreMessageList(0)
                            } else {
                                pullMoreGmailMessageList("")
                            }
                        } else {
                            var emailMessageEntityList50 = mutableListOf<EmailMessageEntity>()
                            if (localMessageList.size > initSize) {
                                for (index in 0 until initSize) {
                                    emailMessageEntityList50.add(index, localMessageList.get(index))
                                }
                            } else {
                                emailMessageEntityList50 = localMessageList;
                            }
                            runOnUiThread {
                                emaiMessageChooseAdapter!!.setNewData(emailMessageEntityList50);
                            }
                        }
                    }
                }
            } else {
//                var localMessageList = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(AppConfig.instance.emailConfig().account), EmailMessageEntityDao.Properties.Menu_.eq(menu)).orderDesc(EmailMessageEntityDao.Properties.SortId).list()
                var localMessageList = EmailUtils.loadLocalEmail(AppConfig.instance.emailConfig().account, menu)
                if (localMessageList == null || localMessageList.size == 0) {
                    showProgressDialog()
                    if (ConstantValue.currentEmailConfigEntity!!.userId == null || ConstantValue.currentEmailConfigEntity!!.userId == "") {
                        if (ConstantValue.currentEmailConfigEntity!!.userId == null || ConstantValue.currentEmailConfigEntity!!.userId == "") {
                            pullMoreMessageList(0)
                        } else {
                            pullMoreGmailMessageList("")
                        }
                    } else {
                        pullMoreGmailMessageList("")
                    }
                } else {
                    var emailMessageEntityList50 = mutableListOf<EmailMessageEntity>()
                    if (localMessageList.size > initSize) {
                        for (index in 0 until initSize) {
                            emailMessageEntityList50.add(index, localMessageList.get(index))
                        }
                    } else {
                        emailMessageEntityList50 = localMessageList;
                    }
                    runOnUiThread {
                        emaiMessageChooseAdapter!!.setNewData(emailMessageEntityList50);
                    }
                }
            }

        }
    }

    fun getMailUnReadCount() {
        var menuList = arrayListOf<String>(ConstantValue.currentEmailConfigEntity!!.inboxMenu, ConstantValue.currentEmailConfigEntity!!.drafMenu, ConstantValue.currentEmailConfigEntity!!.sendMenu, ConstantValue.currentEmailConfigEntity!!.garbageMenu, ConstantValue.currentEmailConfigEntity!!.deleteMenu)
        if (ConstantValue.currentEmailConfigEntity!!.userId == null || ConstantValue.currentEmailConfigEntity!!.userId == "") {
            Islands.circularProgress(AppConfig.instance)
                    .setCancelable(false)
                    .setMessage(getString(R.string.waiting))
                    .run { progressDialog ->
                        val emailReceiveClient = EmailReceiveClient(AppConfig.instance.emailConfig())
                        emailReceiveClient
                                .imapReceiveAsynCount(activity, object : GetCountCallback {
                                    override fun gainSuccess(messageList: List<EmailCount>, count: Int) {
                                        progressDialog.dismiss()
                                        if (messageList.size > 0) {
                                            var emailMessage = messageList.get(0)
                                            var emailConfigEntityList = AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.queryBuilder().where(EmailConfigEntityDao.Properties.Account.eq(ConstantValue.currentEmailConfigEntity!!.account)).list()
                                            var EmailMessage = false
                                            if (emailConfigEntityList.size > 0) {
                                                var emailConfigEntity: EmailConfigEntity = emailConfigEntityList.get(0);
                                                emailConfigEntity.totalCount = emailMessage.totalCount     //Inbox消息总数
                                                emailConfigEntity.unReadCount = emailMessage.unReadCount    //Inbox未读数量
                                                emailConfigEntity.starTotalCount = emailMessage.starTotalCount       //star消息总数
                                                emailConfigEntity.starunReadCount = emailMessage.starunReadCount       //star未读数量
                                                emailConfigEntity.drafTotalCount = emailMessage.drafTotalCount       //draf消息总数
                                                emailConfigEntity.drafUnReadCount = emailMessage.drafUnReadCount       //draf未读数量
                                                emailConfigEntity.sendTotalCount = emailMessage.sendTotalCount       //send消息总数
                                                emailConfigEntity.sendunReadCount = emailMessage.sendunReadCount       //send未读数量
                                                emailConfigEntity.garbageCount = emailMessage.garbageCount         //garbage未读邮件总数
                                                emailConfigEntity.garbageUnReadCount = emailMessage.garbageUnReadCount       //garbage未读数量
                                                emailConfigEntity.deleteTotalCount = emailMessage.deleteTotalCount       //delete消息总数
                                                emailConfigEntity.deleteUnReadCount = emailMessage.deleteUnReadCount       //delete未读数量
                                                ConstantValue.currentEmailConfigEntity = emailConfigEntity;
                                                AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.update(emailConfigEntity)
                                            }
                                        }
                                        EventBus.getDefault().post(ChangeEmailConfig())
                                    }

                                    override fun gainFailure(errorMsg: String) {
                                        progressDialog.dismiss()
                                        //Toast.makeText(AppConfig.instance, "IMAP邮件收取失败", Toast.LENGTH_SHORT).show()
                                    }
                                }, menuList)
                    }
        } else {
            var gmailService = GmailQuickstart.getGmailService(AppConfig.instance, ConstantValue.currentEmailConfigEntity!!.account);
            Islands.circularProgress(AppConfig.instance)
                    .setCancelable(false)
                    .setMessage(getString(R.string.waiting))
                    .run { progressDialog ->
                        val emailReceiveClient = EmailReceiveClient(AppConfig.instance.emailConfig())
                        emailReceiveClient
                                .gmaiApiAsynCount(activity, object : GetCountCallback {
                                    override fun gainSuccess(messageList: List<EmailCount>, count: Int) {
                                        if (messageList.size > 0) {
                                            var emailMessage = messageList.get(0)
                                            var emailConfigEntityList = AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.queryBuilder().where(EmailConfigEntityDao.Properties.Account.eq(ConstantValue.currentEmailConfigEntity!!.account)).list()
                                            var EmailMessage = false
                                            if (emailConfigEntityList.size > 0) {
                                                var emailConfigEntity: EmailConfigEntity = emailConfigEntityList.get(0);
                                                emailConfigEntity.totalCount = emailMessage.totalCount     //Inbox消息总数
                                                emailConfigEntity.unReadCount = emailMessage.unReadCount    //Inbox未读数量
                                                emailConfigEntity.starTotalCount = emailMessage.starTotalCount       //star消息总数
                                                emailConfigEntity.starunReadCount = emailMessage.starunReadCount       //star未读数量
                                                emailConfigEntity.drafTotalCount = emailMessage.drafTotalCount       //draf消息总数
                                                emailConfigEntity.drafUnReadCount = emailMessage.drafUnReadCount       //draf未读数量
                                                emailConfigEntity.sendTotalCount = emailMessage.sendTotalCount       //send消息总数
                                                emailConfigEntity.sendunReadCount = emailMessage.sendunReadCount       //send未读数量
                                                emailConfigEntity.garbageCount = emailMessage.garbageCount         //garbage未读邮件总数
                                                emailConfigEntity.garbageUnReadCount = emailMessage.garbageUnReadCount       //garbage未读数量
                                                emailConfigEntity.deleteTotalCount = emailMessage.deleteTotalCount       //delete消息总数
                                                emailConfigEntity.deleteUnReadCount = emailMessage.deleteUnReadCount       //delete未读数量

                                                emailConfigEntity.inboxMaxMessageId = emailMessage.inboxMaxMessageId
                                                emailConfigEntity.inboxMinMessageId = emailMessage.inboxMinMessageId
                                                emailConfigEntity.nodeMaxMessageId = emailMessage.nodeMaxMessageId
                                                emailConfigEntity.nodeMinMessageId = emailMessage.nodeMinMessageId

                                                emailConfigEntity.starMaxMessageId = emailMessage.starMaxMessageId
                                                emailConfigEntity.starMinMessageId = emailMessage.starMinMessageId
                                                emailConfigEntity.drafMaxMessageId = emailMessage.drafMaxMessageId
                                                emailConfigEntity.drafMinMessageId = emailMessage.drafMinMessageId
                                                emailConfigEntity.sendMaxMessageId = emailMessage.sendMaxMessageId
                                                emailConfigEntity.sendMinMessageId = emailMessage.sendMinMessageId
                                                emailConfigEntity.garbageMaxMessageId = emailMessage.garbageMaxMessageId
                                                emailConfigEntity.garbageMinMessageId = emailMessage.garbageMinMessageId
                                                emailConfigEntity.deleteMaxMessageId = emailMessage.deleteMaxMessageId
                                                emailConfigEntity.deleteMinMessageId = emailMessage.deleteMinMessageId

                                                ConstantValue.currentEmailConfigEntity = emailConfigEntity;
                                                AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.update(emailConfigEntity)
                                            }
                                            EventBus.getDefault().post(ChangeEmailConfig())
                                        } else {
                                            progressDialog.dismiss()
                                        }

                                    }

                                    override fun gainFailure(errorMsg: String) {
                                        progressDialog.dismiss()
                                        //Toast.makeText(AppConfig.instance, "IMAP邮件收取失败", Toast.LENGTH_SHORT).show()
                                    }
                                }, menuList, gmailService, "me")
                    }
        }

    }

    override fun onResume() {
        super.onResume()

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            KLog.i("设置Email")
            EventBus.getDefault().post(ChangFragmentMenu("Email"))


        }
    }

    override fun setupFragmentComponent() {
        DaggerEmailMessageComponent
                .builder()
                .appComponent((activity!!.application as AppConfig).applicationComponent)
                .emailMessageModule(EmailMessageModule(this))
                .build()
                .inject(this)
    }

    override fun setPresenter(presenter: EmailMessageContract.EmailMessageContractPresenter) {
        mPresenter = presenter as EmailMessagePresenter
    }

    //配置完邮箱，第一次拉邮件，拉新邮件
    fun pullNewMessageList(localSize: Long) {
        var root_ = this.activity
        var account = AppConfig.instance.emailConfig().account
        var smtpHost = AppConfig.instance.emailConfig().smtpHost
        Log.i("pullMoreMessageList", account + ":" + smtpHost)

        var emailConfigEntityChoose = AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.queryBuilder().where(EmailConfigEntityDao.Properties.IsChoose.eq(true)).list()
        //var lastTotalCount = 0L;
        var minUUID = 0L
        var maxUUID = 0L
        if (emailConfigEntityChoose.size > 0) {
            var emailConfigEntity: EmailConfigEntity = emailConfigEntityChoose.get(0);
            when (menu) {
                /* emailConfigEntity.inboxMenu->
                 {
                     lastTotalCount = emailConfigEntity.totalCount
                 }
                 emailConfigEntity.drafMenu->
                 {
                     lastTotalCount = emailConfigEntity.drafTotalCount
                 }
                 emailConfigEntity.sendMenu->
                 {
                     lastTotalCount = emailConfigEntity.sendTotalCount
                 }
                 emailConfigEntity.garbageMenu->
                 {
                     lastTotalCount = emailConfigEntity.garbageCount
                 }
                 emailConfigEntity.deleteMenu->
                 {
                     lastTotalCount = emailConfigEntity.deleteTotalCount
                 }*/
                emailConfigEntity.inboxMenu -> {
                    minUUID = emailConfigEntity.inboxMinMessageId
                    maxUUID = emailConfigEntity.inboxMaxMessageId
                }
                emailConfigEntity.drafMenu -> {
                    minUUID = emailConfigEntity.drafMinMessageId
                    maxUUID = emailConfigEntity.drafMaxMessageId
                }
                emailConfigEntity.sendMenu -> {
                    minUUID = emailConfigEntity.sendMinMessageId
                    maxUUID = emailConfigEntity.sendMaxMessageId
                }
                emailConfigEntity.garbageMenu -> {
                    minUUID = emailConfigEntity.garbageMinMessageId
                    maxUUID = emailConfigEntity.garbageMaxMessageId
                }
                emailConfigEntity.deleteMenu -> {
                    minUUID = emailConfigEntity.deleteMinMessageId
                    maxUUID = emailConfigEntity.deleteMaxMessageId
                }
            }
        }
        var firstMessageEntity = emaiMessageChooseAdapter!!.getItem(0)
        var lastMessageEntity = emaiMessageChooseAdapter!!.getItem(emaiMessageChooseAdapter!!.data.size - 1)
        if (firstMessageEntity != null) {
            minUUID = lastMessageEntity!!.msgId.toLong();
            maxUUID = firstMessageEntity!!.msgId.toLong();
        } else {
            minUUID = 0L;
            maxUUID = 0L;
        }
        var account23 = AppConfig.instance.emailConfig().account
        var emailMessageEntityList = mutableListOf<EmailMessageEntity>()
        if (account23 != null) {
            Log.e("initDataEmail", account23)
//            emailMessageEntityList = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(account23), EmailMessageEntityDao.Properties.Menu_.eq(menu)).orderDesc(EmailMessageEntityDao.Properties.SortId).list()
            emailMessageEntityList = EmailUtils.loadLocalEmail(account23, menu)
            Log.e("initDataEmail", emailMessageEntityList.size.toString())
            if (emailMessageEntityList.size > 0) {

                var localEmailMessageEntity: EmailMessageEntity = emailMessageEntityList.get(0)
                if (localEmailMessageEntity != null) {
                    maxUUID = localEmailMessageEntity.sortId;
                    Log.e("initDataEmail", maxUUID.toString())
                    LogUtil.addLogEmail("maxUUID:" + maxUUID + "  &&&  minUUID:" + minUUID, "EmailMessageFragment")
                }
            }
        }
        // var verifyList = AppConfig.instance.mDaoMaster!!.newSession().groupVerifyEntityDao.queryBuilder().where(GroupVerifyEntityDao.Properties.Aduit.eq(selfUserId)).list()
        var pageSize = 150
        if (emaiMessageChooseAdapter!!.data.size == 0) {
            pageSize = 10
        }
        if (true) {
            isRefreshing = true
            //LogUtil.logList.clear()
            //LogUtil.addLogEmail("1_minUUID:"+minUUID+"  &&&  maxUUID"+maxUUID,"EmailMessageFragment");
            var beginIndex = localSize
            /*  AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.deleteAll()
              AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.deleteAll()*/
            Islands.circularProgress(this.activity)
                    .setCancelable(false)
                    .setMessage("同步中...")
                    .run { progressDialog ->
                        val emailReceiveClient = EmailReceiveClient(AppConfig.instance.emailConfig())
                        emailReceiveClient
                                .imapReceiveNewAsyn(this.activity, object : GetReceiveCallback1 {
                                    var localEmailMessageNew = mutableListOf<EmailMessageEntity>()
                                    var beginIndex1 = emaiMessageChooseAdapter!!.data.size
                                    override fun gainPreSuccess(messageList: MutableList<EmailMessage>, totalCount: Long, maxUUID: Long, noMoreData: Boolean, error: String, menuFlag: String) {
                                        //toast(R.string.No_mail)
                                        var flag = 0;



                                        KLog.i("解析邮件，第一次的数量为：" + messageList.size)
                                        for (item in messageList) {
                                            KLog.i("解析邮件：" + item.id)
                                            if (item.id == null) {
                                                continue
                                            }
                                            var emailConfigEntityChoose = AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.queryBuilder().where(EmailConfigEntityDao.Properties.IsChoose.eq(true)).list()
                                            if (emailConfigEntityChoose.size > 0) {
                                                var emailConfigEntity: EmailConfigEntity = emailConfigEntityChoose.get(0);
                                                when (menu) {
                                                    emailConfigEntity.inboxMenu -> {
                                                        if (emailConfigEntity.inboxMaxMessageId == 0L) {
                                                            emailConfigEntity.totalCount = minUUID.toInt()
                                                            emailConfigEntity.inboxMaxMessageId = item.id.toLong()
                                                            emailConfigEntity.inboxMinMessageId = item.id.toLong()
                                                        }

                                                    }
                                                }
                                                AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.update(emailConfigEntity)
                                            }
//                                            var localEmailMessage = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(account), EmailMessageEntityDao.Properties.Menu_.eq(menu), EmailMessageEntityDao.Properties.MsgId.eq(item.id)).list()
                                            var localEmailMessage = EmailUtils.loadLocalEmailWithMsgId(account, menu, item.id)
                                            var name = ""
                                            var account = ""
                                            if (localEmailMessage == null || localEmailMessage.size == 0) {
                                                var eamilMessage = EmailMessageEntity()
                                                eamilMessage.account_ = AppConfig.instance.emailConfig().account
                                                eamilMessage.msgId = item.id
                                                eamilMessage.sortId = item.id.toLong()
                                                eamilMessage.menu_ = menuFlag
                                                eamilMessage.from_ = item.from
                                                eamilMessage.to_ = item.to
                                                eamilMessage.cc = item.cc
                                                eamilMessage.bcc = item.bcc
                                                eamilMessage.date_ = item.date

                                                eamilMessage.subject_ = item.subject

                                                if (eamilMessage.from_.indexOf("<") >= 0) {
                                                    name = eamilMessage.from_.substring(0, eamilMessage.from_.indexOf("<"))
                                                    account = eamilMessage.from_.substring(eamilMessage.from_.indexOf("<") + 1, eamilMessage.from_.length - 1)
                                                } else {
                                                    name = eamilMessage.from_.substring(0, eamilMessage.from_.indexOf("@"))
                                                    account = eamilMessage.from_.substring(0, eamilMessage.from_.length)
                                                }
                                                name = name.replace("\"", "")
                                                name = name.replace("\"", "")

                                                localEmailMessageNew.add(flag, eamilMessage)
                                            } else {
                                                continue
                                            }
                                            flag++
                                        }
                                        runOnUiThread {
                                            KLog.i("解析邮件，第一次添加的数量为：" + localEmailMessageNew.size)
                                            localEmailMessageNew.sortByDescending { it.msgId.toInt() }
                                            emaiMessageChooseAdapter!!.addData(0, localEmailMessageNew)
                                            progressDialog.dismiss()
                                        }
                                        runOnUiThread {
                                            closeProgressDialog()
                                            refreshLayout.finishRefresh()
                                            recyclerView.scrollToPosition(0)
                                        }
                                    }

                                    override fun gainSuccess(messageList: List<EmailMessage>, totalCount: Long, maxUUID: Long, noMoreData: Boolean, errorMs: String, menuFlag: String) {
                                        isRefreshing = false
                                        if (noMoreData) {
                                            runOnUiThread {
                                                closeProgressDialog()
                                                refreshLayout.finishRefresh()
                                                //toast(R.string.No_mail)
                                                //refreshLayout.finishLoadMoreWithNoMoreData()//将不会再次触发加载更多事件
                                            }
                                        } else {
                                            runOnUiThread {
                                                closeProgressDialog()
                                                refreshLayout.finishRefresh()
                                            }
                                        }
                                        if (messageList.size == 0) {
                                            refreshLayout.finishRefresh()
                                            return
                                        }
                                        var emailConfigEntityChoose = AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.queryBuilder().where(EmailConfigEntityDao.Properties.IsChoose.eq(true)).list()
                                        if (emailConfigEntityChoose.size > 0) {
                                            var emailConfigEntity: EmailConfigEntity = emailConfigEntityChoose.get(0);
                                            when (menu) {
                                                emailConfigEntity.inboxMenu -> {
                                                    emailConfigEntity.totalCount += messageList.size
                                                    emailConfigEntity.inboxMaxMessageId = maxUUID
                                                    emailConfigEntity.inboxMenuRefresh = false
                                                }
                                                emailConfigEntity.drafMenu -> {
                                                    emailConfigEntity.drafTotalCount += messageList.size
                                                    emailConfigEntity.drafMaxMessageId = maxUUID
                                                    emailConfigEntity.drafMenuRefresh = false
                                                }
                                                emailConfigEntity.sendMenu -> {
                                                    emailConfigEntity.sendTotalCount += messageList.size
                                                    emailConfigEntity.sendMaxMessageId = maxUUID
                                                    emailConfigEntity.sendMenuRefresh = false
                                                }
                                                emailConfigEntity.garbageMenu -> {
                                                    emailConfigEntity.garbageCount += messageList.size
                                                    emailConfigEntity.garbageMaxMessageId = maxUUID
                                                    emailConfigEntity.garbageMenuRefresh = false
                                                }
                                                emailConfigEntity.deleteMenu -> {
                                                    emailConfigEntity.deleteTotalCount += messageList.size
                                                    emailConfigEntity.deleteMaxMessageId = maxUUID
                                                    emailConfigEntity.deleteMenuRefresh = false
                                                }
                                            }
                                            AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.update(emailConfigEntity)
                                        }


                                        KLog.i("解析邮件，第二次的数量为：" + messageList.size)
                                        messageList.forEachIndexed { index, item ->
                                            localEmailMessageNew.forEachIndexed { index1, eamilMessage ->
                                                if (eamilMessage.msgId.equals(item.id)) {
                                                    eamilMessage.setIsContainerAttachment(item.isContainerAttachment)
                                                    if (menu == "Starred" || menu == "Drafts" || menu == "Sent") {
                                                        eamilMessage.setIsSeen(true)
                                                    } else {
                                                        if (currentInfoId.equals(eamilMessage.msgId)) {
                                                            eamilMessage.setIsSeen(true)
                                                        } else {
                                                            eamilMessage.setIsSeen(item.isSeen)
                                                        }
                                                    }
                                                    eamilMessage.setIsStar(item.isStar)
                                                    eamilMessage.setIsReplySign(item.isReplySign)
                                                    eamilMessage.setAttachmentCount(item.attachmentCount)
                                                    eamilMessage.subject_ = item.subject
                                                    println("time_" + "imapStoreBeginHelp:" + item.subject + menuFlag + "##" + System.currentTimeMillis())
                                                    eamilMessage.content = item.content
                                                    eamilMessage.contentText = item.contentText
                                                    KLog.i("content为：" + eamilMessage.contentText)
                                                    var originMap = getOriginalText(eamilMessage)
                                                    eamilMessage.originalText = originMap.get("originalText")
                                                    eamilMessage.aesKey = originMap.get("aesKey")
                                                    eamilMessage.userId = originMap.get("userId")
//                                                    eamilMessage.date = item.date
                                                    eamilMessage.setTimeStamp_(DateUtil.getDateTimeStame(item.date))
                                                    AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.insert(eamilMessage)
                                                    if (eamilMessage.from_.indexOf("<") >= 0) {
                                                        name = eamilMessage.from_.substring(0, eamilMessage.from_.indexOf("<"))
                                                        account = eamilMessage.from_.substring(eamilMessage.from_.indexOf("<") + 1, eamilMessage.from_.length - 1)
                                                    } else {
                                                        name = eamilMessage.from_.substring(0, eamilMessage.from_.indexOf("@"))
                                                        account = eamilMessage.from_.substring(0, eamilMessage.from_.length)
                                                    }
                                                    name = name.replace("\"", "")
                                                    name = name.replace("\"", "")

                                                    var mailAttachmentList: List<MailAttachment> = item.mailAttachmentList
                                                    for (attachItem in mailAttachmentList) {
                                                        var attachList = AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.queryBuilder().where(EmailAttachEntityDao.Properties.MsgId.eq(menu + "_" + item.id), EmailAttachEntityDao.Properties.Name.eq(attachItem.name)).list()
                                                        if (attachList == null || attachList.size == 0) {
                                                            var eamilAttach = EmailAttachEntity()
                                                            eamilAttach.account = AppConfig.instance.emailConfig().account
                                                            eamilAttach.msgId = menu + "_" + item.id
                                                            eamilAttach.name = attachItem.name
                                                            eamilAttach.data = attachItem.byt
                                                            eamilAttach.hasData = true
                                                            eamilAttach.isCanDelete = false
                                                            AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.insert(eamilAttach)
                                                        }
                                                    }
                                                    account = account.toLowerCase()
                                                    var localEmailContacts = AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.queryBuilder().where(EmailContactsEntityDao.Properties.Account.eq(account)).list()
                                                    if (localEmailContacts.size == 0) {
                                                        var emailContactsEntity = EmailContactsEntity();
                                                        emailContactsEntity.name = name
                                                        emailContactsEntity.account = account
                                                        emailContactsEntity.createTime = System.currentTimeMillis()
                                                        AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.insert(emailContactsEntity)
                                                    }
                                                    runOnUiThread {
                                                        KLog.i("解析邮件, 第二次刷新")
                                                        if (currentInfoId.equals(eamilMessage.msgId)) {
                                                            EventBus.getDefault().post(eamilMessage)
                                                            currentInfoId = "-1"
                                                        }
                                                        emaiMessageChooseAdapter!!.notifyItemChanged(index1, "hehehe")
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    override fun gainFailure(errorMsg: String) {
                                        progressDialog.dismiss()
                                        runOnUiThread {
                                            toast(R.string.Failedmail)
                                            isRefreshing = false
                                            closeProgressDialog()
                                            refreshLayout.finishRefresh()
                                            refreshLayout.resetNoMoreData()
                                        }
                                    }
                                }, menu, minUUID, pageSize, maxUUID)
                    }
        }

    }

    fun pullNewGmailMessageList(localSize: Long) {
        var root_ = this.activity
        var account = AppConfig.instance.emailConfig().account
        var smtpHost = AppConfig.instance.emailConfig().smtpHost
        Log.i("pullMoreMessageList", account + ":" + smtpHost)

        var emailConfigEntityChoose = AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.queryBuilder().where(EmailConfigEntityDao.Properties.IsChoose.eq(true)).list()
        //var lastTotalCount = 0L;
        var minUUID = 0L
        var maxUUID = 0L
        if (emailConfigEntityChoose.size > 0) {
            var emailConfigEntity: EmailConfigEntity = emailConfigEntityChoose.get(0);
            when (menu) {
                emailConfigEntity.inboxMenu -> {
                    minUUID = emailConfigEntity.inboxMinMessageId
                    maxUUID = emailConfigEntity.inboxMaxMessageId
                }
                emailConfigEntity.drafMenu -> {
                    minUUID = emailConfigEntity.drafMinMessageId
                    maxUUID = emailConfigEntity.drafMaxMessageId
                }
                emailConfigEntity.sendMenu -> {
                    minUUID = emailConfigEntity.sendMinMessageId
                    maxUUID = emailConfigEntity.sendMaxMessageId
                }
                emailConfigEntity.garbageMenu -> {
                    minUUID = emailConfigEntity.garbageMinMessageId
                    maxUUID = emailConfigEntity.garbageMaxMessageId
                }
                emailConfigEntity.deleteMenu -> {
                    minUUID = emailConfigEntity.deleteMinMessageId
                    maxUUID = emailConfigEntity.deleteMaxMessageId
                }
            }
        }
        // var verifyList = AppConfig.instance.mDaoMaster!!.newSession().groupVerifyEntityDao.queryBuilder().where(GroupVerifyEntityDao.Properties.Aduit.eq(selfUserId)).list()

        if (true) {
            var beginIndex = localSize
            /*  AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.deleteAll()
              AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.deleteAll()*/
            var gmailService = GmailQuickstart.getGmailService(AppConfig.instance, account);
            var firstMessageEntity = emaiMessageChooseAdapter!!.getItem(0)
            var firstMessageId = "";
            if (firstMessageEntity != null) {
                firstMessageId = firstMessageEntity!!.msgId;
            }
            isRefreshing = true
            Islands.circularProgress(this.activity)
                    .setCancelable(false)
                    .setMessage("同步中...")
                    .run { progressDialog ->
                        val emailReceiveClient = EmailReceiveClient(AppConfig.instance.emailConfig())
                        emailReceiveClient
                                .gmailReceiveNewAsyn(gmailService, "me", this.activity, object : GetGmailReceiveCallback {
                                    override fun googlePlayFailure(availabilityException: GooglePlayServicesAvailabilityIOException?) {
                                        isRefreshing = false
                                        progressDialog.dismiss()
                                        runOnUiThread {
                                            toast(getString(R.string.Failedmail) + " code:" + availabilityException!!.connectionStatusCode)
                                            closeProgressDialog()
                                            refreshLayout.finishRefresh()
                                            refreshLayout.resetNoMoreData()
                                        }
                                    }

                                    override fun authFailure(userRecoverableException: UserRecoverableAuthIOException?) {
                                        runOnUiThread {
                                            closeProgressDialog()
                                            refreshLayout.finishRefresh()
                                            refreshLayout.resetNoMoreData()
                                            isRefreshing = false
                                        }
                                        root_!!.startActivityForResult(
                                                userRecoverableException!!.getIntent(),
                                                MainActivity.REQUEST_AUTHORIZATION);
                                    }

                                    override fun gainSuccess(messageList: List<EmailMessage>, minUUID: Long, maxUUID: Long, noMoreData: Boolean, errorMs: String, menuFlag: String, pageToken: String) {
                                        isRefreshing = false
                                        if (noMoreData) {
                                            runOnUiThread {
                                                closeProgressDialog()
                                                refreshLayout.finishRefresh()
                                                refreshLayout.resetNoMoreData()
                                                //toast(R.string.No_mail)
                                                //refreshLayout.finishLoadMoreWithNoMoreData()//将不会再次触发加载更多事件
                                            }
                                        } else {
                                            runOnUiThread {
                                                closeProgressDialog()
                                                refreshLayout.finishRefresh()
                                                refreshLayout.resetNoMoreData()
                                            }
                                        }
                                        /* if(errorMs != null && errorMs  != "" && "susan.zhou@qlink.mobi" == AppConfig.instance.emailConfig().account)
                                         {
                                             runOnUiThread {
                                                 SweetAlertDialog(root_, SweetAlertDialog.BUTTON_NEUTRAL)
                                                         .setCancelText(getString(R.string.close))
                                                         .setConfirmText(getString(R.string.yes))
                                                         .setContentText(errorMs)
                                                         .setConfirmClickListener {

                                                         }.setCancelClickListener {

                                                         }
                                                         .show()
                                             }
                                         }*/
                                        var emailConfigEntityChoose = AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.queryBuilder().where(EmailConfigEntityDao.Properties.IsChoose.eq(true)).list()
                                        if (emailConfigEntityChoose.size > 0) {
                                            var emailConfigEntity: EmailConfigEntity = emailConfigEntityChoose.get(0);
                                            when (menu) {
                                                emailConfigEntity.inboxMenu -> {
                                                    emailConfigEntity.totalCount += messageList.size
                                                    emailConfigEntity.inboxMaxMessageId = maxUUID
                                                    emailConfigEntity.inboxMenuRefresh = false
                                                }
                                                emailConfigEntity.drafMenu -> {
                                                    emailConfigEntity.drafTotalCount += messageList.size
                                                    emailConfigEntity.drafMaxMessageId = maxUUID
                                                    emailConfigEntity.drafMenuRefresh = false
                                                }
                                                emailConfigEntity.sendMenu -> {
                                                    emailConfigEntity.sendTotalCount += messageList.size
                                                    emailConfigEntity.sendMaxMessageId = maxUUID
                                                    emailConfigEntity.sendMenuRefresh = false
                                                }
                                                emailConfigEntity.garbageMenu -> {
                                                    emailConfigEntity.garbageCount += messageList.size
                                                    emailConfigEntity.garbageMaxMessageId = maxUUID
                                                    emailConfigEntity.garbageMenuRefresh = false
                                                }
                                                emailConfigEntity.deleteMenu -> {
                                                    emailConfigEntity.deleteTotalCount += messageList.size
                                                    emailConfigEntity.deleteMaxMessageId = maxUUID
                                                    emailConfigEntity.deleteMenuRefresh = false
                                                }
                                            }
                                            AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.update(emailConfigEntity)
                                        }
                                        var flag = 0;
                                        var localEmailMessageNew = mutableListOf<EmailMessageEntity>()
                                        var list = messageList;
                                        for (item in messageList) {
//                                            var localEmailMessage = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(account), EmailMessageEntityDao.Properties.Menu_.eq(menu), EmailMessageEntityDao.Properties.MsgId.eq(item.id)).list()
                                            var localEmailMessage = EmailUtils.loadLocalEmailWithMsgId(account, menu, item.id)
                                            var name = ""
                                            var account = ""
                                            if (localEmailMessage == null || localEmailMessage.size == 0) {
                                                var eamilMessage = EmailMessageEntity()
                                                eamilMessage.account_ = AppConfig.instance.emailConfig().account
                                                eamilMessage.msgId = item.id
                                                eamilMessage.menu_ = menuFlag
                                                eamilMessage.from_ = item.from
                                                eamilMessage.to_ = item.to
                                                eamilMessage.cc = item.cc
                                                eamilMessage.bcc = item.bcc
                                                eamilMessage.setIsContainerAttachment(item.isContainerAttachment)
                                                if (menu == "Starred" || menu == "Drafts" || menu == "Sent") {
                                                    eamilMessage.setIsSeen(true)
                                                } else {
                                                    eamilMessage.setIsSeen(item.isSeen)
                                                }
                                                eamilMessage.setIsStar(item.isStar)
                                                eamilMessage.setIsReplySign(item.isReplySign)
                                                eamilMessage.setAttachmentCount(item.attachmentCount)
                                                eamilMessage.subject_ = item.subject
                                                println("time_" + "imapStoreBeginHelp:" + item.subject + menuFlag + "##" + System.currentTimeMillis())
                                                eamilMessage.content = item.content
                                                eamilMessage.contentText = item.contentText
                                                var originMap = getOriginalText(eamilMessage)
                                                eamilMessage.originalText = originMap.get("originalText")
                                                eamilMessage.aesKey = originMap.get("aesKey")
                                                eamilMessage.userId = originMap.get("userId")
                                                eamilMessage.date_ = item.date
                                                eamilMessage.setTimeStamp_(DateUtil.getDateTimeStame(item.date))
                                                eamilMessage.sortId = DateUtil.getDateTimeStame(item.date);
                                                localEmailMessageNew.add(flag, eamilMessage)
                                                AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.insert(eamilMessage)
                                                if (eamilMessage.from_.indexOf("<") >= 0) {
                                                    name = eamilMessage.from_.substring(0, eamilMessage.from_.indexOf("<"))
                                                    account = eamilMessage.from_.substring(eamilMessage.from_.indexOf("<") + 1, eamilMessage.from_.length - 1)
                                                } else {
                                                    name = eamilMessage.from_.substring(0, eamilMessage.from_.indexOf("@"))
                                                    account = eamilMessage.from_.substring(0, eamilMessage.from_.length)
                                                }
                                                name = name.replace("\"", "")
                                                name = name.replace("\"", "")
                                            } else {
                                                continue
                                            }
                                            var mailAttachmentList: List<MailAttachment> = item.mailAttachmentList
                                            for (attachItem in mailAttachmentList) {
                                                var attachList = AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.queryBuilder().where(EmailAttachEntityDao.Properties.MsgId.eq(menu + "_" + item.id), EmailAttachEntityDao.Properties.Name.eq(attachItem.name)).list()
                                                if (attachList == null || attachList.size == 0) {
                                                    var eamilAttach = EmailAttachEntity()
                                                    eamilAttach.account = AppConfig.instance.emailConfig().account
                                                    eamilAttach.msgId = menu + "_" + item.id
                                                    eamilAttach.name = attachItem.name
                                                    eamilAttach.data = attachItem.byt
                                                    eamilAttach.hasData = true
                                                    eamilAttach.isCanDelete = false
                                                    AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.insert(eamilAttach)
                                                }
                                            }


                                            account = account.toLowerCase()
                                            var localEmailContacts = AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.queryBuilder().where(EmailContactsEntityDao.Properties.Account.eq(account)).list()
                                            if (localEmailContacts.size == 0) {
                                                var emailContactsEntity = EmailContactsEntity();
                                                emailContactsEntity.name = name
                                                emailContactsEntity.account = account
                                                emailContactsEntity.createTime = System.currentTimeMillis()
                                                AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.insert(emailContactsEntity)
                                            }
                                            flag++;
                                        }
                                        //var emailMessageEntityList = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.loadAll()
//                                        var localEmailMessage = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(account), EmailMessageEntityDao.Properties.Menu_.eq(menu)).orderDesc(EmailMessageEntityDao.Properties.SortId).list()
                                        var localEmailMessage = EmailUtils.loadLocalEmail(account, menu)
                                        var aabb = "'"
                                        runOnUiThread {
                                            //emaiMessageChooseAdapter!!.setNewData(localEmailMessage);
                                            var localEmailMessageNewSize = localEmailMessageNew.size
                                            for (index in 0 until localEmailMessageNewSize) {
                                                emaiMessageChooseAdapter!!.addData(index, localEmailMessageNew.get(index))
                                            }
                                            if (localEmailMessageNewSize > 0) {
                                                emaiMessageChooseAdapter!!.notifyDataSetChanged()
                                            }
                                            progressDialog.dismiss()
                                        }

                                    }

                                    override fun gainFailure(errorMsg: String) {
                                        progressDialog.dismiss()
                                        runOnUiThread {
                                            isRefreshing = false
                                            toast(R.string.Failedmail)
                                            closeProgressDialog()
                                            refreshLayout.finishRefresh()
                                            refreshLayout.resetNoMoreData()
                                        }
                                    }
                                }, menu, "", 150L, firstMessageId)
                    }
        }

    }

    //拉取更多邮件
    fun pullMoreMessageList(localSize: Int, isRefresh : Boolean = false) {
        var root_ = this.activity;
        var account = AppConfig.instance.emailConfig().account
        var smtpHost = AppConfig.instance.emailConfig().smtpHost
        Log.i("pullMoreMessageList", account + ":" + smtpHost)

        var emailConfigEntityChoose = AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.queryBuilder().where(EmailConfigEntityDao.Properties.IsChoose.eq(true)).list()
        var beginIndex = localSize
        var lastTotalCount = 0;
        if (emailConfigEntityChoose.size > 0) {
            var emailConfigEntity: EmailConfigEntity = emailConfigEntityChoose.get(0);
            when (menu) {
                emailConfigEntity.inboxMenu -> {
                    lastTotalCount = emailConfigEntity.totalCount
                }
                emailConfigEntity.drafMenu -> {
                    lastTotalCount = emailConfigEntity.drafTotalCount
                }
                emailConfigEntity.sendMenu -> {
                    lastTotalCount = emailConfigEntity.sendTotalCount
                }
                emailConfigEntity.garbageMenu -> {
                    lastTotalCount = emailConfigEntity.garbageCount
                }
                emailConfigEntity.deleteMenu -> {
                    lastTotalCount = emailConfigEntity.deleteTotalCount
                }
                /*emailConfigEntity.inboxMenu->
                {
                    lastTotalCount = emailConfigEntity.inboxMaxMessageId
                    beginIndex = emailConfigEntity.inboxMinMessageId
                }
                emailConfigEntity.drafMenu->
                {
                    lastTotalCount = emailConfigEntity.drafMaxMessageId
                    beginIndex = emailConfigEntity.drafMinMessageId
                }
                emailConfigEntity.sendMenu->
                {
                    lastTotalCount = emailConfigEntity.sendMaxMessageId
                    beginIndex = emailConfigEntity.sendMinMessageIdpu
                }
                emailConfigEntity.garbageMenu->
                {
                    lastTotalCount = emailConfigEntity.garbageMaxMessageId
                    beginIndex = emailConfigEntity.garbageMinMessageId
                }
                emailConfigEntity.deleteMenu->
                {
                    lastTotalCount = emailConfigEntity.deleteMaxMessageId
                    beginIndex = emailConfigEntity.deleteMinMessageId
                }*/
            }
        }
        // var verifyList = AppConfig.instance.mDaoMaster!!.newSession().groupVerifyEntityDao.queryBuilder().where(GroupVerifyEntityDao.Properties.Aduit.eq(selfUserId)).list()
        var firstMessageEntity = emaiMessageChooseAdapter!!.getItem(0)
        var lastMessageEntity = emaiMessageChooseAdapter!!.getItem(emaiMessageChooseAdapter!!.data.size - 1)

        var minUUID = 0L;
        var maxUUID = 0L;
        if (firstMessageEntity != null) {
            minUUID = lastMessageEntity!!.msgId.toLong();
            maxUUID = firstMessageEntity!!.msgId.toLong();
        } else {
            minUUID = 0L;
            maxUUID = 0L;
        }

        var emailMessageEntityNextList = mutableListOf<EmailMessageEntity>()

        var pageSize = 20;
        var pageFlag = 1
        var noDataLoad = false
        var pageSizeTemp = 10;
        var k = 0
//        var localEmailMessage = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(account), EmailMessageEntityDao.Properties.Menu_.eq(menu)).orderDesc(EmailMessageEntityDao.Properties.SortId).list()
        var localEmailMessage = EmailUtils.loadLocalEmail(account, menu)
        var uiDataSize = emaiMessageChooseAdapter!!.data.size;
        if (uiDataSize < localEmailMessage.size) {
            for (index in 0 until pageSize) {
                var flagIndex = uiDataSize + index
                if (flagIndex >= localEmailMessage.size) {
                    break;
                }
                emailMessageEntityNextList.add(index, localEmailMessage.get(flagIndex))
            }
        }
        if (emailMessageEntityNextList.size > 0) {
            runOnUiThread {
                closeProgressDialog()
                refreshLayout.finishLoadMore()
                var localEmailMessageNewSize = emailMessageEntityNextList.size
                for (index in 0 until localEmailMessageNewSize) {
                    var beginIndex = emaiMessageChooseAdapter!!.data.size;
                    emaiMessageChooseAdapter!!.addData(beginIndex, emailMessageEntityNextList.get(index))
                }
                if (localEmailMessageNewSize > 0) {
                    emaiMessageChooseAdapter!!.notifyDataSetChanged()
                }
            }
        } else {
            isLoadingMore = true
            if (true) {
                /*  AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.deleteAll()
                AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.deleteAll()*/
                Islands.circularProgress(this.activity)
                        .setCancelable(false)
                        .setMessage("同步中...")
                        .run { progressDialog ->
                            val emailReceiveClient = EmailReceiveClient(AppConfig.instance.emailConfig())
                            emailReceiveClient
                                    .imapReceiveMoreAsynByUUID(this.activity, object : GetReceiveCallback1 {
                                        var localEmailMessageNew = mutableListOf<EmailMessageEntity>()
                                        var beginIndex1 = emaiMessageChooseAdapter!!.data.size
                                        override fun gainPreSuccess(messageList: MutableList<EmailMessage>, totalCount: Long, totalUnreadCount: Long, noMoreData: Boolean, error: String, menuFlag: String) {
                                            var list = messageList;
                                            var flag = 0;
                                            KLog.i("解析邮件，第一次的数量为：" + messageList.size)
                                            for (item in messageList) {

                                                KLog.i("解析邮件：" + item.id)
//                                                var localEmailMessage = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(account), EmailMessageEntityDao.Properties.Menu_.eq(menu), EmailMessageEntityDao.Properties.MsgId.eq(item.id)).list()
                                                var localEmailMessage = EmailUtils.loadLocalEmailWithMsgId(account, menu, item.id)
                                                var name = ""
                                                var account = ""
                                                if (localEmailMessage == null || localEmailMessage.size == 0) {
                                                    var eamilMessage = EmailMessageEntity()
                                                    eamilMessage.account_ = AppConfig.instance.emailConfig().account
                                                    eamilMessage.msgId = item.id
                                                    eamilMessage.menu_ = menuFlag
                                                    eamilMessage.from_ = item.from
                                                    eamilMessage.to_ = item.to
                                                    eamilMessage.cc = item.cc
                                                    eamilMessage.bcc = item.bcc
                                                    eamilMessage.date_ = item.date
                                                    eamilMessage.sortId = item.id.toLong()
                                                    eamilMessage.subject_ = item.subject

                                                    localEmailMessageNew.add(flag, eamilMessage)
                                                    if (eamilMessage.from_.indexOf("<") >= 0) {
                                                        name = eamilMessage.from_.substring(0, eamilMessage.from_.indexOf("<"))
                                                        account = eamilMessage.from_.substring(eamilMessage.from_.indexOf("<") + 1, eamilMessage.from_.length - 1)
                                                    } else {
                                                        name = eamilMessage.from_.substring(0, eamilMessage.from_.indexOf("@"))
                                                        account = eamilMessage.from_.substring(0, eamilMessage.from_.length)
                                                    }
                                                    name = name.replace("\"", "")
                                                    name = name.replace("\"", "")
                                                } else {
                                                    continue
                                                }
                                                flag++
                                            }
                                            runOnUiThread {
                                                KLog.i("解析邮件，第一次添加的数量为：" + localEmailMessageNew.size)
                                                localEmailMessageNew.sortByDescending { it.msgId.toInt() }
                                                emaiMessageChooseAdapter!!.addData(localEmailMessageNew)
                                                progressDialog.dismiss()
                                            }
                                            if (noMoreData) {
                                                runOnUiThread {
                                                    closeProgressDialog()
                                                    if (isRefresh) {
                                                        refreshLayout.finishRefresh()
                                                    } else {
                                                        refreshLayout.finishLoadMore()
                                                    }
                                                }
                                            } else {
                                                runOnUiThread {
                                                    closeProgressDialog()
                                                    if (isRefresh) {
                                                        refreshLayout.finishRefresh()
                                                    } else {
                                                        refreshLayout.finishLoadMore()
                                                    }
                                                }
                                            }
                                        }

                                        override fun gainSuccess(messageList: List<EmailMessage>, minUUID: Long, maxUUID: Long, noMoreData: Boolean, errorMs: String, menuFlag: String) {
                                            isLoadingMore = false
                                            if (noMoreData) {
                                                runOnUiThread {
                                                    closeProgressDialog()
                                                    if (isRefresh) {
                                                        refreshLayout.finishRefresh()
                                                    } else {
                                                        refreshLayout.finishLoadMore()
                                                    }
                                                    //toast(R.string.No_mail)
                                                    //refreshLayout.finishLoadMoreWithNoMoreData()//将不会再次触发加载更多事件
                                                }
                                            } else {
                                                runOnUiThread {
                                                    closeProgressDialog()
                                                    if (isRefresh) {
                                                        refreshLayout.finishRefresh()
                                                    } else {
                                                        refreshLayout.finishLoadMore()
                                                    }
                                                }
                                            }
                                            var list = messageList;
                                            var flag = 0;
                                            KLog.i("解析邮件，第二次的数量为：" + messageList.size)
                                            messageList.forEachIndexed { index, item ->
                                                localEmailMessageNew.forEachIndexed { index1, eamilMessage ->

                                                    if (eamilMessage.msgId.equals(item.id)) {
                                                        var emailConfigEntityChoose = AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.queryBuilder().where(EmailConfigEntityDao.Properties.IsChoose.eq(true)).list()
                                                        if (emailConfigEntityChoose.size > 0) {
                                                            var emailConfigEntity: EmailConfigEntity = emailConfigEntityChoose.get(0);
                                                            when (menu) {
                                                                emailConfigEntity.inboxMenu -> {
                                                                    if (emailConfigEntity.inboxMaxMessageId == 0L) {
                                                                        emailConfigEntity.totalCount = minUUID.toInt()
                                                                        emailConfigEntity.inboxMaxMessageId = eamilMessage.id.toLong()
                                                                        emailConfigEntity.inboxMinMessageId = eamilMessage.id.toLong()
                                                                    }

                                                                }
                                                            }
                                                            AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.update(emailConfigEntity)
                                                        }

                                                        eamilMessage.setIsContainerAttachment(item.isContainerAttachment)
                                                        if (menu == "Starred" || menu == "Drafts" || menu == "Sent") {
                                                            eamilMessage.setIsSeen(true)
                                                        } else {
                                                            if (currentInfoId.equals(eamilMessage.msgId)) {
                                                                eamilMessage.setIsSeen(true)
                                                            } else {
                                                                eamilMessage.setIsSeen(item.isSeen)
                                                            }
                                                        }
                                                        eamilMessage.setIsStar(item.isStar)
                                                        eamilMessage.setIsReplySign(item.isReplySign)
                                                        eamilMessage.setAttachmentCount(item.attachmentCount)
                                                        eamilMessage.subject_ = item.subject
                                                        println("time_" + "imapStoreBeginHelp:" + item.subject + menuFlag + "##" + System.currentTimeMillis())
                                                        eamilMessage.content = item.content
                                                        eamilMessage.contentText = item.contentText
                                                        var originMap = getOriginalText(eamilMessage)
                                                        eamilMessage.originalText = originMap.get("originalText")
                                                        eamilMessage.aesKey = originMap.get("aesKey")
                                                        eamilMessage.userId = originMap.get("userId")
//                                                        eamilMessage.date = item.date
                                                        eamilMessage.setTimeStamp_(DateUtil.getDateTimeStame(item.date))
//                                                        eamilMessage.sortId = item.id.toLong();
                                                        AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.insert(eamilMessage)
                                                        if (eamilMessage.from_.indexOf("<") >= 0) {
                                                            name = eamilMessage.from_.substring(0, eamilMessage.from_.indexOf("<"))
                                                            account = eamilMessage.from_.substring(eamilMessage.from_.indexOf("<") + 1, eamilMessage.from_.length - 1)
                                                        } else {
                                                            name = eamilMessage.from_.substring(0, eamilMessage.from_.indexOf("@"))
                                                            account = eamilMessage.from_.substring(0, eamilMessage.from_.length)
                                                        }
                                                        name = name.replace("\"", "")
                                                        name = name.replace("\"", "")

                                                        var mailAttachmentList: List<MailAttachment> = item.mailAttachmentList
                                                        for (attachItem in mailAttachmentList) {
                                                            var attachList = AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.queryBuilder().where(EmailAttachEntityDao.Properties.MsgId.eq(menu + "_" + item.id), EmailAttachEntityDao.Properties.Name.eq(attachItem.name)).list()
                                                            if (attachList == null || attachList.size == 0) {
                                                                var eamilAttach = EmailAttachEntity()
                                                                eamilAttach.account = AppConfig.instance.emailConfig().account
                                                                eamilAttach.msgId = menu + "_" + item.id
                                                                eamilAttach.name = attachItem.name
                                                                eamilAttach.data = attachItem.byt
                                                                eamilAttach.hasData = true
                                                                eamilAttach.isCanDelete = false
                                                                AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.insert(eamilAttach)
                                                            }
                                                        }
                                                        account = account.toLowerCase()
                                                        var localEmailContacts = AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.queryBuilder().where(EmailContactsEntityDao.Properties.Account.eq(account)).list()
                                                        if (localEmailContacts.size == 0) {
                                                            var emailContactsEntity = EmailContactsEntity();
                                                            emailContactsEntity.name = name
                                                            emailContactsEntity.account = account
                                                            emailContactsEntity.createTime = System.currentTimeMillis()
                                                            AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.insert(emailContactsEntity)
                                                        }
                                                        runOnUiThread {
                                                            KLog.i("解析邮件, 第二次刷新")
                                                            if (currentInfoId.equals(eamilMessage.msgId)) {
                                                                EventBus.getDefault().post(eamilMessage)
                                                                currentInfoId = "-1"
                                                            }
                                                            emaiMessageChooseAdapter!!.notifyItemChanged(beginIndex1 + index1, "hehehe")
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        override fun gainFailure(errorMsg: String) {
                                            progressDialog.dismiss()
                                            runOnUiThread {
                                                isLoadingMore = false
                                                toast(R.string.Failedmail)
                                                closeProgressDialog()
                                                if (isRefresh) {
                                                    refreshLayout.finishRefresh()
                                                } else {
                                                    refreshLayout.finishLoadMore()
                                                }
                                            }
                                        }

                                    }, menu, minUUID, 10, maxUUID)
                        }
            }
        }

    }

    fun pullMoreGmailMessageList(pageToken: String, isRefresh: Boolean = false) {
        var root_ = this.activity;
        var account = AppConfig.instance.emailConfig().account
        var smtpHost = AppConfig.instance.emailConfig().smtpHost
        Log.i("pullMoreMessageList", account + ":" + smtpHost)

        var emailConfigEntityChoose = AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.queryBuilder().where(EmailConfigEntityDao.Properties.IsChoose.eq(true)).list()
        var lastTotalCount = 0;
        if (emailConfigEntityChoose.size > 0) {
            var emailConfigEntity: EmailConfigEntity = emailConfigEntityChoose.get(0);
            when (menu) {
                emailConfigEntity.inboxMenu -> {
                    lastTotalCount = emailConfigEntity.totalCount
                }
                emailConfigEntity.drafMenu -> {
                    lastTotalCount = emailConfigEntity.drafTotalCount
                }
                emailConfigEntity.sendMenu -> {
                    lastTotalCount = emailConfigEntity.sendTotalCount
                }
                emailConfigEntity.garbageMenu -> {
                    lastTotalCount = emailConfigEntity.garbageCount
                }
                emailConfigEntity.deleteMenu -> {
                    lastTotalCount = emailConfigEntity.deleteTotalCount
                }
            }
        }
        /* var firstMessageEntity =  emaiMessageChooseAdapter!!.getItem(0)
         var lastMessageEntity =  emaiMessageChooseAdapter!!.getItem(emaiMessageChooseAdapter!!.data.size -1)

         var minUUID = 0L;
         var maxUUID = 0L;
         if(firstMessageEntity != null)
         {
             minUUID = lastMessageEntity!!.msgId.toLong();
             maxUUID = firstMessageEntity!!.msgId.toLong();
         }else{
             minUUID = 0L;
             maxUUID = 0L;
         }*/

        var emailMessageEntityNextList = mutableListOf<EmailMessageEntity>()
        var pageSize = 10;
        var pageFlag = 1
        var noDataLoad = false
        var pageSizeTemp = 10;
        var k = 0
//        var localEmailMessage = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(account), EmailMessageEntityDao.Properties.Menu_.eq(menu)).orderDesc(EmailMessageEntityDao.Properties.SortId).list()
        var localEmailMessage = EmailUtils.loadLocalEmail(account, menu)
        var uiDataSize = emaiMessageChooseAdapter!!.data.size;
        if (uiDataSize < localEmailMessage.size) {
            for (index in 0 until pageSize) {
                var flagIndex = uiDataSize + index
                if (flagIndex >= localEmailMessage.size) {
                    break;
                }
                emailMessageEntityNextList.add(index, localEmailMessage.get(flagIndex))
            }
        }

        if (emailMessageEntityNextList.size > 0) {
            runOnUiThread {
                closeProgressDialog()
                refreshLayout.finishLoadMore()
                var localEmailMessageNewSize = emailMessageEntityNextList.size
                for (index in 0 until localEmailMessageNewSize) {
                    var beginIndex = emaiMessageChooseAdapter!!.data.size;
                    emaiMessageChooseAdapter!!.addData(beginIndex, emailMessageEntityNextList.get(index))
                }
                if (localEmailMessageNewSize > 0) {
                    emaiMessageChooseAdapter!!.notifyDataSetChanged()
                }
            }
        } else {
            if (true) {
                isLoadingMore = true
                var gmailService = GmailQuickstart.getGmailService(AppConfig.instance, account);
                /*  AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.deleteAll()
                  AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.deleteAll()*/
                Islands.circularProgress(this.activity)
                        .setCancelable(false)
                        .setMessage("同步中...")
                        .run { progressDialog ->
                            val emailReceiveClient = EmailReceiveClient(AppConfig.instance.emailConfig())
                            emailReceiveClient
                                    .gmailReceiveMoreAsyn(gmailService, "me", this.activity, object : GetGmailReceiveCallback {
                                        override fun googlePlayFailure(availabilityException: GooglePlayServicesAvailabilityIOException?) {
                                            progressDialog.dismiss()
                                            isLoadingMore = false
                                            runOnUiThread {
                                                toast(getString(R.string.Failedmail) + " code:" + availabilityException!!.connectionStatusCode)
                                                closeProgressDialog()
                                                if (isRefresh) {
                                                    refreshLayout.finishRefresh()
                                                } else {
                                                    refreshLayout.finishLoadMore()
                                                }
                                            }
                                        }

                                        override fun authFailure(userRecoverableException: UserRecoverableAuthIOException?) {
                                            isLoadingMore = false
                                            runOnUiThread {
                                                closeProgressDialog()
                                                if (isRefresh) {
                                                    refreshLayout.finishRefresh()
                                                } else {
                                                    refreshLayout.finishLoadMore()
                                                }
                                            }
                                            root_!!.startActivityForResult(
                                                    userRecoverableException!!.getIntent(),
                                                    MainActivity.REQUEST_AUTHORIZATION);
                                        }

                                        override fun gainSuccess(messageList: List<EmailMessage>, minUUID: Long, maxUUID: Long, noMoreData: Boolean, errorMs: String, menuFlag: String, pageToken: String) {
                                            isLoadingMore = false
                                            if (noMoreData) {
                                                runOnUiThread {
                                                    closeProgressDialog()
                                                    if (isRefresh) {
                                                        refreshLayout.finishRefresh()
                                                    } else {
                                                        refreshLayout.finishLoadMore()
                                                    }
                                                    //toast(R.string.No_mail)
                                                    //refreshLayout.finishLoadMoreWithNoMoreData()//将不会再次触发加载更多事件
                                                }
                                            } else {
                                                runOnUiThread {
                                                    closeProgressDialog()
                                                    if (isRefresh) {
                                                        refreshLayout.finishRefresh()
                                                    } else {
                                                        refreshLayout.finishLoadMore()
                                                    }
                                                }
                                            }
                                            /*if(errorMs != null && errorMs  != "" && "susan.zhou@qlink.mobi" == AppConfig.instance.emailConfig().account)
                                            {
                                                runOnUiThread {
                                                    SweetAlertDialog(root_, SweetAlertDialog.BUTTON_NEUTRAL)
                                                            .setCancelText(getString(R.string.close))
                                                            .setConfirmText(getString(R.string.yes))
                                                            .setContentText(errorMs)
                                                            .setConfirmClickListener {

                                                            }.setCancelClickListener {

                                                            }
                                                            .show()
                                                }
                                            }*/
                                            var list = messageList;
                                            KLog.i("拉到的谷歌邮件数量为：" + messageList.size)
                                            var flag = 0;
                                            var localEmailMessageNew = mutableListOf<EmailMessageEntity>()
                                            for (item in messageList) {
                                                var emailConfigEntityChoose = AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.queryBuilder().where(EmailConfigEntityDao.Properties.IsChoose.eq(true)).list()
                                                if (emailConfigEntityChoose.size > 0) {
                                                    var emailConfigEntity: EmailConfigEntity = emailConfigEntityChoose.get(0);
                                                    when (menu) {
                                                        emailConfigEntity.inboxMenu -> {
                                                            if (emailConfigEntity.inboxMaxMessageId == 0L) {
                                                                emailConfigEntity.totalCount = minUUID.toInt()
                                                                /* emailConfigEntity.inboxMaxMessageId = item.id.toLong()
                                                                 emailConfigEntity.inboxMinMessageId = item.id.toLong()*/
                                                            }

                                                        }
                                                    }
                                                    emailConfigEntity.pageToken = pageToken;
                                                    AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.update(emailConfigEntity)
                                                }
//                                                var localEmailMessage = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(account), EmailMessageEntityDao.Properties.Menu_.eq(menu), EmailMessageEntityDao.Properties.MsgId.eq(item.id)).list()
                                                var localEmailMessage = EmailUtils.loadLocalEmailWithMsgId(account, menu, item.id)
                                                var name = ""
                                                var account = ""
                                                if (localEmailMessage == null || localEmailMessage.size == 0) {
                                                    var eamilMessage = EmailMessageEntity()
                                                    eamilMessage.account_ = AppConfig.instance.emailConfig().account
                                                    eamilMessage.msgId = item.id
                                                    eamilMessage.menu_ = menuFlag
                                                    eamilMessage.from_ = item.from
                                                    eamilMessage.to_ = item.to
                                                    eamilMessage.cc = item.cc
                                                    eamilMessage.bcc = item.bcc
                                                    eamilMessage.setIsContainerAttachment(item.isContainerAttachment)
                                                    if (menu == "Starred" || menu == "Drafts" || menu == "Sent") {
                                                        eamilMessage.setIsSeen(true)
                                                    } else {
                                                        eamilMessage.setIsSeen(item.isSeen)
                                                    }
                                                    eamilMessage.setIsStar(item.isStar)
                                                    eamilMessage.setIsReplySign(item.isReplySign)
                                                    eamilMessage.setAttachmentCount(item.attachmentCount)
                                                    eamilMessage.subject_ = item.subject
                                                    println("time_" + "imapStoreBeginHelp:" + item.subject + menuFlag + "##" + System.currentTimeMillis())
                                                    eamilMessage.content = item.content
                                                    eamilMessage.contentText = item.contentText
                                                    var originMap = getOriginalText(eamilMessage)
                                                    eamilMessage.originalText = originMap.get("originalText")
                                                    eamilMessage.aesKey = originMap.get("aesKey")
                                                    eamilMessage.userId = originMap.get("userId")
                                                    eamilMessage.date_ = item.date
                                                    eamilMessage.setTimeStamp_(DateUtil.getDateTimeStame(item.date))
                                                    eamilMessage.sortId = DateUtil.getDateTimeStame(item.date);
                                                    localEmailMessageNew.add(flag, eamilMessage)
                                                    AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.insert(eamilMessage)
                                                    if (eamilMessage.from_.indexOf("<") >= 0) {
                                                        name = eamilMessage.from_.substring(0, eamilMessage.from_.indexOf("<"))
                                                        account = eamilMessage.from_.substring(eamilMessage.from_.indexOf("<") + 1, eamilMessage.from_.length - 1)
                                                    } else {
                                                        name = eamilMessage.from_.substring(0, eamilMessage.from_.indexOf("@"))
                                                        account = eamilMessage.from_.substring(0, eamilMessage.from_.length)
                                                    }
                                                    name = name.replace("\"", "")
                                                    name = name.replace("\"", "")
                                                } else {
                                                    continue
                                                }
                                                var mailAttachmentList: List<MailAttachment> = item.mailAttachmentList
                                                for (attachItem in mailAttachmentList) {
                                                    var attachList = AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.queryBuilder().where(EmailAttachEntityDao.Properties.MsgId.eq(menu + "_" + item.id), EmailAttachEntityDao.Properties.Name.eq(attachItem.name)).list()
                                                    if (attachList == null || attachList.size == 0) {
                                                        var eamilAttach = EmailAttachEntity()
                                                        eamilAttach.account = AppConfig.instance.emailConfig().account
                                                        eamilAttach.msgId = menu + "_" + item.id
                                                        eamilAttach.name = attachItem.name
                                                        eamilAttach.data = attachItem.byt
                                                        eamilAttach.hasData = true
                                                        eamilAttach.isCanDelete = false
                                                        AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.insert(eamilAttach)
                                                    }
                                                }
                                                account = account.toLowerCase()
                                                var localEmailContacts = AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.queryBuilder().where(EmailContactsEntityDao.Properties.Account.eq(account)).list()
                                                if (localEmailContacts.size == 0) {
                                                    var emailContactsEntity = EmailContactsEntity();
                                                    emailContactsEntity.name = name
                                                    emailContactsEntity.account = account
                                                    emailContactsEntity.createTime = System.currentTimeMillis()
                                                    AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.insert(emailContactsEntity)
                                                }
                                                flag++;
                                            }
                                            //var emailMessageEntityList = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.loadAll()
//                                            var localEmailMessage = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(account), EmailMessageEntityDao.Properties.Menu_.eq(menu)).orderDesc(EmailMessageEntityDao.Properties.SortId).list()
                                            runOnUiThread {
                                                //emaiMessageChooseAdapter!!.setNewData(localEmailMessage);
                                                var localEmailMessageNewSize = localEmailMessageNew.size
                                                KLog.i("要显示的谷歌邮件数量为：" + localEmailMessageNewSize)
                                                if (localEmailMessageNewSize > 0) {
                                                    for (index in localEmailMessageNewSize - 1 downTo 0) {
                                                        var beginIndex = emaiMessageChooseAdapter!!.data.size
                                                        emaiMessageChooseAdapter!!.addData(beginIndex, localEmailMessageNew.get(index))
                                                    }
                                                    emaiMessageChooseAdapter!!.notifyDataSetChanged()
                                                }
                                                progressDialog.dismiss()
                                            }
                                        }

                                        override fun gainFailure(errorMsg: String) {
                                            progressDialog.dismiss()
                                            isLoadingMore = false
                                            runOnUiThread {
                                                toast(R.string.Failedmail)
                                                closeProgressDialog()
                                                if (isRefresh) {
                                                    refreshLayout.finishRefresh()
                                                } else {
                                                    refreshLayout.finishLoadMore()
                                                }
                                            }
                                        }
                                    }, menu, pageToken, 8L, lastTotalCount)
                        }
            }
        }


    }

    fun getOriginalText(emailMeaasgeData: EmailMessageEntity): HashMap<String, String> {
        var contactMapList = HashMap<String, String>()
        var userID = ""
        try {
            if (emailMeaasgeData!!.content.contains("newconfidantcontent")) {
                //LogUtil.addLogEmail("getOriginalText_0", "EmailMessageFragment")
                var newconfidantcontent = "newconfidantcontent";
                var newconfidantcontentBeginIndex = emailMeaasgeData!!.content.indexOf("newconfidantcontent") + newconfidantcontent.length;
                var newconfidantcontentLeft = emailMeaasgeData!!.content.substring(newconfidantcontentBeginIndex, emailMeaasgeData!!.content.length)
                var newconfidantcontentEndIndex = newconfidantcontentLeft.indexOf("\"")
                if (newconfidantcontentEndIndex == -1) {
                    newconfidantcontentEndIndex = newconfidantcontentLeft.indexOf("'")
                }
                var newconfidantcontentGet = newconfidantcontentLeft.substring(0, newconfidantcontentEndIndex);
                var newconfidantcontentSouce = String(RxEncodeTool.base64Decode(newconfidantcontentGet))
                emailMeaasgeData!!.content = newconfidantcontentSouce;
            }

            if (emailMeaasgeData!!.content.contains("newconfidantpass")) {
                //LogUtil.addLogEmail("getOriginalText_1", "EmailMessageFragment")
                contactMapList.put("originalText", "")
                contactMapList.put("aesKey", "")
                if (emailMeaasgeData!!.content.contains("newconfidantuserid")) {
                    var userIDBeginStr = "newconfidantuserid'"
                    var beginIndex = emailMeaasgeData!!.content.indexOf("newconfidantuserid") + userIDBeginStr.length - 1;
                    var endIndex = beginIndex + 76;
                    userID = emailMeaasgeData!!.content.substring(beginIndex, endIndex)
                }
                contactMapList.put("userId", userID)
                return contactMapList
            }
            if (emailMeaasgeData!!.content.contains("confidantKey=") || emailMeaasgeData!!.content.contains("confidantkey=")) {
                //LogUtil.addLogEmail("getOriginalText_2", "EmailMessageFragment")
                try {
                    var endStr = ""
                    if (emailMeaasgeData!!.content.contains("myconfidantbegin")) {
                        endStr = "<div id=\"myconfidantbegin\">" +
                                "<br />" +
                                " <br />" +
                                " <br />" +
                                "<span>" +
                                getString(R.string.sendfromconfidant) +
                                "</span>" +
                                "</div>"
                    }
                    var miContentSoucreBgeinIndex = 0
                    var newconfidantKeyIndex = emailMeaasgeData!!.content.indexOf("confidantkey=")
                    var emailMeaasgeDataTemp = emailMeaasgeData!!.content.substring(0, newconfidantKeyIndex)
                    var miContentSoucreEndIndex = emailMeaasgeDataTemp.indexOf("<span style='display:none'")
                    if (miContentSoucreEndIndex == -1) {
                        miContentSoucreEndIndex = emailMeaasgeDataTemp.indexOf("<span style='display:none'")
                    }
                    if (miContentSoucreEndIndex == -1) {
                        miContentSoucreEndIndex = emailMeaasgeDataTemp.indexOf("<span style=\"display:none\"")
                    }
                    if (miContentSoucreEndIndex == -1) {
                        miContentSoucreEndIndex = emailMeaasgeDataTemp.indexOf("<span style=\"display:none\"")
                    }
                    var beginIndex = emailMeaasgeData!!.content.indexOf("confidantkey='")
                    if (beginIndex == -1) {
                        beginIndex = emailMeaasgeData!!.content.indexOf("confidantKey='")
                    }
                    if (beginIndex == -1) {
                        beginIndex = emailMeaasgeData!!.content.indexOf("confidantkey=\"")
                    }
                    if (beginIndex == -1) {
                        beginIndex = emailMeaasgeData!!.content.indexOf("confidantKey=\"")
                    }
                    if (beginIndex < 0) {
                        beginIndex = 0;
                    }
                    if (miContentSoucreEndIndex < 0) {
                        miContentSoucreEndIndex = 0;
                    }
                    var miContentSoucreBase64 = emailMeaasgeData!!.content.substring(miContentSoucreBgeinIndex, miContentSoucreEndIndex)
                    var endIndexd = emailMeaasgeData!!.content.length
                    if (endIndexd < beginIndex) {
                        endIndexd = beginIndex
                    }
                    var confidantkeyBefore = emailMeaasgeData!!.content.substring(beginIndex, endIndexd)

                    if (confidantkeyBefore.contains("confidantuserid")) {
                        var userIDBeginStr = "confidantuserid='"
                        userID = ""
                        var userIDBeginIndex = confidantkeyBefore.indexOf(userIDBeginStr)
                        if (userIDBeginIndex == -1) {
                            userIDBeginStr = "confidantuserid=\""
                            userIDBeginIndex = confidantkeyBefore.indexOf("confidantuserid=\"")
                        }
                        var userIDEndIndex = confidantkeyBefore.lastIndexOf("'></span>")
                        if (userIDEndIndex < 0) {
                            userIDEndIndex = confidantkeyBefore.lastIndexOf("\"></span>")
                        }
                        userID = confidantkeyBefore.substring(userIDBeginIndex + userIDBeginStr.length, userIDEndIndex)
                        var aa = ""
                    }
                    var endIndex = confidantkeyBefore.indexOf("'></span>")
                    if (endIndex < 0) {
                        endIndex = confidantkeyBefore.indexOf("\"></span>")
                    }
                    if (endIndex < 14) {
                        endIndex = 14
                    }
                    var confidantkey = confidantkeyBefore.substring(14, endIndex)

                    var confidantkeyArr = listOf<String>()
                    var accountMi = ""
                    var shareMiKey = ""
                    var account = String(RxEncodeTool.base64Decode(accountMi))
                    if (confidantkey!!.contains("##")) {
                        var confidantkeyList = confidantkey.split("##")
                        for (item in confidantkeyList) {
                            if (item.contains("&&")) {
                                confidantkeyArr = item.split("&&")
                            } else {
                                confidantkeyArr = item.split("&amp;&amp;")
                            }

                            accountMi = confidantkeyArr.get(0)
                            shareMiKey = confidantkeyArr.get(1)
                            account = String(RxEncodeTool.base64Decode(accountMi))
                            if (account != "" && account.toLowerCase().contains(AppConfig.instance.emailConfig().account.toLowerCase())) {
                                break;
                            }
                        }

                    } else {
                        if (confidantkey.contains("&&")) {
                            confidantkeyArr = confidantkey.split("&&")
                        } else {
                            confidantkeyArr = confidantkey.split("&amp;&amp;")
                        }
                        accountMi = confidantkeyArr.get(0)
                        shareMiKey = confidantkeyArr.get(1)
                    }
                    var aesKey = LibsodiumUtil.DecryptShareKey(shareMiKey, ConstantValue.libsodiumpublicMiKey!!, ConstantValue.libsodiumprivateMiKey!!);
                    //LogUtil.addLogEmail("getOriginalText_3_aesKey:"+aesKey, "EmailMessageFragment")
                    var miContentSoucreBase = RxEncodeTool.base64Decode(miContentSoucreBase64)
                    val miContent = AESCipher.aesDecryptBytes(miContentSoucreBase, aesKey.toByteArray())
                    var sourceContent = ""
                    try {
                        sourceContent = String(miContent)
                        //LogUtil.addLogEmail("getOriginalText_4", "EmailMessageFragment")
                        contactMapList.put("originalText", sourceContent + endStr)
                        contactMapList.put("aesKey", aesKey)
                        contactMapList.put("userId", userID)
                    } catch (e: Exception) {
                        //LogUtil.addLogEmail("getOriginalText_5_error:"+e.message.toString(), "EmailMessageFragment")
                        contactMapList.put("originalText", "")
                        contactMapList.put("aesKey", "")
                        contactMapList.put("userId", userID)
                    } finally {
                        return contactMapList
                    }
                } catch (e: Exception) {
                    //LogUtil.addLogEmail("getOriginalText_6_error:"+e.message.toString(), "EmailMessageFragment")
                    contactMapList.put("originalText", "")
                    contactMapList.put("aesKey", "")
                    contactMapList.put("userId", userID)
                } finally {
                    return contactMapList
                }

            } else if (emailMeaasgeData!!.content.contains("newconfidantKey") || emailMeaasgeData!!.content.contains("newconfidantkey")) {
                //LogUtil.addLogEmail("getOriginalText_7", "EmailMessageFragment")
                try {
                    var endStr = ""
                    if (emailMeaasgeData!!.content.contains("newmyconfidantbegin")) {
                        endStr = "<div id=\"newmyconfidantbegin\">" +
                                "<br />" +
                                " <br />" +
                                " <br />" +
                                "<span>" +
                                getString(R.string.sendfromconfidant) +
                                "</span>" +
                                "</div>"
                    }
                    var miContentSoucreBgeinIndex = 0
                    var newconfidantKeyIndex = emailMeaasgeData!!.content.indexOf("newconfidantkey")
                    var emailMeaasgeDataTemp = emailMeaasgeData!!.content.substring(0, newconfidantKeyIndex)
                    var miContentSoucreEndIndex = emailMeaasgeDataTemp.indexOf("<span style='display:none'")
                    if (miContentSoucreEndIndex == -1) {
                        miContentSoucreEndIndex = emailMeaasgeDataTemp.indexOf("<span style='display:none'")
                    }
                    if (miContentSoucreEndIndex == -1) {
                        miContentSoucreEndIndex = emailMeaasgeDataTemp.indexOf("<span style=\"display:none\"")
                    }
                    if (miContentSoucreEndIndex == -1) {
                        miContentSoucreEndIndex = emailMeaasgeDataTemp.indexOf("<span style=\"display:none\"")
                    }
                    var beginIndex = emailMeaasgeData!!.content.indexOf("newconfidantkey")
                    if (beginIndex == -1) {
                        beginIndex = emailMeaasgeData!!.content.indexOf("newconfidantKey")
                    }
                    if (beginIndex == -1) {
                        beginIndex = emailMeaasgeData!!.content.indexOf("newconfidantkey")
                    }
                    if (beginIndex == -1) {
                        beginIndex = emailMeaasgeData!!.content.indexOf("newconfidantKey")
                    }
                    if (beginIndex < 0) {
                        beginIndex = 0;
                    }
                    if (miContentSoucreEndIndex < 0) {
                        miContentSoucreEndIndex = 0;
                    }
                    var miContentSoucreBase64 = emailMeaasgeData!!.content.substring(miContentSoucreBgeinIndex, miContentSoucreEndIndex)
                    var endIndexd = emailMeaasgeData!!.content.length
                    if (endIndexd < beginIndex) {
                        endIndexd = beginIndex
                    }
                    var confidantkeyBefore = emailMeaasgeData!!.content.substring(beginIndex, endIndexd)

                    if (confidantkeyBefore.contains("newconfidantuserid")) {
                        var userIDBeginStr = "newconfidantuserid'"
                        userID = ""
                        var userIDBeginIndex = confidantkeyBefore.indexOf(userIDBeginStr)
                        if (userIDBeginIndex == -1) {
                            userIDBeginStr = "newconfidantuserid"
                            userIDBeginIndex = confidantkeyBefore.indexOf("newconfidantuserid")
                        }
                        var userIDEndIndex = confidantkeyBefore.lastIndexOf("'></span>")
                        if (userIDEndIndex < 0) {
                            userIDEndIndex = confidantkeyBefore.lastIndexOf("\"></span>")
                        }
                        userID = confidantkeyBefore.substring(userIDBeginIndex + userIDBeginStr.length, userIDEndIndex)
                        var aa = ""
                    }
                    var endIndex = confidantkeyBefore.indexOf("'></span>")
                    if (endIndex < 0) {
                        endIndex = confidantkeyBefore.indexOf("\"></span>")
                    }
                    if (endIndex < 15) {
                        endIndex = 15
                    }
                    var confidantkey = confidantkeyBefore.substring(15, endIndex)

                    var confidantkeyArr = listOf<String>()
                    var accountMi = ""
                    var shareMiKey = ""
                    var account = String(RxEncodeTool.base64Decode(accountMi))
                    if (confidantkey!!.contains("##")) {
                        var confidantkeyList = confidantkey.split("##")
                        for (item in confidantkeyList) {
                            if (item.contains("&&")) {
                                confidantkeyArr = item.split("&&")
                            } else {
                                confidantkeyArr = item.split("&amp;&amp;")
                            }

                            accountMi = confidantkeyArr.get(0)
                            shareMiKey = confidantkeyArr.get(1)
                            account = String(RxEncodeTool.base64Decode(accountMi))
                            if (account != "" && account.toLowerCase().contains(AppConfig.instance.emailConfig().account.toLowerCase())) {
                                break;
                            }
                        }

                    } else {
                        if (confidantkey.contains("&&")) {
                            confidantkeyArr = confidantkey.split("&&")
                        } else {
                            confidantkeyArr = confidantkey.split("&amp;&amp;")
                        }
                        accountMi = confidantkeyArr.get(0)
                        shareMiKey = confidantkeyArr.get(1)
                    }
                    KLog.i(shareMiKey)
                    KLog.i(ConstantValue.libsodiumpublicMiKey!!)
                    KLog.i(ConstantValue.libsodiumprivateMiKey!!)
                    var aesKey = LibsodiumUtil.DecryptShareKey(shareMiKey, ConstantValue.libsodiumpublicMiKey!!, ConstantValue.libsodiumprivateMiKey!!);
                    var miContentSoucreBase = RxEncodeTool.base64Decode(miContentSoucreBase64)
                    val miContent = AESCipher.aesDecryptBytes(miContentSoucreBase, aesKey.toByteArray())
                    var sourceContent = ""
                    try {
                        sourceContent = String(miContent)
                        //LogUtil.addLogEmail("getOriginalText_8_aesKey:"+aesKey, "EmailMessageFragment")
                        contactMapList.put("originalText", sourceContent + endStr)
                        contactMapList.put("aesKey", aesKey)
                        contactMapList.put("userId", userID)
                    } catch (e: Exception) {
                        //LogUtil.addLogEmail("getOriginalText_9_error:"+e.message.toString(), "EmailMessageFragment")
                        contactMapList.put("originalText", "")
                        contactMapList.put("aesKey", "")
                        contactMapList.put("userId", userID)
                    } finally {
                        return contactMapList
                    }
                } catch (e: Exception) {
                    //LogUtil.addLogEmail("getOriginalText_10_error:"+e.message.toString(), "EmailMessageFragment")
                    contactMapList.put("originalText", "")
                    contactMapList.put("aesKey", "")
                    contactMapList.put("userId", userID)
                } finally {
                    return contactMapList
                }

            } else {
                contactMapList.put("originalText", "")
                contactMapList.put("aesKey", "")
                contactMapList.put("userId", userID)
                return contactMapList
            }
        } catch (e: Exception) {
            //LogUtil.addLogEmail("getOriginalText_11_error" +"emailMeaasgeData:"+emailMeaasgeData.subject+"_error"+ e.message.toString(), "EmailMessageFragment")
            contactMapList.put("originalText", "")
            contactMapList.put("aesKey", "")
            contactMapList.put("userId", userID)
            return contactMapList
        }

    }

    override fun initDataFromLocal() {

    }

    fun initQuerData() {
        query.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                var localMessageList = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(AppConfig.instance.emailConfig().account), EmailMessageEntityDao.Properties.Menu_.eq(menu)).orderDesc(EmailMessageEntityDao.Properties.SortId).list()
                var localMessageList = EmailUtils.loadLocalEmail(AppConfig.instance.emailConfig().account, menu)
                if (localMessageList == null || localMessageList.size > 0) {
                    var localMessageListData = arrayListOf<EmailMessageEntity>()
                    for (item in localMessageList) {
                        localMessageListData.add(item);
                    }
                    fiter(s.toString(), localMessageListData)
                }


            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    override fun showProgressDialog() {
        progressDialog.show()
    }

    fun showProgressDialog(text: String) {
        try {
            KLog.i("弹窗：showProgressDialog_" + text)
            progressDialog.setDialogText(text)
            progressDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun closeProgressDialog() {
        if (progressDialog != null)
            progressDialog.hide()
    }

    fun shouUI(flag: Boolean) {
        searchParent.visibility = if (flag) View.VISIBLE else View.GONE
        if (flag && AppConfig.instance.emailConfig().account != null) {
//            var localMessageList = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account_.eq(AppConfig.instance.emailConfig().account), EmailMessageEntityDao.Properties.Menu_.eq(menu)).orderDesc(EmailMessageEntityDao.Properties.SortId).list()
            var localMessageList = EmailUtils.loadLocalEmail(AppConfig.instance.emailConfig().account, menu)
            if (localMessageList == null || localMessageList.size > 0) {
                var localMessageListData = arrayListOf<EmailMessageEntity>()
                for (item in localMessageList) {
                    localMessageListData.add(item);
                }
                fiter(query.text.toString(), localMessageListData)
            }
        }
    }

    fun fiter(key: String, emailMessageList: ArrayList<EmailMessageEntity>) {
        var contactListTemp: ArrayList<EmailMessageEntity> = arrayListOf<EmailMessageEntity>()
        for (i in emailMessageList) {
            var content = ""
            var aa = i.originalText
            if (aa != null && aa != "") {
                content = aa;
            } else {
                content = i.content
            }
            if (i.from_.toLowerCase().contains(key) || content.toLowerCase().contains(key) || i.subject_.toLowerCase().contains(key)) {
                contactListTemp.add(i)
            }
        }
        if (key != "") {
            emaiMessageChooseAdapter!!.setNewData(contactListTemp);
        } else {
            emaiMessageChooseAdapter!!.setNewData(emailMessageList);
        }
    }

    public fun updateMenu(menuName: String) {
        menu = menuName
    }

    override fun onDestroy() {
        AppConfig.instance.messageReceiver!!.pullMailListCallback = null
        super.onDestroy()
    }
}