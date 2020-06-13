package com.stratagile.pnrouter.ui.activity.feedback

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import com.pawegio.kandroid.d
import com.previewlibrary.GPreviewBuilder
import com.socks.library.KLog
import com.stratagile.pnrouter.R
import com.stratagile.pnrouter.application.AppConfig
import com.stratagile.pnrouter.base.BaseActivity
import com.stratagile.pnrouter.constant.ConstantValue
import com.stratagile.pnrouter.entity.FeedbackList
import com.stratagile.pnrouter.entity.ImageUrl
import com.stratagile.pnrouter.telegram.BottomSheet
import com.stratagile.pnrouter.ui.activity.feedback.component.DaggerFeedbackDetailComponent
import com.stratagile.pnrouter.ui.activity.feedback.contract.FeedbackDetailContract
import com.stratagile.pnrouter.ui.activity.feedback.module.FeedbackDetailModule
import com.stratagile.pnrouter.ui.activity.feedback.presenter.FeedbackDetailPresenter
import com.stratagile.pnrouter.ui.activity.router.UserFragment
import com.stratagile.pnrouter.ui.adapter.feedback.FeedbackChooseImageItemDecoration
import com.stratagile.pnrouter.ui.adapter.feedback.FeedbackDetailItemListAdapter
import com.stratagile.pnrouter.ui.adapter.feedback.FeedbackImageAdapter
import com.stratagile.pnrouter.utils.SpUtil
import com.stratagile.pnrouter.utils.UIUtils
import kotlinx.android.synthetic.main.activity_feedback_detail.*
import javax.inject.Inject


/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.feedback
 * @Description: $description
 * @date 2020/06/12 09:37:41
 * https://github.com/yangchaojiang/ZoomPreviewPicture
 *
 * 状态[SUBMIT/反馈, SERVICE_REPLY/客服回复, USER_ADD/用户追加, RESOLVED/已解决]
 */

class FeedbackDetailActivity : BaseActivity(), FeedbackDetailContract.View {

    @Inject
    internal lateinit var mPresenter: FeedbackDetailPresenter
    lateinit var feedback : FeedbackList.FeedbackListBean
    lateinit var feedbackDetailItemListAdapter: FeedbackDetailItemListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        setContentView(R.layout.activity_feedback_detail)
    }
    override fun initData() {
        title.text = getString(R.string.feedback)
        feedback = intent.getParcelableExtra("feedback")
        feedbackDetailItemListAdapter = FeedbackDetailItemListAdapter(arrayListOf())
        recyclerView.adapter = feedbackDetailItemListAdapter
        getFeedbackDetail()
        tvReply.setOnClickListener {
            startActivityForResult(Intent(this, FeedbackAddActivity::class.java).putExtra("feedback", feedback), 1)
        }
        tvClose.setOnClickListener {
            showProgressDialog()
            mPresenter.feedbackResolved(feedback.id, SpUtil.getString(this, ConstantValue.userId, "")!!)
        }
        feedbackDetailItemListAdapter.setOnItemClickListener { adapter, view, position ->
            if (feedbackDetailItemListAdapter.data[position].imageList != null && feedbackDetailItemListAdapter.data[position].imageList.size != 0) {
                showImage(feedbackDetailItemListAdapter.data[position].imageList.toMutableList())
            }
        }
        if (feedback.replayListBean.size > 1 && !feedback.status.equals("RESOLVED")) {
            cvAnswer.visibility = View.VISIBLE
            tvAnserTime.text = feedback.replayListBean.last().createDate
        }
        if (feedback.status.equals("RESOLVED")) {
            cvFixed.visibility = View.VISIBLE
            tvFixed.text = feedback.resolvedDate
            llOperate.visibility = View.GONE
        }
    }

    override fun closeFeedback() {
        finish()
    }


    var bottomSheet: BottomSheet? = null
    fun showImage(imageList : MutableList<String>) {
        KLog.i(imageList)
        val maskView = LayoutInflater.from(this).inflate(R.layout.feedback_show_image_layout, null)
        val builder = BottomSheet.Builder(this, true, 0xfff5f5f5.toInt())
        builder.setApplyTopPadding(false)
        val recyclerViewImage = maskView.findViewById<RecyclerView>(R.id.recyclerViewImage)
        recyclerViewImage.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        recyclerViewImage.addItemDecoration(FeedbackChooseImageItemDecoration(UIUtils.dip2px(15f, this)))
        var feedbackImageAdapter = FeedbackImageAdapter(imageList)
        feedbackImageAdapter.setOnItemClickListener { adapter, view, position ->
            var list = arrayListOf<ImageUrl>()
            imageList.forEach {
                list.add(ImageUrl("http://confidantop.qlink.mobi" + it))
            }
            GPreviewBuilder.from(this) //activity实例必须
                    .setData(list) //集合
                    .setCurrentIndex(position)
                    .setSingleFling(false) //是否在黑屏区域点击返回
                    .setDrag(false) //是否禁用图片拖拽返回
                    .setType(GPreviewBuilder.IndicatorType.Dot) //指示器类型
                    .start() //启动

        }
        recyclerViewImage.adapter = feedbackImageAdapter
        builder.setCustomView(maskView)
        bottomSheet = builder.create()
        bottomSheet!!.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            feedbackDetailItemListAdapter.addData(data!!.getParcelableExtra<FeedbackList.ReplayList>("reply"))
        }
    }

    fun getFeedbackDetail() {
        tvNumber.text = feedback.number
        tvScenario.text = feedback.scenario
        tvType.text = feedback.type
        var replyListBean = FeedbackList.ReplayList()
        replyListBean.content = feedback.question
        replyListBean.imageList = feedback.imageList
        replyListBean.type = feedback.type
        replyListBean.createDate = feedback.createDate
        replyListBean.email = feedback.email
        replyListBean.userName = SpUtil.getString(this, ConstantValue.username, "")
        if (feedback.replayListBean == null) {
            feedback.replayListBean = arrayListOf()
        }
        feedback.replayListBean.add(0, replyListBean)
        feedbackDetailItemListAdapter.setNewData(feedback.replayListBean)
    }

    override fun setupActivityComponent() {
       DaggerFeedbackDetailComponent
               .builder()
               .appComponent((application as AppConfig).applicationComponent)
               .feedbackDetailModule(FeedbackDetailModule(this))
               .build()
               .inject(this)
    }
    override fun setPresenter(presenter: FeedbackDetailContract.FeedbackDetailContractPresenter) {
            mPresenter = presenter as FeedbackDetailPresenter
        }

    override fun showProgressDialog() {
        progressDialog.show()
    }

    override fun closeProgressDialog() {
        progressDialog.hide()
    }

}