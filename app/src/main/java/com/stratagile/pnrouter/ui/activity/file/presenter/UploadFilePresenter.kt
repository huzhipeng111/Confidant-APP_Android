package com.stratagile.pnrouter.ui.activity.file.presenter
import android.support.annotation.NonNull
import com.stratagile.pnrouter.data.api.HttpAPIWrapper
import com.stratagile.pnrouter.ui.activity.file.contract.UploadFileContract
import com.stratagile.pnrouter.ui.activity.file.UploadFileActivity
import javax.inject.Inject
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.file
 * @Description: presenter of UploadFileActivity
 * @date 2019/01/25 14:59:07
 */
class UploadFilePresenter @Inject
constructor(internal var httpAPIWrapper: HttpAPIWrapper, private val mView: UploadFileContract.View) : UploadFileContract.UploadFileContractPresenter {

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