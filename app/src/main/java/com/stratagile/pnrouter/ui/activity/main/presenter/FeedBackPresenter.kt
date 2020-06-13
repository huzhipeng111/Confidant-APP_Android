package com.stratagile.pnrouter.ui.activity.main.presenter
import android.support.annotation.NonNull
import com.stratagile.pnrouter.data.api.HttpAPIWrapper
import com.stratagile.pnrouter.ui.activity.main.contract.FeedBackContract
import com.stratagile.pnrouter.ui.activity.main.FeedBackActivity
import javax.inject.Inject
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.main
 * @Description: presenter of FeedBackActivity
 * @date 2020/06/08 15:27:19
 */
class FeedBackPresenter @Inject
constructor(internal var httpAPIWrapper: HttpAPIWrapper, private val mView: FeedBackContract.View) : FeedBackContract.FeedBackContractPresenter {

    private val mCompositeDisposable: CompositeDisposable

    init {
        mCompositeDisposable = CompositeDisposable()
    }

    override fun subscribe() {

    }

    fun getFeedbackList(map : HashMap<String, String>, currentPage : Int) {
        mCompositeDisposable.add(httpAPIWrapper.getFeedbackList(map).subscribe({
            mView.setFeedbackList(it, currentPage)
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