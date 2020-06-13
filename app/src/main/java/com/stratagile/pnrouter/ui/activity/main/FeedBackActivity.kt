package com.stratagile.pnrouter.ui.activity.main

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.pawegio.kandroid.inflateLayout
import com.stratagile.pnrouter.R

import com.stratagile.pnrouter.application.AppConfig
import com.stratagile.pnrouter.base.BaseActivity
import com.stratagile.pnrouter.constant.ConstantValue
import com.stratagile.pnrouter.entity.FeedbackList
import com.stratagile.pnrouter.ui.activity.feedback.AddFeedBackActivity
import com.stratagile.pnrouter.ui.activity.feedback.FeedbackDetailActivity
import com.stratagile.pnrouter.ui.activity.main.component.DaggerFeedBackComponent
import com.stratagile.pnrouter.ui.activity.main.contract.FeedBackContract
import com.stratagile.pnrouter.ui.activity.main.module.FeedBackModule
import com.stratagile.pnrouter.ui.activity.main.presenter.FeedBackPresenter
import com.stratagile.pnrouter.ui.adapter.feedback.FeedbackListAdapter
import com.stratagile.pnrouter.ui.adapter.feedback.FeedbackListItemDecoration
import com.stratagile.pnrouter.utils.SpUtil
import com.stratagile.pnrouter.utils.UIUtils
import kotlinx.android.synthetic.main.activity_feed_back.*
import java.util.ArrayList

import javax.inject.Inject;

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.main
 * @Description: $description
 * @date 2020/06/08 15:27:19
 */

class FeedBackActivity : BaseActivity(), FeedBackContract.View {

    @Inject
    internal lateinit var mPresenter: FeedBackPresenter
    lateinit var feedbackListAdapter: FeedbackListAdapter
    var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        setContentView(R.layout.activity_feed_back)
        feedbackListAdapter = FeedbackListAdapter(arrayListOf())
        recyclerView.adapter = feedbackListAdapter
        recyclerView.addItemDecoration(FeedbackListItemDecoration(UIUtils.dip2px(15f, this)))
        feedbackListAdapter.setEnableLoadMore(true)
        feedbackListAdapter.setOnItemClickListener { adapter, view, position ->
            var intent = Intent(this, FeedbackDetailActivity::class.java)
            intent.putExtra("feedback", feedbackListAdapter.data[position])
            startActivity(intent)
        }

    }
    override fun initData() {
        title.text = getString(R.string.feedback)
        currentPage = 0
        getFeedbackList()
        feedbackListAdapter.emptyView = inflateLayout(R.layout.layout_filelist_empty, null, false)
        UIUtils.configSwipeRefreshLayoutColors(refreshLayout)
        feedbackListAdapter.setOnLoadMoreListener({
            getFeedbackList()
        }, recyclerView)
        refreshLayout.setOnRefreshListener {
            refreshLayout.isRefreshing = false
            currentPage = 0
            getFeedbackList()
        }
    }

    override fun setupActivityComponent() {
       DaggerFeedBackComponent
               .builder()
               .appComponent((application as AppConfig).applicationComponent)
               .feedBackModule(FeedBackModule(this))
               .build()
               .inject(this)
    }
    override fun setPresenter(presenter: FeedBackContract.FeedBackContractPresenter) {
            mPresenter = presenter as FeedBackPresenter
        }

    override fun showProgressDialog() {
        progressDialog.show()
    }

    override fun closeProgressDialog() {
        progressDialog.hide()
    }

    fun getFeedbackList() {
        currentPage++
        var infoMap = hashMapOf<String, String>()
        infoMap.put("userId", SpUtil.getString(this, ConstantValue.userId, "")!!)
        infoMap.put("page", currentPage.toString())
        infoMap.put("size", "20")
        mPresenter.getFeedbackList(infoMap, currentPage)
    }

    override fun setFeedbackList(feedbackList: FeedbackList, currentPage: Int) {
        if (currentPage == 1) {
            feedbackListAdapter.setNewData(ArrayList())
        }
        feedbackListAdapter.addData(feedbackList.feedbackList)
        if (currentPage != 1) {
            feedbackListAdapter.loadMoreComplete()
        }
        if (feedbackList.feedbackList.size == 0) {
            feedbackListAdapter.loadMoreEnd(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.add_feedback, menu)
        return true
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.feedback -> {
                startActivityForResult(Intent(this, AddFeedBackActivity::class.java), 0)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            currentPage = 0
            getFeedbackList()
        }
    }

}