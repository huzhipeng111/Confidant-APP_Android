package com.stratagile.pnrouter.ui.activity.main

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Interpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.pawegio.kandroid.runDelayedOnUiThread
import com.pawegio.kandroid.toast
import com.socks.library.KLog
import com.stratagile.pnrouter.R
import com.stratagile.pnrouter.application.AppConfig
import com.stratagile.pnrouter.base.BaseActivity
import com.stratagile.pnrouter.constant.ConstantValue
import com.stratagile.pnrouter.data.web.PNRouterServiceMessageReceiver
import com.stratagile.pnrouter.db.ActiveEntity
import com.stratagile.pnrouter.entity.*
import com.stratagile.pnrouter.telegram.BottomSheet
import com.stratagile.pnrouter.telegram.CubicBezierInterpolator
import com.stratagile.pnrouter.ui.activity.main.component.DaggerActiveListComponent
import com.stratagile.pnrouter.ui.activity.main.contract.ActiveListContract
import com.stratagile.pnrouter.ui.activity.main.module.ActiveListModule
import com.stratagile.pnrouter.ui.activity.main.presenter.ActiveListPresenter
import com.stratagile.pnrouter.ui.adapter.main.ActiveAdapter
import com.stratagile.pnrouter.utils.FireBaseUtils
import com.stratagile.pnrouter.utils.SpUtil
import kotlinx.android.synthetic.main.activity_active_list.*
import org.greenrobot.eventbus.EventBus
import qlc.mng.AccountMng
import qlc.mng.TransactionMng
import javax.inject.Inject

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.main
 * @Description: $description
 * @date 2020/05/19 18:03:37
 */

class ActiveListActivity : BaseActivity(), ActiveListContract.View, PNRouterServiceMessageReceiver.WalletAccountCallback {

