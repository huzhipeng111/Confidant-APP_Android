package com.stratagile.pnrouter.ui.activity.feedback.presenter
import android.support.annotation.NonNull
import com.stratagile.pnrouter.data.api.HttpAPIWrapper
import com.stratagile.pnrouter.ui.activity.feedback.contract.FeedbackChooseContract
import com.stratagile.pnrouter.ui.activity.feedback.FeedbackChooseActivity
import javax.inject.Inject
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.feedback
 * @Description: presenter of FeedbackChooseActivity
 * @date 2020/06/08 17:26:18
 */
class FeedbackChoosePresenter @Inject
constructor(internal var httpAPIWrapper: HttpAPIWrapper, private val mView: FeedbackChooseContract.View) : FeedbackChooseContract.FeedbackChooseContractPresenter {

    private val mCompositeDisposable: CompositeDisposable

    init {
        mCompositeDisposable = CompositeDisposable()
    }

    override fun subscribe() {

    }

    fun getFeedbackType() {
        var infoMap = hashMapOf<String, String>()
        infoMap.put("dictType", "app_dict")
        mCompositeDisposable.add(httpAPIWrapper.getDict(infoMap).subscribe({
            mView.setAppDict(it)
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