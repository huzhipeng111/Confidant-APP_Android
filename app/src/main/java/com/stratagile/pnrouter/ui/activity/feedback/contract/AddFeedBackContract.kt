package com.stratagile.pnrouter.ui.activity.feedback.contract

import com.stratagile.pnrouter.ui.activity.base.BasePresenter
import com.stratagile.pnrouter.ui.activity.base.BaseView
/**
 * @author hzp
 * @Package The contract for AddFeedBackActivity
 * @Description: $description
 * @date 2020/06/08 15:57:42
 */
interface AddFeedBackContract {
    interface View : BaseView<AddFeedBackContractPresenter> {
        /**
         *
         */
        fun showProgressDialog()

        /**
         *
         */
        fun closeProgressDialog()


        fun submitBack()
    }

    interface AddFeedBackContractPresenter : BasePresenter {
//        /**
//         *
//         */
//        fun getBusinessInfo(map : Map)
    }
}