    @Inject
    internal lateinit var mPresenter: ActiveListPresenter
    lateinit var activeAdapter: ActiveAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        statusBarColor = R.color.headmainColor
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        setContentView(R.layout.activity_active_list)
    }

    override fun initData() {
        title.setText(getString(R.string.campaign_update))
        activeAdapter = ActiveAdapter(arrayListOf())
        FireBaseUtils.logEvent(this, FireBaseUtils.FIR_CHECK_CAMPAIGN)
        AppConfig.instance.getPNRouterServiceMessageReceiver().walletAccountCallback = this
        recyclerView.adapter = activeAdapter
        getActiveList()
        ivWallet.setOnClickListener {
            if (this::jGetWalletAccountRsp.isInitialized) {
                if (jGetWalletAccountRsp.params.payload != null && jGetWalletAccountRsp.params.payload.size == 2) {
                    if (jGetWalletAccountRsp.params.payload[0].walletType == 1 && jGetWalletAccountRsp.params.payload[1].walletType == 2) {
                        showWallet(jGetWalletAccountRsp.params.payload[0].address, jGetWalletAccountRsp.params.payload[1].address)
                        return@setOnClickListener
                    }
                    if (jGetWalletAccountRsp.params.payload[0].walletType == 2 && jGetWalletAccountRsp.params.payload[1].walletType == 1) {
                        showWallet(jGetWalletAccountRsp.params.payload[1].address, jGetWalletAccountRsp.params.payload[0].address)
                        return@setOnClickListener
                    }
                    showAddWallet()
                } else {
                    closeAnimation()
                    showAddWallet()
                }
            } else {
                getWalletAccount()
            }
        }
        getWalletAccount()
    }


    fun getActiveList() {
        mPresenter.getActiveList()
    }

    override fun setupActivityComponent() {
        DaggerActiveListComponent
                .builder()
                .appComponent((application as AppConfig).applicationComponent)
                .activeListModule(ActiveListModule(this))
                .build()
                .inject(this)
    }

    override fun setPresenter(presenter: ActiveListContract.ActiveListContractPresenter) {
        mPresenter = presenter as ActiveListPresenter
    }

    override fun showProgressDialog() {
        progressDialog.show()
    }

    override fun closeProgressDialog() {
        progressDialog.hide()
    }

    override fun setActiveList(activeList: ActiveList) {
        KLog.i("发消息1。。")
        try {
            activeAdapter.setNewData(activeList.messageList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        KLog.i("发消息2。。")
        AppConfig.instance.mDaoMaster!!.newSession().activeEntityDao.deleteAll()
        KLog.i("发消息3。。")
        activeList.messageList.forEach {
            AppConfig.instance.mDaoMaster!!.newSession().activeEntityDao.insert(ActiveEntity(it.id))
        }
        KLog.i("发消息4。。")
        EventBus.getDefault().post(activeList)
    }

    fun getWalletAccount() {
        var userId = SpUtil.getString(this, ConstantValue.userId, "")
        var getWalletAccountReq = GetWalletAccountReq(userId!!, 0)
        AppConfig.instance.getPNRouterServiceMessageSender().send(BaseData(6, getWalletAccountReq))
    }

    fun bindWalletAddress(neoAddress: String, qlcAddress : String) {
        var userId = SpUtil.getString(this, ConstantValue.userId, "")
        var setWalletAccountReq = SetWalletAccountReq(userId!!, "1,2", neoAddress + "," + qlcAddress)
        AppConfig.instance.getPNRouterServiceMessageSender().send(BaseData(6, setWalletAccountReq))
    }

    fun showWallet(neoAddress: String, qlcAddress: String) {
        closeAnimation()
        val maskView = LayoutInflater.from(this).inflate(R.layout.active_no_wallet_layout, null)
        val builder = BottomSheet.Builder(this, true, 0xfff5f5f5.toInt())
        builder.setApplyTopPadding(false)
        val etNeoWalletAddress = maskView.findViewById<TextView>(R.id.etNeoWalletAddress)
        val etQlcWalletAddress = maskView.findViewById<TextView>(R.id.etQlcWalletAddress)
        val ivReNeoSetWallet = maskView.findViewById<ImageView>(R.id.ivReNeoSetWallet)
        val ivQlcReSetWallet = maskView.findViewById<ImageView>(R.id.ivQlcReSetWallet)
        var hasNext = false
        ivReNeoSetWallet.setOnClickListener {
            hasNext = true
            builder.create().dismiss()
            showAddWallet()
        }
        ivQlcReSetWallet.setOnClickListener {
            hasNext = true
            builder.create().dismiss()
            showAddWallet()
        }
        etNeoWalletAddress.text = neoAddress
        etQlcWalletAddress.text = qlcAddress
        builder.setCustomView(maskView)
        builder.create().setOnDismissListener {
            if (!hasNext) {
                showAnimation()
//                ivWallet.visibility = View.VISIBLE
            }
        }
        builder.create().show()
    }

    var addWalletBottomSheet: BottomSheet? = null
    fun showAddWallet() {
        val maskView = LayoutInflater.from(this).inflate(R.layout.active_reset_wallet_layout, null)
        val builder = BottomSheet.Builder(this, true, 0xfff5f5f5.toInt())
        builder.setApplyTopPadding(false)
        val etNeoWalletAddress = maskView.findViewById<EditText>(R.id.etNeoWalletAddress)
        val etQlcWalletAddress = maskView.findViewById<EditText>(R.id.etQlcWalletAddress)
        val tvAddWallet = maskView.findViewById<TextView>(R.id.tvAddWallet)
        tvAddWallet.setOnClickListener {
            var neoAddress = etNeoWalletAddress.text.toString()
            var qlcAddress = etQlcWalletAddress.text.toString()
            FireBaseUtils.logEvent(this, FireBaseUtils.FIR_ADD_WALLET_ADDRESS)
            if (!AccountMng.isValidAddress(qlcAddress)) {
                toast("This QLC Chain address is invalid")
                return@setOnClickListener
            }
            if (!isValidNeoAddress(neoAddress)) {
                toast("This Nep-5 address in invalid")
                return@setOnClickListener
            }
            showProgressDialog()
            bindWalletAddress(neoAddress, qlcAddress)
        }
        builder.setCustomView(maskView)
        builder.create().setOnDismissListener {
            showAnimation()
        }
        addWalletBottomSheet = builder.create()
        addWalletBottomSheet!!.show()
    }

    fun isValidNeoAddress(address: String) : Boolean{
        if (address.startsWith("A") && address.length == 34) {
            return true
        }
        return false
    }

    protected var openInterpolator: Interpolator = CubicBezierInterpolator.EASE_OUT_QUINT
    fun showAnimation() {
        var currentSheetAnimation = AnimatorSet()
        currentSheetAnimation.playTogether(
                ObjectAnimator.ofFloat(ivWallet, View.ALPHA, 0f, 1f))
        currentSheetAnimation.setDuration(400)
        currentSheetAnimation.setStartDelay(20)
        currentSheetAnimation.setInterpolator(openInterpolator)
        currentSheetAnimation.start()
    }
    fun closeAnimation() {
        var currentSheetAnimation = AnimatorSet()
        currentSheetAnimation.playTogether(
                ObjectAnimator.ofFloat<View>(ivWallet, View.ALPHA, 1f, 0f))
        currentSheetAnimation.setDuration(400)
        currentSheetAnimation.setStartDelay(20)
        currentSheetAnimation.setInterpolator(CubicBezierInterpolator.EASE_IN)
        currentSheetAnimation.start()
    }

    lateinit var jGetWalletAccountRsp: JGetWalletAccountRsp
    override fun getWalletAccountBack(jGetWalletAccountRsp: JGetWalletAccountRsp) {
        KLog.i("设置jGetWalletAccountRsp")
        this.jGetWalletAccountRsp = jGetWalletAccountRsp
    }

    override fun setWalletAccountBack(jSetWalletAccountRsp: JSetWalletAccountRsp) {
        getWalletAccount()
        runDelayedOnUiThread(500) {
            closeProgressDialog()
            addWalletBottomSheet!!.dismiss()
        }
    }

}