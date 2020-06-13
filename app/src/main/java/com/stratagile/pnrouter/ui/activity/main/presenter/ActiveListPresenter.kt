package com.stratagile.pnrouter.ui.activity.main.presenter

import com.stratagile.pnrouter.data.api.HttpAPIWrapper
import com.stratagile.pnrouter.ui.activity.main.contract.ActiveListContract
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.main
 * @Description: presenter of ActiveListActivity
 * @date 2020/05/19 18:03:37
 */
class ActiveListPresenter @Inject
constructor(internal var httpAPIWrapper: HttpAPIWrapper, private val mView: ActiveListContract.View) : ActiveListContract.ActiveListContractPresenter {

    private val mCompositeDisposable: CompositeDisposable

    init {
        mCompositeDisposable = CompositeDisposable()
    }

    override fun subscribe() {

    }

    fun getActiveList() {
        var infoMap = hashMapOf<String, String>()
        infoMap.put("page", "1")
        infoMap.put("size", "20")
        mCompositeDisposable.add(httpAPIWrapper.getActiveList(infoMap).subscribe({
            mView.setActiveList(it)
        }, {

        }, {

        }))
    }

    override fun unsubscribe() {
        if (!mCompositeDisposable.isDisposed) {
            mCompositeDisposable.dispose()
        }
    }
}