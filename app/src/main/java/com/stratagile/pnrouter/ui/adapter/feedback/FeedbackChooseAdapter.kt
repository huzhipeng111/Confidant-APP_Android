package com.stratagile.pnrouter.ui.adapter.feedback

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.socks.library.KLog
import com.stratagile.pnrouter.R
import com.stratagile.pnrouter.entity.ShareBean
import com.stratagile.pnrouter.utils.RxEncodeTool

class FeedbackChooseAdapter(arrayList: ArrayList<String>) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.layout_feedback_choose_item, arrayList) {
    var selectedItem = 0
    override fun convert(helper: BaseViewHolder?, item: String?, payloads: MutableList<Any>) {
        KLog.i("")
    }
    override fun convert(helper: BaseViewHolder, item: String) {
        helper.setText(R.id.tvContent, item)
        if (helper.layoutPosition == selectedItem) {
            helper.setVisible(R.id.ivSelect, true)
        } else {
            helper.setVisible(R.id.ivSelect, false)
        }
    }

}