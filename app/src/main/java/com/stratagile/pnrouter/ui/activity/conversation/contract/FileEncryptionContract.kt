package com.stratagile.pnrouter.ui.activity.conversation.contract

import com.stratagile.pnrouter.ui.activity.base.BasePresenter
import com.stratagile.pnrouter.ui.activity.base.BaseView
/**
 * @author zl
 * @Package The contract for FileEncryptionFragment
 * @Description: $description
 * @date 2019/11/20 10:12:15
 */
interface FileEncryptionContract {
    interface View : BaseView<FileEncryptionContractPresenter> {
        /**
         *
         */
        fun showProgressDialog()

        /**
         *
         */
        fun closeProgressDialog()
    }

    interface FileEncryptionContractPresenter : BasePresenter {
//        /**
//         *
//         */
//        fun getBusinessInfo(map : Map)
    }
}