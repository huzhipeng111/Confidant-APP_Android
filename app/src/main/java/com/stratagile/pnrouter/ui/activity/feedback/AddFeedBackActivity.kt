package com.stratagile.pnrouter.ui.activity.feedback

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.pawegio.kandroid.toast
import com.smailnet.eamil.Utils.AESCipher
import com.socks.library.KLog
import com.stratagile.pnrouter.R
import com.stratagile.pnrouter.application.AppConfig
import com.stratagile.pnrouter.base.BaseActivity
import com.stratagile.pnrouter.constant.ConstantValue
import com.stratagile.pnrouter.ui.activity.feedback.component.DaggerAddFeedBackComponent
import com.stratagile.pnrouter.ui.activity.feedback.contract.AddFeedBackContract
import com.stratagile.pnrouter.ui.activity.feedback.module.AddFeedBackModule
import com.stratagile.pnrouter.ui.activity.feedback.presenter.AddFeedBackPresenter
import com.stratagile.pnrouter.ui.adapter.feedback.FeedbackChooseImageAdapter
import com.stratagile.pnrouter.ui.adapter.feedback.FeedbackChooseImageItemDecoration
import com.stratagile.pnrouter.ui.adapter.feedback.FeedbackSelectImageBean
import com.stratagile.pnrouter.utils.RxEncodeTool
import com.stratagile.pnrouter.utils.SpUtil
import kotlinx.android.synthetic.main.activity_add_feed_back.*
import me.shouheng.compress.Compress
import me.shouheng.compress.listener.CompressListener
import me.shouheng.compress.strategy.Strategies
import me.shouheng.compress.strategy.config.ScaleMode
import net.lucode.hackware.magicindicator.buildins.UIUtil
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.feedback
 * @Description: $description
 * @date 2020/06/08 15:57:42
 */

class AddFeedBackActivity : BaseActivity(), AddFeedBackContract.View {

    @Inject
    internal lateinit var mPresenter: AddFeedBackPresenter

