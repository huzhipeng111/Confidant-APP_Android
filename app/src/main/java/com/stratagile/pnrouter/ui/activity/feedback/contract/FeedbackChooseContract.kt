package com.stratagile.pnrouter.ui.activity.feedback.contract

import com.stratagile.pnrouter.entity.AppDict
import com.stratagile.pnrouter.ui.activity.base.BasePresenter
import com.stratagile.pnrouter.ui.activity.base.BaseView
/**
 * @author hzp
 * @Package The contract for FeedbackChooseActivity
 * @Description: $description
 * @date 2020/06/08 17:26:18
 */
interface FeedbackChooseContract {
    interface View : BaseView<FeedbackChooseContractPresenter> {
        /**
         *
         */
        fun showProgressDialog()

        /**
         *
         */
        fun closeProgressDialog()

        fun setAppDict(appDict: AppDict)
    }

    interface FeedbackChooseContractPresenter : BasePresenter {
//        /**
//         *
//         */
//        fun getBusinessInfo(map : Map)
    }
}