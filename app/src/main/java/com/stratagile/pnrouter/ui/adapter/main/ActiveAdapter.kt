package com.stratagile.pnrouter.ui.adapter.main

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.socks.library.KLog
import com.stratagile.pnrouter.R
import com.stratagile.pnrouter.db.UserEntity
import com.stratagile.pnrouter.entity.ActiveList
import com.stratagile.pnrouter.entity.JGroupUserPullRsp
import com.stratagile.pnrouter.utils.Base58
import com.stratagile.pnrouter.utils.RxEncodeTool
import com.stratagile.pnrouter.view.ImageButtonWithText

class ActiveAdapter (arrayList: MutableList<ActiveList.MessageListBean>) : BaseQuickAdapter<ActiveList.MessageListBean, BaseViewHolder>(R.layout.item_active, arrayList) {
    override fun convert(helper: BaseViewHolder?, item: ActiveList.MessageListBean?, payloads: MutableList<Any>) {
        KLog.i("")
    }

    override fun convert(helper: BaseViewHolder, item: ActiveList.MessageListBean) {
        helper.setText(R.id.tvTitle, item.title)
        if (helper.layoutPosition < 9) {
            helper.setText(R.id.tvPos, "0" + (helper.layoutPosition + 1).toString())
        } else {
            helper.setText(R.id.tvPos, (helper.layoutPosition + 1).toString())
        }
        helper.setText(R.id.tvContent, item.content)
        helper.setText(R.id.tvTime, item.createDate)
    }

}