package com.stratagile.pnrouter.ui.activity.conversation.presenter
import android.support.annotation.NonNull
import com.stratagile.pnrouter.data.api.HttpAPIWrapper
import com.stratagile.pnrouter.ui.activity.conversation.contract.ConversationContract
import com.stratagile.pnrouter.ui.activity.conversation.ConversationActivity
import javax.inject.Inject
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.conversation
 * @Description: presenter of ConversationActivity
 * @date 2018/09/13 16:38:48
 */
class ConversationPresenter @Inject
constructor(internal var httpAPIWrapper: HttpAPIWrapper, private val mView: ConversationContract.View) : ConversationContract.ConversationContractPresenter {

    private val mCompositeDisposable: CompositeDisposable

    init {
        mCompositeDisposable = CompositeDisposable()
    }

    override fun subscribe() {

    }

    override fun unsubscribe() {
        if (!mCompositeDisposable.isDisposed) {
            mCompositeDisposable.dispose()
        }
    }
}