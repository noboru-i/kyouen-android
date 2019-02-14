package hm.orz.chaos114.android.tumekyouen.network

import hm.orz.chaos114.android.tumekyouen.model.AddAllResponse
import hm.orz.chaos114.android.tumekyouen.model.LoginResult
import io.reactivex.Maybe
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TumeKyouenService {
    @FormUrlEncoded
    @POST("/page/api_login")
    fun login(@Field("token") token: String,
              @Field("token_secret") tokenSecret: String): Single<LoginResult>

    @FormUrlEncoded
    @POST("/page/add_all")
    fun addAll(@Field("data") data: String): Single<AddAllResponse>

    @FormUrlEncoded
    @POST("/page/add")
    fun add(@Field("stageNo") stageNo: Int): Maybe<Void>

    @GET("/kyouen/get")
    fun getStage(@Query("stageNo") stageNo: Int): Single<Response<ResponseBody>>

    @FormUrlEncoded
    @POST("/kyouen/regist")
    fun postStage(@Field("data") data: String): Single<Response<ResponseBody>>
}
