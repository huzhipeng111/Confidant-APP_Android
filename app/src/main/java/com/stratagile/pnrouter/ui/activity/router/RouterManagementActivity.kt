package com.stratagile.pnrouter.ui.activity.router

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.pawegio.kandroid.toast
import com.socks.library.KLog
import com.stratagile.pnrouter.BuildConfig
import com.stratagile.pnrouter.R
import com.stratagile.pnrouter.application.AppConfig
import com.stratagile.pnrouter.base.BaseActivity
import com.stratagile.pnrouter.constant.ConstantValue
import com.stratagile.pnrouter.data.web.PNRouterServiceMessageReceiver
import com.stratagile.pnrouter.db.RouterEntity
import com.stratagile.pnrouter.db.RouterUserEntity
import com.stratagile.pnrouter.db.UserEntityDao
import com.stratagile.pnrouter.entity.*
import com.stratagile.pnrouter.entity.events.ConnectStatus
import com.stratagile.pnrouter.entity.events.RouterChange
import com.stratagile.pnrouter.ui.activity.login.LoginActivityActivity
import com.stratagile.pnrouter.ui.activity.router.component.DaggerRouterManagementComponent
import com.stratagile.pnrouter.ui.activity.router.contract.RouterManagementContract
import com.stratagile.pnrouter.ui.activity.router.module.RouterManagementModule
import com.stratagile.pnrouter.ui.activity.router.presenter.RouterManagementPresenter
import com.stratagile.pnrouter.ui.activity.scan.ScanQrCodeActivity
import com.stratagile.pnrouter.ui.activity.user.EditNickNameActivity
import com.stratagile.pnrouter.ui.activity.user.UserAccoutCodeActivity
import com.stratagile.pnrouter.ui.adapter.router.RouterListAdapter
import com.stratagile.pnrouter.utils.*
import com.stratagile.pnrouter.view.CommonDialog
import com.stratagile.pnrouter.view.SweetAlertDialog
import com.stratagile.tox.toxcore.KotlinToxService
import com.stratagile.tox.toxcore.ToxCoreJni
import com.stratagile.pnrouter.entity.events.ToxStatusEvent
import com.stratagile.pnrouter.statusbar.StatusBarCompat
import kotlinx.android.synthetic.main.activity_router_management.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.router
 * @Description: $description
 * @date 2018/09/26 10:29:17
 */

class RouterManagementActivity : BaseActivity(), RouterManagementContract.View, PNRouterServiceMessageReceiver.GetDiskTotalInfoBack, PNRouterServiceMessageReceiver.ResetRouterNameCallBack, PNRouterServiceMessageReceiver.QlcNodeCallBack {
    override fun enableQlcNodeRsp(jEnableQlcNodeRsp: JEnableQlcNodeRsp) {
        runOnUiThread {
            closeProgressDialog()
        }
        if(jEnableQlcNodeRsp.params.retCode == 0)
        {
            runOnUiThread {
                toast(R.string.success)
            }
        }else if(jEnableQlcNodeRsp.params.retCode == 0)
        {
            runOnUiThread {
                if(enableQlcNodeSwitch.isChecked)
                {
                    enableQlcNodeSwitch.isChecked = false
                }
                toast("Node disk space is insufficient")
            }
        }else{
            runOnUiThread {
                if(enableQlcNodeSwitch.isChecked)
                {
                    enableQlcNodeSwitch.isChecked = false
                }
                toast("No authority")
            }
        }
    }

    override fun checkQlcNodeRsp(jCheckQlcNodeRsp: JCheckQlcNodeRsp) {

        if(jCheckQlcNodeRsp.params.retCode == 0)
        {
            runOnUiThread {
                enableQlcNodeSwitch.isChecked = jCheckQlcNodeRsp.params.status == 1
                enableQlcNode.visibility = View.VISIBLE
            }

        }else{
            runOnUiThread {
                enableQlcNode.visibility = View.GONE
                if(BuildConfig.DEBUG)
                {
                    enableQlcNode.visibility = View.VISIBLE
                }
            }

        }
    }

