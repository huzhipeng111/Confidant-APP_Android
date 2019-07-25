package com.stratagile.pnrouter.ui.activity.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pawegio.kandroid.runOnUiThread
import com.smailnet.eamil.Callback.GetReceiveCallback
import com.smailnet.eamil.EmailMessage
import com.smailnet.eamil.EmailReceiveClient
import com.smailnet.eamil.MailAttachment
import com.smailnet.eamil.Utils.MailUtil
import com.smailnet.islands.Islands
import com.stratagile.pnrouter.R
import com.stratagile.pnrouter.application.AppConfig
import com.stratagile.pnrouter.base.BaseFragment
import com.stratagile.pnrouter.constant.ConstantValue
import com.stratagile.pnrouter.db.*
import com.stratagile.pnrouter.entity.events.ChangEmailMenu
import com.stratagile.pnrouter.entity.events.ChangFragmentMenu
import com.stratagile.pnrouter.entity.events.FromChat
import com.stratagile.pnrouter.ui.activity.email.EmailInfoActivity
import com.stratagile.pnrouter.ui.activity.main.component.DaggerEmailMessageComponent
import com.stratagile.pnrouter.ui.activity.main.contract.EmailMessageContract
import com.stratagile.pnrouter.ui.activity.main.module.EmailMessageModule
import com.stratagile.pnrouter.ui.activity.main.presenter.EmailMessagePresenter
import com.stratagile.pnrouter.ui.adapter.conversation.EmaiMessageAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_menu.view.*
import kotlinx.android.synthetic.main.fragment_mail_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

/**
 * @author zl
 * @Package com.stratagile.pnrouter.ui.activity.main
 * @Description: $description
 * @date 2019/07/11 16:19:12
 */

class EmailMessageFragment : BaseFragment(), EmailMessageContract.View {

    @Inject
    lateinit internal var mPresenter: EmailMessagePresenter
    var emaiMessageChooseAdapter : EmaiMessageAdapter? = null
    var menu = "INBOX"
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun changEmailMenu(changEmailMenu: ChangEmailMenu) {
        menu = changEmailMenu.menu
        pullMessageList()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_mail_list, null);
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var emailMessageEntityList = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.loadAll()
        emaiMessageChooseAdapter = EmaiMessageAdapter(emailMessageEntityList)
        emaiMessageChooseAdapter!!.setOnItemLongClickListener { adapter, view, position ->
            /* val floatMenu = FloatMenu(activity)
             floatMenu.items("菜单1", "菜单2", "菜单3")
             floatMenu.show((activity!! as BaseActivity).point,0,0)*/
            true
        }
        recyclerView.adapter = emaiMessageChooseAdapter
        emaiMessageChooseAdapter!!.setOnItemClickListener { adapter, view, position ->
            var intent = Intent(activity!!, EmailInfoActivity::class.java)
            intent.putExtra("emailMeaasgeData", emaiMessageChooseAdapter!!.getItem(position))
            intent.putExtra("menu", menu)
            startActivity(intent)
        }
        refreshLayout.setOnRefreshListener {
            pullMessageList()
            if (refreshLayout != null)
                refreshLayout.isRefreshing = false
        }
        EventBus.getDefault().register(this)
    }
    override fun onResume() {
        super.onResume()

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if(isVisibleToUser)
        {
            EventBus.getDefault().post(ChangFragmentMenu("Email"))
            //pullMessageList()
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
    fun pullMessageList() {
        var account= AppConfig.instance.emailConfig().account
        var smtpHost = AppConfig.instance.emailConfig().smtpHost
        Log.i("pullMessageList",account +":"+smtpHost)
        // var verifyList = AppConfig.instance.mDaoMaster!!.newSession().groupVerifyEntityDao.queryBuilder().where(GroupVerifyEntityDao.Properties.Aduit.eq(selfUserId)).list()
        var localEmailMessage = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account.eq(account),EmailMessageEntityDao.Properties.Menu.eq(menu)).list()
        if(true)
        {
            Islands.circularProgress(this.activity)
                    .setCancelable(false)
                    .setMessage("同步中...")
                    .show()
                    .run { progressDialog ->
                        val emailReceiveClient = EmailReceiveClient(AppConfig.instance.emailConfig())
                        emailReceiveClient
                                .imapReceiveAsyn(this.activity, object : GetReceiveCallback {
                                    override fun gainSuccess(messageList: List<EmailMessage>, count: Int) {
                                        var list = messageList;
                                        AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.deleteAll()
                                        AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.deleteAll()
                                        for (item in messageList)
                                        {
                                            var eamilMessage = EmailMessageEntity()
                                            eamilMessage.account = AppConfig.instance.emailConfig().account
                                            eamilMessage.msgId = item.id
                                            eamilMessage.menu = menu
                                            eamilMessage.from = item.from
                                            eamilMessage.to = item.to
                                            eamilMessage.cc = item.cc
                                            eamilMessage.bcc = item.bcc
                                            eamilMessage.setIsContainerAttachment(item.isContainerAttachment)
                                            eamilMessage.setAttachmentCount(item.attachmentCount)
                                            eamilMessage.setIsSeen(item.isSeen)
                                            eamilMessage.setIsStar(item.isStar)
                                            eamilMessage.setIsReplySign(item.isReplySign)
                                            eamilMessage.subject = item.subject
                                            eamilMessage.content= item.content
                                            eamilMessage.contentText= item.contentText
                                            eamilMessage.date = item.date
                                            AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.insert(eamilMessage)
                                            var mailAttachmentList: List<MailAttachment> = item.mailAttachmentList
                                            for (attachItem in mailAttachmentList)
                                            {
                                                var eamilAttach = EmailAttachEntity()
                                                eamilAttach.account = AppConfig.instance.emailConfig().account
                                                eamilAttach.msgId = item.id
                                                eamilAttach.name = attachItem.name
                                                eamilAttach.data = attachItem.byt
                                                AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.insert(eamilAttach)
                                            }

                                            var name  = eamilMessage.from.substring(0,eamilMessage.from.indexOf("<"))
                                            var account= eamilMessage.from.substring(eamilMessage.from.indexOf("<")+1,eamilMessage.from.length)
                                            var localEmailContacts = AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.queryBuilder().where(EmailContactsEntityDao.Properties.Account.eq(account)).list()
                                            if(localEmailContacts.size == 0)
                                            {
                                                var emailContactsEntity= EmailContactsEntity();
                                                emailContactsEntity.name = name
                                                emailContactsEntity.account = account
                                                AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.insert(emailContactsEntity)
                                            }

                                        }
                                        //var emailMessageEntityList = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.loadAll()
                                        var localEmailMessage = AppConfig.instance.mDaoMaster!!.newSession().emailMessageEntityDao.queryBuilder().where(EmailMessageEntityDao.Properties.Account.eq(account),EmailMessageEntityDao.Properties.Menu.eq(menu)).list()
                                        runOnUiThread {
                                            emaiMessageChooseAdapter!!.setNewData(localEmailMessage);
                                            progressDialog.dismiss()
                                        }

                                    }

                                    override fun gainFailure(errorMsg: String) {
                                        progressDialog.dismiss()

                                    }
                                },menu)
                    }
        }else{
            runOnUiThread {
                emaiMessageChooseAdapter!!.setNewData(localEmailMessage);
            }
        }

    }
    override fun initDataFromLocal() {

    }

    override fun showProgressDialog() {
        progressDialog.show()
    }

    override fun closeProgressDialog() {
        progressDialog.hide()
    }
}