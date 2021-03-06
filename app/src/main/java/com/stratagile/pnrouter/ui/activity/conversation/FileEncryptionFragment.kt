package com.stratagile.pnrouter.ui.activity.conversation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.stratagile.pnrouter.application.AppConfig
import com.stratagile.pnrouter.base.BaseFragment
import com.stratagile.pnrouter.ui.activity.conversation.component.DaggerFileEncryptionComponent
import com.stratagile.pnrouter.ui.activity.conversation.contract.FileEncryptionContract
import com.stratagile.pnrouter.ui.activity.conversation.module.FileEncryptionModule
import com.stratagile.pnrouter.ui.activity.conversation.presenter.FileEncryptionPresenter

import javax.inject.Inject;

import com.pawegio.kandroid.runOnUiThread
import com.pawegio.kandroid.toast
import com.stratagile.pnrouter.BuildConfig
import com.stratagile.pnrouter.R
import com.stratagile.pnrouter.constant.ConstantValue
import com.stratagile.pnrouter.data.web.PNRouterServiceMessageReceiver
import com.stratagile.pnrouter.entity.*
import com.stratagile.pnrouter.entity.events.ForegroundCallBack
import com.stratagile.pnrouter.ui.activity.encryption.ContactsEncryptionActivity
import com.stratagile.pnrouter.ui.activity.encryption.PicEncryptionActivity
import com.stratagile.pnrouter.ui.activity.encryption.SMSEncryptionActivity
import com.stratagile.pnrouter.ui.activity.encryption.WeiXinEncryptionActivity
import com.stratagile.pnrouter.utils.*
import com.stratagile.tox.toxcore.ToxCoreJni
import kotlinx.android.synthetic.main.fragment_file_encryption.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @author zl
 * @Package com.stratagile.pnrouter.ui.activity.conversation
 * @Description: $description
 * @date 2019/11/20 10:12:15
 */

