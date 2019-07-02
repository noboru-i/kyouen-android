package hm.orz.chaos114.android.tumekyouen.network

import hm.orz.chaos114.android.tumekyouen.model.LoginResult
import hm.orz.chaos114.android.tumekyouen.network.models.LoginParam
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface TumeKyouenV2Service {
    @POST("/v2/users/login")
    fun login(@Body loginParam: LoginParam): Single<LoginResult>
}
