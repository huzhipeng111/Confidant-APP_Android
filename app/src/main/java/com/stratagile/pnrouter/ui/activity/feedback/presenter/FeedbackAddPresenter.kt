package com.stratagile.pnrouter.ui.activity.feedback.presenter
import android.support.annotation.NonNull
import com.stratagile.pnrouter.data.api.HttpAPIWrapper
import com.stratagile.pnrouter.ui.activity.feedback.contract.FeedbackAddContract
import com.stratagile.pnrouter.ui.activity.feedback.FeedbackAddActivity
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
 * @Description: presenter of FeedbackAddActivity
 * @date 2020/06/12 12:54:37
 */
class FeedbackAddPresenter @Inject
constructor(internal var httpAPIWrapper: HttpAPIWrapper, private val mView: FeedbackAddContract.View) : FeedbackAddContract.FeedbackAddContractPresenter {

    private val mCompositeDisposable: CompositeDisposable

    init {
        mCompositeDisposable = CompositeDisposable()
    }

    override fun subscribe() {

    }

    fun feeedbackAdd(@Part("feedbackImages") parts : List<MultipartBody.Part>, @Part("feedbackId") feedbackId : String, @Part("userId") userId : String, @Part("userName") userName : String, @Part("publicKey") publicKey : String, @Part("qrCode") qrCode : String, @Part("email") email : String, @Part("question") question : String) {
        mCompositeDisposable.add(httpAPIWrapper.feedbackAdd(parts, RequestBody.create(MediaType.parse("text/plain"),feedbackId), RequestBody.create(MediaType.parse("text/plain"),userId), RequestBody.create(MediaType.parse("text/plain"),userName), RequestBody.create(MediaType.parse("text/plain"),publicKey), RequestBody.create(MediaType.parse("text/plain"),qrCode), RequestBody.create(MediaType.parse("text/plain"),email), RequestBody.create(MediaType.parse("text/plain"),question)).subscribe({
            mView.submitBack(it)
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