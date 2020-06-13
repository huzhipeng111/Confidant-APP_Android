package com.stratagile.pnrouter.ui.activity.main.contract

import com.stratagile.pnrouter.entity.ActiveList
import com.stratagile.pnrouter.ui.activity.base.BasePresenter
import com.stratagile.pnrouter.ui.activity.base.BaseView
/**
 * @author hzp
 * @Package The contract for ActiveListActivity
 * @Description: $description
 * @date 2020/05/19 18:03:37
 */
interface ActiveListContract {
    interface View : BaseView<ActiveListContractPresenter> {
        /**
         *
         */
        fun showProgressDialog()

        /**
         *
         */
        fun closeProgressDialog()

        fun setActiveList(activeList: ActiveList)
    }

    interface ActiveListContractPresenter : BasePresenter {
//        /**
//         *
//         */
//        fun getBusinessInfo(map : Map)
    }
}