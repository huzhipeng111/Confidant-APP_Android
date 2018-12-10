package com.stratagile.pnrouter.ui.activity.router

import android.graphics.Bitmap
import android.os.Bundle
import cn.bingoogolapple.qrcode.core.BGAQRCodeUtil
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder
import com.stratagile.pnrouter.R
import com.stratagile.pnrouter.application.AppConfig
import com.stratagile.pnrouter.base.BaseActivity
import com.stratagile.pnrouter.db.RouterUserEntity
import com.stratagile.pnrouter.ui.activity.router.component.DaggerUserQRCodeComponent
import com.stratagile.pnrouter.ui.activity.router.contract.UserQRCodeContract
import com.stratagile.pnrouter.ui.activity.router.module.UserQRCodeModule
import com.stratagile.pnrouter.ui.activity.router.presenter.UserQRCodePresenter
import com.stratagile.pnrouter.utils.PopWindowUtil
import com.stratagile.pnrouter.utils.RxEncodeTool
import kotlinx.android.synthetic.main.activity_user_qrcode.*
import javax.inject.Inject

/**
 * @author zl
 * @Package com.stratagile.pnrouter.ui.activity.router
 * @Description: $description
 * @date 2018/12/10 17:38:20
 */

class UserQRCodeActivity : BaseActivity(), UserQRCodeContract.View {

    @Inject
    internal lateinit var mPresenter: UserQRCodePresenter
    lateinit var routerUserEntity: RouterUserEntity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        setContentView(R.layout.activity_user_qrcode)
    }
    override fun initData() {
        title.text = resources.getString(R.string.qr_code_business_card)
        routerUserEntity = intent.getParcelableExtra("user")
        tvRouterNameUser.text = String(RxEncodeTool.base64Decode(routerUserEntity.nickName))
        tvShareUser.setOnClickListener { PopWindowUtil.showSharePopWindow(this, tvShareUser) }
        /* createEnglishQRCode = ThreadUtil.Companion.CreateEnglishQRCode(routerUserEntity.routerId, ivQrCode2)
         createEnglishQRCode.execute()*/
        Thread(Runnable() {
            run() {

                var  bitmap: Bitmap =   QRCodeEncoder.syncEncodeQRCode(routerUserEntity.qrcode, BGAQRCodeUtil.dp2px(AppConfig.instance, 150f), AppConfig.instance.getResources().getColor(R.color.mainColor))
                runOnUiThread {
                    ivQrCodeUser.setImageBitmap(bitmap)
                }

            }
        }).start()
       /* tvSaveToPhoneUser.setOnClickListener {
            saveQrCodeToPhone()
        }*/
    }

    override fun setupActivityComponent() {
       DaggerUserQRCodeComponent
               .builder()
               .appComponent((application as AppConfig).applicationComponent)
               .userQRCodeModule(UserQRCodeModule(this))
               .build()
               .inject(this)
    }
    override fun setPresenter(presenter: UserQRCodeContract.UserQRCodeContractPresenter) {
            mPresenter = presenter as UserQRCodePresenter
        }

    override fun showProgressDialog() {
        progressDialog.show()
    }

    override fun closeProgressDialog() {
        progressDialog.hide()
    }

}