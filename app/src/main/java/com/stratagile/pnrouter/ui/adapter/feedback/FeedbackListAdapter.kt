package com.stratagile.pnrouter.ui.adapter.feedback

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.socks.library.KLog
import com.stratagile.pnrouter.R
import com.stratagile.pnrouter.entity.FeedbackList
import com.stratagile.pnrouter.entity.ShareBean
import com.stratagile.pnrouter.utils.RxEncodeTool

class FeedbackListAdapter(arrayList: ArrayList<FeedbackList.FeedbackListBean>) : BaseQuickAdapter<FeedbackList.FeedbackListBean, BaseViewHolder>(R.layout.layout_feedback_list_item, arrayList) {
    var selectedItem = 0
    override fun convert(helper: BaseViewHolder?, item: FeedbackList.FeedbackListBean?, payloads: MutableList<Any>) {
        KLog.i("")
    }
    override fun convert(helper: BaseViewHolder, item: FeedbackList.FeedbackListBean) {
        helper.setText(R.id.scenario, item.scenario)
        helper.setText(R.id.type, item.type)
        helper.setText(R.id.tvUserName, item.number)
        helper.setText(R.id.tvStatus, item.status)
        helper.setText(R.id.tvTime, item.createDate)
    }

}