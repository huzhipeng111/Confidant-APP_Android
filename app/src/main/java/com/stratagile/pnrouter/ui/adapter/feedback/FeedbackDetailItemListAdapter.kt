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

class FeedbackDetailItemListAdapter(arrayList: ArrayList<FeedbackList.ReplayList>) : BaseQuickAdapter<FeedbackList.ReplayList, BaseViewHolder>(R.layout.layout_feedback_detail_item_item, arrayList) {
    var selectedItem = 0
    override fun convert(helper: BaseViewHolder?, item: FeedbackList.ReplayList?, payloads: MutableList<Any>) {
        KLog.i("")
    }
    override fun convert(helper: BaseViewHolder, item: FeedbackList.ReplayList) {
        helper.setText(R.id.tvUserName, item.userName)
        helper.setText(R.id.tvTime, item.createDate)
        helper.setText(R.id.tvQuestion, item.content)
        if (item.imageList == null || item.imageList.size == 0) {
            helper.setVisible(R.id.tvIvAmount, false)
            helper.setVisible(R.id.iv, false)
        } else {
            helper.setText(R.id.tvIvAmount, item.imageList.size.toString())
            helper.setVisible(R.id.tvIvAmount, true)
            helper.setVisible(R.id.iv, true)
        }
        if (item.type.equals("SERVICE_REPLY")) {
            helper.setVisible(R.id.tvLeft, true)
            helper.setBackgroundColor(R.id.llParent, mContext.resources.getColor(R.color.color_FAF8FF))
        } else {
            helper.setVisible(R.id.tvLeft, false)
            helper.setBackgroundColor(R.id.llParent, mContext.resources.getColor(R.color.white))
        }
    }

}