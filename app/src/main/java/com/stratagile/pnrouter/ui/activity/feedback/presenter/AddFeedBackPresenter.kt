package com.stratagile.pnrouter.ui.activity.feedback.presenter
import android.support.annotation.NonNull
import com.stratagile.pnrouter.data.api.HttpAPIWrapper
import com.stratagile.pnrouter.ui.activity.feedback.contract.AddFeedBackContract
import com.stratagile.pnrouter.ui.activity.feedback.AddFeedBackActivity
import javax.inject.Inject
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Part

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.feedback
 * @Description: presenter of AddFeedBackActivity
 * @date 2020/06/08 15:57:42
 */
class AddFeedBackPresenter @Inject
constructor(internal var httpAPIWrapper: HttpAPIWrapper, private val mView: AddFeedBackContract.View) : AddFeedBackContract.AddFeedBackContractPresenter {

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

    fun subMit(@Part("feedbackImages") parts : List<MultipartBody.Part>, @Part("scenario") scenario : String, @Part("type") type : String, @Part("userId") userId : String, @Part("userName") userName : String, @Part("publicKey") publicKey : String, @Part("qrCode") qrCode : String, @Part("email") email : String, @Part("question") question : String) {
        mCompositeDisposable.add(httpAPIWrapper.submit(parts, RequestBody.create(MediaType.parse("text/plain"),scenario), RequestBody.create(MediaType.parse("text/plain"),type), RequestBody.create(MediaType.parse("text/plain"),userId), RequestBody.create(MediaType.parse("text/plain"),userName), RequestBody.create(MediaType.parse("text/plain"),publicKey), RequestBody.create(MediaType.parse("text/plain"),qrCode), RequestBody.create(MediaType.parse("text/plain"),email), RequestBody.create(MediaType.parse("text/plain"),question)).subscribe({
            mView.submitBack()
        }, {

        }, {

        }))
    }

    fun subMit2(@Part("feedbackImages") parts : List<MultipartBody.Part>, map: Map<String, String>) {
        mCompositeDisposable.add(httpAPIWrapper.submit2(parts, map).subscribe({
            mView.submitBack()
        }, {

        }, {

        }))
    }

}