class FileEncryptionFragment : BaseFragment(), FileEncryptionContract.View , PNRouterServiceMessageReceiver.BakAddrUserNumOutCallback{
    override fun getScanPermissionFaile() {
        if(ContextCompat.checkSelfPermission(AppConfig.instance, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
        {
            var count = FileUtil.getContactCount(this@FileEncryptionFragment.context)
            runOnUiThread {
                localContacts.text = count.toString();
            }
        }
        if(ContextCompat.checkSelfPermission(AppConfig.instance, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED)
        {
            var msgCount = FileUtil.getAllSmsCount(this@FileEncryptionFragment.context)
            runOnUiThread {
                localMessags.text = msgCount.toString();
            }
        }
    }

    override fun getSMSPermissionFaile() {
        if(ContextCompat.checkSelfPermission(AppConfig.instance, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
        {
            var count = FileUtil.getContactCount(this@FileEncryptionFragment.context)
            runOnUiThread {
                localContacts.text = count.toString();
            }
        }
        if(ContextCompat.checkSelfPermission(AppConfig.instance, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED)
        {
            var msgCount = FileUtil.getAllSmsCount(this@FileEncryptionFragment.context)
            runOnUiThread {
                localMessags.text = msgCount.toString();
            }
        }
    }

    override fun getBakContentStatCallback(jGetBakContentStatRsp: JGetBakContentStatRsp) {
        runOnUiThread {
            closeProgressDialog()
        }
        if(jGetBakContentStatRsp.params.retCode == 0)
        {
            runOnUiThread {
                nodeMessags.text = jGetBakContentStatRsp.params.num.toString();
            }
        }else{

        }
    }
   override fun getSMSPermissionSuccess() {
       if(ContextCompat.checkSelfPermission(AppConfig.instance, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
       {
           var count = FileUtil.getContactCount(this@FileEncryptionFragment.context)
           runOnUiThread {
               localContacts.text = count.toString();
           }
       }
       if(ContextCompat.checkSelfPermission(AppConfig.instance, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED)
       {
           var msgCount = FileUtil.getAllSmsCount(this@FileEncryptionFragment.context)
           runOnUiThread {
               localMessags.text = msgCount.toString();
           }
       }
    }

    override fun getScanPermissionSuccess() {
        if(ContextCompat.checkSelfPermission(AppConfig.instance, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
        {
            var count = FileUtil.getContactCount(this@FileEncryptionFragment.context)
            runOnUiThread {
                localContacts.text = count.toString();
            }
        }
        if(ContextCompat.checkSelfPermission(AppConfig.instance, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED)
        {
            var msgCount = FileUtil.getAllSmsCount(this@FileEncryptionFragment.context)
            runOnUiThread {
                localMessags.text = msgCount.toString();
            }
        }
    }

    override fun bakAddrUserNum(jBakAddrUserNumRsp: JBakAddrUserNumRsp) {
       /* runOnUiThread {
            closeProgressDialog()
        }*/
        if(jBakAddrUserNumRsp.params.retCode == 0)
        {
            runOnUiThread {
                nodeContacts.text = jBakAddrUserNumRsp.params.num.toString();
            }
        }else{

        }
    }

    @Inject
    lateinit internal var mPresenter: FileEncryptionPresenter
    var isGoContact = true
    var isGoSms = true;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_file_encryption, null);

        return view
    }
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if(isVisibleToUser)
        {
            var leftData = LocalFileUtils.coverlocalFilesList
            FileUtil.saveRouterData("fileData6", "")
            LocalFileUtils.coverUpdateList(leftData)

            if(ContextCompat.checkSelfPermission(AppConfig.instance, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
            {
                var count = FileUtil.getContactCount(this@FileEncryptionFragment.context)
                runOnUiThread {
                    localContacts.text = count.toString();
                }
            }
            if(ContextCompat.checkSelfPermission(AppConfig.instance, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED)
            {
                var msgCount = FileUtil.getAllSmsCount(this@FileEncryptionFragment.context)
                runOnUiThread {
                    localMessags.text = msgCount.toString();
                }
            }
            if(!BuildConfig.isGooglePlay)
            {
                mPresenter.getSMSPermission()
            }else{
                mPresenter.getScanPermission()
            }
            getNodeData()
        }
    }
    fun getNodeData()
    {
        var selfUserId = SpUtil.getString(AppConfig.instance, ConstantValue.userId, "")
        var filesListPullReq = BakAddrUserNumReq( selfUserId!!, 0)
        var sendData = BaseData(6, filesListPullReq);

        var GetBakContentStatReq = GetBakContentStatReq( 1,selfUserId!!)
        var sendData2 = BaseData(6, GetBakContentStatReq);

        //showProgressDialog();
        if(AppConfig.instance.isOpenSplashActivity)
        {
            if(BuildConfig.DEBUG)
            {
                var baseData = sendData;
                var baseDataJonn = baseData.baseDataToJson().replace("\\","")
            }

        }else
        {

        }
        if (ConstantValue.isWebsocketConnected) {
            AppConfig.instance.getPNRouterServiceMessageSender().send(sendData)
            AppConfig.instance.getPNRouterServiceMessageSender().send(sendData2)
        }else if (ConstantValue.isToxConnected) {
            var baseData = sendData
            var baseDataJson = baseData.baseDataToJson().replace("\\", "")
            if (ConstantValue.isAntox) {
                //var friendKey: FriendKey = FriendKey(ConstantValue.currentRouterId.substring(0, 64))
                //MessageHelper.sendMessageFromKotlin(AppConfig.instance, friendKey, baseDataJson, ToxMessageType.NORMAL)
            }else{
                ToxCoreJni.getInstance().senToxMessage(baseDataJson, ConstantValue.currentRouterId.substring(0, 64))
            }

            var baseData2 = sendData2
            var baseDataJson2 = baseData2.baseDataToJson().replace("\\", "")
            if (ConstantValue.isAntox) {
                //var friendKey: FriendKey = FriendKey(ConstantValue.currentRouterId.substring(0, 64))
                //MessageHelper.sendMessageFromKotlin(AppConfig.instance, friendKey, baseDataJson, ToxMessageType.NORMAL)
            }else{
                ToxCoreJni.getInstance().senToxMessage(baseDataJson2, ConstantValue.currentRouterId.substring(0, 64))
            }
        }
    }
    override fun setupFragmentComponent() {
        DaggerFileEncryptionComponent
                .builder()
                .appComponent((activity!!.application as AppConfig).applicationComponent)
                .fileEncryptionModule(FileEncryptionModule(this))
                .build()
                .inject(this)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
        AppConfig.instance.messageReceiver?.bakAddrUserNumOutCallback = this
        albumMenuRoot.setOnClickListener {
            var intent =  Intent(activity!!, PicEncryptionActivity::class.java)
            startActivity(intent);
        }
        wechatMenu.setOnClickListener {
            var intent =  Intent(activity!!, WeiXinEncryptionActivity::class.java)
            startActivity(intent);
        }
        contactsParent.setOnClickListener {
            if(ContextCompat.checkSelfPermission(AppConfig.instance, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
            {
                var intent =  Intent(activity!!, ContactsEncryptionActivity::class.java)
                startActivity(intent);
            }else{
                toast(R.string.permission_denied)
            }
        }
        messagesParent.setOnClickListener {
            if(ContextCompat.checkSelfPermission(AppConfig.instance, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED)
            {
                var intent =  Intent(activity!!, SMSEncryptionActivity::class.java)
                startActivity(intent);
            }else{
                toast(R.string.permission_denied)
            }
        }
        if(!BuildConfig.isGooglePlay)
        {
            messagesParent.visibility = View.VISIBLE
        }else{
            messagesParent.visibility = View.GONE
        }
        filterMessags.setOnClickListener {

            //PermissionUtils.toPermissionSetting(this@FileEncryptionFragment.context);
            var localIntent = Intent();
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= 9) {
                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                localIntent.setData(Uri.fromParts("package", "com.stratagile.pnrouter", null));
            } else if (Build.VERSION.SDK_INT <= 8) {
                localIntent.setAction(Intent.ACTION_VIEW);
                localIntent.setClassName("com.stratagile.pnrouter", "com.android.settings.InstalledAppDetails");
                localIntent.putExtra("com.android.settings.ApplicationPkgName","com.stratagile.pnrouter");
            }
            startActivity(localIntent);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFileStatusChange(foregroundCallBack: ForegroundCallBack) {
       /* if(foregroundCallBack.isForeground)
        {
            if(!BuildConfig.isGooglePlay)
            {
                mPresenter.getSMSPermission()
            }else{
                mPresenter.getScanPermission()
            }
        }*/
    }
    override fun setPresenter(presenter: FileEncryptionContract.FileEncryptionContractPresenter) {
        mPresenter = presenter as FileEncryptionPresenter
    }

    override fun initDataFromLocal() {

    }

    override fun showProgressDialog() {
        progressDialog.show()
    }

    override fun closeProgressDialog() {
        progressDialog.hide()
    }

    override fun onDestroy() {
        AppConfig.instance.messageReceiver?.bakAddrUserNumOutCallback = null;
        super.onDestroy()
    }
}