package com.stratagile.pnrouter.data.api


import com.alibaba.fastjson.JSONObject
import com.socks.library.KLog
import com.stratagile.pnrouter.entity.*
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Part
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*
import javax.inject.Inject

/**
 * @author hu
 * @desc 对Request实体(不执行)在执行时所调度的线程，以及得到ResponseBody后根据retCode对Result进行进一步处理
 * @date 2017/5/31 16:56
 */
class HttpAPIWrapper @Inject constructor(private val mHttpAPI: HttpApi) {

    //, head : RequestBody
    //map: MultipartBody.Part,
    fun upLoadFile(file : MultipartBody.Part): Observable<BaseBackA> {
        //head,
        return wrapper(mHttpAPI.upLoad(file)).compose(ObservableTransformer {
            it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        })
    }

    fun uLogStr(map : Map<String, String>): Observable<BaseBackA> {
        return wrapper(mHttpAPI.uLogStr(map)).compose(ObservableTransformer {
            it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        })
    }

    fun getActiveList(map : Map<String, String>): Observable<ActiveList> {
        return wrapper(mHttpAPI.getActiveList(addParams(map)!!)).compose(ObservableTransformer {
            it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        })
    }

    fun getDict(map : Map<String, String>): Observable<AppDict> {
        return wrapper(mHttpAPI.getDict(addParams(map)!!)).compose(ObservableTransformer {
            it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        })
    }

    fun getFeedbackList(map : Map<String, String>): Observable<FeedbackList> {
        return wrapper(mHttpAPI.getFeedbackList(addParams(map)!!)).compose(ObservableTransformer {
            it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        })
    }

    fun feedbackResolved(map : Map<String, String>): Observable<BaseBackA> {
        return wrapper(mHttpAPI.feedbackResolved(addParams(map)!!)).compose(ObservableTransformer {
            it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        })
    }

    fun submit(@Part("feedbackImages") parts : List<MultipartBody.Part>, @Part("scenario") scenario : RequestBody, @Part("type") type : RequestBody, @Part("userId") userId : RequestBody, @Part("userName") userName : RequestBody, @Part("publicKey") publicKey : RequestBody, @Part("qrCode") qrCode : RequestBody, @Part("email") email : RequestBody, @Part("question") question : RequestBody): Observable<BaseBackA> {
        return wrapper(mHttpAPI.submit(parts, scenario, type, userId, userName, publicKey, qrCode, email, question).compose(ObservableTransformer {
            it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }))
    }

    fun feedbackAdd(@Part("feedbackImages") parts : List<MultipartBody.Part>, @Part("feedbackId") feedbackId : RequestBody, @Part("userId") userId : RequestBody, @Part("userName") userName : RequestBody, @Part("publicKey") publicKey : RequestBody, @Part("qrCode") qrCode : RequestBody, @Part("email") email : RequestBody, @Part("content") question : RequestBody): Observable<FeedbackAdd> {
        return wrapper(mHttpAPI.feedbackAdd(parts, feedbackId, userId, userName, publicKey, qrCode, email, question).compose(ObservableTransformer {
            it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }))
    }

    fun submit2(@Part("feedbackImages") parts : List<MultipartBody.Part>, map : Map<String, String>): Observable<BaseBackA> {
        return wrapper(mHttpAPI.submit2(parts, addParamsNoParams(map)).compose(ObservableTransformer {
            it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }))
    }


    /**
     * 给任何Http的Observable加上通用的线程调度器
     */
    //    private static final ObservableTransformer SCHEDULERS_TRANSFORMER = new ObservableTransformer() {
    //        @Override
    //        public ObservableSource apply(@NonNull Observable upstream) {
    //            return upstream.subscribeOn(Schedulers.io())
    //                    .observeOn(AndroidSchedulers.mainThread());
    //        }
    //    };

    private fun <T : BaseBackA> wrapper(resourceObservable: Observable<T>): Observable<T> {
        return resourceObservable
                .flatMap(Function<T, ObservableSource<out T>> { baseResponse ->
                    KLog.i(baseResponse)
                    Observable.create { e ->
                        if (baseResponse.code != "0") {
                            e.onComplete()
                        } else {
                            e.onNext(baseResponse)
                        }
                    }
                })
                /**
                 * 网络错误： You've encountered a network error!
                 * 请打开网络：Please open your network.
                 * 请求超时：The request has timed out.
                 * 连接失败: Connection failed.
                 * 请求失败： The request has failed.
                 */
                .doOnError { }
    }

    //需要额外的添加其他的参数进去，所以把原有的参数和额外的参数通过这个方法一起添加进去.
    private fun addParams(data: Map<String, String>): RequestBody? {
        val map: MutableMap<String, Any> = HashMap()
        map["params"] = JSONObject.toJSON(data)
        val textType = MediaType.parse("text/plain")
        var bodyStr = JSONObject.toJSON(map).toString()
        KLog.i("参数为:$bodyStr")
        return RequestBody.create(textType, bodyStr)
    }
    //需要额外的添加其他的参数进去，所以把原有的参数和额外的参数通过这个方法一起添加进去.
    private fun addParamsNoParams(data: Map<String, String>): RequestBody {
        val textType = MediaType.parse("text/plain")
        var bodyStr = JSONObject.toJSON(data).toString()
        KLog.i("参数为:$bodyStr")
        return RequestBody.create(textType, bodyStr)
    }


}
