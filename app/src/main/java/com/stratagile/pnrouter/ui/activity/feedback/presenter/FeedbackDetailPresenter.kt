package com.stratagile.pnrouter.ui.activity.feedback.presenter
import android.support.annotation.NonNull
import com.stratagile.pnrouter.data.api.HttpAPIWrapper
import com.stratagile.pnrouter.ui.activity.feedback.contract.FeedbackDetailContract
import com.stratagile.pnrouter.ui.activity.feedback.FeedbackDetailActivity
import javax.inject.Inject
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.feedback
 * @Description: presenter of FeedbackDetailActivity
 * @date 2020/06/12 09:37:41
 */
class FeedbackDetailPresenter @Inject
constructor(internal var httpAPIWrapper: HttpAPIWrapper, private val mView: FeedbackDetailContract.View) : FeedbackDetailContract.FeedbackDetailContractPresenter {

    private val mCompositeDisposable: CompositeDisposable

    init {
        mCompositeDisposable = CompositeDisposable()
    }

    override fun subscribe() {

    }

    fun feedbackResolved(feedbackId : String, userId : String) {
        var infoMap = hashMapOf<String, String>()
        infoMap.put("feedbackId", feedbackId)
        infoMap.put("userId", userId)
        mCompositeDisposable.add(httpAPIWrapper.feedbackResolved(infoMap).subscribe({
            mView.closeProgressDialog()
            mView.closeFeedback()
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