    override fun pullTmpAccount(jPullTmpAccountRsp: JPullTmpAccountRsp) {
        if(jPullTmpAccountRsp.params.retCode == 0)
        {
            runOnUiThread {
                var intent = Intent(this, ShareTempQRCodeActivity::class.java)
                var routerUserEntity = RouterUserEntity()
                routerUserEntity.userSN = jPullTmpAccountRsp.params.userSN
                routerUserEntity.userType = 1
                routerUserEntity.active = 0
                routerUserEntity.identifyCode = "0"
                routerUserEntity.mnemonic = ""
                routerUserEntity.nickName = ""
                routerUserEntity.userId = ""
                routerUserEntity.lastLoginTime = 0
                routerUserEntity.qrcode = jPullTmpAccountRsp.params.qrcode
                intent.putExtra("user", routerUserEntity)
                startActivity(intent)
                closeProgressDialog()
            }
        }else{
            runOnUiThread {
                closeProgressDialog()
                toast(R.string.error)
            }
        }
    }

    override fun ResetRouterName(jResetRouterNameRsp: JResetRouterNameRsp) {

        runOnUiThread {
            closeProgressDialog()
        }
        if (jResetRouterNameRsp.params.retCode == 0) {
            KLog.i("修改圈子名字成功")
            selectedRouter.routerName = tvRouterName.text.toString()
            selectedRouter.routerAlias = tvRouterName.text.toString()
            AppConfig.instance.mDaoMaster?.newSession()?.routerEntityDao?.update(selectedRouter)
            var userList = AppConfig.instance.mDaoMaster!!.newSession().userEntityDao.queryBuilder().where(UserEntityDao.Properties.RouteId.eq(selectedRouter.routerId)).list()
            userList.forEach {
                it.routeName = tvRouterName.text.toString()
                it.routerAlias = tvRouterName.text.toString()
                AppConfig.instance.mDaoMaster!!.newSession().userEntityDao.update(it)
            }
            LocalRouterUtils.insertLocalAssets(MyRouter(0, selectedRouter))
            EventBus.getDefault().post(RouterChange())
        } else if (jResetRouterNameRsp.params.retCode == 1) {
            runOnUiThread {
                toast("No authority")
            }
        } else {
            runOnUiThread {
                toast("Other mistakes")
            }
        }

    }

    override fun getDiskTotalInfoReq(JGetDiskTotalInfoRsp: JGetDiskTotalInfoRsp) {
        if (JGetDiskTotalInfoRsp.params.retCode == 0) {
            var usedCapacity = 0.0
            if (JGetDiskTotalInfoRsp.params.usedCapacity.contains("M")) {
                usedCapacity = JGetDiskTotalInfoRsp.params.usedCapacity.replace("M", "").toDouble() * 100
            } else if (JGetDiskTotalInfoRsp.params.usedCapacity.contains("G")) {
                usedCapacity = JGetDiskTotalInfoRsp.params.usedCapacity.replace("G", "").toDouble() * 1024 * 100
            } else if (JGetDiskTotalInfoRsp.params.usedCapacity.contains("T")) {
                usedCapacity = JGetDiskTotalInfoRsp.params.usedCapacity.replace("T", "").toDouble() * 1024 * 1024 * 100
            }
            var totalCapacity = 1.0
            if (JGetDiskTotalInfoRsp.params.totalCapacity.contains("M")) {
                totalCapacity = JGetDiskTotalInfoRsp.params.totalCapacity.replace("M", "").toDouble()
            } else if (JGetDiskTotalInfoRsp.params.totalCapacity.contains("G")) {
                totalCapacity = JGetDiskTotalInfoRsp.params.totalCapacity.replace("G", "").toDouble() * 1024
            } else if (JGetDiskTotalInfoRsp.params.totalCapacity.contains("T")) {
                totalCapacity = JGetDiskTotalInfoRsp.params.totalCapacity.replace("T", "").toDouble() * 1024 * 1024
            }
            var precent = (usedCapacity / totalCapacity).toString()
            if (precent.length > 4) {
                precent = precent.substring(0, 4)
            }
            runOnUiThread {
                progressBar.progress = (usedCapacity / totalCapacity).toInt()
                UsedAndTotal.text = JGetDiskTotalInfoRsp.params.usedCapacity + " / " + JGetDiskTotalInfoRsp.params.totalCapacity + " (" + precent + "% )"
//                storage.text =  getString(R.string.Used_Sapce) + JGetDiskTotalInfoRsp.params.totalCapacity
            }
        } else {
            runOnUiThread {
                toast(R.string.system_busy)
            }
        }
    }

