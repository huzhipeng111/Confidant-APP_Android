package com.stratagile.pnrouter.ui.activity.main.contract

import com.stratagile.pnrouter.entity.FeedbackList
import com.stratagile.pnrouter.ui.activity.base.BasePresenter
import com.stratagile.pnrouter.ui.activity.base.BaseView
/**
 * @author hzp
 * @Package The contract for FeedBackActivity
 * @Description: $description
 * @date 2020/06/08 15:27:19
 */
interface FeedBackContract {
    interface View : BaseView<FeedBackContractPresenter> {
        /**
         *
         */
        fun showProgressDialog()

        /**
         *
         */
        fun closeProgressDialog()

        fun setFeedbackList(feedbackList: FeedbackList, currentPage : Int)
    }

    interface FeedBackContractPresenter : BasePresenter {
//        /**
//         *
//         */
//        fun getBusinessInfo(map : Map)
    }
}