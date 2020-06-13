package com.stratagile.pnrouter.ui.adapter.feedback

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.socks.library.KLog
import com.stratagile.pnrouter.R
import com.stratagile.pnrouter.entity.ShareBean
import com.stratagile.pnrouter.utils.RxEncodeTool
import java.io.File

class FeedbackChooseImageAdapter(arrayList: ArrayList<FeedbackSelectImageBean>) : BaseQuickAdapter<FeedbackSelectImageBean, BaseViewHolder>(R.layout.layout_feedback_choose_image_item, arrayList) {
    override fun convert(helper: BaseViewHolder?, item: FeedbackSelectImageBean?, payloads: MutableList<Any>) {
        KLog.i("")
    }
    override fun convert(helper: BaseViewHolder, item: FeedbackSelectImageBean) {
        helper.addOnClickListener(R.id.ivContent)
        helper.addOnClickListener(R.id.ivRemove)
        var imageView = helper.getView<ImageView>(R.id.ivContent)

        if (item.filePath != null) {
            helper.setVisible(R.id.ivRemove, true)
            Glide.with(mContext)
                    .load(File(item.filePath))
                    .into(imageView)
        } else {
            helper.setVisible(R.id.ivRemove, false)
            imageView.setImageResource(R.mipmap.tabbar_feedback_add)
        }
    }

}