    override fun formatDiskReq(jFormatDiskRsp: JFormatDiskRsp) {

    }

    @Inject
    internal lateinit var mPresenter: RouterManagementPresenter

    lateinit var routerListAdapter: RouterListAdapter

    val circleAlias1 = 2
    val circleMember1 = 3
    val circleName1 = 4
    var rebootting:CommonDialog ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        needFront = true
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        setContentView(R.layout.activity_router_management)
        EventBus.getDefault().register(this)
        showViewNeedFront()
        StatusBarCompat.cancelLightStatusBar(this)
        userAvatar.withShape = true
        userAvatar.setText(SpUtil.getString(this, ConstantValue.username, "")!!)
        ivBack.setOnClickListener {
            finish()
        }
    }

    var selectedRouter = RouterEntity()
    override fun initData() {
        title.text = getString(R.string.routerManagement)
        try {
            AppConfig.instance.messageReceiver!!.resetRouterNameCallBack = this
            AppConfig.instance.messageReceiver!!.qlcNodeCallBack = this
        }catch (e:Exception)
        {

        }
        var msgData = CheckQlcNode()
        if (ConstantValue.isWebsocketConnected) {
            AppConfig.instance.getPNRouterServiceMessageSender().send(BaseData(6, msgData))
        } else if (ConstantValue.isToxConnected) {
            var baseData = BaseData(6, msgData)
            var baseDataJson = baseData.baseDataToJson().replace("\\", "")
            if (ConstantValue.isAntox) {
                //var friendKey: FriendKey = FriendKey(ConstantValue.currentRouterId.substring(0, 64))
                //MessageHelper.sendMessageFromKotlin(AppConfig.instance, friendKey, baseDataJson, ToxMessageType.NORMAL)
            } else {
                ToxCoreJni.getInstance().senToxMessage(baseDataJson, ConstantValue.currentRouterId.substring(0, 64))
            }
        }
        var routerList = AppConfig.instance.mDaoMaster!!.newSession().routerEntityDao.loadAll()
        routerList.forEach {
            if (it.lastCheck) {
                selectedRouter = it
                return@forEach
            }
        }
        routerList.remove(selectedRouter)
        AppConfig.instance.messageReceiver?.getDiskTotalInfoBack = this
        if (ConstantValue.currentRouterSN != null && ConstantValue.isCurrentRouterAdmin && ConstantValue.currentRouterSN.equals(selectedRouter.userSn)) {//管理员才调用此接口
            var msgData = GetDiskTotalInfoReq()
            var fileBase58Name = Base58.encode(RxEncodeTool.base64Decode(ConstantValue.libsodiumpublicSignKey)) + ".jpg"
            userAvatar.setImageFile(fileBase58Name)
            if (ConstantValue.isWebsocketConnected) {
                AppConfig.instance.getPNRouterServiceMessageSender().send(BaseData(3, msgData))
            } else if (ConstantValue.isToxConnected) {
                var baseData = BaseData(3, msgData)
                var baseDataJson = baseData.baseDataToJson().replace("\\", "")
                if (ConstantValue.isAntox) {
                    //var friendKey: FriendKey = FriendKey(ConstantValue.currentRouterId.substring(0, 64))
                    //MessageHelper.sendMessageFromKotlin(AppConfig.instance, friendKey, baseDataJson, ToxMessageType.NORMAL)
                } else {
                    ToxCoreJni.getInstance().senToxMessage(baseDataJson, ConstantValue.currentRouterId.substring(0, 64))
                }
            }
        }

        tvRouterName.text = selectedRouter.routerName
        if (ConstantValue.curreantNetworkType.equals("TOX")) {

        } else {
            if (ConnectStatus.currentStatus == 0) {
            } else if (ConnectStatus.currentStatus == 1) {
            } else if (ConnectStatus.currentStatus == 2) {
            }
        }
        if (ConstantValue.currentRouterSN != null && !ConstantValue.currentRouterSN.equals("")&& ConstantValue.isCurrentRouterAdmin && ConstantValue.currentRouterSN.equals(selectedRouter.userSn)) {
            setUI(true)
        } else {
            if(BuildConfig.DEBUG)
            {
                setUI(true)
            }else{
                setUI(false)
            }
        }
        if (selectedRouter.routerAlias == null || "".equals(selectedRouter.routerAlias)) {
            circleAlias.tvContent.text = selectedRouter.routerName
        } else {
            circleAlias.tvContent.text = selectedRouter.routerAlias
        }
        circleAlias.setOnClickListener {
            var intent = Intent(this, EditNickNameActivity::class.java)
            intent.putExtra("flag", "Alias")
            intent.putExtra("hint", "Edit alias")
            intent.putExtra("alias", circleAlias.tvContent.text.toString())
            startActivityForResult(intent, circleAlias1)
        }
        circleMembers.setOnClickListener {
            var intent = Intent(this, RouterAddUserActivity::class.java)
            intent.putExtra("userEntity", selectedRouter)
            startActivityForResult(intent, circleMember1)
        }
        addMembers.setOnClickListener {
            /* var intent = Intent(this, RouterQRCodeActivity::class.java)
             intent.putExtra("router", selectedRouter)
             startActivity(intent)*/
            /*var intent = Intent(this, RouterCreateUserActivity::class.java)
            intent.putExtra("routerUserEntity", selectedRouter)
            startActivityForResult(intent, 0)*/
            showProgressDialog("waiting...")
            val userId = SpUtil.getString(AppConfig.instance, ConstantValue.userId, "")
//            var IdentifyCode = IdentifyCode.text.toString().trim()
            var pullTmpAccountReq = PullTmpAccountReq(userId!!)
            if(ConstantValue.isWebsocketConnected)
            {
                AppConfig.instance.getPNRouterServiceMessageSender().send(BaseData(4,pullTmpAccountReq))
            }
            else if(ConstantValue.isToxConnected)
            {
                var baseData = BaseData(4,pullTmpAccountReq)
                var baseDataJson = baseData.baseDataToJson().replace("\\", "")
                if (ConstantValue.isAntox) {
                    //var friendKey: FriendKey = FriendKey(ConstantValue.scanRouterId.substring(0, 64))
                    //MessageHelper.sendMessageFromKotlin(AppConfig.instance, friendKey, baseDataJson, ToxMessageType.NORMAL)
                }else{
                    ToxCoreJni.getInstance().senToxMessage(baseDataJson, ConstantValue.scanRouterId.substring(0, 64))
                }
            }
        }
        circleName.tvContent.text = selectedRouter.routerName
        circleName.setOnClickListener {
            var intent = Intent(this, EditNickNameActivity::class.java)
            intent.putExtra("flag", "Name")
            intent.putExtra("hint", "Edit Circle Name")
            intent.putExtra("alias", selectedRouter.routerName)
            startActivityForResult(intent, circleName1)
        }

        qrCode.setOnClickListener {
            /*var intent = Intent(this, RouterQRCodeActivity::class.java)
            intent.putExtra("router", selectedRouter)
            startActivity(intent)*/

            var intent = Intent(this, UserAccoutCodeActivity::class.java)
            intent.putExtra("router", selectedRouter)
            startActivity(intent)
        }

        var autoLoginRouterSn = SpUtil.getString(this, ConstantValue.autoLoginRouterSn, "")
        if(autoLoginRouterSn.equals(""))
        {
            autoLoginSwitch.isChecked = false
        }else{
            autoLoginSwitch.isChecked = autoLoginRouterSn.equals(selectedRouter.userSn)
        }

        autoLoginSwitch.setOnClickListener {
            if (autoLoginSwitch.isChecked) {
                SpUtil.putString(this, ConstantValue.autoLoginRouterSn, selectedRouter.userSn)
            } else {
                SpUtil.putString(this, ConstantValue.autoLoginRouterSn, "no")
            }
        }

        enableQlcNodeSwitch.setOnClickListener {
            var open = 1;
            if (enableQlcNodeSwitch.isChecked) {
                open = 1
            } else {
                open = 0
            }
            showProgressDialog("wait…")
            var msgData = EnableQlcNode(open,"")
            if (ConstantValue.isWebsocketConnected) {
                AppConfig.instance.getPNRouterServiceMessageSender().send(BaseData(6, msgData))
            } else if (ConstantValue.isToxConnected) {
                var baseData = BaseData(6, msgData)
                var baseDataJson = baseData.baseDataToJson().replace("\\", "")
                if (ConstantValue.isAntox) {
                    //var friendKey: FriendKey = FriendKey(ConstantValue.currentRouterId.substring(0, 64))
                    //MessageHelper.sendMessageFromKotlin(AppConfig.instance, friendKey, baseDataJson, ToxMessageType.NORMAL)
                } else {
                    ToxCoreJni.getInstance().senToxMessage(baseDataJson, ConstantValue.currentRouterId.substring(0, 64))
                }
            }
        }
        manageDisk.setOnClickListener {
            startActivity(Intent(this, DiskManagementActivity::class.java))
        }
        rebootCircle.setOnClickListener {
            showDialog()
        }
        selectCircle.setOnClickListener {
            startActivityForResult(Intent(this, SelectCircleActivity::class.java), 5)
        }
    }
    fun showDialog() {
        SweetAlertDialog(this, SweetAlertDialog.BUTTON_NEUTRAL)
                .setContentText(getString(R.string.restart))
                .setConfirmClickListener {
                    showProgressDialog()
                    closeProgressDialog()
                    rebotRouter()
                }
                .show()

    }
    private fun  rebotRouter()
    {
        var msgData = RebootReq()
        if (ConstantValue.isWebsocketConnected) {
            AppConfig.instance.getPNRouterServiceMessageSender().send(BaseData(3, msgData))
        } else if (ConstantValue.isToxConnected) {
            var baseData = BaseData(3, msgData)
            var baseDataJson = baseData.baseDataToJson().replace("\\", "")
            if (ConstantValue.isAntox) {
                //var friendKey: FriendKey = FriendKey(ConstantValue.currentRouterId.substring(0, 64))
                //MessageHelper.sendMessageFromKotlin(AppConfig.instance, friendKey, baseDataJson, ToxMessageType.NORMAL)
            } else {
                ToxCoreJni.getInstance().senToxMessage(baseDataJson, ConstantValue.currentRouterId.substring(0, 64))
            }
        }
        showRebootting()
        Thread(Runnable() {
            run() {
                Thread.sleep(5000)
                hideRebootting()
                ConstantValue.isHasWebsocketInit = true
                /* if(AppConfig.instance.messageReceiver != null)
                     AppConfig.instance.messageReceiver!!.close()*/

                ConstantValue.loginOut = true
                ConstantValue.logining = false
                ConstantValue.isHeart = false
                //isUserExit = true
                resetUnCompleteFileRecode()
                if (ConstantValue.isWebsocketConnected) {
                    FileMangerDownloadUtils.init()
                    ConstantValue.webSockeFileMangertList.forEach {
                        it.disconnect(true)
                        //ConstantValue.webSockeFileMangertList.remove(it)
                    }
                    ConstantValue.webSocketFileList.forEach {
                        it.disconnect(true)
                        //ConstantValue.webSocketFileList.remove(it)
                    }
                }else{
                    val intentTox = Intent(this, KotlinToxService::class.java)
                    this.stopService(intentTox)
                }
                ConstantValue.isWebsocketConnected = false
                ConstantValue.loginReq = null
                ConstantValue.isWebsocketReConnect = false
                AppConfig.instance.mAppActivityManager.finishAllActivityWithoutThis()
                var intent = Intent(this, LoginActivityActivity::class.java)
                intent.putExtra("flag", "logout")
                startActivity(intent)
                finish()
            }
        }).start()
    }
    private fun showRebootting() {
        val view = View.inflate(this, R.layout.layout_format, null)
        //取消或确定按钮监听事件处l
        rebootting = CommonDialog(this)
        var content = view.findViewById<TextView>(R.id.content)
        content.text = getString(R.string.rebooting)
        val window = rebootting?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        rebootting?.setView(view)
        rebootting?.show()
    }
    private fun hideRebootting() {
        rebootting?.dismiss()
    }
    fun resetUnCompleteFileRecode()
    {
        var localFilesList = LocalFileUtils.localFilesList
        for (myFie in localFilesList)
        {
            if(myFie.upLoadFile.isComplete == false)
            {
                myFie.upLoadFile.SendGgain = true
                myFie.upLoadFile.isStop = "1"
                myFie.upLoadFile.segSeqResult = 0
                val myRouter = MyFile()
                myRouter.type = 0
                myRouter.userSn = ConstantValue.currentRouterSN
                myRouter.upLoadFile = myFie.upLoadFile
                LocalFileUtils.updateLocalAssets(myRouter)
            }
        }
    }
    fun setUI(isManager: Boolean) {
        if (isManager) {
            circleAlias.visibility = View.GONE

            circleName.visibility = View.VISIBLE
            circleMembers.visibility = View.VISIBLE
            addMembers.visibility = View.GONE
            userdSpace.visibility = View.VISIBLE
            manageDisk.visibility = View.VISIBLE
            enableQlcNode.visibility  = View.VISIBLE
            rebootCircle.visibility  = View.VISIBLE
        } else {
            circleAlias.visibility = View.GONE

            circleName.visibility = View.GONE
            circleMembers.visibility = View.GONE
            addMembers.visibility = View.GONE
            userdSpace.visibility = View.GONE
            manageDisk.visibility = View.GONE
            enableQlcNode.visibility  = View.GONE
            rebootCircle.visibility  = View.GONE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun routerChange(routerChange: RouterChange) {
        initData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onToxConnected(toxStatusEvent: ToxStatusEvent) {
        when (toxStatusEvent.status) {
            0 -> {
            }
            1 -> {
            }
            2 -> {
            }
            3 -> {
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun connectStatusChange(statusChange: ConnectStatus) {
        //连接状态，0已经连接，1正在连接，2未连接
        if (statusChange.status == 0) {
        } else if (statusChange.status == 1) {
        } else if (statusChange.status == 2) {
        } else if (statusChange.status == 3) {
        }
    }

    override fun onResume() {
        super.onResume()
//        initData()
    }

    override fun onDestroy() {
        try {
            EventBus.getDefault().unregister(this)
            AppConfig.instance.messageReceiver!!.resetRouterNameCallBack = null
            AppConfig.instance.messageReceiver!!.getDiskDetailInfoBack = null
            AppConfig.instance.messageReceiver!!.qlcNodeCallBack = null
        }catch (e:Exception)
        {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    private var isCanShotNetCoonect = true
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun connectNetWorkStatusChange(statusChange: ConnectStatus) {
        when (statusChange.status) {
            0 -> {
                progressDialog.hide()
                isCanShotNetCoonect = true
            }
            1 -> {

            }
            2 -> {
                if (isCanShotNetCoonect) {
                    //showProgressDialog(getString(R.string.network_reconnecting))
                    isCanShotNetCoonect = false
                }
            }
            3 -> {
                if (isCanShotNetCoonect) {
                    //showProgressDialog(getString(R.string.network_reconnecting))
                    isCanShotNetCoonect = false
                }
            }
        }
    }

    override fun setupActivityComponent() {
        DaggerRouterManagementComponent
                .builder()
                .appComponent((application as AppConfig).applicationComponent)
                .routerManagementModule(RouterManagementModule(this))
                .build()
                .inject(this)
    }

    override fun setPresenter(presenter: RouterManagementContract.RouterManagementContractPresenter) {
        mPresenter = presenter as RouterManagementPresenter
    }

    override fun showProgressDialog() {
        progressDialog.show()
    }

    override fun closeProgressDialog() {
        progressDialog.hide()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // menuInflater.inflate(R.menu.qr_code, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.qrCode -> {
                mPresenter.getScanPermission()
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getScanPermissionSuccess() {
        var intent = Intent(this, ScanQrCodeActivity::class.java)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            var routerList = AppConfig.instance.mDaoMaster!!.newSession().routerEntityDao.loadAll()
            routerList.forEach {
                if (it.routerId.equals(data!!.getStringExtra("result"))) {
                    toast("this router has been added")
                    return
                }
            }
            var routerEntity = RouterEntity()
            routerEntity.routerName = ("Router " + (routerList.size + 1))
            routerEntity.routerId = data!!.getStringExtra("result")
            routerEntity.username = ""
            routerEntity.lastCheck = false
            AppConfig.instance.mDaoMaster!!.newSession().routerEntityDao.insert(routerEntity)
            initData()
        }
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            selectedRouter.routerAlias = data!!.getStringExtra("alias")
            AppConfig.instance.mDaoMaster!!.newSession().routerEntityDao.update(selectedRouter)
            var userList = AppConfig.instance.mDaoMaster!!.newSession().userEntityDao.queryBuilder().where(UserEntityDao.Properties.RouteId.eq(selectedRouter.routerId)).list()
            userList.forEach {
                it.routerAlias = data!!.getStringExtra("alias")
                AppConfig.instance.mDaoMaster!!.newSession().userEntityDao.update(it)
            }
            LocalRouterUtils.insertLocalAssets(MyRouter(0, selectedRouter))
            initData()
            EventBus.getDefault().post(RouterChange())
        }
        if (requestCode == circleMember1 && resultCode == Activity.RESULT_OK) {
            //修改圈子成员
            initData()
        }
        if (requestCode == circleName1 && resultCode == Activity.RESULT_OK) {
            //修改圈子名字
            var routerNameStr = data!!.getStringExtra("alias")
            KLog.i("想要修改的名字为：" + routerNameStr)
            tvRouterName.text = routerNameStr
            circleName.tvContent.text = routerNameStr

            showProgressDialog("waiting...")
            var resetRouterNameReq = ResetRouterNameReq(selectedRouter.routerId, RxEncodeTool.base64Encode2String(routerNameStr.toByteArray()))
            if (ConstantValue.isWebsocketConnected) {
                AppConfig.instance.getPNRouterServiceMessageSender().send(BaseData(4, resetRouterNameReq))
            } else if (ConstantValue.isToxConnected) {
                var baseData = BaseData(4, resetRouterNameReq)
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