    lateinit var feedbackChooseAdapter: FeedbackChooseImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        isEditActivity = true
        statusBarColor = R.color.white
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        setContentView(R.layout.activity_add_feed_back)
        var orginBean = FeedbackSelectImageBean()
        feedbackChooseAdapter = FeedbackChooseImageAdapter(arrayListOf(orginBean))
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        recyclerView.addItemDecoration(FeedbackChooseImageItemDecoration(UIUtil.dip2px(this, 15.toDouble())))
        feedbackChooseAdapter.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.ivContent -> {
                    if (feedbackChooseAdapter.data[position].filePath == null) {
                        selectPicFromLocal()
                    }
                }
                R.id.ivRemove -> {
                    feedbackChooseAdapter.remove(position)
                }
            }
        }
        recyclerView.adapter = feedbackChooseAdapter
        hideKeyboard()
        etFeedbackDesc.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length > 200) {
                    etFeedbackDesc.setText(s.toString().substring(0, 200))
                    return
                }
                tvFeedbackDescCount.text = s.toString().length.toString() + "/200"
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        send.setOnClickListener {
            var scenario = tvFeedbackScenario.text.toString()
            var type = tvFeedbackType.text.toString()
            var userId = SpUtil.getString(this, ConstantValue.userId, "")!!
            var userName = SpUtil.getString(this, ConstantValue.username, "")!!
            var publicKey = ConstantValue.libsodiumpublicSignKey!!

            var nickName = SpUtil.getString(this, ConstantValue.username, "")
            val selfNickNameBase64 = RxEncodeTool.base64Encode2String(nickName!!.toByteArray())

            var aesSourceStr = ConstantValue.currentRouterSN.substring(0,6)+","+ConstantValue.libsodiumpublicSignKey!!+","+ConstantValue.currentRouterId+","+selfNickNameBase64+","+userId
            var routerCodeDataByte = aesSourceStr.toByteArray();
            var base64Str = AESCipher.aesEncryptBytesToBase64(routerCodeDataByte,"welcometoqlc0101".toByteArray())
            var qrCode = "type_5,"+base64Str
            KLog.i(qrCode)
            var email = etEmail.text.toString().trim()
            var question = etFeedbackDesc.text.toString().trim()
            if ("".equals(question)) {
                toast("question can not be empty!")
                return@setOnClickListener
            }
            showProgressDialog()
            var list = mutableListOf<MultipartBody.Part>()
            feedbackChooseAdapter.data.forEach {
                if (it.part != null) {
                    list.add(it.part)
                }
            }
            var infoMap = hashMapOf<String, String>()
            infoMap["scenario"] = scenario
            infoMap["type"] = type
            infoMap["userId"] = userId
            infoMap["userName"] = userName
            infoMap["publicKey"] = publicKey
            infoMap["nickName"] = nickName
            infoMap["qrCode"] = qrCode
            infoMap["email"] = email
            infoMap["question"] = question
            mPresenter.subMit(list, scenario, type, userId, userName, publicKey, qrCode, email, question)
//            mPresenter.subMit2(list, infoMap)
        }
    }

    override fun submitBack() {
        closeProgressDialog()
        setResult(Activity.RESULT_OK)
        toast(getString(R.string.success))
        finish()
    }

    protected fun hideKeyboard() {
        var inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null) inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }


    protected fun selectPicFromLocal() {
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofAll())
                .maxSelectNum(9)
                .minSelectNum(1)
                .imageSpanCount(3)
                .selectionMode(PictureConfig.SINGLE)
                .previewImage(true)
                .previewVideo(true)
                .enablePreviewAudio(false)
                .isCamera(false)
                .imageFormat(PictureMimeType.PNG)
                .isZoomAnim(true)
                .sizeMultiplier(0.5f)
                .setOutputCameraPath("/CustomPath")
                .enableCrop(false)
                .compress(false)
                .glideOverride(160, 160)
                .hideBottomControls(false)
                .isGif(false)
                .openClickSound(false)
                .minimumCompressSize(100)
                .synOrAsy(true)
                .rotateEnabled(true)
                .scaleEnabled(true)
                .videoMaxSecond(60 * 60 * 3)
                .videoMinSecond(1)
                .isDragFrame(false)
                .forResult(2)
    }

    override fun initData() {
        title.text = getString(R.string.feedback)
        tvFeedbackScenario.setOnClickListener {
            var intent = Intent(this, FeedbackChooseActivity::class.java)
            intent.putExtra("title", "Choose a scenario")
            intent.putExtra("content", "scenario")
            startActivityForResult(intent, 1)
        }
        tvFeedbackType.setOnClickListener {
            var intent = Intent(this, FeedbackChooseActivity::class.java)
            intent.putExtra("title", "Choose a type")
            intent.putExtra("content", "type")
            startActivityForResult(intent, 1)
        }
        if (ConstantValue.currentEmailConfigEntity != null) {
            etEmail.setText(ConstantValue.currentEmailConfigEntity!!.account)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data!!.getStringExtra("content").equals("scenario")) {
                tvFeedbackScenario.text = data.getStringExtra("result")
            }
            if (data!!.getStringExtra("content").equals("type")) {
                tvFeedbackType.text = data.getStringExtra("result")
            }
        }
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            val list = data!!.getParcelableArrayListExtra<LocalMedia>(PictureConfig.EXTRA_RESULT_SELECTION)
            if (list.size > 0) {
                showProgressDialog()
                compress(list)
//                var path = list.get(0).path
//                var name = list.get(0).path.substring(list.get(0).path.lastIndexOf("/") + 1, list.get(0).path.length)
//                val upLoadFile = File(path)
//                val image = RequestBody.create(MediaType.parse("image/jpg"), upLoadFile)
//                val photo = MultipartBody.Part.createFormData("feedbackImages", name, image)
//                feedbackChooseAdapter.data.last().filePath = path
//                feedbackChooseAdapter.data.last().part = photo
//                feedbackChooseAdapter.data.last().name = name
//                feedbackChooseAdapter.notifyItemChanged(feedbackChooseAdapter.data.size - 1)
//                feedbackChooseAdapter.addData(FeedbackSelectImageBean())
            }
        }
    }

    fun compress(list: ArrayList<LocalMedia>) {
        var path = list.get(0).path
        var name = list.get(0).path.substring(list.get(0).path.lastIndexOf("/") + 1, list.get(0).path.length)
        val upLoadFile = File(path)
        val compress = Compress.with(this, upLoadFile)
        compress
                // 指定要求的图片的质量
                .setQuality(60)
                // 指定文件的输出目录（如果返回结果不是 File 的会，无效）
                .setTargetDir(Environment.getExternalStorageDirectory().toString() + ConstantValue.localPath)
                // 指定压缩结果回调（如哦返回结果不是 File 则不会被回调到）
                .setCompressListener(object : CompressListener {
                    override fun onError(throwable: Throwable) {
                        throwable.printStackTrace()
                    }

                    override fun onStart() {
                        KLog.i("开始")
                    }

                    override fun onSuccess(result: File) {
                        KLog.i("完成")
                        runOnUiThread {
                            closeProgressDialog()
                            val image = RequestBody.create(MediaType.parse("image/jpg"), result)
                            val photo = MultipartBody.Part.createFormData("feedbackImages", name, image)
                            feedbackChooseAdapter.data.last().filePath = path
                            feedbackChooseAdapter.data.last().part = photo
                            feedbackChooseAdapter.data.last().name = name
                            feedbackChooseAdapter.notifyItemChanged(feedbackChooseAdapter.data.size - 1)
                            if (feedbackChooseAdapter.data.size < 4) {
                                feedbackChooseAdapter.addData(FeedbackSelectImageBean())
                            }
                        }
                    }
                })
        val compressor = compress
                .strategy(Strategies.compressor())
                .setConfig(Bitmap.Config.ARGB_8888)
//                .setMaxHeight(100f)
//                .setMaxWidth(100f)
                .setScaleMode(ScaleMode.SCALE_SMALLER)
        compressor.launch()
    }

    override fun setupActivityComponent() {
        DaggerAddFeedBackComponent
                .builder()
                .appComponent((application as AppConfig).applicationComponent)
                .addFeedBackModule(AddFeedBackModule(this))
                .build()
                .inject(this)
    }

    override fun setPresenter(presenter: AddFeedBackContract.AddFeedBackContractPresenter) {
        mPresenter = presenter as AddFeedBackPresenter
    }

    override fun showProgressDialog() {
        progressDialog.show()
    }

    override fun closeProgressDialog() {
        progressDialog.hide()
    }

}