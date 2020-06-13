package com.stratagile.pnrouter.data.api

import com.stratagile.pnrouter.entity.*
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*


/**
 * Created by hu on 2017/5/16.
 */

interface HttpApi {
    @POST(API.url_post_file)
    @Multipart
    //@Part("filename") map: RequestBody,
    //@Part head : MultipartBody.Part
    fun upLoad(@Part file: MultipartBody.Part): Observable<BaseBackA>

    @POST(API.DOT_URL_DEVOLP)
    @Headers("Content-Type: application/x-www-form-urlencoded", "Accept: application/json")
    fun uLogStr(@QueryMap map : Map<String, String>): Observable<BaseBackA>

    @POST(API.url_active_list)
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun getActiveList(@Body map : RequestBody): Observable<ActiveList>

    @POST(API.url_appdict)
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun getDict(@Body map : RequestBody): Observable<AppDict>

    @POST(API.url_feedbackList)
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun getFeedbackList(@Body map : RequestBody): Observable<FeedbackList>

    @POST(API.url_feedback_resolved)
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun feedbackResolved(@Body map : RequestBody): Observable<BaseBackA>

    @POST(API.url_submit)
    @Multipart
    fun submit(@Part() parts : List<MultipartBody.Part>, @Part("scenario") scenario : RequestBody, @Part("type") type : RequestBody, @Part("userId") userId : RequestBody, @Part("userName") userName : RequestBody, @Part("publicKey") publicKey : RequestBody, @Part("qrCode") qrCode : RequestBody, @Part("email") email : RequestBody, @Part("question") question : RequestBody): Observable<BaseBackA>

    @POST(API.url_feedback_add)
    @Multipart
    fun feedbackAdd(@Part() parts : List<MultipartBody.Part>, @Part("feedbackId") feedbackId : RequestBody, @Part("userId") userId : RequestBody, @Part("userName") userName : RequestBody, @Part("publicKey") publicKey : RequestBody, @Part("qrCode") qrCode : RequestBody, @Part("email") email : RequestBody, @Part("content") question : RequestBody): Observable<FeedbackAdd>

    @POST(API.url_submit)
    @Multipart
    fun submit2(@Part() parts : List<MultipartBody.Part>, @PartMap map : RequestBody): Observable<BaseBackA>
}
