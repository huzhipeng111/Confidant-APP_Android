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

class FeedbackImageAdapter(arrayList: MutableList<String>) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.layout_feedback_image_item, arrayList) {
    override fun convert(helper: BaseViewHolder?, item: String?, payloads: MutableList<Any>) {
        KLog.i("")
    }
    override fun convert(helper: BaseViewHolder, item: String) {
        var imageView = helper.getView<ImageView>(R.id.image)
        Glide.with(mContext)
                .load("http://confidantop.qlink.mobi" + item)
                .into(imageView)
    }

}