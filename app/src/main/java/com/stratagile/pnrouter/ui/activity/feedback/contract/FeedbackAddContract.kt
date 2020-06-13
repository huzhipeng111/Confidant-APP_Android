package com.stratagile.pnrouter.ui.activity.feedback.contract

import com.stratagile.pnrouter.entity.FeedbackAdd
import com.stratagile.pnrouter.ui.activity.base.BasePresenter
import com.stratagile.pnrouter.ui.activity.base.BaseView
/**
 * @author hzp
 * @Package The contract for FeedbackAddActivity
 * @Description: $description
 * @date 2020/06/12 12:54:37
 */
interface FeedbackAddContract {
    interface View : BaseView<FeedbackAddContractPresenter> {
        /**
         *
         */
        fun showProgressDialog()

        /**
         *
         */
        fun closeProgressDialog()

        fun submitBack(feedbackadd : FeedbackAdd)


    }

    interface FeedbackAddContractPresenter : BasePresenter {
//        /**
//         *
//         */
//        fun getBusinessInfo(map : Map)
    }
}