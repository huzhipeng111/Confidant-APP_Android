package com.stratagile.pnrouter.ui.activity.feedback.contract

import com.stratagile.pnrouter.ui.activity.base.BasePresenter
import com.stratagile.pnrouter.ui.activity.base.BaseView
/**
 * @author hzp
 * @Package The contract for FeedbackDetailActivity
 * @Description: $description
 * @date 2020/06/12 09:37:41
 */
interface FeedbackDetailContract {
    interface View : BaseView<FeedbackDetailContractPresenter> {
        /**
         *
         */
        fun showProgressDialog()

        /**
         *
         */
        fun closeProgressDialog()

        fun closeFeedback()
    }

    interface FeedbackDetailContractPresenter : BasePresenter {
//        /**
//         *
//         */
//        fun getBusinessInfo(map : Map)
    }
}