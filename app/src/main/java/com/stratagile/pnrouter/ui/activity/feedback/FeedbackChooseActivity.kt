package com.stratagile.pnrouter.ui.activity.feedback

import android.app.Activity
import android.os.Bundle
import com.socks.library.KLog
import com.stratagile.pnrouter.R

import com.stratagile.pnrouter.application.AppConfig
import com.stratagile.pnrouter.base.BaseActivity
import com.stratagile.pnrouter.entity.AppDict
import com.stratagile.pnrouter.ui.activity.feedback.component.DaggerFeedbackChooseComponent
import com.stratagile.pnrouter.ui.activity.feedback.contract.FeedbackChooseContract
import com.stratagile.pnrouter.ui.activity.feedback.module.FeedbackChooseModule
import com.stratagile.pnrouter.ui.activity.feedback.presenter.FeedbackChoosePresenter
import com.stratagile.pnrouter.ui.adapter.feedback.FeedbackChooseAdapter
import kotlinx.android.synthetic.main.activity_feedback_choose.*
import java.lang.Exception
import java.util.*

import javax.inject.Inject;

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.feedback
 * @Description: $description
 * @date 2020/06/08 17:26:18
 */

class FeedbackChooseActivity : BaseActivity(), FeedbackChooseContract.View {

    @Inject
    internal lateinit var mPresenter: FeedbackChoosePresenter
    lateinit var feedbackChooseAdapter: FeedbackChooseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        setContentView(R.layout.activity_feedback_choose)
    }
    override fun initData() {
        feedbackChooseAdapter = FeedbackChooseAdapter(arrayListOf())
        title.text = intent.getStringExtra("title")
        recyclerView.adapter = feedbackChooseAdapter
        mPresenter.getFeedbackType()
        feedbackChooseAdapter.setOnItemClickListener { adapter, view, position ->
            feedbackChooseAdapter.selectedItem = position
            feedbackChooseAdapter.notifyDataSetChanged()
            intent.putExtra("result", feedbackChooseAdapter.data[position])
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun setAppDict(appDict: AppDict) {
        if (intent.getStringExtra("content").equals("type")) {
            try {
                var list = appDict.dataBean.conFeedbackType.split(",")
                KLog.i(list)
                feedbackChooseAdapter.setNewData(list)
            } catch (e : Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                var list = appDict.dataBean.conFeedbackScenario.split(",")
                KLog.i(list)
                feedbackChooseAdapter.setNewData(list)
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun setupActivityComponent() {
       DaggerFeedbackChooseComponent
               .builder()
               .appComponent((application as AppConfig).applicationComponent)
               .feedbackChooseModule(FeedbackChooseModule(this))
               .build()
               .inject(this)
    }
    override fun setPresenter(presenter: FeedbackChooseContract.FeedbackChooseContractPresenter) {
            mPresenter = presenter as FeedbackChoosePresenter
        }

    override fun showProgressDialog() {
        progressDialog.show()
    }

    override fun closeProgressDialog() {
        progressDialog.hide()
    }

}